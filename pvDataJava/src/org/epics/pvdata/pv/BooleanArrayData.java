/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata.pv;

/**
 * Class required by get/put PVBooleanArray methods.
 * Get will set data and offset.
 * @author mrk
 *
 */
public class BooleanArrayData {
    /**
     * The boolean[].
     * PVBooleanArray.get sets this value.
     * PVBooleanArray.put requires that the caller set the value. 
     */
    public boolean[] data;
    /**
     * The offset.
     * PVBooleanArray.get sets this value.
     * PVBooleanArray.put requires that the caller set the value. 
     */
    public int offset;
}
