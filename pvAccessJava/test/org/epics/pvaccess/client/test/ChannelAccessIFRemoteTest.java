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

import org.epics.pvaccess.client.ChannelProvider;
import org.epics.pvaccess.client.impl.remote.ClientContextImpl;
import org.epics.pvaccess.server.impl.remote.ServerContextImpl;
import org.epics.pvaccess.server.impl.remote.plugins.DefaultBeaconServerDataProvider;
import org.epics.pvaccess.server.test.TestChannelProviderImpl;

/**
 * Channel Access remote IF test.
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public class ChannelAccessIFRemoteTest extends ChannelAccessIFTest {

	private static ServerContextImpl serverContext;

	public synchronized void initializeServerContext()
	{
		// already initialized
		if (serverContext != null)
			return;

		// Create a context with default configuration values.
		/*final ServerContextImpl*/ serverContext = new ServerContextImpl();
		serverContext.setBeaconServerStatusProvider(new DefaultBeaconServerDataProvider(serverContext));

		try {
			serverContext.initialize(new TestChannelProviderImpl());
		} catch (Throwable th) {
			th.printStackTrace();
		}

		// Display basic information about the context.
        System.out.println(serverContext.getVersion().getVersionString());
        serverContext.printInfo(); System.out.println();

        new Thread(new Runnable() {
        	public void run() {
		        try {
	                System.out.println("Running server...");
	                serverContext.run(0);
	                System.out.println("Done.");
				} catch (Throwable th) {
	                System.out.println("Failure:");
					th.printStackTrace();
				}
			}
		}, "pvAccess server").start();
	}

	public synchronized void destroyServerContext()
	{
		// not yet initialized
		if (serverContext == null)
			return;

		serverContext.dispose();

		serverContext = null;
	}

	/**
     * PVA context.
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
	 * @see org.epics.pvaccess.client.test.ChannelAccessIFTest#getChannelProvider()
	 */
	@Override
	public ChannelProvider getChannelProvider() {
		return context.getProvider();
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
		return false;
	}

	// *************************************************************************** //

	// addition tests for the remote part

	// TODO does not work now, since beacons are triggering alive timestamp
	/*
	// this can take a while, e.g. 1min
	public void testNoTraffic() throws Throwable {
		// echo request is issued

		Channel ch = syncCreateChannel("valueOnly");
		assertEquals(ConnectionState.CONNECTED, ch.getConnectionState());

		Thread.sleep((long)((context.getConnectionTimeout()+1.0)*1000));

		// still connected
		assertEquals(ConnectionState.CONNECTED, ch.getConnectionState());

		Thread.sleep((long)((context.getConnectionTimeout()+1.0)*1000));

		// still connected
		assertEquals(ConnectionState.CONNECTED, ch.getConnectionState());

		ch.destroy();
	}
	*/

	protected void internalFinalize() throws Throwable
	{
		destroyServerContext();
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#runTest()
	 */
	@Override
	protected void runTest() throws Throwable {
		initializeServerContext();
		super.runTest();
	}
}
