/*
 * Copyright (c) 2004 by Cosylab
 *
 * The full license specifying the redistribution, modification, usage and other
 * rights and obligations is included with the distribution of this project in
 * the file "LICENSE-CAJ". If the license is not included visit Cosylab web site,
 * <http://www.cosylab.com>.
 *
 * THIS SOFTWARE IS PROVIDED AS-IS WITHOUT WARRANTY OF ANY KIND, NOT EVEN THE
 * IMPLIED WARRANTY OF MERCHANTABILITY. THE AUTHOR OF THIS SOFTWARE, ASSUMES
 * _NO_ RESPONSIBILITY FOR ANY CONSEQUENCE RESULTING FROM THE USE, MODIFICATION,
 * OR REDISTRIBUTION OF THIS SOFTWARE.
 */

package org.epics.ca.client.example;

import java.util.concurrent.atomic.AtomicInteger;

import org.epics.ca.CAConstants;
import org.epics.ca.CAException;
import org.epics.ca.ClientFactory;
import org.epics.ca.client.Channel;
import org.epics.ca.client.ChannelArray;
import org.epics.ca.client.ChannelArrayRequester;
import org.epics.ca.client.ChannelGet;
import org.epics.ca.client.ChannelGetRequester;
import org.epics.ca.client.ChannelProcess;
import org.epics.ca.client.ChannelProcessRequester;
import org.epics.ca.client.ChannelPut;
import org.epics.ca.client.ChannelPutGet;
import org.epics.ca.client.ChannelPutGetRequester;
import org.epics.ca.client.ChannelPutRequester;
import org.epics.ca.client.ChannelRequester;
import org.epics.ca.client.GetFieldRequester;
import org.epics.ca.client.Channel.ConnectionState;
import org.epics.ca.client.impl.remote.ClientContextImpl;
import org.epics.pvData.factory.FieldFactory;
import org.epics.pvData.factory.PVDataFactory;
import org.epics.pvData.misc.BitSet;
import org.epics.pvData.monitor.Monitor;
import org.epics.pvData.monitor.MonitorElement;
import org.epics.pvData.monitor.MonitorRequester;
import org.epics.pvData.pv.Field;
import org.epics.pvData.pv.FieldCreate;
import org.epics.pvData.pv.MessageType;
import org.epics.pvData.pv.PVArray;
import org.epics.pvData.pv.PVDataCreate;
import org.epics.pvData.pv.PVDouble;
import org.epics.pvData.pv.PVInt;
import org.epics.pvData.pv.PVString;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.ScalarType;
import org.epics.pvData.pv.Status;
import org.epics.pvData.pv.Structure;

/**
 * Simple basic usage test.
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
@SuppressWarnings("unused")
public class TestExample {

	// register remote CA
	static {
		ClientFactory.start();
	}
    /**
     * CA context.
     */
    private ClientContextImpl context = null;
    
    /**
     * Initialize JCA context.
     * @throws CAException	throws on any failure.
     */
    private void initialize() throws CAException {
        
		// Create a context with default configuration values.
		context = new ClientContextImpl();
		context.initialize();

		// Display basic information about the context.
        System.out.println(context.getVersion().getVersionString());
        context.printInfo(); System.out.println();
    }

    /**
     * Destroy JCA context.
     */
    private void destroy() {
        
        try {

            // Destroy the context, check if never initialized.
            if (context != null)
                context.destroy();
            
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }
    
    private static FieldCreate fieldCreate = FieldFactory.getFieldCreate();
    private static PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();

    /**
	 * @param channelName
	 */
	public void execute(String channelName) {

		try {
		    
		    // initialize context
		    initialize();

		    ChannelRequester requester = new ChannelRequester()
		    {
		    	
				/* (non-Javadoc)
				 * @see org.epics.pvData.pv.Requester#getRequesterName()
				 */
				@Override
				public String getRequesterName() {
					return this.getClass().toString();
				}

				/* (non-Javadoc)
				 * @see org.epics.pvData.pv.Requester#message(java.lang.String, org.epics.pvData.pv.MessageType)
				 */
				@Override
				public void message(String message, MessageType messageType) {
					System.err.println("[" + messageType + "] " + message);
				}

				/* (non-Javadoc)
				 * @see org.epics.ca.client.ChannelRequester#channelCreated(org.epics.pvData.pv.Status, org.epics.ca.client.Channel)
				 */
				@Override
				public void channelCreated(Status status,
						org.epics.ca.client.Channel channel) {
				}

				/* (non-Javadoc)
				 * @see org.epics.ca.client.ChannelRequester#channelStateChange(org.epics.ca.client.Channel, org.epics.ca.client.Channel.ConnectionState)
				 */
				@Override
				public void channelStateChange(
						org.epics.ca.client.Channel c,
						ConnectionState connectionStatus) {
					System.out.println();
					System.out.println("Connection event for:");
					System.out.println(c);
					System.out.println();
				}
		    };

		    /*
			Scalar field = FieldFactory.getFieldCreate().createScalar("nameSearchV1", ScalarType.pvString);
			PVString searchData = (PVString)PVDataFactory.getPVDataCreate().createPVScalar(null, field);
			searchData.put("*");
		    context.introspectionSearch(searchData, dl, 3.0f, 0, -1);
		    Thread.sleep(1000);
		    */
		    
		    Channel ch = context.getProvider().createChannel(channelName, requester, CAConstants.CA_DEFAULT_PRIORITY);
			Thread.sleep(3000);

			/*
		    ch.process(compl, 0.0f);
		    Thread.sleep(1000);
			 */
			
		    if (ch.getConnectionState() == ConnectionState.CONNECTED)
		    {
		    	ChannelGetRequester channelGetRequester = new ChannelGetRequester()
		    	{
		    		private volatile ChannelGet channelGet;
		    		private volatile BitSet bitSet;
		    		private volatile PVStructure pvStructure;

		    		@Override
					public void channelGetConnect(Status status, ChannelGet channelGet, PVStructure pvStructure, BitSet bitSet) {
						this.channelGet = channelGet;
						this.pvStructure = pvStructure;
						this.bitSet = bitSet;
						
						System.out.println("channelGetConnect done: " + pvStructure.getStructure());
						
						channelGet.get(false);
					}

					volatile AtomicInteger count = new AtomicInteger(1);
					@Override
					public void getDone(Status success) {
						System.out.println("getDone: " + success);
						System.out.println("\tbitSet: " + bitSet);
						System.out.println("\tpvStrcuture: " + pvStructure);
						
						final int COUNT = 1;
						if (count.get() <= COUNT)
						new Thread(new Runnable() {
							
							@Override
							public void run() {
								try {
									Thread.sleep(1000);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								channelGet.get(count.getAndIncrement() >= COUNT);
							}
						}).start();

					}

					@Override
					public String getRequesterName() {
						return this.getClass().getName();
					}

					@Override
					public void message(String message, MessageType messageType) {
						System.err.println("[" + messageType + "] " + message);
					}
		    		
		    	};

		    	ChannelPutRequester channelPutRequester = new ChannelPutRequester()
		    	{
		    		private volatile ChannelPut channelPut;
		    		private volatile PVStructure pvStructure;
		    		private volatile BitSet bitSet;
		    		private volatile PVDouble val;
		    		
					@Override
					public void channelPutConnect(Status status, ChannelPut channelPut, PVStructure pvStructure, BitSet bitSet) {
						this.channelPut = channelPut;
						this.pvStructure = pvStructure;
						this.bitSet = bitSet;

						System.out.println("channelPutConnect done: " + pvStructure.getStructure());

						channelPut.lock();
						try {
	//						val = pvStructure.getIntField("value");
							val = pvStructure.getDoubleField("value");
							
							val.put(123);
							bitSet.set(val.getFieldOffset());
						} finally {
							channelPut.unlock();
						}
						
						channelPut.put(false);
					}

					final int COUNT = 3;
					volatile AtomicInteger count = new AtomicInteger();
					@Override
					public void putDone(Status success) {
						System.out.println("putDone: " + success);
						
						if (count.get() <= COUNT)
						new Thread(new Runnable() {
							
							@Override
							public void run() {
								try {
									Thread.sleep(1000);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								
								channelPut.get();
								
							}
						}).start();

					}

					@Override
					public void getDone(Status success) {
						System.out.println("getDone on put: " + success);
						System.out.println("\tpvStrcuture: " + pvStructure);
						
						new Thread(new Runnable() {
							
							@Override
							public void run() {
								try {
									Thread.sleep(1000);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								
								val.put(321 + count.get());
								bitSet.set(val.getFieldOffset());

								channelPut.put(count.getAndIncrement() >= COUNT);
								
							}
						}).start();

					}
					
					@Override
					public String getRequesterName() {
						return this.getClass().getName();
					}

					@Override
					public void message(String message, MessageType messageType) {
						System.err.println("[" + messageType + "] " + message);
					}
		    	};
		    	
		    	ChannelPutGetRequester channelPutGetRequester = new ChannelPutGetRequester()
		    	{
		    		private volatile ChannelPutGet channelPutGet;
		    		private volatile PVStructure pvPutStructure;
		    		private volatile PVStructure pvGetStructure;
		    		private volatile PVDouble val;

		    		/* (non-Javadoc)
					 * @see org.epics.ca.client.ChannelPutGetRequester#channelPutGetConnect(Status, org.epics.ca.client.ChannelPutGet, org.epics.pvData.pv.PVStructure, org.epics.pvData.pv.PVStructure)
					 */
					@Override
					public void channelPutGetConnect(
							Status status, 
							ChannelPutGet channelPutGet,
							PVStructure pvPutStructure, PVStructure pvGetStructure) {

						this.channelPutGet = channelPutGet;
						this.pvPutStructure = pvPutStructure;
						this.pvGetStructure = pvGetStructure;

						System.out.println("channelPutGetConnect done: ");
						System.out.println("\tput: " + pvPutStructure.getStructure());
						System.out.println("\tget: " + pvGetStructure.getStructure());
						
						channelPutGet.lock();
						try {
	//						val = this.pvPutStructure.getIntField("value");
							val = this.pvPutStructure.getDoubleField("value");
							val.put(123);
						} finally {
							channelPutGet.unlock();
						}

						channelPutGet.putGet(false);
					}

					/* (non-Javadoc)
					 * @see org.epics.ca.client.ChannelPutGetRequester#getGetDone(Status)
					 */
					@Override
					public void getGetDone(Status success) {
						// TODO Auto-generated method stub
						
					}

					/* (non-Javadoc)
					 * @see org.epics.ca.client.ChannelPutGetRequester#getPutDone(Status)
					 */
					@Override
					public void getPutDone(Status success) {
						// TODO Auto-generated method stub
						
					}

					/* (non-Javadoc)
					 * @see org.epics.ca.client.ChannelPutGetRequester#putGetDone(Status)
					 */
					@Override
					public void putGetDone(Status success) {
						System.out.println("putGetDone: " + success);
						System.out.println("\tpvStrcuture: " + pvGetStructure);
						
						final int COUNT = 1;
						if (count.get() <= COUNT)
						new Thread(new Runnable() {
							
							@Override
							public void run() {
								try {
									Thread.sleep(1000);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								
								channelPutGet.lock();
								try {
									val.put(321 + count.get());
								} finally {
									channelPutGet.unlock();
								}
								
								channelPutGet.putGet(count.getAndIncrement() >= COUNT);
							}
						}).start();
					}

					volatile AtomicInteger count = new AtomicInteger();


					@Override
					public String getRequesterName() {
						return this.getClass().getName();
					}

					@Override
					public void message(String message, MessageType messageType) {
						System.err.println("[" + messageType + "] " + message);
					}
		    		
		    	};
		    	
		    	ChannelProcessRequester channelProcessRequester = new ChannelProcessRequester() {
					
		    		volatile ChannelProcess channelProcess;
		    		
					@Override
					public void processDone(Status success) {
						System.out.println("processDone: " + success);
						channelProcess.process(true);
					}
					
					@Override
					public void channelProcessConnect(Status status, ChannelProcess channelProcess) {
						System.out.println("channelProcessConnect done");
						this.channelProcess = channelProcess;
						channelProcess.process(false);
					}
					
					@Override
					public String getRequesterName() {
						return this.getClass().getName();
					}

					@Override
					public void message(String message, MessageType messageType) {
						System.err.println("[" + messageType + "] " + message);
					}
					
				};
		    	
		    	MonitorRequester channelMonitorRequester = new MonitorRequester() {
					
					@Override
					public String getRequesterName() {
						return this.getClass().getName();
					}

					@Override
					public void message(String message, MessageType messageType) {
						System.err.println("[" + messageType + "] " + message);
					}
					
					@Override
					public void monitorEvent(Monitor monitor) {
						MonitorElement event = monitor.poll();
						System.out.println("monitorEvent: ");
						System.out.println("\tchangedBitSet: " + event.getChangedBitSet());
						System.out.println("\tpvStrcuture: " + event.getPVStructure());
						System.out.println("\toverrunBitSet: " + event.getOverrunBitSet());
						monitor.release(event);
					}
					
					/* (non-Javadoc)
					 * @see org.epics.pvData.monitor.MonitorRequester#monitorConnect(Status, org.epics.pvData.monitor.Monitor, org.epics.pvData.pv.Structure)
					 */
					@Override
					public void monitorConnect(Status status, Monitor channelMonitor, Structure structure) {
						System.out.println("channelMonitorConnect");
						channelMonitor.start();
					}

					@Override
					public void unlisten(Monitor monitor) {
						// TODO Auto-generated method stub
						
					}
				};

		    	ChannelArrayRequester channelArrayRequester = new ChannelArrayRequester() {

		    		volatile ChannelArray channelArray;
		    		volatile PVArray pvArray;
		    		
					@Override
					public void channelArrayConnect(Status status, ChannelArray channelArray, PVArray pvArray) {
						this.channelArray = channelArray;
						this.pvArray = pvArray;
						System.out.println("channelArrayConnect");
						System.out.println("\tpvArray" + pvArray.getField());
						
						//pvArray.setCapacity(2);
						//((PVStringArray)pvArray).put(0, 1, new String[] { "test" }, 0);
						//channelArray.putArray(false, 1, 1);
						channelArray.getArray(true, 1, 2);
					}

					@Override
					public void getArrayDone(Status success) {
						System.out.println("getArrayDone: " + success);
						System.out.println("\tpvArray" + pvArray);
					}

					@Override
					public void putArrayDone(Status success) {
						System.out.println("putArrayDone: " + success);

						channelArray.getArray(true, 1, 2);
					}

					@Override
					public void setLengthDone(Status status) {
						System.out.println("setLengthDone: " + status);
					}

					@Override
					public String getRequesterName() {
						return this.getClass().getName();
					}

					@Override
					public void message(String message, MessageType messageType) {
						System.err.println("[" + messageType + "] " + message);
					}
					
		    	};
		    	
		    	GetFieldRequester getFieldRequester = new GetFieldRequester() {
					
					@Override
					public void getDone(Status status, Field field) {
						System.out.println("getDone (field): " + field);
					}

					@Override
					public String getRequesterName() {
						return this.getClass().getName();
					}

					@Override
					public void message(String message, MessageType messageType) {
						System.err.println("[" + messageType + "] " + message);
					}
				};
		    	PVStructure pvRequest = pvDataCreate.createPVStructure(null, "", new Field[0]);
		        Field newField = fieldCreate.createScalar("alarm", ScalarType.pvString);
		        PVString pvString = (PVString)pvDataCreate.createPVField(pvRequest, newField);
		        pvString.put("alarm");
		        pvRequest.appendPVField(pvString);
		        newField = fieldCreate.createScalar("timeStamp", ScalarType.pvString);
		        pvString = (PVString)pvDataCreate.createPVField(pvRequest, newField);
		        pvString.put("timeStamp");
		        pvRequest.appendPVField(pvString);
		        newField = fieldCreate.createScalar("value", ScalarType.pvString);
		        pvString = (PVString)pvDataCreate.createPVField(pvRequest, newField);
		        pvString.put("value");
		        pvRequest.appendPVField(pvString);
		    	
		    	PVStructure pvValueRequest = pvDataCreate.createPVStructure(null, "", new Field[0]);
		        newField = fieldCreate.createScalar("value", ScalarType.pvString);
		        pvString = (PVString)pvDataCreate.createPVField(pvRequest, newField);
		        pvString.put("value");
		        pvValueRequest.appendPVField(pvString);

		        PVStructure pvOption = pvDataCreate.createPVStructure(null, "pvOption", new Field[0]);
	            PVString pvAlgorithm = (PVString)pvDataCreate.createPVScalar(pvOption, "algorithm", ScalarType.pvString);
	            pvAlgorithm.put("onChange");		// TODO constant!!!
	            pvOption.appendPVField(pvAlgorithm);
	            PVInt pvQueueSize = (PVInt)pvDataCreate.createPVScalar(pvOption, "queueSize", ScalarType.pvInt);
	            pvQueueSize.put(10);
	            pvOption.appendPVField(pvQueueSize);
	            PVDouble pvDeadband = (PVDouble)pvDataCreate.createPVScalar(pvOption, "deadband", ScalarType.pvDouble);
	            pvDeadband.put(0.1);
	            pvOption.appendPVField(pvDeadband);
		        
		        
		        //ch.createChannelGet(channelGetRequester, pvRequest, "get", true, true, null);
		    	//ch.createChannelPut(channelPutRequester, pvRequest, "put", true, true, null);
		    	//ch.createChannelPutGet(channelPutGetRequester, pvValueRequest, "put", true, pvRequest, "get", true, true, null);
		        //ch.createMonitor(channelMonitorRequester, pvRequest, "monitor", pvOption);
		        ch.createChannelProcess(channelProcessRequester, null);
		        
	            //ch.createChannelArray(channelArrayRequester, "alarm.severity.choices", null);
	            //ch.getField(getFieldRequester, null);
	            //ch.getField(getFieldRequester, "alarm.severity.choices");
	            
		    }
			Thread.sleep(300000);

		    System.out.println("Done.");
			
		} catch (Throwable th) {
			th.printStackTrace();
		}
		finally {
		    // always finalize
		    destroy();
		}

	}
	
	
	/**
	 * Program entry point. 
	 * @param args	command-line arguments
	 */
	public static void main(String[] args) {

	    // check command-line arguments
		if (args.length < 1) {
			System.out.println(
				"usage: java " + TestExample.class.getName() + " <pvname>");
			System.exit(1);
		}
	
		// execute
		new TestExample().execute(args[0]);
	}
	
}
