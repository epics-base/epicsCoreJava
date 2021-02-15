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

import org.epics.pvaccess.PVAConstants;
import org.epics.pvaccess.util.InetAddressUtil;
import org.epics.pvaccess.util.configuration.Configuration;
import org.epics.pvaccess.util.configuration.ConfigurationProvider;
import org.epics.pvaccess.util.configuration.impl.ConfigurationFactory;
import org.epics.pvdata.misc.SerializeHelper;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;
import java.util.*;

/**
 * Gets a list of all servers.
 * @author msekoranja
 */
public class ServerList  {

	static
	{
		// force only IPv4 sockets, since EPICS does not work right with IPv6 sockets
		// see http://java.sun.com/j2se/1.5.0/docs/guide/net/properties.html
		System.setProperty("java.net.preferIPv4Stack", "true");
	}

	private static boolean send(DatagramChannel channel,
								InetSocketAddress[] sendAddresses, ByteBuffer buffer)
	{
		// noop check
		if (sendAddresses == null)
			return false;

		for (InetSocketAddress sendAddress : sendAddresses) {
			try {
				// prepare buffer
				buffer.flip();
				channel.send(buffer, sendAddress);
			} catch (NoRouteToHostException noRouteToHostException) {
				System.err.println("No route to host exception caught when sending to: " + sendAddress + ".");
			} catch (Throwable ex) {
				ex.printStackTrace();
				return false;
			}
		}

		return true;
	}

	private static final Map<GUID, ServerEntry> serverMap =
		new HashMap<GUID, ServerEntry>();

	private static class ServerEntry {
		GUID guid;
		String protocol;
		// Set removes duplicates
		final Set<InetSocketAddress> addresses = new HashSet<InetSocketAddress>();
		byte version;

		public ServerEntry(GUID guid, String protocol,
				InetSocketAddress address, byte version) {
			this.guid = guid;
			this.protocol = protocol;
			addresses.add(address);
			this.version = version;
		}

		public void addAddress(InetSocketAddress address)
		{
			addresses.add(address);
		}

		@Override
		public String toString() {
			StringBuffer b = new StringBuffer(200);
			b.append("GUID ").append(guid).append(", version ").append(version).append(": ");
			b.append(protocol).append('@');
			b.append(Arrays.toString(addresses.toArray()));
			return b.toString();
		}

	}

	private static void processSearchResponse(InetSocketAddress responseFrom, ByteBuffer socketBuffer) throws IOException
	{
		// magic code
		final byte magicCode = socketBuffer.get();

		// version
		byte version = socketBuffer.get();

		// flags
		byte flags = socketBuffer.get();
		if ((flags & 0x80) == 0x80)
			socketBuffer.order(ByteOrder.BIG_ENDIAN);
		else
			socketBuffer.order(ByteOrder.LITTLE_ENDIAN);

		// command
		byte command = socketBuffer.get();
		if (command != 0x04)
			return;

		// read payload size
		int payloadSize = socketBuffer.getInt();
		if (payloadSize < (12+4+16+2))
			return;

		// check magic code
		if (magicCode != PVAConstants.PVA_MAGIC)
			return;

		// 12-byte GUID
		byte[] guid = new byte[12];
		socketBuffer.get(guid);

		/*int searchSequenceId = */socketBuffer.getInt();

		// 128-bit IPv6 address
		byte[] byteAddress = new byte[16];
		socketBuffer.get(byteAddress);

		final int port = socketBuffer.getShort() & 0xFFFF;

		// NOTE: Java knows how to compare IPv4/IPv6 :)

		InetAddress addr;
		try {
			addr = InetAddress.getByAddress(byteAddress);
		} catch (UnknownHostException e) {
			return;
		}

		// accept given address if explicitly specified by sender
		if (!addr.isAnyLocalAddress())
			responseFrom = new InetSocketAddress(addr, port);
		else
			responseFrom = new InetSocketAddress(responseFrom.getAddress(), port);

		final String protocol = SerializeHelper.deserializeString(socketBuffer);

		/*boolean found = */socketBuffer.get(); // != 0;

		GUID g = new GUID(guid);
		ServerEntry se = serverMap.get(g);
		if (se != null)
			se.addAddress(responseFrom);
		else
			serverMap.put(g, new ServerEntry(g, protocol, responseFrom, version));
	}

	public static void main(String[] args) throws Throwable {

		final ConfigurationProvider configurationProvider = ConfigurationFactory.getProvider();
		Configuration config = configurationProvider.getConfiguration("pvAccess-client");
		if (config == null)
			config = configurationProvider.getConfiguration("system");

		String addressList = config.getPropertyAsString("EPICS_PVA_ADDR_LIST", "");
		boolean autoAddressList = config.getPropertyAsBoolean("EPICS_PVA_AUTO_ADDR_LIST", true);
		int broadcastPort = config.getPropertyAsInteger("EPICS_PVA_BROADCAST_PORT", PVAConstants.PVA_BROADCAST_PORT);

		// where to send address
		InetSocketAddress[] broadcastAddresses = InetAddressUtil.getBroadcastAddresses(broadcastPort);

		// set broadcast address list
		if (addressList != null && addressList.length() > 0)
		{
			// if auto is true, add it to specified list
			InetSocketAddress[] appendList = null;
			if (autoAddressList)
				appendList = broadcastAddresses;

			broadcastAddresses = InetAddressUtil.getSocketAddressList(addressList, broadcastPort, appendList);
		}

		System.out.println("Searching...");

		DatagramChannel datagramChannel = DatagramChannel.open();
		datagramChannel.configureBlocking(true);
		datagramChannel.socket().setBroadcast(true);
		datagramChannel.socket().setSoTimeout(1000);	// 1 sec
		datagramChannel.connect(new InetSocketAddress(0));


		ByteBuffer sendBuffer = ByteBuffer.allocate(1024);

		sendBuffer.put(PVAConstants.PVA_MAGIC);
		sendBuffer.put(PVAConstants.PVA_VERSION);
		sendBuffer.put((byte)0x80);	// big endian
		sendBuffer.put((byte)0x03);	// search
		sendBuffer.putInt(4+1+3+16+2+1+2);		// payload size

		sendBuffer.putInt(0);	    // sequenceId
		sendBuffer.put((byte)0x81); // reply required // TODO unicast vs multicast; for now we mark ourselves as unicast
		sendBuffer.put((byte)0);		// reserved
		sendBuffer.putShort((short)0);  // reserved

		// NOTE: is it possible (very likely) that address is any local address ::ffff:0.0.0.0
		InetSocketAddress address = new InetSocketAddress(datagramChannel.socket().getInetAddress(), datagramChannel.socket().getPort()) ;
		//(InetSocketAddress)datagramChannel.socket().getLocalAddress();
		InetAddressUtil.encodeAsIPv6Address(sendBuffer, address.getAddress());
		sendBuffer.putShort((short)address.getPort());

		sendBuffer.put((byte)0x00);	// no restriction on protocol
		sendBuffer.putShort((byte)0x00);	// count


		send(datagramChannel, broadcastAddresses, sendBuffer);

		ByteBuffer receiveBuffer = ByteBuffer.allocate(1024);

		DatagramPacket dp = new DatagramPacket(receiveBuffer.array(), receiveBuffer.capacity());
		while (true)
		{
//			SocketAddress responseFrom = datagramChannel.receive(receiveBuffer);
//			if (responseFrom == null)
//				break;

			SocketAddress responseFrom;
			try
			{
				datagramChannel.socket().receive(dp);
				responseFrom = dp.getSocketAddress();
				receiveBuffer.position(dp.getLength());
			}
			catch (SocketTimeoutException ste) {
				break;
			}

			receiveBuffer.flip();
			processSearchResponse((InetSocketAddress)responseFrom, receiveBuffer);
		}

		for (ServerEntry se : serverMap.values())
			System.out.println(se);

		System.out.println("done.");

	}

}
