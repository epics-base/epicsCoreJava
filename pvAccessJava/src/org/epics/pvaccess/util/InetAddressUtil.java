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

package org.epics.pvaccess.util;

import org.epics.util.compat.legacy.net.InterfaceAddress;
import org.epics.util.compat.legacy.net.NetworkInterface;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * <code>InetAddress</code> utility methods.
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public class InetAddressUtil {

	private static final String HOSTNAME_KEY = "HOSTNAME";
	private static final String STRIP_HOSTNAME_KEY = "STRIP_HOSTNAME";

	private static String hostName = null;

	public static synchronized String getHostName()
	{
		if (hostName == null)
			hostName = internalGetHostName();
		return hostName;
	}

	private static String internalGetHostName()
	{
		// default fallback
		String hostName = "localhost";

		try {
			InetAddress localAddress = InetAddress.getLocalHost();
			hostName = localAddress.getHostName();
		} catch (Throwable uhe) {	// not only UnknownHostException
			// try with environment variable
			try {
				String envHN = System.getenv(HOSTNAME_KEY);
				if (envHN != null)
					hostName = envHN;
			} catch (Throwable th) {
				// in case not supported by JVM/OS
			}

			// and system property (overrides env. var.)
			hostName = System.getProperty(HOSTNAME_KEY, hostName);
		}

		if (System.getProperties().contains(STRIP_HOSTNAME_KEY))
		{
			int dotPos = hostName.indexOf('.');
			if (dotPos > 0)
				hostName = hostName.substring(0, dotPos);
		}

		return hostName;
	}

	/**
	 * Get broadcast addresses.
	 * @param port port to be added to get socket address.
	 * @return array of broadcast addresses with given port.
	 */
	public static InetSocketAddress[] getBroadcastAddresses(int port) {
		Enumeration<NetworkInterface> nets;
		try {
			nets = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException se) {
			// fallback
			return new InetSocketAddress[] { new InetSocketAddress("255.255.255.255", port) };
		}

		ArrayList<InetSocketAddress> list = new ArrayList<InetSocketAddress>(10);

		while (nets.hasMoreElements())
		{
			NetworkInterface net = nets.nextElement();
			try
			{
				if (net.isUp())
				{
					List<InterfaceAddress> interfaceAddresses = net.getInterfaceAddresses();
					if (interfaceAddresses != null)
						for (InterfaceAddress addr : interfaceAddresses)
							if (addr.getBroadcast() != null)
							{
								InetSocketAddress isa = new InetSocketAddress(addr.getBroadcast(), port);
								if (!list.contains(isa))
									list.add(isa);
							}
				}
			} catch (Throwable th) {
				// some methods throw exceptions, some return null (and they shouldn't)
				// noop, skip that interface
			}
		}

		InetSocketAddress[] retVal = new InetSocketAddress[list.size()];
		list.toArray(retVal);
		return retVal;
	}

	/**
	 * Get a set of broadcast addresses.
	 * @return set of broadcast addresses.
	 */
	public static Set<InetAddress> getBroadcastAddresses() {

		Set<InetAddress> set = new HashSet<InetAddress>(10);

		Enumeration<NetworkInterface> nets;
		try {
			nets = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException se) {
			// fallback
			try {
				set.add(InetAddress.getByAddress(new byte[] { (byte)255, (byte)255, (byte)255, (byte)255 }));
			} catch (UnknownHostException e) {
				// noop
			}
			return set;
		}

		while (nets.hasMoreElements())
		{
			NetworkInterface net = nets.nextElement();
			try
			{
				if (net.isUp())
				{
					List<InterfaceAddress> interfaceAddresses = net.getInterfaceAddresses();
					if (interfaceAddresses != null)
						for (InterfaceAddress addr : interfaceAddresses)
						{
							InetAddress ba = addr.getBroadcast();
							if (ba != null)		// Set class takes care of duplicates
								set.add(ba);
						}
				}
			} catch (Throwable th) {
				// some methods throw exceptions, some return null (and they shouldn't)
				// noop, skip that interface
			}
		}

		return set;
	}

	/**
	 * Get a loopback NIF.
	 * @return a loopback NIF, <code>null</code> if not found.
	 */
	// TODO support case with multiple loopback NIFs
	public static NetworkInterface getLoopbackNIF() {

		Enumeration<NetworkInterface> nets;
		try {
			nets = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException se) {
			return null;
		}

		while (nets.hasMoreElements())
		{
			NetworkInterface net = nets.nextElement();
			try
			{
				if (net.isUp() && net.isLoopback())
					return net;
			} catch (Throwable th) {
				// some methods throw exceptions, some return null (and they shouldn't)
				// noop, skip that interface
			}
		}

		return null;
	}

	/**
	 * Encode address as IPv6 address.
	 * @param buffer byte-buffer where to put encoded data.
	 * @param address address to encode.
	 * @throws RuntimeException thrown if address is unsupported.
	 */
	public static void encodeAsIPv6Address(ByteBuffer buffer, InetAddress address) throws RuntimeException {
		if (address instanceof Inet6Address)
			buffer.put(address.getAddress());	// always network byte order
		else if (address instanceof Inet4Address)
		{
			// IPv4 compatible IPv6 address
			// first 80-bit are 0
			buffer.putLong(0);
			buffer.putShort((short)0);
			// next 16-bits are 1
			buffer.putShort((short)0xFFFF);
			// following IPv4 address
			buffer.put(address.getAddress());	// always network byte order
		}
		else
			throw new RuntimeException("unsupported network addresses: " + address);
	}

	/**
	 * Convert an integer into an IPv4 INET address.
	 * @param addr integer representation of a given address.
	 * @return IPv4 INET address.
	 */
	public static InetAddress intToIPv4Address(int addr) {
		byte[] a = new byte[4];

		a[0] = (byte) ((addr >> 24) & 0xFF);
		a[1] = (byte) ((addr >> 16) & 0xFF);
		a[2] = (byte) ((addr >>  8) & 0xFF);
		a[3] = (byte) ((addr & 0xFF));

		InetAddress res = null;
		try {
			res = InetAddress.getByAddress(a);
		} catch (UnknownHostException e) { /* noop */ }

		return res;
	}

	/**
	 * Convert an IPv4 INET address to an integer.
	 * @param addr	IPv4 INET address.
	 * @return integer representation of a given address.
	 * @throws IllegalArgumentException if the address is really an IPv6 address
	 */
	public static int ipv4AddressToInt(InetAddress addr) {

		if (addr instanceof Inet6Address)
			throw new IllegalArgumentException("IPv6 address used in IPv4 context");

		byte[] a = addr.getAddress();

		return ((a[0] & 0xFF) << 24)
		     | ((a[1] & 0xFF) << 16)
		   	 | ((a[2] & 0xFF) << 8)
		  	 |  (a[3] & 0xFF);
	}


	/**
	 * Parse space delimited addresses[:port] string and return array of <code>InetSocketAddress</code>.
	 * @param list	space delimited addresses[:port] string.
	 * @param defaultPort	port take if not specified.
	 * @return	array of <code>InetSocketAddress</code>.
	 */
	public static InetSocketAddress[] getSocketAddressList(String list, int defaultPort) {
		return getSocketAddressList(list, defaultPort, null);
	}

	/**
	 * Parse space delimited addresses[:port] string and return array of <code>InetSocketAddress</code>.
	 * @param list	space delimited addresses[:port] string.
	 * @param defaultPort	port take if not specified.
	 * @param appendList 	list to be appended.
	 * @return	array of <code>InetSocketAddress</code>.
	 */
	public static InetSocketAddress[] getSocketAddressList(String list, int defaultPort, InetSocketAddress[] appendList)
	{
		ArrayList<InetSocketAddress> al = new ArrayList<InetSocketAddress>();

		// parse string
		StringTokenizer st = new StringTokenizer(list);
		while (st.hasMoreTokens())
		{
			int port = defaultPort;
			String address = st.nextToken();

			// check port
			int pos = address.indexOf(':');
			if (pos >= 0)
			{
				try {
					port = Integer.parseInt(address.substring(pos + 1));
				}
				catch (NumberFormatException nfe) { /* noop */ }

				address = address.substring(0, pos);
			}

			try
			{
				InetSocketAddress isa = new InetSocketAddress(address, port);

				// add parsed address if resolved
				if (!isa.isUnresolved())
					al.add(isa);
			}
			catch (Throwable th) {
				// TODO
				th.printStackTrace();
			}
		}

		// copy to array
		int appendSize = (appendList == null) ? 0 : appendList.length;
		InetSocketAddress[] isar = new InetSocketAddress[al.size() + appendSize];
		al.toArray(isar);
		if (appendSize > 0)
			System.arraycopy(appendList, 0, isar, al.size(), appendSize);
		return isar;
	}

}
