/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient.datasource;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A data source that can dispatch a request to multiple different
 * data sources.
 * <p>
 * This supports lazy initialization of DataSources: DataSourceProviders are
 * initialized only if the a channel of the corresponding DataSource is opened.
 * There is not penalty, then, if no channel is every opened.
 *
 * @author carcassi
 */
public class CompositeDataSource extends DataSource {

    private static final Logger log = Logger.getLogger(CompositeDataSource.class.getName());

    // Stores all data sources by name
    private final Map<String, DataSource> dataSources = new ConcurrentHashMap<String, DataSource>();
    private final Map<String, DataSourceProvider> dataSourceProviders = new ConcurrentHashMap<String, DataSourceProvider>();

    private final String delimiter;
    private final String defaultDataSource;

    /**
     * Creates a new CompositeDataSource.
     */
    public CompositeDataSource() {
        this(new CompositeDataSourceConfiguration());
    }

    /**
     * Creates a new CompositeDataSource with the given configuration.
     *
     * @param conf the configuration for the new CompositeDataSource
     */
    public CompositeDataSource(CompositeDataSourceConfiguration conf) {
        this.delimiter = conf.getDelimiter();
        this.defaultDataSource = conf.getDefaultDataSource();
    }

    /**
     * The configuration used for the composite data source.
     *
     * @return the configuration; can't be null
     */
    public CompositeDataSourceConfiguration getConfiguration() {
        return new CompositeDataSourceConfiguration().delimiter(delimiter).defaultDataSource(defaultDataSource);
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
        int indexDelimiter = channelName.indexOf(delimiter);
        if (indexDelimiter == -1) {
            return channelName;
        } else {
            return channelName.substring(indexDelimiter + delimiter.length());
        }
    }

    private String sourceOf(String channelName) {
        int indexDelimiter = channelName.indexOf(delimiter);
        if (indexDelimiter == -1) {
            if (defaultDataSource == null)
                throw new IllegalArgumentException("Channel " + channelName + " uses default data source but one was never set.");
            if (!dataSourceProviders.containsKey(defaultDataSource)) {
                throw new IllegalArgumentException("Channel " + channelName + " uses default data source " + defaultDataSource + " which was not found.");
            }
            return defaultDataSource;
        } else {
            String source = channelName.substring(0, indexDelimiter);
            if (dataSourceProviders.containsKey(source))
                return source;
            throw new IllegalArgumentException("Data source " + source + " for " + channelName + " was not configured.");
        }
    }

    @Override
    public void startRead(final ReadSubscription readRecipe) {
        try {
            String name = nameOf(readRecipe.getChannelName());
            String dataSource = sourceOf(readRecipe.getChannelName());

            if (dataSource == null)
                throw new IllegalArgumentException("Channel " + name + " uses the default data source but one was never set.");

            retrieveDataSource(dataSource).startRead(new ReadSubscription(name, readRecipe.getCollector()));
        } catch (RuntimeException ex) {
            // If data source fail, report the error
            readRecipe.getCollector().notifyError(ex);
        }
    }

    @Override
    public void stopRead(ReadSubscription readRecipe) {
        try {
            String name = nameOf(readRecipe.getChannelName());
            String dataSource = sourceOf(readRecipe.getChannelName());

            if (dataSource == null)
                throw new IllegalArgumentException("Channel " + name + " uses the default data source but one was never set.");

            retrieveDataSource(dataSource).stopRead(new ReadSubscription(name, readRecipe.getCollector()));
        } catch (RuntimeException ex) {
            // If data source fail, report the error
            readRecipe.getCollector().notifyError(ex);
        }
    }

    private DataSource retrieveDataSource(String name) {
        DataSource dataSource = dataSources.get(name);
        if (dataSource == null) {
            DataSourceProvider factory = dataSourceProviders.get(name);
            if (factory == null) {
                throw new IllegalArgumentException("DataSource '" + name + delimiter + "' was not configured.");
            } else {
                dataSource = factory.createInstance();
                if (dataSource == null) {
                    throw new IllegalStateException("DataSourceProvider '" + name + delimiter + "' did not create a valid datasource.");
                }
                dataSources.put(name, dataSource);
                log.log(Level.CONFIG, "Created instance for data source {0} ({1})", new Object[]{name, dataSource.getClass().getSimpleName()});
            }
        }
        return dataSource;
    }

    @Override
    public void startWrite(WriteSubscription writeRecipe) {
        try {
            String name = nameOf(writeRecipe.getChannelName());
            String dataSource = sourceOf(writeRecipe.getChannelName());

            if (dataSource == null)
                throw new IllegalArgumentException("Channel " + name + " uses the default data source but one was never set.");

            retrieveDataSource(dataSource).startWrite(new WriteSubscription(name, writeRecipe.getCollector()));
        } catch (RuntimeException ex) {
            // If data source fail, report the error
            writeRecipe.getCollector().notifyError(ex);
        }
    }

    @Override
    public void stopWrite(WriteSubscription writeRecipe) {
        try {
            String name = nameOf(writeRecipe.getChannelName());
            String dataSource = sourceOf(writeRecipe.getChannelName());

            if (dataSource == null)
                throw new IllegalArgumentException("Channel " + name + " uses the default data source but one was never set.");

            retrieveDataSource(dataSource).stopWrite(new WriteSubscription(name, writeRecipe.getCollector()));
        } catch (RuntimeException ex) {
            // If data source fail, report the error
            writeRecipe.getCollector().notifyError(ex);
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
        for (Entry<String, DataSource> entry : dataSources.entrySet()) {
            String dataSourceName = entry.getKey();
            DataSource dataSource = entry.getValue();
            for (Entry<String, ChannelHandler> channelEntry : dataSource.getChannels().entrySet()) {
                String channelName = channelEntry.getKey();
                ChannelHandler channelHandler = channelEntry.getValue();
                channels.put(dataSourceName + delimiter + channelName, channelHandler);
            }
        }

        return channels;
    }

}
