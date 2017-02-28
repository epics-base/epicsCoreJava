/*
 * The License for this software can be found in the file LICENSE that is included with the distribution.
 */
package org.epics.pvdata.copy;

import org.epics.pvdata.pv.PVField;
/**
 * A filter plugin that attaches to a field of a PVStrcture.
 * PVCopy looks for plugins defined in pvRequest and calls the filter when a pvCopy is updated.
 * @author mrk
 * @since 2017.02.23
 * 
 * Interface for a filter plugin for PVCopy.
 *
 */

public interface PVPlugin {
	/**
	 * Create a PVFilter.
	 * @param requestValue The value part of a name=value request option.
	 * @param pvCopy The PVCopy to which the PVFilter will be attached.
	 * @param master The field in the master PVStructure to which the PVFilter will be attached.
	 * @return The PVFilter. A null is returned if master of requestValue is not appropriate for the plugin.
	 */
	public PVFilter create(String requestValue,PVCopy pvCopy,PVField master);
}
