/*
 * Copyright (c) 2004 by Cosylab
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

package org.epics.ca.client.impl.remote;

import java.net.InetSocketAddress;

import org.epics.ca.impl.remote.ProtocolType;
import org.epics.ca.impl.remote.Transport;
import org.epics.pvData.property.TimeStamp;
import org.epics.pvData.pv.PVField;


/**
 * CA beacon handler.
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public class BeaconHandlerImpl implements BeaconHandler {
	
	/**
	 * Context instance.
	 */
	private final ClientContextImpl context;

	/**
	 * Remote address.
	 */
	private final InetSocketAddress responseFrom;

	/**
	 * Last beacon sequence ID.
	 *
	private int lastBeaconSequenceID;
	
	/**
	 * Last beacon timestamp.
	 *
	private long lastBeaconTimeStamp = Long.MIN_VALUE;

	/**
	 * Server startup timestamp.
	 */
	private TimeStamp serverStartupTime = null;

	/**
	 * Constructor.
	 * @param context context ot handle.
	 * @param responseFrom server to handle.
	 */
	public BeaconHandlerImpl(ClientContextImpl context, InetSocketAddress responseFrom)
	{
		this.context = context;
		this.responseFrom = responseFrom;
	}
	
	/**
	 * Update beacon period and do analitical checks (server restared, routing problems, etc.)
	 * @param from who is notifying.
	 * @param remoteTransportRevision encoded (major, minor) revision.
	 * @param timestamp timewhen beacon was received.
	 * @param startupTime server (reported) startup time.
	 * @param sequentalID sequential ID (unsigned short).
	 * @param data server status data, can be <code>null</code>.
	 */
	public void beaconNotify(InetSocketAddress from, byte remoteTransportRevision,
							 long timestamp, TimeStamp startupTime, int sequentalID,
							 PVField data)
	{
		boolean networkChanged = updateBeacon(remoteTransportRevision, timestamp, startupTime, sequentalID);
		if (networkChanged)
			changedTransport();
	}

	/**
	 * Update beacon.
	 * @param remoteTransportRevision
	 * @param timestamp
	 * @param sequentalID
	 * @return	network change (server restarted) detected.
	 */
	private synchronized boolean updateBeacon(byte remoteTransportRevision, long timestamp,
											  TimeStamp startupTime, int sequentalID) {
		
		// first beacon notification check
		if (serverStartupTime == null)
		{
			serverStartupTime = startupTime;
			
			// new server up...
			context.newServerDetected();

			return false;
		}
		
		final boolean networkChange = !serverStartupTime.equals(startupTime);
		if (networkChange)
			context.newServerDetected();
	
		return networkChange;
	}

	/**
	 * Changed transport (server restarted) notify. 
	 */
	private void changedTransport()
	{
		Transport[] transports = context.getTransportRegistry().get(ProtocolType.TCP.name(), responseFrom);
		if (transports == null)
			return;

		// notify all
		for (int i = 0; i < transports.length; i++)
			transports[i].changedTransport();
	}
}
