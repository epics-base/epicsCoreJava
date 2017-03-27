/*
 * The License for this software can be found in the file LICENSE that is included with the distribution.
 */
package org.epics.pvdata.copy;

import org.epics.pvdata.misc.BitSet;
import org.epics.pvdata.pv.PVField;

/**
 * A Filter that is called when a copy PVStructure is being updated.
 * @author mrk
 * @since 2017.02.23
 *
 * This interface define a filter to update a copy of a field from a master PVStructure.
 * of the data in the master.
 */
public interface PVFilter {
    /**
     * Update copy or master.
     * @param copy The data for copy.
     * @param bitSet The BitSet for copy.
     * @param toCopy (true,false) means copy (from master to copy,from copy to master)
     * @return (true,false) if filter (modified, did not modify) destination.
     */
    boolean filter(PVField copy,BitSet bitSet,boolean toCopy);
    /**
     * Get the filter name.
     * This is the name part of a request name=value pair.
     * @return The name.
     */
    String getName();
}
