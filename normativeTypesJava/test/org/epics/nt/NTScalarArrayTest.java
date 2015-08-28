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
 * JUnit test for NTScalarArray.
 * @author dgh
 *
 */
public class NTScalarArrayTest extends NTTestBase
{
    // Test creation of NTScalarArrayBuilder

    public static void testCreateBuilder()
	{
        NTScalarArrayBuilder builder1 = NTScalarArray.createBuilder();
		assertNotNull(builder1);

        NTScalarArrayBuilder builder2 = NTScalarArray.createBuilder();		
		assertNotSame(builder1, builder2);
	}

    // Test NTScalarArrays created with Builder

    public static void testNTScalarArray_BuilderCreated1()
    {
        testNTScalarArray_BuilderCreatedImpl(PVDoubleArray.class, ScalarType.pvDouble);
    }

    public static void testNTScalarArray_BuilderCreated2()
    {
        testNTScalarArray_BuilderCreatedImpl(PVStringArray.class, ScalarType.pvString,
            new String[] {"timeStamp"});
    }

    public static void testNTScalarArray_BuilderCreated3()
    {
        testNTScalarArray_BuilderCreatedImpl(PVIntArray.class, ScalarType.pvInt,
            new String[] {"descriptor", "alarm", "timeStamp"} );
    }

    public static void testNTScalarArray_BuilderCreated4()
    {
        testNTScalarArray_BuilderCreatedImpl(PVDoubleArray.class, ScalarType.pvDouble,
            new String[] {"descriptor", "alarm", "control", "display", "timeStamp"} );
    }

    public static void testNTScalarArray_BuilderCreated5()
    {
        testNTScalarArray_BuilderCreatedImpl(PVULongArray.class, ScalarType.pvULong,
            new String[] {"descriptor", "control", "display", "timeStamp"},
            new String[] {"extra1"}, 
            new Field[]  { fieldCreate.createScalar(ScalarType.pvDouble)});
    }


    // Test is_a

    public static void testNTScalarArrayIs_a()
    {
        Structure s = NTScalarArray.createBuilder().value(ScalarType.pvDouble).createStructure();
		assertTrue(NTScalarArray.is_a(s));
    }

    public static void testStructureIs_a()
    {
        testStructureIs_aImpl(NTScalarArray.URI, true);
        testStructureIs_aImpl("epics:nt/NTScalarArray:1.0", true);
        testStructureIs_aImpl("epics:nt/NTScalarArray:1.1", true);
        testStructureIs_aImpl("epics:nt/NTScalarArray:2.0", false);
        testStructureIs_aImpl("epics:nt/NTScalarArray", false);
        testStructureIs_aImpl("nt/NTScalarArray:1.0", false);
        testStructureIs_aImpl("NTScalarArray:1.0", false);
        testStructureIs_aImpl("NTScalarArray", false);
        testStructureIs_aImpl("epics:nt/NTScalar:1.0", false);
    }

    // test compatibility - null structures

    public static void testStructureIsCompatibleNull()
    {
        PVStructure pvs = null;
        assertFalse(NTScalarArray.isCompatible(pvs));
    }

    // test compatibility - compatible structures

    public static void testStructureIsCompatible1a()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTScalarArray.URI).
                addArray("value", ScalarType.pvDouble).
                createStructure(),
            true);  
    }

    public static void testStructureIsCompatible1b()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTScalarArray.URI).
                addArray("value", ScalarType.pvDouble).
                add("extra", ScalarType.pvString).
                createStructure(),
            true);
    }

    public static void testStructureIsCompatible1c()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTScalarArray.URI).
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
                setId(NTScalarArray.URI).
                addArray("Value", ScalarType.pvDouble).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2b()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTScalarArray.URI).
                add("value", ScalarType.pvDouble).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2c()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTScalarArray.URI).
                addArray("value", ntField.createTimeStamp()).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2d1()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTScalarArray.URI).
                addArray("value", ScalarType.pvDouble).
                add("descriptor", ScalarType.pvInt).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2d2()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTScalarArray.URI).
                addArray("value", ScalarType.pvDouble).
                add("alarm", ScalarType.pvInt).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2d3()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTScalarArray.URI).
                addArray("value", ScalarType.pvDouble).
                add("timeStamp", ScalarType.pvInt).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2d4()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTScalarArray.URI).
                addArray("value", ScalarType.pvDouble).
                add("display", ScalarType.pvInt).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2d5()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTScalarArray.URI).
                addArray("value", ScalarType.pvDouble).
                add("control", ScalarType.pvInt).
                createStructure(),
            false);
    }

    // test wrapping compatible structures

    public static void testWrappedNTScalarArray1()
    {
        Structure s = fieldCreate.createFieldBuilder().
            setId(NTScalarArray.URI).
            addArray("value", ScalarType.pvDouble).
            createStructure();

        NTScalarArray ntscalarArray = NTScalarArray.wrap(dataCreate.createPVStructure(s));

        ntScalarChecks(ntscalarArray, PVDoubleArray.class,
            ScalarType.pvDouble, new String[0],
            new String[0], new Field[0]);

        NTScalarArray ntscalarArray2 = NTScalarArray.wrapUnsafe(dataCreate.
            createPVStructure(s));

        ntScalarChecks(ntscalarArray2, PVDoubleArray.class,
            ScalarType.pvDouble, new String[0],
            new String[0], new Field[0]);
    }


    public static void testWrappedNTScalarArray2()
    {
        Structure s = fieldCreate.createFieldBuilder().
            setId(NTScalarArray.URI).
            addArray("value", ScalarType.pvString).
            add("descriptor", ScalarType.pvString).
            add("alarm", ntField.createAlarm()).
            add("timeStamp", ntField.createTimeStamp()).
            add("display", ntField.createDisplay()).
            add("control", ntField.createControl()).
            addArray("extra", ScalarType.pvString).
            createStructure();

        String[] standardFields = { "descriptor", "alarm", "control",
            "display", "timeStamp" };
        String[] extraNames = { "extra" };
        Field[] extraFields = { fieldCreate.createScalarArray(ScalarType.pvString) };

        NTScalarArray ntscalarArray = NTScalarArray.wrap(dataCreate.createPVStructure(s));

        ntScalarChecks(ntscalarArray, PVStringArray.class,
            ScalarType.pvString, standardFields,
            extraNames,extraFields);

        NTScalarArray ntscalarArray2 = NTScalarArray.wrapUnsafe(dataCreate.
            createPVStructure(s));

        ntScalarChecks(ntscalarArray, PVStringArray.class,
            ScalarType.pvString, standardFields,
            extraNames,extraFields);
    }

    // test attaching timeStamps

    public static void testTimeStamp1()
    {
        NTScalarArray ntscalarArray = NTScalarArray.createBuilder().
            value(ScalarType.pvDouble).
            addTimeStamp().create();

        testAttachTimeStamp(ntscalarArray, true);
    }
 
    public static void testTimeStamp2()
    {
        NTScalarArray ntscalarArray = NTScalarArray.createBuilder().
            value(ScalarType.pvDouble).create();

        testAttachTimeStamp(ntscalarArray, false);
    }

    // test attaching alarms

    public static void testAlarm1()
    {
        NTScalarArray ntscalarArray = NTScalarArray.createBuilder().
            value(ScalarType.pvDouble).
            addAlarm().create();

        testAttachAlarm(ntscalarArray, true);
    }

    public static void testAlarm2()
    {
        NTScalarArray ntscalarArray = NTScalarArray.createBuilder().
            value(ScalarType.pvDouble).create();

        testAttachAlarm(ntscalarArray, false);
    }

    // test attaching displays

    public static void testDisplay1()
    {
        NTScalarArray ntscalarArray = NTScalarArray.createBuilder().
            value(ScalarType.pvDouble).
            addDisplay().create();

        testAttachDisplay(ntscalarArray, true);
    }

    public static void testDisplay2()
    {
        NTScalarArray ntscalarArray = NTScalarArray.createBuilder().
            value(ScalarType.pvDouble).create();

        testAttachDisplay(ntscalarArray, false);
    }

    // test attaching controls

    public static void testControl1()
    {
        NTScalarArray ntscalarArray = NTScalarArray.createBuilder().
            value(ScalarType.pvDouble).
            addControl().create();

        testAttachControl(ntscalarArray, true);
    }

    public static void testControl2()
    {
        NTScalarArray ntscalarArray = NTScalarArray.createBuilder().
            value(ScalarType.pvDouble).create();

        testAttachControl(ntscalarArray, false);
    }

    public static void testBuilderResets()
    {
        NTScalarArrayBuilder builder = NTScalarArray.createBuilder();

        Structure s1 = builder.
            value(ScalarType.pvString).
            createStructure();

        Structure s2 = builder.
            value(ScalarType.pvDouble).
            addDescriptor().
            addTimeStamp().
            addAlarm().
            addDisplay().
            addControl().
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
            addDisplay().
            addControl().
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
        assertEquals(expected, NTScalarArray.is_a(s));
    }



    public static void testStructureIsCompatibleImpl(Structure s, boolean expected)
    {
        PVStructure pvSt = dataCreate.createPVStructure(s);
        assertEquals(expected, NTScalarArray.isCompatible(pvSt));
    }

    private static <T extends PVScalarArray>
    void testNTScalarArray_BuilderCreatedImpl(Class<T> c, ScalarType scalarType)
    {
        testNTScalarArray_BuilderCreatedImpl(c, scalarType, new String[0], new String[0], new Field[0]);
    }

    private static <T extends PVScalarArray>
    void testNTScalarArray_BuilderCreatedImpl(Class<T> c, ScalarType scalarType, String[] standardFields)
    {
        testNTScalarArray_BuilderCreatedImpl(c, scalarType, standardFields, new String[0], new Field[0]);
    }


    private static <T extends PVScalarArray>
    void testNTScalarArray_BuilderCreatedImpl(Class<T> c, ScalarType scalarType, String[] standardFields, String[] extraNames, Field[] extraFields)
    {
        NTScalarArray ntscalarArray = createNTScalarArray(c,scalarType,
            standardFields,extraNames,extraFields);

        ntScalarChecks(ntscalarArray,c,scalarType,
            standardFields,extraNames,extraFields);        
    }


    private static <T extends PVScalarArray>
    NTScalarArray createNTScalarArray(Class<T> c, ScalarType scalarType, String[] standardFields, String[] extraNames, Field[] extraFields)
    {
        boolean hasDescriptor = find("descriptor", standardFields);
        boolean hasTimeStamp  = find("timeStamp", standardFields);
        boolean hasAlarm      = find("alarm", standardFields);
        boolean hasDisplay    = find("display", standardFields);
        boolean hasControl    = find("control", standardFields);

        // Create NTScalarArray
        NTScalarArrayBuilder builder = NTScalarArray.createBuilder().value(scalarType);

        if (hasDescriptor) builder.addDescriptor();
        if (hasTimeStamp) builder.addTimeStamp();
        if (hasAlarm) builder.addAlarm();
        if (hasDisplay) builder.addDisplay();
        if (hasControl) builder.addControl();

        for (int i = 0; i < extraNames.length; ++i)
            builder.add(extraNames[i], extraFields[i]);

        return builder.create();
    }

    private static <T extends PVScalarArray>
    void ntScalarChecks(NTScalarArray ntscalarArray, Class<T> c,
        ScalarType scalarType, String[] standardFields,
        String[] extraNames, Field[] extraFields)
    {
        // parse optional fields
        boolean hasDescriptor = find("descriptor", standardFields);
        boolean hasTimeStamp  = find("timeStamp", standardFields);
        boolean hasAlarm      = find("alarm", standardFields);
        boolean hasDisplay    = find("display", standardFields);
        boolean hasControl    = find("control", standardFields);

        // Test value field through NTScalarArray interface
        PVScalarArray pvValue = ntscalarArray.getValue();
		assertNotNull(pvValue);
        T pvValue2 = (T)pvValue;
		assertNotNull(pvValue2);
        T pvValue3 = ntscalarArray.getValue(c);
		assertNotNull(pvValue3);

		// Test optional fields through NTScalarArray interface
        NTField ntField = NTField.get();

        PVString pvDescriptor = ntscalarArray.getDescriptor();
        if (hasDescriptor)
        {
            assertNotNull(pvDescriptor);
        }
        else
            assertNull(pvDescriptor);

        PVStructure pvTimeStamp = ntscalarArray.getTimeStamp();
        if (hasTimeStamp)
        {
            assertNotNull(pvTimeStamp);
            assertTrue(ntField.isTimeStamp(pvTimeStamp.getField()));
        }
        else
            assertNull(pvTimeStamp);

        PVStructure pvAlarm = ntscalarArray.getAlarm();
        if (hasAlarm)
        {
            assertNotNull(pvAlarm);
            assertTrue(ntField.isAlarm(pvAlarm.getField()));
        }
        else
            assertNull(pvAlarm);

        PVStructure pvDisplay = ntscalarArray.getDisplay();
        if (hasDisplay)
        {
            assertNotNull(pvDisplay);
            assertTrue(ntField.isDisplay(pvDisplay.getField()));
        }
        else
            assertNull(pvDisplay);

        PVStructure pvControl = ntscalarArray.getControl();
        if (hasControl)
        {
            assertNotNull(ntscalarArray.getControl());
            assertTrue(ntField.isControl(pvControl.getField()));
        }
        else
            assertNull(ntscalarArray.getControl());

        // Test PVStructure from NTScalarArray
        PVStructure pvStructure = ntscalarArray.getPVStructure();
        assertTrue(NTScalarArray.is_a(pvStructure.getStructure()));
        assertTrue(NTScalarArray.isCompatible(pvStructure));

        assertSame(pvValue3, pvStructure.getSubField(c, "value"));
        assertSame(pvDescriptor,pvStructure.getSubField(PVString.class, "descriptor"));
        assertSame(pvTimeStamp, pvStructure.getSubField(PVStructure.class, "timeStamp"));
        assertSame(pvAlarm, pvStructure.getSubField(PVStructure.class, "alarm"));
        assertSame(pvDisplay, pvStructure.getSubField(PVStructure.class, "display"));
        assertSame(pvControl, pvStructure.getSubField(PVStructure.class, "control"));

        for (int i = 0; i < extraNames.length; ++i)
        {
            PVField pvField = pvStructure.getSubField(extraNames[i]);
            assertNotNull(pvField);
            assertSame(pvField.getField(),extraFields[i]);
        }
    }
}

