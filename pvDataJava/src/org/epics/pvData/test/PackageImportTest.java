/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.test;

import junit.framework.TestCase;

import org.epics.pvData.factory.PVDatabaseFactory;
import org.epics.pvData.factory.PVReplaceFactory;
import org.epics.pvData.pv.PVDatabase;
import org.epics.pvData.pv.PVRecord;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.Requester;
import org.epics.pvData.xml.XMLToPVDatabaseFactory;


/**
 * JUnit test for DBListener.
 * @author mrk
 *
 */
public class PackageImportTest extends TestCase {
    /**
     * test DBListener.
     */
    public static void testXML() {
    	PVDatabase master = PVDatabaseFactory.getMaster();
        Requester iocRequester = new RequesterForTesting("packageImportTest");
        XMLToPVDatabaseFactory.convert(master,"test/packageImport/package1.xml", iocRequester);
        XMLToPVDatabaseFactory.convert(master,"test/packageImport/package2.xml", iocRequester);
        XMLToPVDatabaseFactory.convert(master,"test/packageImport/packageExtend.xml", iocRequester);
        XMLToPVDatabaseFactory.convert(master,"test/packageImport/records.xml", iocRequester);
        PVReplaceFactory.replace(master);
        PVRecord[] pvRecords = master.getRecords();
        PVStructure[] pvStructurtes = master.getStructures();
        for(PVStructure pvStructure : pvStructurtes) {
            String value = pvStructure.toString();
            System.out.println(value);
        }
        for(PVRecord pvRecord : pvRecords) {
            String value = pvRecord.toString();
            System.out.println(value);
        }
    }
}
