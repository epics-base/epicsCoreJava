/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.test;

import junit.framework.TestCase;

import org.epics.pvData.factory.PVDatabaseFactory;
import org.epics.pvData.pv.PVDatabase;
import org.epics.pvData.pv.PVRecord;
import org.epics.pvData.pv.Requester;
import org.epics.pvData.xml.IncludeSubstituteDetailsXMLListener;
import org.epics.pvData.xml.XMLToPVDatabaseFactory;
/**
 * JUnit test for XMLToIOCDB.
 * This also is a test for pv, dbd, and db because XMLToDBD
 * is called, which makes extensive use of dbd and pv, and
 * XMLToIOCDB is called, which makes extensive use of db.
 * It also provides an example of parsing database definitions.
 * The output is a dump of all the record instance files it reads.
 * @author mrk
 *
 */
public class SubstituteTest extends TestCase {
    private static PVDatabase master = PVDatabaseFactory.getMaster();
        
    /**
     * test XMLToIOCDB.
     */
    public static void testSubstitute() {
        Requester iocRequester = new RequesterForTesting("accessTest");
        XMLToPVDatabaseFactory.convert(master,"${JAVAIOC}/xml/structures.xml", iocRequester,true,null,null,null);
        IncludeSubstituteDetailsXMLListener listener = new IncludeSubstituteDetailsXMLLIstenerForTesting();
        XMLToPVDatabaseFactory.convert(master,
            "src/org/epics/pvData/test/substituteDB.xml",iocRequester,true,null,null,listener);
        System.out.printf("%n%nrecord list%n");
        PVRecord[] pvRecords = master.getRecords();
        for(PVRecord pvRecord : pvRecords) {
            System.out.printf("%n%s",pvRecord.getRecordName());
        }
        System.out.printf("%n%nrecord contents%n");
        for(PVRecord pvRecord : pvRecords) {
            System.out.print(pvRecord.toString());
        }
    }
}
