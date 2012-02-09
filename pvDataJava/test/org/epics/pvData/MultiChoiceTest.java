/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData;

import junit.framework.TestCase;

import org.epics.pvData.factory.FieldFactory;
import org.epics.pvData.factory.PVDataFactory;
import org.epics.pvData.misc.MultiChoice;
import org.epics.pvData.misc.MultiChoiceFactory;
import org.epics.pvData.pv.Field;
import org.epics.pvData.pv.FieldCreate;
import org.epics.pvData.pv.PVDataCreate;
import org.epics.pvData.pv.PVField;
import org.epics.pvData.pv.PVStringArray;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.ScalarArray;
import org.epics.pvData.pv.ScalarType;
import org.epics.pvData.pv.Structure;



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
        ScalarArray bitMask = fieldCreate.createScalarArray("bitMask", ScalarType.pvByte);
    	ScalarArray choices =fieldCreate.createScalarArray("choices", ScalarType.pvString);
    	Field[] fields = new Field[]{bitMask,choices};
    	Structure structure = fieldCreate.createStructure("multiChoice", fields);
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

