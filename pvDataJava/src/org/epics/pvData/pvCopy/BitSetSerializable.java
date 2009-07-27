/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.pvCopy;

import java.nio.ByteBuffer;

import org.epics.pvData.misc.BitSet;



/**
 * Base interface for partital serializaion.
 * @author mse
 *
 */
public interface BitSetSerializable {
	
	/**
     * Serialize field into given buffer.
	 * @param buffer serialization buffer.
     * @param bitSet The BitSet which shows the fields to serialize.
	 */
	void serialize(ByteBuffer buffer, BitSet bitSet);

	/**
     * Deserialize buffer.
	 * @param buffer serialization buffer.
     * @param bitSet The BitSet which shows the fields to deserialize.
	 */
     void deserialize(ByteBuffer buffer, BitSet bitSet);
}
