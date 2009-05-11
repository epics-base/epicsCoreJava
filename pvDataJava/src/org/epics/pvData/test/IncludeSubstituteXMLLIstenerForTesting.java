/**
 * 
 */
package org.epics.pvData.test;
import java.util.Map;

import org.epics.pvData.xml.IncludeSubstituteXMLListener;

/**
 * @author mrk
 *
 */
public class IncludeSubstituteXMLLIstenerForTesting implements IncludeSubstituteXMLListener {    
    /* (non-Javadoc)
     * @see org.epics.pvData.xml.IncludeSubstituteXMLListener#element(java.lang.String)
     */
    public void element(String content) {
        System.out.println("element " + content);
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.xml.IncludeSubstituteXMLListener#endDocument()
     */
    public void endDocument() {
        System.out.println("endDocument");
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.xml.IncludeSubstituteXMLListener#endElement(java.lang.String)
     */
    public void endElement(String name) {
        System.out.println("endElement " + name);
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.xml.IncludeSubstituteXMLListener#startElement(java.lang.String, java.util.Map)
     */
    public void startElement(String name, Map<String, String> attributes) {
        System.out.println("startElement " + name);
    }
}
