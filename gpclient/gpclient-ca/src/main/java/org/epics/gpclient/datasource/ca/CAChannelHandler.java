package org.epics.gpclient.datasource.ca;

import static org.epics.gpclient.datasource.ca.CADataSource.log;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.regex.Pattern;

import org.epics.gpclient.datasource.MultiplexedChannelHandler;

import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.Monitor;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBRType;
import gov.aps.jca.dbr.DBR_CTRL_Double;
import gov.aps.jca.dbr.DBR_LABELS_Enum;
import gov.aps.jca.dbr.DBR_String;
import gov.aps.jca.dbr.DBR_TIME_Byte;
import gov.aps.jca.dbr.DBR_TIME_Double;
import gov.aps.jca.dbr.DBR_TIME_Enum;
import gov.aps.jca.dbr.DBR_TIME_Float;
import gov.aps.jca.dbr.DBR_TIME_Int;
import gov.aps.jca.dbr.DBR_TIME_Short;
import gov.aps.jca.dbr.DBR_TIME_String;
import gov.aps.jca.event.AccessRightsEvent;
import gov.aps.jca.event.AccessRightsListener;
import gov.aps.jca.event.ConnectionEvent;
import gov.aps.jca.event.ConnectionListener;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;

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
                channel = caDataSource.getContext().createChannel(getChannelName(), connectionListener,
                        Channel.PRIORITY_MIN);
            } else {
                channel = caDataSource.getContext().createChannel(getChannelName(), connectionListener,
                        (short) (Channel.PRIORITY_MIN + 1));
            }
        } catch (CAException ex) {
            throw new RuntimeException("JCA Connection failed", ex);
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
            throw new RuntimeException("JCA Disconnect fail", ex);
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

    private final ConnectionListener connectionListener = new ConnectionListener() {

        @Override
        public void connectionChanged(ConnectionEvent ev) {
            synchronized (CAChannelHandler.this) {
                try {
                    if (log.isLoggable(Level.FINEST)) {
                        log.log(Level.FINEST, "JCA connectionChanged for channel {0} event {1}",
                                new Object[] { getChannelName(), ev });
                    }

                    // Take the channel from the event so that there is no
                    // synchronization problem
                    Channel channel = (Channel) ev.getSource();

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

                        @Override
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

        @Override
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

        @Override
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

}
