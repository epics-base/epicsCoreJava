/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
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
