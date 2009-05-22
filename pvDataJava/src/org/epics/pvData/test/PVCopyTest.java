/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.test;

import junit.framework.TestCase;

import org.epics.pvData.factory.ConvertFactory;
import org.epics.pvData.factory.FieldFactory;
import org.epics.pvData.factory.PVDataFactory;
import org.epics.pvData.factory.PVDatabaseFactory;
import org.epics.pvData.pv.Array;
import org.epics.pvData.pv.BooleanArrayData;
import org.epics.pvData.pv.ByteArrayData;
import org.epics.pvData.pv.Convert;
import org.epics.pvData.pv.Field;
import org.epics.pvData.pv.FieldCreate;
import org.epics.pvData.pv.PVArray;
import org.epics.pvData.pv.PVAuxInfo;
import org.epics.pvData.pv.PVBoolean;
import org.epics.pvData.pv.PVBooleanArray;
import org.epics.pvData.pv.PVByte;
import org.epics.pvData.pv.PVByteArray;
import org.epics.pvData.pv.PVDataCreate;
import org.epics.pvData.pv.PVDatabase;
import org.epics.pvData.pv.PVDouble;
import org.epics.pvData.pv.PVDoubleArray;
import org.epics.pvData.pv.PVField;
import org.epics.pvData.pv.PVFloat;
import org.epics.pvData.pv.PVFloatArray;
import org.epics.pvData.pv.PVInt;
import org.epics.pvData.pv.PVIntArray;
import org.epics.pvData.pv.PVLong;
import org.epics.pvData.pv.PVLongArray;
import org.epics.pvData.pv.PVScalar;
import org.epics.pvData.pv.PVShort;
import org.epics.pvData.pv.PVShortArray;
import org.epics.pvData.pv.PVString;
import org.epics.pvData.pv.PVStringArray;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.Requester;
import org.epics.pvData.pv.Scalar;
import org.epics.pvData.pv.ScalarType;
import org.epics.pvData.pv.StringArrayData;
import org.epics.pvData.pv.Structure;
import org.epics.pvData.pv.*;
import org.epics.pvData.pvCopy.*;
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
        PVStructure pvDataCreate = null;
        Field newField = null;
        PVString pvString = null;
        PVCopy pvCopy = null;
        PVStructure pvStructureFromCopy = null;
        PVCopyIterator pvIterator = null;
        PVStructure pvStructureInRecord = null;
        PVField pvInRecord = null;
        PVField pvFromRecord = null;
        PVField pvFromCopy = null;
        Requester iocRequester = new RequesterForTesting("accessTest");
        XMLToPVDatabaseFactory.convert(master,"${JAVAIOC}/xml/structures.xml", iocRequester);
        XMLToPVDatabaseFactory.convert(master,"${JAVAIOC}/test/powerSupply/powerSupplyDB.xml", iocRequester);
        PVRecord pvRecord = master.findRecord("psEmbeded");

        pvDataCreate = dataCreate.createPVStructure(null, "", new Field[0]);
        newField = fieldCreate.createScalar("value", ScalarType.pvString);
        pvString = (PVString)dataCreate.createPVField(pvDataCreate, newField);
        pvString.put("power.value");
        pvDataCreate.appendPVField(pvString);
        newField = fieldCreate.createScalar("timeStamp", ScalarType.pvString);
        pvString = (PVString)dataCreate.createPVField(pvDataCreate, newField);
        pvString.put("timeStamp");
        pvDataCreate.appendPVField(pvString);
        pvCopy = PVCopyFactory.create(pvRecord, pvDataCreate);
        pvStructureFromCopy = pvCopy.createPVStructure();
        pvIterator = PVCopyIteratorFactory.create(pvStructureFromCopy);
        pvInRecord = pvRecord.getSubField("power.value");
        offset = pvCopy.getOffset(pvInRecord);
        pvFromRecord = pvCopy.getPVField(offset);
        pvFromCopy = pvIterator.getPVField(offset);
        assertTrue(pvInRecord==pvFromRecord);
        assertTrue(pvFromCopy.getField().getFieldName().equals("value"));

        pvDataCreate = dataCreate.createPVStructure(null, "", new Field[0]);
        newField = fieldCreate.createScalar("value", ScalarType.pvString);
        pvString = (PVString)dataCreate.createPVField(pvDataCreate, newField);
        pvString.put("power.value");
        pvDataCreate.appendPVField(pvString);
        newField = fieldCreate.createScalar("alarm", ScalarType.pvString);
        pvString = (PVString)dataCreate.createPVField(pvDataCreate, newField);
        pvString.put("alarm");
        pvDataCreate.appendPVField(pvString);
        newField = fieldCreate.createScalar("timeStamp", ScalarType.pvString);
        pvString = (PVString)dataCreate.createPVField(pvDataCreate, newField);
        pvString.put("timeStamp");
        pvDataCreate.appendPVField(pvString);
        pvCopy = PVCopyFactory.create(pvRecord, pvDataCreate);
        pvStructureFromCopy = pvCopy.createPVStructure();
        pvIterator = PVCopyIteratorFactory.create(pvStructureFromCopy);
//System.out.println(pvStructureFromCopy.toString());
        pvInRecord = pvRecord.getSubField("power.value");
        offset = pvCopy.getOffset(pvInRecord);
        pvFromRecord = pvCopy.getPVField(offset);
        pvFromCopy = pvIterator.getPVField(offset);
        assertTrue(pvInRecord==pvFromRecord);
        assertTrue(pvFromCopy.getField().getFieldName().equals("value"));
        pvInRecord = (PVStructure)pvRecord.getSubField("alarm");
        offset = pvCopy.getOffset(pvInRecord);
        pvFromRecord = pvCopy.getPVField(offset);
        pvFromCopy = pvIterator.getPVField(offset);
        assertTrue(pvInRecord==pvFromRecord);
        assertTrue(pvFromCopy.getField().getFieldName().equals("alarm"));
        pvStructureInRecord = (PVStructure)pvRecord.getSubField("alarm");
        pvInRecord = pvRecord.getSubField("alarm.message");
        offset = pvCopy.getOffset(pvStructureInRecord,pvInRecord);
        pvFromRecord = pvCopy.getPVField(offset);
        pvFromCopy = pvIterator.getPVField(offset);
        assertTrue(pvInRecord==pvFromRecord);
        assertTrue(pvFromCopy.getField().getFieldName().equals("message"));
        


        pvDataCreate = dataCreate.createPVStructure(null, "", new Field[0]);
        newField = fieldCreate.createScalar("alarm", ScalarType.pvString);
        pvString = (PVString)dataCreate.createPVField(pvDataCreate, newField);
        pvString.put("alarm");
        pvDataCreate.appendPVField(pvString);
        newField = fieldCreate.createScalar("timeStamp", ScalarType.pvString);
        pvString = (PVString)dataCreate.createPVField(pvDataCreate, newField);
        pvString.put("timeStamp");
        pvDataCreate.appendPVField(pvString);
        PVStructure pvStructure = dataCreate.createPVStructure(pvDataCreate, "power", new Field[0]);
        newField = fieldCreate.createScalar("value", ScalarType.pvString);
        pvString = (PVString)dataCreate.createPVField(pvStructure, newField);
        pvString.put("power.value");
        pvStructure.appendPVField(pvString);
        newField = fieldCreate.createScalar("alarm", ScalarType.pvString);
        pvString = (PVString)dataCreate.createPVField(pvStructure, newField);
        pvString.put("power.alarm");
        pvStructure.appendPVField(pvString);
        pvDataCreate.appendPVField(pvStructure);
        pvStructure = dataCreate.createPVStructure(pvDataCreate, "current", new Field[0]);
        newField = fieldCreate.createScalar("value", ScalarType.pvString);
        pvString = (PVString)dataCreate.createPVField(pvStructure, newField);
        pvString.put("current.value");
        pvStructure.appendPVField(pvString);
        newField = fieldCreate.createScalar("alarm", ScalarType.pvString);
        pvString = (PVString)dataCreate.createPVField(pvStructure, newField);
        pvString.put("current.alarm");
        pvStructure.appendPVField(pvString);
        pvDataCreate.appendPVField(pvStructure);
        pvStructure = dataCreate.createPVStructure(pvDataCreate, "voltage", new Field[0]);
        newField = fieldCreate.createScalar("value", ScalarType.pvString);
        pvString = (PVString)dataCreate.createPVField(pvStructure, newField);
        pvString.put("voltage.value");
        pvStructure.appendPVField(pvString);
        newField = fieldCreate.createScalar("alarm", ScalarType.pvString);
        pvString = (PVString)dataCreate.createPVField(pvStructure, newField);
        pvString.put("voltage.alarm");
        pvStructure.appendPVField(pvString);
        pvDataCreate.appendPVField(pvStructure);
        pvCopy = PVCopyFactory.create(pvRecord, pvDataCreate);
        pvStructureFromCopy = pvCopy.createPVStructure();
        pvIterator = PVCopyIteratorFactory.create(pvStructureFromCopy);
//System.out.println(pvCopy.toString());
        pvInRecord = pvRecord.getSubField("current.value");
        offset = pvCopy.getOffset(pvInRecord);
        pvFromRecord = pvCopy.getPVField(offset);
        pvFromCopy = pvIterator.getPVField(offset);
        assertTrue(pvInRecord==pvFromRecord);
        assertTrue(pvFromCopy.getField().getFieldName().equals("value"));
        pvStructureInRecord = (PVStructure)pvRecord.getSubField("current.alarm");
        pvInRecord = pvRecord.getSubField("current.alarm.message");
        offset = pvCopy.getOffset(pvStructureInRecord,pvInRecord);
        pvFromRecord = pvCopy.getPVField(offset);
        pvFromCopy = pvIterator.getPVField(offset);
        assertTrue(pvInRecord==pvFromRecord);
        assertTrue(pvFromCopy.getField().getFieldName().equals("message"));
        

        PVStructure empty = dataCreate.createPVStructure(null, "", new Field[0]);
        PVCopy pvCopyAll = PVCopyFactory.create(pvRecord, empty);
        PVStructure pvCopyRecord = pvCopyAll.createPVStructure();
        System.out.println(pvCopyRecord.toString());   
    }
}

