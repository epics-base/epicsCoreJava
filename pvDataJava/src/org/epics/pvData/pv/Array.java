/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.pv;

/**
 * Base interface for array field reflection.
 * @author mrk
 *
 */
public interface Array extends Field{
    /**
     * Get the element type for the array.
     * @return The element ScalarType.
     */
    ScalarType getElementType();
}
