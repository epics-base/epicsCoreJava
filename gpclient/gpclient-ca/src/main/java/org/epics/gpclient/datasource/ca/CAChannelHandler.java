/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient.datasource.ca;

import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.Monitor;
import gov.aps.jca.dbr.*;
import gov.aps.jca.event.*;
import org.epics.gpclient.ReadCollector;
import org.epics.gpclient.WriteCollector.WriteRequest;
import org.epics.gpclient.datasource.MultiplexedChannelHandler;
import org.epics.gpclient.datasource.ca.types.CATypeAdapter;
import org.epics.util.array.ListNumber;
import org.epics.util.array.UnsafeUnwrapper;
import org.epics.vtype.*;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.regex.Pattern;

import static org.epics.gpclient.datasource.ca.CADataSource.log;
import static org.epics.util.array.UnsafeUnwrapper.wrappedArray;
import static org.epics.util.array.UnsafeUnwrapper.wrappedDoubleArray;

public class CAChannelHandler extends MultiplexedChannelHandler<CAConnectionPayload, CAMessagePayload> {

    private static final int LARGE_ARRAY = 10000000;

    private final CADataSource caDataSource;

    private volatile Channel channel;
    private volatile boolean largeArray = false;
    private volatile boolean sentReadOnlyException = false;

    private Monitor valueMonitor;
    private Monitor metadataMonitor;

    public CAChannelHandler(String channelName, CADataSource caDataSource) {
        super(channelName);
        this.caDataSource = caDataSource;
    }

    /**
     * The datasource this channel refers to.
     *
     * @return a ca data source
     */
    public CADataSource getCADataSource() {
        return caDataSource;
    }

    @Override
    protected void connect() {
        try {
            // Give the listener right away so that no event gets lost
            // If it's a large array, connect using lower priority
            if (largeArray) {
                channel = caDataSource.getContext().createChannel(getChannelName(), connectionListener, Channel.PRIORITY_MIN);
            } else {
                channel = caDataSource.getContext().createChannel(getChannelName(), connectionListener, (short) (Channel.PRIORITY_MIN + 1));
            }
        } catch (CAException ex) {
            reportExceptionToAllReadersAndWriters(ex);
            log.log(Level.WARNING, "JCA Connection failed", ex);
        }
    }

    @Override
    protected void disconnect() {
        try {
            // Close the channel
            // Need to guard because the channel may be closed if the
            // context was already destroyed
            if (channel.getConnectionState() != Channel.ConnectionState.CLOSED) {
                channel.removeConnectionListener(connectionListener);
                channel.destroy();
            }
        } catch (CAException ex) {
            reportExceptionToAllReadersAndWriters(ex);
            log.log(Level.WARNING, "JCA Disconnect fail", ex);
        } finally {
            channel = null;
            processConnection(null);
        }
    }

    @Override
    protected boolean isConnected(CAConnectionPayload connPayload) {
        return connPayload != null && connPayload.isChannelConnected();
    }

    @Override
    protected boolean isWriteConnected(CAConnectionPayload connPayload) {
        return connPayload != null && connPayload.isWriteConnected();
    }

    @Override
    protected CATypeAdapter findTypeAdapter(ReadCollector<?, ?> cache, CAConnectionPayload connection) {
        return caDataSource.getCaTypeSupport().find(cache, connection);
    }

    private final ConnectionListener connectionListener = new ConnectionListener() {

        public void connectionChanged(ConnectionEvent ev) {
            synchronized (CAChannelHandler.this) {
                try {
                    if (log.isLoggable(Level.FINEST)) {
                        log.log(Level.FINEST, "JCA connectionChanged for channel {0} event {1}",
                                new Object[] { getChannelName(), ev });
                    }

                    // Take the channel from the event so that there is no
                    // synchronization problem
                    final Channel channel = (Channel) ev.getSource();

                    // Check whether the channel is large and was opened
                    // as large. Reconnect if does not match
                    if (ev.isConnected() && channel.getElementCount() >= LARGE_ARRAY && !largeArray) {
                        disconnect();
                        largeArray = true;
                        connect();
                        return;
                    }

                    processConnection(new CAConnectionPayload(CAChannelHandler.this, channel, getConnectionPayload()));
                    if (ev.isConnected()) {
                        // If connected, no write access and exception was not sent, notify writers
                        if (!channel.getWriteAccess() && !sentReadOnlyException) {
                            reportExceptionToAllWriters(createReadOnlyException());
                            sentReadOnlyException = true;
                        }
                        // Setup monitors on connection
                        setup(channel);
                    } else {
                        resetMessage();
                        // Next connection, resend the read only exception if that's the case
                        sentReadOnlyException = false;
                    }

                    channel.addAccessRightsListener(new AccessRightsListener() {
                        public void accessRightsChanged(AccessRightsEvent ev) {
                            if (log.isLoggable(Level.FINEST)) {
                                log.log(Level.FINEST, "JCA accessRightsChanged for channel {0} event {1}",
                                        new Object[] { getChannelName(), ev });
                            }
                            processConnection(
                                    new CAConnectionPayload(CAChannelHandler.this, channel, getConnectionPayload()));
                            if (!sentReadOnlyException && !channel.getWriteAccess()) {
                                reportExceptionToAllWriters(createReadOnlyException());
                                sentReadOnlyException = true;
                            }
                        }

                    });
                } catch (Exception ex) {
                    reportExceptionToAllReadersAndWriters(ex);
                }
            }
        }
    };;

    private void setup(Channel channel) throws CAException {
        DBRType metaType = metadataFor(channel);

        // If metadata is needed, get it
        if (metaType != null) {
            DBR dbr = channel.get(metaType, 1);
            if (log.isLoggable(Level.FINEST)) {
                log.log(Level.FINEST, "JCA metadata getCompleted for channel {0} event {1}",
                        new Object[] { getChannelName(), dbr });
            }
            processMessage(new CAMessagePayload(dbr, null));

        }
        // At each (re)connect, we need to create a new monitor:
        // since the type could be changed, we would have a type mismatch
        // between the current type and the old type when the monitor was
        // created

        // XXX: Ideally, we would destroy the monitor on reconnect,
        // but currently this does not work with CAJ (you get an
        // IllegalStateException because the transport is not there
        // anymore). So, for now, we destroy the monitor during the
        // the connection callback.

        // XXX: Ideally, we should just close (clear) the monitor, but
        // this would cause one last event to reach the monitorListener.
        // So, we remove the monitorListener right before the clear.

        if (valueMonitor != null) {
            valueMonitor.removeMonitorListener(monitorListener);
            valueMonitor.clear();
            valueMonitor = null;
        }

        valueMonitor = channel.addMonitor(valueTypeFor(channel), countFor(channel), caDataSource.getMonitorMask(),
                monitorListener);
        // Remove current metadata monitor
        if (metadataMonitor != null) {
            metadataMonitor.removeMonitorListener(metadataListener);
            metadataMonitor.clear();
            metadataMonitor = null;
        }

        // Setup metadata monitor if required
        if (caDataSource.isDbePropertySupported() && metaType != null) {
            metadataMonitor = channel.addMonitor(metaType, 1, Monitor.PROPERTY, metadataListener);
        }

        // Flush the entire context (it's the best we can do)
        channel.getContext().flushIO();
    }

    private final MonitorListener monitorListener = new MonitorListener() {

        public void monitorChanged(MonitorEvent event) {
            synchronized (CAChannelHandler.this) {
                if (log.isLoggable(Level.FINEST)) {
                    log.log(Level.FINEST, "JCA value monitorChanged for channel {0} value {1}, event {2}",
                            new Object[] { getChannelName(), toStringDBR(event.getDBR()), event });
                }

                DBR metadata = null;
                if (getLastMessagePayload() != null) {
                    metadata = getLastMessagePayload().getMetadata();
                }
                processMessage(new CAMessagePayload(metadata, event));
            }
        }
    };

    private final MonitorListener metadataListener = new MonitorListener() {

        public void monitorChanged(MonitorEvent ev) {
            synchronized (CAChannelHandler.this) {
                if (log.isLoggable(Level.FINEST)) {
                    log.log(Level.FINEST, "JCA metadata monitorChanged for channel {0} event {1}",
                            new Object[] { getChannelName(), ev });
                }

                // In case the metadata arrives after the monitor
                MonitorEvent event = null;
                if (getLastMessagePayload() != null) {
                    event = getLastMessagePayload().getEvent();
                }
                processMessage(new CAMessagePayload(ev.getDBR(), event));
            }
        }
    };

    @Override
    protected void write(Object newValue) {
        if(newValue instanceof VNumberArray){
            newValue = ((VNumberArray) newValue).getData();
        }
        // If it's a ListNumber, extract the array
        if (newValue instanceof ListNumber) {
            ListNumber data = (ListNumber) newValue;
            UnsafeUnwrapper.Array<?> wrappedArray = wrappedArray(data);
            if (wrappedArray == null) {
                wrappedArray = wrappedDoubleArray(data);
            }
            newValue = wrappedArray.array;
        }
        try {
            if (newValue instanceof Double[]) {

                log.warning("You are writing a Double[] to channel " + getChannelName()
                        + ": use org.epics.util.array.ListDouble instead");
                final Double dbl[] = (Double[]) newValue;
                final double val[] = new double[dbl.length];
                for (int i = 0; i < val.length; ++i) {
                    val[i] = dbl[i].doubleValue();
                }
                newValue = val;
                channel.put((double[])newValue);

            }
            else if (newValue instanceof Integer[]) {
                log.warning("You are writing a Integer[] to channel " + getChannelName()
                        + ": use org.epics.util.array.ListInt instead");
                final Integer ival[] = (Integer[]) newValue;
                final int val[] = new int[ival.length];
                for (int i = 0; i < val.length; ++i) {
                    val[i] = ival[i].intValue();
                }
                newValue = val;
                channel.put((int[])newValue);
            }

            else if(newValue instanceof Double){
                channel.put(((Double) newValue).doubleValue());
            }
            else if(newValue instanceof Integer){
                channel.put(((Integer) newValue).intValue());
            }
            else if(newValue instanceof BigInteger){
                channel.put(((BigInteger) newValue).intValue());
            }
            else if(newValue instanceof Short){
                channel.put(((Short) newValue).shortValue());
            }
            else if(newValue instanceof Float){
                channel.put(((Float) newValue).floatValue());
            }
            else if(newValue instanceof Byte){
                channel.put(((Byte) newValue).byteValue());
            }
            else if (newValue instanceof String) {
                if (isLongString()) {
                    channel.put(toBytes(newValue.toString()));
                } else {
                    if (channel.getFieldType().isBYTE() && channel.getElementCount() > 1) {
                        log.warning("You are writing the String " + newValue + " to BYTE channel " + getChannelName()
                                + ": use {\"longString\":true} for support");
                        channel.put(toBytes(newValue.toString()));
                    } else {
                        channel.put(newValue.toString());
                    }
                }
            } else if (newValue instanceof byte[]) {
                channel.put((byte[]) newValue);
            } else if (newValue instanceof short[]) {
                channel.put((short[]) newValue);
            } else if (newValue instanceof int[]) {
                channel.put((int[]) newValue);
            } else if (newValue instanceof float[]) {
                channel.put((float[]) newValue);
            } else if (newValue instanceof double[]) {
                channel.put((double[]) newValue);
            } else if (newValue instanceof long[]) {
                long[] longs = (long[])newValue;
                double[] value = new double[longs.length];
                for(int i = 0; i < longs.length; i++){
                    value[i] = (double)longs[i];
                }
                channel.put(value);
            } else if (newValue instanceof VByte) {
                channel.put(((VByte) newValue).getValue());
            } else if (newValue instanceof VShort) {
                channel.put(((VShort) newValue).getValue());
            } else if (newValue instanceof VInt) {
                channel.put(((VInt)newValue).getValue());
            } else if (newValue instanceof VLong) {
                // XXX: Channel access does not support 64 bit integers
                // If fits 32 bits, use int. Use double otherwise
                long value64 = ((VLong) newValue).getValue();
                int value32 = (int) value64;
                if (value32 == value64) {
                    channel.put(value32);
                } else {
                    channel.put((double) value64);
                }
            } else if (newValue instanceof VFloat) {
                channel.put(((VFloat) newValue).getValue());
            } else if (newValue instanceof VDouble) {
                channel.put(((VDouble) newValue).getValue());
            } else if(newValue instanceof VEnum){
                channel.put(((VEnum) newValue).getValue());
            }
            else {
                // callback.channelWritten(new Exception(new RuntimeException("Unsupported type
                // for CA: " + newValue.getClass())));
                return;
            }
            caDataSource.getContext().flushIO();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void processWriteRequest(WriteRequest<?> request) {
        try {
            write(request.getValue());
            request.writeSuccessful();
        } catch (Exception ex) {
            request.writeFailed(ex);
        }
    }

    protected int countFor(Channel channel) {
        if (channel.getElementCount() == 1)
            return 1;

        if (caDataSource.isVarArraySupported())
            return 0;
        else
            return channel.getElementCount();
    }

    protected DBRType metadataFor(Channel channel) {
        DBRType type = channel.getFieldType();

        if (type.isBYTE() || type.isSHORT() || type.isINT() || type.isFLOAT() || type.isDOUBLE())
            return DBR_CTRL_Double.TYPE;

        if (type.isENUM())
            return DBR_LABELS_Enum.TYPE;

        return null;
    }

    static Pattern rtypeStringPattern = Pattern.compile(".+\\.RTYP.*");

    protected DBRType valueTypeFor(Channel channel) {
        DBRType type = channel.getFieldType();

        if (type.isBYTE()) {
            return DBR_TIME_Byte.TYPE;
        } else if (type.isSHORT()) {
            return DBR_TIME_Short.TYPE;
        } else if (type.isINT()) {
            return DBR_TIME_Int.TYPE;
        } else if (type.isFLOAT()) {
            return DBR_TIME_Float.TYPE;
        } else if (type.isDOUBLE()) {
            return DBR_TIME_Double.TYPE;
        } else if (type.isENUM()) {
            return DBR_TIME_Enum.TYPE;
        } else if (type.isSTRING()) {
            if (caDataSource.isRtypValueOnly() && rtypeStringPattern.matcher(channel.getName()).matches()) {
                return DBR_String.TYPE;
            }
            return DBR_TIME_String.TYPE;
        }

        throw new IllegalArgumentException("Unsupported type " + type);
    }

    private Exception createReadOnlyException() {
        return new RuntimeException("'" + getChannelName() + "' is read-only");
    }

    private String toStringDBR(DBR value) {
        StringBuilder builder = new StringBuilder();
        if (value == null) {
            return "null";
        }
        if (value.getValue() instanceof double[]) {
            builder.append(Arrays.toString((double[]) value.getValue()));
        } else if (value.getValue() instanceof short[]) {
            builder.append(Arrays.toString((short[]) value.getValue()));
        } else if (value.getValue() instanceof String[]) {
            builder.append(Arrays.toString((String[]) value.getValue()));
        } else {
            builder.append(value.getValue().toString());
        }
        return builder.toString();
    }

    /**
     * Converts a String into byte array.
     *
     * @param text the string to be converted
     * @return byte array, always including '\0' termination
     */
    static byte[] toBytes(final String text) {
        // TODO: it's unclear what encoding is used and how

        // Write string as byte array WITH '\0' TERMINATION!
        final byte[] bytes = new byte[text.length() + 1];
        System.arraycopy(text.getBytes(), 0, bytes, 0, text.length());
        bytes[text.length()] = '\0';
        return bytes;
    }

    /**
     * Converts a byte array into a String. It
     *
     * @param data the array to be converted
     * @return the string
     */
    static String toString(byte[] data) {
        int index = 0;
        while (index < data.length && data[index] != '\0') {
            index++;
        }

        return new String(data, 0, index);
    }

    public boolean isLongString() {
        // TODO Auto-generated method stub
        return false;
    }

}
