/**
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.ca.server.impl.local;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.epics.ca.client.AccessRights;
import org.epics.ca.client.Channel;
import org.epics.ca.client.ChannelAccessFactory;
import org.epics.ca.client.ChannelArray;
import org.epics.ca.client.ChannelArrayRequester;
import org.epics.ca.client.ChannelFind;
import org.epics.ca.client.ChannelFindRequester;
import org.epics.ca.client.ChannelGet;
import org.epics.ca.client.ChannelGetRequester;
import org.epics.ca.client.ChannelProcess;
import org.epics.ca.client.ChannelProcessRequester;
import org.epics.ca.client.ChannelProvider;
import org.epics.ca.client.ChannelPut;
import org.epics.ca.client.ChannelPutGet;
import org.epics.ca.client.ChannelPutGetRequester;
import org.epics.ca.client.ChannelPutRequester;
import org.epics.ca.client.ChannelRPC;
import org.epics.ca.client.ChannelRPCRequester;
import org.epics.ca.client.ChannelRequester;
import org.epics.ca.client.GetFieldRequester;
import org.epics.ca.client.Query;
import org.epics.ca.client.QueryRequester;
import org.epics.ca.client.Channel.ConnectionState;
import org.epics.ca.server.ChannelProcessor;
import org.epics.ca.server.ChannelProcessorProvider;
import org.epics.ca.server.ChannelProcessorRequester;
import org.epics.ca.server.ChannelServer;
import org.epics.pvData.factory.ConvertFactory;
import org.epics.pvData.factory.PVDataFactory;
import org.epics.pvData.factory.PVDatabaseFactory;
import org.epics.pvData.factory.StatusFactory;
import org.epics.pvData.misc.BitSet;
import org.epics.pvData.monitor.Monitor;
import org.epics.pvData.monitor.MonitorFactory;
import org.epics.pvData.monitor.MonitorRequester;
import org.epics.pvData.pv.*;
import org.epics.pvData.pv.MessageType;
import org.epics.pvData.pv.PVArray;
import org.epics.pvData.pv.PVBoolean;
import org.epics.pvData.pv.PVDataCreate;
import org.epics.pvData.pv.PVDatabase;
import org.epics.pvData.pv.PVField;
import org.epics.pvData.pv.PVRecord;
import org.epics.pvData.pv.PVString;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.PVStructureArray;
import org.epics.pvData.pv.Scalar;
import org.epics.pvData.pv.ScalarType;
import org.epics.pvData.pv.Status;
import org.epics.pvData.pv.StatusCreate;
import org.epics.pvData.pv.Type;
import org.epics.pvData.pv.Status.StatusType;
import org.epics.pvData.pvCopy.PVCopy;
import org.epics.pvData.pvCopy.PVCopyFactory;

/**
 * Factory and implementation of local channel access, i.e. channel access that
 * accesses database records in the local pvDatabase..
 * User callbacks are called with the appropriate record locked except for
 * 1) all methods of ChannelRequester, 2) all methods of ChannelFieldGroupListener,
 * and 3) ChannelRequester.requestDone
 * @author mrk
 *
 */
public class ChannelServerFactory  {
    
    /**
     * Register. This is called by ChannelAccessFactory.
     */
    static public void register() {
        ChannelAccessFactory.registerChannelProvider(channelServer);
    }
    
    static public ChannelServer getChannelServer() {
        return channelServer;
    }
    
    private static final ChannelServerLocal channelServer = new ChannelServerLocal();
    private static final ChannelFind channelFind = new ChannelFindLocal();
    private static final String providerName = "local";
    private static final PVDatabase pvDatabase = PVDatabaseFactory.getMaster();
    private static final PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
    private static final Convert convert = ConvertFactory.getConvert();
    private static final StatusCreate statusCreate = StatusFactory.getStatusCreate();
    private static final Status okStatus = statusCreate.getStatusOK();
    private static final Status notFoundStatus = statusCreate.createStatus(StatusType.ERROR, "channel not found", null);
    private static final Status capacityImmutableStatus = statusCreate.createStatus(StatusType.ERROR, "capacity is immutable", null);
    private static final Status subFieldDoesNotExistStatus = statusCreate.createStatus(StatusType.ERROR, "subField does not exist", null);
    private static final Status subFieldNotDefinedStatus = statusCreate.createStatus(StatusType.ERROR, "subField not defined", null);
    private static final Status cannotProcessErrorStatus = statusCreate.createStatus(StatusType.ERROR, "can not process", null);
    private static final Status cannotProcessWarningStatus = statusCreate.createStatus(StatusType.WARNING, "can not process", null);
    private static final Status subFieldNotArrayStatus = statusCreate.createStatus(StatusType.ERROR, "subField is not an array", null);
    private static final Status channelDestroyedStatus = statusCreate.createStatus(StatusType.ERROR, "channel destroyed", null);
    private static final Status requestDestroyedStatus = statusCreate.createStatus(StatusType.ERROR, "request destroyed", null);
    private static final Status illegalRequestStatus = statusCreate.createStatus(StatusType.ERROR, "illegal pvRequest", null);
    private static final AtomicReference<ChannelProcessorProvider> channelProcessorProviderAtomic= new AtomicReference<ChannelProcessorProvider>();
    private static LinkedList<Channel> channelList = new LinkedList<Channel>();
   
    private static class ChannelFindLocal implements ChannelFind {
        
        private ChannelFindLocal() {
        }
        /* (non-Javadoc)
         * @see org.epics.ca.client.ChannelFind#cancelChannelFind()
         */
        @Override
        public void cancelChannelFind() {}
        /* (non-Javadoc)
         * @see org.epics.ca.client.ChannelFind#getChannelProvider()
         */
        @Override
        public ChannelProvider getChannelProvider() {
            return channelServer;
        }
        
    }
    
    private static class ChannelServerLocal implements ChannelServer{
        /* (non-Javadoc)
         * @see org.epics.ca.client.ChannelProvider#destroy()
         */
        @Override
        public void destroy() {
            Channel channel = null;
            while(true) {
                synchronized(channelList) {
                    if(channelList.size()<1) return;
                    channel = channelList.pop();
                }
                channel.destroy();
            }
        }
        /* (non-Javadoc)
         * @see org.epics.ca.client.ChannelProvider#getProviderName()
         */
        @Override
        public String getProviderName() {
            return providerName;
        }
        /* (non-Javadoc)
         * @see org.epics.ca.client.ChannelProvider#channelFind(java.lang.String, org.epics.ca.client.ChannelFindRequester)
         */
        @Override
        public ChannelFind channelFind(String channelName,ChannelFindRequester channelFindRequester) {
        	if (channelFindRequester == null)
        		throw new IllegalArgumentException("null channelFindRequester");
        	if (channelName == null)
        		throw new IllegalArgumentException("null channelName");
            PVRecord pvRecord = pvDatabase.findRecord(channelName);
            if(pvRecord==null) {
                PVDatabase beingInstalled = PVDatabaseFactory.getBeingInstalled();
                if(beingInstalled!=null) pvRecord = beingInstalled.findRecord(channelName);
            }
            boolean wasFound = ((pvRecord==null) ? false : true);
            channelFindRequester.channelFindResult(okStatus, channelFind, wasFound);
            return channelFind;
        }
		/* (non-Javadoc)
		 * @see org.epics.ca.client.ChannelProvider#query(org.epics.pvData.pv.PVField, org.epics.ca.client.QueryRequester)
		 */
		@Override
		public Query query(PVField query, QueryRequester queryRequester) {
			return null;
		}
        /* (non-Javadoc)
         * @see org.epics.ca.client.ChannelProvider#createChannel(java.lang.String, org.epics.ca.client.ChannelRequester, short)
         */
        @Override
        public Channel createChannel(String channelName,ChannelRequester channelRequester, short priority) {
        	if (channelRequester == null)
        		throw new IllegalArgumentException("null channelRequester");
        	if (channelName == null)
        		throw new IllegalArgumentException("null channelName");
        	if (priority < PRIORITY_MIN || priority > PRIORITY_MAX)
        		throw new IllegalArgumentException("priority out of bounds");
        	
            PVRecord pvRecord = pvDatabase.findRecord(channelName);
            if(pvRecord==null) {
                PVDatabase beingInstalled = PVDatabaseFactory.getBeingInstalled();
                if(beingInstalled!=null) pvRecord = beingInstalled.findRecord(channelName);
            }
            boolean wasFound = ((pvRecord==null) ? false : true);
            if(wasFound) {
                ChannelImpl channel = new ChannelImpl(this,pvRecord,channelRequester);
                channelRequester.channelCreated(okStatus, channel);
                synchronized(channelList) {
                    channelList.add(channel);
                }
                channelRequester.channelStateChange(channel, ConnectionState.CONNECTED);
               return channel;
            } else {
                channelRequester.channelCreated(notFoundStatus, null);
                return null;
            }
        }
        /* (non-Javadoc)
         * @see org.epics.ca.server.ChannelServer#registerChannelProcessProvider(org.epics.ca.server.ChannelProcessorProvider)
         */
        @Override
        public boolean registerChannelProcessProvider(ChannelProcessorProvider channelProcessorProvider) {
            return channelProcessorProviderAtomic.compareAndSet(null, channelProcessorProvider);
        }
    }
    
    private static class ChannelImpl implements Channel,PVRecordClient{
    	private final ChannelProvider provider;
        private final PVRecord pvRecord;
        private final ChannelRequester channelRequester;
        private final AtomicBoolean isDestroyed = new AtomicBoolean(false);
        private LinkedList<ChannelProcess> channelProcessList = new LinkedList<ChannelProcess>();
        private LinkedList<ChannelGet> channelGetList = new LinkedList<ChannelGet>();
        private LinkedList<ChannelPut> channelPutList = new LinkedList<ChannelPut>();
        private LinkedList<ChannelPutGet> channelPutGetList = new LinkedList<ChannelPutGet>();
        private LinkedList<ChannelRPC> channelRPCList = new LinkedList<ChannelRPC>();
        private LinkedList<Monitor> monitorList = new LinkedList<Monitor>();
        private LinkedList<ChannelArray> channelArrayList = new LinkedList<ChannelArray>();
        
        
        private ChannelImpl(ChannelProvider provider,PVRecord pvRecord,ChannelRequester channelRequester)
        {
        	this.provider = provider;
            this.pvRecord = pvRecord;
            this.channelRequester = channelRequester;
            pvRecord.registerClient(this);
        }       
        
        private ChannelProcessor requestChannelProcessor(ChannelProcessorRequester channelProcessorRequester)  {
            ChannelProcessorProvider channelProcessorProvider = channelProcessorProviderAtomic.get();
            if(channelProcessorProvider==null) return null;
            return channelProcessorProvider.requestChannelProcessor(pvRecord, channelProcessorRequester);
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.Requester#getRequesterName()
         */
        @Override
        public String getRequesterName() {
            return channelRequester.getRequesterName();
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.Requester#message(java.lang.String, org.epics.pvData.pv.MessageType)
         */
        @Override
        public void message(String message, MessageType messageType) {
            channelRequester.message(message, messageType);
        }
        /* (non-Javadoc)
         * @see org.epics.ca.client.Channel#isConnected()
         */
        @Override
        public boolean isConnected() {
            return !isDestroyed.get();
        }
        /* (non-Javadoc)
         * @see org.epics.ca.client.Channel#destroy()
         */
        @Override
        public void destroy() {
            if(!isDestroyed.compareAndSet(false, true)) return;
            while(true) {
                ChannelProcess channelProcess = null;
                synchronized(channelProcessList) {
                    if(channelProcessList.size()>0) {
                        channelProcess = channelProcessList.get(channelProcessList.size()-1);
                    } else {
                        break;
                    }
                }
                channelProcess.destroy();
            }
            while(true) {
                ChannelGet channelGet = null;
                synchronized(channelGetList) {
                    if(channelGetList.size()>0) {
                        channelGet = channelGetList.get(channelGetList.size()-1);
                    } else {
                        break;
                    }
                }
                channelGet.destroy();
            }
            while(true) {
                ChannelPut channelPut = null;
                synchronized(channelPutList) {
                    if(channelPutList.size()>0) {
                        channelPut = channelPutList.get(channelPutList.size()-1);
                    } else {
                        break;
                    }
                }
                channelPut.destroy();
            }
            while(true) {
                ChannelPutGet channelPutGet = null;
                synchronized(channelPutGetList) {
                    if(channelPutGetList.size()>0) {
                        channelPutGet = channelPutGetList.get(channelPutGetList.size()-1);
                    } else {
                        break;
                    }
                }
                channelPutGet.destroy();
            }
            while(true) {
                ChannelRPC channelRPC = null;
                synchronized(channelRPCList) {
                    if(channelRPCList.size()>0) {
                        channelRPC = channelRPCList.get(channelRPCList.size()-1);
                    } else {
                        break;
                    }
                }
                channelRPC.destroy();
            }
            while(true) {
                Monitor monitor = null;
                synchronized(monitorList) {
                    if(monitorList.size()>0) {
                        monitor = monitorList.get(monitorList.size()-1);
                    } else {
                        break;
                    }
                }
                monitor.destroy();
            }
            while(true) {
                ChannelArray channelArray = null;
                synchronized(channelArrayList) {
                    if(channelArrayList.size()>0) {
                    	channelArray = channelArrayList.get(channelArrayList.size()-1);
                    } else {
                        break;
                    }
                }
                channelArray.destroy();
            }
            synchronized(channelList) {
                channelList.remove(this);
            }
            channelRequester.channelStateChange(this, ConnectionState.DESTROYED);
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVRecordClient#detach(org.epics.pvData.pv.PVRecord)
         */
        @Override
		public void detach(PVRecord pvRecord) {
			destroy();
		}
		/* (non-Javadoc)
         * @see org.epics.ca.client.Channel#getConnectionState()
         */
        @Override
        public ConnectionState getConnectionState() {
        	if (isDestroyed.get())
        		return ConnectionState.DESTROYED;
        	else
        		return ConnectionState.CONNECTED;
        }
        /* (non-Javadoc)
         * @see org.epics.ca.client.Channel#getRemoteAddress()
         */
        @Override
        public String getRemoteAddress() {
            return providerName;
        }
        /* (non-Javadoc)
         * @see org.epics.ca.client.Channel#getField(org.epics.ca.client.GetFieldRequester, java.lang.String)
         */
        @Override
        public void getField(GetFieldRequester requester,String subField) {
        	if (requester == null)
        		throw new IllegalArgumentException("null requester");
            if(isDestroyed.get()) {
            	requester.getDone(channelDestroyedStatus, null);
            	return;
            }
            if(subField==null || subField.length()<1) {
                requester.getDone(okStatus, pvRecord.getPVStructure().getStructure());
                return;
            }
            PVField pvField = pvRecord.getPVStructure().getSubField(subField);
            if(pvField==null) {
                requester.getDone(subFieldDoesNotExistStatus, null);
            } else {
                requester.getDone(okStatus, pvField.getField());
            }
        }
        /* (non-Javadoc)
         * @see org.epics.ca.client.Channel#createChannelProcess(org.epics.ca.client.ChannelProcessRequester, org.epics.pvData.pv.PVStructure)
         */
        @Override
        public ChannelProcess createChannelProcess(
                ChannelProcessRequester channelProcessRequester,
                PVStructure pvRequest)
        {
        	if (channelProcessRequester == null)
        		throw new IllegalArgumentException("null channelProcessRequester");
            if(isDestroyed.get()) {
            	channelProcessRequester.channelProcessConnect(channelDestroyedStatus, null);
            	return null;
            }
            ChannelProcessImpl channelProcess = new ChannelProcessImpl(this,channelProcessRequester);
            if(channelProcess.canProcess()) {
                synchronized(channelProcessList) {
                    channelProcessList.add(channelProcess);           
                }
                channelProcessRequester.channelProcessConnect(okStatus, channelProcess);
                return channelProcess;
            } else {
                channelProcessRequester.channelProcessConnect(cannotProcessErrorStatus, null);
                return null;
            }
        }
        /* (non-Javadoc)
         * @see org.epics.ca.client.Channel#createChannelGet(org.epics.ca.client.ChannelGetRequester, org.epics.pvData.pv.PVStructure)
         */
        @Override
        public ChannelGet createChannelGet(
                ChannelGetRequester channelGetRequester, PVStructure pvRequest)
        {
        	if (channelGetRequester == null)
        		throw new IllegalArgumentException("null channelGetRequester");
        	if (pvRequest == null)
        		throw new IllegalArgumentException("null pvRequest");
            if(isDestroyed.get()) {
            	channelGetRequester.channelGetConnect(channelDestroyedStatus, null, null, null);
            	return null;
            }
            PVCopy pvCopy = PVCopyFactory.create(pvRecord, pvRequest,"field");
            PVStructure pvStructure = pvCopy.createPVStructure();
            return new ChannelGetImpl(this,channelGetRequester,pvStructure,pvCopy,getProcess(pvRequest));
        }
        /* (non-Javadoc)
         * @see org.epics.ca.client.Channel#createChannelPut(org.epics.ca.client.ChannelPutRequester, org.epics.pvData.pv.PVStructure)
         */
        @Override
        public ChannelPut createChannelPut(ChannelPutRequester channelPutRequester, PVStructure pvRequest)
        {
        	if (channelPutRequester == null)
        		throw new IllegalArgumentException("null channelPutRequester");
        	if (pvRequest == null)
        		throw new IllegalArgumentException("null pvRequest");
            if(isDestroyed.get()) {
            	channelPutRequester.channelPutConnect(channelDestroyedStatus, null, null, null);
            	return null;
            }
        	PVCopy pvCopy = PVCopyFactory.create(pvRecord, pvRequest,"field");
            PVStructure pvStructure = pvCopy.createPVStructure();
            return new ChannelPutImpl(this,channelPutRequester,pvStructure,pvCopy,getProcess(pvRequest));
        }
        /* (non-Javadoc)
         * @see org.epics.ca.client.Channel#createChannelPutGet(org.epics.ca.client.ChannelPutGetRequester, org.epics.pvData.pv.PVStructure, boolean, org.epics.pvData.pv.PVStructure, boolean, boolean, org.epics.pvData.pv.PVStructure)
         */
        @Override
        public ChannelPutGet createChannelPutGet(
                ChannelPutGetRequester channelPutGetRequester,
                PVStructure pvRequest)
        {
        	if (channelPutGetRequester == null)
        		throw new IllegalArgumentException("null channelPutRequester");
        	if (pvRequest == null)
        		throw new IllegalArgumentException("null pvRequest");
            if(isDestroyed.get()) {
            	channelPutGetRequester.channelPutGetConnect(channelDestroyedStatus, null, null, null);
            	return null;
            }
            boolean process = getProcess(pvRequest);

            PVField pvField = pvRequest.getSubField("putField");
            if(pvField==null || pvField.getField().getType()!=Type.structure) {
            	channelPutGetRequester.message("pvRequest does not have a putField request structure", MessageType.error);
            	channelPutGetRequester.message(pvRequest.toString(),MessageType.warning);
            	channelPutGetRequester.channelPutGetConnect(illegalRequestStatus, null, null, null);
            	return null;
            }
        	PVCopy pvPutCopy = PVCopyFactory.create(pvRecord, pvRequest, "putField");
        	pvField = pvRequest.getSubField("getField");
            if(pvField==null || pvField.getField().getType()!=Type.structure) {
            	channelPutGetRequester.message("pvRequest does not have a getField request structure", MessageType.error);
            	channelPutGetRequester.message(pvRequest.toString(),MessageType.warning);
            	channelPutGetRequester.channelPutGetConnect(illegalRequestStatus, null, null, null);
            	return null;
            }
        	PVCopy pvGetCopy = PVCopyFactory.create(pvRecord, pvRequest, "getField");
        	PVStructure pvPutStructure = pvPutCopy.createPVStructure();
            PVStructure pvGetStructure = pvGetCopy.createPVStructure();
            return new ChannelPutGetImpl(this,channelPutGetRequester,pvPutStructure,pvPutCopy,pvGetStructure,pvGetCopy,process);
        }
        /* (non-Javadoc)
         * @see org.epics.ca.client.Channel#createChannelRPC(org.epics.ca.client.ChannelRPCRequester, org.epics.pvData.pv.PVStructure)
         */
        @Override
		public ChannelRPC createChannelRPC(
				ChannelRPCRequester channelRPCRequester, PVStructure pvRequest)
        {
        	if (channelRPCRequester == null)
        		throw new IllegalArgumentException("null channelRPCRequester");
        	if (pvRequest == null)
        		throw new IllegalArgumentException("null pvRequest");
        	if(isDestroyed.get()) {
        		channelRPCRequester.channelRPCConnect(channelDestroyedStatus, null, null,null);
        		return null;
        	}
        	ChannelRPCImpl channelRPCImpl = new ChannelRPCImpl(this,pvRecord,channelRPCRequester,pvRequest);
        	boolean isOK = channelRPCImpl.init();
        	if(!isOK) return null;
        	return channelRPCImpl;
        }
		/* (non-Javadoc)
         * @see org.epics.ca.client.Channel#createMonitor(org.epics.pvData.monitor.MonitorRequester, org.epics.pvData.pv.PVStructure, org.epics.pvData.pv.PVStructure)
         */
        @Override
        public Monitor createMonitor(
                MonitorRequester monitorRequester,
                PVStructure pvRequest)
        {
        	if (monitorRequester == null)
        		throw new IllegalArgumentException("null channelPutRequester");
        	if (pvRequest == null)
        		throw new IllegalArgumentException("null pvRequest");
            if(isDestroyed.get()) {
            	monitorRequester.monitorConnect(channelDestroyedStatus, null, null);
            	return null;
            }
            return MonitorFactory.create(pvRecord, monitorRequester, pvRequest);
        }
        /* (non-Javadoc)
         * @see org.epics.ca.client.Channel#createChannelArray(org.epics.ca.client.ChannelArrayRequester, java.lang.String, org.epics.pvData.pv.PVStructure)
         */
        @Override
        public ChannelArray createChannelArray(
                ChannelArrayRequester channelArrayRequester, PVStructure pvRequest)
        {
        	if (channelArrayRequester == null)
        		throw new IllegalArgumentException("null channelArrayRequester");
            if(isDestroyed.get()) {
            	channelArrayRequester.channelArrayConnect(channelDestroyedStatus, null, null);
            	return null;
            }
            PVField pvField = pvRequest.getSubField("field");
            if(pvField==null || pvField.getField().getType()!=Type.scalar) {
            	channelArrayRequester.channelArrayConnect(subFieldNotDefinedStatus, null, null);
                return null;
            }
            Scalar scalar = (Scalar)pvField.getField();
            if(scalar.getScalarType()!=ScalarType.pvString) {
            	channelArrayRequester.channelArrayConnect(subFieldNotDefinedStatus, null, null);
                return null;
            }
            PVString pvString = (PVString)pvField;
    		pvField = pvRecord.getPVStructure().getSubField(pvString.get());
            if(pvField==null) {
            	channelArrayRequester.channelArrayConnect(subFieldDoesNotExistStatus, null, null);
                return null;
            }
            if(pvField.getField().getType()!=Type.scalarArray) {
                channelArrayRequester.channelArrayConnect(subFieldNotArrayStatus, null, null);
                return null;
            }
            PVArray pvArray = (PVArray)pvField;
            PVArray pvCopy = null;
            if(pvArray.getArray().getElementType()==ScalarType.pvStructure) {
            	PVStructureArray pvStructureArray = (PVStructureArray)pvArray;
            	pvCopy = pvDataCreate.createPVStructureArray(null, pvStructureArray.getStructureArray());
            } else {
                pvCopy = pvDataCreate.createPVArray(null, "", pvArray.getArray().getElementType());
            }
            return new ChannelArrayImpl(this,channelArrayRequester,pvArray,pvCopy);
        }
        /* (non-Javadoc)
         * @see org.epics.ca.client.Channel#getAccessRights(org.epics.pvData.pv.PVField)
         */
        @Override
        public AccessRights getAccessRights(PVField pvField) {
            // TODO Auto-generated method stub
            return null;
        }
        /* (non-Javadoc)
         * @see org.epics.ca.client.Channel#getChannelRequester()
         */
        @Override
        public ChannelRequester getChannelRequester() {
            return channelRequester;
        }
        /* (non-Javadoc)
         * @see org.epics.ca.client.Channel#getChannelName()
         */
        @Override
        public String getChannelName() {
            return pvRecord.getRecordName();
        }
        /* (non-Javadoc)
         * @see org.epics.ca.client.Channel#getProvider()
         */
        @Override
        public ChannelProvider getProvider() {
            return provider;
        }
        
        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "{ name = " + pvRecord.getRecordName() + (isDestroyed.get() ? " disconnected }" : " connected }" ); 
        }
        
        private boolean getProcess(PVStructure pvRequest) {
        	PVField pvField = pvRequest.getSubField("record.process");
        	if(pvField==null || pvField.getField().getType()!=Type.scalar) return false;
        	Scalar scalar = (Scalar)pvField.getField();
        	if(scalar.getScalarType()==ScalarType.pvString) {
        		PVString pvString = (PVString)pvField;
        		return (pvString.get().equalsIgnoreCase("true")) ? true : false;
        	} else if(scalar.getScalarType()==ScalarType.pvBoolean) {
        		PVBoolean pvBoolean = (PVBoolean)pvField;
        		return pvBoolean.get();
        	}
        	return false;
        }
        
        private static class ChannelProcessImpl implements ChannelProcess,ChannelProcessorRequester
        {
            ChannelProcessImpl(ChannelImpl channelImpl,ChannelProcessRequester channelProcessRequester)
            {
                this.channelImpl = channelImpl;
                this.channelProcessRequester = channelProcessRequester;
                channelProcessor = channelImpl.requestChannelProcessor(this);
            }
            
            boolean canProcess() {
                return (channelProcessor==null) ? false : true;
            }

            private ChannelImpl channelImpl;
            private ChannelProcessRequester channelProcessRequester;
            private ChannelProcessor channelProcessor = null;
            private final AtomicBoolean isDestroyed = new AtomicBoolean(false);
            private Status success = null;
            private boolean lastRequest = false;
            /* (non-Javadoc)
             * @see org.epics.ca.client.ChannelProcess#destroy()
             */
            @Override
            public void destroy() {
                if(!isDestroyed.compareAndSet(false, true)) return;
                channelProcessor.detach();
                synchronized(channelImpl.channelProcessList) {
                    channelImpl.channelProcessList.remove(this);
                }
            }
            
            /* (non-Javadoc)
             * @see org.epics.ca.client.ChannelProcess#process(boolean)
             */
            @Override
            public void process(boolean lastRequest) {
                if(isDestroyed.get()) {
                    channelProcessRequester.processDone(requestDestroyedStatus);
                    return;
                }
                this.lastRequest = lastRequest;
                channelProcessor.requestProcess();
            }
            /* (non-Javadoc)
             * @see org.epics.ca.server.ChannelProcessorRequester#becomeProcessor()
             */
            @Override
            public void becomeProcessor() {
                channelProcessor.process(false, null);
            }
            /* (non-Javadoc)
             * @see org.epics.ca.server.ChannelProcessorRequester#canNotProcess(java.lang.String)
             */
            @Override
			public void canNotProcess(String reason) {
            	message(reason,MessageType.error);
            	channelProcessRequester.processDone(cannotProcessErrorStatus);
                if(lastRequest) destroy();
			}
			/* (non-Javadoc)
             * @see org.epics.ca.server.ChannelProcessorRequester#recordProcessComplete()
             */
            @Override
            public void recordProcessComplete() {
                channelProcessRequester.processDone(success);
                if(lastRequest) destroy();
            }
            /* (non-Javadoc)
             * @see org.epics.ca.server.ChannelProcessorRequester#lostRightToProcess()
             */
            @Override
			public void lostRightToProcess() {
            	message("lost ability to process",MessageType.fatalError);
				channelImpl.destroy();
			}
			/* (non-Javadoc)
             * @see org.epics.ca.server.ChannelProcessorRequester#recordProcessResult(org.epics.pvData.pv.Status)
             */
            @Override
            public void recordProcessResult(Status success) {
                this.success = success;
            }
            /* (non-Javadoc)
             * @see org.epics.ioc.util.Requester#getRequesterName()
             */
            @Override
            public String getRequesterName() {
                return channelProcessRequester.getRequesterName();
            }
            /* (non-Javadoc)
             * @see org.epics.ioc.util.Requester#message(java.lang.String, org.epics.ioc.util.MessageType)
             */
            @Override
            public void message(String message, MessageType messageType) {
                channelProcessRequester.message(message, messageType);
            }
            
        }
        
        private class ChannelGetImpl implements ChannelGet,ChannelProcessorRequester
        {
            private ChannelGetImpl(ChannelImpl channelImpl,ChannelGetRequester channelGetRequester,PVStructure pvStructure,PVCopy pvCopy,boolean process)
            {
                this.channelImpl = channelImpl;
                this.channelGetRequester = channelGetRequester;
                this.pvStructure = pvStructure;
                this.pvCopy = pvCopy;
                this.process = process;
                Status status = okStatus;
                bitSet = new BitSet(pvStructure.getNumberFields());
                pvCopy.initCopy(pvStructure, bitSet, true);
                if(process) {
                    this.process = true;
                    channelProcessor = channelImpl.requestChannelProcessor(this);
                    if(channelProcessor==null) {
                    	status = cannotProcessWarningStatus;
                        this.process = false;
                    }
                }
                synchronized(channelImpl.channelGetList) {
                    channelImpl.channelGetList.add(this);
                }
                channelGetRequester.channelGetConnect(status, this, pvStructure,bitSet);
            }
            
            private boolean firstTime = true;
            private ChannelImpl channelImpl;
            private ChannelGetRequester channelGetRequester;
            private PVStructure pvStructure;
            private PVCopy pvCopy;
            private boolean process;
            private Status success;
            private ChannelProcessor channelProcessor = null;
            private BitSet bitSet = null;
            private final AtomicBoolean isDestroyed = new AtomicBoolean(false);
            private boolean lastRequest = false;
            /* (non-Javadoc)
             * @see org.epics.pvData.misc.Destroyable#destroy()
             */
            @Override
            public void destroy() {
                if(!isDestroyed.compareAndSet(false, true)) return;
                if(process) channelProcessor.detach();
                synchronized(channelImpl.channelGetList) {
                    channelImpl.channelGetList.remove(this);
                }
            }
            /* (non-Javadoc)
             * @see org.epics.ca.client.ChannelGet#get()
             */
            @Override
            public void get(boolean lastRequest) {
                if(isDestroyed.get()) {
                    channelGetRequester.getDone(requestDestroyedStatus);
                    return;
                }
                this.lastRequest = lastRequest;
                bitSet.clear();
                if(process) {
                    channelProcessor.requestProcess();
                    return;
                }
                pvRecord.lock();
                try {
                    getData();
                } finally {
                    pvRecord.unlock();
                }
                channelGetRequester.getDone(okStatus);
            }
            /* (non-Javadoc)
             * @see org.epics.ca.server.ChannelProcessorRequester#becomeProcessor()
             */
            @Override
            public void becomeProcessor() {
                channelProcessor.process(true, null);
            }
            /* (non-Javadoc)
             * @see org.epics.ca.server.ChannelProcessorRequester#canNotProcess(java.lang.String)
             */
            @Override
			public void canNotProcess(String reason) {
            	message(reason,MessageType.error);
            	channelGetRequester.getDone(cannotProcessErrorStatus);
                if(lastRequest) destroy();
			}
            /* (non-Javadoc)
             * @see org.epics.ca.server.ChannelProcessorRequester#recordProcessComplete()
             */
            @Override
            public void recordProcessComplete() {
                channelGetRequester.getDone(success);
                if(lastRequest) destroy();
            }
            /* (non-Javadoc)
             * @see org.epics.ca.server.ChannelProcessorRequester#recordProcessResult(org.epics.pvData.pv.Status)
             */
            @Override
            public void recordProcessResult(Status success) {
                this.success = success;
                getData();
                channelProcessor.setInactive();
            }
            /* (non-Javadoc)
             * @see org.epics.ca.server.ChannelProcessorRequester#lostRightToProcess()
             */
            @Override
			public void lostRightToProcess() {
            	message("lost ability to process",MessageType.fatalError);
				channelImpl.destroy();
			}
            /* (non-Javadoc)
             * @see org.epics.ioc.util.Requester#getRequesterName()
             */
            @Override
            public String getRequesterName() {
                return channelGetRequester.getRequesterName();
            }
            /* (non-Javadoc)
             * @see org.epics.ioc.util.Requester#message(java.lang.String, org.epics.ioc.util.MessageType)
             */
            @Override
            public void message(String message, MessageType messageType) {
                channelGetRequester.message(message, messageType);
            }
            
            private void getData() {
                pvCopy.updateCopySetBitSet(pvStructure, bitSet, false);
                if(firstTime) {
                    bitSet.clear();
                    bitSet.set(0);
                    firstTime = false;
                } 
            }
        }
        
        private class ChannelPutImpl implements ChannelPut,ChannelProcessorRequester
        {
            private ChannelPutImpl(
                    ChannelImpl channelImpl,
                    ChannelPutRequester channelPutRequester,
                    PVStructure pvStructure,
                    PVCopy pvCopy,
                    boolean process)
            {
                this.channelImpl = channelImpl;
                this.channelPutRequester = channelPutRequester;
                this.pvStructure = pvStructure;
                this.pvCopy = pvCopy;
                this.process = process;
                Status status = okStatus;
                bitSet = new BitSet(pvStructure.getNumberFields());
                pvCopy.initCopy(pvStructure, bitSet, true);
                if(process) {
                    this.process = true;
                    channelProcessor = channelImpl.requestChannelProcessor(this);
                    if(channelProcessor==null) {
                    	status = cannotProcessWarningStatus;
                        this.process = false;
                    }
                }
                synchronized(channelImpl.channelPutList) {
                    channelImpl.channelPutList.add(this);
                }
                channelPutRequester.channelPutConnect(status, this, pvStructure,bitSet);
            }
            
            private ChannelImpl channelImpl;
            private ChannelPutRequester channelPutRequester = null;
            private PVStructure pvStructure;
            private PVCopy pvCopy;
            private boolean process;
            private Status success;
            private ChannelProcessor channelProcessor = null;
            private BitSet bitSet = null;
            private final AtomicBoolean isDestroyed = new AtomicBoolean(false);
            private boolean lastRequest = false;
            
            /* (non-Javadoc)
             * @see org.epics.pvData.misc.Destroyable#destroy()
             */
            public void destroy() {
                if(!isDestroyed.compareAndSet(false, true))  return;
                if(process) channelProcessor.detach();
                synchronized(channelImpl.channelPutList) {
                    channelImpl.channelPutList.remove(this);
                }
            }
            /* (non-Javadoc)
             * @see org.epics.ca.client.ChannelPut#put(boolean)
             */
            public void put(boolean lastRequest) {
                if(isDestroyed.get()) {
                    channelPutRequester.putDone(requestDestroyedStatus);
                    return;
                }
                success = okStatus;
                this.lastRequest = lastRequest;
                if(process) {
                    channelProcessor.requestProcess();
                    return;
                }
                pvRecord.lock();
                try {
                    putData();
                } finally {
                    pvRecord.unlock();
                }
                channelPutRequester.putDone(success);
                return;
            } 
            /* (non-Javadoc)
             * @see org.epics.ca.server.ChannelProcessorRequester#becomeProcessor()
             */
            @Override
            public void becomeProcessor() {
            	pvRecord.lock();
                try {
                    putData();
                } finally {
                    pvRecord.unlock();
                }
            	channelProcessor.process(false, null);
            	return;
            }
            /* (non-Javadoc)
             * @see org.epics.ca.server.ChannelProcessorRequester#canNotProcess(java.lang.String)
             */
            @Override
			public void canNotProcess(String reason) {
            	message(reason,MessageType.error);
            	channelPutRequester.putDone(cannotProcessErrorStatus);
                if(lastRequest) destroy();
			}
            /* (non-Javadoc)
             * @see org.epics.ca.server.ChannelProcessorRequester#recordProcessComplete()
             */
            @Override
            public void recordProcessComplete() {
                channelPutRequester.putDone(success);
                if(lastRequest) destroy();
            }
            /* (non-Javadoc)
             * @see org.epics.ca.server.ChannelProcessorRequester#recordProcessResult(org.epics.pvData.pv.Status)
             */
            @Override
            public void recordProcessResult(Status success) {
                this.success = success;
            }
            /* (non-Javadoc)
             * @see org.epics.ca.server.ChannelProcessorRequester#lostRightToProcess()
             */
            @Override
			public void lostRightToProcess() {
            	message("lost ability to process",MessageType.fatalError);
				channelImpl.destroy();
			}
            /* (non-Javadoc)
             * @see org.epics.ca.client.ChannelPut#get()
             */
            @Override
            public void get() {
                if(isDestroyed.get()) {
                    channelPutRequester.getDone(requestDestroyedStatus);
                    return;
                }
                pvRecord.lock();
                try {
                    getData();
                } finally {
                    pvRecord.unlock();
                }
                channelPutRequester.getDone(okStatus);
                return;
            }
            /* (non-Javadoc)
             * @see org.epics.ioc.util.Requester#getRequesterName()
             */
            @Override
            public String getRequesterName() {
                return channelPutRequester.getRequesterName();
            }
            /* (non-Javadoc)
             * @see org.epics.ioc.util.Requester#message(java.lang.String, org.epics.ioc.util.MessageType)
             */
            @Override
            public void message(String message, MessageType messageType) {
                channelPutRequester.message(message, messageType);
            }
            
            private void putData() {
               pvCopy.updateRecord(pvStructure, bitSet, false);
            }
            
            private void getData() {
                bitSet.clear();
                bitSet.set(0);
                pvCopy.updateCopyFromBitSet(pvStructure, bitSet, false);
             }
        }
        
        private class ChannelPutGetImpl implements ChannelPutGet,ChannelProcessorRequester
        {
            private ChannelPutGetImpl(
                    ChannelImpl channelImpl,
                    ChannelPutGetRequester channelPutGetRequester,
                    PVStructure pvPutStructure,
                    PVCopy pvPutCopy,
                    PVStructure pvGetStructure,
                    PVCopy pvGetCopy,
                    boolean process)
            {
                this.channelImpl = channelImpl;
                this.channelPutGetRequester = channelPutGetRequester;
                this.pvPutStructure = pvPutStructure;
                this.pvPutCopy = pvPutCopy;
                this.pvGetStructure = pvGetStructure;
                this.pvGetCopy = pvGetCopy;
                this.process = process;
                putBitSet = new BitSet(pvPutStructure.getNumberFields());
                pvPutCopy.initCopy(pvPutStructure, putBitSet, true);
                getBitSet = new BitSet(pvGetStructure.getNumberFields());
                pvGetCopy.initCopy(pvGetStructure, getBitSet, true);
                Status status = okStatus;
                if(process) {
                    this.process = true;
                    channelProcessor = channelImpl.requestChannelProcessor(this);
                    if(channelProcessor==null) {
                    	status = cannotProcessWarningStatus;
                        this.process = false;
                    }
                }
                synchronized(channelImpl.channelPutGetList) {
                    channelImpl.channelPutGetList.add(this);
                }
                channelPutGetRequester.channelPutGetConnect(status, this, pvPutStructure,pvGetStructure);
            }
            
            private ChannelImpl channelImpl;
            private ChannelPutGetRequester channelPutGetRequester = null;
            private PVStructure pvPutStructure;
            private PVCopy pvPutCopy;
            private PVStructure pvGetStructure;
            private PVCopy pvGetCopy;
            private boolean process;
            private Status success;
            private ChannelProcessor channelProcessor = null;
            private BitSet putBitSet = null;
            private BitSet getBitSet = null;
            private final AtomicBoolean isDestroyed = new AtomicBoolean(false);
            private boolean lastRequest = false;
            /* (non-Javadoc)
             * @see org.epics.pvData.misc.Destroyable#destroy()
             */
            @Override
            public void destroy() {
                if(!isDestroyed.compareAndSet(false, true)) return;
                if(process) channelProcessor.detach();
                synchronized(channelImpl.channelPutGetList) {
                    channelImpl.channelPutGetList.remove(this);
                }
            }
            /* (non-Javadoc)
             * @see org.epics.ca.client.ChannelPutGet#putGet(boolean)
             */
            @Override
            public void putGet(boolean lastRequest)
            {
                if(isDestroyed.get()) {
                    channelPutGetRequester.putGetDone(requestDestroyedStatus);
                    return;
                }
                success = okStatus;
                this.lastRequest = lastRequest;
                if(process) {
                    channelProcessor.requestProcess();
                    return;
                }
                pvRecord.lock();
                try {
                    putData();
                    getData();
                } finally {
                    pvRecord.unlock();
                }
                channelPutGetRequester.putGetDone(success);
            }
            /* (non-Javadoc)
             * @see org.epics.ca.server.ChannelProcessorRequester#becomeProcessor()
             */
            @Override
            public void becomeProcessor() {
            	pvRecord.lock();
            	try {
            		putData();
            	} finally {
            		pvRecord.unlock();
            	}
            	channelProcessor.process(true, null);
            }
            /* (non-Javadoc)
             * @see org.epics.ca.server.ChannelProcessorRequester#canNotProcess(java.lang.String)
             */
            @Override
			public void canNotProcess(String reason) {
            	getData();
            	message(reason,MessageType.error);
            	channelPutGetRequester.putGetDone(cannotProcessErrorStatus);
                if(lastRequest) destroy();
			}

            /* (non-Javadoc)
             * @see org.epics.ca.server.ChannelProcessorRequester#recordProcessComplete()
             */
            @Override
            public void recordProcessComplete() {
                getData();
                channelProcessor.setInactive();
                channelPutGetRequester.putGetDone(success);
                if(lastRequest) destroy();
            }
            /* (non-Javadoc)
             * @see org.epics.ca.server.ChannelProcessorRequester#recordProcessResult(org.epics.pvData.pv.Status)
             */
            @Override
            public void recordProcessResult(Status success) {
                this.success = success;
            }
            /* (non-Javadoc)
             * @see org.epics.ca.server.ChannelProcessorRequester#lostRightToProcess()
             */
            @Override
			public void lostRightToProcess() {
            	message("lost ability to process",MessageType.fatalError);
				channelImpl.destroy();
			}
            /* (non-Javadoc)
             * @see org.epics.ca.client.ChannelPutGet#getGet()
             */
            @Override
            public void getGet() {
                if(isDestroyed.get()) {
                    channelPutGetRequester.getGetDone(requestDestroyedStatus);
                    return;
                }
                pvRecord.lock();
                try {
                    getData();
                } finally {
                    pvRecord.unlock();
                }
                channelPutGetRequester.getGetDone(okStatus);
            }
            /* (non-Javadoc)
             * @see org.epics.ca.client.ChannelPutGet#getPut()
             */
            @Override
            public void getPut() {
                if(isDestroyed.get()) {
                    channelPutGetRequester.getPutDone(requestDestroyedStatus);
                    return;
                }
                pvRecord.lock();
                try {
                    getPutData();
                } finally {
                    pvRecord.unlock();
                }
                channelPutGetRequester.getPutDone(okStatus);
                
            }
            /* (non-Javadoc)
             * @see org.epics.pvData.pv.Requester#getRequesterName()
             */
            @Override
            public String getRequesterName() {
                return channelPutGetRequester.getRequesterName();
            }     
            /* (non-Javadoc)
             * @see org.epics.ioc.util.Requester#message(java.lang.String, org.epics.ioc.util.MessageType)
             */
            @Override
            public void message(String message, MessageType messageType) {
                channelPutGetRequester.message(message, messageType);
            }
            
            private void putData() {
                putBitSet.clear();
                putBitSet.set(0);
                pvPutCopy.updateRecord(pvPutStructure, putBitSet, false);
            }
            
            private void getData() {
                pvGetCopy.updateCopySetBitSet(pvGetStructure, getBitSet, false);
                getBitSet.clear();
                getBitSet.set(0);
            }
            
            private void getPutData() {
                pvPutCopy.updateCopySetBitSet(pvPutStructure, putBitSet, false);
                putBitSet.clear();
                putBitSet.set(0);
            }
        }
        
        private class ChannelRPCImpl implements ChannelRPC
        {
        	
			private ChannelRPCImpl(ChannelImpl channelImpl,PVRecord pvRecord,ChannelRPCRequester channelRPCRequester,PVStructure pvRequest)
        	{
        		this.channelImpl = channelImpl;
        		this.pvRecord = pvRecord;
        		this.channelRPCRequester = channelRPCRequester;
        		this.pvRequest = pvRequest;
        	}
            
            @SuppressWarnings("unchecked")
			private boolean init() {
            	PVString pvFactory = pvRecord.getPVStructure().getStringField("factoryRPC");
            	if(pvFactory==null) return false;
            	String factoryName = pvFactory.get();
            	Class supportClass;
                server = null;
                Method method = null;
                try {
                    supportClass = Class.forName(factoryName);
                }catch (ClassNotFoundException e) {
                	String message = " factory " + e.getLocalizedMessage() + " class not found";
                	Status status = statusCreate.createStatus(StatusType.ERROR, message, null);
                	channelRPCRequester.channelRPCConnect(status, null, null,null);
                    return false;
                }
               
                try {
                    method = supportClass.getDeclaredMethod("create");    
                } catch (NoSuchMethodException e) {
                	String message = " create " + e.getLocalizedMessage() + " no factory method";
                	Status status = statusCreate.createStatus(StatusType.ERROR, message, null);
                	channelRPCRequester.channelRPCConnect(status, null, null,null);
                    return false;
                }
                if(!Modifier.isStatic(method.getModifiers())) {
                	String message = " create is not a static method ";
                	Status status = statusCreate.createStatus(StatusType.ERROR, message, null);
                	channelRPCRequester.channelRPCConnect(status, null, null,null);
                    return false;
                }
                try {
                	server = (RPCServer)method.invoke(null);
                } catch(IllegalAccessException e) {
                	String message = "create invoke IllegalAccessException  ";
                	Status status = statusCreate.createStatus(StatusType.ERROR, message, null);
                	channelRPCRequester.channelRPCConnect(status, null, null,null);
                    return false;
                } catch(IllegalArgumentException e) {
                	String message = "create invoke IllegalArgumentException " + e.getLocalizedMessage();
                	Status status = statusCreate.createStatus(StatusType.ERROR, message, null);
                	channelRPCRequester.channelRPCConnect(status, null, null,null);
                    return false;
                } catch(InvocationTargetException e) {
                	String message = " create invoke InvocationTargetException " + e.getLocalizedMessage();
                	Status status = statusCreate.createStatus(StatusType.ERROR, message, null);
                	channelRPCRequester.channelRPCConnect(status, null, null,null);
                    return false;
                }
                if(server==null) {
                	String message = " create server failed ";
                	Status status = statusCreate.createStatus(StatusType.ERROR, message, null);
                	channelRPCRequester.channelRPCConnect(status, null, null,null);
                	return false;
                }
                PVCopy pvCopy = PVCopyFactory.create(pvRecord, pvRequest,"field");
                pvStructure = pvCopy.createPVStructure();
                PVStructure pvArgument = pvStructure.getStructureField("arguments");
                if(pvArgument==null) {
                	String message = "arguments not defined in record";
                	Status status = statusCreate.createStatus(StatusType.ERROR, message, null);
                	channelRPCRequester.channelRPCConnect(status, null, null,null);
                	return false;
                }
                BitSet bitSet = new BitSet(pvStructure.getNumberFields());
                pvCopy.initCopy(pvStructure, bitSet, true);
                Status status =server.initialize(channelImpl, pvRecord, channelRPCRequester, pvArgument,bitSet, pvRequest);
                if(!status.isOK()) {
                	channelRPCRequester.channelRPCConnect(status, null, null,null);
                	return false;
                }
                synchronized(channelRPCList) {
                	channelRPCList.add(this);
                }
                channelRPCRequester.channelRPCConnect(status, this, pvArgument,bitSet);
            	return true;
            }
            private final AtomicBoolean isDestroyed = new AtomicBoolean(false);
            private ChannelImpl channelImpl;
            private PVRecord pvRecord;
            private ChannelRPCRequester channelRPCRequester;
            private PVStructure pvRequest;
            
            private RPCServer server = null;
            private PVStructure pvStructure;
			/* (non-Javadoc)
			 * @see org.epics.pvData.misc.Destroyable#destroy()
			 */
			@Override
			public void destroy() {
				if(!isDestroyed.compareAndSet(false, true)) return;
				if(server!=null) server.destroy();
				synchronized(channelRPCList) {
                	channelRPCList.remove(this);
                }
			}
			/* (non-Javadoc)
			 * @see org.epics.ca.client.ChannelRPC#request(boolean)
			 */
			@Override
			public void request(boolean lastRequest) {
				server.request();
				if(lastRequest) destroy();
			}
        }
        
        private static class ChannelArrayImpl implements ChannelArray {
            private ChannelArrayImpl(ChannelImpl channelImpl,
                    ChannelArrayRequester channelArrayRequester,
                    PVArray pvArray,PVArray pvCopy)
            {
                this.channelImpl = channelImpl;
                this.channelArrayRequester = channelArrayRequester;
                this.pvArray = pvArray;
                this.pvCopy = pvCopy;
                pvRecord = channelImpl.pvRecord;

                synchronized(channelImpl.channelArrayList) {
                    channelImpl.channelArrayList.add(this);
                }
                channelArrayRequester.channelArrayConnect(okStatus, this, pvCopy);
            }

            private ChannelImpl channelImpl;
            private ChannelArrayRequester channelArrayRequester;
            private PVArray pvArray;
            private PVArray pvCopy;
            private PVRecord pvRecord;
            private final AtomicBoolean isDestroyed = new AtomicBoolean(false);
            /* (non-Javadoc)
             * @see org.epics.pvData.misc.Destroyable#destroy()
             */
            @Override
            public void destroy() {
                if(!isDestroyed.compareAndSet(false, true)) return;
                synchronized(channelImpl.channelArrayList) {
                    channelImpl.channelArrayList.remove(this);
                }
            }
            /* (non-Javadoc)
             * @see org.epics.ca.client.ChannelArray#getArray(boolean, int, int)
             */
            @Override
            public void getArray(boolean lastRequest, int offset, int count) {
                if(isDestroyed.get()) {
                	channelArrayRequester.getArrayDone(requestDestroyedStatus);
                	return;
                }
                if(count<=0) count = pvArray.getLength();
                pvRecord.lock();
                try {
                    int len = convert.copyArray(pvArray, offset, pvCopy, 0, count);
                    if(!pvCopy.isImmutable()) pvCopy.setLength(len);
                } finally  {
                    pvRecord.unlock();
                }
                channelArrayRequester.getArrayDone(okStatus);
                if(lastRequest) destroy();
            }
            /* (non-Javadoc)
             * @see org.epics.ca.client.ChannelArray#putArray(boolean, int, int)
             */
            @Override
            public void putArray(boolean lastRequest, int offset, int count) {
                if(isDestroyed.get()) {
                	channelArrayRequester.getArrayDone(requestDestroyedStatus);
                	return;
                }
                if(count<=0) count = pvCopy.getLength();
                pvRecord.lock();
                try {
                    convert.copyArray(pvCopy, 0, pvArray, offset, count);
                } finally  {
                    pvRecord.unlock();
                }
                channelArrayRequester.putArrayDone(okStatus);
                if(lastRequest) destroy();
            }
			/* (non-Javadoc)
			 * @see org.epics.ca.client.ChannelArray#setLength(boolean, int, int)
			 */
			@Override
			public void setLength(boolean lastRequest, int length, int capacity) {
				if(isDestroyed.get()) {
                	channelArrayRequester.setLengthDone(requestDestroyedStatus);
                	return;
                }
				if(capacity>=0 && !pvArray.isCapacityMutable()) {
					channelArrayRequester.setLengthDone(capacityImmutableStatus);
					return;
				}
				pvRecord.lock();
                try {
                    if(length>=0) {
                    	if(pvArray.getLength()!=length) pvArray.setLength(length);
                    }
                    if(capacity>=0) {
                    	if(pvArray.getCapacity()!=capacity) pvArray.setCapacity(capacity);
                    }
                } finally  {
                    pvRecord.unlock();
                }
                channelArrayRequester.setLengthDone(okStatus);
                if(lastRequest) destroy();
			}
        }
    }
}
