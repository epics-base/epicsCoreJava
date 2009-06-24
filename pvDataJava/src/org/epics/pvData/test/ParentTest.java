/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.test;

import junit.framework.TestCase;

import org.epics.pvData.factory.PVDatabaseFactory;
import org.epics.pvData.pv.PVDatabase;
import org.epics.pvData.pv.PVField;
import org.epics.pvData.pv.PVRecord;
import org.epics.pvData.pv.Requester;
import org.epics.pvData.xml.XMLToPVDatabaseFactory;

/**
 * JUnit test for parent.
 * It shows the parent for each field.
 * @author mrk
 *
 */
public class ParentTest extends TestCase {
    private static PVDatabase master = PVDatabaseFactory.getMaster();
    /**
     * show the parent of various nodes.
     */
    public static void testParent() {
        Requester iocRequester = new RequesterForTesting("accessTest");
        XMLToPVDatabaseFactory.convert(master,"${JAVAIOC}/xml/structures.xml", iocRequester);
        XMLToPVDatabaseFactory.convert(master,"${JAVAIOC}/example/exampleDB.xml", iocRequester);
        
              
//        System.out.printf("%n%nstructures");
//        PVStructure[] pvStructures = master.getStructures();
//        for(PVStructure pvStructure: pvStructures) {
//          System.out.println(pvStructure.toString());
//        }

        XMLToPVDatabaseFactory.convert(master,"${JAVAIOC}/test/analog/analogDB.xml", iocRequester);
        XMLToPVDatabaseFactory.convert(master,"${JAVAIOC}/test/powerSupply/powerSupplyDB.xml", iocRequester);
        XMLToPVDatabaseFactory.convert(master,"${JAVAIOC}/test/types/allTypesStructure.xml", iocRequester);
        XMLToPVDatabaseFactory.convert(master,"${JAVAIOC}/test/types/allTypesRecords.xml", iocRequester);

//        PVRecord pvRecord = master.findRecord("allTypesInitial");
//        if(pvRecord!=null) System.out.println(pvRecord.toString());
//        System.out.printf("%n%nrecords");
//        PVRecord[] pvRecords = master.getRecords();
//        for(PVRecord pvRecord: pvRecords) {
//          System.out.println(pvRecord.toString());
//        }
        
//        System.out.printf("%nrecords%n");
//        Map<String,DBRecord> recordMap = iocdb.getRecordMap();
//        Set<String> keys = recordMap.keySet();
//        for(String key: keys) {
//            DBRecord record = recordMap.get(key);
//            System.out.print(record.toString());
//        }

        
        showParent("ai","alarm.severity");
        showParent("ai","timeStamp");
        showParent("ai","value");
        showParent("ai","input.value");
        System.out.printf("%n");
        showParent("psEmbeded","power");
        showParent("psEmbeded","current");
        showParent("psEmbeded","voltage");
        System.out.printf("%n");
        showParent("powerSupplyArray","alarm.severity");
        showParent("powerSupplyArray","supply.0.power");
        showParent("powerSupplyArray","supply.0.power.value");
        showParent("powerSupplyArray","supply.0.current");
        showParent("powerSupplyArray","supply.0.voltage");
        showParent("powerSupplyArray","supply.0.voltage.input.value");
        System.out.printf("%n");
        showParent("powerSupplyArray","supply.1");
        System.out.printf("%n");
        showParent("allTypesInitial","boolean");
        showParent("allTypesInitial","byte");
        showParent("allTypesInitial","short");
        showParent("allTypesInitial","int");
        showParent("allTypesInitial","long");
        showParent("allTypesInitial","float");
        showParent("allTypesInitial","double");
        showParent("allTypesInitial","booleanArray");
        showParent("allTypesInitial","byteArray");
        showParent("allTypesInitial","shortArray");
        showParent("allTypesInitial","intArray");
        showParent("allTypesInitial","longArray");
        showParent("allTypesInitial","floatArray");
        showParent("allTypesInitial","doubleArray");
        showParent("allTypesInitial","structArray");
        showParent("allTypesInitial","arrayArray");
        showParent("allTypesInitial","allTypes.boolean");
        showParent("allTypesInitial","allTypes.byte");
        showParent("allTypesInitial","allTypes.short");
        showParent("allTypesInitial","allTypes.int");
        showParent("allTypesInitial","allTypes.long");
        showParent("allTypesInitial","allTypes.float");
        showParent("allTypesInitial","allTypes.double");
        showParent("allTypesInitial","allTypes.booleanArray");
        showParent("allTypesInitial","allTypes.byteArray");
        showParent("allTypesInitial","allTypes.shortArray");
        showParent("allTypesInitial","allTypes.intArray");
        showParent("allTypesInitial","allTypes.longArray");
        showParent("allTypesInitial","allTypes.floatArray");
        showParent("allTypesInitial","allTypes.doubleArray");
        showParent("allTypesInitial","allTypes.structArray");
        showParent("allTypesInitial","allTypes.structArray.0");
        showParent("allTypesInitial","allTypes.structArray.0.low");
        showParent("allTypesInitial","allTypes.arrayArray");
        showParent("allTypesInitial","allTypes.arrayArray.0");
    }

    static void showParent(String recordName,String fieldName) {
        PVRecord pvRecord = master.findRecord(recordName);
        if(pvRecord==null) {
            System.out.printf("record %s not found%n",recordName);
            return;
        }
        PVField pvField = pvRecord.getSubField(fieldName);
        if(pvField==null){
            System.out.printf("field %s not in record %s%n",fieldName,recordName);
            return;
        }
        PVRecord record = pvField.getPVRecord();
        System.out.printf("fieldName %s actualField %s record %s fullFieldName %s fullName %s%n",
            fieldName,
            pvField.getField().getFieldName(),
            record.getRecordName(),
            pvField.getFullFieldName(),
            pvField.getFullName());
        PVField parent = pvField.getParent();
        while(parent!=null) {
            record = parent.getPVRecord();
            System.out.printf("     parent %s\n",parent.getFullName());
            parent = parent.getParent();
        }
        
    }
}
