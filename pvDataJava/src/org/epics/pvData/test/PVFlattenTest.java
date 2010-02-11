/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.test;

import junit.framework.TestCase;

import org.epics.pvData.factory.PVDataFactory;
import org.epics.pvData.factory.PVDatabaseFactory;
import org.epics.pvData.pv.PVDataCreate;
import org.epics.pvData.pv.PVDatabase;
import org.epics.pvData.pv.PVField;
import org.epics.pvData.pv.PVRecord;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.Requester;
import org.epics.pvData.xml.XMLToPVDatabaseFactory;



/**
 * JUnit test for pvAccess.
 * It also provides examples of how to create a PVField[] that has all the fields in a structure..
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
        PVStructure pvStructure = pvRecord.getPVStructure();
        PVField[] pvFields = dataCreate.flattenPVStructure(pvStructure);
        assertTrue(pvFields.length==pvStructure.getNumberFields());
        int offset = 0;
        for(PVField pvField: pvFields) {
            PVField fromPVRecord = pvStructure.getSubField(offset);
            assertTrue(pvField==fromPVRecord);
            offset++;
        }
        PVStructure pvCurrent = (PVStructure)pvStructure.getSubField("current");
        pvFields = dataCreate.flattenPVStructure(pvCurrent);
        assertTrue(pvFields.length==(pvCurrent.getNextFieldOffset()-pvCurrent.getFieldOffset()));
        offset = pvCurrent.getFieldOffset();
        for(PVField pvField: pvFields) {
            PVField fromPVRecord = pvStructure.getSubField(offset);
            assertTrue(pvField==fromPVRecord);
            offset++;
        }
        System.out.println("PVFlattenTest OK");
    }
}

