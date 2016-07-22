/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.pv;

/**
 * PVScalar extends PVField for a scalar field.
 * @author mrk
 *
 */
public interface PVScalar extends PVField{
    /**
     * Get the Scalar reflection interface.
     *
     * @return the Scalar interface
     */
    Scalar getScalar();
}
