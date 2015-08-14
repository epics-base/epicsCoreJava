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
 * JUnit test for NTScalar.
 * @author dgh
 *
 */
public class NTScalarTest extends NTTestBase
{
    // Test creation of NTScalarBuilder

    public static void testCreateBuilder()
	{
        NTScalarBuilder builder1 = NTScalar.createBuilder();
		assertNotNull(builder1);

        NTScalarBuilder builder2 = NTScalar.createBuilder();		
		assertNotSame(builder1, builder2);
	}

    // Test NTScalars created with Builder

    public static void testNTScalar_BuilderCreated1()
    {
        testNTScalar_BuilderCreatedImpl(PVDouble.class, ScalarType.pvDouble);
    }

    public static void testNTScalar_BuilderCreated2()
    {
        testNTScalar_BuilderCreatedImpl(PVString.class, ScalarType.pvString,
            new String[] {"timeStamp"});
    }

    public static void testNTScalar_BuilderCreated3()
    {
        testNTScalar_BuilderCreatedImpl(PVInt.class, ScalarType.pvInt,
            new String[] {"descriptor", "alarm", "timeStamp"} );
    }

    public static void testNTScalar_BuilderCreated4()
    {
        testNTScalar_BuilderCreatedImpl(PVDouble.class, ScalarType.pvDouble,
            new String[] {"descriptor", "alarm", "control", "display", "timeStamp"} );
    }

    public static void testNTScalar_BuilderCreated5()
    {
        testNTScalar_BuilderCreatedImpl(PVULong.class, ScalarType.pvULong,
            new String[] {"descriptor", "control", "display", "timeStamp"},
            new String[] {"extra1"}, 
            new Field[]  { fieldCreate.createScalar(ScalarType.pvDouble)});
    }


    // Test is_a

    public static void testNTScalarIs_a()
    {
        Structure s = NTScalar.createBuilder().value(ScalarType.pvDouble).createStructure();
		assertTrue(NTScalar.is_a(s));
    }

    public static void testStructureIs_a()
    {
        testStructureIs_aImpl(NTScalar.URI, true);
        testStructureIs_aImpl("epics:nt/NTScalar:1.0", true);
        testStructureIs_aImpl("epics:nt/NTScalar:1.1", true);
        testStructureIs_aImpl("epics:nt/NTScalar:2.0", false);
        testStructureIs_aImpl("epics:nt/NTScalar", false);
        testStructureIs_aImpl("nt/NTScalar:1.0", false);
        testStructureIs_aImpl("NTScalar:1.0", false);
        testStructureIs_aImpl("NTScalar", false);
        testStructureIs_aImpl("epics:nt/NTScalarArray:1.0", false);
    }

    // test compatibility - compatible structures

    public static void testStructureIsCompatible1a()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTScalar.URI).
                add("value", ScalarType.pvDouble).
                createStructure(),
            true);  
    }

    public static void testStructureIsCompatible1b()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTScalar.URI).
                add("value", ScalarType.pvDouble).
                add("extra", ScalarType.pvString).
                createStructure(),
            true);
    }

    public static void testStructureIsCompatible1c()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTScalar.URI).
                add("value", ScalarType.pvUByte).
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
                setId(NTScalar.URI).
                add("Value", ScalarType.pvDouble).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2b()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTScalar.URI).
                addArray("value", ScalarType.pvDouble).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2c()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTScalar.URI).
                add("value", ntField.createTimeStamp()).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2d1()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTScalar.URI).
                add("value", ScalarType.pvDouble).
                add("descriptor", ScalarType.pvInt).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2d2()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTScalar.URI).
                add("value", ScalarType.pvDouble).
                add("alarm", ScalarType.pvInt).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2d3()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTScalar.URI).
                add("value", ScalarType.pvDouble).
                add("timeStamp", ScalarType.pvInt).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2d4()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTScalar.URI).
                add("value", ScalarType.pvDouble).
                add("display", ScalarType.pvInt).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2d5()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTScalar.URI).
                add("value", ScalarType.pvDouble).
                add("control", ScalarType.pvInt).
                createStructure(),
            false);
    }

    // test wrapping compatible structures

    public static void testWrappedNTScalar1()
    {
        Structure s = fieldCreate.createFieldBuilder().
            setId(NTScalar.URI).
            add("value", ScalarType.pvDouble).
            createStructure();

        NTScalar ntscalar = NTScalar.wrap(dataCreate.createPVStructure(s));

        ntScalarChecks(ntscalar, PVDouble.class,
            ScalarType.pvDouble, new String[0],
            new String[0], new Field[0]);

        NTScalar ntscalar2 = NTScalar.wrapUnsafe(dataCreate.
            createPVStructure(s));

        ntScalarChecks(ntscalar2, PVDouble.class,
            ScalarType.pvDouble, new String[0],
            new String[0], new Field[0]);
    }


    public static void testWrappedNTScalar2()
    {
        Structure s = fieldCreate.createFieldBuilder().
            setId(NTScalar.URI).
            add("value", ScalarType.pvString).
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

        NTScalar ntscalar = NTScalar.wrap(dataCreate.createPVStructure(s));

        ntScalarChecks(ntscalar, PVString.class,
            ScalarType.pvString, standardFields,
            extraNames,extraFields);

        NTScalar ntscalar2 = NTScalar.wrapUnsafe(dataCreate.
            createPVStructure(s));

        ntScalarChecks(ntscalar, PVString.class,
            ScalarType.pvString, standardFields,
            extraNames,extraFields);
    }

    // test standard fields

    // test attaching timeStamps

    public static void testTimeStamp1()
    {
        NTScalar ntscalar = NTScalar.createBuilder().
            value(ScalarType.pvDouble).
            addTimeStamp().create();

        testAttachTimeStamp(ntscalar, true);
    }
 
    public static void testTimeStamp2()
    {
        NTScalar ntscalar = NTScalar.createBuilder().
            value(ScalarType.pvDouble).create();

        testAttachTimeStamp(ntscalar, false);
    }

    // test attaching alarms

    public static void testAlarm1()
    {
        NTScalar ntscalar = NTScalar.createBuilder().
            value(ScalarType.pvDouble).
            addAlarm().create();

        testAttachAlarm(ntscalar, true);
    }

    public static void testAlarm2()
    {
        NTScalar ntscalar = NTScalar.createBuilder().
            value(ScalarType.pvDouble).create();

        testAttachAlarm(ntscalar, false);
    }

    // test attaching displays

    public static void testDisplay1()
    {
        NTScalar ntscalar = NTScalar.createBuilder().
            value(ScalarType.pvDouble).
            addDisplay().create();

        testAttachDisplay(ntscalar, true);
    }

    public static void testDisplay2()
    {
        NTScalar ntscalar = NTScalar.createBuilder().
            value(ScalarType.pvDouble).create();

        testAttachDisplay(ntscalar, false);
    }

    // test attaching controls

    public static void testControl1()
    {
        NTScalar ntscalar = NTScalar.createBuilder().
            value(ScalarType.pvDouble).
            addControl().create();

        testAttachControl(ntscalar, true);
    }

    public static void testControl2()
    {
        NTScalar ntscalar = NTScalar.createBuilder().
            value(ScalarType.pvDouble).create();

        testAttachControl(ntscalar, false);
    }

    public static void testBuilderResets()
    {
        NTScalarBuilder builder = NTScalar.createBuilder();

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
        assertEquals(NTScalar.is_a(s), expected);
    }



    public static void testStructureIsCompatibleImpl(Structure s, boolean expected)
    {
        PVStructure pvSt = dataCreate.createPVStructure(s);
        assertEquals(NTScalar.isCompatible(pvSt), expected);
    }

    private static <T extends PVScalar>
    void testNTScalar_BuilderCreatedImpl(Class<T> c, ScalarType scalarType)
    {
        testNTScalar_BuilderCreatedImpl(c, scalarType, new String[0], new String[0], new Field[0]);
    }

    private static <T extends PVScalar>
    void testNTScalar_BuilderCreatedImpl(Class<T> c, ScalarType scalarType, String[] standardFields)
    {
        testNTScalar_BuilderCreatedImpl(c, scalarType, standardFields, new String[0], new Field[0]);
    }


    private static <T extends PVScalar>
    void testNTScalar_BuilderCreatedImpl(Class<T> c, ScalarType scalarType, String[] standardFields, String[] extraNames, Field[] extraFields)
    {
        NTScalar ntscalar = createNTScalar(c,scalarType,
            standardFields,extraNames,extraFields);

        ntScalarChecks(ntscalar,c,scalarType,
            standardFields,extraNames,extraFields);        
    }


    private static <T extends PVScalar>
    NTScalar createNTScalar(Class<T> c, ScalarType scalarType, String[] standardFields, String[] extraNames, Field[] extraFields)
    {
        boolean hasDescriptor = find("descriptor", standardFields);
        boolean hasTimeStamp  = find("timeStamp", standardFields);
        boolean hasAlarm      = find("alarm", standardFields);
        boolean hasDisplay    = find("display", standardFields);
        boolean hasControl    = find("control", standardFields);

        // Create NTScalar
        NTScalarBuilder builder = NTScalar.createBuilder().value(scalarType);

        if (hasDescriptor) builder.addDescriptor();
        if (hasTimeStamp) builder.addTimeStamp();
        if (hasAlarm) builder.addAlarm();
        if (hasDisplay) builder.addDisplay();
        if (hasControl) builder.addControl();

        for (int i = 0; i < extraNames.length; ++i)
            builder.add(extraNames[i], extraFields[i]);

        return builder.create();
    }

    private static <T extends PVScalar>
    void ntScalarChecks(NTScalar ntscalar, Class<T> c,
        ScalarType scalarType, String[] standardFields,
        String[] extraNames, Field[] extraFields)
    {
        // parse optional fields
        boolean hasDescriptor = find("descriptor", standardFields);
        boolean hasTimeStamp  = find("timeStamp", standardFields);
        boolean hasAlarm      = find("alarm", standardFields);
        boolean hasDisplay    = find("display", standardFields);
        boolean hasControl    = find("control", standardFields);

        // Test value field through NTScalar interface
        PVScalar pvValue = ntscalar.getValue();
		assertNotNull(pvValue);
        T pvValue2 = (T)pvValue;
		assertNotNull(pvValue2);
        T pvValue3 = ntscalar.getValue(c);
		assertNotNull(pvValue3);

		// Test optional fields through NTScalar interface
        NTField ntField = NTField.get();

        PVString pvDescriptor = ntscalar.getDescriptor();
        if (hasDescriptor)
        {
            assertNotNull(pvDescriptor);
        }
        else
            assertNull(pvDescriptor);

        PVStructure pvTimeStamp = ntscalar.getTimeStamp();
        if (hasTimeStamp)
        {
            assertNotNull(pvTimeStamp);
            assertTrue(ntField.isTimeStamp(pvTimeStamp.getField()));
        }
        else
            assertNull(pvTimeStamp);

        PVStructure pvAlarm = ntscalar.getAlarm();
        if (hasAlarm)
        {
            assertNotNull(pvAlarm);
            assertTrue(ntField.isAlarm(pvAlarm.getField()));
        }
        else
            assertNull(pvAlarm);

        PVStructure pvDisplay = ntscalar.getDisplay();
        if (hasDisplay)
        {
            assertNotNull(pvDisplay);
            assertTrue(ntField.isDisplay(pvDisplay.getField()));
        }
        else
            assertNull(pvDisplay);

        PVStructure pvControl = ntscalar.getControl();
        if (hasControl)
        {
            assertNotNull(ntscalar.getControl());
            assertTrue(ntField.isControl(pvControl.getField()));
        }
        else
            assertNull(ntscalar.getControl());

        // Test PVStructure from NTScalar
        PVStructure pvStructure = ntscalar.getPVStructure();
        assertTrue(NTScalar.is_a(pvStructure.getStructure()));
        assertTrue(NTScalar.isCompatible(pvStructure));

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

