/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata.pv;

/**
 * Reflection interface for a scalar field.
 * @author mrk
 *
 */
public interface BoundedString extends Scalar {

	/**
	 * Get string maximum length.
	 * @return string maximum length.
	 */
	int getMaximumLength();
}
