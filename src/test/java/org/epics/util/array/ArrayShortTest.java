/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.util.array;

import org.epics.util.array.ArrayShort;
import org.epics.util.array.CollectionNumbers;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author carcassi
 */
public class ArrayShortTest extends FeatureTestListShort{

    @Override
    public ArrayShort createConstantCollection() {
        return new ArrayShort(new short[] {1, 1, 1, 1, 1, 1, 1, 1, 1, 1});
    }

    @Override
    public ArrayShort createRampCollection() {
        return new ArrayShort(new short[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
    }

    @Override
    public ArrayShort createModifiableCollection() {
        return new ArrayShort(new short[10], false);
    }
}
