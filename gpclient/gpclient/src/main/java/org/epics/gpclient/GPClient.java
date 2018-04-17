/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.epics.gpclient;

import java.time.Duration;
import java.util.concurrent.Executors;
import org.epics.gpclient.datasource.DataSourceProvider;
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
    
    public static <R> PVReaderConfiguration<R> read(Expression<R, ?> expression) {
        return gpClient.read(expression);
    }
    
    public static PVConfiguration<VType, Object> readAndWrite(String channelName) {
        return gpClient.readAndWrite(channelName);
    }

    public static <R, W> PVConfiguration<R, W> readAndWrite(Expression<R, W> expression) {
        return gpClient.readAndWrite(expression);
    }
    
    public static <R> ReadCollector<R, R> cacheLastValue(Class<R> readType) {
        return new LatestValueCollector<>(readType);
    }
    
    public static <W> WriteCollector<W> writeType(Class<W> writeType) {
        return new WriteCollector<>();
    }
    
    public static <R, W> Expression<R, W> channel(String channelName, ReadCollector<?, R> readCollector, WriteCollector<W> writeCollector) {
        return new DataSourceChannelExpression<>(channelName, readCollector, writeCollector);
    }
    
    public static <R, Object> Expression<R, Object> channel(String channelName, ReadCollector<?, R> readCollector) {
        return new DataSourceChannelExpression<>(channelName, readCollector, new WriteCollector<>());
    }

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
