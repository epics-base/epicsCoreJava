/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.nt;

/**
 * Utility methods for NT types.
 * 
 * @author dgh
 */
public class NTUtils
{

    /**
     * Checks whether NT types are compatible by checking their IDs,
     * i.e. their names and major version must match.
     *
     * @param u1 the first URI
     * @param u2 the second URI
     * @return true if URIs are compatible, false otherwise
     */
    public static boolean is_a(String u1, String u2)
    {
        int majorsionEndIndex1 = u1.lastIndexOf('.');
        String su1 = majorsionEndIndex1 >= 0 ? 
            u1.substring(0, majorsionEndIndex1) : u1;

        int majorsionEndIndex2 = u2.lastIndexOf('.');
        String su2 = majorsionEndIndex2 >= 0 ? 
            u2.substring(0, majorsionEndIndex2) : u2;
        return su1.equals(su2);
    }

    // disable object creation
    private NTUtils() {}
}

