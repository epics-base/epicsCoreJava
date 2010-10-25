/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.misc;

import java.nio.ByteBuffer;

import org.epics.pvData.pv.DeserializableControl;
import org.epics.pvData.pv.SerializableControl;

/**
 * Serialization helper.
 */
public final class SerializeHelper {

	/**
	 * Serialize array size.
	 * @param s size to encode.
	 * @param buffer serialization buffer.
	 * @param flusher flusher.
	 */
	public final static void writeSize(final int s, ByteBuffer buffer, SerializableControl flusher) {
		flusher.ensureBuffer(Long.SIZE/Byte.SIZE + 1);
		writeSize(s, buffer);
	}

	/**
	 * Serialize array size.
	 * @param s size to encode.
	 * @param buffer serialization buffer.
	 */
	private final static void writeSize(final int s, ByteBuffer buffer) {
		if (s == -1)					// null
			buffer.put((byte)-1);
		else if (s < 254)
			buffer.put((byte)s);
		else
			buffer.put((byte)-2).putInt(s);	// (byte)-2 + size
	}

	/**
	 * Deserialize array size.
	 * @param buffer deserialization buffer.
	 * @return array size.
	 */
	public final static int readSize(ByteBuffer buffer, DeserializableControl control)
	{
		control.ensureData(1);
		final byte b = buffer.get();
		if (b == -1)
			return -1;
		else if (b == -2) {
			control.ensureData(Integer.SIZE/Byte.SIZE);
			final int s = buffer.getInt();
			if (s < 0)
				throw new RuntimeException("negative array size");
			return s;
		}
		else
			return (int)(b < 0 ? b + 256 : b);
	}

	/**
	 * String serialization helper method.
	 */
	public final static void serializeString(final String value, ByteBuffer buffer, SerializableControl flusher) {
		if (value == null)
			writeSize(-1, buffer, flusher);
		else {
			final int len = value.length(); 
			writeSize(len, buffer, flusher);
			int i = 0;
			while (true) {
				final int maxToWrite = Math.min(len-i, buffer.remaining());
				buffer.put(value.getBytes(), i, maxToWrite);	// UTF-8
				i += maxToWrite;
				if (i < len)
					flusher.flushSerializeBuffer();
				else
					break;
			}
		}
	}

	/**
	 * String serialization helper method.
	 */
	public final static void serializeSubstring(final String value, int offset,
			int count, ByteBuffer buffer, SerializableControl flusher) {
		if (value == null)
			writeSize(-1, buffer, flusher);
		else {
			writeSize(count, buffer, flusher);
			int i = 0;
			while (true) {
				final int maxToWrite = Math.min(count - i, buffer.remaining());
				buffer.put(value.getBytes(), i + offset, maxToWrite); // UTF-8
				i += maxToWrite;
				if (i < count)
					flusher.flushSerializeBuffer();
				else
					break;
			}
		}
	}

	/**
	 * String serializaton helper method.
	 */
	public final static String deserializeString(ByteBuffer buffer, DeserializableControl control) {
		int size = SerializeHelper.readSize(buffer, control);
		if (size >= 0) {
			byte[] bytes = new byte[size];
			int i = 0;
			while (true)
			{
				final int toRead = Math.min(size-i, buffer.remaining());
				buffer.get(bytes, i, toRead);
				i += toRead;
				if (i < size)
					control.ensureData(1);
				else
					break;
			}
			return new String(bytes);
		}
		else
			return null;
	}

}
