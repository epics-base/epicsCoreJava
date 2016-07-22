/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.pv;

import java.nio.ByteBuffer;


/**
 * Deserialization control interface.
 * @author mse
 */
public interface DeserializableControl {

	/**
	 * Ensures that the specified number of bytes are available for deserialization.
     *
	 * @param size the number of bytes
	 */
	void ensureData(int size);
	
	/**
	 * Align buffer.
	 * Note that this takes care only current buffer alignment.
	 * If streaming protocol is used, care must be taken that entire stream is aligned.
     *
	 * @param alignment size in bytes, must be power of two 
	 */
	void alignData(int alignment);
	
	/**
	 * Deserialize <i>Field</i> instance via cache.
     *
	 * @param buffer Buffer to be deserialized from
	 * @return the Field resulting from the deserialization
	 */
	Field cachedDeserialize(ByteBuffer buffer);
	
}
