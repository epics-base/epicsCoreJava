/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.xml;

import java.util.Map;

import org.epics.pvData.pv.MessageType;

/**
 * Methods implemented by a listener for IOC XML processing.
 * @author mrk
 *
 */
public interface PVXMLListener {
    /**
     * No more input. 
     */
    void endDocument();
    /**
     * Start of a new element.
     * @param name The element tag name.
     * @param attributes Attributes for the element.
     */
    void startElement(String name, Map<String,String> attributes);
    /**
     * Some characters for the element.
     * This can be called zero or more times between the calls to startElement and endElement.
     * @param ch The array of characters.
     * @param start The index of the first character.
     * @param length The number of characters.
     */
    void characters(char[] ch, int start, int length);
    /**
     * The end of the element.
     * @param name The tag name.
     */
    void endElement(String name);
    /**
     * Message.
     * This is called by IOCXMLReader.message when it is called..
     * @param message The message.
     * IOCXMLReader adds the location in the xml files when the message was generated.
     * @param messageType The type of message.
     */
    void message(String message,MessageType messageType);
}
