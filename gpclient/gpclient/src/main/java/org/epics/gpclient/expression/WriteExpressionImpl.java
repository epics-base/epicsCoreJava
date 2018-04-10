/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.gpclient.expression;

import org.epics.gpclient.WriteCollector;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.epics.gpclient.PVDirector;

/**
 * Implementation class for {@link WriteExpression}.
 *
 * @param <W> type of the write payload
 * @author carcassi
 */
public class WriteExpressionImpl<W> extends WriteExpressionListImpl<W> implements WriteExpression<W> {

    private final Consumer<W> writeFunction;
    private final WriteExpressionList<?> expressionChildren;
    
    {
        // Make sure that the list includes this expression
        addThis();
    }
    
    public WriteExpressionImpl(WriteExpressionList<?> childExpressions, Consumer<W> writeFunction) {
        this.expressionChildren = childExpressions;
        this.writeFunction = writeFunction;
    }

    /**
     * Returns the function represented by this expression.
     *
     * @return the function
     */
    @Override
    public final Consumer<W> getWriteFunction() {
        return writeFunction;
    }

    @Override
    public void startWrite(PVDirector pvDirector) {
        if (expressionChildren != null) {
            for (WriteExpression<?> writeExpression : expressionChildren.getWriteExpressions()) {
                writeExpression.startWrite(pvDirector);
            }
        }
    }

    @Override
    public void stopWrite(PVDirector pvDirector) {
        if (expressionChildren != null) {
            for (WriteExpression<?> writeExpression : expressionChildren.getWriteExpressions()) {
                writeExpression.stopWrite(pvDirector);
            }
        }
    }

}
