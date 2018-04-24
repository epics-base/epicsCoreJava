/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.gpclient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
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

    public PVWriter<T> start();
}
