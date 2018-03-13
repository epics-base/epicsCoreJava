/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.gpclient.expression;

import java.util.function.Consumer;

/**
 * An expression to write.
 * <p>
 * Don't implement objects with this interface, use {@link WriteExpressionImpl}.
 *
 * @param <W> the write payload
 * @author carcassi
 */
public interface WriteExpression<W> extends WriteExpressionList<W> {
    
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
     * @param builder the recipe to fill
     */
    public void startWrite(Object director);
}
