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
public class ListIntegerTest extends FeatureTestListNumber {

    @Override
    public ListInteger createConstantCollection() {
        return new ListInteger() {

            @Override
            public int size() {
                return 10;
            }

            @Override
            public int getInt(int index) {
                return 1;
            }
        };
    }

    @Override
    public ListInteger createRampCollection() {
        return new ListInteger() {

            @Override
            public int size() {
                return 10;
            }

            @Override
            public int getInt(int index) {
                return index;
            }
        };
    }

    @Override
    public ListNumber createModifiableCollection() {
        return new ListInteger() {
            
            private int[] array = new int[10];
            
            @Override
            public int getInt(int index) {
                return array[index];
            }

            @Override
            public void setInt(int index, int value) {
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
        return new ListInteger() {

            @Override
            public int size() {
                return 0;
            }

            @Override
            public int getInt(int index) {
                return 1;
            }
        };
    }
}
