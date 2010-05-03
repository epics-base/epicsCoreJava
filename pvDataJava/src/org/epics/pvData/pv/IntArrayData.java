/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.pv;
/**
 * Class required by get/put PVIntArray methods.
 * Get will set data and offset.
 * @author mrk
 *
 */
public class IntArrayData {
    /**
     * The int[].
     * PVIntArray.get sets this value.
     */
    public int[] data;
    /**
     * The offset.
     * PVIntArray.get sets this value.
     */
    public int offset;
}
