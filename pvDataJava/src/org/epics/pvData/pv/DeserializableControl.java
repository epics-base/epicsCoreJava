/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.pv;


/**
 * Deserialization control interface.
 * @author mse
 */
public interface DeserializableControl {

	/**
	 * Helper method. Ensures specified size of bytes, provides it if necessary.
	 * @param size
	 */
	void ensureBuffer(int size);
}
