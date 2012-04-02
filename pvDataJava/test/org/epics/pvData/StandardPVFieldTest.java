/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData;

import junit.framework.TestCase;

import org.epics.pvData.factory.FieldFactory;
import org.epics.pvData.factory.PVDataFactory;
import org.epics.pvData.factory.StandardPVFieldFactory;
import org.epics.pvData.pv.*;



/**
 * JUnit test for pvAccess.
 * It also provides examples of how to use the pvAccess interfaces.
 * @author mrk
 *
 */
public class StandardPVFieldTest extends TestCase {
    
    private static final FieldCreate fieldCreate = FieldFactory.getFieldCreate();
    private static final PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
    private static final StandardPVField standardPVField = StandardPVFieldFactory.getStandardPVField();
	
    static private void print(String name,String value) {
        System.out.println();
        System.out.println(name);
        System.out.println(value);
    }
    
    public static void testSimple() {     
        PVStructure pvStructure = standardPVField.scalar(null, "value",ScalarType.pvDouble,"alarm,timeStamp.display,control,valueAlarm");
        print("simpleTest",pvStructure.toString());
        PVDouble pvValue = pvStructure.getDoubleField("value");
        pvValue.put(10.0);
        PVInt pvSeverity = pvStructure.getIntField("alarm.severity");
        pvSeverity.put(2);
        PVString pvMessage = pvStructure.getStringField("alarm.message");
        pvMessage.put("test message");
        print("simpleTest",pvStructure.toString());
    }
    
}

