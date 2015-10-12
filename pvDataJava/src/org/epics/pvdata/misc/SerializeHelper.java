/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata.misc;

import java.nio.ByteBuffer;

import org.epics.pvdata.pv.DeserializableControl;
import org.epics.pvdata.pv.SerializableControl;

/**
 * Serialization helper.
 */
public final class SerializeHelper {

	/**
	 * Serialize the specified array size into the specified buffer, flushing when necessary.
	 * The specified SerializableControl manages any flushing required.
	 * @param s       size to encode
	 * @param buffer  the buffer to be serialized into
	 * @param flusher the SerializableControl to manage the flushing
	 */
	public final static void writeSize(final int s, ByteBuffer buffer, SerializableControl flusher) {
		flusher.ensureBuffer(Long.SIZE/Byte.SIZE + 1);
		writeSize(s, buffer);
	}

	/**
	 * Serialize the specified array size into the specified buffer.
	 * @param s       size to encode
	 * @param buffer  the buffer to be serialized into
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
	 * Deserialize the array size from the specified buffer.
	 * The specified DeserializableControl ensures sufficient bytes are available.
	 * @param buffer the buffer to serialize from
	 * @param control the DeserializableControl
	 * @return the deserialized array size
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
	 * Deserialize the array size from the specified buffer.
	 * @param buffer the buffer to serialize from
	 * @return the deserialized array size
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
	 * Serialize the specified string into the specified buffer, flushing when necessary.
	 * The specified SerializableControl manages any flushing required.
	 * @param value   the string to be serialized
	 * @param buffer  the buffer to be serialized into
	 * @param flusher the SerializableControl to manage the flushing
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
	 * Serialize the specified string into the specified buffer.
	 * @param value   the string to be serialized
	 * @param buffer  the buffer to be serialized into
	 */
	public final static void serializeString(final String value, ByteBuffer buffer) {
		if (value == null)
			writeSize(-1, buffer);
		else {
			final int len = value.length(); 
			writeSize(len, buffer);
			buffer.put(value.getBytes());	// UTF-8
		}
	}


	/**
	 * Serialize a substring of a specified string into the specified buffer, flushing when necessary.
	 * The substring serialized is of the specified length and starts
	 * at the specified offset relative to supplied string.
	 * The specified SerializableControl manages any flushing required.
	 * @param value   the string from which a substring is to be serialized
	 * @param offset  the start of the substring relative to supplied string
	 * @param count   the length of the substring
	 * @param buffer  the buffer to be serialized into
	 * @param flusher the SerializableControl to manage the flushing
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
	 * Deserialize a string from the specified buffer.
	 * The specified DeserializableControl ensures sufficient bytes are available.
	 * @param buffer  the buffer to serialize from
	 * @param control the DeserializableControl
	 * @return the deserialized string
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

	/**
	 * Deserialize a string from the specified buffer.
	 * @param buffer  the buffer to serialize from
	 * @return the deserialized string
	 */
	public final static String deserializeString(ByteBuffer buffer) {
		int size = SerializeHelper.readSize(buffer);
		if (size >= 0) {
			byte[] bytes = new byte[size];
			buffer.get(bytes, 0, size);		// UTF-8
			return new String(bytes);
		}
		else
			return null;
	}
}
