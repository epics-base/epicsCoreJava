/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.pv;


/**
 * Flush control interface.
 * @author mse
 */
public interface SerializableControl {

	/**
	 * Request to flush serialization buffer.
	 * This call will block until buffer is not flushed.
	 * To be called when buffer is out of space.
	 */
	void flushSerializeBuffer();
	
	
	/**
	 * Helper method. Ensures specified size of bytes, flushes if necessary.
	 * @param size
	 */
	void ensureBuffer(int size);
}
