/**
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.array;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author carcassi
 */
public class ListShortTest extends FeatureTestListNumber {

    @Override
    public ListShort createConstantCollection() {
        return new ListShort() {

            @Override
            public int size() {
                return 10;
            }

            @Override
            public short getShort(int index) {
                return 1;
            }
        };
    }

    @Override
    public ListShort createRampCollection() {
        return new ListShort() {

            @Override
            public int size() {
                return 10;
            }

            @Override
            public short getShort(int index) {
                return (short) index;
            }
        };
    }

    @Override
    public ListNumber createModifiableCollection() {
        return new ListShort() {
            
            private short[] array = new short[10];
            
            @Override
            public short getShort(int index) {
                return array[index];
            }

            @Override
            public void setShort(int index, short value) {
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
        return new ListShort() {

            @Override
            public int size() {
                return 0;
            }

            @Override
            public short getShort(int index) {
                return 1;
            }
        };
    }

}
