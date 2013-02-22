package org.epics.pvaccess.impl.remote.utils;

import java.io.PrintStream;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * Simple console untility class that shows network intefaces info.
 * @author msekoranja
 * @version $Id$
 */
public class NetworkInfo {
	private static final PrintStream console = System.out;

	public static void main(String args[]) throws SocketException {
		Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
		for (NetworkInterface netint : Collections.list(nets)) {
			displayInterfaceInformation(netint);
		}
	}

	private static void displayInterfaceInformation(NetworkInterface netint)
			throws SocketException {
		console.printf("Display name: %s%n", netint.getDisplayName());
		console.printf("Name: %s%n", netint.getName());
		Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
		for (InetAddress inetAddress : Collections.list(inetAddresses)) {
			console.printf("InetAddress: %s%n", inetAddress);
		}

		console.printf("Parent: %s%n", netint.getParent());
		try
		{
			console.printf("Up? %s%n", netint.isUp());
			console.printf("Loopback? %s%n", netint.isLoopback());
			console.printf("PointToPoint? %s%n", netint.isPointToPoint());
			console.printf("Supports multicast? %s%n", netint.isVirtual());
			console.printf("Virtual? %s%n", netint.isVirtual());
			final byte[] hwAddr = netint.getHardwareAddress();
			if (hwAddr != null)
			{
				StringBuffer strHWAddr = new StringBuffer();
				for (int i = 0; i < hwAddr.length; i++)
				{
					int val = hwAddr[i];
					if (val < 0) val += 256;	// fix signess
					strHWAddr.append(Integer.toHexString(val));
					if (i < hwAddr.length - 1) strHWAddr.append(':');
				}
				console.printf("Hardware address: %s%n", strHWAddr.toString());
			}
			console.printf("MTU: %s%n", netint.getMTU());
	
			List<InterfaceAddress> interfaceAddresses = netint.getInterfaceAddresses();
			for (InterfaceAddress addr : interfaceAddresses) {
				console.printf("InterfaceAddress: %s BroadcastAddress: %s%n", addr.getAddress(), addr.getBroadcast());
			}
			
			console.printf("%n");
			Enumeration<NetworkInterface> subInterfaces = netint.getSubInterfaces();
			for (NetworkInterface networkInterface : Collections.list(subInterfaces)) {
				console.printf("%nSubInterface%n");
				displayInterfaceInformation(networkInterface);
			}
		} 
		catch (Throwable th)
		{
			// simply dump exception
			th.printStackTrace();
		}
		console.printf("%n");
	}
}