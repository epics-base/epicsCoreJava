/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.pv;

import java.nio.ByteBuffer;



/**
 * Base interface for serializaion.
 * @author mse
 *
 */
public interface Serializable {
	
	/**
     * Serialize field into given buffer.
	 * @param buffer serialization buffer.
	 * @param flusher flush interface.
	 */
	void serialize(ByteBuffer buffer, SerializableControl flusher);

	/**
     * Deserialize buffer.
	 * @param buffer serialization buffer.
	 * @param control deserialization control.
	 */
     void deserialize(ByteBuffer buffer, DeserializableControl control);
}
