package org.epics.pvaccess.client.pvds.util;

/**
 * Serializes an object to a byte array
 * @author msekoranja
 * @param <T> object type to be handled by this serializator.
 */
public interface ToByteArraySerializator<T>
{
	/**
	 * Serialize <code>object</code> to a <code>byte[]</code>.
	 * @param object object to be serialized.
	 * @return byte array.
	 */
	byte[] toBytes(T object);
}
