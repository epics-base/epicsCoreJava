/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
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
