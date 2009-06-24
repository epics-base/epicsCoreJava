/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.misc;


import org.epics.pvData.pv.PVInt;
import org.epics.pvData.pv.PVStringArray;
import org.epics.pvData.pv.PVStructure;

/**
 * Interface for enumerated data.
 * @author mrk
 *
 */
public interface Enumerated {
    /**
     * Get the index field of an enumerated structure.
     * @return The interface.
     */
    PVInt getIndex();
    /**
     * Get the choice of an enumerated structure.
     * @return The string value of the choice.
     */
    String getChoice();
    /**
     * * Get the choices field of an enumerated structure.
     * @return The interface.
     */
    PVStringArray getChoices();
    /**
     * Get the PVField interface.
     * @return The interface.
     */
    PVStructure getPV();
}
