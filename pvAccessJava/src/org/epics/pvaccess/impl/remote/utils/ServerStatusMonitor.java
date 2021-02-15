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

package org.epics.pvaccess.impl.remote.utils;

import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.epics.pvaccess.PVAException;
import org.epics.pvaccess.client.impl.remote.BeaconHandler;
import org.epics.pvaccess.client.impl.remote.ClientContextImpl;
import org.epics.pvdata.pv.PVField;

/**
 * Simple server monitor GUI.
 * @author msekoranja
 * @version $Id$
 */
public class ServerStatusMonitor {

	/**
	 * Context implementation.
	 */
	private static class BeaconMonitorContextImpl extends ClientContextImpl
	{
		/**
		 * Beacon handler map.
		 */
		private final Map<String, BeaconHandler> beaconHandlerMap =
				Collections.synchronizedMap(new HashMap<String, BeaconHandler>());

		/**
		 * Constructor.
		 */
		public BeaconMonitorContextImpl() {
			super();
		}

		@Override
		public BeaconHandler getBeaconHandler(String protocol, InetSocketAddress responseFrom) {
			BeaconHandler bh = beaconHandlerMap.get(protocol);
			if (bh == null)
			{
				bh = new BeaconHandlerImpl(protocol);
				beaconHandlerMap.put(protocol, bh);
			}
			return bh;
		}

	}


	static class BeaconHandlerImpl implements BeaconHandler
	{
		/**
		 * ISO 8601 date formatter.
		 */
		private static SimpleDateFormat timeFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

		private final String protocol;

		public BeaconHandlerImpl(String protocol)
		{
			this.protocol = protocol;
		}

		public void beaconNotify(InetSocketAddress from, byte remoteTransportRevision,
								 long timestamp, byte[] guid, int sequentalID,
								 int changeCount, PVField data) {
			// sync timeFormatter and System.out
			synchronized(timeFormatter)
			{
				System.out.printf("[%s] %s@%s: seqID %d, version %d, guid %s, change %d\n",
						timeFormatter.format(new Date(timestamp)),
						protocol, from,
						sequentalID, remoteTransportRevision,
						GUID.toString(guid),
						changeCount);
				if (data != null)
					System.out.println(data);
			}
		}
	}


    /**
     * PVA context.
     */
    private BeaconMonitorContextImpl context = null;

    /**
     * Initialize JCA context.
     * @throws PVAException	throws on any failure.
     */
    private void initialize() throws PVAException {

		// Create a context with default configuration values.
		context = new BeaconMonitorContextImpl();
		context.initialize();

		// Display basic information about the context.
        System.out.println(context.getVersion().getVersionString());
        context.printInfo(); System.out.println();
    }

    /**
     * Destroy JCA context.
     */
    public void destroy() {

        try {

            // Destroy the context, check if never initialized.
            if (context != null)
                context.destroy();

        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

	/**
	 * Do the work...
	 */
	public void execute() {

		try {

		    // initialize context
		    initialize();

		} catch (Throwable th) {
			th.printStackTrace();
		}

	}


	/**
	 * Program entry point.
	 * @param args	command-line arguments
	 */
	public static void main(String[] args) {
		// execute
		new ServerStatusMonitor().execute();
	}

}
