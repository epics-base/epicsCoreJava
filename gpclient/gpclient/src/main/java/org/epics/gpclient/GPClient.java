/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.epics.gpclient;

import java.time.Duration;
import java.util.concurrent.Executors;
import org.epics.gpclient.datasource.DataSourceProvider;
import org.epics.gpclient.expression.ReadExpression;
import org.epics.vtype.VType;

/**
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
    
    public static PVReaderConfiguration<VType> read(String channelName) {
        return gpClient.read(channelName);
    }
    
    public static <R> PVReaderConfiguration<R> read(ReadExpression<R> expression) {
        return gpClient.read(expression);
    }
    
    /**
     * Reads the given channel, skipping values in case of a burst of updates.
     * 
     * @param <R> the type to read
     * @param channel the channel name
     * @return the read expression
     */
    public static <R> ReadExpression<R> latestValueFrom(DataChannel<R> channel) {
        return new LatestValueFromChannelExpression<>(channel, new LatestValueCollector<>(channel.getReadType()));
    }
    
    /**
     * A datasource channel.
     * 
     * @param <R> the type of data to read
     * @param channelName the channel name
     * @param readType the type of data to read
     * @return the channel
     */
    public static <R> DataChannel<R> channel(String channelName, Class<R> readType) {
        return new DataSourceChannel<>(channelName, readType);
    }

    /**
     * A datasource channel that reads {@link VType}s.
     * 
     * @param channelName the channel name
     * @return the channel
     */
    public static DataChannel<VType> channel(String channelName) {
        return new DataSourceChannel<>(channelName, VType.class);
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
