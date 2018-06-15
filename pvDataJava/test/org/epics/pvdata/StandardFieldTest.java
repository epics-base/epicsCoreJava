/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata;


import junit.framework.TestCase;

import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.StandardFieldFactory;
import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.StandardField;
import org.epics.pvdata.pv.Structure;

/**
 * JUnit test for BitSet.
 * NOTE not complete.
 * @author mse
 *
 */
public class StandardFieldTest extends TestCase {
	private static FieldCreate fieldCreate = FieldFactory.getFieldCreate();
	private static StandardField standardField = StandardFieldFactory.getStandardField();
	
	static private void print(String name,String value) {
	    System.out.println();
		System.out.println(name);
		System.out.println(value);
	}

	public void testStandardField() {
		Structure doubleValue = standardField.scalar(ScalarType.pvDouble,
				"alarm,timeStamp,display,control,valueAlarm");
		print("doubleValue",doubleValue.toString());
		Structure stringArrayValue = standardField.scalarArray(ScalarType.pvString, "alarm,timeStamp");
		print("stringArrayValue",stringArrayValue.toString());
		Structure enumeratedValue = standardField.enumerated("alarm,timeStamp,valueAlarm");
		print("enumeratedValue",enumeratedValue.toString());
		Field[] fields = new Field[2];
        fields[0] = fieldCreate.createScalar(ScalarType.pvDouble);
        fields[1] = fieldCreate.createScalarArray(ScalarType.pvDouble);
        String[] fieldNames = new String[2];
        fieldNames[0] = "scalarValue";
        fieldNames[1] = "arrayValue";
        Structure structure = fieldCreate.createStructure(fieldNames, fields);
        Structure structureArrayValue = standardField.structureArray(structure, "alarm,timeStamp");
        print("structureArrayValue",structureArrayValue.toString());
	}
}
