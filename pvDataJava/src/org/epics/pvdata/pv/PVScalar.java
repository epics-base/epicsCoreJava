/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
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
