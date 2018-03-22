/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.gpclient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;
import org.epics.gpclient.datasource.DataSource;
import org.epics.gpclient.expression.ReadCollector;
import org.epics.gpclient.expression.ReadExpression;
import org.epics.gpclient.expression.ScannerParameters;
import org.epics.gpclient.expression.SourceDesiredRateDecoupler;

/**
 * Allows to configure the type of read/write PV to create.
 *
 * @param <R> the read payload
 * @param <W> the write payload
 * @author carcassi
 */
public class PVConfiguration<R, W> implements PVReaderConfiguration<R> {

    final ReadExpression<R> readExpression;
    final GPClientConfiguration config;
    
    Executor notificationExecutor;
    DataSource dataSource;
    Duration connectionTimeout;
    String connectionTimeoutMessage;
    Duration maxRate;
    PVReaderListener<R> listener;

    public PVConfiguration(GPClientConfiguration config, ReadExpression<R> readExpression) {
        this.config = config;
        this.readExpression = readExpression;
    }

    /**
     * Defines which DataSource should be used to read the data.
     *
     * @param dataSource a connection manager
     * @return this
     */
    public PVConfiguration<R, W> from(DataSource dataSource) {
        // TODO: check all parameters for double setting
        if (dataSource == null) {
            throw new IllegalArgumentException("dataSource can't be null");
        }
        this.dataSource = dataSource;
        return this;
    }

    /**
     * Defines on which thread the PVManager should notify the client.
     *
     * @param onThread the thread on which to notify
     * @return this
     */
    public PVConfiguration<R, W> notifyOn(Executor onThread) {
        if (this.notificationExecutor == null) {
            this.notificationExecutor = onThread;
        } else {
            throw new IllegalStateException("Already set what thread to notify");
        }
        return this;
    }
    
    public PVConfiguration<R, W> maxRate(Duration maxRate) {
        if (this.maxRate != null)
            throw new IllegalStateException("Max rate already set");
        GPClientGlobalChecks.validateMaxRate(maxRate);
        this.maxRate = maxRate;
        return this;
    }
    
    public PVConfiguration<R, W> connectionTimeout(Duration timeout) {
        if (this.connectionTimeout != null)
            throw new IllegalStateException("Timeout already set");
        this.connectionTimeout = timeout;
        return this;
    }
    
    public PVConfiguration<R, W> connectionTimeout(Duration timeout, String timeoutMessage) {
        connectionTimeout(timeout);
        this.connectionTimeoutMessage = timeoutMessage;
        return this;
    }

    void checkParameters() {
        // Get defaults
        if (dataSource == null) {
            dataSource = config.defaultDataSource;
        }
        if (notificationExecutor == null) {
            notificationExecutor = config.defaultNotificationExecutor;
        }
        
        if (maxRate == null) {
            maxRate = config.defaultMaxRate;
        }

        if (connectionTimeoutMessage == null)
            connectionTimeoutMessage = "Connection timeout";
        
        // Check that a data source has been specified
        if (dataSource == null) {
            throw new IllegalStateException("You need to specify a source either "
                    + "using PVManager.setDefaultDataSource or by using "
                    + "read(...).from(dataSource).");
        }

        // Check that thread switch has been specified
        if (notificationExecutor == null) {
            throw new IllegalStateException("You need to specify a thread either "
                    + "using PVManager.setDefaultThreadSwitch or by using "
                    + "read(...).andNotify(threadSwitch).");
        }
        
        // Check that a listener has been specified
        if (listener == null) {
            throw new IllegalStateException("You need to specify a listener "
                    + "by using "
                    + "read(...).listener(listener).");
        }
    }
    
    public PVConfiguration<R, W>  addListener(PVReaderListener<R> listener) {
        this.listener = listener;
        return this;
    }

    public PVConfiguration<R, W> addListener(PVWriterListener<W> listener) {
        //this.listener = listener;
        return this;
    }

    public PVConfiguration<R, W> addListener(PVListener<R, W> listener) {
        this.listener = listener;
        return this;
    }
    
    public PV<R, W> start() {
        checkParameters();
        PVImpl<R, W> pv = new PVImpl<R, W>(listener);
        Supplier<R> readFunction = readExpression.getFunction();
        
        PVDirector<R, W> director = new PVDirector<R, W>(pv, readFunction, config.dataProcessingThreadPool,
                notificationExecutor, dataSource);
        if (connectionTimeout != null) {
            director.readTimeout(connectionTimeout, connectionTimeoutMessage);
        }
        
        ScannerParameters scannerParameters = new ScannerParameters()
                .desiredRateListener(director.getDesiredRateEventListener())
                .scannerExecutor(config.dataProcessingThreadPool)
                .maxDuration(maxRate);
        if (readFunction instanceof ReadCollector) {
            scannerParameters.type(ScannerParameters.Type.PASSIVE);
        } else {
            scannerParameters.type(ScannerParameters.Type.ACTIVE);
        }
        SourceDesiredRateDecoupler rateDecoupler = scannerParameters.build();
        
        pv.setDirector(director);
        director.setScanner(rateDecoupler);
        director.connectReadExpression(readExpression);
        rateDecoupler.start();
        
        return pv;
    }
    
}
