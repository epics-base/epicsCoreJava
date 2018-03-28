/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata;

import junit.framework.TestCase;

import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.Structure;



/**
 * JUnit test for pvAccess.
 * It also provides examples of how to use the pvAccess interfaces.
 * @author mrk
 *
 */
public class PVAllTypesTest extends TestCase {
    
    private static final FieldCreate fieldCreate = FieldFactory.getFieldCreate();
    private static final PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
	
	
    public static void testSimple() {     
        Structure allScalar = CreateTestStructure.allScalar();
        Structure allArray = CreateTestStructure.allScalarArray();
        String[] scalarNames = allScalar.getFieldNames();
        Field[] scalarFields = allScalar.getFields();
        String[] arrayNames = allArray.getFieldNames();
        Field[] arrayFields = allArray.getFields();
        int numscalar = scalarNames.length;
        int numarray = arrayNames.length;
        int num = numscalar + numarray;
        String[] names = new String[num];
        Field[] fields = new Field[num];
        for(int i=0; i<numscalar; i++) {
            names[i] = scalarNames[i];
            fields[i] = scalarFields[i];
        }
        for(int i=0; i<numarray; i++) {
            names[i+numscalar] = arrayNames[i];
            fields[i+numscalar] = arrayFields[i];
        }
        Structure structure = fieldCreate.createStructure(names, fields);
        PVStructure pvStructure = pvDataCreate.createPVStructure(structure);
        System.out.println(pvStructure);
    }
    
}

