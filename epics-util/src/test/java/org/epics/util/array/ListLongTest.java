/**
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.array;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author carcassi
 */
public class ListLongTest extends FeatureTestListNumber {

    @Override
    public ListLong createConstantCollection() {
        return new ListLong() {

            @Override
            public int size() {
                return 10;
            }

            @Override
            public long getLong(int index) {
                return 1L;
            }
        };
    }

    @Override
    public ListLong createRampCollection() {
        return new ListLong() {

            @Override
            public int size() {
                return 10;
            }

            @Override
            public long getLong(int index) {
                return index;
            }
        };
    }

    @Override
    public ListNumber createModifiableCollection() {
        return new ListLong() {
            
            private long[] array = new long[10];
            
            @Override
            public long getLong(int index) {
                return array[index];
            }

            @Override
            public void setLong(int index, long value) {
                array[index] = value;
            }

            @Override
            public int size() {
                return array.length;
            }
        };
    }

    @Override
    public ListNumber createEmpty() {
        return new ListLong() {

            @Override
            public int size() {
                return 0;
            }

            @Override
            public long getLong(int index) {
                return 1L;
            }
        };
    }
}
