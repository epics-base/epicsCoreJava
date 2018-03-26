/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.gpclient.datasource;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A data source that can dispatch a request to multiple different
 * data sources.
 *
 * @author carcassi
 */
public class CompositeDataSource extends DataSource {
    
    private static final Logger log = Logger.getLogger(CompositeDataSource.class.getName());
    
    // XXX: quickest way to implement the idea of a session is to keep a handle
    // to a global composite datasource, so that some parts can be overridden
    // for each session (say the local variables) and other can be shared
    // (say data and network/protocol channels).
    private final CompositeDataSource globalDataSource;

    // Stores all data sources by name
    private final Map<String, DataSource> dataSources = new ConcurrentHashMap<>();
    private final Map<String, DataSourceProvider> dataSourceProviders = new ConcurrentHashMap<>();

    private volatile CompositeDataSourceConfiguration conf = new CompositeDataSourceConfiguration();

    /**
     * Creates a new CompositeDataSource.
     */
    public CompositeDataSource() {
        super(true);
        globalDataSource = null;
    }

    /**
     * Creates a session on the globalDataSource.
     * 
     * @param globalDataSource the parent data source
     */
    private CompositeDataSource(CompositeDataSource globalDataSource) {
        super(true);
        this.globalDataSource = globalDataSource;
    }

    /**
     * The configuration used for the composite data source.
     *
     * @return the configuration; can't be null
     */
    public CompositeDataSourceConfiguration getConfiguration() {
        return conf;
    }

    /**
     * Changes the composite data source configuration.
     * <p>
     * NOTE: the configuration should be changed before any channel
     * is opened. The result of later changes is not well defined.
     *
     * @param conf the new configuration; can't be null
     */
    public void setConfiguration(CompositeDataSourceConfiguration conf) {
        if (conf == null) {
            throw new NullPointerException("Configuration can't be null.");
        }
        this.conf = conf;
    }

    /**
     * Adds/replaces the data source corresponding to the given name.
     *
     * @param name the name of the data source
     * @param dataSource the data source to add/replace
     */
    public void putDataSource(final String name, final DataSource dataSource) {
        putDataSource(new DataSourceProvider() {

            @Override
            public String getName() {
                return name;
            }

            @Override
            public DataSource createInstance() {
                return dataSource;
            }
        });
    }

    /**
     * Adds/replaces the data source corresponding to the given name.
     *
     * @param dataSourceProvider the data source to add/replace
     */
    public void putDataSource(DataSourceProvider dataSourceProvider) {
        // XXX: datasources should be closed
        dataSources.remove(dataSourceProvider.getName());
        dataSourceProviders.put(dataSourceProvider.getName(), dataSourceProvider);
    }
    
    /**
     * Returns the data sources used by this composite data source.
     * <p>
     * Returns only the data sources that have been created.
     * 
     * @return the registered data sources
     */
    public Map<String, DataSource> getDataSources() {
        return Collections.unmodifiableMap(dataSources);
    }
    
    /**
     * Returns the data source providers registered to this composite data source.
     * <p>
     * Returns all registered data sources.
     * 
     * @return the registered data source providers
     */
    public Map<String, DataSourceProvider> getDataSourceProviders() {
        return Collections.unmodifiableMap(dataSourceProviders);
    }
    
    private  String nameOf(String channelName) {
        String delimiter = conf.delimiter;
        int indexDelimiter = channelName.indexOf(delimiter);
        if (indexDelimiter == -1) {
            return channelName;
        } else {
            return channelName.substring(indexDelimiter + delimiter.length());
        }
    }
    
    private String sourceOf(String channelName) {
        String delimiter = conf.delimiter;
        String defaultDataSource = conf.defaultDataSource;
        int indexDelimiter = channelName.indexOf(delimiter);
        if (indexDelimiter == -1) {
            if (defaultDataSource == null)
                throw new IllegalArgumentException("Channel " + channelName + " uses default data source but one was never set.");
            if (!dataSourceProviders.containsKey(defaultDataSource) && (globalDataSource == null || !globalDataSource.dataSourceProviders.containsKey(defaultDataSource))) {
                throw new IllegalArgumentException("Channel " + channelName + " uses default data source " + defaultDataSource + " which was not found.");
            }
            return defaultDataSource;
        } else {
            String source = channelName.substring(0, indexDelimiter);
            if (dataSourceProviders.containsKey(source) || (globalDataSource != null && globalDataSource.dataSourceProviders.containsKey(source)))
                return source;
            throw new IllegalArgumentException("Data source " + source + " for " + channelName + " was not configured.");
        }
    }
    
    @Override
    public void connectRead(final ChannelReadRecipe readRecipe) {
        try {
            String name = nameOf(readRecipe.getChannelName());
            String dataSource = sourceOf(readRecipe.getChannelName());

            if (dataSource == null)
                throw new IllegalArgumentException("Channel " + name + " uses the default data source but one was never set.");

            retrieveDataSource(dataSource).connectRead(new ChannelReadRecipe(name, readRecipe.getReadSubscription()));
        } catch (RuntimeException ex) {
            // If data source fail, report the error
            readRecipe.getReadSubscription().notifyError(ex);
        }
    }

    @Override
    public void disconnectRead(ChannelReadRecipe readRecipe) {
        try {
            String name = nameOf(readRecipe.getChannelName());
            String dataSource = sourceOf(readRecipe.getChannelName());

            if (dataSource == null)
                throw new IllegalArgumentException("Channel " + name + " uses the default data source but one was never set.");

            retrieveDataSource(dataSource).disconnectRead(new ChannelReadRecipe(name, readRecipe.getReadSubscription()));
        } catch (RuntimeException ex) {
            // If data source fail, report the error
            readRecipe.getReadSubscription().notifyError(ex);
        }
    }
    
    private DataSource retrieveDataSource(String name) {
        DataSource dataSource = dataSources.get(name);
        if (dataSource == null) {
            DataSourceProvider factory = dataSourceProviders.get(name);
            if (factory == null) {
                if (globalDataSource != null) {
                    return globalDataSource.retrieveDataSource(name);
                }
                throw new IllegalArgumentException("DataSource '" + name + conf.delimiter + "' was not configured.");
            } else {
                dataSource = factory.createInstance();
                if (dataSource == null) {
                    throw new IllegalStateException("DataSourceProvider '" + name + conf.delimiter + "' did not create a valid datasource.");
                }
                dataSources.put(name, dataSource);
                log.log(Level.CONFIG, "Created instance for data source {0} ({1})", new Object[]{name, dataSource.getClass().getSimpleName()});
            }
        }
        return dataSource;
    }

    @Override
    public void connectWrite(ChannelWriteRecipe writeRecipe) {
        try {
            String name = nameOf(writeRecipe.getChannelName());
            String dataSource = sourceOf(writeRecipe.getChannelName());

            if (dataSource == null)
                throw new IllegalArgumentException("Channel " + name + " uses the default data source but one was never set.");

            retrieveDataSource(dataSource).connectWrite(new ChannelWriteRecipe(name, writeRecipe.getWriteSubscription()));
        } catch (RuntimeException ex) {
            // If data source fail, report the error
            writeRecipe.getWriteSubscription().notifyError(ex);
        }
    }

    @Override
    public void disconnectWrite(ChannelWriteRecipe writeRecipe) {
        try {
            String name = nameOf(writeRecipe.getChannelName());
            String dataSource = sourceOf(writeRecipe.getChannelName());

            if (dataSource == null)
                throw new IllegalArgumentException("Channel " + name + " uses the default data source but one was never set.");

            retrieveDataSource(dataSource).disconnectWrite(new ChannelWriteRecipe(name, writeRecipe.getWriteSubscription()));
        } catch (RuntimeException ex) {
            // If data source fail, report the error
            writeRecipe.getWriteSubscription().notifyError(ex);
        }
    }
    

    @Override
    ChannelHandler channel(String channelName) {
        String name = nameOf(channelName);
        String dataSource = sourceOf(channelName);
        return retrieveDataSource(dataSource).channel(name);
    }
    
    @Override
    protected ChannelHandler createChannel(String channelName) {
        throw new UnsupportedOperationException("Composite data source can't create channels directly.");
    }

    /**
     * Closes all DataSources that are registered in the composite.
     */
    @Override
    public void close() {
        for (DataSource dataSource : dataSources.values()) {
            dataSource.close();
        }
    }

    @Override
    public Map<String, ChannelHandler> getChannels() {
        Map<String, ChannelHandler> channels = new HashMap<String, ChannelHandler>();
        if (globalDataSource != null) {
            channels.putAll(globalDataSource.getChannels());
        }
        for (Entry<String, DataSource> entry : dataSources.entrySet()) {
            String dataSourceName = entry.getKey();
            DataSource dataSource = entry.getValue();
            for (Entry<String, ChannelHandler> channelEntry : dataSource.getChannels().entrySet()) {
                String channelName = channelEntry.getKey();
                ChannelHandler channelHandler = channelEntry.getValue();
                channels.put(dataSourceName + conf.delimiter + channelName, channelHandler);
            }
        }
        
        return channels;
    }
    
    /**
     * Creates a new composite data source that can be overridden.
     * 
     * @return a new composite datasource
     */
    public CompositeDataSource createSessionDataSource() {
        CompositeDataSource session = new CompositeDataSource(this);
        session.setConfiguration(conf);
        return session;
    }

}
