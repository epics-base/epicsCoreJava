/*
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvaccess.client;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author mrk
 *
 */
public class ChannelProviderRegistryFactory {
    private static final Map<String,ChannelProviderFactory> channelProviderMap = new TreeMap<String,ChannelProviderFactory>();
    private static final ChannelProviderRegistryImpl channelProviderRegistry = new ChannelProviderRegistryImpl();

    /**
     * Get the ChannelProviderRegistry interface.
     * @return The interface.
     */
    public static ChannelProviderRegistry getChannelProviderRegistry() {
        return channelProviderRegistry;
    }

    public static void registerChannelProviderFactory(ChannelProviderFactory channelProviderFactory) {
        synchronized(channelProviderMap) {
            channelProviderMap.put(channelProviderFactory.getFactoryName(), channelProviderFactory);
        }
    }

    public static void unregisterChannelProviderFactory(ChannelProviderFactory channelProviderFactory) {
        synchronized(channelProviderMap) {
        	ChannelProviderFactory registered = channelProviderMap.get(channelProviderFactory.getFactoryName());
        	if (registered == channelProviderFactory)
        		channelProviderMap.remove(channelProviderFactory.getFactoryName());
        }
    }

    private static class ChannelProviderRegistryImpl implements ChannelProviderRegistry{

        /* (non-Javadoc)
         * @see org.epics.pvaccess.client.ChannelAccess#getProvider(java.lang.String)
         */
        public ChannelProvider getProvider(String providerName) {
            synchronized(channelProviderMap) {
            	ChannelProviderFactory cpf = channelProviderMap.get(providerName);
            	if (cpf != null)
            		return cpf.sharedInstance();
            	else
            		return null;
            }
        }
        /* (non-Javadoc)
		 * @see org.epics.pvaccess.client.ChannelAccess#createProvider(java.lang.String)
		 */
		public ChannelProvider createProvider(String providerName) {
            synchronized(channelProviderMap) {
            	ChannelProviderFactory cpf = channelProviderMap.get(providerName);
            	if (cpf != null)
            		return cpf.newInstance();
            	else
            		return null;
            }
		}

		/* (non-Javadoc)
         * @see org.epics.pvaccess.client.ChannelAccess#getProviderNames()
         */
        public String[] getProviderNames() {
            synchronized(channelProviderMap) {
                String[] names = new String[channelProviderMap.size()];
                channelProviderMap.keySet().toArray(names);
                return names;
            }
        }
    }
}
