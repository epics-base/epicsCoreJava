/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.xml;

import java.util.Map;

/**
 * Methods implemented by a listener for IOC XML processing.
 * @author mrk
 *
 */
public interface IncludeSubstituteXMLListener {
    /**
     * No more input. 
     */
    void endDocument();
    /**
     * Start of a new element.
     * The attributes values are after any substitutions.
     * @param name The element tag name.
     * @param attributes Attributes for the element.
     */
    void startElement(String name, Map<String,String> attributes);
    /**
     * The element content after any substitutions.
     * Leading and trailing white space from any content fragment are removed.
     * @param content The content.
     */
    void element(String content);
    /**
     * The end of the element.
     * @param name The tag name.
     */
    void endElement(String name);
}
