/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata;

import junit.framework.TestCase;

import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.FieldBuilder;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVDouble;
import org.epics.pvdata.pv.PVInt;
import org.epics.pvdata.pv.PVShort;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.PVUInt;
import org.epics.pvdata.pv.PVUnion;
import org.epics.pvdata.pv.Scalar;
import org.epics.pvdata.pv.ScalarArray;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Structure;
import org.epics.pvdata.pv.StructureArray;
import org.epics.pvdata.pv.Union;
import org.epics.pvdata.pv.UnionArray;

/**
 * JUnit test for Equality.
 * @author dgh
 *
 */
public class EqualityTest extends TestCase {

    private static final FieldCreate fieldCreate = FieldFactory.getFieldCreate();
    private static final PVDataCreate dataCreate = PVDataFactory.getPVDataCreate();

    FieldBuilder fb = fieldCreate.createFieldBuilder();

    // Basic test: Two equal structures.
    // Two structures differing by field name 
    public void testStructure1()
    {
        Structure s1a = fb.
            add("x", ScalarType.pvInt).
            createStructure();

        // same s
        Structure s1b = fb.
            add("x", ScalarType.pvInt).
            createStructure();

        // wrong field name
        Structure s1c = fb.
            add("y", ScalarType.pvInt).
            createStructure();

        assertTrue(s1a.equals(s1a));

        assertTrue(s1a.equals(s1b));
        assertTrue(s1b.equals(s1a));

        assertFalse(s1a.equals(s1c));
        assertFalse(s1c.equals(s1a));


        PVStructure pvStructure1a = dataCreate.createPVStructure(s1a);
        PVStructure pvStructure1b = dataCreate.createPVStructure(s1b);
        PVStructure pvStructure1c = dataCreate.createPVStructure(s1c);

        assertTrue(pvStructure1a.equals(pvStructure1a));

        assertTrue(pvStructure1a.equals(pvStructure1b));
        assertTrue(pvStructure1b.equals(pvStructure1a));

        assertFalse(pvStructure1a.equals(pvStructure1c));
        assertFalse(pvStructure1c.equals(pvStructure1a));
        assertFalse(pvStructure1a.equals(pvStructure1c));

        pvStructure1a.getSubField(PVInt.class, "x").put(42);
        assertFalse(pvStructure1a.equals(pvStructure1b));
        assertFalse(pvStructure1b.equals(pvStructure1a));
    }

    // Test structures differing by field type
    public void testStructure2()
    {
        Structure s2a = fb.
            add("x", ScalarType.pvInt).
            createStructure();

        Structure s2b = fb.
            add("x", ScalarType.pvDouble).
            createStructure();

        Structure s2c = fb.
            addArray("x", ScalarType.pvInt).
            createStructure();

        Structure s2d = fb.
            addNestedStructure("x").
                 add("x", ScalarType.pvInt).
                 endNested().
            createStructure();

        Structure s2e = fb.
            addNestedStructureArray("x").
                 add("x", ScalarType.pvInt).
                 endNested().
            createStructure();

        Structure s2f = fb.
            addNestedUnion("x").
                 add("x", ScalarType.pvInt).
                 endNested().
            createStructure();

        Structure s2g = fb.
            addNestedUnionArray("x").
                 add("x", ScalarType.pvDouble).
                 endNested().
            createStructure();

        Structure s2h = fb.
            add("x", fieldCreate.createVariantUnion()).
            createStructure();

        Structure s2i = fb.
            addArray("x", fieldCreate.createVariantUnion()).
            createStructure();

        Structure[] structures = { s2a, s2b, s2c, s2d, s2e, s2f, s2g, s2i };
        checkDifferentStructures(structures);
    }

    // Test structures differing by type ID
    public void testStructure3()
    {
        Structure s3a = fb.
            setId("xx").
            add("x", ScalarType.pvInt).
            createStructure();

        // same structure
        Structure s3a2 = fb.
            setId("xx").
            add("x", ScalarType.pvInt).
            createStructure();

        // wrong ID
        Structure s3b = fb.
            setId("xxx").
            add("x", ScalarType.pvInt).
            createStructure();

        // wrong ID
        Structure s3c = fb.
            add("x", ScalarType.pvInt).
            createStructure();

        Structure[] structures = { s3a, s3a2 };
        checkEqualStructures(structures);

        Structure[] structures2 = { s3a, s3b, s3c };
        checkDifferentStructures(structures2);
    }

    // Test structures differing by missing or out of order fields
    public void testStructure4()
    {
        Structure s4a = fb.
            add("x", ScalarType.pvInt).
            add("y", ScalarType.pvInt).
            createStructure();

        // same structure
        Structure s4a2 = fb.
            add("x", ScalarType.pvInt).
            add("y", ScalarType.pvInt).
            createStructure();

        Structure s4b = fb.
            add("x", ScalarType.pvInt).
            // missing field
            createStructure();

        Structure s4c = fb.
            // missing field
            add("y", ScalarType.pvInt).
            createStructure();

        Structure s4d = fb.
            // wrong order
            add("y", ScalarType.pvInt).
            add("x", ScalarType.pvInt).
            createStructure();

        Structure[] structures = { s4a, s4a2 };
        checkEqualStructures(structures);

        Structure[] structures2 = { s4a, s4b, s4c, s4d };
        checkDifferentStructures(structures2);
    }

    // test structures differing in structure subfields
    public void testStructure5()
    {
        String NESTED_ID = "nestedID";
        Structure s5a = fb.addNestedStructure("nested").
                             setId(NESTED_ID).
                             add("short", ScalarType.pvShort).
                             add("long", ScalarType.pvLong).
                             endNested().
                         createStructure();

        // same structure
        Structure s5a2 = fb.addNestedStructure("nested").
                             setId(NESTED_ID).
                             add("short", ScalarType.pvShort).
                             add("long", ScalarType.pvLong).
                             endNested().
                         createStructure();

        Structure s5b = fb.
                         addNestedStructure("nested").
                            // missing ID
                             add("short", ScalarType.pvShort).
                             add("long", ScalarType.pvLong).
                             endNested().
                         createStructure();

        Structure s5c = fb.addNestedStructure("nested").
                             setId(NESTED_ID).
                            // wrong order
                             add("long", ScalarType.pvLong).
                             add("short", ScalarType.pvShort).
                             endNested().
                         createStructure();

        Structure s5d = fb.addNestedStructure("nested").
                             setId(NESTED_ID).
                             add("long", ScalarType.pvULong). // wrong scalar type
                             add("short", ScalarType.pvShort).
                             endNested().
                         createStructure();

        Structure s5e = fb.
                         addNestedStructure("nested").
                             setId(NESTED_ID).
                            // missing field
                             add("short", ScalarType.pvShort).
                             endNested().
                         createStructure();

        Structure[] structures = { s5a, s5a2 };
        checkEqualStructures(structures);

        Structure[] structures2 = { s5a, s5b, s5c };
        checkDifferentStructures(structures2);
    }

    // test structures differing in regular union subfield's
    // selected field or value data
    public void testRegularUnion()
    {
        String NESTED_ID = "nestedID";
        Structure s = fb.addNestedUnion("x").
                             setId(NESTED_ID).
                             add("short", ScalarType.pvShort).
                             add("int", ScalarType.pvInt).
                             add("double", ScalarType.pvDouble).
                             add("uint", ScalarType.pvUInt).
                             endNested().
                         createStructure();

        PVStructure pvs1 = dataCreate.createPVStructure(s);

        PVStructure pvs2a = dataCreate.createPVStructure(s);
        pvs2a.getSubField(PVUnion.class, "x").
            select(PVShort.class, "short");

        PVStructure pvs2b = dataCreate.createPVStructure(s);
        pvs2b.getSubField(PVUnion.class, "x").
            select(PVShort.class, "short").put((short)42);

        PVStructure pvs3a = dataCreate.createPVStructure(s);
        pvs3a.getSubField(PVUnion.class, "x").
            select(PVInt.class, "int"); 

        PVStructure pvs3b = dataCreate.createPVStructure(s);
        pvs3b.getSubField(PVUnion.class, "x").
            select(PVInt.class, "int").put(1600);

        PVStructure pvs4a = dataCreate.createPVStructure(s);
        pvs4a.getSubField(PVUnion.class, "x").
            select(PVDouble.class, "double"); 

        PVStructure pvs4b = dataCreate.createPVStructure(s);
        pvs4b.getSubField(PVUnion.class, "x").
            select(PVDouble.class, "double").put(4.669201);

        PVStructure pvs5a = dataCreate.createPVStructure(s);
        pvs5a.getSubField(PVUnion.class, "x").
            select(PVUInt.class, "uint"); 

        PVStructure pvs5b = dataCreate.createPVStructure(s);
        pvs5b.getSubField(PVUnion.class, "x").
            select(PVUInt.class, "uint").put(1600);

        PVStructure[] pvss = { pvs1, pvs2a, pvs2b, pvs3a, pvs3b,
            pvs4a, pvs4b, pvs5a, pvs5b };
        checkDifferentPVStructures(pvss);
    }

    // test structures differing in union subfield introspection type
    public void testRegularUnion2()
    {
        String NESTED_ID = "nestedID";
        Structure sa  = fb.addNestedUnion("x").
                             setId(NESTED_ID).
                             add("int", ScalarType.pvInt).
                             add("double", ScalarType.pvDouble).
                             endNested().
                         createStructure();

        // same structure
        Structure sa2 = fb.addNestedUnion("x").
                             setId(NESTED_ID).
                             add("int", ScalarType.pvInt).
                             add("double", ScalarType.pvDouble).
                             endNested().
                         createStructure();

        Structure sb = fb.addNestedUnion("x").
                             // missing ID
                             add("int", ScalarType.pvInt).
                             add("double", ScalarType.pvDouble).
                             endNested().
                         createStructure();

        Structure sc = fb.addNestedUnion("x").
                             setId(NESTED_ID).
                             add("int2", ScalarType.pvInt). // wrong field name
                             add("double", ScalarType.pvDouble).
                             endNested().
                         createStructure();

        Structure sd  = fb.addNestedUnion("x").
                             setId(NESTED_ID).
                             // wrong order
                             add("double", ScalarType.pvDouble).
                             add("int", ScalarType.pvInt).
                             endNested().
                         createStructure();

        Structure se  = fb.addNestedUnion("x").
                             setId(NESTED_ID).
                             add("double", ScalarType.pvDouble).
                             add("int", ScalarType.pvUInt). // wrong scalar type
                             endNested().
                         createStructure();

        Structure[] structures = { sa, sa2 };
        checkEqualStructures(structures);

        Structure[] structures2 = { sa, sb, sc, sd };
        checkDifferentStructures(structures2);
    }

    // test structures differing in variant union subfield's
    // stored field
    public void testVariantUnion()
    {
        Structure s = fb.
            add("x", fieldCreate.createVariantUnion()).
            createStructure();

        PVStructure pvs1 = dataCreate.createPVStructure(s);

        PVStructure pvs2a = dataCreate.createPVStructure(s);
        {
            PVInt stored = (PVInt)dataCreate.createPVScalar(ScalarType.pvInt);
            pvs2a.getSubField(PVUnion.class, "x").set(stored);
        }

        PVStructure pvs2b = dataCreate.createPVStructure(s);
        {
            PVInt stored = (PVInt)dataCreate.createPVScalar(ScalarType.pvInt);
            stored.put(42);
            pvs2b.getSubField(PVUnion.class, "x").set(stored);
        }

        PVStructure pvs3a = dataCreate.createPVStructure(s);
        {
            PVDouble stored = (PVDouble)dataCreate.createPVScalar(ScalarType.pvDouble);
            pvs3a.getSubField(PVUnion.class, "x").set(stored);
        }

        PVStructure pvs3b = dataCreate.createPVStructure(s);
        {
            PVDouble stored = (PVDouble)dataCreate.createPVScalar(ScalarType.pvDouble);
            stored.put(42.0);
            pvs3b.getSubField(PVUnion.class, "x").set(stored);
        }

        PVStructure[] pvStructures = { pvs1, pvs2a, pvs2b, pvs3a, pvs3b };
        checkDifferentPVStructures(pvStructures);
    }

    void checkEqualStructures(Structure[] structures)
    {
        for (Structure s1 : structures)
        {
            for (Structure s2 : structures)
            {
                String message = String.format("Expected equal:\nLHS: %s\nRHS: %2$s", s1.toString(), s2.toString());
                assertTrue(message, s1.equals(s2));

                PVStructure pvStructure1 = dataCreate.createPVStructure(s1);
                PVStructure pvStructure2 = dataCreate.createPVStructure(s2);

                String message2 = String.format(
                    "Expected equal:\nLHS: %s\nRHS: %2$s",
                    pvStructure1.toString(), pvStructure2.toString());
                assertTrue(message2, pvStructure1.equals(pvStructure2));
            }
        }    
    }

    void checkDifferentStructures(Structure[] structures)
    {
        for (Structure s1 : structures)
        {
            for (Structure s2 : structures)
            {
                if (s1 == s2)
                {
                    String message = String.format(
                        "Expected equal:\nLHS: %s\nRHS: %2$s",
                        s1.toString(), s2.toString());
                    assertTrue(message, s1.equals(s2));
                }
                else
                {
                    String message = String.format(
                        "Expected not equal:\nLHS: %s\nRHS: %2$s",
                        s1.toString(), s2.toString());
                    assertFalse(message, s1.equals(s2));
                }

                PVStructure pvStructure1 = dataCreate.createPVStructure(s1);
                PVStructure pvStructure2 = dataCreate.createPVStructure(s2);

                if (s1 == s2)
                {
                    String message = String.format(
                        "Expected equal:\nLHS: %s\nRHS: %2$s",
                        pvStructure1.toString(), pvStructure2.toString());
                    assertTrue(message, pvStructure1.equals(pvStructure2));
                }
                else
                {
                    String message = String.format(
                        "Expected not equal:\nLHS: %s\nRHS: %2$s",
                        pvStructure1.toString(), pvStructure2.toString());
                    assertFalse(message, pvStructure1.equals(pvStructure2));
                }
            }
        }
    }

    void checkDifferentPVStructures(PVStructure[] pvStructures)
    {
        for (PVStructure pvs1 : pvStructures)
        {
            for (PVStructure pvs2 : pvStructures)
            {
                if (pvs1 == pvs2)
                {
                    String message = String.format(
                        "Expected equal:\nLHS: %s\nRHS: %2$s",
                        pvs1.toString(), pvs2.toString());
                    assertTrue(message, pvs1.equals(pvs2));
                }
                else
                {
                    String message = String.format(
                        "Expected not equal:\nLHS: %s\nRHS: %2$s",
                        pvs1.toString(), pvs2.toString());
                    assertFalse(message, pvs1.equals(pvs2));
                }
            }
        }
    }
}
