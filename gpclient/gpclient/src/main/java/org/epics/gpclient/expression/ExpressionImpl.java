/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.gpclient.expression;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Implementation class for {@link Expression}.
 *
 * @param <R> type of the read payload
 * @param <W> type of the write payload
 * @author carcassi
 */
public class ExpressionImpl<R, W> extends ExpressionListImpl<R, W> implements Expression<R, W> {

    private final Supplier<R> readFunction;
    private final ReadCollector<?, R> readCollector;

    private final Consumer<W> writeFunction;
    private final WriteCollector<W> writeCollector;
    
    private final ExpressionList<?, ?> expressionChildren;
    
    {
        // Make sure that the list includes this expression
        addThis();
    }

    public ExpressionImpl(ReadCollector<?, R> readCollector, WriteCollector<W> writeCollector) {
        //TODO: check for null
        this.readCollector = readCollector;
        this.writeCollector = writeCollector;
        this.expressionChildren = null;
        // This makes sure the expression does not expose the full collector
        this.readFunction = readCollector::getValue;
        this.writeFunction = writeCollector::queueValue;
    }

    public ExpressionImpl(ExpressionList<?, ?> childExpressions, Supplier<R> readFunction, Consumer<W> writeFunction) {
        this.readCollector = null;
        this.writeCollector = null;
        this.expressionChildren = childExpressions;
        this.readFunction = readFunction;
        this.writeFunction = writeFunction;
    }

    @Override
    public final Supplier<R> getFunction() {
        return readFunction;
    }

    @Override
    public final Consumer<W> getWriteFunction() {
        return writeFunction;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public void startRead(Object director) {
        if (expressionChildren != null) {
            for (ReadExpression<?> readExpression : expressionChildren.getReadExpressions()) {
                readExpression.startRead(director);
            }
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public void startWrite(Object director) {
        if (expressionChildren != null) {
            for (WriteExpression<?> writeExpression : expressionChildren.getWriteExpressions()) {
                writeExpression.startWrite(director);
            }
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public void start(Object director) {
        if (expressionChildren != null) {
            for (Expression<?, ?> writeExpression : expressionChildren.getExpressions()) {
                writeExpression.start(director);
            }
        }
    }
    
}
