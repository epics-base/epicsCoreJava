/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.pv;

/**
 * PVStructure interface.
 * @author mrk
 *
 */
public interface PVStructureScalar extends PVScalar {
	/**
	 * Get the introspection interface.
	 * @return The interface.
	 */
	StructureScalar getStructureScalar();
	/**
	 * Get the top level PVStructure for this field.
	 * @return The interface.
	 */
	PVStructure getPVStructure();
	/**
	 * Must be called whenever the pvStructure is modified. 
	 */
	void put();
}
