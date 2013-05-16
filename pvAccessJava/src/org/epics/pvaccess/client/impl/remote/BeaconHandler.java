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

package org.epics.pvaccess.client.impl.remote;

import java.net.InetSocketAddress;

import org.epics.pvdata.property.TimeStamp;
import org.epics.pvdata.pv.PVField;

/**
 * PVA beacon handler interface.
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public interface BeaconHandler  {
	
	/**
	 * Beacon arrival notification.
	 * @param from who is notifying.
	 * @param remoteTransportRevision encoded (major, minor) revision.
	 * @param timestamp time when beacon was received.
	 * @param startupTime server (reported) startup time.
	 * @param sequentalID sequential ID (unsigned short).
	 * @param data server status data, can be <code>null</code>.
	 */
	public void beaconNotify(InetSocketAddress from, byte remoteTransportRevision,
							 long timestamp, TimeStamp startupTime, int sequentalID,
							 PVField data);

}
