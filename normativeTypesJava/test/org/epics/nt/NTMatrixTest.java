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
 * JUnit test for NTMatrix.
 * @author dgh
 *
 */
public class NTMatrixTest extends NTTestBase
{
    // Test creation of NTMatrixBuilder

    public static void testCreateBuilder()
	{
        NTMatrixBuilder builder1 = NTMatrix.createBuilder();
		assertNotNull(builder1);

        NTMatrixBuilder builder2 = NTMatrix.createBuilder();		
		assertNotSame(builder1, builder2);
	}

    // Test NTMatrixs created with Builder

    public static void testNTMatrix_BuilderCreated1()
    {
        testNTMatrix_BuilderCreatedImpl(new String[0]);
    }

    public static void testNTMatrix_BuilderCreated2()
    {
        testNTMatrix_BuilderCreatedImpl(new String[] {"timeStamp"});
    }

    public static void testNTMatrix_BuilderCreated3()
    {
        testNTMatrix_BuilderCreatedImpl(
            new String[] {"descriptor", "alarm", "timeStamp"} );
    }

    public static void testNTMatrix_BuilderCreated4()
    {
        testNTMatrix_BuilderCreatedImpl(
            new String[] {"descriptor", "display", "timeStamp"} );
    }

    public static void testNTMatrix_BuilderCreated5()
    {
        testNTMatrix_BuilderCreatedImpl(
            new String[] {"descriptor", "alarm", "display", "timeStamp"},
            new String[] {"extra1"}, 
            new Field[]  { fieldCreate.createScalar(ScalarType.pvDouble)});
    }


    // Test is_a

    public static void testNTMatrixIs_a()
    {
        Structure s = NTMatrix.createBuilder().createStructure();
		assertTrue(NTMatrix.is_a(s));
    }

    public static void testStructureIs_a()
    {
        testStructureIs_aImpl(NTMatrix.URI, true);
        testStructureIs_aImpl("epics:nt/NTMatrix:1.0", true);
        testStructureIs_aImpl("epics:nt/NTMatrix:1.1", true);
        testStructureIs_aImpl("epics:nt/NTMatrix:2.0", false);
        testStructureIs_aImpl("epics:nt/NTMatrix", false);
        testStructureIs_aImpl("nt/NTMatrix:1.0", false);
        testStructureIs_aImpl("NTMatrix:1.0", false);
        testStructureIs_aImpl("NTMatrix", false);
        testStructureIs_aImpl("epics:nt/NTScalarArray:1.0", false);
    }

    // test compatibility - compatible structures

    public static void testStructureIsCompatible1a()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTMatrix.URI).
                addArray("value", ScalarType.pvDouble).
                addArray("dim", ScalarType.pvInt).
                createStructure(),
            true);  
    }

    public static void testStructureIsCompatible1b()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTMatrix.URI).
                addArray("value", ScalarType.pvDouble).
                addArray("dim", ScalarType.pvInt).
                add("extra", ScalarType.pvString).
                createStructure(),
            true);
    }

    public static void testStructureIsCompatible1c()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTMatrix.URI).
                addArray("value", ScalarType.pvDouble).
                addArray("dim", ScalarType.pvInt).
                add("descriptor", ScalarType.pvString).
                add("alarm", ntField.createAlarm()).
                add("timeStamp", ntField.createTimeStamp()).
                add("display", ntField.createDisplay()).
                addArray("extra", ScalarType.pvString).
                createStructure(),
            true);
    }

    // test compatibility - incompatible structures

    public static void testStructureIsCompatible2a()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTMatrix.URI).
                addArray("Value", ScalarType.pvDouble).
                addArray("dim", ScalarType.pvInt).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2b()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTMatrix.URI).
                add("value", ScalarType.pvDouble).
                addArray("dim", ScalarType.pvInt).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2c()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTMatrix.URI).
                addArray("value", ntField.createTimeStamp()).
                addArray("dim", ScalarType.pvInt).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2c2()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTMatrix.URI).
                addArray("value", ScalarType.pvInt).
                addArray("dim", ScalarType.pvInt).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2d1()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTMatrix.URI).
                addArray("value", ScalarType.pvDouble).
                addArray("dim", ScalarType.pvInt).
                add("descriptor", ScalarType.pvInt).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2d2()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTMatrix.URI).
                addArray("value", ScalarType.pvDouble).
                addArray("dim", ScalarType.pvInt).
                add("alarm", ScalarType.pvInt).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2d3()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTMatrix.URI).
                addArray("value", ScalarType.pvDouble).
                addArray("dim", ScalarType.pvInt).
                add("timeStamp", ScalarType.pvInt).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2d4()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTMatrix.URI).
                addArray("value", ScalarType.pvDouble).
                addArray("dim", ScalarType.pvInt).
                add("display", ScalarType.pvInt).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2e()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTMatrix.URI).
                addArray("value", ScalarType.pvDouble).
                addArray("dim", ScalarType.pvUInt).
                createStructure(),
            false);
    }
    // test wrapping compatible structures

    public static void testWrappedNTMatrix1()
    {
        Structure s = fieldCreate.createFieldBuilder().
            setId(NTMatrix.URI).
            addArray("value", ScalarType.pvDouble).
            addArray("dim", ScalarType.pvInt).
            createStructure();

        NTMatrix ntmatrix = NTMatrix.wrap(dataCreate.createPVStructure(s));

        assertTrue(ntmatrix!=null);

        ntmatrixChecks(ntmatrix, new String[0],
            new String[0], new Field[0]);

        NTMatrix ntmatrix2 = NTMatrix.wrapUnsafe(dataCreate.
            createPVStructure(s));

        ntmatrixChecks(ntmatrix2, new String[0],
            new String[0], new Field[0]);
    }


    public static void testWrappedNTMatrix2()
    {
        Structure s = fieldCreate.createFieldBuilder().
            setId(NTMatrix.URI).
            addArray("value", ScalarType.pvDouble).
            addArray("dim", ScalarType.pvInt).
            add("descriptor", ScalarType.pvString).
            add("alarm", ntField.createAlarm()).
            add("timeStamp", ntField.createTimeStamp()).
            add("display", ntField.createDisplay()).
            addArray("extra", ScalarType.pvString).
            createStructure();

        String[] standardFields = { "descriptor", "alarm",
            "display", "timeStamp" };
        String[] extraNames = { "extra" };
        Field[] extraFields = { fieldCreate.createScalarArray(ScalarType.pvString) };

        NTMatrix ntmatrix = NTMatrix.wrap(dataCreate.createPVStructure(s));

        ntmatrixChecks(ntmatrix, standardFields,
            extraNames,extraFields);

        NTMatrix ntmatrix2 = NTMatrix.wrapUnsafe(dataCreate.
            createPVStructure(s));

        ntmatrixChecks(ntmatrix, standardFields,
            extraNames,extraFields);
    }

    // test attaching timeStamps

    public static void testTimeStamp1()
    {
        NTMatrix ntmatrix = NTMatrix.createBuilder().
            addTimeStamp().create();

        testAttachTimeStamp(ntmatrix, true);
    }
 
    public static void testTimeStamp2()
    {
        NTMatrix ntmatrix = NTMatrix.createBuilder().create();

        testAttachTimeStamp(ntmatrix, false);
    }

    // test attaching alarms

    public static void testAlarm1()
    {
        NTMatrix ntmatrix = NTMatrix.createBuilder().
            addAlarm().create();

        testAttachAlarm(ntmatrix, true);
    }

    public static void testAlarm2()
    {
        NTMatrix ntmatrix = NTMatrix.createBuilder().create();

        testAttachAlarm(ntmatrix, false);
    }

    // test attaching displays

    public static void testDisplay1()
    {
        NTMatrix ntmatrix = NTMatrix.createBuilder().
            addDisplay().create();

        testAttachDisplay(ntmatrix, true);
    }

    public static void testDisplay2()
    {
        NTMatrix ntmatrix = NTMatrix.createBuilder().create();

        testAttachDisplay(ntmatrix, false);
    }

    public static void testBuilderResets()
    {
        NTMatrixBuilder builder = NTMatrix.createBuilder();

        Structure s1 = builder.createStructure();

        Structure s2 = builder.
            addDescriptor().
            addTimeStamp().
            addAlarm().
            addDisplay().
            add("extra", fieldCreate.createScalar(ScalarType.pvInt)).
            createStructure();

        Structure s3 = builder.
            createStructure();

        Structure s4 = builder.
            addDescriptor().
            addTimeStamp().
            addAlarm().
            addDisplay().
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
        assertEquals(expected, NTMatrix.is_a(s));
    }



    public static void testStructureIsCompatibleImpl(Structure s, boolean expected)
    {
        PVStructure pvSt = dataCreate.createPVStructure(s);
        assertEquals(expected, NTMatrix.isCompatible(pvSt));
    }

    /*private static void testNTMatrix_BuilderCreatedImpl()
    {
        testNTMatrix_BuilderCreatedImpl(new String[0], new String[0], new Field[0]);
    }*/

    private static void testNTMatrix_BuilderCreatedImpl(String[] standardFields)
    {
        testNTMatrix_BuilderCreatedImpl(standardFields, new String[0], new Field[0]);
    }


    private static void testNTMatrix_BuilderCreatedImpl(String[] standardFields, String[] extraNames, Field[] extraFields)
    {
        NTMatrix ntmatrix = createNTMatrix(standardFields, extraNames, extraFields);

        ntmatrixChecks(ntmatrix, standardFields, extraNames, extraFields);        
    }


    private static NTMatrix createNTMatrix(String[] standardFields,
        String[] extraNames, Field[] extraFields)
    {
        boolean hasDescriptor = find("descriptor", standardFields);
        boolean hasTimeStamp  = find("timeStamp", standardFields);
        boolean hasAlarm      = find("alarm", standardFields);
        boolean hasDisplay    = find("display", standardFields);

        // Create NTMatrix
        NTMatrixBuilder builder = NTMatrix.createBuilder();

        if (hasDescriptor) builder.addDescriptor();
        if (hasTimeStamp) builder.addTimeStamp();
        if (hasAlarm) builder.addAlarm();
        if (hasDisplay) builder.addDisplay();

        for (int i = 0; i < extraNames.length; ++i)
            builder.add(extraNames[i], extraFields[i]);

        return builder.create();
    }

    private static void ntmatrixChecks(NTMatrix ntmatrix,
        String[] standardFields, String[] extraNames, Field[] extraFields)
    {
        // parse optional fields
        boolean hasDescriptor = find("descriptor", standardFields);
        boolean hasTimeStamp  = find("timeStamp", standardFields);
        boolean hasAlarm      = find("alarm", standardFields);
        boolean hasDisplay    = find("display", standardFields);

        // Test value field through NTMatrix interface
        PVScalarArray pvValue = ntmatrix.getValue();
		assertNotNull(pvValue);

		// Test optional fields through NTMatrix interface

        PVString pvDescriptor = ntmatrix.getDescriptor();
        if (hasDescriptor)
        {
            assertNotNull(pvDescriptor);
        }
        else
            assertNull(pvDescriptor);

        PVStructure pvTimeStamp = ntmatrix.getTimeStamp();
        if (hasTimeStamp)
        {
            assertNotNull(pvTimeStamp);
            assertTrue(ntField.isTimeStamp(pvTimeStamp.getField()));
        }
        else
            assertNull(pvTimeStamp);

        PVStructure pvAlarm = ntmatrix.getAlarm();
        if (hasAlarm)
        {
            assertNotNull(pvAlarm);
            assertTrue(ntField.isAlarm(pvAlarm.getField()));
        }
        else
            assertNull(pvAlarm);

        PVStructure pvDisplay = ntmatrix.getDisplay();
        if (hasDisplay)
        {
            assertNotNull(pvDisplay);
            assertTrue(ntField.isDisplay(pvDisplay.getField()));
        }
        else
            assertNull(pvDisplay);

        // Test PVStructure from NTMatrix
        PVStructure pvStructure = ntmatrix.getPVStructure();
        assertTrue(NTMatrix.is_a(pvStructure.getStructure()));
        assertTrue(NTMatrix.isCompatible(pvStructure));

        assertSame(pvValue, pvStructure.getSubField(PVDoubleArray.class, "value"));
        assertSame(pvDescriptor,pvStructure.getSubField(PVString.class, "descriptor"));
        assertSame(pvTimeStamp, pvStructure.getSubField(PVStructure.class, "timeStamp"));
        assertSame(pvAlarm, pvStructure.getSubField(PVStructure.class, "alarm"));
        assertSame(pvDisplay, pvStructure.getSubField(PVStructure.class, "display"));

        for (int i = 0; i < extraNames.length; ++i)
        {
            PVField pvField = pvStructure.getSubField(extraNames[i]);
            assertNotNull(pvField);
            assertSame(pvField.getField(),extraFields[i]);
        }
    }
}

