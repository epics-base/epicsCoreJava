package org.epics.pvData.misc;


import org.epics.pvData.pv.PVInt;
import org.epics.pvData.pv.PVString;
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
     * Get the choice field of an enumerated structure.
     * @return The interface.
     */
    PVString getChoice();
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
