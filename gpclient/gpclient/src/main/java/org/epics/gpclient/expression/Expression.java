/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.gpclient.expression;

/**
 * An expression to write and to read at the desired rate.
 * <p>
 * Don't implement objects with this interface, use {@link DesiredRateReadWriteExpressionImpl}.
 *
 * @param <R> type of the read payload
 * @param <W> type of the write payload
 * @author carcassi
 */
public interface Expression<R, W> extends ReadExpression<R>, WriteExpression<W>, ExpressionList<R, W> {
    
    public void start(Object director);
    
}
