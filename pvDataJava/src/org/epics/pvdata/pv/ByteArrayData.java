/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata.pv;

/**
 * Class required by get/put PVByteArray methods.
 * Get will set data and offset.
 * @author mrk
 *
 */
public class ByteArrayData {
    /**
     * The byte[].
     * PVByteArray.get sets this value.
     */
    public byte[] data;
    /**
     * The offset.
     * PVByteArray.get sets this value.
     */
    public int offset;
}
