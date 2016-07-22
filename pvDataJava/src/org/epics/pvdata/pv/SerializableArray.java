/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.pv;

import java.nio.ByteBuffer;



/**
 * Base interface for array serializaion.
 * @author mse
 *
 */
public interface SerializableArray extends Serializable {
	
    /**
     * Serialize field into given buffer.
     * 
	 * @param buffer serialization buffer
	 * @param flusher flush interface
	 * @param offset offset in array
	 * @param count number of elements in array
	 */
	void serialize(ByteBuffer buffer, SerializableControl flusher, int offset, int count);
}
