/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.test;

import junit.framework.TestCase;

import org.epics.pvData.factory.*;
import org.epics.pvData.factory.FieldFactory;
import org.epics.pvData.factory.PVDataFactory;
import org.epics.pvData.factory.PVDatabaseFactory;
import org.epics.pvData.pv.*;
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
public class PVFlattenTest extends TestCase {
    private static PVDatabase master = PVDatabaseFactory.getMaster();
    private static PVDataCreate dataCreate = PVDataFactory.getPVDataCreate();
    
    public static void testPVFlatten() {
        Requester iocRequester = new RequesterForTesting("accessTest");
        XMLToPVDatabaseFactory.convert(master,"${JAVAIOC}/xml/structures.xml", iocRequester);
        XMLToPVDatabaseFactory.convert(master,"${JAVAIOC}/test/powerSupply/powerSupplyDB.xml", iocRequester);
        PVRecord pvRecord = master.findRecord("psEmbeded");
        
        PVField[] pvFields = dataCreate.flattenPVStructure(pvRecord);
        assertTrue(pvFields.length==(pvRecord.getNextFieldOffset()-pvRecord.getFieldOffset()));
        int offset = 0;
        for(PVField pvField: pvFields) {
            PVField fromPVRecord = pvRecord.getSubField(offset);
            assertTrue(pvField==fromPVRecord);
            offset++;
        }
        PVStructure pvCurrent = (PVStructure)pvRecord.getSubField("current");
        pvFields = dataCreate.flattenPVStructure(pvCurrent);
        assertTrue(pvFields.length==(pvCurrent.getNextFieldOffset()-pvCurrent.getFieldOffset()));
        offset = pvCurrent.getFieldOffset();
        for(PVField pvField: pvFields) {
            PVField fromPVRecord = pvRecord.getSubField(offset);
            assertTrue(pvField==fromPVRecord);
            offset++;
        }
        System.out.println("PVFlattenTest OK");
    }
}

