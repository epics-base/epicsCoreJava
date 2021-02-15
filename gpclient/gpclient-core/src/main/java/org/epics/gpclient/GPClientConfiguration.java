/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient;

import org.joda.time.Duration;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import org.epics.gpclient.datasource.DataSource;

/**
 * The configuration for an instance of the generic purpose client.
 *
 * @author carcassi
 */
public class GPClientConfiguration {

    Executor defaultNotificationExecutor;
    DataSource defaultDataSource;
    ScheduledExecutorService dataProcessingThreadPool;
    Duration defaultMaxRate;

    /**
     * Sets the default executor on which all notifications are going to be posted.
     *
     * @param defaultNotificationExecutor the default notification executor
     * @return this configuration
     */
    public GPClientConfiguration notificationExecutor(Executor defaultNotificationExecutor) {
        this.defaultNotificationExecutor = defaultNotificationExecutor;
        return this;
    }

    /**
     * Sets the default source for data.
     *
     * @param defaultDataSource the default data source
     * @return this configuration
     */
    public GPClientConfiguration dataSource(DataSource defaultDataSource) {
        this.defaultDataSource = defaultDataSource;
        return this;
    }

    /**
     * Sets the thread pool for data processing. This includes: data
     * rate decoupling, data aggregation, data computation and (possibly)
     * data notification (if notification is set on the local thread).
     *
     * @param dataProcessingThreadPool the thread pool used for data processing
     * @return this configuration
     */
    public GPClientConfiguration dataProcessingThreadPool(ScheduledExecutorService dataProcessingThreadPool) {
        this.dataProcessingThreadPool = dataProcessingThreadPool;
        return this;
    }

    /**
     * Sets the default maximum rate of notification for pvs. A maxRate of
     * 100 ms means notification are not going to come faster than every 100 ms.
     * This value can be set pv by pv.
     *
     * @param defaultMaxRate the default maximum rate of notification for pvs
     * @return this configuration
     */
    public GPClientConfiguration defaultMaxRate(Duration defaultMaxRate) {
        GPClientGlobalChecks.validateMaxRate(defaultMaxRate);
        this.defaultMaxRate = defaultMaxRate;
        return this;
    }

    private void validateConfiguration() {

    }

    public GPClientInstance build() {
        validateConfiguration();
        return new GPClientInstance(this);
    }

}
