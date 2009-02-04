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
public class MatejListenerTest extends TestCase {
    private static PVProperty pvProperty = PVPropertyFactory.getPVProperty();
    private static PVDatabase master = PVDatabaseFactory.getMaster();
    private static Convert convert = ConvertFactory.getConvert();
    /**
     * test DBListener.
     * @throws InterruptedException 
     */
    public static void testListener() throws InterruptedException {
        Requester iocRequester = new RequesterForTesting("accessTest");
        XMLToPVDatabaseFactory.convert(master,"xml/structures.xml", iocRequester);
        XMLToPVDatabaseFactory.convert(master,"test/structures.xml", iocRequester);
        XMLToPVDatabaseFactory.convert(master,"test/example/counterDB.xml", iocRequester);
        
              
        XMLToPVDatabaseFactory.convert(master,"test/analog/analogDB.xml", iocRequester);
        XMLToPVDatabaseFactory.convert(master,"test/powerSupply/powerSupplyDB.xml", iocRequester);
        XMLToPVDatabaseFactory.convert(master,"test/types/allTypes.xml", iocRequester);

        /*
        PVRecord pvRecord = master.findRecord("allTypesInitial");
        if(pvRecord!=null) System.out.println(pvRecord.toString());
        System.out.printf("%n%nrecords");
        PVRecord[] pvRecords = master.getRecords();
        for(PVRecord pvRecord1: pvRecords) {
          System.out.println(pvRecord1.toString());
        }
        */
        PVReplaceFactory.replace(master);
        
        new PVListenerForTesting(master,"ai",null,false,true);

        // to separate notifications :S
        testPut("ai","alarm.severity.index",1.0);
        
        /*
        FieldCreate fieldCreate = FieldFactory.getFieldCreate();
        PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();        
        Field[] fields = new Field[2];
        fields[0] = fieldCreate.createScalar("secondsSinceEpoch",ScalarType.pvLong);
        fields[1] = fieldCreate.createScalar("nanoSeconds",ScalarType.pvInt);
        PVStructure pvStructure = pvDataCreate.createPVStructure(null,"timeStamp",fields);
        pvStructure.getLongField(fields[0].getFieldName()).put(123);
        pvStructure.getIntField(fields[1].getFieldName()).put(456);
        */
        testPut("ai","timeStamp",123);
        
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
                recordName + "." + pvField.getFullFieldName());
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
