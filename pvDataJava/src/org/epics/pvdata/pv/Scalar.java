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
public interface Scalar extends Field {
    /**
     * Get the ScalarType.
     *
     * @return the ScalarType (non-<code>null</code>)
     */
    ScalarType getScalarType();
}
