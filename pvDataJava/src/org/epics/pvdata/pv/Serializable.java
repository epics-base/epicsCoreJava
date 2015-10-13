/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata.pv;

import java.nio.ByteBuffer;



/**
 * Base interface for serialization.
 * @author mse
 *
 */
public interface Serializable {
    
    /**
     * Serialize field into given buffer.
     * 
     * @param buffer the serialization buffer
     * @param flusher the flush interface
     */
    void serialize(ByteBuffer buffer, SerializableControl flusher);

    /**
     * Deserialize buffer.
     * 
     * @param buffer the serialization buffer
     * @param control the deserialization control
     */
     void deserialize(ByteBuffer buffer, DeserializableControl control);
}
