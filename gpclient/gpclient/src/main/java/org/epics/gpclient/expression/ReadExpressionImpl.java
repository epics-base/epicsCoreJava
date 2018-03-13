/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.gpclient.expression;

import java.util.function.Supplier;

/**
 * Implementation class for {@link ReadExpression}.
 *
 * @param <R> type of the read payload
 * @author carcassi
 */
public class ReadExpressionImpl<R> extends ReadExpressionListImpl<R> implements ReadExpression<R> {

    private final Supplier<R> function;
    private final ReadCollector<?, R> readCollector;
    private final ReadExpressionList<?> expressionChildren;
    
    {
        // Make sure that the list includes this expression
        addThis();
    }

    public ReadExpressionImpl(ReadCollector<?, R> readCollector) {
        this.readCollector = readCollector;
        this.expressionChildren = null;
        this.function = readCollector::getValue;
    }

    public ReadExpressionImpl(ReadExpressionList<?> childExpressions, Supplier<R> function) {
        this.readCollector = null;
        this.expressionChildren = childExpressions;
        this.function = function;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void startRead(Object director) {
        if (expressionChildren != null) {
            for (ReadExpression<?> desiredRateExpression : expressionChildren.getReadExpressions()) {
                desiredRateExpression.startRead(director);
            }
        }
    }

    /**
     * The function that calculates new values for this expression.
     *
     * @return a function
     */
    @Override
    public final Supplier<R> getFunction() {
        return function;
    }
    
}
