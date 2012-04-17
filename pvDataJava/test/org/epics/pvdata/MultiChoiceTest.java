/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata;

import junit.framework.TestCase;

import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.misc.MultiChoice;
import org.epics.pvdata.misc.MultiChoiceFactory;
import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVStringArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarArray;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Structure;



/**
 * JUnit test for pvAccess.
 * It also provides examples of how to use the pvAccess interfaces.
 * @author mrk
 *
 */
public class MultiChoiceTest extends TestCase {
    
    private static final FieldCreate fieldCreate = FieldFactory.getFieldCreate();
    private static final PVDataCreate dataCreate = PVDataFactory.getPVDataCreate();
   
    
    /**
     * test structure.
     */
    public static void testMultiString() {
        ScalarArray bitMask = fieldCreate.createScalarArray(ScalarType.pvByte);
    	ScalarArray choices =fieldCreate.createScalarArray(ScalarType.pvString);
    	Field[] fields = new Field[]{bitMask,choices};
    	String[] fieldNames = {"bitMask","choices"};
    	Structure structure = fieldCreate.createStructure(fieldNames, fields);
    	PVStructure pvMultiChoice = dataCreate.createPVStructure(null, structure);
    	MultiChoice multiChoice = MultiChoiceFactory.getMultiChoice(pvMultiChoice);
    	assertTrue(multiChoice!=null);
    	PVField pvField = pvMultiChoice.getScalarArrayField("choices", ScalarType.pvString);
    	assertTrue(pvField!=null);
    	String[] testStrings = new String[17];
    	int length = testStrings.length;
    	for(int i=0; i<length; i++) testStrings[i] = "str" + i;
    	PVStringArray pvsa = (PVStringArray)pvField;
    	pvsa.setCapacity(testStrings.length/2);
    	int[] inds = new int[length+1];
    	for(int i=0; i<length; i++) inds[i] = multiChoice.registerChoice(testStrings[i]);
    	inds[length] = multiChoice.registerChoice(testStrings[0]);
    	assertTrue(inds[0]==inds[length]);
    	byte[] bitSet = multiChoice.getBitMask();
    	assertTrue(bitSet.length==3);
    	multiChoice.clear();
    	multiChoice.setBit(inds[0]);
    	multiChoice.setBit(inds[9]);
    	System.out.printf("bitSet %x %x%n",bitSet[0],bitSet[1]);
    	MultiChoice.Choices mcs = multiChoice.getSelectedChoices();
    	int n = mcs.getNumberChoices();
    	assertTrue(n==2);
    	String[] values = mcs.getChoices();
    	assertTrue(values[0]==testStrings[inds[0]]);
    	assertTrue(values[1]==testStrings[inds[9]]);
    }
    
}

