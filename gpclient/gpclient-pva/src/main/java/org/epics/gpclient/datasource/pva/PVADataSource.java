/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient.datasource.pva;

import org.epics.gpclient.datasource.ChannelHandler;
import org.epics.gpclient.datasource.DataSource;
import org.epics.pvaccess.ClientFactory;
import org.epics.pvaccess.client.ChannelProvider;
import org.epics.pvaccess.client.ChannelProviderRegistry;
import org.epics.pvaccess.client.ChannelProviderRegistryFactory;

import java.util.logging.Logger;

/**
 *
 * @author msekoranja
 */
public class PVADataSource extends DataSource {

    private static final Logger log = Logger.getLogger(PVADataSource.class.getName());
    private final short defaultPriority;
    private final ChannelProvider pvaChannelProvider;

    private final PVATypeSupport pvaTypeSupport = new PVATypeSupport(new PVAVTypeAdapterSet());

    public PVADataSource() {
        this(ChannelProvider.PRIORITY_DEFAULT);
    }

    public PVADataSource(short defaultPriority) {
        this.defaultPriority = defaultPriority;

        try {
            // This takes more than a second: should be moved to a background thread
            ClientFactory.start();
            final ChannelProviderRegistry registry = ChannelProviderRegistryFactory.getChannelProviderRegistry();
            this.pvaChannelProvider = registry.createProvider("pva");
            if (this.pvaChannelProvider == null) {
                throw new RuntimeException("pvAccess ChannelProvider not installed");
            }

        } catch (Throwable th) {
            throw new RuntimeException("Failed to intialize pvAccess context.", th);
        }
    }

    public PVADataSource(ChannelProvider channelProvider, short defaultPriority) {
        this.pvaChannelProvider = channelProvider;
        this.defaultPriority = defaultPriority;
    }

    public short getDefaultPriority() {
        return defaultPriority;
    }

    @Override
    public void close() {
        if (this.pvaChannelProvider != null) {
            pvaChannelProvider.destroy();
        }
    }

    @Override
    protected ChannelHandler createChannel(String channelName) {
        return PVAChannelHandler.create(channelName, pvaChannelProvider, defaultPriority, pvaTypeSupport);
    }

}
