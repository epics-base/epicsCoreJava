/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.pv;

import java.nio.ByteBuffer;

import org.epics.pvdata.misc.BitSet;



/**
 * Base interface for partital serializaion.
 * @author mse
 *
 */
public interface BitSetSerializable {
    
    /**
     * Serialize field into the given buffer.
     *
     * @param buffer serialization buffer
     * @param flusher flush interface
     * @param bitSet the BitSet which shows the fields to serialize
     */
    void serialize(ByteBuffer buffer, SerializableControl flusher, BitSet bitSet);

    /**
     * Deserialize buffer.
     *
     * @param buffer serialization buffer
     * @param control deserialization control instance
     * @param bitSet the BitSet which shows the fields to deserialize
     */
     void deserialize(ByteBuffer buffer, DeserializableControl control, BitSet bitSet);
}
