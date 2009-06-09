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
import org.epics.pvData.pv.PVString;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.Requester;
import org.epics.pvData.pv.ScalarType;
import org.epics.pvData.pvCopy.PVCopy;
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
    private static PVDataCreate dataCreate = PVDataFactory.getPVDataCreate();
    
    public static void testPVCopy() {
        int offset = 0;
        PVStructure pvTopStructure = null;
        Field newField = null;
        PVString pvString = null;
        PVCopy pvCopy = null;
        PVStructure pvStructureFromCopy = null;
        BitSet bitSet = null;
        PVStructure pvStructureInRecord = null;
        PVField pvInRecord = null;
        PVField pvFromRecord = null;
        PVField pvFromCopy = null;
        PVField pvTimeStamp = null;
        PVLong pvSeconds = null;
        PVInt pvNanoSeconds = null;
        PVField pvCopyPowerValue = null;
        PVDouble pvRecordPowerValue = null;
        PVField pvCopyCurrentValue = null;
        PVDouble pvRecordCurrentValue = null;
        Requester iocRequester = new RequesterForTesting("accessTest");
        XMLToPVDatabaseFactory.convert(master,"${PVDATA}/xml/structures.xml", iocRequester);
        XMLToPVDatabaseFactory.convert(master,"${PVDATA}/test/powerSupply/powerSupply.xml", iocRequester);
        XMLToPVDatabaseFactory.convert(master,"${PVDATA}/test/powerSupply/powerSupplyArray.xml", iocRequester);
        PVReplaceFactory.replace(master);
        PVRecord pvRecord = master.findRecord("powerSupply");

        pvTopStructure = dataCreate.createPVStructure(null, "", new Field[0]);
        newField = fieldCreate.createScalar("value", ScalarType.pvString);
        pvString = (PVString)dataCreate.createPVField(pvTopStructure, newField);
        pvString.put("power.value");
        pvTopStructure.appendPVField(pvString);
        newField = fieldCreate.createScalar("alarm", ScalarType.pvString);
        pvString = (PVString)dataCreate.createPVField(pvTopStructure, newField);
        pvString.put("alarm");
        pvTopStructure.appendPVField(pvString);
        newField = fieldCreate.createScalar("timeStamp", ScalarType.pvString);
        pvString = (PVString)dataCreate.createPVField(pvTopStructure, newField);
        pvString.put("timeStamp");
        pvTopStructure.appendPVField(pvString);
        pvCopy = PVCopyFactory.create(pvRecord, pvTopStructure);
        pvStructureFromCopy = pvCopy.createPVStructure();
//System.out.println(pvStructureFromCopy.toString());
        pvInRecord = pvRecord.getSubField("power.value");
        offset = pvCopy.getCopyOffset(pvInRecord);
        pvFromRecord = pvCopy.getRecordPVField(offset);
        pvFromCopy = pvStructureFromCopy.getSubField(offset);
        assertTrue(pvInRecord==pvFromRecord);
        assertTrue(pvFromCopy.getField().getFieldName().equals("value"));
        pvInRecord = (PVStructure)pvRecord.getSubField("alarm");
        offset = pvCopy.getCopyOffset(pvInRecord);
        pvFromRecord = pvCopy.getRecordPVField(offset);
        pvFromCopy = pvStructureFromCopy.getSubField(offset);
        assertTrue(pvInRecord==pvFromRecord);
        assertTrue(pvFromCopy.getField().getFieldName().equals("alarm"));
        pvStructureInRecord = (PVStructure)pvRecord.getSubField("alarm");
        pvInRecord = pvRecord.getSubField("alarm.message");
        offset = pvCopy.getCopyOffset(pvStructureInRecord,pvInRecord);
        pvFromRecord = pvCopy.getRecordPVField(offset);
        pvFromCopy = pvStructureFromCopy.getSubField(offset);
        assertTrue(pvInRecord==pvFromRecord);
        assertTrue(pvFromCopy.getField().getFieldName().equals("message"));
        bitSet = new BitSet(pvStructureFromCopy.getNumberFields());
        pvCopy.initCopy(pvStructureFromCopy, bitSet);
        bitSet.clear();
        pvCopyPowerValue = pvStructureFromCopy.getSubField("value");  
        pvTimeStamp = pvStructureFromCopy.getSubField("timeStamp");
        pvSeconds = (PVLong)pvRecord.getSubField("timeStamp.secondsPastEpoch");
        pvNanoSeconds = (PVInt)pvRecord.getSubField("timeStamp.nanoSeconds");
        pvRecordPowerValue = (PVDouble)pvRecord.getSubField("power.value");
        pvNanoSeconds.put(1000);
        pvCopy.updateCopy(pvStructureFromCopy, bitSet);
        pvCopy.checkBitSet(bitSet);
        showModified("update nanoSeconds " + pvTimeStamp.toString(),pvStructureFromCopy,bitSet);
        bitSet.clear();
        pvRecordPowerValue.put(2.0);
        pvSeconds.put(20000);
        pvNanoSeconds.put(2000);
        pvCopy.updateCopy(pvStructureFromCopy, bitSet);
        pvCopy.checkBitSet(bitSet);
        showModified("update value and timeStamp " + pvCopyPowerValue.toString() + " " + pvTimeStamp.toString(),pvStructureFromCopy,bitSet);

        pvTopStructure = dataCreate.createPVStructure(null, "", new Field[0]);
        newField = fieldCreate.createScalar("alarm", ScalarType.pvString);
        pvString = (PVString)dataCreate.createPVField(pvTopStructure, newField);
        pvString.put("alarm");
        pvTopStructure.appendPVField(pvString);
        newField = fieldCreate.createScalar("timeStamp", ScalarType.pvString);
        pvString = (PVString)dataCreate.createPVField(pvTopStructure, newField);
        pvString.put("timeStamp");
        pvTopStructure.appendPVField(pvString);
        PVStructure pvStructure = dataCreate.createPVStructure(pvTopStructure, "power", new Field[0]);
        newField = fieldCreate.createScalar("value", ScalarType.pvString);
        pvString = (PVString)dataCreate.createPVField(pvStructure, newField);
        pvString.put("power.value");
        pvStructure.appendPVField(pvString);
        newField = fieldCreate.createScalar("alarm", ScalarType.pvString);
        pvString = (PVString)dataCreate.createPVField(pvStructure, newField);
        pvString.put("power.alarm");
        pvStructure.appendPVField(pvString);
        pvTopStructure.appendPVField(pvStructure);
        pvStructure = dataCreate.createPVStructure(pvTopStructure, "current", new Field[0]);
        newField = fieldCreate.createScalar("value", ScalarType.pvString);
        pvString = (PVString)dataCreate.createPVField(pvStructure, newField);
        pvString.put("current.value");
        pvStructure.appendPVField(pvString);
        newField = fieldCreate.createScalar("alarm", ScalarType.pvString);
        pvString = (PVString)dataCreate.createPVField(pvStructure, newField);
        pvString.put("current.alarm");
        pvStructure.appendPVField(pvString);
        pvTopStructure.appendPVField(pvStructure);
        pvStructure = dataCreate.createPVStructure(pvTopStructure, "voltage", new Field[0]);
        newField = fieldCreate.createScalar("value", ScalarType.pvString);
        pvString = (PVString)dataCreate.createPVField(pvStructure, newField);
        pvString.put("voltage.value");
        pvStructure.appendPVField(pvString);
        newField = fieldCreate.createScalar("alarm", ScalarType.pvString);
        pvString = (PVString)dataCreate.createPVField(pvStructure, newField);
        pvString.put("voltage.alarm");
        pvStructure.appendPVField(pvString);
        pvTopStructure.appendPVField(pvStructure);
        pvCopy = PVCopyFactory.create(pvRecord, pvTopStructure);
        pvStructureFromCopy = pvCopy.createPVStructure();
//System.out.println(pvCopy.toString());
        pvInRecord = pvRecord.getSubField("current.value");
        offset = pvCopy.getCopyOffset(pvInRecord);
        pvFromRecord = pvCopy.getRecordPVField(offset);
        pvFromCopy = pvStructureFromCopy.getSubField(offset);
        assertTrue(pvInRecord==pvFromRecord);
        assertTrue(pvFromCopy.getField().getFieldName().equals("value"));
        pvStructureInRecord = (PVStructure)pvRecord.getSubField("current.alarm");
        pvInRecord = pvRecord.getSubField("current.alarm.message");
        offset = pvCopy.getCopyOffset(pvStructureInRecord,pvInRecord);
        pvFromRecord = pvCopy.getRecordPVField(offset);
        pvFromCopy = pvStructureFromCopy.getSubField(offset);
        assertTrue(pvInRecord==pvFromRecord);
        assertTrue(pvFromCopy.getField().getFieldName().equals("message"));
        bitSet = new BitSet(pvStructureFromCopy.getNumberFields());
        pvCopy.initCopy(pvStructureFromCopy, bitSet);
        bitSet.clear();
        pvCopyPowerValue = pvStructureFromCopy.getSubField("power.value"); 
        pvRecordPowerValue = (PVDouble)pvRecord.getSubField("power.value");
        pvCopyCurrentValue = pvStructureFromCopy.getSubField("current.value"); 
        pvRecordCurrentValue = (PVDouble)pvRecord.getSubField("current.value");
        pvTimeStamp = pvStructureFromCopy.getSubField("timeStamp");
        pvSeconds = (PVLong)pvRecord.getSubField("timeStamp.secondsPastEpoch");
        pvNanoSeconds = (PVInt)pvRecord.getSubField("timeStamp.nanoSeconds");
        pvNanoSeconds.put(1000);
        pvCopy.updateCopy(pvStructureFromCopy, bitSet);
        pvCopy.checkBitSet(bitSet);
        showModified("update nanoSeconds " + pvTimeStamp.toString(),pvStructureFromCopy,bitSet);
        bitSet.clear();
        pvRecordPowerValue.put(4.0);
        pvRecordCurrentValue.put(.4);
        pvSeconds.put(40000);
        pvNanoSeconds.put(4000);
        pvCopy.updateCopy(pvStructureFromCopy, bitSet);
        pvCopy.checkBitSet(bitSet);
        showModified("update value, current, and timeStamp " + pvCopyPowerValue.toString() + " "
                + pvCopyCurrentValue.toString() + " "+ pvTimeStamp.toString(),pvStructureFromCopy,bitSet);
        
        PVStructure empty = dataCreate.createPVStructure(null, "", new Field[0]);
        pvCopy = PVCopyFactory.create(pvRecord, empty);
        pvStructureFromCopy = pvCopy.createPVStructure();
        bitSet = new BitSet(pvStructureFromCopy.getNumberFields());
        pvCopy.initCopy(pvStructureFromCopy, bitSet);
        compareCopyWithRecord("after init",pvStructureFromCopy,pvCopy);
        pvRecordPowerValue.put(6.0);
        pvRecordCurrentValue.put(.6);
        pvSeconds.put(60000);
        pvNanoSeconds.put(6000); 
        compareCopyWithRecord("after change record ",pvStructureFromCopy,pvCopy);
        pvCopy.updateCopy(pvStructureFromCopy, bitSet);
        compareCopyWithRecord("after updateCopy",pvStructureFromCopy,pvCopy);
        pvSeconds = (PVLong)pvStructureFromCopy.getSubField("timeStamp.secondsPastEpoch");
        pvNanoSeconds = (PVInt)pvStructureFromCopy.getSubField("timeStamp.nanoSeconds");
        PVDouble pvDouble = (PVDouble)pvStructureFromCopy.getSubField("power.value");
        pvDouble.put(7.0);
        pvSeconds.put(700);
        pvNanoSeconds.put(7000);
        compareCopyWithRecord("after change copy ",pvStructureFromCopy,pvCopy);
        pvCopy.updateRecord(pvStructureFromCopy, bitSet);
        compareCopyWithRecord("after updateRecord",pvStructureFromCopy,pvCopy);
        
        pvRecord = master.findRecord("powerSupplyArray");
        pvTopStructure = dataCreate.createPVStructure(null, "", new Field[0]);
        newField = fieldCreate.createScalar("value", ScalarType.pvString);
        pvString = (PVString)dataCreate.createPVField(pvTopStructure, newField);
        pvString.put("supply.0.power.value");
        pvTopStructure.appendPVField(pvString);
        newField = fieldCreate.createScalar("alarm", ScalarType.pvString);
        pvString = (PVString)dataCreate.createPVField(pvTopStructure, newField);
        pvString.put("supply.0.alarm");
        pvTopStructure.appendPVField(pvString);
        newField = fieldCreate.createScalar("timeStamp", ScalarType.pvString);
        pvString = (PVString)dataCreate.createPVField(pvTopStructure, newField);
        pvString.put("timeStamp");
        pvTopStructure.appendPVField(pvString);
        pvCopy = PVCopyFactory.create(pvRecord, pvTopStructure);
        pvStructureFromCopy = pvCopy.createPVStructure();
//System.out.println(pvStructureFromCopy.toString());
        pvInRecord = pvRecord.getSubField("supply.0.power.value");
        offset = pvCopy.getCopyOffset(pvInRecord);
        pvFromRecord = pvCopy.getRecordPVField(offset);
        pvFromCopy = pvStructureFromCopy.getSubField(offset);
        assertTrue(pvInRecord==pvFromRecord);
        assertTrue(pvFromCopy.getField().getFieldName().equals("value"));
        pvInRecord = (PVStructure)pvRecord.getSubField("supply.0.alarm");
        offset = pvCopy.getCopyOffset(pvInRecord);
        pvFromRecord = pvCopy.getRecordPVField(offset);
        pvFromCopy = pvStructureFromCopy.getSubField(offset);
        assertTrue(pvInRecord==pvFromRecord);
        assertTrue(pvFromCopy.getField().getFieldName().equals("alarm"));
        pvStructureInRecord = (PVStructure)pvRecord.getSubField("supply.0.alarm");
        pvInRecord = pvRecord.getSubField("supply.0.alarm.message");
        offset = pvCopy.getCopyOffset(pvStructureInRecord,pvInRecord);
        pvFromRecord = pvCopy.getRecordPVField(offset);
        pvFromCopy = pvStructureFromCopy.getSubField(offset);
        assertTrue(pvInRecord==pvFromRecord);
        assertTrue(pvFromCopy.getField().getFieldName().equals("message"));
        bitSet = new BitSet(pvStructureFromCopy.getNumberFields());
        pvCopy.initCopy(pvStructureFromCopy, bitSet);
        bitSet.clear();
        pvCopyPowerValue = pvStructureFromCopy.getSubField("value");  
        pvTimeStamp = pvStructureFromCopy.getSubField("timeStamp");
        pvSeconds = (PVLong)pvRecord.getSubField("timeStamp.secondsPastEpoch");
        pvNanoSeconds = (PVInt)pvRecord.getSubField("timeStamp.nanoSeconds");
        pvRecordPowerValue = (PVDouble)pvRecord.getSubField("supply.0.power.value");
        pvNanoSeconds.put(1000);
        pvCopy.updateCopy(pvStructureFromCopy, bitSet);
        pvCopy.checkBitSet(bitSet);
        showModified("update nanoSeconds " + pvTimeStamp.toString(),pvStructureFromCopy,bitSet);
        bitSet.clear();
        pvRecordPowerValue.put(2.0);
        pvSeconds.put(20000);
        pvNanoSeconds.put(2000);
        pvCopy.updateCopy(pvStructureFromCopy, bitSet);
        pvCopy.checkBitSet(bitSet);
        showModified("update value and timeStamp " + pvCopyPowerValue.toString() + " " + pvTimeStamp.toString(),pvStructureFromCopy,bitSet);
    }
    
    static void showModified(String message,PVStructure pvStructure,BitSet bitSet) {
        System.out.println(message + "modifiedFields");
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

