/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */

package org.epics.pvdata.factory;

import java.nio.ByteBuffer;

import org.epics.pvdata.misc.SerializeHelper;
import org.epics.pvdata.pv.DeserializableControl;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.Scalar;
import org.epics.pvdata.pv.SerializableControl;

/**
 * Base class for PVString.
 * It provides a complete implementation can be extended.
 * @author mrk
 *
 */
public class BasePVString extends AbstractPVScalar implements PVString
{
    protected String value = "";
    
    public BasePVString(Scalar scalar) {
        super(scalar);
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVString#get()
     */
    @Override
    public String get() {
        return value;
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVString#put(java.lang.String)
     */
    @Override
    public void put(String value) {
        if(super.isImmutable()) {
            throw new IllegalArgumentException("field is immutable");
        }
        this.value = value;
        super.postPut();
        
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.Serializable#serialize(java.nio.ByteBuffer, org.epics.pvdata.pv.SerializableControl)
     */
    @Override
    public void serialize(ByteBuffer buffer, SerializableControl flusher) {
        SerializeHelper.serializeString(value, buffer, flusher);
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.SerializableArray#serialize(java.nio.ByteBuffer, org.epics.pvdata.pv.SerializableControl, int, int)
     */
    @Override
	public void serialize(ByteBuffer buffer, SerializableControl flusher, int offset, int count) {
		// check bounds
    	final int length = (value == null) ? 0 : value.length();
		if (offset < 0) offset = 0;
		else if (offset > length) offset = length;
		if (count < 0) count = length;

		final int maxCount = length - offset;
		if (count > maxCount)
			count = maxCount;
		
		// write
		SerializeHelper.serializeSubstring(value, offset, count, buffer, flusher);
	}
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.Serializable#deserialize(java.nio.ByteBuffer, org.epics.pvdata.pv.DeserializableControl)
     */
    @Override
    public void deserialize(ByteBuffer buffer, DeserializableControl control) {
        value = SerializeHelper.deserializeString(buffer, control);
    }
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        // TODO anything else?
        if (obj instanceof PVString) {
            PVString b = (PVString)obj;
            final String bv = b.get();
            if (bv != null)
                return bv.equals(value);
            else
                return value == null;
        }
        else
            return false;
    }
    /* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (value == null) ? 0 : value.hashCode();
	}
}
