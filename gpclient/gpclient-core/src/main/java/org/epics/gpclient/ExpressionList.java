/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient;

import java.util.ArrayList;
import java.util.List;

/**
 * A list of expressions that can be read and written by the gpclient.
 *
 * @param <R> type of read payload
 * @param <W> type of write payload
 * @author carcassi
 */
public class ExpressionList<R, W> {

    private final List<Expression<R, W>> expressions = new ArrayList<Expression<R, W>>();

    final void addThis() {
        expressions.add((Expression<R, W>) this);
    }

    /**
     * Creates a new empty expression list.
     */
    public ExpressionList() {
    }

    /**
     * Adds the given expressions to this list.
     *
     * @param expressions a list of expressions
     * @return this
     */
    public final ExpressionList<R, W> and(ExpressionList<? extends R, ? extends W> expressions) {
        @SuppressWarnings("unchecked")
        ExpressionList<R, W> newExpression = (ExpressionList<R, W>) (ExpressionList) expressions;
        this.expressions.addAll(newExpression.getExpressions());
        return this;
    }

    /**
     * The expressions of this list.
     *
     * @return a list of expressions
     */
    public final List<Expression<R, W>> getExpressions() {
        return expressions;
    }

}
