/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.util.array;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static org.epics.util.array.CollectionNumbers.*;

/**
 *
 * @author carcassi
 */
public class ListDoubleTest extends FeatureTestListDouble {

    @Override
    public ListDouble createConstantCollection() {
        return new ListDouble() {
            @Override
            public int size() {
                return 10;
            }

            @Override
            public double getDouble(int index) {
                return 1.0;
            }
        };
    }

    @Override
    public ListDouble createRampCollection() {
        return new ListDouble() {
            @Override
            public int size() {
                return 10;
            }

            @Override
            public double getDouble(int index) {
                return index;
            }
        };
    }

    @Override
    public ListNumber createModifiableCollection() {
        return new ListDouble() {
            
            private double[] array = new double[10];
            
            @Override
            public double getDouble(int index) {
                return array[index];
            }

            @Override
            public void setDouble(int index, double value) {
                array[index] = value;
            }

            @Override
            public int size() {
                return array.length;
            }
        };
    }
}
