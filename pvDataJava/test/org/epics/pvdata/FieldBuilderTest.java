/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata;

import junit.framework.TestCase;

import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.FieldBuilder;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.Scalar;
import org.epics.pvdata.pv.ScalarArray;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Structure;
import org.epics.pvdata.pv.StructureArray;
import org.epics.pvdata.pv.Union;
import org.epics.pvdata.pv.UnionArray;

/**
 * JUnit test for FieldBuilder.
 * @author mse
 *
 */
public class FieldBuilderTest extends TestCase {

	private static final FieldCreate fieldCreate = FieldFactory.getFieldCreate();
	
	public void testFactory()
	{
		FieldBuilder fb = fieldCreate.createFieldBuilder();
		assertNotNull(fb);
		
		FieldBuilder fb2 = fieldCreate.createFieldBuilder();
		assertNotSame(fb, fb2);
	}
	
	public void testStructure()
	{
		FieldBuilder fb = fieldCreate.createFieldBuilder();
		
		// test with simple (non-nested) structure
		String ID = "testStructureID";
		Structure s = fb.setId(ID).
						 add("double", ScalarType.pvDouble).
						 addArray("intArray", ScalarType.pvInt).
						 createStructure();
		assertNotNull(s);
		assertEquals(ID, s.getID());
		assertEquals(2, s.getFields().length);
		
		Field f0 = s.getField(0);
		assertTrue(f0 instanceof Scalar);
		assertEquals("double", s.getFieldName(0));
		assertEquals(ScalarType.pvDouble, ((Scalar)f0).getScalarType());
		
		Field f1 = s.getField(1);
		assertTrue(f1 instanceof ScalarArray);
		assertEquals("intArray", s.getFieldName(1));
		assertEquals(ScalarType.pvInt, ((ScalarArray)f1).getElementType());
		
		// test reuse with empty structure
		Structure emptyStructure = fb.createStructure();
		assertNotNull(emptyStructure);
		assertEquals(Structure.DEFAULT_ID, emptyStructure.getID());
		assertEquals(0, emptyStructure.getFields().length);
		
		// test add/addArray with Field
		Structure s2 = fb.add("s", s).
		 				  addArray("sArray", s).
		 				  createStructure();
		
		assertNotNull(s2);
		assertEquals(Structure.DEFAULT_ID, s2.getID());
		assertEquals(2, s2.getFields().length);
		
		f0 = s2.getField(0);
		assertTrue(f0 instanceof Structure);
		assertEquals("s", s2.getFieldName(0));
		assertSame(s, f0);
		
		f1 = s2.getField(1);
		assertTrue(f1 instanceof StructureArray);
		assertEquals("sArray", s2.getFieldName(1));
		assertSame(s, ((StructureArray)f1).getStructure());
	}
	
	public void testUnion()
	{
		FieldBuilder fb = fieldCreate.createFieldBuilder();
		
		// test with simple (non-nested) union
		String ID = "testUnionID";
		Union u = fb.setId(ID).
						 add("double", ScalarType.pvDouble).
						 addArray("intArray", ScalarType.pvInt).
						 createUnion();
		assertNotNull(u);
		assertEquals(ID, u.getID());
		assertEquals(2, u.getFields().length);
		
		Field f0 = u.getField(0);
		assertTrue(f0 instanceof Scalar);
		assertEquals("double", u.getFieldName(0));
		assertEquals(ScalarType.pvDouble, ((Scalar)f0).getScalarType());
		
		Field f1 = u.getField(1);
		assertTrue(f1 instanceof ScalarArray);
		assertEquals("intArray", u.getFieldName(1));
		assertEquals(ScalarType.pvInt, ((ScalarArray)f1).getElementType());
		
		// test reuse with empty structure
		Union emptyUnion = fb.createUnion();
		assertNotNull(emptyUnion);
		assertEquals(Union.ANY_ID, emptyUnion.getID());
		assertEquals(0, emptyUnion.getFields().length);
		
		// test add/addArray with Field
		Union u2 = fb.add("u", u).
		 				  addArray("uArray", u).
		 				  createUnion();
		
		assertNotNull(u2);
		assertEquals(Union.DEFAULT_ID, u2.getID());
		assertEquals(2, u2.getFields().length);
		
		f0 = u2.getField(0);
		assertTrue(f0 instanceof Union);
		assertEquals("u", u2.getFieldName(0));
		assertSame(u, f0);
		
		f1 = u2.getField(1);
		assertTrue(f1 instanceof UnionArray);
		assertEquals("uArray", u2.getFieldName(1));
		assertSame(u, ((UnionArray)f1).getUnion());
	}
	
	public void testArraySizeTypes()
	{
		Structure s = fieldCreate.createFieldBuilder().
			addArray("variableArray", ScalarType.pvDouble).
			addFixedArray("fixedArray", ScalarType.pvDouble, 10).
			addBoundedArray("boundedArray", ScalarType.pvDouble, 1024).
				createStructure();
			assertNotNull(s);
			assertEquals(Structure.DEFAULT_ID, s.getID());
			assertEquals(3, s.getFields().length);
	}
	
	public void testNestedStructure()
	{
		String NESTED_ID = "nestedID";
		Structure s = fieldCreate.createFieldBuilder().
		 				add("double", ScalarType.pvDouble).
		 				addNestedStructure("nested").
		 					setId(NESTED_ID).
		 					add("short", ScalarType.pvShort).
		 					add("long", ScalarType.pvLong).
		 					endNested().
		 				addArray("intArray", ScalarType.pvInt).
		 				createStructure();
		assertNotNull(s);
		assertEquals(Structure.DEFAULT_ID, s.getID());
		assertEquals(3, s.getFields().length);
		
		Field f0 = s.getField(0);
		assertTrue(f0 instanceof Scalar);
		assertEquals("double", s.getFieldName(0));
		assertEquals(ScalarType.pvDouble, ((Scalar)f0).getScalarType());
		
		Field f1 = s.getField(1);
		assertTrue(f1 instanceof Structure);
		assertEquals("nested", s.getFieldName(1));

		{
			Structure s2 = (Structure)f1;
			assertEquals(NESTED_ID, s2.getID());
			assertEquals(2, s2.getFields().length);

			Field f20 = s2.getField(0);
			assertTrue(f20 instanceof Scalar);
			assertEquals("short", s2.getFieldName(0));
			assertEquals(ScalarType.pvShort, ((Scalar)f20).getScalarType());

			Field f21 = s2.getField(1);
			assertTrue(f21 instanceof Scalar);
			assertEquals("long", s2.getFieldName(1));
			assertEquals(ScalarType.pvLong, ((Scalar)f21).getScalarType());
		}
		
		Field f2 = s.getField(2);
		assertTrue(f2 instanceof ScalarArray);
		assertEquals("intArray", s.getFieldName(2));
		assertEquals(ScalarType.pvInt, ((ScalarArray)f2).getElementType());
	}	
	
	public void testNestedStructureArray()
	{
		String NESTED_ID = "nestedID";
		Structure s = fieldCreate.createFieldBuilder().
		 				add("double", ScalarType.pvDouble).
		 				addNestedStructureArray("nested").
		 					setId(NESTED_ID).
		 					add("short", ScalarType.pvShort).
		 					add("long", ScalarType.pvLong).
		 					endNested().
		 				addArray("intArray", ScalarType.pvInt).
		 				createStructure();
		assertNotNull(s);
		assertEquals(Structure.DEFAULT_ID, s.getID());
		assertEquals(3, s.getFields().length);
		
		Field f0 = s.getField(0);
		assertTrue(f0 instanceof Scalar);
		assertEquals("double", s.getFieldName(0));
		assertEquals(ScalarType.pvDouble, ((Scalar)f0).getScalarType());
		
		Field f1 = s.getField(1);
		assertTrue(f1 instanceof StructureArray);
		assertEquals("nested", s.getFieldName(1));

		{
			Structure s2 = ((StructureArray)f1).getStructure();
			assertEquals(NESTED_ID, s2.getID());
			assertEquals(2, s2.getFields().length);

			Field f20 = s2.getField(0);
			assertTrue(f20 instanceof Scalar);
			assertEquals("short", s2.getFieldName(0));
			assertEquals(ScalarType.pvShort, ((Scalar)f20).getScalarType());

			Field f21 = s2.getField(1);
			assertTrue(f21 instanceof Scalar);
			assertEquals("long", s2.getFieldName(1));
			assertEquals(ScalarType.pvLong, ((Scalar)f21).getScalarType());
		}
		
		Field f2 = s.getField(2);
		assertTrue(f2 instanceof ScalarArray);
		assertEquals("intArray", s.getFieldName(2));
		assertEquals(ScalarType.pvInt, ((ScalarArray)f2).getElementType());
	}	

	public void testNestedUnion()
	{
		String NESTED_ID = "nestedID";
		Union u = fieldCreate.createFieldBuilder().
		 				add("double", ScalarType.pvDouble).
		 				addNestedUnion("nested").
		 					setId(NESTED_ID).
		 					add("short", ScalarType.pvShort).
		 					add("long", ScalarType.pvLong).
		 					endNested().
		 				addArray("intArray", ScalarType.pvInt).
		 				createUnion();
		assertNotNull(u);
		assertEquals(Union.DEFAULT_ID, u.getID());
		assertEquals(3, u.getFields().length);
		
		Field f0 = u.getField(0);
		assertTrue(f0 instanceof Scalar);
		assertEquals("double", u.getFieldName(0));
		assertEquals(ScalarType.pvDouble, ((Scalar)f0).getScalarType());
		
		Field f1 = u.getField(1);
		assertTrue(f1 instanceof Union);
		assertEquals("nested", u.getFieldName(1));

		{
			Union u2 = (Union)f1;
			assertEquals(NESTED_ID, u2.getID());
			assertEquals(2, u2.getFields().length);

			Field f20 = u2.getField(0);
			assertTrue(f20 instanceof Scalar);
			assertEquals("short", u2.getFieldName(0));
			assertEquals(ScalarType.pvShort, ((Scalar)f20).getScalarType());

			Field f21 = u2.getField(1);
			assertTrue(f21 instanceof Scalar);
			assertEquals("long", u2.getFieldName(1));
			assertEquals(ScalarType.pvLong, ((Scalar)f21).getScalarType());
		}
		
		Field f2 = u.getField(2);
		assertTrue(f2 instanceof ScalarArray);
		assertEquals("intArray", u.getFieldName(2));
		assertEquals(ScalarType.pvInt, ((ScalarArray)f2).getElementType());
	}
	
	public void testNestedUnionArray()
	{
		String NESTED_ID = "nestedID";
		Union u = fieldCreate.createFieldBuilder().
		 				add("double", ScalarType.pvDouble).
		 				addNestedUnionArray("nested").
		 					setId(NESTED_ID).
		 					add("short", ScalarType.pvShort).
		 					add("long", ScalarType.pvLong).
		 					endNested().
		 				addArray("intArray", ScalarType.pvInt).
		 				createUnion();
		assertNotNull(u);
		assertEquals(Union.DEFAULT_ID, u.getID());
		assertEquals(3, u.getFields().length);
		
		Field f0 = u.getField(0);
		assertTrue(f0 instanceof Scalar);
		assertEquals("double", u.getFieldName(0));
		assertEquals(ScalarType.pvDouble, ((Scalar)f0).getScalarType());
		
		Field f1 = u.getField(1);
		assertTrue(f1 instanceof UnionArray);
		assertEquals("nested", u.getFieldName(1));

		{
			Union u2 = ((UnionArray)f1).getUnion();
			assertEquals(NESTED_ID, u2.getID());
			assertEquals(2, u2.getFields().length);

			Field f20 = u2.getField(0);
			assertTrue(f20 instanceof Scalar);
			assertEquals("short", u2.getFieldName(0));
			assertEquals(ScalarType.pvShort, ((Scalar)f20).getScalarType());

			Field f21 = u2.getField(1);
			assertTrue(f21 instanceof Scalar);
			assertEquals("long", u2.getFieldName(1));
			assertEquals(ScalarType.pvLong, ((Scalar)f21).getScalarType());
		}
		
		Field f2 = u.getField(2);
		assertTrue(f2 instanceof ScalarArray);
		assertEquals("intArray", u.getFieldName(2));
		assertEquals(ScalarType.pvInt, ((ScalarArray)f2).getElementType());
	}

	public void testInvalid()
	{
		try 
		{
			fieldCreate.createFieldBuilder()
				.add("f1", ScalarType.pvByte)
				.endNested();
			fail("createNested() allowed in non-nested FieldBuilder");
		} catch (IllegalStateException ise) {
			// ok
		}

		try 
		{
			fieldCreate.createFieldBuilder()
				.add("f1", ScalarType.pvByte)
				.addNestedStructure("nested")
					.add("n1", ScalarType.pvUInt)
					.createStructure();
			fail("createStructure() allowed in nested FieldBuilder");
		} catch (IllegalStateException ise) {
			// ok
		}
	}

	public void testBadFieldName()
	{
		try {
			fieldCreate.createFieldBuilder()
				.add("ok", ScalarType.pvByte)
				.add("0bad", ScalarType.pvBoolean)
				.createStructure();
			fail("Missed expected exception");
		} catch (IllegalArgumentException ex) {
			// ok
		}

		try {
			fieldCreate.createFieldBuilder()
				.add("ok", ScalarType.pvByte)
				.add("also.bad", ScalarType.pvBoolean)
				.createUnion();
			fail("Missed expected exception");
		} catch (IllegalArgumentException ex) {
			// ok
		}
	}
}
