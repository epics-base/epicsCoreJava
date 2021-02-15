/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient.datasource.pva;

import org.epics.pvdata.pv.PVField;

/**
 * @author msekoranja
 *
 */
class PVAPVField {

	protected final PVField pvField;

	public PVAPVField(PVField pvField, boolean disconnected)
	{
		this.pvField = pvField;
	}

	public PVField getPVField() {
		return pvField;
	}

	@Override
	public String toString() {
		return pvField.toString();
	}


}
