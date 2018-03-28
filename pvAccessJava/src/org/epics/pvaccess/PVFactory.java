/*
 * Copyright (c) 2007 by Cosylab
 *
 * The full license specifying the redistribution, modification, usage and other
 * rights and obligations is included with the distribution of this project in
 * the file "LICENSE-CAJ". If the license is not included visit Cosylab web site,
 * <http://www.cosylab.com>.
 *
 * THIS SOFTWARE IS PROVIDED AS-IS WITHOUT WARRANTY OF ANY KIND, NOT EVEN THE
 * IMPLIED WARRANTY OF MERCHANTABILITY. THE AUTHOR OF THIS SOFTWARE, ASSUMES
 * _NO_ RESPONSIBILITY FOR ANY CONSEQUENCE RESULTING FROM THE USE, MODIFICATION,
 * OR REDISTRIBUTION OF THIS SOFTWARE.
 */

package org.epics.pvaccess;

import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.factory.StatusFactory;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.StatusCreate;

/**
 * Class to be used by pvAccess to get all the PVData related factory.
 * For not this implementation is pretty simple, but in the future
 * it should allow getting different implementations.
 * @author msekoranja
 *
 */
public class PVFactory {
	private final static FieldCreate fieldCreate = FieldFactory.getFieldCreate();
	private final static PVDataCreate dataCreate = PVDataFactory.getPVDataCreate();
	private final static StatusCreate statusCreate = StatusFactory.getStatusCreate();
	
	public static final FieldCreate getFieldCreate()
	{
		return fieldCreate;
	}
	
	public static final PVDataCreate getPVDataCreate()
	{
		return dataCreate;
	}

	public static final StatusCreate getStatusCreate()
	{
		return statusCreate;
	}
}
