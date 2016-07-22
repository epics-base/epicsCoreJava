/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.pv;

/**
 * Process Variable Data Type.
 * @author mrk
 *
 */
public enum Type {
    /**
     * The type is a scalar and has a ScalarType.
     */
    scalar,
    /**
     *  The type is an array of scalars.
     */
    scalarArray,
    /**
     * The type is a structure.
     */
    structure,
    /**
     * The type is an array of structures.
     */
    structureArray,
    /**
     * The type is an union.
     */
    union,
    /**
     * The type is an array of unions.
     */
    unionArray;    
}
