/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.test;

import junit.framework.TestCase;

import org.epics.pvData.factory.*;
import org.epics.pvData.factory.PVReplaceFactory;
import org.epics.pvData.pv.*;
import org.epics.pvData.pv.PVField;
import org.epics.pvData.pv.PVRecord;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.Requester;
import org.epics.pvData.pv.Type;
import org.epics.pvData.xml.XMLToPVDatabaseFactory;


/**
 * JUnit test for DBListener.
 * @author mrk
 *
 */
public class RenameTest extends TestCase {
    private static final PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
    /**
     * test DBListener.
     */
    public static void testRename() {
    	PVDatabase master = PVDatabaseFactory.getMaster();
        Requester iocRequester = new RequesterForTesting("xmlTest");
        XMLToPVDatabaseFactory.convert(master,"${JAVAIOC}/xml/structures.xml", iocRequester,false,null,null,null);
        XMLToPVDatabaseFactory.convert(master,"test/types/allTypesStructure.xml", iocRequester,false,null,null,null);
        XMLToPVDatabaseFactory.convert(master,"test/types/allTypesRecords.xml", iocRequester,false,null,null,null);
        PVReplaceFactory.replace(master);
        PVRecord pvRecord = master.findRecord("allTypesInitial");
        assertTrue(pvRecord!=null);
        PVField pvField = pvRecord.getSubField("boolean");
        assertTrue(pvField!=null);
        rename(pvField);
        pvField = pvRecord.getSubField("allTypes.byteArray");
        assertTrue(pvField!=null);
        rename(pvField);
        pvField = pvRecord.getSubField("allTypes.doubleLimit");
        assertTrue(pvField!=null);
        rename(pvField);
        pvField = pvRecord.getSubField("allTypes.structureArray.1");
        assertTrue(pvField!=null);
        rename(pvField);
        PVStructure pvStructure = master.findStructure("org.epics.pvData.alarm");
        pvStructure = pvDataCreate.createPVStructure(null, "alarm", pvStructure);
        pvField = pvStructure.getSubField("message");
        assertTrue(pvField!=null);
        rename(pvField);
        pvField = pvStructure.getSubField("severity.index");
        assertTrue(pvField!=null);
        rename(pvField);
        rename(pvStructure);
    }
    
    private static void rename (PVField pvField) {
        System.out.println();
        System.out.println(
              "before rename"
              + " fieldName "+ pvField.getField().getFieldName()
              + " fullFieldName " + pvField.getFullFieldName()
              + " fullName " + pvField.getFullName());
        pvField.renameField("replaceName");
        System.out.println(
             "after rename"
              + " fieldName "+ pvField.getField().getFieldName()
              + " fullFieldName " + pvField.getFullFieldName()
              + " fullName " + pvField.getFullName());
        if(pvField.getField().getType()==Type.structure) printSubFields((PVStructure)pvField,1);
    }
    
    private static final String prefix = "  ";
    private static void printSubFields(PVStructure pvStructure,int level) {
        String blanks = prefix;
        for(int i=0; i<level; i++) blanks += blanks;
        
        PVField[] pvFields = pvStructure.getPVFields();
        for(PVField pvField: pvFields) {
            System.out.println(blanks + pvField.getField().getFieldName() + " " + pvField.getFullFieldName() + " " + pvField.getFullName());
            if(pvField.getField().getType()==Type.structure) printSubFields((PVStructure)pvField,level +1);
        }
    }
}
