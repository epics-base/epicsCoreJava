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
import org.epics.pvdata.pv.PVStringArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.PVScalar;
import org.epics.pvdata.pv.PVUnion;
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.property.*;

/**
 * JUnit test for NTNDArrayAttribute.
 * @author dgh
 *
 */
public class NTNDArrayAttributeTest extends NTTestBase
{
    // Test creation of NTNDArrayAttributeBuilder

    public static void testCreateBuilder()
	{
        NTNDArrayAttributeBuilder builder1 = NTNDArrayAttribute.createBuilder();
		assertNotNull(builder1);

        NTNDArrayAttributeBuilder builder2 = NTNDArrayAttribute.createBuilder();		
		assertNotSame(builder1, builder2);
	}

    // Test NTNDArrayAttributes created with Builder

    public static void testNTNDArrayAttribute_BuilderCreated1()
    {
        testNTNDArrayAttribute_BuilderCreatedImpl(new String[] {});
    }

    public static void testNTNDArrayAttribute_BuilderCreated2()
    {
        testNTNDArrayAttribute_BuilderCreatedImpl(new String[] {"timeStamp"});
    }

    public static void testNTNDArrayAttribute_BuilderCreated3()
    {
        testNTNDArrayAttribute_BuilderCreatedImpl(new String[] {"tags", "alarm", "timeStamp"} );
    }

    public static void testNTNDArrayAttribute_BuilderCreated4()
    {
        testNTNDArrayAttribute_BuilderCreatedImpl(new String[] { "alarm", "timeStamp"} );
    }

    public static void testNTNDArrayAttribute_BuilderCreated5()
    {
        testNTNDArrayAttribute_BuilderCreatedImpl(
            new String[] {"tags", "timeStamp"},
            new String[] {"extra1"}, 
            new Field[]  { fieldCreate.createScalar(ScalarType.pvDouble)});
    }


    // Test is_a

    public static void testNTNDArrayAttributeIs_a()
    {
        Structure s = NTNDArrayAttribute.createBuilder().createStructure();
		assertTrue(NTNDArrayAttribute.is_a(s));
    }

    public static void testStructureIs_a()
    {
        testStructureIs_aImpl(NTNDArrayAttribute.URI, true);
        testStructureIs_aImpl("epics:nt/NTAttribute:1.0", true);
        testStructureIs_aImpl("epics:nt/NTAttribute:1.1", true);
        testStructureIs_aImpl("epics:nt/NTAttribute:2.0", false);
        testStructureIs_aImpl("epics:nt/NTAttribute", false);
        testStructureIs_aImpl("nt/NTAttribute:1.0", false);
        testStructureIs_aImpl("NTAttribute:1.0", false);
        testStructureIs_aImpl("NTNDArrayAttribute", false);
        testStructureIs_aImpl("epics:nt/NTScalar:1.0", false);
    }

    // test compatibility - null structures

    public static void testStructureIsCompatibleNull()
    {
        PVStructure pvs = null;
        assertFalse(NTNDArrayAttribute.isCompatible(pvs));
    }

    // test compatibility - compatible structures

    public static void testStructureIsCompatible1a()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTNDArrayAttribute.URI).
                add("name", ScalarType.pvString).
                add("value", fieldCreate.createVariantUnion()).
                add("descriptor", ScalarType.pvString).
                add("sourceType", ScalarType.pvInt).
                add("source", ScalarType.pvString).
                createStructure(),
            true);  
    }

    public static void testStructureIsCompatible1b()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTNDArrayAttribute.URI).
                add("name", ScalarType.pvString).
                add("value", fieldCreate.createVariantUnion()).
                addArray("tags", ScalarType.pvString).
                add("descriptor", ScalarType.pvString).
                add("sourceType", ScalarType.pvInt).
                add("source", ScalarType.pvString).
                add("extra", ScalarType.pvString).
                createStructure(),
            true);
    }

    public static void testStructureIsCompatible1c()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTNDArrayAttribute.URI).
                add("name", ScalarType.pvString).
                add("value", fieldCreate.createVariantUnion()).
                addArray("tags", ScalarType.pvString).
                add("descriptor", ScalarType.pvString).
                add("sourceType", ScalarType.pvInt).
                add("source", ScalarType.pvString).
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
                setId(NTNDArrayAttribute.URI).
                add("name", ScalarType.pvString).
                add("Value", fieldCreate.createVariantUnion()).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2a2()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTNDArrayAttribute.URI).
                add("name", ScalarType.pvString).
                addArray("value", fieldCreate.createVariantUnion()).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2a3()
    {
        Union u = fieldCreate.createFieldBuilder().
           add("x", ScalarType.pvDouble).
           add("y", ScalarType.pvInt).
           createUnion();

        testStructureIsCompatibleImpl( 
            fieldCreate.createFieldBuilder().
                setId(NTNDArrayAttribute.URI).
                add("name", ScalarType.pvString).
                addArray("value", u).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2b1()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTNDArrayAttribute.URI).
                add("name", ScalarType.pvString).
                add("value", fieldCreate.createVariantUnion()).
                add("tags", ScalarType.pvString).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2b2()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTNDArrayAttribute.URI).
                add("name", ScalarType.pvString).
                add("value", fieldCreate.createVariantUnion()).
                addArray("tags", ScalarType.pvInt).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2c()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTNDArrayAttribute.URI).
                add("name", ScalarType.pvString).
                add("value", ntField.createTimeStamp()).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2d1()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTNDArrayAttribute.URI).
                add("name", ScalarType.pvString).
                add("value", fieldCreate.createVariantUnion()).
                add("descriptor", ScalarType.pvInt).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2d2()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTNDArrayAttribute.URI).
                add("name", ScalarType.pvString).
                add("value", fieldCreate.createVariantUnion()).
                add("alarm", ScalarType.pvInt).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2d3()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTNDArrayAttribute.URI).
                add("name", ScalarType.pvString).
                add("value", fieldCreate.createVariantUnion()).
                add("timeStamp", ScalarType.pvInt).
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible1e()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTNDArrayAttribute.URI).
                add("name", ScalarType.pvString).
                add("value", fieldCreate.createVariantUnion()).
                createStructure(),
            false);  
    }

    // test wrapping compatible structures

    public static void testWrappedNTNDArrayAttribute1()
    {
        Structure s = fieldCreate.createFieldBuilder().
            setId(NTNDArrayAttribute.URI).
            add("name", ScalarType.pvString).
            add("value", fieldCreate.createVariantUnion()).
            add("descriptor", ScalarType.pvString).
            add("sourceType", ScalarType.pvInt).
            add("source", ScalarType.pvString).
            createStructure();

        NTNDArrayAttribute ntattribute = NTNDArrayAttribute.wrap(dataCreate.createPVStructure(s));

        ntAttributeChecks(ntattribute, new String[0],
            new String[0], new Field[0]);

        NTNDArrayAttribute ntattribute2 = NTNDArrayAttribute.wrapUnsafe(dataCreate.
            createPVStructure(s));

        ntAttributeChecks(ntattribute2, new String[0],
            new String[0], new Field[0]);
    }


    public static void testWrappedNTNDArrayAttribute2()
    {
        Structure s = fieldCreate.createFieldBuilder().
            setId(NTNDArrayAttribute.URI).
            add("name", ScalarType.pvString).
            add("value", fieldCreate.createVariantUnion()).
            addArray("tags", ScalarType.pvString).
            add("descriptor", ScalarType.pvString).
            add("sourceType", ScalarType.pvInt).
            add("source", ScalarType.pvString).
            add("alarm", ntField.createAlarm()).
            add("timeStamp", ntField.createTimeStamp()).
            addArray("extra", ScalarType.pvString).
            createStructure();

        String[] standardFields = { "tags", "alarm", "timeStamp" };
        String[] extraNames = { "extra" };
        Field[] extraFields = { fieldCreate.createScalarArray(ScalarType.pvString) };

        NTNDArrayAttribute ntattribute = NTNDArrayAttribute.wrap(dataCreate.createPVStructure(s));

        ntAttributeChecks(ntattribute, 
            standardFields,
            extraNames,extraFields);

        NTNDArrayAttribute ntattribute2 = NTNDArrayAttribute.wrapUnsafe(dataCreate.
            createPVStructure(s));

        ntAttributeChecks(ntattribute, 
            standardFields,
            extraNames,extraFields);
    }

    // test standard fields

    // test attaching timeStamps

    public static void testTimeStamp1()
    {
        NTNDArrayAttribute ntattribute = NTNDArrayAttribute.createBuilder().
            addTimeStamp().create();

        testAttachTimeStamp(ntattribute, true);
    }
 
    public static void testTimeStamp2()
    {
        NTNDArrayAttribute ntattribute = NTNDArrayAttribute.createBuilder().create();

        testAttachTimeStamp(ntattribute, false);
    }

    // test attaching alarms

    public static void testAlarm1()
    {
        NTNDArrayAttribute ntattribute = NTNDArrayAttribute.createBuilder().
            addAlarm().create();

        testAttachAlarm(ntattribute, true);
    }

    public static void testAlarm2()
    {
        NTNDArrayAttribute ntattribute = NTNDArrayAttribute.createBuilder().create();

        testAttachAlarm(ntattribute, false);
    }


    public static void testBuilderResets()
    {
        NTNDArrayAttributeBuilder builder = NTNDArrayAttribute.createBuilder();

        Structure s1 = builder.createStructure();

        Structure s2 = builder.
            addTags().
            addDescriptor().
            addTimeStamp().
            addAlarm().
            add("extra", fieldCreate.createScalar(ScalarType.pvInt)).
            createStructure();

        Structure s3 = builder.createStructure();

        Structure s4 = builder.
            addTags().
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
        assertEquals(expected, NTNDArrayAttribute.is_a(s));
    }

    public static void testStructureIsCompatibleImpl(Structure s, boolean expected)
    {
        PVStructure pvSt = dataCreate.createPVStructure(s);
        assertEquals(expected, NTNDArrayAttribute.isCompatible(pvSt));
    }

    private static
    void testNTNDArrayAttribute_BuilderCreatedImpl(String[] standardFields)
    {
        testNTNDArrayAttribute_BuilderCreatedImpl(standardFields, new String[0], new Field[0]);
    }


    private static
    void testNTNDArrayAttribute_BuilderCreatedImpl(String[] standardFields, String[] extraNames, Field[] extraFields)
    {
        NTNDArrayAttribute ntattribute = createNTNDArrayAttribute(standardFields,extraNames,extraFields);

        ntAttributeChecks(ntattribute,standardFields,extraNames,extraFields);        
    }


    private static
    NTNDArrayAttribute createNTNDArrayAttribute(String[] standardFields, String[] extraNames, Field[] extraFields)
    {
        boolean hasTags       = find("tags", standardFields);
        boolean hasTimeStamp  = find("timeStamp", standardFields);
        boolean hasAlarm      = find("alarm", standardFields);

        // Create NTNDArrayAttribute
        NTNDArrayAttributeBuilder builder = NTNDArrayAttribute.createBuilder();
        if (hasTags) builder.addTags();
        builder.addDescriptor();
        if (hasTimeStamp) builder.addTimeStamp();
        if (hasAlarm) builder.addAlarm();

        for (int i = 0; i < extraNames.length; ++i)
            builder.add(extraNames[i], extraFields[i]);

        return builder.create();
    }

    private static
    void ntAttributeChecks(NTNDArrayAttribute ntattribute, String[] standardFields,
        String[] extraNames, Field[] extraFields)
    {
        // parse optional fields
        boolean hasTags       = find("tags", standardFields);
        boolean hasTimeStamp  = find("timeStamp", standardFields);
        boolean hasAlarm      = find("alarm", standardFields);

        // Test value field through NTNDArrayAttribute interface
        PVString pvName = ntattribute.getName();
		assertNotNull(pvName);

        PVUnion pvValue = ntattribute.getValue();
		assertNotNull(pvValue);

        PVString pvDescriptor = ntattribute.getDescriptor();
        assertNotNull(pvDescriptor);

        PVInt pvSourceType = ntattribute.getSourceType();
        assertNotNull(pvSourceType);

        PVString pvSource = ntattribute.getSource();
        assertNotNull(pvSource);

		// Test optional fields through NTNDArrayAttribute interface
        NTField ntField = NTField.get();

        PVStringArray pvTags = ntattribute.getTags();
        if (hasTags)
        {
            assertNotNull(pvTags);
        }
        else
            assertNull(pvTags);

        PVStructure pvTimeStamp = ntattribute.getTimeStamp();
        if (hasTimeStamp)
        {
            assertNotNull(pvTimeStamp);
            assertTrue(ntField.isTimeStamp(pvTimeStamp.getField()));
        }
        else
            assertNull(pvTimeStamp);

        PVStructure pvAlarm = ntattribute.getAlarm();
        if (hasAlarm)
        {
            assertNotNull(pvAlarm);
            assertTrue(ntField.isAlarm(pvAlarm.getField()));
        }
        else
            assertNull(pvAlarm);

        // Test PVStructure from NTNDArrayAttribute
        PVStructure pvStructure = ntattribute.getPVStructure();
        assertTrue(NTNDArrayAttribute.is_a(pvStructure.getStructure()));
        assertTrue(NTNDArrayAttribute.isCompatible(pvStructure));

        assertSame(pvName, pvStructure.getSubField(PVString.class, "name"));
        assertSame(pvValue, pvStructure.getSubField(PVUnion.class, "value"));
        assertSame(pvTags, pvStructure.getSubField(PVStringArray.class, "tags"));

        assertSame(pvDescriptor,pvStructure.getSubField(PVString.class, "descriptor"));
        assertSame(pvTimeStamp, pvStructure.getSubField(PVStructure.class, "timeStamp"));
        assertSame(pvAlarm, pvStructure.getSubField(PVStructure.class, "alarm"));

        assertSame(pvSourceType,pvStructure.getSubField(PVInt.class, "sourceType"));
        assertSame(pvSource,pvStructure.getSubField(PVString.class, "source"));

        for (int i = 0; i < extraNames.length; ++i)
        {
            PVField pvField = pvStructure.getSubField(extraNames[i]);
            assertNotNull(pvField);
            assertSame(pvField.getField(),extraFields[i]);
        }
    }
}

