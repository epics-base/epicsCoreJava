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
import org.epics.pvdata.pv.Union;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVDouble;
import org.epics.pvdata.pv.PVInt;
import org.epics.pvdata.pv.PVLong;
import org.epics.pvdata.pv.PVULong;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.PVScalar;
import org.epics.pvdata.pv.PVUnion;
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.property.*;

/**
 * JUnit test for NTUnion.
 * @author dgh
 *
 */
public class NTUnionTest extends NTTestBase
{
    // Test creation of NTUnionBuilder

    public static void testCreateBuilder()
	{
        NTUnionBuilder builder1 = NTUnion.createBuilder();
		assertNotNull(builder1);

        NTUnionBuilder builder2 = NTUnion.createBuilder();		
		assertNotSame(builder1, builder2);
	}

    // Test NTUnions created with Builder

    public static void testNTUnion_BuilderCreated1()
    {
        testNTUnion_BuilderCreatedImpl(variantUnion);
    }

    public static void testNTUnion_BuilderCreated2()
    {
        testNTUnion_BuilderCreatedImpl(exampleRegularUnion(),
            new String[] {"timeStamp"});
    }

    public static void testNTUnion_BuilderCreated3()
    {
        testNTUnion_BuilderCreatedImpl(variantUnion,
            new String[] {"descriptor", "alarm", "timeStamp"} );
    }

    public static void testNTUnion_BuilderCreated4()
    {
        testNTUnion_BuilderCreatedImpl(exampleRegularUnion(),
            new String[] {"descriptor", "alarm", "timeStamp"} );
    }

    public static void testNTUnion_BuilderCreated5()
    {
        testNTUnion_BuilderCreatedImpl(exampleRegularUnion(),
            new String[] {"descriptor", "timeStamp"},
            new String[] {"extra1"}, 
            new Field[]  { fieldCreate.createScalar(ScalarType.pvDouble)});
    }


    // Test is_a

    public static void testNTUnionIs_a()
    {
        Structure s = NTUnion.createBuilder().value(variantUnion).createStructure();
		assertTrue(NTUnion.is_a(s));
    }

    public static void testStructureIs_a()
    {
        testStructureIs_aImpl(NTUnion.URI, true);
        testStructureIs_aImpl("epics:nt/NTUnion:1.0", true);
        testStructureIs_aImpl("epics:nt/NTUnion:1.1", true);
        testStructureIs_aImpl("epics:nt/NTUnion:2.0", false);
        testStructureIs_aImpl("epics:nt/NTUnion", false);
        testStructureIs_aImpl("nt/NTUnion:1.0", false);
        testStructureIs_aImpl("NTUnion:1.0", false);
        testStructureIs_aImpl("NTUnion", false);
        testStructureIs_aImpl("epics:nt/NTUnionArray:1.0", false);
    }

    // test compatibility - compatible structures

    public static void testStructureIsCompatible1a()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTUnion.URI).
                add("value", variantUnion).
                createStructure(),
            true);  
    }

    public static void testStructureIsCompatible1b()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTUnion.URI).
                add("value", variantUnion).
                add("extra", exampleRegularUnion()).
                createStructure(),
            true);
    }

    public static void testStructureIsCompatible1c()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTUnion.URI).
                add("value", variantUnion).
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
                setId(NTUnion.URI).
                add("Value", variantUnion).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2b()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTUnion.URI).
                addArray("value", variantUnion).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2c()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTUnion.URI).
                add("value", ntField.createTimeStamp()).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2d1()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTUnion.URI).
                add("value", variantUnion).
                add("descriptor", ScalarType.pvInt).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2d2()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTUnion.URI).
                add("value", variantUnion).
                add("alarm", ScalarType.pvInt).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2d3()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTUnion.URI).
                add("value", variantUnion).
                add("timeStamp", ScalarType.pvInt).
                createStructure(),
            false);
    }

    // test wrapping compatible structures

    public static void testWrappedNTUnion1()
    {
        Structure s = fieldCreate.createFieldBuilder().
            setId(NTUnion.URI).
            add("value", exampleRegularUnion()).
            createStructure();

        NTUnion ntunion = NTUnion.wrap(dataCreate.createPVStructure(s));

        ntUnionChecks(ntunion,
            variantUnion, new String[0],
            new String[0], new Field[0]);

        NTUnion ntunion2 = NTUnion.wrapUnsafe(dataCreate.
            createPVStructure(s));

        ntUnionChecks(ntunion2,
            variantUnion, new String[0],
            new String[0], new Field[0]);
    }


    public static void testWrappedNTUnion2()
    {
        Structure s = fieldCreate.createFieldBuilder().
            setId(NTUnion.URI).
            add("value", exampleRegularUnion()).
            add("descriptor", ScalarType.pvString).
            add("alarm", ntField.createAlarm()).
            add("timeStamp", ntField.createTimeStamp()).
            addArray("extra", ScalarType.pvString).
            createStructure();

        String[] standardFields = { "descriptor", "alarm", "timeStamp" };
        String[] extraNames = { "extra" };
        Field[] extraFields = { fieldCreate.createScalarArray(ScalarType.pvString) };

        NTUnion ntunion = NTUnion.wrap(dataCreate.createPVStructure(s));

        ntUnionChecks(ntunion,exampleRegularUnion(), standardFields,
            extraNames,extraFields);

        NTUnion ntunion2 = NTUnion.wrapUnsafe(dataCreate.
            createPVStructure(s));

        ntUnionChecks(ntunion,exampleRegularUnion(), standardFields,
            extraNames,extraFields);
    }

    // test standard fields

    // test attaching timeStamps

    public static void testTimeStamp1()
    {
        NTUnion ntunion = NTUnion.createBuilder().
            value(variantUnion).
            addTimeStamp().create();

        testAttachTimeStamp(ntunion, true);
    }
 
    public static void testTimeStamp2()
    {
        NTUnion ntunion = NTUnion.createBuilder().
            value(variantUnion).create();

        testAttachTimeStamp(ntunion, false);
    }

    // test attaching alarms

    public static void testAlarm1()
    {
        NTUnion ntunion = NTUnion.createBuilder().
            value(variantUnion).
            addAlarm().create();

        testAttachAlarm(ntunion, true);
    }

    public static void testAlarm2()
    {
        NTUnion ntunion = NTUnion.createBuilder().
            value(variantUnion).create();

        testAttachAlarm(ntunion, false);
    }


    public static void testBuilderResets()
    {
        NTUnionBuilder builder = NTUnion.createBuilder();

        Structure s1 = builder.
            value(exampleRegularUnion()).
            createStructure();

        Structure s2 = builder.
            value(variantUnion).
            addDescriptor().
            addTimeStamp().
            addAlarm().
            add("extra", fieldCreate.createScalar(ScalarType.pvInt)).
            createStructure();

        Structure s3 = builder.
            value(exampleRegularUnion()).
            createStructure();

        Structure s4 = builder.
            value(variantUnion).
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
        assertEquals(expected, NTUnion.is_a(s));
    }



    public static void testStructureIsCompatibleImpl(Structure s, boolean expected)
    {
        PVStructure pvSt = dataCreate.createPVStructure(s);
        assertEquals(expected, NTUnion.isCompatible(pvSt));
    }

    private static
    void testNTUnion_BuilderCreatedImpl(Union u)
    {
        testNTUnion_BuilderCreatedImpl(u, new String[0], new String[0], new Field[0]);
    }

    private static
    void testNTUnion_BuilderCreatedImpl(Union u, String[] standardFields)
    {
        testNTUnion_BuilderCreatedImpl(u, standardFields, new String[0], new Field[0]);
    }


    private static
    void testNTUnion_BuilderCreatedImpl(Union u, String[] standardFields, String[] extraNames, Field[] extraFields)
    {
        NTUnion ntunion = createNTUnion(u,standardFields,extraNames,extraFields);

        ntUnionChecks(ntunion,u,standardFields,extraNames,extraFields);        
    }


    private static
    NTUnion createNTUnion(Union u, String[] standardFields, String[] extraNames, Field[] extraFields)
    {
        boolean hasDescriptor = find("descriptor", standardFields);
        boolean hasTimeStamp  = find("timeStamp", standardFields);
        boolean hasAlarm      = find("alarm", standardFields);

        // Create NTUnion
        NTUnionBuilder builder = NTUnion.createBuilder().value(u);

        if (hasDescriptor) builder.addDescriptor();
        if (hasTimeStamp) builder.addTimeStamp();
        if (hasAlarm) builder.addAlarm();

        for (int i = 0; i < extraNames.length; ++i)
            builder.add(extraNames[i], extraFields[i]);

        return builder.create();
    }

    private static
    void ntUnionChecks(NTUnion ntunion,
        Union u, String[] standardFields,
        String[] extraNames, Field[] extraFields)
    {
        // parse optional fields
        boolean hasDescriptor = find("descriptor", standardFields);
        boolean hasTimeStamp  = find("timeStamp", standardFields);
        boolean hasAlarm      = find("alarm", standardFields);

        // Test value field through NTUnion interface
        PVUnion pvValue = ntunion.getValue();
		assertNotNull(pvValue);

		// Test optional fields through NTUnion interface
        NTField ntField = NTField.get();

        PVString pvDescriptor = ntunion.getDescriptor();
        if (hasDescriptor)
        {
            assertNotNull(pvDescriptor);
        }
        else
            assertNull(pvDescriptor);

        PVStructure pvTimeStamp = ntunion.getTimeStamp();
        if (hasTimeStamp)
        {
            assertNotNull(pvTimeStamp);
            assertTrue(ntField.isTimeStamp(pvTimeStamp.getField()));
        }
        else
            assertNull(pvTimeStamp);

        PVStructure pvAlarm = ntunion.getAlarm();
        if (hasAlarm)
        {
            assertNotNull(pvAlarm);
            assertTrue(ntField.isAlarm(pvAlarm.getField()));
        }
        else
            assertNull(pvAlarm);

        // Test PVStructure from NTUnion
        PVStructure pvStructure = ntunion.getPVStructure();
        assertTrue(NTUnion.is_a(pvStructure.getStructure()));
        assertTrue(NTUnion.isCompatible(pvStructure));

        assertSame(pvValue, pvStructure.getSubField(PVUnion.class, "value"));
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

    private static Union exampleRegularUnion()
    {
        if (_exampleRegularUnion == null)
        {
            FieldBuilder fieldBuilder = FieldFactory.getFieldCreate().createFieldBuilder();

            for (ScalarType st : ScalarType.values())
                fieldBuilder.add(st.toString() + "Value", st);

            _exampleRegularUnion = fieldBuilder.createUnion();

        }
        return _exampleRegularUnion;
    }
    private static Union variantUnion = fieldCreate.createVariantUnion();
    private static Union _exampleRegularUnion = null;
}

