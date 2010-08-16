/*
 * Copyright (c) 2007 by Cosylab
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

package org.epics.ca;

import org.epics.ca.client.ChannelAccessFactory;
import org.epics.ca.server.impl.remote.ServerContextImpl;
import org.epics.ca.server.impl.remote.plugins.DefaultBeaconServerDataProvider;
import org.epics.pvData.misc.RunnableReady;
import org.epics.pvData.misc.ThreadCreate;
import org.epics.pvData.misc.ThreadCreateFactory;
import org.epics.pvData.misc.ThreadReady;

public class ServerFactory {
    /**
     * This starts the Channel Access Server.
     */
    public static void start() {
        new ThreadInstance();
    }
    
    private static final ThreadCreate threadCreate = ThreadCreateFactory.getThreadCreate();
    
    private static class ThreadInstance implements RunnableReady {

        private ThreadInstance() {
            threadCreate.create("pvAccessServer", 3, this);
        }
        
    	/**
         * JCA server context.
         */
        private ServerContextImpl context = null;
        
        /**
         * Initialize JCA context.
         * @throws CAException	throws on any failure.
         */
        private void initialize() throws CAException {
            
    		// Create a context with default configuration values.
    		context = new ServerContextImpl();
    		context.setBeaconServerStatusProvider(new DefaultBeaconServerDataProvider(context));
    		
    		context.initialize(ChannelAccessFactory.getChannelAccess());

    		// Display basic information about the context.
            System.out.println(context.getVersion().getVersionString());
            context.printInfo(); System.out.println();
        }

        /**
         * Destroy JCA server  context.
         */
        private void destroy() {
            
            try {

                // Destroy the context, check if never initialized.
                if (context != null)
                    context.destroy();
                
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }               
        /* (non-Javadoc)
         * @see org.epics.ioc.util.RunnableReady#run(org.epics.ioc.util.ThreadReady)
         */
        public void run(final ThreadReady threadReady) {
                Thread runThread = new Thread(new Runnable() {
					@Override
					public void run() {
			            try {
			                // initialize context
			                initialize();
			                System.out.println("Running server...");
			                // run server 
							context.run(0);
			                System.out.println("Done.");
			            } catch (Throwable th) {
			                th.printStackTrace();
			            }
			            finally {
			                // always finalize
			                destroy();
			            }
					}
				}, "CA-server");
                runThread.setDaemon(false);
                runThread.start();
                threadReady.ready();
        }
    }
}
