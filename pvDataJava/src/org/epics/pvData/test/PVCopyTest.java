/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.test;

import java.util.BitSet;

import junit.framework.TestCase;

import org.epics.pvData.factory.FieldFactory;
import org.epics.pvData.factory.PVDataFactory;
import org.epics.pvData.factory.PVDatabaseFactory;
import org.epics.pvData.factory.PVReplaceFactory;
import org.epics.pvData.pv.Field;
import org.epics.pvData.pv.FieldCreate;
import org.epics.pvData.pv.PVDataCreate;
import org.epics.pvData.pv.PVDatabase;
import org.epics.pvData.pv.PVDouble;
import org.epics.pvData.pv.PVField;
import org.epics.pvData.pv.PVInt;
import org.epics.pvData.pv.PVLong;
import org.epics.pvData.pv.PVRecord;
import org.epics.pvData.pv.*;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.Requester;
import org.epics.pvData.pv.ScalarType;
import org.epics.pvData.pvCopy.*;
import org.epics.pvData.pvCopy.PVCopyFactory;
import org.epics.pvData.xml.XMLToPVDatabaseFactory;



/**
 * JUnit test for pvAccess.
 * It also provides examples of how to use the pvAccess interfaces.
 * @author mrk
 *
 */
public class PVCopyTest extends TestCase {
    private static PVDatabase master = PVDatabaseFactory.getMaster();
    private static FieldCreate fieldCreate = FieldFactory.getFieldCreate();
    private static PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
    private static BitSetUtil bitSetUtil = BitSetUtilFactory.getCompressBitSet();
    
    public static void testPVCopy() {
     // get database for testing
        Requester iocRequester = new RequesterForTesting("accessTest");
        XMLToPVDatabaseFactory.convert(master,"${PVDATA}/xml/structures.xml", iocRequester);
        XMLToPVDatabaseFactory.convert(master,"${PVDATA}/test/powerSupply/powerSupply.xml", iocRequester);
        XMLToPVDatabaseFactory.convert(master,"${PVDATA}/test/powerSupply/powerSupplyArray.xml", iocRequester);
        XMLToPVDatabaseFactory.convert(master,"${PVDATA}/src/org/epics/pvData/test/structuresForPVCopyTest.xml", iocRequester);
        PVReplaceFactory.replace(master);
        exampleTest();
        exampleShareDataTest();
        longTest();
    }
    
    public static void exampleTest() {
        System.out.printf("%n%n****Example****%n");
        // definitions for request structure to pass to PVCopyFactory
        PVRecord pvRecord = null;
        PVStructure pvRequest = null;
        // definitions for PVCopy
        PVCopy pvCopy = null;
        PVStructure pvCopyStructure = null;
        BitSet bitSet = null;
        
        System.out.printf(
                "%nalarm,timeStamp,power.value from powerSupply%n");
        pvRecord = master.findRecord("powerSupply");
        pvRequest = master.findStructure("powerFromPowerSupply");        
        pvCopy = PVCopyFactory.create(pvRecord, pvRequest, "", false);
        pvCopyStructure = pvCopy.createPVStructure();
        bitSet = new BitSet(pvCopyStructure.getNumberFields());
        pvCopy.updateCopy(pvCopyStructure, bitSet);
        System.out.println(pvCopyStructure.toString());
        
        System.out.printf(
             "%npower, current, voltage. For each value and alarm."
              + " Note that PVRecord.power does NOT have an alarm field.%n");
        pvRequest = master.findStructure("powerSupplyFromPowerSupply");        
        pvCopy = PVCopyFactory.create(pvRecord, pvRequest, "", false);
        pvCopyStructure = pvCopy.createPVStructure();
        bitSet = new BitSet(pvCopyStructure.getNumberFields());
        pvCopy.initCopy(pvCopyStructure, bitSet);
        System.out.println(pvCopyStructure.toString());
        
        System.out.printf(
        "%npowerSupply from powerSupplyArray%n");
        pvRecord = master.findRecord("powerSupplyArray");
        pvRequest = master.findStructure("powerSupplyFromPowerSupplyArray");        
        pvCopy = PVCopyFactory.create(pvRecord, pvRequest, "", false);
        pvCopyStructure = pvCopy.createPVStructure();
        bitSet = new BitSet(pvCopyStructure.getNumberFields());
        pvCopy.updateCopy(pvCopyStructure, bitSet);
        System.out.println(pvCopyStructure.toString());
    }
    
    public static void exampleShareDataTest() {
        System.out.printf("%n%n****Example Share Data****%n");
        // definitions for request structure to pass to PVCopyFactory
        PVRecord pvRecord = null;
        PVStructure pvRequest = null;
        // definitions for PVCopy
        PVCopy pvCopy = null;
        PVStructure pvCopyStructure = null;
        BitSet bitSet = null;
        
        System.out.printf(
                "%nalarm,timeStamp,power.value from powerSupply%n");
        pvRecord = master.findRecord("powerSupply");
        pvRequest = master.findStructure("powerFromPowerSupply");        
        pvCopy = PVCopyFactory.create(pvRecord, pvRequest, "", true);
        pvCopyStructure = pvCopy.createPVStructure();
        bitSet = new BitSet(pvCopyStructure.getNumberFields());
        pvCopy.updateCopy(pvCopyStructure, bitSet);
        System.out.println(pvCopyStructure.toString());
        
        System.out.printf(
             "%npower, current, voltage. For each value and alarm."
              + " Note that PVRecord.power does NOT have an alarm field.%n");
        pvRequest = master.findStructure("powerSupplyFromPowerSupply");        
        pvCopy = PVCopyFactory.create(pvRecord, pvRequest, "", true);
        pvCopyStructure = pvCopy.createPVStructure();
        bitSet = new BitSet(pvCopyStructure.getNumberFields());
        pvCopy.initCopy(pvCopyStructure, bitSet);
        System.out.println(pvCopyStructure.toString());
        
        System.out.printf(
        "%npowerSupply from powerSupplyArray%n");
        pvRecord = master.findRecord("powerSupplyArray");
        pvRequest = master.findStructure("powerSupplyFromPowerSupplyArray");        
        pvCopy = PVCopyFactory.create(pvRecord, pvRequest, "", true);
        pvCopyStructure = pvCopy.createPVStructure();
        bitSet = new BitSet(pvCopyStructure.getNumberFields());
        pvCopy.updateCopy(pvCopyStructure, bitSet);
        System.out.println(pvCopyStructure.toString());
    }
    
    public static void longTest() {  
        System.out.printf("%n%n****Long Test****%n");
        // definitions for request structure to pass to PVCopyFactory
        int offset = 0;
        PVStructure pvRequest = null;
        Field newField = null;
        PVString pvString = null;
        // definitions for PVCopy
        PVCopy pvCopy = null;
        PVStructure pvRecordStructure = null;
        PVStructure pvCopyStructure = null;
        BitSet bitSet = null;
        PVField pvInRecord = null;
        PVField pvFromRecord = null;
        PVField pvFromCopy = null;
        // fields in pvRecordStructure
        PVLong pvRecordSeconds = null;
        PVInt pvRecordNanoSeconds = null;
        PVDouble pvRecordPowerValue = null;
        PVDouble pvRecordCurrentValue = null;
        // fields in pvCopyStructure
        PVField pvCopyTimeStamp = null;
        PVLong pvCopySeconds = null;
        PVInt pvCopyNanoSeconds = null;
        PVField pvCopyPower = null;
        PVField pvCopyPowerValue = null;
        PVField pvCopyCurrentValue = null;
        
        PVRecord pvRecord = master.findRecord("powerSupply");
        
        System.out.printf("%nvalue, alarm, timeStamp%n");
        pvRequest = pvDataCreate.createPVStructure(null, "", new Field[0]);
        newField = fieldCreate.createScalar("value", ScalarType.pvString);
        pvString = (PVString)pvDataCreate.createPVField(pvRequest, newField);
        pvString.put("power.value");
        pvRequest.appendPVField(pvString);
        newField = fieldCreate.createScalar("alarm", ScalarType.pvString);
        pvString = (PVString)pvDataCreate.createPVField(pvRequest, newField);
        pvString.put("alarm");
        pvRequest.appendPVField(pvString);
        newField = fieldCreate.createScalar("timeStamp", ScalarType.pvString);
        pvString = (PVString)pvDataCreate.createPVField(pvRequest, newField);
        pvString.put("timeStamp");
        pvRequest.appendPVField(pvString);
        pvCopy = PVCopyFactory.create(pvRecord, pvRequest, "", false);
        pvCopyStructure = pvCopy.createPVStructure();
        bitSet = new BitSet(pvCopyStructure.getNumberFields());
//System.out.println(pvStructureFromCopy.toString());
        pvInRecord = pvRecord.getSubField("power.value");
        offset = pvCopy.getCopyOffset(pvInRecord);
        pvFromRecord = pvCopy.getRecordPVField(offset);
        pvFromCopy = pvCopyStructure.getSubField(offset);
        assertTrue(pvInRecord==pvFromRecord);
        assertTrue(pvFromCopy.getField().getFieldName().equals("value"));
        pvInRecord = (PVStructure)pvRecord.getSubField("alarm");
        offset = pvCopy.getCopyOffset(pvInRecord);
        pvFromRecord = pvCopy.getRecordPVField(offset);
        pvFromCopy = pvCopyStructure.getSubField(offset);
        assertTrue(pvInRecord==pvFromRecord);
        assertTrue(pvFromCopy.getField().getFieldName().equals("alarm"));
        pvRecordStructure = (PVStructure)pvRecord.getSubField("alarm");
        pvInRecord = pvRecord.getSubField("alarm.message");
        offset = pvCopy.getCopyOffset(pvRecordStructure,pvInRecord);
        pvFromRecord = pvCopy.getRecordPVField(offset);
        pvFromCopy = pvCopyStructure.getSubField(offset);
        assertTrue(pvInRecord==pvFromRecord);
        assertTrue(pvFromCopy.getField().getFieldName().equals("message"));
        pvCopy.initCopy(pvCopyStructure, bitSet);
        bitSet.clear();
        pvCopyPowerValue = pvCopyStructure.getSubField("value");  
        pvCopyTimeStamp = pvCopyStructure.getSubField("timeStamp");
        pvRecordSeconds = (PVLong)pvRecord.getSubField("timeStamp.secondsPastEpoch");
        pvRecordNanoSeconds = (PVInt)pvRecord.getSubField("timeStamp.nanoSeconds");
        pvRecordPowerValue = (PVDouble)pvRecord.getSubField("power.value");
        pvRecordNanoSeconds.put(1000);
        pvCopy.updateCopy(pvCopyStructure, bitSet);
        bitSetUtil.compress(bitSet, pvCopyStructure);
        showModified("update nanoSeconds " + pvCopyTimeStamp.toString(),pvCopyStructure,bitSet);
        bitSet.clear();
        pvRecordPowerValue.put(2.0);
        pvRecordSeconds.put(20000);
        pvRecordNanoSeconds.put(2000);
        pvCopy.updateCopy(pvCopyStructure, bitSet);
        bitSetUtil.compress(bitSet, pvCopyStructure);
        showModified("update value and timeStamp " + pvCopyPowerValue.toString() + " " + pvCopyTimeStamp.toString(),pvCopyStructure,bitSet);

        System.out.printf(
             "%npower, current, voltage. For each value and alarm."
              + " Note that PVRecord.power does NOT have an alarm field.%n");
        pvRequest = pvDataCreate.createPVStructure(null, "", new Field[0]);
        newField = fieldCreate.createScalar("alarm", ScalarType.pvString);
        pvString = (PVString)pvDataCreate.createPVField(pvRequest, newField);
        pvString.put("alarm");
        pvRequest.appendPVField(pvString);
        newField = fieldCreate.createScalar("timeStamp", ScalarType.pvString);
        pvString = (PVString)pvDataCreate.createPVField(pvRequest, newField);
        pvString.put("timeStamp");
        pvRequest.appendPVField(pvString);
        PVStructure pvStructure = pvDataCreate.createPVStructure(pvRequest, "power", new Field[0]);
        newField = fieldCreate.createScalar("value", ScalarType.pvString);
        pvString = (PVString)pvDataCreate.createPVField(pvStructure, newField);
        pvString.put("power.value");
        pvStructure.appendPVField(pvString);
        newField = fieldCreate.createScalar("alarm", ScalarType.pvString);
        pvString = (PVString)pvDataCreate.createPVField(pvStructure, newField);
        pvString.put("power.alarm");
        pvStructure.appendPVField(pvString);
        pvRequest.appendPVField(pvStructure);
        pvStructure = pvDataCreate.createPVStructure(pvRequest, "current", new Field[0]);
        newField = fieldCreate.createScalar("value", ScalarType.pvString);
        pvString = (PVString)pvDataCreate.createPVField(pvStructure, newField);
        pvString.put("current.value");
        pvStructure.appendPVField(pvString);
        newField = fieldCreate.createScalar("alarm", ScalarType.pvString);
        pvString = (PVString)pvDataCreate.createPVField(pvStructure, newField);
        pvString.put("current.alarm");
        pvStructure.appendPVField(pvString);
        pvRequest.appendPVField(pvStructure);
        pvStructure = pvDataCreate.createPVStructure(pvRequest, "voltage", new Field[0]);
        newField = fieldCreate.createScalar("value", ScalarType.pvString);
        pvString = (PVString)pvDataCreate.createPVField(pvStructure, newField);
        pvString.put("voltage.value");
        pvStructure.appendPVField(pvString);
        newField = fieldCreate.createScalar("alarm", ScalarType.pvString);
        pvString = (PVString)pvDataCreate.createPVField(pvStructure, newField);
        pvString.put("voltage.alarm");
        pvStructure.appendPVField(pvString);
        pvRequest.appendPVField(pvStructure);
        pvCopy = PVCopyFactory.create(pvRecord, pvRequest, "", false);
        pvCopyStructure = pvCopy.createPVStructure();
        bitSet = new BitSet(pvCopyStructure.getNumberFields());
//System.out.println(pvCopy.toString());
        pvInRecord = pvRecord.getSubField("current.value");
        offset = pvCopy.getCopyOffset(pvInRecord);
        pvFromRecord = pvCopy.getRecordPVField(offset);
        pvFromCopy = pvCopyStructure.getSubField(offset);
        assertTrue(pvInRecord==pvFromRecord);
        assertTrue(pvFromCopy.getField().getFieldName().equals("value"));
        pvRecordStructure = (PVStructure)pvRecord.getSubField("current.alarm");
        pvInRecord = pvRecord.getSubField("current.alarm.message");
        offset = pvCopy.getCopyOffset(pvRecordStructure,pvInRecord);
        pvFromRecord = pvCopy.getRecordPVField(offset);
        pvFromCopy = pvCopyStructure.getSubField(offset);
        assertTrue(pvInRecord==pvFromRecord);
        assertTrue(pvFromCopy.getField().getFieldName().equals("message"));
        pvCopy.initCopy(pvCopyStructure, bitSet);
        bitSet.clear();
        // get pvRecord fields
        pvRecordSeconds = (PVLong)pvRecord.getSubField("timeStamp.secondsPastEpoch");
        pvRecordNanoSeconds = (PVInt)pvRecord.getSubField("timeStamp.nanoSeconds");
        pvRecordPowerValue = (PVDouble)pvRecord.getSubField("power.value");
        pvRecordCurrentValue = (PVDouble)pvRecord.getSubField("current.value");
        // get pvStructureForCopy fields
        pvCopyTimeStamp = pvCopyStructure.getSubField("timeStamp");
        pvCopySeconds = (PVLong)pvCopyStructure.getSubField("timeStamp.secondsPastEpoch");
        pvCopyNanoSeconds = (PVInt)pvCopyStructure.getSubField("timeStamp.nanoSeconds");
        pvCopyPower = pvCopyStructure.getSubField("power");
        pvCopyPowerValue = pvCopyStructure.getSubField("power.value"); 
        pvCopyCurrentValue = pvCopyStructure.getSubField("current.value"); 
        pvRecordNanoSeconds.put(1000);
        pvCopy.updateCopy(pvCopyStructure, bitSet);
        bitSetUtil.compress(bitSet, pvCopyStructure);
        showModified("update nanoSeconds " + pvCopyTimeStamp.toString(),pvCopyStructure,bitSet);
        bitSet.clear();
        pvRecordPowerValue.put(4.0);
        pvRecordCurrentValue.put(.4);
        pvRecordSeconds.put(40000);
        pvRecordNanoSeconds.put(4000);
        pvCopy.updateCopy(pvCopyStructure, bitSet);
        assertTrue(bitSet.get(pvCopyPowerValue.getFieldOffset()));
        assertFalse(bitSet.get(pvCopyPower.getFieldOffset()));
        assertTrue(bitSet.get(pvCopySeconds.getFieldOffset()));
        assertTrue(bitSet.get(pvCopyNanoSeconds.getFieldOffset()));
        assertFalse(bitSet.get(pvCopyTimeStamp.getFieldOffset()));
        showModified("before compress update value, current, and timeStamp " + pvCopyPowerValue.toString() + " "
                + pvCopyCurrentValue.toString() + " "+ pvCopyTimeStamp.toString(),pvCopyStructure,bitSet);
        bitSetUtil.compress(bitSet, pvCopyStructure);
        assertFalse(bitSet.get(pvCopyPowerValue.getFieldOffset()));
        assertTrue(bitSet.get(pvCopyPower.getFieldOffset()));
        assertFalse(bitSet.get(pvCopySeconds.getFieldOffset()));
        assertFalse(bitSet.get(pvCopyNanoSeconds.getFieldOffset()));
        assertTrue(bitSet.get(pvCopyTimeStamp.getFieldOffset()));
        showModified("after compress update value, current, and timeStamp " + pvCopyPowerValue.toString() + " "
                + pvCopyCurrentValue.toString() + " "+ pvCopyTimeStamp.toString(),pvCopyStructure,bitSet);
        bitSetUtil.compress(bitSet, pvCopyStructure);
        showModified("after second compress update value, current, and timeStamp " + pvCopyPowerValue.toString() + " "
                + pvCopyCurrentValue.toString() + " "+ pvCopyTimeStamp.toString(),pvCopyStructure,bitSet);
        PVStructure empty = pvDataCreate.createPVStructure(null, "", new Field[0]);
        pvCopy = PVCopyFactory.create(pvRecord, empty, "", false);
        pvCopyStructure = pvCopy.createPVStructure();
        bitSet = new BitSet(pvCopyStructure.getNumberFields());
        pvCopy.initCopy(pvCopyStructure, bitSet);
        compareCopyWithRecord("after init",pvCopyStructure,pvCopy);
        pvRecordPowerValue.put(6.0);
        pvRecordCurrentValue.put(.6);
        pvRecordSeconds.put(60000);
        pvRecordNanoSeconds.put(6000); 
        compareCopyWithRecord("after change record ",pvCopyStructure,pvCopy);
        pvCopy.updateCopy(pvCopyStructure, bitSet);
        compareCopyWithRecord("after updateCopy",pvCopyStructure,pvCopy);
        pvCopySeconds = (PVLong)pvCopyStructure.getSubField("timeStamp.secondsPastEpoch");
        pvCopyNanoSeconds = (PVInt)pvCopyStructure.getSubField("timeStamp.nanoSeconds");
        PVDouble pvDouble = (PVDouble)pvCopyStructure.getSubField("power.value");
        pvDouble.put(7.0);
        pvCopySeconds.put(700);
        pvCopyNanoSeconds.put(7000);
        compareCopyWithRecord("after change copy ",pvCopyStructure,pvCopy);
        pvCopy.updateRecord(pvCopyStructure, bitSet);
        compareCopyWithRecord("after updateRecord",pvCopyStructure,pvCopy);
        
        System.out.printf("%npowerSupplyArray: value alarm and timeStamp."
                 + " Note where power and alarm are chosen.%n");
        pvRecord = master.findRecord("powerSupplyArray");
        pvRequest = pvDataCreate.createPVStructure(null, "", new Field[0]);
        newField = fieldCreate.createScalar("value", ScalarType.pvString);
        pvString = (PVString)pvDataCreate.createPVField(pvRequest, newField);
        pvString.put("supply.0.power.value");
        pvRequest.appendPVField(pvString);
        newField = fieldCreate.createScalar("alarm", ScalarType.pvString);
        pvString = (PVString)pvDataCreate.createPVField(pvRequest, newField);
        pvString.put("supply.0.alarm");
        pvRequest.appendPVField(pvString);
        newField = fieldCreate.createScalar("timeStamp", ScalarType.pvString);
        pvString = (PVString)pvDataCreate.createPVField(pvRequest, newField);
        pvString.put("timeStamp");
        pvRequest.appendPVField(pvString);
        pvCopy = PVCopyFactory.create(pvRecord, pvRequest, "", false);
        pvCopyStructure = pvCopy.createPVStructure();
        bitSet = new BitSet(pvCopyStructure.getNumberFields());
//System.out.println(pvStructureFromCopy.toString());
        pvInRecord = pvRecord.getSubField("supply.0.power.value");
        offset = pvCopy.getCopyOffset(pvInRecord);
        pvFromRecord = pvCopy.getRecordPVField(offset);
        pvFromCopy = pvCopyStructure.getSubField(offset);
        assertTrue(pvInRecord==pvFromRecord);
        assertTrue(pvFromCopy.getField().getFieldName().equals("value"));
        pvInRecord = (PVStructure)pvRecord.getSubField("supply.0.alarm");
        offset = pvCopy.getCopyOffset(pvInRecord);
        pvFromRecord = pvCopy.getRecordPVField(offset);
        pvFromCopy = pvCopyStructure.getSubField(offset);
        assertTrue(pvInRecord==pvFromRecord);
        assertTrue(pvFromCopy.getField().getFieldName().equals("alarm"));
        pvRecordStructure = (PVStructure)pvRecord.getSubField("supply.0.alarm");
        pvInRecord = pvRecord.getSubField("supply.0.alarm.message");
        offset = pvCopy.getCopyOffset(pvRecordStructure,pvInRecord);
        pvFromRecord = pvCopy.getRecordPVField(offset);
        pvFromCopy = pvCopyStructure.getSubField(offset);
        assertTrue(pvInRecord==pvFromRecord);
        assertTrue(pvFromCopy.getField().getFieldName().equals("message"));
        pvCopy.initCopy(pvCopyStructure, bitSet);
        bitSet.clear();
        pvCopyPowerValue = pvCopyStructure.getSubField("value");  
        pvCopyTimeStamp = pvCopyStructure.getSubField("timeStamp");
        pvRecordSeconds = (PVLong)pvRecord.getSubField("timeStamp.secondsPastEpoch");
        pvRecordNanoSeconds = (PVInt)pvRecord.getSubField("timeStamp.nanoSeconds");
        pvRecordPowerValue = (PVDouble)pvRecord.getSubField("supply.0.power.value");
        pvRecordNanoSeconds.put(1000);
        pvCopy.updateCopy(pvCopyStructure, bitSet);
        bitSetUtil.compress(bitSet, pvCopyStructure);
        showModified("update nanoSeconds " + pvCopyTimeStamp.toString(),pvCopyStructure,bitSet);
        bitSet.clear();
        pvRecordPowerValue.put(2.0);
        pvRecordSeconds.put(20000);
        pvRecordNanoSeconds.put(2000);
        pvCopy.updateCopy(pvCopyStructure, bitSet);
        bitSetUtil.compress(bitSet, pvCopyStructure);
        showModified("update value and timeStamp " + pvCopyPowerValue.toString() + " " + pvCopyTimeStamp.toString(),pvCopyStructure,bitSet);
    }
    
    static void showModified(String message,PVStructure pvStructure,BitSet bitSet) {
        System.out.println();
        System.out.println(message);
        System.out.printf("modifiedFields bitSet %x%n",bitSet.toLongArray()[0]);
        int size = bitSet.size();
        int index = -1;
        while(++index < size) {
            if(bitSet.get(index)) {
                PVField pvField = pvStructure.getSubField(index);
               System.out.println("   " + pvField.getFullFieldName());
            }
        }
    }
    
    static void compareCopyWithRecord(String message,PVStructure pvStructure,PVCopy pvCopy) {
        System.out.println();
        System.out.println(message);
        int length = pvStructure.getNumberFields();
        for(int offset=0; offset<length; offset++) {
            PVField pvCopyField = pvStructure.getSubField(offset);
            PVField pvRecordField = pvCopy.getRecordPVField(offset);
            if(!pvCopyField.equals(pvRecordField)) {
                System.out.println("    " + pvCopyField.getFullFieldName() + " NE " + pvRecordField.getFullFieldName());
            }
        }
    }
}

