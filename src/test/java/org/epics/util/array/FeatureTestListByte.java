/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.util.array;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

public abstract class FeatureTestListByte extends FeatureTestListNumber {

    @Override
    abstract public ListByte createConstantCollection();
    
    @Override
    abstract public ListByte createRampCollection();
    
    @Test
    public void toString1() {
        assertThat(createRampCollection().toString(), equalTo("[0, 1, 2, 3, 4, 5, 6, 7, 8, 9]"));
    }
    
}
