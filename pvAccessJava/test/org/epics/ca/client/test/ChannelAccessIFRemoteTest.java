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
import org.epics.ca.client.impl.remote.ClientContextImpl;
import org.epics.ca.server.impl.remote.ServerContextImpl;
import org.epics.ca.server.impl.remote.plugins.DefaultBeaconServerDataProvider;
import org.epics.ca.server.test.TestChannelProviderImpl;

/**
 * Channel Access remote IF test.
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public class ChannelAccessIFRemoteTest extends ChannelAccessIFTest {
	
	static {
		ChannelProvider channelProviderImpl = new TestChannelProviderImpl();
		ChannelAccessFactory.registerChannelProvider(channelProviderImpl);
		
		System.setProperty("EPICS4_CAS_PROVIDER_NAME", channelProviderImpl.getProviderName());
		
		// Create a context with default configuration values.
		final ServerContextImpl context = new ServerContextImpl();
		context.setBeaconServerStatusProvider(new DefaultBeaconServerDataProvider(context));
		
		try {
			context.initialize(ChannelAccessFactory.getChannelAccess());
		} catch (Throwable th) {
			th.printStackTrace();
		}

		// Display basic information about the context.
        System.out.println(context.getVersion().getVersionString());
        context.printInfo(); System.out.println();

        new Thread(new Runnable() {
			
			@Override
			public void run() {
		        try {
	                System.out.println("Running server...");
					context.run(0);
	                System.out.println("Done.");
				} catch (Throwable th) {
	                System.out.println("Failure:");
					th.printStackTrace();
				}
			}
		}, "pvAccess server").start();
	}

	/**
     * CA context.
     */
    protected ClientContextImpl context = null;
    
    /* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		// Create a context with default configuration values.
		context = new ClientContextImpl();
		context.initialize();
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
        // Destroy the context, check if never initialized.
        if (context != null) {
            context.dispose();
        }
	}

	/* (non-Javadoc)
	 * @see org.epics.ca.client.test.ChannelAccessIFTest#getChannelProvider()
	 */
	@Override
	public ChannelProvider getChannelProvider() {
		return context.getProvider();
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
		return false;
	}
	
}
