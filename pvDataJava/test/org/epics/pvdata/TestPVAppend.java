/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata;

import junit.framework.TestCase;

import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Type;



/**
 * JUnit test for pvAccess.
 * It also provides examples of how to use the pvAccess interfaces.
 * @author mrk
 *
 */
public class TestPVAppend extends TestCase {
    
    private static final PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
	
	private static void checkNameAndParent(PVStructure pvStructure)
	{
	    PVField[] pvFields = pvStructure.getPVFields();
	    String[] fieldNames = pvStructure.getStructure().getFieldNames();
	    for(int i=0; i<pvFields.length; i++) {
	        PVField pvField = pvFields[i];
	        assert(pvField.getParent()==pvStructure);
	        assert(pvField.getFieldName().equals(fieldNames[i]));
	        if(pvField.getField().getType()==Type.structure) {
	             PVStructure xxx = (PVStructure)pvField;
	             checkNameAndParent(xxx);
	        }
	    }
	}
	
	public static void testAppendSimple()
	{
	    System.out.printf("%ntestAppendSimple%n");
	    PVStructure pvParent = pvDataCreate.createPVStructure(new String[0],new PVField[0]);
	    PVString pvStringField = (PVString)pvDataCreate.createPVScalar(ScalarType.pvString);
	    pvStringField.put("value,timeStamp");
	    PVField pvField = pvStringField;
	    pvParent.appendPVField("fieldlist",pvField);
	    pvStringField = (PVString)pvDataCreate.createPVScalar(ScalarType.pvString);
	    pvStringField.put("junk");
	    pvField = pvStringField;
	    pvParent.appendPVField("extra",pvField);
	    System.out.printf("%s%n",pvParent.toString());
	}
	
	public static void testAppendMore()
	{
	    System.out.printf("%ntestAppendMore%n");
	    PVStructure pvStructure = pvDataCreate.createPVStructure(new String[0],new PVField[0]);
	    PVStructure pvChild1 = pvDataCreate.createPVStructure(new String[0],new PVField[0]);
	    PVString pvStringField = (PVString) pvDataCreate.createPVScalar(ScalarType.pvString);
	    pvStringField.put("bla");
	    PVField pvField = pvStringField;
	    pvChild1.appendPVField("value",pvField);
	    pvField = pvChild1;
	    pvStructure.appendPVField("child1",pvField);
	    PVStructure pvChild2 = pvDataCreate.createPVStructure(new String[0],new PVField[0]);
	    pvStringField = (PVString)pvDataCreate.createPVScalar(ScalarType.pvString);
	    pvStringField.put("blabla");
	    pvField = pvStringField;
	    pvChild2.appendPVField("value",pvField);
	    pvField = pvChild2;
	    pvStructure.appendPVField("child2",pvField);
	    System.out.printf("%s%n",pvStructure.toString());
	    checkNameAndParent(pvStructure);
	}
	
	private static void append2(PVStructure pvStructure,
	        String oneName,String twoName,
	        String oneValue,String twoValue)
	{
	    PVField[] pvFields = new PVField[2];
	    pvFields[0] = pvDataCreate.createPVScalar(ScalarType.pvString);
	    pvFields[1] = pvDataCreate.createPVScalar(ScalarType.pvString);
	    String[] names = new String[2];
	    names[0] = oneName;
	    names[1] = twoName;
	    PVString pvString = (PVString)pvFields[0];
	    pvString.put(oneValue);
	    pvString = (PVString)pvFields[1];
	    pvString.put(twoValue);
	    pvStructure.appendPVFields(names, pvFields);
	}
	
	public static void testAppends()
	{
	    System.out.printf("%ntestAppends%n");
	   	PVField[] pvFields = new PVField[2];
	    String[] names = new String[2];
	    names[0] = "child1";
	    names[1] = "child2";
	    PVStructure pvChild = pvDataCreate.createPVStructure(new String[0],new PVField[0]);
	    append2(pvChild,"Joe","Mary","Good Guy","Good Girl");
	    pvFields[0] = pvChild;
//System.out.printf("%nchild0%n", pvChild.toString());
	    pvChild = pvDataCreate.createPVStructure(new String[0],new PVField[0]);
	    append2(pvChild,"Bill","Jane","Bad Guy","Bad Girl");
	    pvFields[1] = pvChild;
//System.out.printf("%nchild0%n", pvChild.toString());
	    PVStructure pvStructure = pvDataCreate.createPVStructure(names,pvFields);
	   
	    System.out.printf("%s%n",pvStructure.toString());
	    checkNameAndParent(pvStructure);
	    PVField pvField = pvStructure.getSubField("child2.Bill");
	    assert(pvField!=null);
	    pvField.renameField("Joe");
	    System.out.printf("%s%n",pvStructure.toString());
	    pvField.getParent().removePVField("Joe");
	    System.out.printf("%s%n",pvStructure.toString());
	    checkNameAndParent(pvStructure);
	}

}

