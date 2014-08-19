/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
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
	
	public BaseBoundedString(int maxLength) {
		super(ScalarType.pvString);

		if (maxLength <= 0)
			throw new IllegalArgumentException("maxLength <= 0");
		
		this.maxLength = maxLength;
	}

	@Override
	public String getID() {
		return super.getID() + '(' + maxLength + ')';
	}

	@Override
	public void serialize(ByteBuffer buffer, SerializableControl control) {
		control.ensureBuffer(1);
		buffer.put((byte)0x83);
		SerializeHelper.writeSize(maxLength, buffer, control);
	}

	@Override
	public int getMaximumLength() {
		return maxLength;
	}

	@Override
	public int hashCode() {
		return super.hashCode() | maxLength << 4;
	}

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
