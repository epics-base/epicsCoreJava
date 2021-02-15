/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.factory;

import java.nio.ByteBuffer;

import org.epics.pvdata.misc.SerializeHelper;
import org.epics.pvdata.pv.BoundedString;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.SerializableControl;

/**
 * Class for implementing a bounded string.
 * @author mse
 */
public class BaseBoundedString extends BaseScalar implements BoundedString {

	public final int maxLength;

	/**
	 * Constructor
	 * @param maxLength The maximum length the string can have.
	 */
	public BaseBoundedString(int maxLength) {
		super(ScalarType.pvString);

		if (maxLength <= 0)
			throw new IllegalArgumentException("maxLength <= 0");

		this.maxLength = maxLength;
	}

	/* (non-Javadoc)
	 * @see org.epics.pvdata.factory.BaseScalar#getID()
	 */
	@Override
	public String getID() {
		return super.getID() + '(' + maxLength + ')';
	}

	/* (non-Javadoc)
	 * @see org.epics.pvdata.factory.BaseScalar#serialize(java.nio.ByteBuffer, org.epics.pvdata.pv.SerializableControl)
	 */
	@Override
	public void serialize(ByteBuffer buffer, SerializableControl control) {
		control.ensureBuffer(1);
		buffer.put((byte)0x83);
		SerializeHelper.writeSize(maxLength, buffer, control);
	}

	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.BoundedString#getMaximumLength()
	 */
	public int getMaximumLength() {
		return maxLength;
	}

	/* (non-Javadoc)
	 * @see org.epics.pvdata.factory.BaseScalar#hashCode()
	 */
	@Override
	public int hashCode() {
		return super.hashCode() | maxLength << 4;
	}

	/* (non-Javadoc)
	 * @see org.epics.pvdata.factory.BaseScalar#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		final BaseBoundedString other = (BaseBoundedString) obj;
		return (maxLength == other.maxLength);
	}
}
