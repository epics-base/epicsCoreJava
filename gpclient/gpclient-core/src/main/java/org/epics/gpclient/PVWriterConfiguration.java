/**
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient;

import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import org.epics.gpclient.datasource.DataSource;

/**
 * An expression used to set the final parameters on how the pv expression
 * should be read.
 * 
 * @param <T> the type of the expression
 * @author carcassi
 */
public interface PVWriterConfiguration<T> {

    public PVWriterConfiguration<T> from(DataSource dataSource);

    public PVWriterConfiguration<T> notifyOn(Executor onThread);
    
    public PVWriterConfiguration<T> connectionTimeout(Duration timeout);
    
    public PVWriterConfiguration<T> connectionTimeout(Duration timeout, String timeoutMessage);
    
    public PVWriterConfiguration<T> maxRate(Duration maxRate);
    
    public PVWriterConfiguration<T> addListener(Consumer<PVEvent> listener);

    public PVWriter<T> start();
}
