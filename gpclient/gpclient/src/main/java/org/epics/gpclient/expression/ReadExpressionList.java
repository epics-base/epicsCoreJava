/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.gpclient.expression;

import java.util.List;

/**
 * A list of expressions to read at the desired rate.
 * <p>
 * Don't implement objects with this interface, use {@link DesiredRateExpressionListImpl}.
 *
 * @param <R> type of the read payload
 * @author carcassi
 */
public interface ReadExpressionList<R> {
    
    /**
     * Adds the given expressions to this list.
     * 
     * @param expressions a list of expressions
     * @return this
     */
    public ReadExpressionList<R> and(ReadExpressionList<? extends R> expressions);

    /**
     * The expressions of this list.
     * 
     * @return a list of expressions
     */
    public List<ReadExpression<R>> getReadExpressions();
    
}
