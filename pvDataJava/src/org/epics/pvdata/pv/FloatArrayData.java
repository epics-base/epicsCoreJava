/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata.pv;
/**
 * Class required by get/put PVFloatArray methods.
 * Get will set data and offset.
 * @author mrk
 *
 */
public class FloatArrayData {
    /**
     * The float[].
     * PVFloatArray.get sets this value.
     */
    public float[] data;
    /**
     * The offset.
     * PVFloatArray.get sets this value. 
     */
    public int offset;
}
