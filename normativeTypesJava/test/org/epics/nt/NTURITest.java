/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.nt;

import junit.framework.TestCase;
import org.junit.Assert;

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
 * JUnit test for NTURI.
 * @author dgh
 *
 */
public class NTURITest extends NTTestBase
{
    // Test creation of NTURIBuilder

    public static void testCreateBuilder()
	{
        NTURIBuilder builder1 = NTURI.createBuilder();
		assertNotNull(builder1);

        NTURIBuilder builder2 = NTURI.createBuilder();		
		assertNotSame(builder1, builder2);
	}

    // Test NTURIs created with Builder

    public static void testNTURI_BuilderCreated1()
    {
        testNTURI_BuilderCreatedImpl(new String[] {"entity", "period", "count"},
            new ScalarType[] {ScalarType.pvString, ScalarType.pvDouble,
                ScalarType.pvInt});
    }

    public static void testNTURI_BuilderCreated2()
    {
        testNTURI_BuilderCreatedImpl(new String[] {"entity", "period", "count"},
            new ScalarType[] {ScalarType.pvString, ScalarType.pvDouble,
                ScalarType.pvInt}, true);
    }

    public static void testNTURI_BuilderCreated3()
    {
        testNTURI_BuilderCreatedImpl(new String[] {"entity", "period", "count"},
            new ScalarType[] {ScalarType.pvString, ScalarType.pvDouble,
            ScalarType.pvInt}, true, new String[] {"extra1"},
            new Field[]  { fieldCreate.createScalar(ScalarType.pvDouble)});
    }


    // Test is_a

    public static void testNTURIIs_a()
    {
        Structure s = NTURI.createBuilder().createStructure();
		assertTrue(NTURI.is_a(s));
    }

    public static void testStructureIs_a()
    {
        testStructureIs_aImpl(NTURI.URI, true);
        testStructureIs_aImpl("epics:nt/NTURI:1.0", true);
        testStructureIs_aImpl("epics:nt/NTURI:1.1", true);
        testStructureIs_aImpl("epics:nt/NTURI:2.0", false);
        testStructureIs_aImpl("epics:nt/NTURI", false);
        testStructureIs_aImpl("nt/NTURI:1.0", false);
        testStructureIs_aImpl("NTURI:1.0", false);
        testStructureIs_aImpl("NTURI", false);
        testStructureIs_aImpl("epics:nt/NTTable:1.0", false);
    }

    // test compatibility - compatible structures

    public static void testStructureIsCompatible1a()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTURI.URI).
                add("scheme", ScalarType.pvString).
                add("path", ScalarType.pvString).
                createStructure(),
            true);  
    }

    public static void testStructureIsCompatible1b()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTURI.URI).
                add("scheme", ScalarType.pvString).
                add("authority", ScalarType.pvString).
                add("path", ScalarType.pvString).
                createStructure(),
            true);  
    }

    public static void testStructureIsCompatible1c()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTURI.URI).
                add("scheme", ScalarType.pvString).
                add("authority", ScalarType.pvString).
                add("path", ScalarType.pvString).
                addNestedStructure("query").
                    add("name", ScalarType.pvString).
                    add("start", ScalarType.pvInt).
                    add("end", ScalarType.pvInt).
                endNested().
                createStructure(),
            true);  
    }

    public static void testStructureIsCompatible1d()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTURI.URI).
                add("scheme", ScalarType.pvString).
                add("authority", ScalarType.pvString).
                add("path", ScalarType.pvString).
                addNestedStructure("query").
                    add("name", ScalarType.pvString).
                    add("start", ScalarType.pvInt).
                    add("end", ScalarType.pvInt).
                endNested().
                add("extra", ScalarType.pvString).               
                createStructure(),
            true);  
    }

    

    // test compatibility - incompatible structures

    public static void testStructureIsCompatible2a1()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTURI.URI).
                add("scheme", ScalarType.pvString).
                addNestedStructure("query").
                    add("name", ScalarType.pvString).
                    add("start", ScalarType.pvInt).
                    add("end", ScalarType.pvInt).
                endNested().
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2a2()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTURI.URI).
                add("path", ScalarType.pvString).
                addNestedStructure("query").
                    add("name", ScalarType.pvString).
                    add("start", ScalarType.pvInt).
                    add("end", ScalarType.pvInt).
                endNested().
                createStructure(),
            false);
    }

    public static void testStructureIsCompatible2b1()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTURI.URI).
                add("scheme", ScalarType.pvInt).
                add("authority", ScalarType.pvString).
                add("path", ScalarType.pvString).
                addNestedStructure("query").
                    add("name", ScalarType.pvString).
                    add("start", ScalarType.pvInt).
                    add("end", ScalarType.pvInt).
                endNested().
                createStructure(),
            false);  
    }

    public static void testStructureIsCompatible2b2()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTURI.URI).
                add("scheme", ScalarType.pvString).
                add("authority", ScalarType.pvString).
                add("path", ScalarType.pvInt).
                addNestedStructure("query").
                    add("name", ScalarType.pvString).
                    add("start", ScalarType.pvInt).
                    add("end", ScalarType.pvInt).
                endNested().
                createStructure(),
            false);  
    }

    public static void testStructureIsCompatible2b3()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTURI.URI).
                add("scheme", ScalarType.pvString).
                add("authority", ScalarType.pvInt).
                add("path", ScalarType.pvString).
                addNestedStructure("query").
                    add("name", ScalarType.pvString).
                    add("start", ScalarType.pvInt).
                    add("end", ScalarType.pvInt).
                endNested().
                createStructure(),
            false);  
    }

    public static void testStructureIsCompatible2b4()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTURI.URI).
                add("scheme", ScalarType.pvString).
                add("path", ScalarType.pvString).
                add("query", ScalarType.pvString).
                createStructure(),
            false);  
    }

    public static void testStructureIsCompatible2c1()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTURI.URI).
                add("scheme", ScalarType.pvString).
                add("path", ScalarType.pvString).
                addNestedStructure("query").
                    add("name", ScalarType.pvString).
                    add("start", ScalarType.pvLong).
                    add("end", ScalarType.pvLong).
                endNested().
                createStructure(),
            false);  
    }

    public static void testStructureIsCompatible2c2()
    {
        testStructureIsCompatibleImpl(
            fieldCreate.createFieldBuilder().
                setId(NTURI.URI).
                add("scheme", ScalarType.pvString).
                add("path", ScalarType.pvString).
                addNestedStructure("query").
                    addArray("name", ScalarType.pvString).
                endNested().
                createStructure(),
            false);  
    }

    
    // test wrapping compatible structures

    public static void testWrappedNTURI1()
    {
        Structure s = fieldCreate.createFieldBuilder().
            setId(NTURI.URI).
            add("scheme", ScalarType.pvString).
            add("path", ScalarType.pvString).
            createStructure();

        NTURI nturi = NTURI.wrap(dataCreate.createPVStructure(s));

        nturiChecks(nturi, new String[0], new ScalarType[0],
            false, new String[0], new Field[0]);

        NTURI nturi2 = NTURI.wrapUnsafe(dataCreate.
            createPVStructure(s));

        nturiChecks(nturi2, new String[0], new ScalarType[0],
            false, new String[0], new Field[0]);
    }


    /*public static void testWrappedNTURI2()
    {
        Structure s = fieldCreate.createFieldBuilder().
            setId(NTURI.URI).
            add("value", ntField.createEnumerated()).
            add("descriptor", ScalarType.pvString).
            add("alarm", ntField.createAlarm()).
            add("timeStamp", ntField.createTimeStamp()).
            addArray("extra", ScalarType.pvString).
            createStructure();

        String[] standardFields = { "descriptor", "alarm", "timeStamp" };
        String[] extraNames = { "extra" };
        Field[] extraFields = { fieldCreate.createScalarArray(ScalarType.pvString) };

        NTURI nturi = NTURI.wrap(dataCreate.createPVStructure(s));

        nturiChecks(nturi, 
            standardFields,
            extraNames,extraFields);

        NTURI nturi2 = NTURI.wrapUnsafe(dataCreate.
            createPVStructure(s));

        nturiChecks(nturi, 
            standardFields,
            extraNames,extraFields);
    }*/

    public static void testBuilderResets()
    {
        NTURIBuilder builder = NTURI.createBuilder();

        Structure s1 = builder.createStructure();

        Structure s2 = builder.
            addAuthority().
            addQueryString("entity").
            addQueryDouble("period").
            addQueryInt("count").
            add("extra", fieldCreate.createScalar(ScalarType.pvInt)).
            createStructure();

        Structure s3 = builder.createStructure();

        Structure s4 = builder.
            addAuthority().
            addQueryString("entity").
            addQueryDouble("period").
            addQueryInt("count").
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
        assertEquals(expected, NTURI.is_a(s));
    }

    public static void testStructureIsCompatibleImpl(Structure s, boolean expected)
    {
        PVStructure pvSt = dataCreate.createPVStructure(s);
        assertEquals(expected, NTURI.isCompatible(pvSt));
    }

    private static
    void testNTURI_BuilderCreatedImpl(String[] queryNames, ScalarType[] queryTypes)
    {
        testNTURI_BuilderCreatedImpl(queryNames,queryTypes,
            false, new String[0],new Field[0]);
    }

    private static
    void testNTURI_BuilderCreatedImpl(String[] queryNames, ScalarType[] queryTypes,
        boolean authority)
    {
        testNTURI_BuilderCreatedImpl(queryNames,queryTypes,
            authority, new String[0], new Field[0]);
    }

    private static
    void testNTURI_BuilderCreatedImpl(String[] queryNames, ScalarType[] queryTypes, boolean authority, String[] extraNames, Field[] extraFields)
    {
        NTURI nturi = createNTURI(queryNames,queryTypes,authority,
            extraNames, extraFields);

        nturiChecks(nturi,queryNames,queryTypes,authority,
            extraNames, extraFields);        
    }


    private static
    NTURI createNTURI(String[] queryNames, ScalarType[] queryTypes, boolean authority, String[] extraNames, Field[] extraFields)
    {
        // Create NTURI
        NTURIBuilder builder = NTURI.createBuilder();

        if (authority) builder.addAuthority();

        for (int i = 0; i < queryNames.length; ++i)
        {
            switch (queryTypes[i])
            {
                case pvString:
                    builder.addQueryString(queryNames[i]);
                    break;

                case pvDouble:
                    builder.addQueryDouble(queryNames[i]);
                    break;
                case pvInt:
                    builder.addQueryInt(queryNames[i]);
                    break;
                default:
                    throw new RuntimeException("Illegal scalar type");
            }
        }

        for (int i = 0; i < extraNames.length; ++i)
            builder.add(extraNames[i], extraFields[i]);

        return builder.create();
    }

    private static
    void nturiChecks(NTURI nturi, String[] queryNames, ScalarType[] queryTypes, boolean authority, String[] extraNames, Field[] extraFields)
    {
        // Test value field through NTURI interface
        PVString pvScheme = nturi.getScheme();
		assertNotNull(pvScheme);

        PVString pvPath = nturi.getPath();
		assertNotNull(pvPath);

		// Test optional fields through NTURI interface

        PVString pvAuthority = nturi.getAuthority();
        if (authority)
        {
            assertNotNull(pvAuthority);
        }
        else
            assertNull(pvAuthority);

        PVStructure pvQuery = nturi.getQuery();
        if (queryNames.length > 0)
		    assertNotNull(pvQuery);

        if (queryNames != null)
        {
            // To do check query fields
            String[] qNames = nturi.getQueryNames();
            Assert.assertArrayEquals(queryNames,qNames);

            for (String name : qNames)
                assertNotNull(nturi.getQueryField(name));

            for (int i = 0; i < qNames.length; ++i)
            {
                PVScalar qF = nturi.getQueryField(qNames[i]);
                assertNotNull(qF);
                assertEquals(queryTypes[i], qF.getScalar().getScalarType());
            }

        }
        // Test PVStructure from NTURI
        PVStructure pvStructure = nturi.getPVStructure();
        assertTrue(NTURI.is_a(pvStructure.getStructure()));
        assertTrue(NTURI.isCompatible(pvStructure));

        assertSame(pvScheme, pvStructure.getSubField(PVString.class, "scheme"));
        assertSame(pvAuthority,pvStructure.getSubField(PVString.class,"authority"));
        assertSame(pvPath, pvStructure.getSubField(PVString.class, "path"));
        assertSame(pvQuery, pvStructure.getSubField(PVStructure.class, "query"));

        for (int i = 0; i < extraNames.length; ++i)
        {
            PVField pvField = pvStructure.getSubField(extraNames[i]);
            assertNotNull(pvField);
            assertSame(pvField.getField(),extraFields[i]);
        }
    }
}

