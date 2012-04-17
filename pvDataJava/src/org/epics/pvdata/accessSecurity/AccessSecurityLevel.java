/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
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
     * @param level The level.
     * @return The name associated with the level. Throws an exception if the level does not exist.
     * @throws IndexOutOfBoundsException
     */
    String getName(int level) throws IndexOutOfBoundsException;
    /**
     * Get the access security level with the specified name.
     * @param name The name.
     * @return The access security level. An exception is thrown for an illegal name.
     * @throws NoSuchFieldException
     */
    int getLevel(String name) throws NoSuchFieldException;
    /**
     * Get an array of names for each access security level.
     * @return An String[] of names.
     */
    String[] getNames();
}
