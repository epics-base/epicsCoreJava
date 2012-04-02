/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData;


import junit.framework.TestCase;



import org.epics.pvData.factory.FieldFactory;
import org.epics.pvData.factory.StandardFieldFactory;
import org.epics.pvData.pv.*;

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
		Structure enumeratedValue = standardField.enumerated("alarm,timeStamp,enumeratedAtarm");
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
