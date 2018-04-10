/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.epics.gpclient;

import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import org.epics.gpclient.datasource.DataSource;
import org.epics.gpclient.expression.ReadExpression;
import org.epics.vtype.VType;
import static org.epics.gpclient.GPClient.*;

/**
 *
 * @author carcassi
 */
public class GPClientInstance {
    
    final ScheduledExecutorService dataProcessingThreadPool;
    final DataSource defaultDataSource;
    final Duration defaultMaxRate;
    final Executor defaultNotificationExecutor;

    GPClientInstance(GPClientConfiguration config) {
        this.dataProcessingThreadPool = config.dataProcessingThreadPool;
        this.defaultDataSource = config.defaultDataSource;
        this.defaultMaxRate = config.defaultMaxRate;
        this.defaultNotificationExecutor = config.defaultNotificationExecutor;
    }
    
    public PVReaderConfiguration<VType> read(String channelName) {
        return read(channel(channelName));
    }
    
    public <R> PVReaderConfiguration<R> read(ReadExpression<R> expression) {
        return new PVConfiguration<>(this, expression);
    }
    
    public DataSource getDefaultDataSource() {
        return defaultDataSource;
    }
}
