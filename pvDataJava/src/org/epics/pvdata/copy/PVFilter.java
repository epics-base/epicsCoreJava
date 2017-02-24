/*
 * The License for this software can be found in the file LICENSE that is included with the distribution.
 */
package org.epics.pvdata.copy;

import org.epics.pvdata.misc.BitSet;
import org.epics.pvdata.pv.*;

/**
 * A Filter that is called when a copy PVStructure is being updated.
 * @author mrk
 * @date 2017.02.23
 *
 * This interface define a filter to update a copy of a field from a master PVStructure.
 * of the data in the master.
 */
public interface PVFilter {
	/**
	 * Update copy from master.
	 * @param master The data from master.
	 * @param copy The data for copy.
	 * @param bitSet The BitSet for copy.
	 * @return (true,false) if filter modified copy.
	 */
	boolean filter(PVField copy,BitSet bitSet);
	/**
	 * Get the filter name.
	 * This is the name part of a request name=value pair.
	 * @return The name.
	 */
	String getName();
}
