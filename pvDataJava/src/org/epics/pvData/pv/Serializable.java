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
	 */
	void serialize(ByteBuffer buffer);
	
    /**
     * Return size (in bytes) required to serialize this instance.
     * @return size (in bytes).
     */
    int getSerializationSize();

    /**
     * Deserialize buffer.
	 * @param buffer serialization buffer.
	 */
     void deserialize(ByteBuffer buffer);
}
