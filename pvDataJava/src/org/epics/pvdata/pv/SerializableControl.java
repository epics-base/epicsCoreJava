/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata.pv;

import java.nio.ByteBuffer;


/**
 * Flush control interface.
 * @author mse
 */
public interface SerializableControl {

	/**
	 * Request to flush serialization buffer.
	 * This call will block until buffer is not flushed.
	 * To be called when buffer is out of space.
	 */
	void flushSerializeBuffer();
	
	
	/**
	 * Helper method. Ensures specified size of bytes, flushes if necessary.
	 * @param size
	 */
	void ensureBuffer(int size);

	/**
	 * Align buffer.
	 * Note that this takes care only current buffer alignment. If streaming protocol is used,
	 * care must be taken that entire stream is aligned.
	 * @param alignment size in bytes, must be power of two. 
	 */
	void alignBuffer(int alignment);
	
	/**
	 * Serialize <i>Field</i> instance via cache.
	 * @param field <i>Field</i> instance to be serialized.
	 * @param buffer Buffer to be serialized to.
	 */
	void cachedSerialize(Field field, ByteBuffer buffer);
	
}
