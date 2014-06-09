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

package org.epics.pvaccess.client.impl.remote.handlers;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.logging.Level;

import org.epics.pvaccess.PVFactory;
import org.epics.pvaccess.client.impl.remote.ClientContextImpl;
import org.epics.pvaccess.impl.remote.Transport;
import org.epics.pvdata.misc.SerializeHelper;
import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVField;


/**
 * Beacon message handler.
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public class BeaconHandler extends AbstractClientResponseHandler {

	/**
	 * PVField factory.
	 */
	private static final PVDataCreate pvDataCreate = PVFactory.getPVDataCreate();

	/**
	 * Field factory.
	 */
	private static final FieldCreate fieldCreate = PVFactory.getFieldCreate();

	/**
	 * @param context
	 */
	public BeaconHandler(ClientContextImpl context) {
		super(context, "Beacon");
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.impl.remote.AbstractResponseHandler#handleResponse(java.net.InetSocketAddress, org.epics.pvaccess.core.Transport, byte, byte, int, java.nio.ByteBuffer)
	 */
	@Override
	public void handleResponse(InetSocketAddress responseFrom, Transport transport, byte version, byte command, int payloadSize, ByteBuffer payloadBuffer) {

		// reception timestamp
		final long timestamp = System.currentTimeMillis();

		super.handleResponse(responseFrom, transport, version, command, payloadSize, payloadBuffer);
		
		transport.ensureData(12+2+2+16+2);
		
		final byte[] guid = new byte[12];
		payloadBuffer.get(guid);
		
		final int sequentalID = payloadBuffer.getShort() & 0x0000FFFF;
		final int changeCount = payloadBuffer.getShort() & 0x0000FFFF;
		
		// 128-bit IPv6 address
		byte[] byteAddress = new byte[16]; 
		payloadBuffer.get(byteAddress);
	
		final int port = payloadBuffer.getShort() & 0xFFFF;
		
		// NOTE: Java knows how to compare IPv4/IPv6 :)
		
		InetAddress addr;
		try {
			addr = InetAddress.getByAddress(byteAddress);
		} catch (UnknownHostException e) {
			context.getLogger().log(Level.FINER, "Invalid address '" +  new String(byteAddress) + "' in beacon received from: " + responseFrom, e);
			return;
		}

		// accept given address if explicitly specified by sender
		if (!addr.isAnyLocalAddress())
			responseFrom = new InetSocketAddress(addr, port);
		else
			responseFrom = new InetSocketAddress(responseFrom.getAddress(), port);
		
		final String protocol = SerializeHelper.deserializeString(payloadBuffer, transport);

		// extra data
		PVField data = null;
		final Field field = fieldCreate.deserialize(payloadBuffer, transport);
		if (field != null)
		{
			data = pvDataCreate.createPVField(field);
			data.deserialize(payloadBuffer, transport);
		}
		
		org.epics.pvaccess.client.impl.remote.BeaconHandler beaconHandler = context.getBeaconHandler(protocol, responseFrom);
		// currently we care only for servers used by this context  
		if (beaconHandler == null)
			return;

		// notify beacon handler
		beaconHandler.beaconNotify(responseFrom, version, timestamp, guid, sequentalID, changeCount, data);
	}

}
