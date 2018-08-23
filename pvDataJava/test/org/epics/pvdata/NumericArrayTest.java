/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata;


import junit.framework.TestCase;

import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.pv.PVByteArray;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVDoubleArray;
import org.epics.pvdata.pv.PVFloatArray;
import org.epics.pvdata.pv.PVIntArray;
import org.epics.pvdata.pv.PVLongArray;
import org.epics.pvdata.pv.PVNumberArray;
import org.epics.pvdata.pv.PVShortArray;
import org.epics.pvdata.pv.PVUByteArray;
import org.epics.pvdata.pv.PVUIntArray;
import org.epics.pvdata.pv.PVULongArray;
import org.epics.pvdata.pv.PVUShortArray;
import org.epics.pvdata.pv.ScalarType;
import org.epics.util.array.ArrayByte;
import org.epics.util.array.ArrayDouble;
import org.epics.util.array.ArrayFloat;
import org.epics.util.array.ArrayInteger;
import org.epics.util.array.ArrayLong;
import org.epics.util.array.ArrayShort;
import org.epics.util.array.ArrayUByte;
import org.epics.util.array.ArrayUInteger;
import org.epics.util.array.ArrayULong;
import org.epics.util.array.ArrayUShort;
import org.epics.util.array.CollectionNumbers;
import org.epics.util.array.ListNumber;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

public class NumericArrayTest extends TestCase {

    public void testPutDoubleArray1() {
        PVDataCreate factory = PVDataFactory.getPVDataCreate();
        PVDoubleArray pvArray = (PVDoubleArray) factory.createPVScalarArray(ScalarType.pvDouble);
        assertThat(pvArray.get(), instanceOf(ArrayDouble.class));
        pvArray.put(0, CollectionNumbers.toListDouble(0,1,2,3,4,5,6,7,8,9));
        assertThat(pvArray.get(), equalTo(CollectionNumbers.toListDouble(0,1,2,3,4,5,6,7,8,9)));
        pvArray.put(2, CollectionNumbers.toListFloat(3,2));
        assertThat(pvArray.get(), equalTo(CollectionNumbers.toListDouble(0,1,3,2,4,5,6,7,8,9)));
    }

    public void testPutFloatArray1() {
        PVDataCreate factory = PVDataFactory.getPVDataCreate();
        PVFloatArray pvArray = (PVFloatArray) factory.createPVScalarArray(ScalarType.pvFloat);
        assertThat(pvArray.get(), instanceOf(ArrayFloat.class));
        pvArray.put(0, CollectionNumbers.toListFloat(0,1,2,3,4,5,6,7,8,9));
        assertThat(pvArray.get(), equalTo(CollectionNumbers.toListFloat(0,1,2,3,4,5,6,7,8,9)));
        pvArray.put(2, CollectionNumbers.toListDouble(3,2));
        assertThat(pvArray.get(), equalTo(CollectionNumbers.toListFloat(0,1,3,2,4,5,6,7,8,9)));
    }

    public void testPutLongArray1() {
        PVDataCreate factory = PVDataFactory.getPVDataCreate();
        PVLongArray pvArray = (PVLongArray) factory.createPVScalarArray(ScalarType.pvLong);
        assertThat(pvArray.get(), instanceOf(ArrayLong.class));
        pvArray.put(0, CollectionNumbers.toListLong(0,1,2,3,4,5,6,7,8,9));
        assertThat(pvArray.get(), equalTo(CollectionNumbers.toListLong(0,1,2,3,4,5,6,7,8,9)));
        pvArray.put(2, CollectionNumbers.toListInt(3,2));
        assertThat(pvArray.get(), equalTo(CollectionNumbers.toListLong(0,1,3,2,4,5,6,7,8,9)));
    }

    public void testPutULongArray1() {
        PVDataCreate factory = PVDataFactory.getPVDataCreate();
        PVULongArray pvArray = (PVULongArray) factory.createPVScalarArray(ScalarType.pvULong);
        assertThat(pvArray.get(), instanceOf(ArrayULong.class));
        pvArray.put(0, CollectionNumbers.toListULong(0,1,2,3,4,5,6,7,8,9));
        assertThat(pvArray.get(), equalTo(CollectionNumbers.toListULong(0,1,2,3,4,5,6,7,8,9)));
        pvArray.put(2, CollectionNumbers.toListInt(3,2));
        assertThat(pvArray.get(), equalTo(CollectionNumbers.toListULong(0,1,3,2,4,5,6,7,8,9)));
    }

    public void testPutIntArray1() {
        PVDataCreate factory = PVDataFactory.getPVDataCreate();
        PVIntArray pvArray = (PVIntArray) factory.createPVScalarArray(ScalarType.pvInt);
        assertThat(pvArray.get(), instanceOf(ArrayInteger.class));
        pvArray.put(0, CollectionNumbers.toListInt(0,1,2,3,4,5,6,7,8,9));
        assertThat(pvArray.get(), equalTo(CollectionNumbers.toListInt(0,1,2,3,4,5,6,7,8,9)));
        pvArray.put(2, CollectionNumbers.toListLong(3,2));
        assertThat(pvArray.get(), equalTo(CollectionNumbers.toListInt(0,1,3,2,4,5,6,7,8,9)));
    }

    public void testPutUIntArray1() {
        PVDataCreate factory = PVDataFactory.getPVDataCreate();
        PVUIntArray pvArray = (PVUIntArray) factory.createPVScalarArray(ScalarType.pvUInt);
        assertThat(pvArray.get(), instanceOf(ArrayUInteger.class));
        pvArray.put(0, CollectionNumbers.toListUInt(0,1,2,3,4,5,6,7,8,9));
        assertThat(pvArray.get(), equalTo(CollectionNumbers.toListUInt(0,1,2,3,4,5,6,7,8,9)));
        pvArray.put(2, CollectionNumbers.toListLong(3,2));
        assertThat(pvArray.get(), equalTo(CollectionNumbers.toListUInt(0,1,3,2,4,5,6,7,8,9)));
    }

    public void testPutShortArray1() {
        PVDataCreate factory = PVDataFactory.getPVDataCreate();
        PVShortArray pvArray = (PVShortArray) factory.createPVScalarArray(ScalarType.pvShort);
        assertThat(pvArray.get(), instanceOf(ArrayShort.class));
        pvArray.put(0, CollectionNumbers.toListShort(new short[] {0,1,2,3,4,5,6,7,8,9}));
        assertThat(pvArray.get(), equalTo(CollectionNumbers.toListShort(new short[] {0,1,2,3,4,5,6,7,8,9})));
        pvArray.put(2, CollectionNumbers.toListInt(3,2));
        assertThat(pvArray.get(), equalTo(CollectionNumbers.toListShort(new short[] {0,1,3,2,4,5,6,7,8,9})));
    }

    public void testPutUShortArray1() {
        PVDataCreate factory = PVDataFactory.getPVDataCreate();
        PVUShortArray pvArray = (PVUShortArray) factory.createPVScalarArray(ScalarType.pvUShort);
        assertThat(pvArray.get(), instanceOf(ArrayUShort.class));
        pvArray.put(0, CollectionNumbers.toListUShort(new short[] {0,1,2,3,4,5,6,7,8,9}));
        assertThat(pvArray.get(), equalTo(CollectionNumbers.toListUShort(new short[] {0,1,2,3,4,5,6,7,8,9})));
        pvArray.put(2, CollectionNumbers.toListInt(3,2));
        assertThat(pvArray.get(), equalTo(CollectionNumbers.toListUShort(new short[] {0,1,3,2,4,5,6,7,8,9})));
    }

    public void testPutByteArray1() {
        PVDataCreate factory = PVDataFactory.getPVDataCreate();
        PVByteArray pvArray = (PVByteArray) factory.createPVScalarArray(ScalarType.pvByte);
        assertThat(pvArray.get(), instanceOf(ArrayByte.class));
        pvArray.put(0, CollectionNumbers.toListByte(new byte[] {0,1,2,3,4,5,6,7,8,9}));
        assertThat(pvArray.get(), equalTo(CollectionNumbers.toListByte(new byte[] {0,1,2,3,4,5,6,7,8,9})));
        pvArray.put(2, CollectionNumbers.toListInt(3,2));
        assertThat(pvArray.get(), equalTo(CollectionNumbers.toListByte(new byte[] {0,1,3,2,4,5,6,7,8,9})));
    }

    public void testPutUByteArray1() {
        PVDataCreate factory = PVDataFactory.getPVDataCreate();
        PVUByteArray pvArray = (PVUByteArray) factory.createPVScalarArray(ScalarType.pvUByte);
        assertThat(pvArray.get(), instanceOf(ArrayUByte.class));
        pvArray.put(0, CollectionNumbers.toListUByte(new byte[] {0,1,2,3,4,5,6,7,8,9}));
        assertThat(pvArray.get(), equalTo(CollectionNumbers.toListUByte(new byte[] {0,1,2,3,4,5,6,7,8,9})));
        pvArray.put(2, CollectionNumbers.toListInt(3,2));
        assertThat(pvArray.get(), equalTo(CollectionNumbers.toListUByte(new byte[] {0,1,3,2,4,5,6,7,8,9})));
    }

    public void testPutNumericArray1() {
        PVDataCreate factory = PVDataFactory.getPVDataCreate();
        PVNumberArray pvArray = (PVNumberArray) factory.createPVScalarArray(ScalarType.pvInt);
        assertThat(pvArray.get(), instanceOf(ArrayInteger.class));
        pvArray.put(0, CollectionNumbers.toListInt(0,1,2,3,4,5,6,7,8,9));
        assertThat(pvArray.get(), equalTo((ListNumber) CollectionNumbers.toListInt(0,1,2,3,4,5,6,7,8,9)));
        pvArray.put(2, CollectionNumbers.toListInt(3,2));
        assertThat(pvArray.get(), equalTo((ListNumber) CollectionNumbers.toListInt(0,1,3,2,4,5,6,7,8,9)));
    }
}
