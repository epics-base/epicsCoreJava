/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.nt;

import junit.framework.TestCase;

import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.FieldBuilder;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.Scalar;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Structure;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVDouble;
import org.epics.pvdata.pv.PVDoubleArray;
import org.epics.pvdata.pv.PVInt;
import org.epics.pvdata.pv.PVIntArray;
import org.epics.pvdata.pv.PVLong;
import org.epics.pvdata.pv.PVLongArray;
import org.epics.pvdata.pv.PVShortArray;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStringArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.PVScalar;
import org.epics.pvdata.pv.PVScalarArray;
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.property.*;

/**
 * JUnit test for NTHistogram.
 * @author dgh
 *
 */
public class NTHistogramTest extends NTTestBase
{
    // Test creation of NTHistogramBuilder

    public static void testCreateBuilder()
	{
        NTHistogramBuilder builder1 = NTHistogram.createBuilder();
		assertNotNull(builder1);

        NTHistogramBuilder builder2 = NTHistogram.createBuilder();		
		assertNotSame(builder1, builder2);
	}

    // Test NTHistograms created with Builder

    public static void testNTHistogram_BuilderCreated1()
    {
        testNTHistogram_BuilderCreatedImpl(ScalarType.pvShort);
    }

    public static void testNTHistogram_BuilderCreated2()
    {
        testNTHistogram_BuilderCreatedImpl(ScalarType.pvInt, new String[] {"alarm", "timeStamp"});
    }

    public static void testNTHistogram_BuilderCreated3()
    {
        testNTHistogram_BuilderCreatedImpl(ScalarType.pvLong,
            new String[] { "descriptor", "alarm", "timeStamp"},
            new String[] {"extra1"}, 
            new Field[]  { fieldCreate.createScalar(ScalarType.pvDouble)});
    }


    // Test is_a

    public static void testNTHistogramIs_a()
    {
        Structure s = NTHistogram.createBuilder().
            value(ScalarType.pvLong).createStructure();
		assertTrue(NTHistogram.is_a(s));
    }

    public static void testStructureIs_a()
    {
        testStructureIs_aImpl(NTHistogram.URI, true);
        testStructureIs_aImpl("epics:nt/NTHistogram:1.0", true);
        testStructureIs_aImpl("epics:nt/NTHistogram:1.1", true);
        testStructureIs_aImpl("epics:nt/NTHistogram:2.0", false);
        testStructureIs_aImpl("epics:nt/NTHistogram", false);
        testStructureIs_aImpl("nt/NTHistogram:1.0", false);
        testStructureIs_aImpl("NTHistogram:1.0", false);
        testStructureIs_aImpl("NTHistogram", false);
        testStructureIs_aImpl("epics:nt/NTScalarArray:1.0", false);
    }

    // test compatibility - null structures

    public static void testStructureIsCompatibleNull()
    {
        PVStructure pvs = null;
        assertFalse(NTHistogram.isCompatible(pvs));
    }

    // test compatibility - compatible structures

    public static void testStructureIsCompatible1a1()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTHistogram.URI).
                addArray("ranges", ScalarType.pvDouble).
                addArray("value", ScalarType.pvInt).
                createStructure(),
            true);  
    }


    public static void testStructureIsCompatible1a2()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTHistogram.URI).
                addArray("ranges", ScalarType.pvDouble).
                addArray("value", ScalarType.pvShort).
                createStructure(),
            true);  
    }


    public static void testStructureIsCompatible1b()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTHistogram.URI).
                addArray("ranges", ScalarType.pvDouble).
                addArray("value", ScalarType.pvLong).
                add("descriptor", ScalarType.pvString).
                add("alarm", ntField.createAlarm()).
                add("timeStamp", ntField.createTimeStamp()).
                addArray("extra", ScalarType.pvString).
                createStructure(),
            true);
    }

    // test compatibility - incompatible structures

    public static void testStructureIsCompatible2a()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTHistogram.URI).
                addArray("ranges", ScalarType.pvDouble).
                addArray("Value", ScalarType.pvShort).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2b()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTHistogram.URI).
                addArray("Ranges", ScalarType.pvDouble).
                add("value", ScalarType.pvShort).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2c()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTHistogram.URI).
                addArray("ranges", ScalarType.pvDouble).
                addArray("value", ScalarType.pvUInt).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2d1()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTHistogram.URI).
                addArray("ranges", ScalarType.pvDouble).
                addArray("value", ScalarType.pvInt).
                add("descriptor", ScalarType.pvInt).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2d2()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTHistogram.URI).
                addArray("ranges", ScalarType.pvDouble).
                addArray("value", ScalarType.pvInt).
                add("alarm", ScalarType.pvInt).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2d3()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTHistogram.URI).
                addArray("ranges", ScalarType.pvDouble).
                addArray("value", ScalarType.pvInt).
                addArray("units", ScalarType.pvString).
                add("timeStamp", ScalarType.pvInt).
                createStructure(),
            false);
    }

    // test wrapping compatible structures

    public static void testWrappedNTHistogram1()
    {
        Structure s = fieldCreate.createFieldBuilder().
            setId(NTHistogram.URI).
            addArray("ranges", ScalarType.pvDouble).
            addArray("value", ScalarType.pvInt).
            createStructure();

        NTHistogram nthistogram = NTHistogram.wrap(dataCreate.createPVStructure(s));

        assertTrue(nthistogram!=null);

        nthistogramChecks(nthistogram, PVIntArray.class, new String[0],
            new String[0], new Field[0]);

        NTHistogram nthistogram2 = NTHistogram.wrapUnsafe(dataCreate.
            createPVStructure(s));

        nthistogramChecks(nthistogram2, PVIntArray.class, new String[0],
            new String[0], new Field[0]);
    }


    public static void testWrappedNTHistogram2()
    {
        Structure s = fieldCreate.createFieldBuilder().
            setId(NTHistogram.URI).
            addArray("ranges", ScalarType.pvDouble).
            addArray("value", ScalarType.pvLong).
            add("descriptor", ScalarType.pvString).
            add("alarm", ntField.createAlarm()).
            add("timeStamp", ntField.createTimeStamp()).
            addArray("extra", ScalarType.pvString).
            createStructure();

        String[] standardFields = { "descriptor", "alarm",
            "timeStamp" };
        String[] extraNames = { "extra" };
        Field[] extraFields = { fieldCreate.createScalarArray(ScalarType.pvString) };

        NTHistogram nthistogram = NTHistogram.wrap(dataCreate.createPVStructure(s));

        nthistogramChecks(nthistogram, PVLongArray.class, standardFields,
            extraNames,extraFields);

        NTHistogram nthistogram2 = NTHistogram.wrapUnsafe(dataCreate.
            createPVStructure(s));

        nthistogramChecks(nthistogram, PVLongArray.class, standardFields,
            extraNames,extraFields);
    }

     // test isValid()  
    public static void testIsValid()
    {
        NTHistogram nthistogram = NTHistogram.createBuilder().
            value(ScalarType.pvInt).create();

        PVDoubleArray pvRanges = nthistogram.getRanges();
        PVIntArray pvValue = nthistogram.getValue(PVIntArray.class);

        assertFalse(nthistogram.isValid());

        double[] ranges = {-1.0, 1.0 };
        pvRanges.put(0, ranges.length, ranges, 0);
        assertFalse(nthistogram.isValid());

        int[] values = { 42 };
        pvValue.put(0, values.length, values, 0);
        assertTrue(nthistogram.isValid());

        values = new int[]{ 42, 24, 42 };
        pvValue.put(0, values.length, values, 0);
        assertFalse(nthistogram.isValid());

        ranges = new double[]{-2.0, -1.0, 1.0, 2.0 };
        pvRanges.put(0, ranges.length, ranges, 0);
        assertTrue(nthistogram.isValid());
    }



    public static void testTimeStamp1()
    {
        NTHistogram nthistogram = NTHistogram.createBuilder().
            value(ScalarType.pvInt).
            addTimeStamp().create();

        testAttachTimeStamp(nthistogram, true);
    }
 
    public static void testTimeStamp2()
    {
        NTHistogram nthistogram = NTHistogram.createBuilder().
            value(ScalarType.pvInt).create();

        testAttachTimeStamp(nthistogram, false);
    }

    // test attaching alarms

    public static void testAlarm1()
    {
        NTHistogram nthistogram = NTHistogram.createBuilder().
            value(ScalarType.pvInt).
            addAlarm().create();

        testAttachAlarm(nthistogram, true);
    }

    public static void testAlarm2()
    {
        NTHistogram nthistogram = NTHistogram.createBuilder().
            value(ScalarType.pvInt).create();

        testAttachAlarm(nthistogram, false);
    }

    public static void testBuilderResets()
    {
        NTHistogramBuilder builder = NTHistogram.createBuilder();

        Structure s1 = builder.value(ScalarType.pvShort).createStructure();

        Structure s2 = builder.
            value(ScalarType.pvLong).
            addDescriptor().
            addTimeStamp().
            addAlarm().
            add("extra", fieldCreate.createScalar(ScalarType.pvInt)).
            createStructure();

        Structure s3 = builder. value(ScalarType.pvShort).createStructure();

        Structure s4 = builder.
            value(ScalarType.pvLong).
            addDescriptor().
            addTimeStamp().
            addAlarm().
            add("extra", fieldCreate.createScalar(ScalarType.pvInt)).
            createStructure();

        assertEquals(s1.toString(),s3.toString());
        assertEquals(s2.toString(),s4.toString());
        assertFalse(s1.toString().equals(s2.toString()));
        assertFalse(s3.toString().equals(s4.toString()));
    }

    // Implementations of tests

    public static void testStructureIs_aImpl(String str, boolean expected)
    {
        FieldBuilder builder = fieldCreate.createFieldBuilder();
        Structure s = builder.setId(str).createStructure();
        assertEquals(expected, NTHistogram.is_a(s));
    }

    public static void testStructureIsCompatibleImpl(Structure s, boolean expected)
    {
        PVStructure pvSt = dataCreate.createPVStructure(s);
        assertEquals(expected, NTHistogram.isCompatible(pvSt));
    }

    private static void testNTHistogram_BuilderCreatedImpl(ScalarType scalarType)
    {
        testNTHistogram_BuilderCreatedImpl(scalarType, new String[0], new String[0], new Field[0]);
    }

    private static void testNTHistogram_BuilderCreatedImpl(ScalarType scalarType,
        String[] standardFields)
    {
        testNTHistogram_BuilderCreatedImpl(scalarType, standardFields, new String[0], new Field[0]);
    }


    private static void testNTHistogram_BuilderCreatedImpl(ScalarType scalarType,
        String[] standardFields, String[] extraNames, Field[] extraFields)
    {
        NTHistogram nthistogram = createNTHistogram(scalarType, standardFields, extraNames, extraFields);

        switch (scalarType)
        {
        case pvShort:
            nthistogramChecks(nthistogram, PVShortArray.class, standardFields,
                extraNames, extraFields);
            break;
        case pvInt:
            nthistogramChecks(nthistogram, PVIntArray.class, standardFields,
                extraNames, extraFields);
            break;
        case pvLong:
            nthistogramChecks(nthistogram, PVLongArray.class, standardFields,
                extraNames, extraFields);
            break;
        default:
            throw new RuntimeException("Illegal scalar type");
        }
    }


    private static NTHistogram createNTHistogram(ScalarType scalarType,
        String[] standardFields,
        String[] extraNames, Field[] extraFields)
    {
        boolean hasDescriptor = find("descriptor", standardFields);
        boolean hasTimeStamp  = find("timeStamp", standardFields);
        boolean hasAlarm      = find("alarm", standardFields);

        // Create NTHistogram
        NTHistogramBuilder builder = NTHistogram.createBuilder().
            value(scalarType);

        if (hasDescriptor) builder.addDescriptor();
        if (hasTimeStamp) builder.addTimeStamp();
        if (hasAlarm) builder.addAlarm();

        for (int i = 0; i < extraNames.length; ++i)
            builder.add(extraNames[i], extraFields[i]);

        return builder.create();
    }

    private static <T extends PVScalarArray>
    void nthistogramChecks(NTHistogram nthistogram, Class<T> c,
        String[] standardFields, String[] extraNames, Field[] extraFields)
    {
        // parse optional fields
        boolean hasDescriptor = find("descriptor", standardFields);
        boolean hasTimeStamp  = find("timeStamp", standardFields);
        boolean hasAlarm      = find("alarm", standardFields);

        // Test required fields through NTHistogram interface
        PVDoubleArray pvRanges = nthistogram.getRanges();
		assertNotNull(pvRanges);

        PVScalarArray pvValue = nthistogram.getValue();
		assertNotNull(pvValue);

        T pvValue2 = nthistogram.getValue(c);
		assertNotNull(pvValue2);

		// Test optional fields through NTHistogram interface

        PVString pvDescriptor = nthistogram.getDescriptor();
        if (hasDescriptor)
        {
            assertNotNull(pvDescriptor);
        }
        else
            assertNull(pvDescriptor);

        PVStructure pvTimeStamp = nthistogram.getTimeStamp();
        if (hasTimeStamp)
        {
            assertNotNull(pvTimeStamp);
            assertTrue(ntField.isTimeStamp(pvTimeStamp.getField()));
        }
        else
            assertNull(pvTimeStamp);

        PVStructure pvAlarm = nthistogram.getAlarm();
        if (hasAlarm)
        {
            assertNotNull(pvAlarm);
            assertTrue(ntField.isAlarm(pvAlarm.getField()));
        }
        else
            assertNull(pvAlarm);

        // Test PVStructure from NTHistogram
        PVStructure pvStructure = nthistogram.getPVStructure();
        assertTrue(NTHistogram.is_a(pvStructure.getStructure()));
        assertTrue(NTHistogram.isCompatible(pvStructure));

        assertSame(pvRanges, pvStructure.getSubField(PVScalarArray.class, "ranges"));
        assertSame(pvValue, pvStructure.getSubField(PVScalarArray.class, "value"));

        assertSame(pvValue2, pvStructure.getSubField(c, "value"));


        assertSame(pvDescriptor,pvStructure.getSubField(PVString.class, "descriptor"));
        assertSame(pvTimeStamp, pvStructure.getSubField(PVStructure.class, "timeStamp"));
        assertSame(pvAlarm, pvStructure.getSubField(PVStructure.class, "alarm"));

        for (int i = 0; i < extraNames.length; ++i)
        {
            PVField pvField = pvStructure.getSubField(extraNames[i]);
            assertNotNull(pvField);
            assertSame(pvField.getField(),extraFields[i]);
        }
    }
}

