/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata;

import junit.framework.TestCase;

import org.epics.pvdata.factory.StandardPVFieldFactory;
import org.epics.pvdata.pv.IntArrayData;
import org.epics.pvdata.pv.PVIntArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.StandardPVField;



/**
 * JUnit test for pvAccess.
 * It also provides examples of how to use the pvAccess interfaces.
 * @author mrk
 *
 */
public class PVArrayEqualsTest extends TestCase {
    private static final StandardPVField standardPVField = StandardPVFieldFactory.getStandardPVField();
	
    static private void print(String name,String value) {
        System.out.println();
        System.out.println(name);
        System.out.println(value);
    }
        
    public static void testArrayEquals() {  
        int len = 5;
        PVStructure pvStructureA = standardPVField.scalarArray(ScalarType.pvInt,"alarm,timeStamp");
        PVIntArray pvValueA = (PVIntArray)pvStructureA.getScalarArrayField("value",ScalarType.pvInt);
        PVStructure pvStructureB = standardPVField.scalarArray(ScalarType.pvInt,"alarm,timeStamp");
        PVIntArray pvValueB = (PVIntArray)pvStructureB.getScalarArrayField("value",ScalarType.pvInt);
        IntArrayData data = new IntArrayData();
        pvValueA.setCapacity(len);
        pvValueA.setLength(len);
        pvValueA.get(0, len, data);
        for(int i=0; i<len; i++) data.data[i] = i;
        pvValueB.setCapacity(len);
        pvValueB.setLength(len);
        pvValueB.get(0, len, data);
        for(int i=0; i<len; i++) data.data[i] = i;
        boolean isEqual = pvValueA.equals(pvValueB);
        System.out.println("isEqual " + isEqual);
        for(int i=0; i<len; i++) data.data[i] = i+1;
        isEqual = pvValueA.equals(pvValueB);
        System.out.println("isEqual " + isEqual);
        print("testArrayEquals A",pvStructureA.toString());
        print("testArrayEquals B",pvStructureB.toString());

    }
}

