/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
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
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStringArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.PVScalar;
import org.epics.pvdata.pv.PVScalarArray;
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.property.*;

/**
 * JUnit test for NTContinuum.
 * @author dgh
 *
 */
public class NTContinuumTest extends NTTestBase
{
    // Test creation of NTContinuumBuilder

    public static void testCreateBuilder()
	{
        NTContinuumBuilder builder1 = NTContinuum.createBuilder();
		assertNotNull(builder1);

        NTContinuumBuilder builder2 = NTContinuum.createBuilder();
		assertNotSame(builder1, builder2);
	}

    // Test NTContinuums created with Builder

    public static void testNTContinuum_BuilderCreated1()
    {
        testNTContinuum_BuilderCreatedImpl(new String[0]);
    }

    public static void testNTContinuum_BuilderCreated2()
    {
        testNTContinuum_BuilderCreatedImpl(new String[] {"alarm", "timeStamp"});
    }

    public static void testNTContinuum_BuilderCreated3()
    {
        testNTContinuum_BuilderCreatedImpl(
            new String[] {"descriptor", "alarm", "timeStamp"},
            new String[] {"extra1"},
            new Field[]  { fieldCreate.createScalar(ScalarType.pvDouble)});
    }


    // Test is_a

    public static void testNTContinuumIs_a()
    {
        Structure s = NTContinuum.createBuilder().createStructure();
		assertTrue(NTContinuum.is_a(s));
    }

    public static void testStructureIs_a()
    {
        testStructureIs_aImpl(NTContinuum.URI, true);
        testStructureIs_aImpl("epics:nt/NTContinuum:1.0", true);
        testStructureIs_aImpl("epics:nt/NTContinuum:1.1", true);
        testStructureIs_aImpl("epics:nt/NTContinuum:2.0", false);
        testStructureIs_aImpl("epics:nt/NTContinuum", false);
        testStructureIs_aImpl("nt/NTContinuum:1.0", false);
        testStructureIs_aImpl("NTContinuum:1.0", false);
        testStructureIs_aImpl("NTContinuum", false);
        testStructureIs_aImpl("epics:nt/NTScalarArray:1.0", false);
    }

    // test compatibility - null structures

    public static void testStructureIsCompatibleNull()
    {
        PVStructure pvs = null;
        assertFalse(NTContinuum.isCompatible(pvs));
    }

    // test compatibility - compatible structures

    public static void testStructureIsCompatible1a()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTContinuum.URI).
                addArray("base", ScalarType.pvDouble).
                addArray("value", ScalarType.pvDouble).
                addArray("units", ScalarType.pvString).
                createStructure(),
            true);
    }


    public static void testStructureIsCompatible1b()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTContinuum.URI).
                addArray("base", ScalarType.pvDouble).
                addArray("value", ScalarType.pvDouble).
                addArray("units", ScalarType.pvString).
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
                setId(NTContinuum.URI).
                addArray("base", ScalarType.pvDouble).
                addArray("Value", ScalarType.pvDouble).
                addArray("units", ScalarType.pvString).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2b()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTContinuum.URI).
                addArray("base", ScalarType.pvDouble).
                add("value", ScalarType.pvDouble).
                addArray("units", ScalarType.pvString).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2c()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTContinuum.URI).
                addArray("base", ScalarType.pvDouble).
                addArray("value", ScalarType.pvFloat).
                addArray("units", ScalarType.pvString).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2c2()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTContinuum.URI).
                addArray("base", ScalarType.pvInt).
                addArray("value", ScalarType.pvDouble).
                addArray("units", ScalarType.pvString).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2d1()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTContinuum.URI).
                addArray("base", ScalarType.pvDouble).
                addArray("value", ScalarType.pvDouble).
                addArray("units", ScalarType.pvString).
                add("descriptor", ScalarType.pvInt).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2d2()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTContinuum.URI).
                addArray("base", ScalarType.pvDouble).
                addArray("value", ScalarType.pvDouble).
                addArray("units", ScalarType.pvString).
                add("alarm", ScalarType.pvInt).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2d3()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTContinuum.URI).
                addArray("base", ScalarType.pvDouble).
                addArray("value", ScalarType.pvDouble).
                addArray("units", ScalarType.pvString).
                add("timeStamp", ScalarType.pvInt).
                createStructure(),
            false);
    }

    // test wrapping compatible structures

    public static void testWrappedNTContinuum1()
    {
        Structure s = fieldCreate.createFieldBuilder().
            setId(NTContinuum.URI).
            addArray("base", ScalarType.pvDouble).
            addArray("value", ScalarType.pvDouble).
            addArray("units", ScalarType.pvString).
            createStructure();

        NTContinuum ntcontinuum = NTContinuum.wrap(dataCreate.createPVStructure(s));

        assertTrue(ntcontinuum!=null);

        ntcontinuumChecks(ntcontinuum, new String[0],
            new String[0], new Field[0]);

        NTContinuum ntcontinuum2 = NTContinuum.wrapUnsafe(dataCreate.
            createPVStructure(s));

        ntcontinuumChecks(ntcontinuum2, new String[0],
            new String[0], new Field[0]);
    }


    public static void testWrappedNTContinuum2()
    {
        Structure s = fieldCreate.createFieldBuilder().
            setId(NTContinuum.URI).
            addArray("base", ScalarType.pvDouble).
            addArray("value", ScalarType.pvDouble).
            addArray("units", ScalarType.pvString).
            add("descriptor", ScalarType.pvString).
            add("alarm", ntField.createAlarm()).
            add("timeStamp", ntField.createTimeStamp()).
            addArray("extra", ScalarType.pvString).
            createStructure();

        String[] standardFields = { "descriptor", "alarm",
            "timeStamp" };
        String[] extraNames = { "extra" };
        Field[] extraFields = { fieldCreate.createScalarArray(ScalarType.pvString) };

        NTContinuum ntcontinuum = NTContinuum.wrap(dataCreate.createPVStructure(s));

        ntcontinuumChecks(ntcontinuum, standardFields,
            extraNames,extraFields);

        NTContinuum ntcontinuum2 = NTContinuum.wrapUnsafe(dataCreate.
            createPVStructure(s));

        ntcontinuumChecks(ntcontinuum, standardFields,
            extraNames,extraFields);
    }

     // test isValid()  
    public static void testIsValid()
    {
        NTContinuum ntcontinuum = NTContinuum.createBuilder().create();

        PVDoubleArray pvValue = ntcontinuum.getValue();
        PVDoubleArray pvBase = ntcontinuum.getBase();
        PVStringArray pvUnits = ntcontinuum.getUnits();

        // valid sizes
        pvValue.setLength(3);
        pvBase.setLength(3);
        pvUnits.setLength(2);
        assertTrue(ntcontinuum.isValid());

        pvValue.setLength(3);
        pvBase.setLength(1);
        pvUnits.setLength(4);
        assertTrue(ntcontinuum.isValid());

        pvValue.setLength(12);
        pvBase.setLength(3);
        pvUnits.setLength(5);
        assertTrue(ntcontinuum.isValid());

        // invalid sizes
        pvValue.setLength(3);
        pvBase.setLength(2);
        pvUnits.setLength(2);
        assertFalse(ntcontinuum.isValid());

        pvValue.setLength(3);
        pvBase.setLength(4);
        pvUnits.setLength(2);
        assertFalse(ntcontinuum.isValid());

        pvValue.setLength(3);
        pvBase.setLength(2);
        pvUnits.setLength(2);
        assertFalse(ntcontinuum.isValid());

        pvValue.setLength(3);
        pvBase.setLength(1);
        pvUnits.setLength(3);
        assertFalse(ntcontinuum.isValid());

        pvValue.setLength(3);
        pvBase.setLength(1);
        pvUnits.setLength(5);
        assertFalse(ntcontinuum.isValid());
    }

    // test attaching timeStamps

    public static void testTimeStamp1()
    {
        NTContinuum ntcontinuum = NTContinuum.createBuilder().
            addTimeStamp().create();

        testAttachTimeStamp(ntcontinuum, true);
    }

    public static void testTimeStamp2()
    {
        NTContinuum ntcontinuum = NTContinuum.createBuilder().create();

        testAttachTimeStamp(ntcontinuum, false);
    }

    // test attaching alarms

    public static void testAlarm1()
    {
        NTContinuum ntcontinuum = NTContinuum.createBuilder().
            addAlarm().create();

        testAttachAlarm(ntcontinuum, true);
    }

    public static void testAlarm2()
    {
        NTContinuum ntcontinuum = NTContinuum.createBuilder().create();

        testAttachAlarm(ntcontinuum, false);
    }

    public static void testBuilderResets()
    {
        NTContinuumBuilder builder = NTContinuum.createBuilder();

        Structure s1 = builder.createStructure();

        Structure s2 = builder.
            addDescriptor().
            addTimeStamp().
            addAlarm().
            add("extra", fieldCreate.createScalar(ScalarType.pvInt)).
            createStructure();

        Structure s3 = builder.
            createStructure();

        Structure s4 = builder.
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
        assertEquals(expected, NTContinuum.is_a(s));
    }

    public static void testStructureIsCompatibleImpl(Structure s, boolean expected)
    {
        PVStructure pvSt = dataCreate.createPVStructure(s);
        assertEquals(expected, NTContinuum.isCompatible(pvSt));
    }

    private static void testNTContinuum_BuilderCreatedImpl(String[] standardFields)
    {
        testNTContinuum_BuilderCreatedImpl(standardFields, new String[0], new Field[0]);
    }


    private static void testNTContinuum_BuilderCreatedImpl(String[] standardFields, String[] extraNames, Field[] extraFields)
    {
        NTContinuum ntcontinuum = createNTContinuum(standardFields, extraNames, extraFields);

        ntcontinuumChecks(ntcontinuum, standardFields, extraNames, extraFields);
    }


    private static NTContinuum createNTContinuum(String[] standardFields,
        String[] extraNames, Field[] extraFields)
    {
        boolean hasDescriptor = find("descriptor", standardFields);
        boolean hasTimeStamp  = find("timeStamp", standardFields);
        boolean hasAlarm      = find("alarm", standardFields);

        // Create NTContinuum
        NTContinuumBuilder builder = NTContinuum.createBuilder();

        if (hasDescriptor) builder.addDescriptor();
        if (hasTimeStamp) builder.addTimeStamp();
        if (hasAlarm) builder.addAlarm();

        for (int i = 0; i < extraNames.length; ++i)
            builder.add(extraNames[i], extraFields[i]);

        return builder.create();
    }

    private static void ntcontinuumChecks(NTContinuum ntcontinuum,
        String[] standardFields, String[] extraNames, Field[] extraFields)
    {
        // parse optional fields
        boolean hasDescriptor = find("descriptor", standardFields);
        boolean hasTimeStamp  = find("timeStamp", standardFields);
        boolean hasAlarm      = find("alarm", standardFields);

        // Test required fields through NTContinuum interface
        PVDoubleArray pvValue = ntcontinuum.getValue();
		assertNotNull(pvValue);

        PVDoubleArray pvBase = ntcontinuum.getBase();
		assertNotNull(pvBase);

        PVStringArray pvUnits = ntcontinuum.getUnits();
		assertNotNull(pvUnits);

		// Test optional fields through NTContinuum interface

        PVString pvDescriptor = ntcontinuum.getDescriptor();
        if (hasDescriptor)
        {
            assertNotNull(pvDescriptor);
        }
        else
            assertNull(pvDescriptor);

        PVStructure pvTimeStamp = ntcontinuum.getTimeStamp();
        if (hasTimeStamp)
        {
            assertNotNull(pvTimeStamp);
            assertTrue(ntField.isTimeStamp(pvTimeStamp.getField()));
        }
        else
            assertNull(pvTimeStamp);

        PVStructure pvAlarm = ntcontinuum.getAlarm();
        if (hasAlarm)
        {
            assertNotNull(pvAlarm);
            assertTrue(ntField.isAlarm(pvAlarm.getField()));
        }
        else
            assertNull(pvAlarm);

        // Test PVStructure from NTContinuum
        PVStructure pvStructure = ntcontinuum.getPVStructure();
        assertTrue(NTContinuum.is_a(pvStructure.getStructure()));
        assertTrue(NTContinuum.isCompatible(pvStructure));

        assertSame(pvValue, pvStructure.getSubField(PVDoubleArray.class, "value"));
        assertSame(pvBase, pvStructure.getSubField(PVDoubleArray.class, "base"));
        assertSame(pvUnits, pvStructure.getSubField(PVStringArray.class, "units"));

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

