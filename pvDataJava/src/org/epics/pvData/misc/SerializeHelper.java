/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.misc;

import java.nio.ByteBuffer;

/**
 * Serialization helper.
 */
public final class SerializeHelper {

	/**
	 * Serialize array size.
	 * @param s size to encode.
	 * @param buffer serizalization buffer.
	 */
	public final static void writeSize(final int s, ByteBuffer buffer) {
		if (s == -1)					// null
			buffer.put((byte)-1);
		else if (s < 254)
			buffer.put((byte)s);
		else
			buffer.put((byte)-2).putInt(s);	// (byte)-2 + size
	}

	/**
	 * Deserializa array size.
	 * @param buffer deserialization buffer.
	 * @return array size.
	 */
	public final static int readSize(ByteBuffer buffer)
	{
		final byte b = buffer.get();
		if (b == -1)
			return -1;
		else if (b == -2) {
			final int s = buffer.getInt();
			if (s < 0)
				throw new RuntimeException("negative array size");
			return s;
		}
		else
			return (int)(b < 0 ? b + 256 : b);
	}

	/**
	 * String serializaton helper method.
	 * @see org.epics.pvData.pv.Serializable#serialize(java.nio.ByteBuffer)
	 */
	public final static void serializeString(final String value, ByteBuffer buffer) {
		if (value == null)
			writeSize(-1, buffer);
		else {
			writeSize(value.length(), buffer);
			buffer.put(value.getBytes());	// UTF-8
		}
	}

	/**
	 * String serializaton helper method.
	 * @see org.epics.pvData.pv.Serializable#deserialize(java.nio.ByteBuffer)
	 */
	public final static String deserializeString(ByteBuffer buffer) {
		int size = SerializeHelper.readSize(buffer);
		if (size >= 0) {
			byte[] bytes = new byte[size];
			buffer.get(bytes);
			return new String(bytes);
		}
		else
			return null;
	}

}
