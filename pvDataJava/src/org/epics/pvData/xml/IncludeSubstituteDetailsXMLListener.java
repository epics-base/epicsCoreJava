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
public interface IncludeSubstituteDetailsXMLListener {
    /**
     * The original startElement attributes before any substitutions.
     * @param name The element tag name.
     * @param attributes The original attributes.
     */
    void startElementBeforeSubstitution(String name, Map<String,String> attributes);
    /**
     * The element content before any substitutions.
     * @param content The content.
     */
    void elementBeforeSubstitution(String content);
    /**
     * Beginning of a new source file.
     * This is called for every file including the file passed to IncludeSubstituteXMLReader.parse
     * @param fileName The new source filename.
     * If the file name contains ${xxx} the fileName returned is not expanded.
     */
    void newSourceFile(String fileName);
    /**
     * End of the current source file. This is not called for every file including the file passed to IncludeSubstituteXMLReader.parse.
     */
    void endSourceFile();
    /**
     * A path is being added.
     * @param pathName The path name.
     */
    void addPath(String pathName);
    /**
     * A path is being removed.
     * @param pathName The pathname.
     */
    void removePath(String pathName);
    /**
     * A new substitution is being defined.
     * If a substitution with the from name already exists the old value is replaced with the new value. 
     * @param from The from value which appears as ${value}
     * @param to The replacemnt for ${value"
     */
    void substitute(String from,String to);
    /**
     * Remove a substitution.
     * @param from The substitution to remove.
     */
    void removeSubstitute(String from);
}
