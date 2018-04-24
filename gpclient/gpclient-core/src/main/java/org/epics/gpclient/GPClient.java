/**
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient;

import java.time.Duration;
import java.util.concurrent.Executors;
import org.epics.gpclient.datasource.DataSourceProvider;
import org.epics.vtype.VType;

/**
 * The default instance for the generic purpose client.
 *
 * @author carcassi
 */
public class GPClient {
    
    static {
        gpClient = new GPClientConfiguration().defaultMaxRate(Duration.ofMillis(50))
                .notificationExecutor(org.epics.util.concurrent.Executors.localThread())
                .dataSource(DataSourceProvider.createDataSource())
                .dataProcessingThreadPool(Executors.newScheduledThreadPool(Math.max(1, Runtime.getRuntime().availableProcessors() - 1),
                org.epics.util.concurrent.Executors.namedPool("PVMgr Worker "))).build();
    }
    
    private static final GPClientInstance gpClient;
    
    /**
     * Reads the channel with the given name, asking for {@link VType} values.
     * 
     * @param channelName the name of the channel
     * @return the configuration options
     */
    public static PVReaderConfiguration<VType> read(String channelName) {
        return gpClient.read(channelName);
    }
    
    /**
     * Reads the given expression.
     * 
     * @param <R> the read type
     * @param expression the expression to read
     * @return the configuration options
     */
    public static <R> PVReaderConfiguration<R> read(Expression<R, ?> expression) {
        return gpClient.read(expression);
    }
    
    /**
     * Reads and writes the channel with the given name, asking for {@link VType} values.
     * 
     * @param channelName the name of the channel
     * @return the configuration options
     */
    public static PVConfiguration<VType, Object> readAndWrite(String channelName) {
        return gpClient.readAndWrite(channelName);
    }

    /**
     * Reads and writes the given expression.
     * 
     * @param <R> the read type
     * @param <W> the write type
     * @param expression the expression to read and write
     * @return the configuration options
     */
    public static <R, W> PVConfiguration<R, W> readAndWrite(Expression<R, W> expression) {
        return gpClient.readAndWrite(expression);
    }
    
    /**
     * Keep only the latest value from the channel.
     * <p>
     * In case of data bursts (i.e. data coming in at rate faster than the
     * reader can handle) this strategy will skip the notification in between,
     * but always notify on the last value.
     * 
     * @param <R> the type to read
     * @param readType the type to read
     * @return the caching strategy
     */
    public static <R> ReadCollector<R, R> cacheLastValue(Class<R> readType) {
        return new LatestValueCollector<>(readType);
    }

    /**
     * A write buffer for the the given type.
     * 
     * @param <W> the type to write
     * @param writeType the type to write
     * @return the caching strategy
     */
    public static <W> WriteCollector<W> writeType(Class<W> writeType) {
        return new WriteCollector<>();
    }

    /**
     * A channel that reads and writes the given data types with the given strategy.
     * 
     * @param <R> the type to read
     * @param <W> the type to write
     * @param channelName the name of the channel
     * @param readCollector the read buffer
     * @param writeCollector the write buffer
     * @return a new channel expression
     */
    public static <R, W> Expression<R, W> channel(String channelName, ReadCollector<?, R> readCollector, WriteCollector<W> writeCollector) {
        return new DataSourceChannelExpression<>(channelName, readCollector, writeCollector);
    }
    
    /**
     * A channel that reads the given data type with the given strategy.
     * 
     * @param <R> the type to read
     * @param channelName the name of the channel
     * @param readCollector the read buffer
     * @return a new channel expression
     */
    public static <R, Object> Expression<R, Object> channel(String channelName, ReadCollector<?, R> readCollector) {
        return new DataSourceChannelExpression<>(channelName, readCollector, new WriteCollector<>());
    }

    /**
     * A channel that reads {@link VType}s caching the latest value.
     * 
     * @param channelName the name of the channel
     * @return a new channel expression
     */
    public static Expression<VType, Object> channel(String channelName) {
        return channel(channelName, cacheLastValue(VType.class));
    }

    /**
     * The default instance of the general purpose client.
     * 
     * @return the default instance
     */
    public static GPClientInstance defaultInstance() {
        return gpClient;
    }
    
}
