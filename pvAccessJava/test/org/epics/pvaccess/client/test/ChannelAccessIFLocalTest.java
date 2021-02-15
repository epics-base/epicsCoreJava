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
package org.epics.pvaccess.client.test;

import org.epics.pvaccess.server.test.TestChannelProviderImpl;
import org.epics.pvaccess.client.ChannelProviderRegistryFactory;
import org.epics.pvaccess.client.ChannelProvider;
import org.epics.pvaccess.client.ChannelProviderFactory;

/**
 * Channel Access local IF test.
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public class ChannelAccessIFLocalTest extends ChannelAccessIFTest {

	static {
		ChannelProviderFactory channelProviderImplFactory = new ChannelProviderFactory() {

			private TestChannelProviderImpl provider;

			public synchronized ChannelProvider sharedInstance() {
				if (provider == null)
					provider = new TestChannelProviderImpl();
				return provider;
			}

			public ChannelProvider newInstance() {
				throw new RuntimeException("not supported");
			}

			public String getFactoryName() {
				return TestChannelProviderImpl.PROVIDER_NAME;
			}
		};

		ChannelProviderRegistryFactory.registerChannelProviderFactory(channelProviderImplFactory);
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.client.test.ChannelAccessIFTest#getChannelProvider()
	 */
	@Override
	public ChannelProvider getChannelProvider() {
		return ChannelProviderRegistryFactory.getChannelProviderRegistry()
					.getProvider(TestChannelProviderImpl.PROVIDER_NAME);
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.client.test.ChannelAccessIFTest#getTimeoutMs()
	 */
	@Override
	public long getTimeoutMs() {
		return 3000;
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.client.test.ChannelAccessIFTest#isLocal()
	 */
	@Override
	public boolean isLocal() {
		return true;
	}

}
