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
import org.epics.pvdata.pv.PVInt;
import org.epics.pvdata.pv.PVLong;
import org.epics.pvdata.pv.PVULong;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.PVScalar;
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.property.*;

/**
 * JUnit test for NTEnum.
 * @author dgh
 *
 */
public class NTEnumTest extends NTTestBase
{
    // Test creation of NTEnumBuilder

    public static void testCreateBuilder()
	{
        NTEnumBuilder builder1 = NTEnum.createBuilder();
		assertNotNull(builder1);

        NTEnumBuilder builder2 = NTEnum.createBuilder();		
		assertNotSame(builder1, builder2);
	}

    // Test NTEnums created with Builder

    public static void testNTEnum_BuilderCreated1()
    {
        testNTEnum_BuilderCreatedImpl(new String[] {});
    }

    public static void testNTEnum_BuilderCreated2()
    {
        testNTEnum_BuilderCreatedImpl(new String[] {"timeStamp"});
    }

    public static void testNTEnum_BuilderCreated3()
    {
        testNTEnum_BuilderCreatedImpl(new String[] {"descriptor", "alarm", "timeStamp"} );
    }

    public static void testNTEnum_BuilderCreated4()
    {
        testNTEnum_BuilderCreatedImpl(new String[] {"descriptor", "alarm", "timeStamp"} );
    }

    public static void testNTEnum_BuilderCreated5()
    {
        testNTEnum_BuilderCreatedImpl(
            new String[] {"descriptor", "timeStamp"},
            new String[] {"extra1"}, 
            new Field[]  { fieldCreate.createScalar(ScalarType.pvDouble)});
    }


    // Test is_a

    public static void testNTEnumIs_a()
    {
        Structure s = NTEnum.createBuilder().createStructure();
		assertTrue(NTEnum.is_a(s));
    }

    public static void testStructureIs_a()
    {
        testStructureIs_aImpl(NTEnum.URI, true);
        testStructureIs_aImpl("epics:nt/NTEnum:1.0", true);
        testStructureIs_aImpl("epics:nt/NTEnum:1.1", true);
        testStructureIs_aImpl("epics:nt/NTEnum:2.0", false);
        testStructureIs_aImpl("epics:nt/NTEnum", false);
        testStructureIs_aImpl("nt/NTEnum:1.0", false);
        testStructureIs_aImpl("NTEnum:1.0", false);
        testStructureIs_aImpl("NTEnum", false);
        testStructureIs_aImpl("epics:nt/NTEnumArray:1.0", false);
    }

    // test compatibility - null structures

    public static void testStructureIsCompatibleNull()
    {
        PVStructure pvs = null;
        assertFalse(NTEnum.isCompatible(pvs));
    }

    // test compatibility - compatible structures

    public static void testStructureIsCompatible1a()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTEnum.URI).
                add("value", ntField.createEnumerated()).
                createStructure(),
            true);  
    }

    public static void testStructureIsCompatible1b()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTEnum.URI).
                add("value", ntField.createEnumerated()).
                add("extra", ScalarType.pvString).
                createStructure(),
            true);
    }

    public static void testStructureIsCompatible1c()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTEnum.URI).
                add("value", ntField.createEnumerated()).
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
                setId(NTEnum.URI).
                add("Value", ntField.createEnumerated()).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2b()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTEnum.URI).
                addArray("value", ntField.createEnumerated()).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2c()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTEnum.URI).
                add("value", ntField.createTimeStamp()).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2d1()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTEnum.URI).
                add("value", ntField.createEnumerated()).
                add("descriptor", ScalarType.pvInt).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2d2()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTEnum.URI).
                add("value", ntField.createEnumerated()).
                add("alarm", ScalarType.pvInt).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2d3()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTEnum.URI).
                add("value", ntField.createEnumerated()).
                add("timeStamp", ScalarType.pvInt).
                createStructure(),
            false);
    }

    // test wrapping compatible structures

    public static void testWrappedNTEnum1()
    {
        Structure s = fieldCreate.createFieldBuilder().
            setId(NTEnum.URI).
            add("value", ntField.createEnumerated()).
            createStructure();

        NTEnum ntenum = NTEnum.wrap(dataCreate.createPVStructure(s));

        ntEnumChecks(ntenum, new String[0],
            new String[0], new Field[0]);

        NTEnum ntenum2 = NTEnum.wrapUnsafe(dataCreate.
            createPVStructure(s));

        ntEnumChecks(ntenum2, new String[0],
            new String[0], new Field[0]);
    }


    public static void testWrappedNTEnum2()
    {
        Structure s = fieldCreate.createFieldBuilder().
            setId(NTEnum.URI).
            add("value", ntField.createEnumerated()).
            add("descriptor", ScalarType.pvString).
            add("alarm", ntField.createAlarm()).
            add("timeStamp", ntField.createTimeStamp()).
            addArray("extra", ScalarType.pvString).
            createStructure();

        String[] standardFields = { "descriptor", "alarm", "timeStamp" };
        String[] extraNames = { "extra" };
        Field[] extraFields = { fieldCreate.createScalarArray(ScalarType.pvString) };

        NTEnum ntenum = NTEnum.wrap(dataCreate.createPVStructure(s));

        ntEnumChecks(ntenum, 
            standardFields,
            extraNames,extraFields);

        NTEnum ntenum2 = NTEnum.wrapUnsafe(dataCreate.
            createPVStructure(s));

        ntEnumChecks(ntenum, 
            standardFields,
            extraNames,extraFields);
    }

    // test standard fields

    // test attaching timeStamps

    public static void testTimeStamp1()
    {
        NTEnum ntenum = NTEnum.createBuilder().
            addTimeStamp().create();

        testAttachTimeStamp(ntenum, true);
    }
 
    public static void testTimeStamp2()
    {
        NTEnum ntenum = NTEnum.createBuilder().create();

        testAttachTimeStamp(ntenum, false);
    }

    // test attaching alarms

    public static void testAlarm1()
    {
        NTEnum ntenum = NTEnum.createBuilder().
            addAlarm().create();

        testAttachAlarm(ntenum, true);
    }

    public static void testAlarm2()
    {
        NTEnum ntenum = NTEnum.createBuilder().create();

        testAttachAlarm(ntenum, false);
    }


    public static void testBuilderResets()
    {
        NTEnumBuilder builder = NTEnum.createBuilder();

        Structure s1 = builder.createStructure();

        Structure s2 = builder.
            addDescriptor().
            addTimeStamp().
            addAlarm().
            add("extra", fieldCreate.createScalar(ScalarType.pvInt)).
            createStructure();

        Structure s3 = builder.createStructure();

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
        assertEquals(expected, NTEnum.is_a(s));
    }

    public static void testStructureIsCompatibleImpl(Structure s, boolean expected)
    {
        PVStructure pvSt = dataCreate.createPVStructure(s);
        assertEquals(expected, NTEnum.isCompatible(pvSt));
    }

    private static
    void testNTEnum_BuilderCreatedImpl(String[] standardFields)
    {
        testNTEnum_BuilderCreatedImpl(standardFields, new String[0], new Field[0]);
    }


    private static
    void testNTEnum_BuilderCreatedImpl(String[] standardFields, String[] extraNames, Field[] extraFields)
    {
        NTEnum ntenum = createNTEnum(standardFields,extraNames,extraFields);

        ntEnumChecks(ntenum,standardFields,extraNames,extraFields);        
    }


    private static
    NTEnum createNTEnum(String[] standardFields, String[] extraNames, Field[] extraFields)
    {
        boolean hasDescriptor = find("descriptor", standardFields);
        boolean hasTimeStamp  = find("timeStamp", standardFields);
        boolean hasAlarm      = find("alarm", standardFields);

        // Create NTEnum
        NTEnumBuilder builder = NTEnum.createBuilder();

        if (hasDescriptor) builder.addDescriptor();
        if (hasTimeStamp) builder.addTimeStamp();
        if (hasAlarm) builder.addAlarm();

        for (int i = 0; i < extraNames.length; ++i)
            builder.add(extraNames[i], extraFields[i]);

        return builder.create();
    }

    private static
    void ntEnumChecks(NTEnum ntenum, String[] standardFields,
        String[] extraNames, Field[] extraFields)
    {
        // parse optional fields
        boolean hasDescriptor = find("descriptor", standardFields);
        boolean hasTimeStamp  = find("timeStamp", standardFields);
        boolean hasAlarm      = find("alarm", standardFields);

        // Test value field through NTEnum interface
        PVStructure pvValue = ntenum.getValue();
		assertNotNull(pvValue);

		// Test optional fields through NTEnum interface
        NTField ntField = NTField.get();

        PVString pvDescriptor = ntenum.getDescriptor();
        if (hasDescriptor)
        {
            assertNotNull(pvDescriptor);
        }
        else
            assertNull(pvDescriptor);

        PVStructure pvTimeStamp = ntenum.getTimeStamp();
        if (hasTimeStamp)
        {
            assertNotNull(pvTimeStamp);
            assertTrue(ntField.isTimeStamp(pvTimeStamp.getField()));
        }
        else
            assertNull(pvTimeStamp);

        PVStructure pvAlarm = ntenum.getAlarm();
        if (hasAlarm)
        {
            assertNotNull(pvAlarm);
            assertTrue(ntField.isAlarm(pvAlarm.getField()));
        }
        else
            assertNull(pvAlarm);

        // Test PVStructure from NTEnum
        PVStructure pvStructure = ntenum.getPVStructure();
        assertTrue(NTEnum.is_a(pvStructure.getStructure()));
        assertTrue(NTEnum.isCompatible(pvStructure));

        assertSame(pvValue, pvStructure.getSubField(PVStructure.class, "value"));
        assertSame(pvDescriptor,pvStructure.getSubField("descriptor"));
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

