/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.test;

import junit.framework.TestCase;

import org.epics.pvData.factory.PVDatabaseFactory;
import org.epics.pvData.factory.PVReplaceFactory;
import org.epics.pvData.property.PVProperty;
import org.epics.pvData.property.PVPropertyFactory;
import org.epics.pvData.pv.Field;
import org.epics.pvData.pv.PVDatabase;
import org.epics.pvData.pv.PVField;
import org.epics.pvData.pv.PVRecord;
import org.epics.pvData.pv.Requester;
import org.epics.pvData.xml.XMLToPVDatabaseFactory;

/**
 * JUnit test for PVAccess.
 * @author mrk
 *
 */
public class AccessTest extends TestCase {
    private static PVProperty pvProperty = PVPropertyFactory.getPVProperty();
    private static PVDatabase master = PVDatabaseFactory.getMaster();
    /**
     * test PVAccess.
     */
    public static void testAccess() {
        Requester iocRequester = new RequesterForTesting("accessTest");
        XMLToPVDatabaseFactory.convert(master,"${JAVAIOC}/xml/structures.xml", iocRequester);
        XMLToPVDatabaseFactory.convert(master,"${JAVAIOC}/example/exampleDB.xml", iocRequester);
        
//        PVStructure pvStructure = master.findStructure("scan");
//        System.out.println(pvStructure.toString());
//        PVRecord pvRecord = master.findRecord("counter");
//        System.out.println(pvRecord.toString());
        
              
//        System.out.printf("%n%nstructures");
//        PVStructure[] pvStructures = master.getStructures();
//        for(PVStructure pvStructure: pvStructures) {
//        	System.out.println(pvStructure.toString());
//        }

        XMLToPVDatabaseFactory.convert(master,"${JAVAIOC}/test/analog/analogDB.xml", iocRequester);
        XMLToPVDatabaseFactory.convert(master,"${JAVAIOC}/test/powerSupply/powerSupplyDB.xml", iocRequester);
        XMLToPVDatabaseFactory.convert(master,"${JAVAIOC}/test/types/allTypesStructure.xml", iocRequester);
        XMLToPVDatabaseFactory.convert(master,"${JAVAIOC}/test/types/allTypesRecords.xml", iocRequester);

     
//        System.out.printf("%n%nrecords");
//        PVRecord[] pvRecords = master.getRecords();
//        for(PVRecord pvRecord: pvRecords) {
//        	System.out.println(pvRecord.toString());
//        }
        
        PVReplaceFactory.replace(master);
          
        testAccess("ai","value");
        testAccess("ai","input.value");
        System.out.printf("%n");
        testAccess("psEmbeded","power.value");
        testAccess("psEmbeded","current.value");
        testAccess("psEmbeded","voltage.value");
        System.out.printf("%n");
        testAccess("psLinked","power.value");
        testAccess("psLinked","current.value");
        testAccess("psLinked","voltage.value");
        System.out.printf("%n");
        testAccess("powerSupplyArray","alarm");
        testAccess("powerSupplyArray","timeStamp");
        testAccess("powerSupplyArray","supply.0.power.value");
        testAccess("powerSupplyArray","supply.0.current.value");
        testAccess("powerSupplyArray","supply.0.voltage.value");
        testAccess("powerSupplyArray","supply.1.power.value");
        testAccess("powerSupplyArray","supply.1.current.value");
        testAccess("powerSupplyArray","supply.1.voltage.value");
        System.out.printf("%n");
        testAccess("allTypesInitial","boolean");
        testAccess("allTypesInitial","byte");
        testAccess("allTypesInitial","short");
        testAccess("allTypesInitial","int");
        testAccess("allTypesInitial","long");
        testAccess("allTypesInitial","float");
        testAccess("allTypesInitial","double");
        testAccess("allTypesInitial","string");
        testAccess("allTypesInitial","booleanArray");
        testAccess("allTypesInitial","byteArray");
        testAccess("allTypesInitial","shortArray");
        testAccess("allTypesInitial","intArray");
        testAccess("allTypesInitial","longArray");
        testAccess("allTypesInitial","floatArray");
        testAccess("allTypesInitial","doubleArray");
        testAccess("allTypesInitial","stringArray");
        testAccess("allTypesInitial","structArray");
        testAccess("allTypesInitial","arrayArray");
        testAccess("allTypesInitial","allTypes.boolean");
        testAccess("allTypesInitial","allTypes.byte");
        testAccess("allTypesInitial","allTypes.short");
        testAccess("allTypesInitial","allTypes.int");
        testAccess("allTypesInitial","allTypes.long");
        testAccess("allTypesInitial","allTypes.float");
        testAccess("allTypesInitial","allTypes.double");
        testAccess("allTypesInitial","allTypes.string");
        testAccess("allTypesInitial","allTypes.booleanArray");
        testAccess("allTypesInitial","allTypes.byteArray");
        testAccess("allTypesInitial","allTypes.shortArray");
        testAccess("allTypesInitial","allTypes.intArray");
        testAccess("allTypesInitial","allTypes.longArray");
        testAccess("allTypesInitial","allTypes.floatArray");
        testAccess("allTypesInitial","allTypes.doubleArray");
        testAccess("allTypesInitial","allTypes.stringArray");
        testAccess("allTypesInitial","allTypes.structureArray");
        testAccess("allTypesInitial","allTypes.arrayArray");
        testAccess("allTypesInitial","allTypes.arrayArray.1");
    }
    
    static void testAccess(String recordName,String fieldName) {
        PVRecord pvRecord = master.findRecord(recordName);
        if(pvRecord==null) {
            System.out.printf("record %s not found%n",recordName);
            return;
        }
        PVField pvField = pvRecord.getSubField(fieldName);
        if(pvField==null) {
            System.out.printf("field %s of record %s not found%n",fieldName,recordName);
            return;
        }
        PVField parent = pvField.getParent();
        PVRecord record = pvField.getPVRecordField().getPVRecord();
        String parentName = "none";
        Field field = pvField.getField();
        if(parent!=null) parentName = parent.getFullFieldName();
        System.out.printf("record %s fieldRequested %s fieldActual %s parent %s%n",
                record.getRecordName(),
                fieldName,field.getFieldName(),
                parentName);
        System.out.printf("    value %s%n",pvField.toString(1));
        String[] propertyNames = pvProperty.getPropertyNames(pvField);
        if(propertyNames==null) return;
            System.out.printf("    properties {");
            for(String propertyName: propertyNames) {
                System.out.printf("%s ",propertyName);
            }
            System.out.printf("}%n");
    }
}
