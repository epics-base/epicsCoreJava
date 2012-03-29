/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData;


import junit.framework.TestCase;

import org.epics.pvData.factory.*;
import org.epics.pvData.misc.BitSet;
import org.epics.pvData.pv.DeserializableControl;
import org.epics.pvData.pv.*;
import org.epics.pvData.pv.SerializableControl;

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
