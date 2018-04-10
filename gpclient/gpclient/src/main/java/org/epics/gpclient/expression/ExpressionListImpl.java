/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.gpclient.expression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation class for {@link ExpressionList}.
 *
 * @param <R> type of read payload
 * @param <W> type of write payload
 * @author carcassi
 */
public class ExpressionListImpl<R, W> implements ExpressionList<R, W> {
    
    private final List<Expression<R, W>> expressions = new ArrayList<Expression<R, W>>();
    
    final void addThis() {
        expressions.add((Expression<R, W>) this);
    }

    @Override
    public final ExpressionList<R, W> and(ExpressionList<? extends R, ? extends W> expressions) {
        @SuppressWarnings("unchecked")
        ExpressionList<R, W> newExpression = (ExpressionList<R, W>) (ExpressionList) expressions;
        this.expressions.addAll(newExpression.getExpressions());
        return this;
    }

    @Override
    public final List<Expression<R, W>> getExpressions() {
        return expressions;
    }
    
}
