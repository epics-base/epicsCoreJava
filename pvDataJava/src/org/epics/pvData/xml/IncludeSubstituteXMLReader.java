/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.xml;

import org.epics.pvData.pv.MessageType;
import org.epics.pvData.pv.Requester;

/**
 * A reader for xml that processes include and macro substitution and passes the processed information to
 * the IncludeSubstituteXMLListener.
 * This is an abstraction above the XML SAX reader.
 * @author mrk
 *
 */
public interface IncludeSubstituteXMLReader {
    /**
     * Parse an XML file.
     * Include and macro substitution are done before the listener is called.
     * If a file is already being parsed the listener will be given an error message and parse will just return.
     * All errors result in a call to listener.errorMessage.
     * @param rootElementName The root element tag name.
     * The root file and any included files must have the same rootElementName.
     * @param fileName The file.
     * @param requester The requester.
     * @param reportSubstitutionFailure Should an error be reported if a ${from} does not have a substitution. 
     * @param listener The IncludeSubstituteXMLListener listener.
     * @param detailsListener The IncludeSubstituteDetailsXMLListener listener.
     */
    void parse(String rootElementName,String fileName,Requester requester,boolean reportSubstitutionFailure,
        IncludeSubstituteXMLListener listener,
        IncludeSubstituteDetailsXMLListener detailsListener);
    /**
     * Message.
     * The current location in the xml files together with the message are given to listener.message.
     * @param message The message.
     * @param messageType The type of message.
     */
    void message(String message,MessageType messageType);
}
