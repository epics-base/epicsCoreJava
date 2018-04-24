/**
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient;

import java.time.Duration;
import java.util.concurrent.Executor;
import org.epics.gpclient.datasource.DataSource;

/**
 * An expression used to set the final parameters on how the pv expression
 * should be read.
 * 
 * @param <T> the type of the expression
 * @author carcassi
 */
public interface PVReaderConfiguration<T> {

    public PVReaderConfiguration<T> from(DataSource dataSource);

    public PVReaderConfiguration<T> notifyOn(Executor onThread);
    
    public PVReaderConfiguration<T> connectionTimeout(Duration timeout);
    
    public PVReaderConfiguration<T> connectionTimeout(Duration timeout, String timeoutMessage);
    
    public PVReaderConfiguration<T> maxRate(Duration maxRate);
    
    public PVReaderConfiguration<T> addListener(PVReaderListener<T> listener);

    public PVReader<T> start();
}
