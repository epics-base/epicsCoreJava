/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.gpclient.expression;

import java.util.function.Supplier;
import org.epics.gpclient.PVDirector;

/**
 * An expression to read at the desired rate.
 * <p>
 * Don't implement objects with this interface, use {@link ReadExpressionImpl}.
 *
 * @param <R> type of the read payload
 * @author carcassi
 */
public interface ReadExpression<R> extends ReadExpressionList<R> {
    
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
}
