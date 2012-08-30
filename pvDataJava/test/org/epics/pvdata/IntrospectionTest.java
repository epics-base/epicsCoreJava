/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata;


import junit.framework.TestCase;

import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.Scalar;
import org.epics.pvdata.pv.ScalarArray;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Structure;
import org.epics.pvdata.pv.StructureArray;
import org.epics.pvdata.pv.Type;

/**
 * JUnit test for BitSet.
 * NOTE not complete.
 * @author mse
 *
 */
public class IntrospectionTest extends TestCase {
	private static FieldCreate fieldCreate = FieldFactory.getFieldCreate();
	

	public void testIntrospection() {
	    Scalar scalar = fieldCreate.createScalar(ScalarType.pvBoolean);
	    assertTrue(scalar.getType()==Type.scalar);
	    assertTrue(scalar.getScalarType()==ScalarType.pvBoolean);
	    scalar = fieldCreate.createScalar(ScalarType.pvByte);
        assertTrue(scalar.getType()==Type.scalar);
        assertTrue(scalar.getScalarType()==ScalarType.pvByte);
        scalar = fieldCreate.createScalar(ScalarType.pvShort);
        assertTrue(scalar.getType()==Type.scalar);
        assertTrue(scalar.getScalarType()==ScalarType.pvShort);
        scalar = fieldCreate.createScalar(ScalarType.pvInt);
        assertTrue(scalar.getType()==Type.scalar);
        assertTrue(scalar.getScalarType()==ScalarType.pvInt);
        scalar = fieldCreate.createScalar(ScalarType.pvLong);
        assertTrue(scalar.getType()==Type.scalar);
        assertTrue(scalar.getScalarType()==ScalarType.pvLong);
        scalar = fieldCreate.createScalar(ScalarType.pvFloat);
        assertTrue(scalar.getType()==Type.scalar);
        assertTrue(scalar.getScalarType()==ScalarType.pvFloat);
        scalar = fieldCreate.createScalar(ScalarType.pvDouble);
        assertTrue(scalar.getType()==Type.scalar);
        assertTrue(scalar.getScalarType()==ScalarType.pvDouble);
        scalar = fieldCreate.createScalar(ScalarType.pvString);
        assertTrue(scalar.getType()==Type.scalar);
        assertTrue(scalar.getScalarType()==ScalarType.pvString);
        
        ScalarArray scalarArray = fieldCreate.createScalarArray(ScalarType.pvBoolean);
        assertTrue(scalarArray.getType()==Type.scalarArray);
        assertTrue(scalarArray.getElementType()==ScalarType.pvBoolean);
        scalarArray = fieldCreate.createScalarArray(ScalarType.pvByte);
        assertTrue(scalarArray.getType()==Type.scalarArray);
        assertTrue(scalarArray.getElementType()==ScalarType.pvByte);
        scalarArray = fieldCreate.createScalarArray(ScalarType.pvShort);
        assertTrue(scalarArray.getType()==Type.scalarArray);
        assertTrue(scalarArray.getElementType()==ScalarType.pvShort);
        scalarArray = fieldCreate.createScalarArray(ScalarType.pvInt);
        assertTrue(scalarArray.getType()==Type.scalarArray);
        assertTrue(scalarArray.getElementType()==ScalarType.pvInt);
        scalarArray = fieldCreate.createScalarArray(ScalarType.pvLong);
        assertTrue(scalarArray.getType()==Type.scalarArray);
        assertTrue(scalarArray.getElementType()==ScalarType.pvLong);
        scalarArray = fieldCreate.createScalarArray(ScalarType.pvFloat);
        assertTrue(scalarArray.getType()==Type.scalarArray);
        assertTrue(scalarArray.getElementType()==ScalarType.pvFloat);
        scalarArray = fieldCreate.createScalarArray(ScalarType.pvDouble);
        assertTrue(scalarArray.getType()==Type.scalarArray);
        assertTrue(scalarArray.getElementType()==ScalarType.pvDouble);
        scalarArray = fieldCreate.createScalarArray(ScalarType.pvString);
        assertTrue(scalarArray.getType()==Type.scalarArray);
        assertTrue(scalarArray.getElementType()==ScalarType.pvString);
		Field[] fields = new Field[2];
		fields[0] = fieldCreate.createScalar(ScalarType.pvDouble);
		fields[1] = fieldCreate.createScalarArray(ScalarType.pvDouble);
		String[] fieldNames = new String[2];
		fieldNames[0] = "scalarValue";
		fieldNames[1] = "arrayValue";
		Structure structure = fieldCreate.createStructure(fieldNames, fields);
		System.out.println(structure);
		StructureArray structureArray = fieldCreate.createStructureArray(structure);
		System.out.println(structureArray);
		fields = new Field[2];
		fields[0] = structure;
		fields[1] = structureArray;
		fieldNames = new String[2];
		fieldNames[0] = "structureValue";
		fieldNames[1] = "structureArrayValue";
		structure = fieldCreate.createStructure(fieldNames, fields);
		System.out.println(structure);
	}
}
