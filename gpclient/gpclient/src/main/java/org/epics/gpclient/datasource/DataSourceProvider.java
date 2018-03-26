/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.gpclient.datasource;

import java.io.InputStream;
import java.util.ServiceLoader;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class that provides support for a DataSource.
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
     * Looks up the registered factories and creates a CompositeDataSource
     * using them.
     * 
     * @return a new DataSource
     */
    public static CompositeDataSource createDataSource() {
        CompositeDataSource composite = new CompositeDataSource();
        load(DataSourceProvider.class, log, composite::putDataSource);
        return composite;
    }
    
    public static <T> void load(Class<T> serviceClazz, Logger log, Consumer<T> consumer) {
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
