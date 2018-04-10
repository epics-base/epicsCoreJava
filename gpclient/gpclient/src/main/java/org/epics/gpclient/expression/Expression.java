/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.gpclient.expression;

import java.util.function.Consumer;
import java.util.function.Supplier;
import org.epics.gpclient.PVDirector;

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
