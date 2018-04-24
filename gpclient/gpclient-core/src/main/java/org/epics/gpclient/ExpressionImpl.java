/**
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient;

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
    private final Consumer<W> writeFunction;
    
    private final ExpressionList<?, ?> expressionChildren;
    
    {
        // Make sure that the list includes this expression
        addThis();
    }

    public ExpressionImpl(ExpressionList<?, ?> childExpressions, Supplier<R> readFunction, Consumer<W> writeFunction) {
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
    public void startRead(PVDirector director) {
        if (expressionChildren != null) {
            for (Expression<?, ?> readExpression : expressionChildren.getExpressions()) {
                readExpression.startRead(director);
            }
        }
    }

    @Override
    public void stopRead(PVDirector director) {
        if (expressionChildren != null) {
            for (Expression<?, ?> readExpression : expressionChildren.getExpressions()) {
                readExpression.startRead(director);
            }
        }
    }
    
    @Override
    public void startWrite(PVDirector director) {
        if (expressionChildren != null) {
            for (Expression<?, ?> writeExpression : expressionChildren.getExpressions()) {
                writeExpression.startWrite(director);
            }
        }
    }
    
    @Override
    public void stopWrite(PVDirector director) {
        if (expressionChildren != null) {
            for (Expression<?, ?> writeExpression : expressionChildren.getExpressions()) {
                writeExpression.startWrite(director);
            }
        }
    }
    
}
