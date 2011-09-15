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

package org.epics.ca.client.test;

import junit.framework.TestCase;

import org.epics.ca.CAConstants;
import org.epics.ca.client.Channel;
import org.epics.ca.client.ChannelRequester;
import org.epics.ca.client.ClientContext;
import org.epics.ca.client.Channel.ConnectionState;
import org.epics.ca.client.impl.remote.ClientContextImpl;
import org.epics.pvData.pv.MessageType;
import org.epics.pvData.pv.Status;

/**
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public class ConnectedDisconnectIT extends TestCase {

	/**
     * CA context.
     */
    protected ClientContext context = null;
    
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
		 * @see org.epics.ca.client.ChannelRequester#channelCreated(org.epics.pvData.pv.Status, org.epics.ca.client.Channel)
		 */
		@Override
		public void channelCreated(Status status,
				org.epics.ca.client.Channel channel) {
			if (!status.isOK())
				System.err.println(status);
		}

		/* (non-Javadoc)
		 * @see org.epics.ca.client.ChannelRequester#channelStateChange(org.epics.ca.client.Channel, org.epics.ca.client.Channel.ConnectionState)
		 */
		@Override
		public void channelStateChange(
				org.epics.ca.client.Channel c,
				ConnectionState connectionStatus) {
 			synchronized (this) {
				notification = new Boolean(connectionStatus == ConnectionState.CONNECTED);
				this.notify();
			}
		}

		/* (non-Javadoc)
		 * @see org.epics.pvData.pv.Requester#getRequesterName()
		 */
		@Override
		public String getRequesterName() {
			// TODO Auto-generated method stub
			return null;
		}

		/* (non-Javadoc)
		 * @see org.epics.pvData.pv.Requester#message(java.lang.String, org.epics.pvData.pv.MessageType)
		 */
		@Override
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
    	    Channel ch = context.getProvider().createChannel("valueOnly", cl, CAConstants.CA_DEFAULT_PRIORITY);
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
