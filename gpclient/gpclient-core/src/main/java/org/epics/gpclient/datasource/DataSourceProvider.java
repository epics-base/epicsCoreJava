/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient.datasource;

import org.epics.util.compat.legacy.functional.Consumer;
import org.epics.util.compat.legacy.service.ServiceLoader;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class that provides support for a {@link DataSource}.
 * <p>
 * This interface allows different modules to registers a DataSource through
 * the ServiceLoader. Implementations that are correctly registered will
 * be asked to create a DataSource instance which will be registered into
 * a CompositeDataSource with the given name.
 * <p>
 * The factory only needs to care about the DataSource creation, and not the
 * rest of the life-cycle.
 *
 * @author carcassi
 */
public abstract class DataSourceProvider {

    private static final Logger log = Logger.getLogger(DataSourceProvider.class.getName());

    /**
     * The name to be used when registering the DataSource with the
     * CompositeDataSource.
     *
     * @return a short String
     */
    public abstract String getName();

    /**
     * Creates a new instance of the DataSource.
     *
     * @return a new DataSource
     */
    public abstract DataSource createInstance();

    /**
     * Looks up the registered factories and creates a {@link CompositeDataSource}
     * using them.
     *
     * @return a new DataSource
     */
    public static CompositeDataSource createDataSource() {
        final CompositeDataSource composite = new CompositeDataSource();
        load(DataSourceProvider.class, log, new Consumer<DataSourceProvider>() {
            @Override
            public void accept(DataSourceProvider dataSourceProvider) {
                composite.putDataSource(dataSourceProvider);
            }
        });
        return composite;
    }

    private static <T> void load(Class<T> serviceClazz, Logger log, Consumer<T> consumer) {
        log.log(Level.CONFIG, "Fetching {0}s", serviceClazz.getSimpleName());
        int count = 0;
        for (T service : ServiceLoader.load(serviceClazz)) {
            log.log(Level.CONFIG, "Found {0} ({1})", new Object[] {serviceClazz.getSimpleName(), service.getClass().getSimpleName()});
            try {
                consumer.accept(service);
                count++;
            } catch (RuntimeException ex) {
                log.log(Level.WARNING, "Couldn't register " + serviceClazz.getSimpleName() + " (" + service.getClass().getSimpleName() + ")", ex);
            }
        }
        log.log(Level.CONFIG, "Found {0} {1}s", new Object[] {count, serviceClazz.getSimpleName()});
    }
}
