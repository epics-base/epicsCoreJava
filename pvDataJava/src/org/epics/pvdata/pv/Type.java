/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
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
