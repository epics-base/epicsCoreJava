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

package org.epics.ca.server.test;

import org.epics.ca.client.Channel;
import org.epics.ca.client.ChannelFind;
import org.epics.ca.client.ChannelFindRequester;
import org.epics.ca.client.ChannelProvider;
import org.epics.ca.client.ChannelRequester;
import org.epics.ca.client.Query;
import org.epics.ca.client.QueryRequester;
import org.epics.pvData.factory.StatusFactory;
import org.epics.pvData.pv.PVField;

/**
 * Implementation of a channel provider for tests.
 * @author msekoranja
 */
public class TestChannelProviderImpl implements ChannelProvider
{
	public static final String PROVIDER_NAME = "test";

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getProviderName() {
		return PROVIDER_NAME;
	}

	private ChannelFind channelFind = new ChannelFind() {
		
		@Override
		public ChannelProvider getChannelProvider() {
			return getChannelProvider();
		}
		
		@Override
		public void cancelChannelFind() {
			// noop, sync call
		}
	};
	@Override
	public ChannelFind channelFind(String channelName,
			ChannelFindRequester channelFindRequester) {
		if (channelName.equals("counter"))
				channelFindRequester.channelFindResult(
						StatusFactory.getStatusCreate().getStatusOK(),
						channelFind,
						true);
		return channelFind;
	}

	@Override
	public Query query(PVField query, QueryRequester queryRequester) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Channel createChannel(String channelName,
			ChannelRequester channelRequester, short priority) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Channel createChannel(String channelName,
			ChannelRequester channelRequester, short priority,
			String address) {
		throw new UnsupportedOperationException();
	}
	
}