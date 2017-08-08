/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.util.array;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author carcassi
 */
public class ListByteTest extends FeatureTestListByte {

    @Override
    public ListByte createConstantCollection() {
        return new ListByte() {

            @Override
            public int size() {
                return 10;
            }

            @Override
            public byte getByte(int index) {
                return 1;
            }
        };
    }

    @Override
    public ListByte createRampCollection() {
        return new ListByte() {

            @Override
            public int size() {
                return 10;
            }

            @Override
            public byte getByte(int index) {
                return (byte) index;
            }
        };
    }

}
