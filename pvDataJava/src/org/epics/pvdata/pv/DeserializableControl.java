/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata.pv;

import java.nio.ByteBuffer;


/**
 * Deserialization control interface.
 * @author mse
 */
public interface DeserializableControl {

	/**
	 * Helper method. Ensures specified size of bytes, provides it if necessary.
	 * @param size
	 */
	void ensureData(int size);
	
	/**
	 * Align buffer.
	 * Note that this takes care only current buffer alignment. If streaming protocol is used,
	 * care must be taken that entire stream is aligned.
	 * @param alignment size in bytes, must be power of two. 
	 */
	void alignData(int alignment);
	
	/**
	 * Deserialize <i>Field</i> instance via cache.
	 * @param buffer Buffer to be deserialized from.
	 * @param deserialized <i>Field</i> instance.
	 */
	Field cachedDeserialize(ByteBuffer buffer);
	
}
