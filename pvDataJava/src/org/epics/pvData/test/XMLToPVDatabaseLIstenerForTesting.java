/**
 * 
 */
package org.epics.pvData.test;
import java.util.Map;
import java.util.Set;

import org.epics.pvData.pv.PVArray;
import org.epics.pvData.pv.PVRecord;
import org.epics.pvData.pv.PVScalar;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.xml.XMLToPVDatabaseListener;

/**
 * @author mrk
 *
 */
public class XMLToPVDatabaseLIstenerForTesting implements XMLToPVDatabaseListener {

    /* (non-Javadoc)
     * @see org.epics.pvData.xml.XMLToPVDatabaseListener#endArray()
     */
    public void endArray() {
        System.out.println("endArray");
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.xml.XMLToPVDatabaseListener#endAuxInfo()
     */
    public void endAuxInfo() {
        System.out.println("endAuxInfo");
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.xml.XMLToPVDatabaseListener#endRecord()
     */
    public void endRecord() {
        System.out.println("endRecord");
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.xml.XMLToPVDatabaseListener#endScalar()
     */
    public void endScalar() {
        System.out.println("endScalar");
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.xml.XMLToPVDatabaseListener#endStructure()
     */
    public void endStructure() {
        System.out.println("endStructure");
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.xml.XMLToPVDatabaseListener#endStructureField()
     */
    public void endStructureField() {
        System.out.println("endStructureField");
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.xml.XMLToPVDatabaseListener#newStructureField(org.epics.pvData.pv.PVStructure)
     */
    public void newStructureField(PVStructure pvStructure) {
        System.out.println("newStructureField " + pvStructure.getFullName());
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.xml.XMLToPVDatabaseListener#startArray(org.epics.pvData.pv.PVArray)
     */
    public void startArray(PVArray pvArray) {
        System.out.println("startArray " + pvArray.getFullName());
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.xml.XMLToPVDatabaseListener#startAuxInfo(java.lang.String, java.util.Map)
     */
    public void startAuxInfo(String name, Map<String, String> attributes) {
        System.out.println("startAuxInfo " + name);
        Set<Map.Entry<String, String>> set = attributes.entrySet();
        for(Map.Entry<String,String> entry : set) {
            String key = entry.getKey();
            String value = attributes.get(key);
            if(value.indexOf("${")>=0) {
                System.out.println("   " +key + " is " + value);
            }
        }
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.xml.XMLToPVDatabaseListener#startRecord(org.epics.pvData.pv.PVRecord)
     */
    public void startRecord(PVRecord pvRecord) {
        System.out.println("startRecord " + pvRecord.getRecordName());
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.xml.XMLToPVDatabaseListener#startScalar(org.epics.pvData.pv.PVScalar)
     */
    public void startScalar(PVScalar pvScalar) {
        System.out.println("startScalar " + pvScalar.getFullName());
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.xml.XMLToPVDatabaseListener#startStructure(org.epics.pvData.pv.PVStructure)
     */
    public void startStructure(PVStructure pvStructure) {
        System.out.println("startStructure " + pvStructure.getFullName());
    }

}
