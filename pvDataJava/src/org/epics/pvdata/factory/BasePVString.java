/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.factory;

import java.nio.ByteBuffer;

import org.epics.pvdata.misc.SerializeHelper;
import org.epics.pvdata.pv.BoundedString;
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
    protected final int maxLength;

    /**
     * Constructor
     * @param scalar The introspection interface.
     */
    public BasePVString(Scalar scalar) {
        super(scalar);
        if (scalar instanceof BoundedString)
        	maxLength = ((BoundedString)scalar).getMaximumLength();
        else
        	maxLength = 0;
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVString#get()
     */
    public String get() {
        return value;
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVString#put(java.lang.String)
     */
    public void put(String value) {
        if (super.isImmutable()) {
            throw new IllegalArgumentException("field is immutable");
        } else if (maxLength > 0 && value.length() > maxLength) {
            throw new IllegalArgumentException("string length out of bounds");
        }
        this.value = value;
        super.postPut();

    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.Serializable#serialize(java.nio.ByteBuffer, org.epics.pvdata.pv.SerializableControl)
     */
    public void serialize(ByteBuffer buffer, SerializableControl flusher) {
        SerializeHelper.serializeString(value, buffer, flusher);
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.SerializableArray#serialize(java.nio.ByteBuffer, org.epics.pvdata.pv.SerializableControl, int, int)
     */
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
