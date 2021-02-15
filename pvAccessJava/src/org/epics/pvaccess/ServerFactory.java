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

package org.epics.pvaccess;

import org.epics.pvaccess.client.ChannelProviderRegistryFactory;
import org.epics.pvaccess.server.impl.remote.ServerContextImpl;
import org.epics.pvaccess.server.impl.remote.plugins.DefaultBeaconServerDataProvider;
import org.epics.pvdata.misc.RunnableReady;
import org.epics.pvdata.misc.ThreadCreate;
import org.epics.pvdata.misc.ThreadCreateFactory;
import org.epics.pvdata.misc.ThreadReady;

/**
 * Utility class that starts pvAccess server.
 * @author msekoranja
 */
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
         * @throws PVAException	throws on any failure.
         */
        private void initialize() throws PVAException {

    		// Create a context with default configuration values.
    		context = new ServerContextImpl();
    		context.setBeaconServerStatusProvider(new DefaultBeaconServerDataProvider(context));

    		context.initialize(ChannelProviderRegistryFactory.getChannelProviderRegistry());

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
				}, "pvAccess-server");
                runThread.setDaemon(false);
                runThread.start();
                threadReady.ready();
        }
    }
}
