/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
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
     *
     * @return string maximum length
     */
    int getMaximumLength();
}
