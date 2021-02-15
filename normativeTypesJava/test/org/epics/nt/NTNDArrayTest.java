/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.nt;

import junit.framework.TestCase;

import org.epics.pvdata.pv.*;
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.property.*;

/**
 * JUnit test for NTNDArray.
 * @author dgh
 *
 */
public class NTNDArrayTest extends NTTestBase
{
    public static void test1()
    {
        assertTrue(NTNDArray.isCompatible(NTNDArray.createBuilder().createStructure()));
    }

    public static void test2()
    {
        assertTrue(NTNDArray.isCompatible(NTNDArray.createBuilder().createPVStructure()));
    }


    // test compatibility - null structures

    public static void testStructureIsCompatibleNull()
    {
        PVStructure pvs = null;
        assertFalse(NTNDArray.isCompatible(pvs));
    }

    public static void testIsValid1()
    {
        NTNDArray ntndarray = NTNDArray.createBuilder().create();

        assertTrue(ntndarray.isValid());

        int[] vals = { 0, 1, 2, 3, 4, 5 };
        ntndarray.getValue().select(PVIntArray.class,"intValue").
            put(0,vals.length,vals,0);

        assertFalse(ntndarray.isValid());

        ntndarray.getCompressedDataSize().put(24);
        assertTrue(ntndarray.isValid());

        ntndarray.getUncompressedDataSize().put(1);
        assertFalse(ntndarray.isValid());

        Structure dimStruc = ntndarray.getDimension().getStructureArray().
            getStructure();

        PVStructure pvDim1 = dataCreate.createPVStructure(dimStruc);
        PVStructure pvDim2 = dataCreate.createPVStructure(dimStruc);

        pvDim1.getSubField(PVInt.class, "size").put(3);
        pvDim2.getSubField(PVInt.class, "size").put(2);

        PVStructure[] dims = {pvDim1, pvDim2};
        ntndarray.getDimension().put(0,dims.length,dims,0);

        assertFalse(ntndarray.isValid());

        ntndarray.getUncompressedDataSize().put(24);
        assertTrue(ntndarray.isValid());

        pvDim1.getSubField(PVInt.class, "size").put(2);
        assertFalse(ntndarray.isValid());

        ntndarray.getUncompressedDataSize().put(16);
        assertTrue(ntndarray.isValid());
    }

    public static void testIsValid2()
    {
        testIsValid2Impl(PVByteArray.class, "byteValue", 1);
        testIsValid2Impl(PVShortArray.class, "shortValue", 2);
        testIsValid2Impl(PVIntArray.class, "intValue", 4);
        testIsValid2Impl(PVLongArray.class, "longValue", 8);

        testIsValid2Impl(PVUByteArray.class, "ubyteValue", 1);
        testIsValid2Impl(PVUShortArray.class, "ushortValue", 2);
        testIsValid2Impl(PVUIntArray.class, "uintValue", 4);
        testIsValid2Impl(PVULongArray.class, "ulongValue", 8);

        testIsValid2Impl(PVFloatArray.class, "floatValue", 4);
        testIsValid2Impl(PVDoubleArray.class, "doubleValue", 8);

        testIsValid2Impl(PVBooleanArray.class, "booleanValue", 1);
    }

    private static <T extends PVScalarArray>
    void testIsValid2Impl(Class<T> c, String valueName, int valueSize)
    {
        NTNDArray ntndarray = NTNDArray.createBuilder().create();

        assertTrue(ntndarray.isValid());


        ntndarray.getValue().select(c, valueName).
            setLength(6);

        assertFalse(ntndarray.isValid());

        ntndarray.getCompressedDataSize().put(6L *valueSize);
        assertTrue(ntndarray.isValid());

        ntndarray.getUncompressedDataSize().put(1L);
        assertFalse(ntndarray.isValid());

        Structure dimStruc = ntndarray.getDimension().getStructureArray().
            getStructure();

        PVStructure pvDim1 = dataCreate.createPVStructure(dimStruc);
        PVStructure pvDim2 = dataCreate.createPVStructure(dimStruc);

        pvDim1.getSubField(PVInt.class, "size").put(3);
        pvDim2.getSubField(PVInt.class, "size").put(2);

        PVStructure[] dims = {pvDim1, pvDim2};
        ntndarray.getDimension().put(0,dims.length,dims,0);

        assertFalse(ntndarray.isValid());

        ntndarray.getUncompressedDataSize().put(6L *valueSize);
        assertTrue(ntndarray.isValid());

        pvDim1.getSubField(PVInt.class, "size").put(2);
        assertFalse(ntndarray.isValid());

        ntndarray.getUncompressedDataSize().put(4L *valueSize);
        assertTrue(ntndarray.isValid());
    }


}

