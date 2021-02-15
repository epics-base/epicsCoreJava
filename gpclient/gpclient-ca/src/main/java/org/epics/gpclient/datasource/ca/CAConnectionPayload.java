/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient.datasource.ca;

import gov.aps.jca.Channel;
import gov.aps.jca.dbr.DBRType;
import org.joda.time.Instant;

/**
 * Represents the connection payload, which consists of the actual JCA
 * Channel and the CADataSource (which can be used to extract
 * configuration parameters).
 *
 * @author carcassi
 */
public class CAConnectionPayload {
    private final CADataSource caDataSource;
    private final Channel channel;
    private final boolean connected;
    private final boolean longString;
    private final DBRType fieldType;
    private final Instant eventTime = Instant.now();

    public CAConnectionPayload(CAChannelHandler channleHandler, Channel channel, CAConnectionPayload previousPayload) {
        this.caDataSource = channleHandler.getCADataSource();
        this.channel = channel;
        this.connected = channel != null && channel.getConnectionState() == Channel.ConnectionState.CONNECTED;
        this.longString = channleHandler.isLongString();
        this.fieldType = channel.getFieldType();
    }

    /**
     * The CADataSource that is using the channel.
     *
     * @return the CA data source
     */
    public CADataSource getCADataSource() {
        return caDataSource;
    }

    /**
     * The JCA channel.
     *
     * @return JCA channel
     */
    public Channel getChannel() {
        return channel;
    }

    public DBRType getFieldType() {
        return fieldType;
    }

    /**
     * True if the channel is not null and the connection state is connected.
     *
     * @return ture if channel exists and is connected
     */
    public boolean isChannelConnected() {
        return connected;
    }

    /**
     * True if the channel is not null, connected, and can be written to.
     *
     * @return true if the channel is ready for write
     */
    public boolean isWriteConnected() {
        return isChannelConnected() && channel.getWriteAccess();
    }

    /**
     * Whether the message payload should be handled as a long string.
     *
     * @return true if long string support should be used
     */
    public boolean isLongString() {
        return longString;
    }

    /**
     * Returns the local time of the connection event.
     *
     * @return client connection/disconnection time
     */
    public Instant getEventTime() {
        return eventTime;
    }

    @Override
    public String toString() {
        return "CAConnection [connected: " +isChannelConnected() + " writeConnected: " + isWriteConnected() + " channel: " + channel + "]";
    }
}
