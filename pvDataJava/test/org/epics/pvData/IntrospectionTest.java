/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData;


import junit.framework.TestCase;

import org.epics.pvData.factory.FieldFactory;
import org.epics.pvData.factory.StandardFieldFactory;
import org.epics.pvData.pv.FieldCreate;
import org.epics.pvData.pv.ScalarType;
import org.epics.pvData.pv.StandardField;
import org.epics.pvData.pv.Structure;

/**
 * JUnit test for BitSet.
 * NOTE not complete.
 * @author mse
 *
 */
public class IntrospectionTest extends TestCase {
	private static FieldCreate fieldCreate = FieldFactory.getFieldCreate();
	private static StandardField standardField = StandardFieldFactory.getStandardField();
	
	static private void print(String name,String value) {
		System.out.println(name);
		System.out.println(value);
	}

	public void testIntrospection() {
		Structure doubleValue = standardField.scalar(ScalarType.pvDouble,
				"alarm,timeStamp,display,control,valueAlarm");
		print("doubleValue",doubleValue.toString());
	}
}
