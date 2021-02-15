/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient.datasource.pva;

import org.epics.pvdata.pv.PVStructure;

/**
 * @author msekoranja
 *
 */
class PVAPVStructure extends PVAPVField {

	public PVAPVStructure(PVStructure pvStructure, boolean disconnected)
	{
		super(pvStructure, disconnected);
	}

	public PVStructure getPVStructure() {
		return (PVStructure)pvField;
	}

}
