/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata.pv;

/**
 * Class required by get/put PVUnionArray methods.
 * Get will set data and offset.
 * @author mse
 *
 */
public class UnionArrayData {
    /**
     * The PVUnion[].
     * PVUnionArray.get sets this value.
     */
    public PVUnion[] data;
    /**
     * The offset.
     * PVUnionArray.get sets this value. 
     */
    public int offset;
}
