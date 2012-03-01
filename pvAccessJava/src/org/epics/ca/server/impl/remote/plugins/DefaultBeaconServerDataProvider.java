/*
 * Copyright (c) 2009 by Cosylab
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

package org.epics.ca.server.impl.remote.plugins;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;

import org.epics.ca.PVFactory;
import org.epics.ca.server.impl.remote.ServerContextImpl;
import org.epics.ca.server.plugins.BeaconServerStatusProvider;
import org.epics.pvData.pv.Field;
import org.epics.pvData.pv.FieldCreate;
import org.epics.pvData.pv.PVDataCreate;
import org.epics.pvData.pv.PVField;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.ScalarType;

/**
 * Default <code>BeaconServerDataProvider</code> implementation.
 * @author Matej Sekoranja (matej.sekoranja@cosylab.com)
 * @version $Id$
 */
public class DefaultBeaconServerDataProvider implements
		BeaconServerStatusProvider {

	/**
	 * Monitored server context.
	 */
	protected ServerContextImpl context;

	/**
	 * Status structure.
	 */
	protected PVStructure status;
	
	/**
	 * Constructor.
	 * @param context server context to be monitored.
	 */
	public DefaultBeaconServerDataProvider(ServerContextImpl context) {
		this.context = context;
		
		initialize();
	}
	
	/**
	 * Initialize data stuctures.
	 */
	private void initialize()
	{
        FieldCreate fieldCreate = PVFactory.getFieldCreate();
        PVDataCreate pvDataCreate = PVFactory.getPVDataCreate();        
        Field[] fields = new Field[6];
        // TODO hierarchy can be used...
        fields[0] = fieldCreate.createScalar("connections",ScalarType.pvInt);
        fields[1] = fieldCreate.createScalar("allocatedMemory",ScalarType.pvLong);
        fields[2] = fieldCreate.createScalar("freeMemory",ScalarType.pvLong);
        fields[3] = fieldCreate.createScalar("threads",ScalarType.pvInt);
        fields[4] = fieldCreate.createScalar("deadlocks",ScalarType.pvInt);
        fields[5] = fieldCreate.createScalar("averageSystemLoad",ScalarType.pvDouble);
        
        status = pvDataCreate.createPVStructure(null,"status",fields);
	}

	/* (non-Javadoc)
	 * @see org.epics.ca.server.plugins.BeaconServerStatusProvider#getServerStatusData()
	 */
	public PVField getServerStatusData() {
		
		status.getIntField("connections").put(context.getTransportRegistry().numberOfActiveTransports());
		status.getLongField("allocatedMemory").put(Runtime.getRuntime().totalMemory());
		status.getLongField("freeMemory").put(Runtime.getRuntime().freeMemory());

		ThreadMXBean threadMBean = ManagementFactory.getThreadMXBean();
		status.getIntField("threads").put(threadMBean.getThreadCount());
		
		final long[] deadlocks = threadMBean.isSynchronizerUsageSupported() ?
        	threadMBean.findDeadlockedThreads() :
        	threadMBean.findMonitorDeadlockedThreads();
    	status.getIntField("deadlocks").put((deadlocks != null) ? deadlocks.length : 0);

        OperatingSystemMXBean osMBean = ManagementFactory.getOperatingSystemMXBean();
		status.getDoubleField("averageSystemLoad").put(osMBean.getSystemLoadAverage());

		return status;
	}

}
