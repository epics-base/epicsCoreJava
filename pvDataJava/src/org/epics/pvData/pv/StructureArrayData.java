/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.pv;

/**
 * Class required by get/put PVDoubleArray methods.
 * Get will set data and offset.
 * @author mrk
 *
 */
public class StructureArrayData {
    /**
     * The PVStructure[].
     * PVStructureArray.get sets this value.
     */
    public PVStructure[] data;
    /**
     * The offset.
     * PVStructureArray.get sets this value. 
     */
    public int offset;
}
