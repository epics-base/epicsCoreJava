/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.xml;

import org.epics.pvData.pv.MessageType;

/**
 * The reader for IOC XML processing.
 * This is an abstraction above the XML SAX reader.
 * @author mrk
 *
 */
public interface PVXMLReader {
    /**
     * Parse an XML file.
     * Include and macro substitution are done before the listener is called.
     * If a file is already being parsed the listener will be given an error message and parse will just return.
     * All errors result in a call to listener.errorMessage.
     * @param rootElementName The root element tag name.
     * The root file and any included files must have the same rootElementName.
     * @param fileName The file.
     * @param listener The callback listener.
     */
    void parse(String rootElementName,String fileName, PVXMLListener listener);
    /**
     * Message.
     * The current location in the xml files together with the message are given to listener.message.
     * @param message The message.
     * @param messageType The type of message.
     */
    void message(String message,MessageType messageType);
}
