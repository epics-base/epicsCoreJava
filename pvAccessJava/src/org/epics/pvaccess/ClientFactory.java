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

package org.epics.pvaccess;

import org.epics.pvaccess.client.ChannelProviderRegistryFactory;
import org.epics.pvaccess.client.ChannelProvider;
import org.epics.pvaccess.client.ChannelProviderFactory;
import org.epics.pvaccess.client.impl.remote.ClientContextImpl;

/**
 * Utility class that starts/stops remote pvAccess client channel provider.
 * @author msekoranja
 */
public class ClientFactory {

	/**
	 * Name if the provider this factory registers.
	 */
	public static final String PROVIDER_NAME = ClientContextImpl.PROVIDER_NAME;

	static private ChannelProviderFactoryImpl factory = null;
    static private ClientContextImpl context = null;

    private static class ChannelProviderFactoryImpl implements ChannelProviderFactory
    {

		public String getFactoryName() {
			return PROVIDER_NAME;
		}

		public synchronized ChannelProvider sharedInstance() {
	        try
	        {
	        	if (context == null)
	        	{
		        	ClientContextImpl lcontext = new ClientContextImpl();
					lcontext.initialize();
					context = lcontext;
	        	}

				return context.getProvider();
	        } catch (Throwable e) {
	            throw new RuntimeException("Failed to initialize shared pvAccess client instance.", e);
	        }
		}

		public ChannelProvider newInstance() {
	        try
	        {
	        	ClientContextImpl lcontext = new ClientContextImpl();
				lcontext.initialize();
				return lcontext.getProvider();
	        } catch (Throwable e) {
	            throw new RuntimeException("Failed to initialize new pvAccess client instance.", e);
	        }
		}

		public synchronized boolean destroySharedInstance() {
			boolean destroyed = true;
			if (context != null)
			{
				context.dispose();
				destroyed = context.isDestroyed();
				context = null;
			}
			return destroyed;
		}
    }

    /**
     * Registers pvAccess client channel provider factory.
     */
    public static synchronized void start() {
        if (factory != null) return;
        factory = new ChannelProviderFactoryImpl();
        ChannelProviderRegistryFactory.registerChannelProviderFactory(factory);
    }

    /**
     * Unregisters pvAccess client channel provider factory and destroys shared channel provider instance (if necessary).
     */
    public static synchronized void stop() {
    	if (factory != null)
    	{
    		ChannelProviderRegistryFactory.unregisterChannelProviderFactory(factory);
    		if(factory.destroySharedInstance())
    		{
    			factory=null;
    		}
    	}
    }
}
