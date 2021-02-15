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

package org.epics.pvaccess.client.test;

import junit.framework.TestCase;

import org.epics.pvaccess.PVAConstants;
import org.epics.pvaccess.client.Channel;
import org.epics.pvaccess.client.ChannelRequester;
import org.epics.pvaccess.client.Channel.ConnectionState;
import org.epics.pvaccess.client.impl.remote.ClientContextImpl;
import org.epics.pvdata.pv.MessageType;
import org.epics.pvdata.pv.Status;

/**
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public class ConnectedDisconnectIT extends TestCase {

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
        // Destroy the context, check if never initialized.
        if (context != null)
            context.destroy();
	}


    private static class ConnectionListener implements ChannelRequester
    {
    	private Boolean notification = null;

 		/* (non-Javadoc)
		 * @see org.epics.pvaccess.client.ChannelRequester#channelCreated(org.epics.pvdata.pv.Status, org.epics.pvaccess.client.Channel)
		 */
		public void channelCreated(Status status,
				org.epics.pvaccess.client.Channel channel) {
			if (!status.isOK())
				System.err.println(status);
		}

		/* (non-Javadoc)
		 * @see org.epics.pvaccess.client.ChannelRequester#channelStateChange(org.epics.pvaccess.client.Channel, org.epics.pvaccess.client.Channel.ConnectionState)
		 */
		public void channelStateChange(
				org.epics.pvaccess.client.Channel c,
				ConnectionState connectionStatus) {
 			synchronized (this) {
				notification = connectionStatus == ConnectionState.CONNECTED;
				this.notify();
			}
		}

		/* (non-Javadoc)
		 * @see org.epics.pvdata.pv.Requester#getRequesterName()
		 */
		public String getRequesterName() {
			// TODO Auto-generated method stub
			return null;
		}

		/* (non-Javadoc)
		 * @see org.epics.pvdata.pv.Requester#message(java.lang.String, org.epics.pvdata.pv.MessageType)
		 */
		public void message(String message, MessageType messageType) {
			// TODO Auto-generated method stub

		}

		public void waitAndCheck() {
			synchronized (this) {
				if (notification == null)
				{
					try {
						//final long t1 = System.currentTimeMillis();
						this.wait(TIMEOUT_MS);
						//final long t2 = System.currentTimeMillis();
					} catch (InterruptedException e) {
						e.printStackTrace();
						// noop
					}
				}

				assertNotNull("channel connect timeout", notification);
				assertTrue("channel not connected", notification.booleanValue());
			}
		}
    };

    final static int TIMEOUT_MS = 3000;

    public void testConnectDisconnect() throws Throwable
    {
    	final long start = System.currentTimeMillis();
    	final int COUNT = 1000000;
    	for (int i = 1; i <= COUNT; i++)
    	{
    		//System.out.println("testing #" + i);
    		ConnectionListener cl = new ConnectionListener();
    	    Channel ch = context.getProvider().createChannel("valueOnly", cl, PVAConstants.PVA_DEFAULT_PRIORITY);
    		cl.waitAndCheck();
    		if ((i % 100)==0) {
    			System.out.println("done #" + i);
    			final long end = System.currentTimeMillis();
    			System.out.println("avg:" + (end-start)/(double)i);
    		}
    		ch.destroy();
    	}
    }
}
