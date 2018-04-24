/**
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient;

import java.util.List;

/**
 * An list of expressions to write and to read at the desired rate.
 *
 * @param <R> type of the read payload
 * @param <W> type of the write payload
 * @author carcassi
 */
public interface ExpressionList<R, W> {
    
    /**
     * Adds the given expressions to this list.
     * 
     * @param expressions a list of expressions
     * @return this
     */
    public ExpressionList<R, W> and(ExpressionList<? extends R, ? extends W> expressions);

    /**
     * The expressions of this list.
     * 
     * @return a list of expressions
     */
    public List<Expression<R, W>> getExpressions();
}
