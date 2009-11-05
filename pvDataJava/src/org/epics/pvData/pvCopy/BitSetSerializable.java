/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.pvCopy;

import java.nio.ByteBuffer;

import org.epics.pvData.misc.BitSet;
import org.epics.pvData.pv.DeserializableControl;
import org.epics.pvData.pv.SerializableControl;



/**
 * Base interface for partital serializaion.
 * @author mse
 *
 */
public interface BitSetSerializable {
	
	/**
     * Serialize field into given buffer.
	 * @param buffer serialization buffer
	 * @param flusher flush interface.
     * @param bitSet The BitSet which shows the fields to serialize.
	 */
	void serialize(ByteBuffer buffer, SerializableControl flusher, BitSet bitSet);

	/**
     * Deserialize buffer.
	 * @param buffer serialization buffer.
	 * @param control deserialization control instance.
     * @param bitSet The BitSet which shows the fields to deserialize.
	 */
     void deserialize(ByteBuffer buffer, DeserializableControl control, BitSet bitSet);
}
