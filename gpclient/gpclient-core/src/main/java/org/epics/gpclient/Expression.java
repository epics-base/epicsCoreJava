/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient;

import org.epics.util.compat.legacy.functional.Consumer;
import org.epics.util.compat.legacy.functional.Supplier;

/**
 * An expression that can be read or written by the gpclient.
 *
 * @param <R> type of the read payload
 * @param <W> type of the write payload
 * @author carcassi
 */
public class Expression<R, W> extends ExpressionList<R, W> {

    private final Supplier<R> readFunction;
    private final Consumer<W> writeFunction;

    private final ExpressionList<?, ?> expressionChildren;

    {
        // Make sure that the list includes this expression
        addThis();
    }

    /**
     * Creates a new expression. If not null, start/stop read/write will cascade on the
     * children.
     *
     * @param childExpressions the child expression, can be null
     * @param readFunction the read function, can't be null
     * @param writeFunction the write function, can't be null
     */
    public Expression(ExpressionList<?, ?> childExpressions, Supplier<R> readFunction, Consumer<W> writeFunction) {
        if (readFunction == null) {
            throw new NullPointerException("readFunction can't be null");
        }
        if (writeFunction == null) {
            throw new NullPointerException("writeFunction can't be null");
        }
        this.expressionChildren = childExpressions;
        this.readFunction = readFunction;
        this.writeFunction = writeFunction;
    }

    /**
     * The function that returns the current values for this expression.
     *
     * @return the read function
     */
    public final Supplier<R> getFunction() {
        return readFunction;
    }

    /**
     * The function that allows to write a value for this expression.
     *
     * @return the write function
     */
    public final Consumer<W> getWriteFunction() {
        return writeFunction;
    }

    /**
     * Starts the read notifications for this expression.
     * <p>
     * A dynamic expression, one for which the child expressions can change,
     * can keep a reference to the director to connect/disconnect new child
     * expressions.
     *
     * @param director the director for the reader
     */
    public void startRead(PVDirector director) {
        if (expressionChildren != null) {
            for (Expression<?, ?> readExpression : expressionChildren.getExpressions()) {
                readExpression.startRead(director);
            }
        }
    }

    /**
     * Stops all read notifications for this expression.
     *
     * @param director the director for the reader
     */
    public void stopRead(PVDirector director) {
        if (expressionChildren != null) {
            for (Expression<?, ?> readExpression : expressionChildren.getExpressions()) {
                readExpression.startRead(director);
            }
        }
    }

    /**
     * Starts the write notifications for this expression.
     * <p>
     * A dynamic expression, one for which the child expressions can change,
     * can keep a reference to the director to connect/disconnect new child
     * expressions.
     *
     * @param director the director for the writer
     */
    public void startWrite(PVDirector director) {
        if (expressionChildren != null) {
            for (Expression<?, ?> writeExpression : expressionChildren.getExpressions()) {
                writeExpression.startWrite(director);
            }
        }
    }

    /**
     * Stops all write notifications for this expression.
     *
     * @param director the director for the writer
     */
    public void stopWrite(PVDirector director) {
        if (expressionChildren != null) {
            for (Expression<?, ?> writeExpression : expressionChildren.getExpressions()) {
                writeExpression.startWrite(director);
            }
        }
    }

}
