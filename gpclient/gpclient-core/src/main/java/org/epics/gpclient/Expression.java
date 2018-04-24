/**
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * An expression to write and to read at the desired rate.
 *
 * @param <R> type of the read payload
 * @param <W> type of the write payload
 * @author carcassi
 */
public interface Expression<R, W> extends ExpressionList<R, W> {
    
    /**
     * Prepares the recipe to connect the channels needed by this expression.
     * <p>
     * A dynamic expression, one for which the child expressions can change,
     * can keep a reference to the director to connect/disconnect new child
     * expressions.
     *
     * @param director the director for the reader
     */
    public void startRead(PVDirector director);
    
    public void stopRead(PVDirector director);
    
    /**
     * The function that calculates this expression.
     *
     * @return the expression function
     */
    public Supplier<R> getFunction();
    
    /**
     * The function that implements this expression.
     *
     * @return the expression function
     */
    public Consumer<W> getWriteFunction();
    
    /**
     * Prepares the recipe to connect the channels needed by this expression.
     * <p>
     * A dynamic expression, one for which the child expressions can change,
     * can keep a reference to the director to connect/disconnect new child
     * expressions.
     *
     * @param director the director for the reader
     */
    public void startWrite(PVDirector director);
    
    public void stopWrite(PVDirector director);
}