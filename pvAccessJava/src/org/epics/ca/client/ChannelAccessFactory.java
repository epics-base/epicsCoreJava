/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.ca.client;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author mrk
 *
 */
public class ChannelAccessFactory {
    private static final Map<String,ChannelProvider> channelProviderMap = new TreeMap<String,ChannelProvider>();
    private static ChannelAccessImpl channelAccess = new ChannelAccessImpl();
    
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
    
    private static class ChannelAccessImpl implements ChannelAccess{
        ChannelProvider[] channelProviders = null;
        Set<String> keySet = null;

        /* (non-Javadoc)
         * @see org.epics.ca.client.ChannelAccess#getProvider(java.lang.String)
         */
        @Override
        public ChannelProvider getProvider(String providerName) {
            synchronized(channelProviderMap) {
                init();
                for(int i=0; i<channelProviders.length; i++) {
                    ChannelProvider channelProvider =  channelProviders[i];
                    if(channelProvider.getProviderName().equals(providerName)) return channelProvider;
                }
            }
            return null;
        }
        /* (non-Javadoc)
         * @see org.epics.ca.client.ChannelAccess#getProviderNames()
         */
        @Override
        public String[] getProviderNames() {
            synchronized(channelProviderMap) {
                init();
                String[] names = new String[keySet.size()];
                for(int i=0; i<channelProviders.length; i++) {
                    ChannelProvider channelProvider =  channelProviders[i];
                    names[i] = channelProvider.getProviderName();
                }
                return names;
            }
        } 
        private void init() {
            if(channelProviders==null || channelProviders.length!=channelProviderMap.size()) {
                channelProviders = new ChannelProvider[channelProviderMap.size()];
                keySet = channelProviderMap.keySet();
                int index = 0;
                for(String key : keySet) {
                    channelProviders[index++] = channelProviderMap.get(key);
                }
            }
        }
    }
}
