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
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.PVScalar;
import org.epics.pvdata.pv.PVScalarArray;
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.property.*;

/**
 * JUnit test for NTAggregate.
 * @author dgh
 *
 */
public class NTAggregateTest extends NTTestBase
{
    // Test creation of NTAggregateBuilder

    public static void testCreateBuilder()
	{
        NTAggregateBuilder builder1 = NTAggregate.createBuilder();
		assertNotNull(builder1);

        NTAggregateBuilder builder2 = NTAggregate.createBuilder();		
		assertNotSame(builder1, builder2);
	}

    // Test NTAggregates created with Builder

    public static void testNTAggregate_BuilderCreated1()
    {
        testNTAggregate_BuilderCreatedImpl(new String[0]);
    }

    public static void testNTAggregate_BuilderCreated2()
    {
        testNTAggregate_BuilderCreatedImpl(new String[] {"dispersion", "timeStamp"});
    }

    public static void testNTAggregate_BuilderCreated3()
    {
        testNTAggregate_BuilderCreatedImpl(
            new String[] {"first", "firstTimeStamp", "descriptor", "alarm", "timeStamp"} );
    }

    public static void testNTAggregate_BuilderCreated4()
    {
        testNTAggregate_BuilderCreatedImpl(
            new String[] {"last", "lastTimeStamp", "descriptor", "alarm", "timeStamp"} );
    }

    public static void testNTAggregate_BuilderCreated5()
    {
        testNTAggregate_BuilderCreatedImpl(
            new String[] {"first", "last", "descriptor", "alarm", "timeStamp"} );
    }

    public static void testNTAggregate_BuilderCreated6()
    {
        testNTAggregate_BuilderCreatedImpl(
            new String[] { "dispersion", "first", "firstTimeStamp", "last",
                "lastTimeStamp", "descriptor", "alarm", "timeStamp"},
            new String[] {"extra1"}, 
            new Field[]  { fieldCreate.createScalar(ScalarType.pvDouble)});
    }


    // Test is_a

    public static void testNTAggregateIs_a()
    {
        Structure s = NTAggregate.createBuilder().createStructure();
		assertTrue(NTAggregate.is_a(s));
    }

    public static void testStructureIs_a()
    {
        testStructureIs_aImpl(NTAggregate.URI, true);
        testStructureIs_aImpl("epics:nt/NTAggregate:1.0", true);
        testStructureIs_aImpl("epics:nt/NTAggregate:1.1", true);
        testStructureIs_aImpl("epics:nt/NTAggregate:2.0", false);
        testStructureIs_aImpl("epics:nt/NTAggregate", false);
        testStructureIs_aImpl("nt/NTAggregate:1.0", false);
        testStructureIs_aImpl("NTAggregate:1.0", false);
        testStructureIs_aImpl("NTAggregate", false);
        testStructureIs_aImpl("epics:nt/NTScalarArray:1.0", false);
    }

    // test compatibility - compatible structures

    public static void testStructureIsCompatible1a()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTAggregate.URI).
                add("value", ScalarType.pvDouble).
                add("N", ScalarType.pvLong).
                createStructure(),
            true);  
    }

    public static void testStructureIsCompatible1b()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTAggregate.URI).
                add("value", ScalarType.pvDouble).
                add("N", ScalarType.pvLong).
                add("extra", ScalarType.pvString).
                createStructure(),
            true);
    }

    public static void testStructureIsCompatible1c()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTAggregate.URI).
                add("value", ScalarType.pvDouble).
                add("N", ScalarType.pvLong).
                add("descriptor", ScalarType.pvString).
                add("alarm", ntField.createAlarm()).
                add("timeStamp", ntField.createTimeStamp()).
                addArray("extra", ScalarType.pvString).
                createStructure(),
            true);
    }

    // test compatibility - incompatible structures

    public static void testStructureIsCompatible2a1()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTAggregate.URI).
                add("N", ScalarType.pvLong).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2a2()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTAggregate.URI).
                add("value", ScalarType.pvDouble).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2a3()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTAggregate.URI).
                add("Value", ScalarType.pvDouble).
                add("N", ScalarType.pvLong).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2a4()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTAggregate.URI).
                add("value", ScalarType.pvDouble).
                add("n", ScalarType.pvLong).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2b1()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTAggregate.URI).
                addArray("value", ScalarType.pvDouble).
                add("N", ScalarType.pvLong).
                createStructure(),
            false);
    }


    public static void testStructureIsCompatible2b2()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTAggregate.URI).
                add("value", ScalarType.pvInt).
                add("N", ScalarType.pvLong).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2b3()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTAggregate.URI).
                add("value", ntField.createTimeStamp()).
                add("N", ScalarType.pvLong).
                createStructure(),
            false);
    }


    public static void testStructureIsCompatible2c1()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTAggregate.URI).
                add("value", ScalarType.pvDouble).
                add("N", ScalarType.pvInt).
                add("dispersion", ScalarType.pvFloat).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2c2()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTAggregate.URI).
                add("value", ScalarType.pvDouble).
                add("N", ScalarType.pvInt).
                add("max", ScalarType.pvInt).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2c3()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTAggregate.URI).
                add("value", ScalarType.pvDouble).
                add("N", ScalarType.pvInt).
                add("max", ScalarType.pvLong).
                createStructure(),
            false);
    }


    public static void testStructureIsCompatible2c4()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTAggregate.URI).
                add("value", ScalarType.pvDouble).
                add("N", ScalarType.pvInt).
                add("max", ScalarType.pvLong).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2d1()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTAggregate.URI).
                add("value", ScalarType.pvDouble).
                add("N", ScalarType.pvLong).
                add("descriptor", ScalarType.pvInt).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2d2()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTAggregate.URI).
                add("value", ScalarType.pvDouble).
                add("N", ScalarType.pvLong).
                add("alarm", ScalarType.pvInt).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2d3()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTAggregate.URI).
                add("value", ScalarType.pvDouble).
                add("N", ScalarType.pvLong).
                add("timeStamp", ScalarType.pvInt).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2e()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTAggregate.URI).
                add("value", ScalarType.pvDouble).
                addArray("dim", ScalarType.pvUInt).
                createStructure(),
            false);
    }
    // test wrapping compatible structures

    public static void testWrappedNTAggregate1()
    {
        Structure s = fieldCreate.createFieldBuilder().
            setId(NTAggregate.URI).
            add("value", ScalarType.pvDouble).
            add("N", ScalarType.pvLong).
            createStructure();

        NTAggregate ntaggregate = NTAggregate.wrap(dataCreate.createPVStructure(s));

        assertTrue(ntaggregate!=null);

        ntaggregateChecks(ntaggregate, new String[0],
            new String[0], new Field[0]);

        NTAggregate ntaggregate2 = NTAggregate.wrapUnsafe(dataCreate.
            createPVStructure(s));

        ntaggregateChecks(ntaggregate2, new String[0],
            new String[0], new Field[0]);
    }


    public static void testWrappedNTAggregate2()
    {
        Structure s = fieldCreate.createFieldBuilder().
            setId(NTAggregate.URI).
            add("value", ScalarType.pvDouble).
            add("N", ScalarType.pvLong).
            add("descriptor", ScalarType.pvString).
            add("alarm", ntField.createAlarm()).
            add("timeStamp", ntField.createTimeStamp()).
            addArray("extra", ScalarType.pvString).
            createStructure();

        String[] standardFields = { "descriptor", "alarm", "timeStamp" };
        String[] extraNames = { "extra" };
        Field[] extraFields = { fieldCreate.createScalarArray(ScalarType.pvString) };

        NTAggregate ntaggregate = NTAggregate.wrap(dataCreate.createPVStructure(s));

        ntaggregateChecks(ntaggregate, standardFields,
            extraNames,extraFields);

        NTAggregate ntaggregate2 = NTAggregate.wrapUnsafe(dataCreate.
            createPVStructure(s));

        ntaggregateChecks(ntaggregate, standardFields,
            extraNames,extraFields);
    }

    // test attaching timeStamps

    public static void testTimeStamp1()
    {
        NTAggregate ntaggregate = NTAggregate.createBuilder().
            addTimeStamp().create();

        testAttachTimeStamp(ntaggregate, true);
    }
 
    public static void testTimeStamp2()
    {
        NTAggregate ntaggregate = NTAggregate.createBuilder().create();

        testAttachTimeStamp(ntaggregate, false);
    }

    // test attaching alarms

    public static void testAlarm1()
    {
        NTAggregate ntaggregate = NTAggregate.createBuilder().
            addAlarm().create();

        testAttachAlarm(ntaggregate, true);
    }

    public static void testAlarm2()
    {
        NTAggregate ntaggregate = NTAggregate.createBuilder().create();

        testAttachAlarm(ntaggregate, false);
    }

    // test builder resets

    public static void testBuilderResets()
    {
        NTAggregateBuilder builder = NTAggregate.createBuilder();

        Structure s1 = builder.createStructure();

        Structure s2 = builder.
            addFirst().
            addFirstTimeStamp().
            addLast().
            addLastTimeStamp().
            addLast().
            addLastTimeStamp().
            addMax().
            addMin().
            addDescriptor().
            addTimeStamp().
            addAlarm().
            add("extra", fieldCreate.createScalar(ScalarType.pvInt)).
            createStructure();

        Structure s3 = builder.
            createStructure();

        Structure s4 = builder.
            addFirst().
            addFirstTimeStamp().
            addLast().
            addLastTimeStamp().
            addLast().
            addLastTimeStamp().
            addMax().
            addMin().
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
        assertEquals(expected, NTAggregate.is_a(s));
    }

    public static void testStructureIsCompatibleImpl(Structure s, boolean expected)
    {
        PVStructure pvSt = dataCreate.createPVStructure(s);
        assertEquals(expected, NTAggregate.isCompatible(pvSt));
    }

    private static void testNTAggregate_BuilderCreatedImpl(String[] standardFields)
    {
        testNTAggregate_BuilderCreatedImpl(standardFields, new String[0], new Field[0]);
    }


    private static void testNTAggregate_BuilderCreatedImpl(String[] standardFields, String[] extraNames, Field[] extraFields)
    {
        NTAggregate ntaggregate = createNTAggregate(standardFields, extraNames, extraFields);

        ntaggregateChecks(ntaggregate, standardFields, extraNames, extraFields);        
    }


    private static NTAggregate createNTAggregate(String[] standardFields,
        String[] extraNames, Field[] extraFields)
    {
        boolean hasDispersion     = find("dispersion", standardFields);
        boolean hasFirst          = find("first", standardFields);
        boolean hasFirstTimeStamp = find("firstTimeStamp", standardFields);
        boolean hasLast           = find("last", standardFields);
        boolean hasLastTimeStamp  = find("lastTimeStamp", standardFields);
        boolean hasMax            = find("max", standardFields);
        boolean hasMin            = find("min", standardFields);
        boolean hasDescriptor     = find("descriptor", standardFields);
        boolean hasTimeStamp      = find("timeStamp", standardFields);
        boolean hasAlarm          = find("alarm", standardFields);

        // Create NTAggregate
        NTAggregateBuilder builder = NTAggregate.createBuilder();

        if (hasDispersion) builder.addDispersion();
        if (hasFirst) builder.addFirst();
        if (hasFirstTimeStamp) builder.addFirstTimeStamp();
        if (hasLast) builder.addLast();
        if (hasLastTimeStamp) builder.addLastTimeStamp();
        if (hasMax) builder.addMax();
        if (hasMin) builder.addMin();

        if (hasDescriptor) builder.addDescriptor();
        if (hasTimeStamp) builder.addTimeStamp();
        if (hasAlarm) builder.addAlarm();

        for (int i = 0; i < extraNames.length; ++i)
            builder.add(extraNames[i], extraFields[i]);

        return builder.create();
    }

    private static void ntaggregateChecks(NTAggregate ntaggregate,
        String[] standardFields, String[] extraNames, Field[] extraFields)
    {
        // parse optional fields
        boolean hasDispersion     = find("dispersion", standardFields);
        boolean hasFirst          = find("first", standardFields);
        boolean hasFirstTimeStamp = find("firstTimeStamp", standardFields);
        boolean hasLast           = find("last", standardFields);
        boolean hasLastTimeStamp  = find("lastTimeStamp", standardFields);
        boolean hasMax            = find("max", standardFields);
        boolean hasMin            = find("min", standardFields);
        boolean hasDescriptor     = find("descriptor", standardFields);
        boolean hasTimeStamp      = find("timeStamp", standardFields);
        boolean hasAlarm          = find("alarm", standardFields);

        // Test value field through NTAggregate interface
        PVDouble pvValue = ntaggregate.getValue();
		assertNotNull(pvValue);

        PVLong pvN = ntaggregate.getN();
		assertNotNull(pvN);

		// Test optional fields through NTAggregate interface

        PVDouble pvDispersion = ntaggregate.getDispersion();
        if (hasDispersion)
        {
            assertNotNull(pvDispersion);
        }
        else
            assertNull(pvDispersion);

        PVDouble pvFirst = ntaggregate.getFirst();
        if (hasFirst)
        {
            assertNotNull(pvFirst);
        }
        else
            assertNull(pvFirst);

        PVStructure pvFirstTimeStamp = ntaggregate.getFirstTimeStamp();
        if (hasFirstTimeStamp)
        {
            assertNotNull(pvFirstTimeStamp);
        }
        else
            assertNull(pvFirstTimeStamp);

        PVDouble pvLast = ntaggregate.getLast();
        if (hasLast)
        {
            assertNotNull(pvLast);
        }
        else
            assertNull(pvLast);

        PVStructure pvLastTimeStamp = ntaggregate.getLastTimeStamp();
        if (hasLastTimeStamp)
        {
            assertNotNull(pvLastTimeStamp);
        }
        else
            assertNull(pvLastTimeStamp);

        PVDouble pvMax = ntaggregate.getMax();
        if (hasMax)
        {
            assertNotNull(pvMax);
        }
        else
            assertNull(pvMax);

        PVDouble pvMin = ntaggregate.getMin();
        if (hasMin)
        {
            assertNotNull(pvMin);
        }
        else
            assertNull(pvMin);

        PVString pvDescriptor = ntaggregate.getDescriptor();
        if (hasDescriptor)
        {
            assertNotNull(pvDescriptor);
        }
        else
            assertNull(pvDescriptor);

        PVStructure pvTimeStamp = ntaggregate.getTimeStamp();
        if (hasTimeStamp)
        {
            assertNotNull(pvTimeStamp);
            assertTrue(ntField.isTimeStamp(pvTimeStamp.getField()));
        }
        else
            assertNull(pvTimeStamp);

        PVStructure pvAlarm = ntaggregate.getAlarm();
        if (hasAlarm)
        {
            assertNotNull(pvAlarm);
            assertTrue(ntField.isAlarm(pvAlarm.getField()));
        }
        else
            assertNull(pvAlarm);

        // Test PVStructure from NTAggregate
        PVStructure pvStructure = ntaggregate.getPVStructure();
        assertTrue(NTAggregate.is_a(pvStructure.getStructure()));
        assertTrue(NTAggregate.isCompatible(pvStructure));

        assertSame(pvValue, pvStructure.getSubField(
            PVDouble.class, "value"));

        assertSame(pvN,pvStructure.getSubField(PVLong.class, "N"));

        assertSame(pvDispersion, pvStructure.getSubField(
             PVDouble.class, "dispersion"));

        assertSame(pvFirst, pvStructure.getSubField(
            PVDouble.class, "first"));

        assertSame(pvFirstTimeStamp, pvStructure.getSubField(
            PVStructure.class, "firstTimeStamp"));

        assertSame(pvLast,pvStructure.getSubField(PVDouble.class, "last"));

        assertSame(pvLastTimeStamp, pvStructure.getSubField(
            PVStructure.class, "lastTimeStamp"));

        assertSame(pvMax, pvStructure.getSubField(PVStructure.class, "max"));
        assertSame(pvMin, pvStructure.getSubField(PVStructure.class, "min"));

        assertSame(pvDescriptor,pvStructure.getSubField(PVString.class,
            "descriptor"));
        assertSame(pvTimeStamp, pvStructure.getSubField(PVStructure.class,
            "timeStamp"));
        assertSame(pvAlarm, pvStructure.getSubField(PVStructure.class,
            "alarm"));

        for (int i = 0; i < extraNames.length; ++i)
        {
            PVField pvField = pvStructure.getSubField(extraNames[i]);
            assertNotNull(pvField);
            assertSame(pvField.getField(),extraFields[i]);
        }
    }
}

