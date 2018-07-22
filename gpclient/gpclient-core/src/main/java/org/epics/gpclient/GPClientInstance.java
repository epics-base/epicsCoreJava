/**
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.epics.gpclient.datasource.DataSource;
import org.epics.vtype.VType;
import static org.epics.gpclient.GPClient.*;

/**
 * An instance of the general purpose client. Typically one would use the
 * default instance provided by {@link GPClient}.
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
    
    /**
     * Reads the value of the given expression, asking for {@link VType} values.
     * 
     * @param channelName the name of the channel
     * @return the future value
     */
    public Future<VType> readOnce(String channelName) {
        return readOnce(channel(channelName));
    }
    
    /**
     * Reads the value of the given expression.
     * 
     * @param <R> the read type
     * @param expression the expression to read
     * @return the future value
     */
    public <R> Future<R> readOnce(Expression<R, ?> expression) {
        final AtomicReference<R> result = new AtomicReference<>();
        final AtomicReference<Exception> error = new AtomicReference<>();
        final CountDownLatch latch = new CountDownLatch(1);
        final PVReader<R> pvReader = read(expression).addReadListener((event, pv) -> {
            if (event.isType(PVEvent.Type.VALUE)) {
                result.set(pv.getValue());
                pv.close();
                latch.countDown();
            }
            if (event.isType(PVEvent.Type.EXCEPTION)) {
                error.set(event.getException());
                pv.close();
                latch.countDown();
            }
        }).start();
        Future<R> future = new Future<R>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                boolean toCancel = !pvReader.isClosed();
                if (toCancel) {
                    pvReader.close();
                }
                return toCancel;
            }

            @Override
            public boolean isCancelled() {
                return pvReader.isClosed() && result.get() == null;
            }

            @Override
            public boolean isDone() {
                return pvReader.isClosed() && result.get() != null;
            }

            @Override
            public R get() throws InterruptedException, ExecutionException {
                latch.await();
                return getResult();
            }
            
            private R getResult() throws ExecutionException {
                if (error.get() != null) {
                    throw new ExecutionException(error.get());
                } else {
                    return result.get();
                }
            }

            @Override
            public R get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                if (latch.await(timeout, unit)) {
                    return getResult();
                } else {
                    throw new TimeoutException();
                }
            }
        };
        return future;
    }
    
    /**
     * Reads the channel with the given name, asking for {@link VType} values.
     * 
     * @param channelName the name of the channel
     * @return the configuration options
     */
    public PVReaderConfiguration<VType> read(String channelName) {
        return read(channel(channelName));
    }
    
    /**
     * Create a reader for the given expression.
     * 
     * @param <R> the read type
     * @param expression the expression to read
     * @return the configuration options
     */
    public <R> PVReaderConfiguration<R> read(Expression<R, ?> expression) {
        return new PVConfiguration<>(this, expression, PVConfiguration.Mode.READ);
    }
    
    /**
     * Creates a writer for the given expression.
     * 
     * @param <W> the write type
     * @param expression the expression to be written to
     * @return the configurations options
     */
    public <W> PVWriterConfiguration<W> write(Expression<?, W> expression) {
        return new PVConfiguration<>(this, expression, PVConfiguration.Mode.WRITE);
    }
    
    /**
     * Reads and writes the channel with the given name, asking for {@link VType} values.
     * 
     * @param channelName the name of the channel
     * @return the configuration options
     */
    public PVConfiguration<VType, Object> readAndWrite(String channelName) {
        return readAndWrite(channel(channelName));
    }
    
    /**
     * Reads and writes the given expression.
     * 
     * @param <R> the read type
     * @param <W> the write type
     * @param expression the expression to read and write
     * @return the configuration options
     */
    public <R, W> PVConfiguration<R, W> readAndWrite(Expression<R, W> expression) {
        return new PVConfiguration<>(this, expression, PVConfiguration.Mode.READ_WRITE);
    }
    
    /**
     * The default {@link DataSource} used by this client instance.
     * 
     * @return the default data source; never null
     */
    public DataSource getDefaultDataSource() {
        return defaultDataSource;
    }

    /**
     * Closes the gpclient instance.
     */
    public void close() {
        defaultDataSource.close();
    }
}
