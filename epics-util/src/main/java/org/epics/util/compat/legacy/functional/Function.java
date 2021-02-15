package org.epics.util.compat.legacy.functional;

/**
 * Implementation of the functional interface class Function
 *
 * @param <T> Parameter type
 * @param <R> Return type
 * @author George McIntyre. 15-Feb-2021, SLAC
 */
public abstract class Function<T, R> {

    /**
     * Applies this function to the given argument.
     *
     * @param t the function argument
     * @return the function result
     */
    public abstract R apply(T t);

}
