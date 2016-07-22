/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
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
