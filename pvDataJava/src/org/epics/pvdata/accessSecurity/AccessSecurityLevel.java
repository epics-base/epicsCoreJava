/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.accessSecurity;

/**
 * Interface for assigning names to access security levels
 * @author mrk
 *
 */
public interface AccessSecurityLevel {
    /**
     * Get the name associated with an access security level.
     *
     * @param level the level
     * @return the name associated with the level
     * @throws IndexOutOfBoundsException if the level does not exist
     */
    String getName(int level) throws IndexOutOfBoundsException;

    /**
     * Get the access security level with the specified name.
     *
     * @param name the name
     * @return the access security level. An exception is thrown for an illegal name.
     * @throws NoSuchFieldException if name is illegal
     */
    int getLevel(String name) throws NoSuchFieldException;

    /**
     * Get an array of names for each access security level.
     *
     * @return a String array of names
     */
    String[] getNames();
}
