/**
 * 
 */
package org.epics.pvData.test;
import java.util.Map;
import java.util.Set;

import org.epics.pvData.pv.PVScalar;
import org.epics.pvData.xml.IncludeSubstituteDetailsXMLListener;

/**
 * @author mrk
 *
 */
public class IncludeSubstituteDetailsXMLLIstenerForTesting implements IncludeSubstituteDetailsXMLListener {

    /* (non-Javadoc)
     * @see org.epics.pvData.xml.IncludeSubstituteXMLListener#addPath(java.lang.String)
     */
    public void addPath(String pathName) {
        System.out.println("addPath " + pathName);
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.xml.IncludeSubstituteXMLListener#elementBeforeSubstitution(java.lang.String)
     */
    public void elementBeforeSubstitution(String content) {
        System.out.println("elementBeforeSubstitution " + content);
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.xml.IncludeSubstituteXMLListener#endSourceFile()
     */
    public void endSourceFile() {
        System.out.println("endSourceFile");
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.xml.IncludeSubstituteXMLListener#newSourceFile(java.lang.String)
     */
    public void newSourceFile(String fileName) {
        System.out.println("newSourceFile " + fileName);
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.xml.IncludeSubstituteXMLListener#removePath(java.lang.String)
     */
    public void removePath(String pathName) {
        System.out.println("removePath " + pathName);
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.xml.IncludeSubstituteXMLListener#removeSubstitute(java.lang.String)
     */
    public void removeSubstitute(String from) {
        System.out.println("removeSubstitute " + from);
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.xml.IncludeSubstituteXMLListener#startElementBeforeSubstitution(java.lang.String, java.util.Map)
     */
    public void startElementBeforeSubstitution(String name,Map<String, String> attributes) {
        System.out.println("startElementBeforeSubstitution " + name);
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
     * @see org.epics.pvData.xml.IncludeSubstituteXMLListener#substitute(java.lang.String, java.lang.String)
     */
    public void substitute(String from, String to) {
        System.out.println("substitute from " + from + " to " + to);
    }
}
