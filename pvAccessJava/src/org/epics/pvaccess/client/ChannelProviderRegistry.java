/*
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvaccess.client;

/**
 * Interface for locating channel providers.
 * @author mrk
 *
 */
public interface ChannelProviderRegistry {
    /**
     * Get a shared instance of the provider with the specified name.
     * @param providerName The name of the provider.
     * @return The interface for the provider or null if the provider is not known.
     */
    ChannelProvider getProvider(String providerName);
    /**
     * Creates a new instance of the provider with the specified name.
     * @param providerName The name of the provider.
     * @return The interface for the provider or null if the provider is not known.
     */
    ChannelProvider createProvider(String providerName);
    /**
     * Get a array of the names of all the known providers.
     * @return The names.
     */
    String[] getProviderNames();
}
