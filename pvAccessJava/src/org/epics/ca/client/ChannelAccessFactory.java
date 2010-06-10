/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.ca.client;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author mrk
 *
 */
public class ChannelAccessFactory {
    private static final Map<String,ChannelProvider> channelProviderMap = new TreeMap<String,ChannelProvider>();
    private static final ChannelAccessImpl channelAccess = new ChannelAccessImpl();
    
    /**
     * Get the ChannelAccess interface.
     * @return The interface.
     */
    public static ChannelAccess getChannelAccess() {
        return channelAccess;
    }
    
    public static void registerChannelProvider(ChannelProvider channelProvider) {
        synchronized(channelProviderMap) {
            channelProviderMap.put(channelProvider.getProviderName(), channelProvider);
        }
    }
    
    public static void unregisterChannelProvider(ChannelProvider channelProvider) {
        synchronized(channelProviderMap) {
        	ChannelProvider registered = channelProviderMap.get(channelProvider.getProviderName());
        	if (registered == channelProvider)
        		channelProviderMap.remove(channelProvider.getProviderName());
        }
    }

    private static class ChannelAccessImpl implements ChannelAccess{

        /* (non-Javadoc)
         * @see org.epics.ca.client.ChannelAccess#getProvider(java.lang.String)
         */
        @Override
        public ChannelProvider getProvider(String providerName) {
            synchronized(channelProviderMap) {
            	return channelProviderMap.get(providerName);
            }
        }
        /* (non-Javadoc)
         * @see org.epics.ca.client.ChannelAccess#getProviderNames()
         */
        @Override
        public String[] getProviderNames() {
            synchronized(channelProviderMap) {
                String[] names = new String[channelProviderMap.size()];
                channelProviderMap.keySet().toArray(names);
                return names;
            }
        } 
    }
}
