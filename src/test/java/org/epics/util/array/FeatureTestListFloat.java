/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.util.array;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

public abstract class FeatureTestListFloat extends FeatureTestListNumber {

    @Override
    abstract public ListFloat createConstantCollection();
    
    @Override
    abstract public ListFloat createRampCollection();

    @Test
    public void toString1() {
        assertThat(createRampCollection().toString(), equalTo("[0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0]"));
    }
    
}
