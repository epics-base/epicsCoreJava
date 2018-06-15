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

package org.epics.pvaccess.impl.remote;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.epics.pvaccess.util.IntHashMap;


/**
 * Class to cache PVA transports (connections to other hosts).
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public final class TransportRegistry {

	/**
	 * Map caching transports.
	 */
	private Map<InetSocketAddress, IntHashMap> transports;
	
	/**
	 * Array of all transports.
	 */
	private ArrayList<Transport> allTransports;

	/**
	 * Constructor.
	 */
	public TransportRegistry() {
		transports = new HashMap<InetSocketAddress, IntHashMap>();
		allTransports = new ArrayList<Transport>();
	}

	/**
	 * Save/cache new transport into the registry.
	 * @param transport transport to be registered.
	 */
	public void put(Transport transport)
	{
		// TODO support type
		//final String type = transport.getType();
		final short priority = transport.getPriority();
		final InetSocketAddress address = transport.getRemoteAddress();
		synchronized (transports) {
			IntHashMap priorities = (IntHashMap)transports.get(address);
			if (priorities == null) {
				priorities = new IntHashMap();
				transports.put(address, priorities);
			}
			priorities.put(priority, transport);
			allTransports.add(transport);
		}
	}

	/**
	 * Lookup for a transport for given address.
	 * @param type 	protocol type.
	 * @param address	address of the host computer.
	 * @param priority  priority of the transport.
	 * @return corresponding transport, <code>null</code> if none found.
	 */
	public Transport get(String type, InetSocketAddress address, short priority)
	{
		// TODO support type
		synchronized (transports) {
			IntHashMap priorities = transports.get(address);
			if (priorities != null)
				return (Transport)priorities.get(priority);
			else
				return null;
		}
	}

	/**
	 * Lookup for a transport for given address (all priorities).
	 * @param type	protocol type (e.g. tcp, udp, ssl, etc.).
	 * @param address	address of the host computer.
	 * @return array of corresponding transports, <code>null</code> if none found.
	 */
	public Transport[] get(String type, InetSocketAddress address)
	{
		// TODO support type
		synchronized (transports) {
			IntHashMap priorities = transports.get(address);
			if (priorities != null)
			{
				// TODO optimize
				Transport[] ts = new Transport[priorities.size()];
				priorities.toArray(ts);
				return ts;
			}
			else
				return null;
		}
	}

	/**
	 * Remove transport from the registry.
	 * @param transport transport to remove.
	 * @return removed transport, <code>null</code> if none found.
	 */
	public Transport remove(Transport transport)
	{
		// TODO support type
		//final String type = transport.getType();
		final short priority = transport.getPriority();
		final InetSocketAddress address = transport.getRemoteAddress();
		synchronized (transports) {
			IntHashMap priorities = transports.get(address);
			if (priorities != null) {
				transport = (Transport)priorities.remove(priority);
				if (priorities.size() == 0)
					transports.remove(address);
				if (transport != null)
					allTransports.remove(transport);
				return transport;
			}
			else
				return null;
		}
	}

	/**
	 * Clear cache.
	 */
	public void clear()
	{
		synchronized (transports) {
			transports.clear();
			allTransports.clear();
		}
	}
	
	/**
	 * Get number of active (cached) transports.
	 * @return number of active (cached) transports.
	 */
	public int numberOfActiveTransports()
	{
		synchronized (transports) {
			return allTransports.size();
		}
	}

	/**
	 * Get array of all active (cached) transports.
	 * @param type	protocol type (e.g. tcp, udp, ssl, etc.).
	 * @return array of all active (cached) transports.
	 */
	public Transport[] toArray(String type)
	{
		// TODO support type
		synchronized (transports) {
			return (Transport[]) allTransports.toArray(new Transport[transports.size()]);
		}
	}

	/**
	 * Get array of all active (cached) transports.
	 * @return array of all active (cached) transports.
	 */
	public Transport[] toArray()
	{
		synchronized (transports) {
			return (Transport[]) allTransports.toArray(new Transport[transports.size()]);
		}
	}
}
