/*
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvaccess.client;


/**
 * Interface implemented by code that can provide access to the record
 * to which a channel connects.
 * @author mrk
 *
 */
public interface ChannelProvider {

	/** Minimal priority. */
	static final public short PRIORITY_MIN = 0;
	/** Maximal priority. */
	static final public short PRIORITY_MAX = 99;
	/** Default priority. */
	static final public short PRIORITY_DEFAULT = PRIORITY_MIN;
	/** DB links priority. */
	static final public short PRIORITY_LINKS_DB = PRIORITY_MAX;
	/** Archive priority. */
	static final public short PRIORITY_ARCHIVE = (PRIORITY_MAX + PRIORITY_MIN) / 2;
	/** OPI priority. */
	static final public short PRIORITY_OPI = PRIORITY_MIN;

	/**
     * Terminate.
     */
    void destroy();
    /**
     * Get the provider name.
     * @return The name.
     */
    String getProviderName();
    /**
     * Find a channel.
     * @param channelName The channel name.
     * @param channelFindRequester The requester.
     * @return An interface for the find.
     */
    ChannelFind channelFind(String channelName,ChannelFindRequester channelFindRequester);
    /**
     * Find a channel.
     * @param channelListRequester The requester.
     * @return An interface for the find.
     */
    ChannelFind channelList(ChannelListRequester channelListRequester);
    /**
     * Create a channel.
     * @param channelName The name of the channel.
     * @param channelRequester The requester.
     * @param priority channel priority, must be <code>PRIORITY_MIN</code> &le; priority &le; <code>PRIORITY_MAX</code>.
     * @return <code>Channel</code> instance. If channel does not exist <code>null</code> is returned and <code>channelRequester</code> notified.
     */
    Channel createChannel(String channelName,ChannelRequester channelRequester,short priority);
    /**
     * Create a channel.
     * @param channelName The name of the channel.
     * @param channelRequester The requester.
     * @param priority channel priority, must be <code>PRIORITY_MIN</code> &le; priority &le; <code>PRIORITY_MAX</code>.
     * @param address address (or list of addresses) where to look for a channel. Implementation independent string.
     * @return <code>Channel</code> instance. If channel does not exist <code>null</code> is returned and <code>channelRequester</code> notified.
     */
    Channel createChannel(String channelName,ChannelRequester channelRequester,short priority,String address);
}
