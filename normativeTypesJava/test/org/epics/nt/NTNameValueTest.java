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
import org.epics.pvdata.pv.PVULongArray;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStringArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.PVScalar;
import org.epics.pvdata.pv.PVScalarArray;
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.property.*;

/**
 * JUnit test for NTNameValue.
 * @author dgh
 *
 */
public class NTNameValueTest extends NTTestBase
{
    // Test creation of NTNameValueBuilder

    public static void testCreateBuilder()
	{
        NTNameValueBuilder builder1 = NTNameValue.createBuilder();
		assertNotNull(builder1);

        NTNameValueBuilder builder2 = NTNameValue.createBuilder();		
		assertNotSame(builder1, builder2);
	}

    // Test NTNameValues created with Builder

    public static void testNTNameValue_BuilderCreated1()
    {
        testNTNameValue_BuilderCreatedImpl(PVDoubleArray.class, ScalarType.pvDouble);
    }

    public static void testNTNameValue_BuilderCreated2()
    {
        testNTNameValue_BuilderCreatedImpl(PVStringArray.class, ScalarType.pvString,
            new String[] {"timeStamp"});
    }

    public static void testNTNameValue_BuilderCreated3()
    {
        testNTNameValue_BuilderCreatedImpl(PVIntArray.class, ScalarType.pvInt,
            new String[] {"descriptor", "alarm", "timeStamp"} );
    }

    public static void testNTNameValue_BuilderCreated4()
    {
        testNTNameValue_BuilderCreatedImpl(PVDoubleArray.class, ScalarType.pvDouble,
            new String[] {"descriptor", "alarm", "timeStamp"} );
    }

    public static void testNTNameValue_BuilderCreated5()
    {
        testNTNameValue_BuilderCreatedImpl(PVULongArray.class, ScalarType.pvULong,
            new String[] {"descriptor", "timeStamp"},
            new String[] {"extra1"}, 
            new Field[]  { fieldCreate.createScalar(ScalarType.pvDouble)});
    }


    // Test is_a

    public static void testNTNameValueIs_a()
    {
        Structure s = NTNameValue.createBuilder().value(ScalarType.pvDouble).createStructure();
		assertTrue(NTNameValue.is_a(s));
    }

    public static void testStructureIs_a()
    {
        testStructureIs_aImpl(NTNameValue.URI, true);
        testStructureIs_aImpl("epics:nt/NTNameValue:1.0", true);
        testStructureIs_aImpl("epics:nt/NTNameValue:1.1", true);
        testStructureIs_aImpl("epics:nt/NTNameValue:2.0", false);
        testStructureIs_aImpl("epics:nt/NTNameValue", false);
        testStructureIs_aImpl("nt/NTNameValue:1.0", false);
        testStructureIs_aImpl("NTNameValue:1.0", false);
        testStructureIs_aImpl("NTNameValue", false);
        testStructureIs_aImpl("epics:nt/NTScalar:1.0", false);
    }

    // test compatibility - null structures

    public static void testStructureIsCompatibleNull()
    {
        PVStructure pvs = null;
        assertFalse(NTNameValue.isCompatible(pvs));
    }

    // test compatibility - compatible structures

    public static void testStructureIsCompatible1a()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTNameValue.URI).
                addArray("name", ScalarType.pvString).
                addArray("value", ScalarType.pvDouble).
                createStructure(),
            true);  
    }

    public static void testStructureIsCompatible1b()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTNameValue.URI).
                addArray("name", ScalarType.pvString).
                addArray("value", ScalarType.pvDouble).
                add("extra", ScalarType.pvString).
                createStructure(),
            true);
    }

    public static void testStructureIsCompatible1c()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTNameValue.URI).
                addArray("name", ScalarType.pvString).
                addArray("value", ScalarType.pvUByte).
                add("descriptor", ScalarType.pvString).
                add("alarm", ntField.createAlarm()).
                add("timeStamp", ntField.createTimeStamp()).
                add("display", ntField.createDisplay()).
                add("control", ntField.createControl()).
                addArray("extra", ScalarType.pvString).
                createStructure(),
            true);
    }

    // test compatibility - incompatible structures

    public static void testStructureIsCompatible2a()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTNameValue.URI).
                addArray("name", ScalarType.pvString).
                addArray("Value", ScalarType.pvDouble).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2b()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTNameValue.URI).
                addArray("name", ScalarType.pvString).
                add("value", ScalarType.pvDouble).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2c()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTNameValue.URI).
                addArray("name", ScalarType.pvString).
                addArray("value", ntField.createTimeStamp()).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2d1()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTNameValue.URI).
                addArray("name", ScalarType.pvString).
                addArray("value", ScalarType.pvDouble).
                add("descriptor", ScalarType.pvInt).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2d2()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTNameValue.URI).
                addArray("name", ScalarType.pvString).
                addArray("value", ScalarType.pvDouble).
                add("alarm", ScalarType.pvInt).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2d3()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTNameValue.URI).
                addArray("name", ScalarType.pvString).
                addArray("value", ScalarType.pvDouble).
                add("timeStamp", ScalarType.pvInt).
                createStructure(),
            false);
    }

    // test wrapping compatible structures

    public static void testWrappedNTNameValue1()
    {
        Structure s = fieldCreate.createFieldBuilder().
            setId(NTNameValue.URI).
            addArray("name", ScalarType.pvString).
            addArray("value", ScalarType.pvDouble).
            createStructure();

        NTNameValue ntnameValue = NTNameValue.wrap(dataCreate.createPVStructure(s));

        ntScalarChecks(ntnameValue, PVDoubleArray.class,
            ScalarType.pvDouble, new String[0],
            new String[0], new Field[0]);

        NTNameValue ntnameValue2 = NTNameValue.wrapUnsafe(dataCreate.
            createPVStructure(s));

        ntScalarChecks(ntnameValue2, PVDoubleArray.class,
            ScalarType.pvDouble, new String[0],
            new String[0], new Field[0]);
    }


    public static void testWrappedNTNameValue2()
    {
        Structure s = fieldCreate.createFieldBuilder().
            setId(NTNameValue.URI).
            addArray("name", ScalarType.pvString).
            addArray("value", ScalarType.pvString).
            add("descriptor", ScalarType.pvString).
            add("alarm", ntField.createAlarm()).
            add("timeStamp", ntField.createTimeStamp()).
            addArray("extra", ScalarType.pvString).
            createStructure();

        String[] standardFields = { "descriptor", "alarm", "control",
            "display", "timeStamp" };
        String[] extraNames = { "extra" };
        Field[] extraFields = { fieldCreate.createScalarArray(ScalarType.pvString) };

        NTNameValue ntnameValue = NTNameValue.wrap(dataCreate.createPVStructure(s));

        ntScalarChecks(ntnameValue, PVStringArray.class,
            ScalarType.pvString, standardFields,
            extraNames,extraFields);

        NTNameValue ntnameValue2 = NTNameValue.wrapUnsafe(dataCreate.
            createPVStructure(s));

        ntScalarChecks(ntnameValue, PVStringArray.class,
            ScalarType.pvString, standardFields,
            extraNames,extraFields);
    }

    // test is valid
    public static void testStructureIsValid()
    {
        NTNameValue ntnameValue = NTNameValue.createBuilder().
            value(ScalarType.pvDouble).create();

        PVDoubleArray pvValue = ntnameValue.getValue(PVDoubleArray.class);
        PVStringArray pvName = ntnameValue.getName();

        double[] vals = { 1.0, 2.0, 3.0 };
        String[] names = { "a", "b", "c", };

        assertTrue(ntnameValue.isValid());

        pvValue.put(0, vals.length, vals, 0);
        pvName.put(0, names.length, names, 0);

        assertTrue(ntnameValue.isValid());

        pvName.setLength(2); 
        assertFalse(ntnameValue.isValid());

        pvValue.setLength(1); 
        assertFalse(ntnameValue.isValid());

        pvName.setLength(0); 
        assertFalse(ntnameValue.isValid());

        pvValue.setLength(0); 
        assertTrue(ntnameValue.isValid());
    }

    // test attaching timeStamps

    public static void testTimeStamp1()
    {
        NTNameValue ntnameValue = NTNameValue.createBuilder().
            value(ScalarType.pvDouble).
            addTimeStamp().create();

        testAttachTimeStamp(ntnameValue, true);
    }
 
    public static void testTimeStamp2()
    {
        NTNameValue ntnameValue = NTNameValue.createBuilder().
            value(ScalarType.pvDouble).create();

        testAttachTimeStamp(ntnameValue, false);
    }

    // test attaching alarms

    public static void testAlarm1()
    {
        NTNameValue ntnameValue = NTNameValue.createBuilder().
            value(ScalarType.pvDouble).
            addAlarm().create();

        testAttachAlarm(ntnameValue, true);
    }

    public static void testAlarm2()
    {
        NTNameValue ntnameValue = NTNameValue.createBuilder().
            value(ScalarType.pvDouble).create();

        testAttachAlarm(ntnameValue, false);
    }

    public static void testBuilderResets()
    {
        NTNameValueBuilder builder = NTNameValue.createBuilder();

        Structure s1 = builder.
            value(ScalarType.pvString).
            createStructure();

        Structure s2 = builder.
            value(ScalarType.pvDouble).
            addDescriptor().
            addTimeStamp().
            addAlarm().
            add("extra", fieldCreate.createScalar(ScalarType.pvInt)).
            createStructure();

        Structure s3 = builder.
            value(ScalarType.pvString).
            createStructure();

        Structure s4 = builder.
            value(ScalarType.pvDouble).
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
        assertEquals(expected, NTNameValue.is_a(s));
    }



    public static void testStructureIsCompatibleImpl(Structure s, boolean expected)
    {
        PVStructure pvSt = dataCreate.createPVStructure(s);
        assertEquals(expected, NTNameValue.isCompatible(pvSt));
    }

    private static <T extends PVScalarArray>
    void testNTNameValue_BuilderCreatedImpl(Class<T> c, ScalarType scalarType)
    {
        testNTNameValue_BuilderCreatedImpl(c, scalarType, new String[0], new String[0], new Field[0]);
    }

    private static <T extends PVScalarArray>
    void testNTNameValue_BuilderCreatedImpl(Class<T> c, ScalarType scalarType, String[] standardFields)
    {
        testNTNameValue_BuilderCreatedImpl(c, scalarType, standardFields, new String[0], new Field[0]);
    }


    private static <T extends PVScalarArray>
    void testNTNameValue_BuilderCreatedImpl(Class<T> c, ScalarType scalarType, String[] standardFields, String[] extraNames, Field[] extraFields)
    {
        NTNameValue ntnameValue = createNTNameValue(c,scalarType,
            standardFields,extraNames,extraFields);

        ntScalarChecks(ntnameValue,c,scalarType,
            standardFields,extraNames,extraFields);        
    }


    private static <T extends PVScalarArray>
    NTNameValue createNTNameValue(Class<T> c, ScalarType scalarType, String[] standardFields, String[] extraNames, Field[] extraFields)
    {
        boolean hasDescriptor = find("descriptor", standardFields);
        boolean hasTimeStamp  = find("timeStamp", standardFields);
        boolean hasAlarm      = find("alarm", standardFields);

        // Create NTNameValue
        NTNameValueBuilder builder = NTNameValue.createBuilder().value(scalarType);

        if (hasDescriptor) builder.addDescriptor();
        if (hasTimeStamp) builder.addTimeStamp();
        if (hasAlarm) builder.addAlarm();

        for (int i = 0; i < extraNames.length; ++i)
            builder.add(extraNames[i], extraFields[i]);

        return builder.create();
    }

    private static <T extends PVScalarArray>
    void ntScalarChecks(NTNameValue ntnameValue, Class<T> c,
        ScalarType scalarType, String[] standardFields,
        String[] extraNames, Field[] extraFields)
    {
        // parse optional fields
        boolean hasDescriptor = find("descriptor", standardFields);
        boolean hasTimeStamp  = find("timeStamp", standardFields);
        boolean hasAlarm      = find("alarm", standardFields);

        // Test value field through NTNameValue interface
        PVScalarArray pvValue = ntnameValue.getValue();
		assertNotNull(pvValue);
        T pvValue2 = (T)pvValue;
		assertNotNull(pvValue2);
        T pvValue3 = ntnameValue.getValue(c);
		assertNotNull(pvValue3);

        PVStringArray pvName = ntnameValue.getName();
		assertNotNull(pvName);

		// Test optional fields through NTNameValue interface
        NTField ntField = NTField.get();

        PVString pvDescriptor = ntnameValue.getDescriptor();
        if (hasDescriptor)
        {
            assertNotNull(pvDescriptor);
        }
        else
            assertNull(pvDescriptor);

        PVStructure pvTimeStamp = ntnameValue.getTimeStamp();
        if (hasTimeStamp)
        {
            assertNotNull(pvTimeStamp);
            assertTrue(ntField.isTimeStamp(pvTimeStamp.getField()));
        }
        else
            assertNull(pvTimeStamp);

        PVStructure pvAlarm = ntnameValue.getAlarm();
        if (hasAlarm)
        {
            assertNotNull(pvAlarm);
            assertTrue(ntField.isAlarm(pvAlarm.getField()));
        }
        else
            assertNull(pvAlarm);


        // Test PVStructure from NTNameValue
        PVStructure pvStructure = ntnameValue.getPVStructure();
        assertTrue(NTNameValue.is_a(pvStructure.getStructure()));
        assertTrue(NTNameValue.isCompatible(pvStructure));

        assertSame(pvName, pvStructure.getSubField(PVStringArray.class, "name"));


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

