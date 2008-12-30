/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.pv;
/**
 * Class required by get/put PVShortArray methods.
 * Get will set data and offset.
 * @author mrk
 *
 */
public class ShortArrayData {
    /**
     * The PVShort[].
     * PVShortArray.get sets this value.
     * PVShortArray.put requires that the caller set the value. 
     */
    public short[] data;
    /**
     * The offset.
     * PVShortArray.get sets this value.
     * PVShortArray.put requires that the caller set the value. 
     */
    public int offset;
}
