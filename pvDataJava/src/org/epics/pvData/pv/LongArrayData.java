/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.pv;
/**
 * Class required by get/put PVLongArray methods.
 * Get will set data and offset.
 * @author mrk
 *
 */
public class LongArrayData {
    /**
     * The PVLong[].
     * PVLongArray.get sets this value.
     * PVLongArray.put requires that the caller set the value. 
     */
    public long[] data;
    /**
     * The offset.
     * PVLongArray.get sets this value.
     * PVLongArray.put requires that the caller set the value. 
     */
    public int offset;
}
