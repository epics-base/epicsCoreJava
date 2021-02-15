/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.vtype;

/**
 * A value that provides time information.
 *
 * @author carcassi
 */
public interface TimeProvider {

    /**
     * The time associated with this value.
     *
     * @return the time; not null
     */
    Time getTime();
}
