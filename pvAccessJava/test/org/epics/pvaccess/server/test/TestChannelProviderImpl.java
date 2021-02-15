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

package org.epics.pvaccess.server.test;

import org.epics.pvaccess.PVFactory;
import org.epics.pvaccess.client.*;
import org.epics.pvaccess.server.test.helpers.*;
import org.epics.pvaccess.server.test.helpers.PVTopStructure.PVTopStructureListener;
import org.epics.pvdata.factory.ConvertFactory;
import org.epics.pvdata.misc.BitSet;
import org.epics.pvdata.misc.ThreadPriority;
import org.epics.pvdata.misc.Timer;
import org.epics.pvdata.misc.TimerFactory;
import org.epics.pvdata.monitor.Monitor;
import org.epics.pvdata.monitor.MonitorElement;
import org.epics.pvdata.monitor.MonitorRequester;
import org.epics.pvdata.pv.*;
import org.epics.pvdata.pv.Status.StatusType;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of a channel provider for tests.
 *
 * @author msekoranja
 */
public class TestChannelProviderImpl implements ChannelProvider {
    private static final FieldCreate fieldCreate = PVFactory.getFieldCreate();
    private static final PVDataCreate pvDataCreate = PVFactory.getPVDataCreate();
    private static final StatusCreate statusCreate = PVFactory.getStatusCreate();
    private static final Convert convert = ConvertFactory.getConvert();

    private static final Status okStatus = statusCreate.getStatusOK();
    private static final Status fieldDoesNotExistStatus =
            statusCreate.createStatus(StatusType.ERROR, "field does not exist", null);
    private static final Status destroyedStatus =
            statusCreate.createStatus(StatusType.ERROR, "channel destroyed", null);
    private static final Status illegalRequestStatus =
            statusCreate.createStatus(StatusType.ERROR, "illegal pvRequest", null);
    private static final Status subFieldDoesNotExistStatus =
            statusCreate.createStatus(StatusType.ERROR, "subField does not exist", null);
    //private static final Status subFieldNotDefinedStatus =
    //	statusCreate.createStatus(StatusType.ERROR, "subField not defined", null);
    private static final Status subFieldNotArrayStatus =
            statusCreate.createStatus(StatusType.ERROR, "subField is not an array", null);

    class TestChannelImpl implements Channel {

        class TestBasicChannelRequest implements ChannelRequest {
            protected final Channel channel;
            protected final PVTopStructure pvTopStructure;
            protected final AtomicBoolean destroyed = new AtomicBoolean();
            protected final Mapper mapper;
            protected final ReentrantLock lock = new ReentrantLock();
            protected volatile boolean lastRequest = false;

            public TestBasicChannelRequest(Channel channel, PVTopStructure pvTopStructure, PVStructure pvRequest) {
                this.channel = channel;
                this.pvTopStructure = pvTopStructure;

                if (pvRequest != null)
                    mapper = new Mapper(pvTopStructure.getPVStructure(), pvRequest);
                else
                    mapper = null;

                registerRequest(this);
            }

            public void lock() {
                lock.lock();
            }

            public void unlock() {
                lock.unlock();
            }

            public final void destroy() {
                if (destroyed.getAndSet(true))
                    return;
                unregisterRequest(this);
                internalDestroy();
            }

            protected void internalDestroy() {
                // noop
            }

            public void cancel() {
                // noop, not supported
            }

            public Channel getChannel() {
                return channel;
            }

            public void lastRequest() {
                lastRequest = true;
            }

        }

        class TestChannelGetImpl extends TestBasicChannelRequest implements ChannelGet, PVTopStructureListener {
            private final ChannelGetRequester channelGetRequester;
            private final PVStructure pvGetStructure;
            private final BitSet bitSet;        // for user
            private final BitSet activeBitSet;        // changed monitoring
            private final boolean process;
            private final AtomicBoolean firstGet = new AtomicBoolean(true);

            public TestChannelGetImpl(PVTopStructure pvTopStructure, ChannelGetRequester channelGetRequester, PVStructure pvRequest) {
                super(TestChannelImpl.this, pvTopStructure, pvRequest);

                this.channelGetRequester = channelGetRequester;

                process = PVRequestUtils.getProcess(pvRequest);

                pvGetStructure = mapper.getCopyStructure();
                activeBitSet = new BitSet(pvGetStructure.getNumberFields());
                activeBitSet.set(0);    // initial get gets all

                bitSet = new BitSet(pvGetStructure.getNumberFields());

                channelGetRequester.channelGetConnect(okStatus, this, pvGetStructure.getStructure());
            }

            public void get() {
                if (destroyed.get()) {
                    channelGetRequester.getDone(destroyedStatus, this, null, null);
                    return;
                }

                lock();
                pvTopStructure.lock();
                try {
                    if (process)
                        pvTopStructure.process();

                    mapper.updateCopyStructureOriginBitSet(activeBitSet, bitSet);
                    activeBitSet.clear();
                    if (firstGet.getAndSet(false))
                        pvTopStructure.registerListener(this);
                } finally {
                    pvTopStructure.unlock();
                    unlock();
                }

                channelGetRequester.getDone(okStatus, this, pvGetStructure, bitSet);

                if (lastRequest)
                    destroy();
            }

            @Override
            public void internalDestroy() {
                pvTopStructure.unregisterListener(this);
            }

            public void topStructureChanged(BitSet changedBitSet) {
                lock();
                activeBitSet.or(changedBitSet);
                unlock();
            }

        }

        // TODO only queueSize==1 impl.
        class TestChannelMonitorImpl extends TestBasicChannelRequest implements Monitor, PVTopStructureListener, MonitorElement {
            private final MonitorRequester monitorRequester;
            private final PVStructure pvGetStructure;
            private final BitSet bitSet;        // for user
            private final BitSet activeBitSet;        // changed monitoring
            private final AtomicBoolean started = new AtomicBoolean(false);


            // TODO tmp
            private final BitSet allChanged;
            private final BitSet noOverrun;


            public TestChannelMonitorImpl(PVTopStructure pvTopStructure, MonitorRequester monitorRequester, PVStructure pvRequest) {
                super(TestChannelImpl.this, pvTopStructure, pvRequest);

                this.monitorRequester = monitorRequester;

                pvGetStructure = mapper.getCopyStructure();
                activeBitSet = new BitSet(pvGetStructure.getNumberFields());
                activeBitSet.set(0);    // initial get gets all

                bitSet = new BitSet(pvGetStructure.getNumberFields());


                allChanged = new BitSet(pvGetStructure.getNumberFields());
                allChanged.set(0);
                noOverrun = new BitSet(pvGetStructure.getNumberFields());

                monitorRequester.monitorConnect(okStatus, this, pvGetStructure.getStructure());
            }

            @Override
            public void internalDestroy() {
                pvTopStructure.unregisterListener(this);
            }

            public void topStructureChanged(BitSet changedBitSet) {
                lock();
                activeBitSet.or(changedBitSet);

                // add to queue, trigger
                lock();
                pvTopStructure.lock();
                try {
                    mapper.updateCopyStructureOriginBitSet(activeBitSet, bitSet);
                    activeBitSet.clear();
                } finally {
                    pvTopStructure.unlock();
                    unlock();
                }
                unlock();
                // TODO not a safe copy...
                monitorRequester.monitorEvent(this);
            }

            public Status start() {
                if (started.getAndSet(true))
                    return okStatus;

                // force monitor immediately
                topStructureChanged(allChanged);

                pvTopStructure.registerListener(this);

                return okStatus;
            }

            public Status stop() {
                if (!started.getAndSet(false))
                    return okStatus;

                // TODO clear queue

                pvTopStructure.unregisterListener(this);

                return okStatus;
            }


            private final AtomicBoolean pooled = new AtomicBoolean(false);

            public MonitorElement poll() {
                if (pooled.getAndSet(true))
                    return null;

                return this;
            }

            public void release(MonitorElement monitorElement) {
                pooled.set(false);
            }

            /* (non-Javadoc)
             * @see org.epics.pvdata.monitor.MonitorElement#getPVStructure()
             */
            public PVStructure getPVStructure() {
                return pvGetStructure;
            }

            /* (non-Javadoc)
             * @see org.epics.pvdata.monitor.MonitorElement#getChangedBitSet()
             */
            public BitSet getChangedBitSet() {
                return allChanged;
            }

            /* (non-Javadoc)
             * @see org.epics.pvdata.monitor.MonitorElement#getOverrunBitSet()
             */
            public BitSet getOverrunBitSet() {
                return noOverrun;
            }


        }


        class TestChannelProcessImpl extends TestBasicChannelRequest implements ChannelProcess {
            private final ChannelProcessRequester channelProcessRequester;

            public TestChannelProcessImpl(PVTopStructure pvTopStructure, ChannelProcessRequester channelProcessRequester, PVStructure pvRequest) {
                super(TestChannelImpl.this, pvTopStructure, pvRequest);

                this.channelProcessRequester = channelProcessRequester;

                channelProcessRequester.channelProcessConnect(okStatus, this);
            }

            /* (non-Javadoc)
             * @see org.epics.pvaccess.client.ChannelProcess#process(boolean)
             */
            public void process() {
                if (destroyed.get()) {
                    channelProcessRequester.processDone(destroyedStatus, this);
                    return;
                }

                pvTopStructure.lock();
                try {
                    pvTopStructure.process();
                } finally {
                    pvTopStructure.unlock();
                }

                channelProcessRequester.processDone(okStatus, this);

                if (lastRequest)
                    destroy();
            }
        }


        class TestChannelRPCImpl extends TestBasicChannelRequest implements ChannelRPC {
            private final ChannelRPCRequester channelRPCRequester;

            public TestChannelRPCImpl(PVTopStructure pvTopStructure, ChannelRPCRequester channelRPCRequester, PVStructure pvRequest) {
                super(TestChannelImpl.this, pvTopStructure, pvRequest);

                this.channelRPCRequester = channelRPCRequester;

                channelRPCRequester.channelRPCConnect(okStatus, this);
            }

            public void request(PVStructure pvArgument) {
                if (destroyed.get()) {
                    channelRPCRequester.requestDone(destroyedStatus, this, null);
                    return;
                }

                // TODO async support
                PVStructure result = null;
                Status status = okStatus;
                pvTopStructure.lock();
                try {
                    result = pvTopStructure.request(pvArgument);
                } catch (Throwable th) {
                    status = statusCreate.createStatus(StatusType.ERROR, "exceptuon caught: " + th.getMessage(), th);
                } finally {
                    pvTopStructure.unlock();
                }

                channelRPCRequester.requestDone(status, this, result);

                if (lastRequest)
                    destroy();
            }
        }


        class TestChannelPutImpl extends TestBasicChannelRequest implements ChannelPut {
            private final ChannelPutRequester channelPutRequester;
            private final PVStructure pvPutStructure;
            private final BitSet bitSet;        // for user
            private final boolean process;

            public TestChannelPutImpl(PVTopStructure pvTopStructure, ChannelPutRequester channelPutRequester, PVStructure pvRequest) {
                super(TestChannelImpl.this, pvTopStructure, pvRequest);

                this.channelPutRequester = channelPutRequester;

                process = PVRequestUtils.getProcess(pvRequest);

                pvPutStructure = mapper.getCopyStructure();
                bitSet = new BitSet(pvPutStructure.getNumberFields());

                channelPutRequester.channelPutConnect(okStatus, this, pvPutStructure.getStructure());
            }

            public void put(PVStructure pvStructure, BitSet pvBitSet) {
                if (destroyed.get()) {
                    channelPutRequester.putDone(destroyedStatus, this);
                    return;
                }

                lock();
                pvTopStructure.lock();
                try {
                    mapper.updateOriginStructure(pvStructure, pvBitSet);

                    if (process)
                        pvTopStructure.process();

                } finally {
                    pvTopStructure.unlock();
                    unlock();
                }

                channelPutRequester.putDone(okStatus, this);

                if (lastRequest)
                    destroy();
            }

            public void get() {
                if (destroyed.get()) {
                    channelPutRequester.getDone(destroyedStatus, this, null, null);
                    return;
                }

                lock();
                pvTopStructure.lock();
                try {
                    mapper.updateCopyStructure(null);
                } finally {
                    pvTopStructure.unlock();
                    unlock();
                }

                // TODO
                bitSet.clear();
                bitSet.set(0);
                channelPutRequester.getDone(okStatus, this, pvPutStructure, bitSet);
            }

        }


        class TestChannelScalarArrayImpl extends TestBasicChannelRequest implements ChannelArray {
            private final ChannelArrayRequester channelArrayRequester;
            private final PVScalarArray pvArray;
            private final PVScalarArray pvCopy;
            private final boolean process;

            public TestChannelScalarArrayImpl(PVTopStructure pvTopStructure, ChannelArrayRequester channelArrayRequester, PVScalarArray array, PVStructure pvRequest) {
                super(TestChannelImpl.this, pvTopStructure, null);

                this.channelArrayRequester = channelArrayRequester;
                this.pvArray = array;
                this.pvCopy = pvDataCreate.createPVScalarArray(pvArray.getScalarArray().getElementType());

                process = false; // TODO PVRequestUtils.getProcess(pvRequest);

                channelArrayRequester.channelArrayConnect(okStatus, this, pvCopy.getArray());
            }

            public void putArray(PVArray pvCopyArray, int offset, int count, int stride) {
                PVScalarArray pvCopy = (PVScalarArray) pvCopyArray;
                if (destroyed.get()) {
                    channelArrayRequester.putArrayDone(destroyedStatus, this);
                    return;
                }

                if (stride != 1)
                    throw new UnsupportedOperationException("stride != 1");

                lock();
                pvTopStructure.lock();
                try {
                    if (count <= 0) count = pvCopy.getLength();
                    convert.copyScalarArray(pvCopy, 0, (PVScalarArray) pvArray, offset, count);

                    if (process)
                        pvTopStructure.process();

                } finally {
                    pvTopStructure.unlock();
                    unlock();
                }

                channelArrayRequester.putArrayDone(okStatus, this);

                if (lastRequest)
                    destroy();
            }

            public void getArray(int offset, int count, int stride) {
                if (destroyed.get()) {
                    channelArrayRequester.getArrayDone(destroyedStatus, this, null);
                    return;
                }

                if (stride != 1)
                    throw new UnsupportedOperationException("stride != 1");

                lock();
                pvTopStructure.lock();
                try {
                    //if (process)
                    //	pvTopStructure.process();

                    if (count == 0) count = pvArray.getLength() - offset;
                    int len = convert.copyScalarArray(pvArray, offset, pvCopy, 0, count);
                    if (!pvCopy.isImmutable()) pvCopy.setLength(len);
                } finally {
                    pvTopStructure.unlock();
                    unlock();
                }

                channelArrayRequester.getArrayDone(okStatus, this, pvCopy);

                if (lastRequest)
                    destroy();
            }

            public void setLength(int length) {
                if (destroyed.get()) {
                    channelArrayRequester.setLengthDone(destroyedStatus, this);
                    return;
                }

                // TODO process???

                lock();
                pvTopStructure.lock();
                try {
                    if (pvArray.getLength() != length) pvArray.setLength(length);
                } finally {
                    pvTopStructure.unlock();
                    unlock();
                }

                channelArrayRequester.setLengthDone(okStatus, this);

                if (lastRequest)
                    destroy();
            }

            public void getLength() {
                if (destroyed.get()) {
                    channelArrayRequester.getLengthDone(destroyedStatus, this, 0);
                    return;
                }

                int length;

                lock();
                pvTopStructure.lock();
                try {
                    length = pvArray.getLength();
                } finally {
                    pvTopStructure.unlock();
                    unlock();
                }

                channelArrayRequester.getLengthDone(okStatus, this, length);

                if (lastRequest)
                    destroy();
            }


        }

        class TestChannelPutGetImpl extends TestBasicChannelRequest implements ChannelPutGet {
            private final ChannelPutGetRequester channelPutGetRequester;
            private PVStructure pvGetStructure;
            private PVStructure pvPutStructure;
            private BitSet pvGetBitSet;
            private BitSet pvPutBitSet;
            private Mapper putMapper;
            private Mapper getMapper;
            private boolean process;

            public TestChannelPutGetImpl(PVTopStructure pvTopStructure, ChannelPutGetRequester channelPutGetRequester, PVStructure pvRequest) {
                super(TestChannelImpl.this, pvTopStructure, null);

                this.channelPutGetRequester = channelPutGetRequester;

                PVField pvField = pvRequest.getSubField("putField");
                if (pvField == null || pvField.getField().getType() != Type.structure) {
                    channelPutGetRequester.message("pvRequest does not have a putField request structure", MessageType.error);
                    channelPutGetRequester.message(pvRequest.toString(), MessageType.warning);
                    channelPutGetRequester.channelPutGetConnect(illegalRequestStatus, null, null, null);
                    return;
                }
                putMapper = new Mapper(pvTopStructure.getPVStructure(), pvRequest, "putField");


                pvField = pvRequest.getSubField("getField");
                if (pvField == null || pvField.getField().getType() != Type.structure) {
                    channelPutGetRequester.message("pvRequest does not have a getField request structure", MessageType.error);
                    channelPutGetRequester.message(pvRequest.toString(), MessageType.warning);
                    channelPutGetRequester.channelPutGetConnect(illegalRequestStatus, null, null, null);
                    return;
                }
                getMapper = new Mapper(pvTopStructure.getPVStructure(), pvRequest, "getField");

                process = PVRequestUtils.getProcess(pvRequest);

                pvPutStructure = putMapper.getCopyStructure();
                pvGetStructure = getMapper.getCopyStructure();

                // TODO
                pvPutBitSet = new BitSet(pvPutStructure.getNumberFields());
                pvPutBitSet.set(0);
                pvGetBitSet = new BitSet(pvGetStructure.getNumberFields());
                pvGetBitSet.set(0);

                channelPutGetRequester.channelPutGetConnect(okStatus, this, pvPutStructure.getStructure(), pvGetStructure.getStructure());
            }

            /* (non-Javadoc)
             * @see org.epics.pvaccess.client.ChannelPutGet#putGet(boolean)
             */
            public void putGet(PVStructure pvPutStructure, BitSet pvPutBitSet) {
                if (destroyed.get()) {
                    channelPutGetRequester.putGetDone(destroyedStatus, this, null, null);
                    return;
                }

                lock();
                pvTopStructure.lock();
                try {
                    putMapper.updateOriginStructure(pvPutStructure, pvPutBitSet);
                    if (process)
                        pvTopStructure.process();
                    getMapper.updateCopyStructure(null);
                } finally {
                    pvTopStructure.unlock();
                    unlock();
                }

                channelPutGetRequester.putGetDone(okStatus, this, pvGetStructure, pvGetBitSet);

                if (lastRequest)
                    destroy();
            }

            /* (non-Javadoc)
             * @see org.epics.pvaccess.client.ChannelPutGet#getPut()
             */
            public void getPut() {
                if (destroyed.get()) {
                    channelPutGetRequester.getPutDone(destroyedStatus, this, null, null);
                    return;
                }

                lock();
                pvTopStructure.lock();
                try {
                    putMapper.updateCopyStructure(null);
                } finally {
                    pvTopStructure.unlock();
                    unlock();
                }

                channelPutGetRequester.getPutDone(okStatus, this, pvPutStructure, pvPutBitSet);
            }

            /* (non-Javadoc)
             * @see org.epics.pvaccess.client.ChannelPutGet#getGet()
             */
            public void getGet() {
                if (destroyed.get()) {
                    channelPutGetRequester.getGetDone(destroyedStatus, this, null, null);
                    return;
                }

                lock();
                pvTopStructure.lock();
                try {
                    getMapper.updateCopyStructure(null);
                } finally {
                    pvTopStructure.unlock();
                    unlock();
                }

                channelPutGetRequester.getGetDone(okStatus, this, pvGetStructure, pvGetBitSet);
            }

        }

        private final String channelName;
        private final ChannelRequester channelRequester;
        private final PVTopStructure pvTopStructure;

        private final ArrayList<ChannelRequest> channelRequests = new ArrayList<ChannelRequest>();

        TestChannelImpl(String channelName, ChannelRequester channelRequester, PVTopStructure pvTopStructure) {
            this.channelName = channelName;
            this.channelRequester = channelRequester;

            this.pvTopStructure = pvTopStructure;

            setConnectionState(ConnectionState.CONNECTED);
        }

        public void registerRequest(ChannelRequest request) {
            synchronized (channelRequests) {
                channelRequests.add(request);
            }
        }

        public void unregisterRequest(ChannelRequest request) {
            synchronized (channelRequests) {
                channelRequests.remove(request);
            }
        }

        private void destroyRequests() {
            synchronized (channelRequests) {
                while (!channelRequests.isEmpty())
                    channelRequests.get(channelRequests.size() - 1).destroy();
            }
        }

        public String getRequesterName() {
            return channelRequester.getRequesterName();
        }

        public void message(String message, MessageType messageType) {
            System.err.println("[" + messageType + "] " + message);
        }

        public ChannelProvider getProvider() {
            return TestChannelProviderImpl.this;
        }

        public String getRemoteAddress() {
            return "local";
        }

        private volatile ConnectionState connectionState = ConnectionState.NEVER_CONNECTED;

        private void setConnectionState(ConnectionState state) {
            this.connectionState = state;
            channelRequester.channelStateChange(this, state);
        }

        public ConnectionState getConnectionState() {
            return connectionState;
        }

        public boolean isConnected() {
            return getConnectionState() == ConnectionState.CONNECTED;
        }

        private final AtomicBoolean destroyed = new AtomicBoolean(false);

        public void destroy() {
            if (destroyed.getAndSet(true) == false) {
                destroyRequests();

                setConnectionState(ConnectionState.DISCONNECTED);
                setConnectionState(ConnectionState.DESTROYED);
            }
        }

        public String getChannelName() {
            return channelName;
        }

        public ChannelRequester getChannelRequester() {
            return channelRequester;
        }

        public void getField(GetFieldRequester requester, String subField) {

            if (requester == null)
                throw new IllegalArgumentException("requester");

            if (destroyed.get()) {
                requester.getDone(destroyedStatus, null);
                return;
            }

            Field field;
            if (subField == null || subField.trim().length() == 0)
                field = pvTopStructure.getPVStructure().getStructure();
            else
                field = pvTopStructure.getPVStructure().getStructure().getField(subField);

            if (field != null)
                requester.getDone(okStatus, field);
            else
                requester.getDone(fieldDoesNotExistStatus, null);
        }

        public AccessRights getAccessRights(PVField pvField) {
            // TODO implement
            return AccessRights.readWrite;
        }

        public ChannelProcess createChannelProcess(
                ChannelProcessRequester channelProcessRequester,
                PVStructure pvRequest) {

            if (channelProcessRequester == null)
                throw new IllegalArgumentException("channelProcessRequester");

            if (destroyed.get()) {
                channelProcessRequester.channelProcessConnect(destroyedStatus, null);
                return null;
            }

            return new TestChannelProcessImpl(pvTopStructure, channelProcessRequester, pvRequest);
        }

        public ChannelGet createChannelGet(
                ChannelGetRequester channelGetRequester, PVStructure pvRequest) {

            if (channelGetRequester == null)
                throw new IllegalArgumentException("channelGetRequester");

            if (pvRequest == null)
                throw new IllegalArgumentException("pvRequest");

            if (destroyed.get()) {
                channelGetRequester.channelGetConnect(destroyedStatus, null, null);
                return null;
            }

            return new TestChannelGetImpl(pvTopStructure, channelGetRequester, pvRequest);
        }

        public ChannelPut createChannelPut(
                ChannelPutRequester channelPutRequester, PVStructure pvRequest) {

            if (channelPutRequester == null)
                throw new IllegalArgumentException("channelPutRequester");

            if (pvRequest == null)
                throw new IllegalArgumentException("pvRequest");

            if (destroyed.get()) {
                channelPutRequester.channelPutConnect(destroyedStatus, null, null);
                return null;
            }

            return new TestChannelPutImpl(pvTopStructure, channelPutRequester, pvRequest);
        }

        public ChannelPutGet createChannelPutGet(
                ChannelPutGetRequester channelPutGetRequester,
                PVStructure pvRequest) {

            if (channelPutGetRequester == null)
                throw new IllegalArgumentException("channelPutGetRequester");

            if (pvRequest == null)
                throw new IllegalArgumentException("pvRequest");

            if (destroyed.get()) {
                channelPutGetRequester.channelPutGetConnect(destroyedStatus, null, null, null);
                return null;
            }

            return new TestChannelPutGetImpl(pvTopStructure, channelPutGetRequester, pvRequest);
        }

        public ChannelRPC createChannelRPC(
                ChannelRPCRequester channelRPCRequester, PVStructure pvRequest) {

            if (channelRPCRequester == null)
                throw new IllegalArgumentException("channelRPCRequester");

			/*
			if (pvRequest == null)
				throw new IllegalArgumentException("pvRequest");
			*/

            if (destroyed.get()) {
                channelRPCRequester.channelRPCConnect(destroyedStatus, null);
                return null;
            }

            return new TestChannelRPCImpl(pvTopStructure, channelRPCRequester, pvRequest);
        }

        public Monitor createMonitor(MonitorRequester monitorRequester,
                                     PVStructure pvRequest) {

            if (monitorRequester == null)
                throw new IllegalArgumentException("monitorRequester");

            if (pvRequest == null)
                throw new IllegalArgumentException("pvRequest");

            if (destroyed.get()) {
                monitorRequester.monitorConnect(destroyedStatus, null, null);
                return null;
            }

            return new TestChannelMonitorImpl(pvTopStructure, monitorRequester, pvRequest);
        }

        public ChannelArray createChannelArray(
                ChannelArrayRequester channelArrayRequester,
                PVStructure pvRequest) {

            if (channelArrayRequester == null)
                throw new IllegalArgumentException("channelArrayRequester");

            if (pvRequest == null)
                throw new IllegalArgumentException("pvRequest");

            if (destroyed.get()) {
                channelArrayRequester.channelArrayConnect(destroyedStatus, null, null);
                return null;
            }
            PVField[] pvFields = pvRequest.getPVFields();
            if (pvFields.length != 1) {
                channelArrayRequester.channelArrayConnect(illegalRequestStatus, null, null);
                return null;
            }
            PVField pvField = pvFields[0];
            StringBuilder fieldName = new StringBuilder();
            while (pvField != null) {
                String name = pvField.getFieldName();
                if (name != null && name.length() > 0) {
                    if (fieldName.length() > 0) fieldName.append('.');
                    fieldName.append(name);
                }
                PVStructure pvs = (PVStructure) pvField;
                pvFields = pvs.getPVFields();
                if (pvFields.length != 1) break;
                pvField = pvFields[0];
            }
            if (fieldName.toString().startsWith("field."))
                fieldName = new StringBuilder(fieldName.substring(6));
            pvField = pvTopStructure.getPVStructure().getSubField(fieldName.toString());
            if (pvField == null) {
                channelArrayRequester.channelArrayConnect(subFieldDoesNotExistStatus, null, null);
                return null;
            }
            if (pvField.getField().getType() == Type.structureArray) {
                //PVStructureArray pvArray = (PVStructureArray)pvField;
                throw new RuntimeException("todo todo");
                //return new TestChannelStructureArrayImpl(pvTopStructure,channelArrayRequester,pvArray,pvRequest);
            }
            if (pvField.getField().getType() != Type.scalarArray) {
                channelArrayRequester.channelArrayConnect(subFieldNotArrayStatus, null, null);
                return null;
            }
            PVScalarArray pvArray = (PVScalarArray) pvField;
            return new TestChannelScalarArrayImpl(pvTopStructure, channelArrayRequester, pvArray, pvRequest);
        }
    }


    public static final String PROVIDER_NAME = "test";

    public TestChannelProviderImpl() {
        // not nice but users would like to see this
        System.out.println("Created 'test' ChannelProvider that hosts the following channels: "
                + HOSTED_CHANNELS_SET.toString());
    }

    public String getProviderName() {
        return PROVIDER_NAME;
    }

    private ChannelFind channelFind = new ChannelFind() {

        public ChannelProvider getChannelProvider() {
            return TestChannelProviderImpl.this;
        }

        public void cancel() {
            // noop, sync call
        }
    };

    private static final String[] HOSTED_CHANNELS =
            new String[]{
                    "counter",
                    "simpleCounter",
                    "valueOnly",
                    "arrayDouble",
                    "sum",
                    "testAny"
            };

    private static Set<String> HOSTED_CHANNELS_SET =
            new HashSet<String>(Arrays.asList(HOSTED_CHANNELS));

    private boolean isSupported(String channelName) {
        return HOSTED_CHANNELS_SET.contains(channelName) || channelName.startsWith("test");
    }

    private static final Timer timer = TimerFactory.create("counter timer", ThreadPriority.middle);
    private final HashMap<String, PVTopStructure> tops = new HashMap<String, PVTopStructure>();

    private static final Pattern TESTARRAY_PATTERN = Pattern.compile("testArray(\\d+)(.+)?");

    private synchronized PVTopStructure getTopStructure(String channelName) {
        //synchronized (tops) {
        PVTopStructure cached = tops.get(channelName);
        if (cached != null)
            return cached;
        //}

        PVTopStructure retVal;

        // inc with 1Hz
        if (channelName.equals("counter")) {
            retVal = new CounterTopStructure(1.0, timer);
        }
        // inc on process only
        else if (channelName.equals("simpleCounter")) {
            retVal = new CounterTopStructure(0.0, timer);
        } else if (channelName.equals("valueOnly")) {
            retVal = new PVTopStructure(fieldCreate.createScalar(ScalarType.pvDouble));
        } else if (channelName.equals("sum")) {
            retVal = new RPCTopStructure();
        } else if (channelName.equals("arrayDouble")) {
            retVal = new PVTopStructure(fieldCreate.createScalarArray(ScalarType.pvDouble));
            PVDoubleArray pvArray = (PVDoubleArray) retVal.getPVStructure().getSubField("value");
            final double[] ARRAY_VALUE = new double[]{1.1, 2.2, 3.3, 4.4, 5.5};
            pvArray.setCapacity(ARRAY_VALUE.length);
            pvArray.setLength(ARRAY_VALUE.length);
            pvArray.put(0, ARRAY_VALUE.length, ARRAY_VALUE, 0);
        } else if (channelName.startsWith("testArray")) {
            Matcher matcher = TESTARRAY_PATTERN.matcher(channelName);
            int length = 1024 * 1024;
            double inc = 1.1;
            if (matcher.matches()) {
                length = Integer.parseInt(matcher.group(1));
                inc = 1.0;
            }

            Structure scalarArrayStructure = fieldCreate.createStructure("epics:nt/NTScalarArray:1.0",
                    new String[]{"value"},
                    new Field[]{fieldCreate.createScalarArray(ScalarType.pvDouble)});

            retVal = new PVTopStructure(pvDataCreate.createPVStructure(scalarArrayStructure));
            PVDoubleArray pvArray = (PVDoubleArray) retVal.getPVStructure().getSubField("value");
            pvArray.setCapacity(length);
            pvArray.setLength(length);

            double v = 0.0;
            int ix = 0;
            int ARRAY_SIZE = 1024;
            int stage = 0;
            double[] array = new double[ARRAY_SIZE];
            while (ix < length) {
                int toFill = length - ix;
                stage = Math.min(toFill, ARRAY_SIZE);
                for (int i = 0; i < stage; i++) {
                    array[i] = v;
                    v += inc;
                }
                pvArray.put(ix, stage, array, 0);
                ix += stage;
            }
        }
        // 1Hz changing union
        else if (channelName.equals("testAny")) {
            retVal = new ChangingVariantUnionTopStructure(1.0, timer);
        }
        // else if (channelName.startsWith("test"))	// double scalar
        else {
            // default
            retVal = new PVTopStructure(fieldCreate.createScalar(ScalarType.pvDouble));
        }

        //synchronized (tops) {
        tops.put(channelName, retVal);
        //}

        return retVal;
    }

    public ChannelFind channelFind(String channelName,
                                   ChannelFindRequester channelFindRequester) {

        if (channelName == null)
            throw new IllegalArgumentException("channelName");

        if (channelFindRequester == null)
            throw new IllegalArgumentException("channelFindRequester");

        boolean found = isSupported(channelName);
        channelFindRequester.channelFindResult(
                okStatus,
                channelFind,
                found);

        return channelFind;
    }

    public ChannelFind channelList(ChannelListRequester channelListRequester) {

        if (channelListRequester == null)
            throw new IllegalArgumentException("null requester");

        channelListRequester.channelListResult(okStatus, channelFind, HOSTED_CHANNELS_SET, true);
        return channelFind;
    }

    private static final Status channelNotFoundStatus =
            statusCreate.createStatus(StatusType.ERROR, "channel not found", null);

    public Channel createChannel(String channelName,
                                 ChannelRequester channelRequester, short priority) {

        if (channelName == null)
            throw new IllegalArgumentException("channelName");

        if (channelRequester == null)
            throw new IllegalArgumentException("channelRequester");

        if (priority < ChannelProvider.PRIORITY_MIN ||
                priority > ChannelProvider.PRIORITY_MAX)
            throw new IllegalArgumentException("priority out of range");

        Channel channel = isSupported(channelName) ?
                new TestChannelImpl(channelName, channelRequester, getTopStructure(channelName)) :
                null;

        Status status = (channel == null) ? channelNotFoundStatus : okStatus;
        channelRequester.channelCreated(status, channel);

        return channel;
    }

    public Channel createChannel(String channelName,
                                 ChannelRequester channelRequester, short priority,
                                 String address) {
        throw new UnsupportedOperationException();
    }

    public void destroy() {
    }

}
