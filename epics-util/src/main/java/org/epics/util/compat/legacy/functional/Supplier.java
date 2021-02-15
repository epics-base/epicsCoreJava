package org.epics.util.compat.legacy.functional;

/**
 * Implementation of the Supplier functional interface
 *
 * @param <T> the type of the supplied object
 * @author George McIntyre. 15-Feb-2021, SLAC
 */
public abstract class Supplier<T> {

    /**
     * Gets a result.
     *
     * @return a result
     */
    public abstract T get();
}
