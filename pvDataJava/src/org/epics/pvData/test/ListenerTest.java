/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.test;

import junit.framework.TestCase;

import org.epics.pvData.factory.ConvertFactory;
import org.epics.pvData.factory.PVDatabaseFactory;
import org.epics.pvData.factory.PVReplaceFactory;
import org.epics.pvData.property.PVProperty;
import org.epics.pvData.property.PVPropertyFactory;
import org.epics.pvData.pv.Convert;
import org.epics.pvData.pv.PVDatabase;
import org.epics.pvData.pv.PVField;
import org.epics.pvData.pv.PVRecord;
import org.epics.pvData.pv.PVScalar;
import org.epics.pvData.pv.PVString;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.Requester;
import org.epics.pvData.pv.ScalarType;
import org.epics.pvData.pv.Type;
import org.epics.pvData.xml.XMLToPVDatabaseFactory;


/**
 * JUnit test for DBListener.
 * @author mrk
 *
 */
public class ListenerTest extends TestCase {
    private static PVProperty pvProperty = PVPropertyFactory.getPVProperty();
    private static PVDatabase master = PVDatabaseFactory.getMaster();
    private static Convert convert = ConvertFactory.getConvert();
    /**
     * test DBListener.
     */
    public static void testListener() {
        Requester iocRequester = new RequesterForTesting("accessTest");
        XMLToPVDatabaseFactory.convert(master,"xml/structures.xml", iocRequester);
        XMLToPVDatabaseFactory.convert(master,"test/structures.xml", iocRequester);
        XMLToPVDatabaseFactory.convert(master,"test/example/counterDB.xml", iocRequester);
        
              
//        System.out.printf("%n%nstructures");
//        PVStructure[] pvStructures = master.getStructures();
//        for(PVStructure pvStructure: pvStructures) {
//          System.out.println(pvStructure.toString());
//        }

        XMLToPVDatabaseFactory.convert(master,"test/analog/analogDB.xml", iocRequester);
        XMLToPVDatabaseFactory.convert(master,"test/powerSupply/powerSupplyDB.xml", iocRequester);
        XMLToPVDatabaseFactory.convert(master,"test/types/allTypes.xml", iocRequester);

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
        
        PVReplaceFactory.replace(master);
        
        System.out.printf("%ntest put and listen exampleAi%n");
        new PVListenerForTesting(master,"aiRawCounter","scan",false,true);
        new PVListenerForTesting(master,"ai","value",true,true);
        new PVListenerForTesting(master,"ai","alarm.severity",false,true);
        new PVListenerForTesting(master,"ai","input.value",true,true);
        new PVListenerForTesting(master,"ai",null,false,true);
        testPut("aiRawCounter","scan.priority.index",2.0);
        testPut("ai","value",5.0);
        testPut("ai","input.value",2.0);
        testPut("ai","alarm.severity.index",1.0);
        testPut("ai","timeStamp.secondsPastEpoch",100.0);
        System.out.printf("%ntest put and listen examplePowerSupply%n");
        new PVListenerForTesting(master,"psSimple","power.value");
        new PVListenerForTesting(master,"psSimple","current.value");
        new PVListenerForTesting(master,"psSimple","voltage.value");
        new PVListenerForTesting(master,"psSimple",null);
        testPut("psSimple","current.value",25.0);
        testPut("psSimple","voltage.value",2.0);
        testPut("psSimple","power.value",50.0);
        System.out.printf("%ntest put and listen powerSupplyArray%n");
        new PVListenerForTesting(master,"powerSupplyArray","supply.0.power");
        new PVListenerForTesting(master,"powerSupplyArray","supply.0.current");
        new PVListenerForTesting(master,"powerSupplyArray","supply.0.voltage");
        new PVListenerForTesting(master,"powerSupplyArray","supply.1.power");
        new PVListenerForTesting(master,"powerSupplyArray","supply.1.current");
        new PVListenerForTesting(master,"powerSupplyArray","supply.1.voltage");
        testPut("powerSupplyArray","supply.0.current.value",25.0);
        testPut("powerSupplyArray","supply.0.voltage.value",2.0);
        testPut("powerSupplyArray","supply.0.power.value",50.0);
        testPut("powerSupplyArray","supply.1.current.value",2.50);
        testPut("powerSupplyArray","supply.1.voltage.value",1.00);
        testPut("powerSupplyArray","supply.1.power.value",2.50);
        testPut("powerSupplyArray","timeStamp",100.0);
    }
    
    static void testPut(String recordName,String fieldName,double value) {
        PVRecord pvRecord = master.findRecord(recordName);
        if(pvRecord==null) {
            System.out.printf("%nrecord %s not found%n",recordName);
            return;
        }
        PVField pvField = pvProperty.findProperty(pvRecord, fieldName);
        if(pvField==null){
            System.out.printf("%nfield %s not in record %s%n",fieldName,recordName);
            return;
        }
        
        Type type = pvField.getField().getType();
        if(type==Type.scalar) {
            PVScalar pvScalar = (PVScalar)pvField;
            System.out.printf("%ntestPut recordName %s fieldName %s value %f%n",
                recordName,fieldName,value);
            convert.fromDouble(pvScalar,value);
            return;
        }
        if(type!=Type.structure) {
            System.out.printf("%ntestPut recordName %s fieldName %s cant handle%n",
                fieldName,recordName);
            return;
        }
        PVStructure pvStructure = (PVStructure)pvField;
        PVField[] pvFields = pvStructure.getPVFields();
        System.out.printf("%ntestPut begin structure put %s%n",
                recordName + pvField.getFullFieldName());
        pvRecord.beginGroupPut();
        for(PVField pv : pvFields) {
            if(pv.getField().getType()!=Type.scalar) continue;
            PVScalar pvScalar = (PVScalar)pv;
            ScalarType fieldType = pvScalar.getScalar().getScalarType();
            if(fieldType.isNumeric()) {
                System.out.printf("testPut recordName %s fieldName %s value %f%n",
                        recordName,pv.getField().getFieldName(),value);
                    convert.fromDouble(pvScalar,value);
            } else if (fieldType==ScalarType.pvString) {
                String valueString = Double.toString(value);
                System.out.printf("testPut recordName %s fieldName %s value %s%n",
                        recordName,pv.getField().getFieldName(),valueString);
                PVString pvString = (PVString)pv;
                pvString.put(valueString);
            } else {
                continue;
            }
        }
        pvRecord.endGroupPut();
    }
}
