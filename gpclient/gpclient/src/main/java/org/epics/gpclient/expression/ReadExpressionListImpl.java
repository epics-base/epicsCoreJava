/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.gpclient.expression;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Implementation class for {@link ReadExpressionList}.
 *
 * @param <R> type of the read payload
 * @author carcassi
 */
public class ReadExpressionListImpl<R> implements ReadExpressionList<R> {
    
    private List<ReadExpression<R>> readExpressions;
    
    final void addThis() {
        readExpressions.add((ReadExpression<R>) this);
    }

    /**
     * Creates a new empty expression list.
     */
    public ReadExpressionListImpl() {
        this.readExpressions = new ArrayList<ReadExpression<R>>();
    }

    ReadExpressionListImpl(Collection<? extends ReadExpression<R>> desiredRateExpressions) {
        this.readExpressions = new ArrayList<ReadExpression<R>>(desiredRateExpressions);
    }
    
    @Override
    public final ReadExpressionListImpl<R> and(ReadExpressionList<? extends R> expressions) {
        @SuppressWarnings("unchecked")
        ReadExpressionList<R> newExpression = (ReadExpressionList<R>) (ReadExpressionList) expressions;
        readExpressions.addAll(newExpression.getReadExpressions());
        return this;
    }

    @Override
    public final List<ReadExpression<R>> getReadExpressions() {
        return readExpressions;
    }
    
}
