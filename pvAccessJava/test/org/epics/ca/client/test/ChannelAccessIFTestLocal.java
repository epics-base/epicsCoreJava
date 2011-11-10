/*
 * Copyright (c) 2009 by Cosylab
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
package org.epics.ca.client.test;

import org.epics.ca.client.ChannelAccessFactory;
import org.epics.ca.client.ChannelProvider;
import org.epics.ca.server.test.TestChannelProviderImpl;

/**
 * Channel Access local IF test.
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public class ChannelAccessIFTestLocal extends ChannelAccessIFTest {
	
	static {
		ChannelProvider channelProviderImpl = new TestChannelProviderImpl();
		ChannelAccessFactory.registerChannelProvider(channelProviderImpl);
	}

	/* (non-Javadoc)
	 * @see org.epics.ca.client.test.ChannelAccessIFTest#getChannelProvider()
	 */
	@Override
	public ChannelProvider getChannelProvider() {
		return ChannelAccessFactory.getChannelAccess().getProvider(TestChannelProviderImpl.PROVIDER_NAME);
	}

	/* (non-Javadoc)
	 * @see org.epics.ca.client.test.ChannelAccessIFTest#getTimeoutMs()
	 */
	@Override
	public long getTimeoutMs() {
		return 3000;
	}
	
	/* (non-Javadoc)
	 * @see org.epics.ca.client.test.ChannelAccessIFTest#isLocal()
	 */
	@Override
	public boolean isLocal() {
		return true;
	}
	
}
