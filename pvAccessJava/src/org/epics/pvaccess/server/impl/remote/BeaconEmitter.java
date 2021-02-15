/*
 * Copyright (c) 2006 by Cosylab
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

package org.epics.pvaccess.server.impl.remote;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.epics.pvaccess.impl.remote.SerializationHelper;
import org.epics.pvaccess.impl.remote.Transport;
import org.epics.pvaccess.impl.remote.TransportSendControl;
import org.epics.pvaccess.impl.remote.TransportSender;
import org.epics.pvaccess.server.plugins.BeaconServerStatusProvider;
import org.epics.pvaccess.util.InetAddressUtil;
import org.epics.pvdata.misc.SerializeHelper;
import org.epics.pvdata.misc.Timer;
import org.epics.pvdata.misc.Timer.TimerCallback;
import org.epics.pvdata.misc.Timer.TimerNode;
import org.epics.pvdata.misc.TimerFactory;
import org.epics.pvdata.pv.PVField;


/**
 * Beacon emitter.
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public class BeaconEmitter implements TimerCallback, TransportSender {

	/**
	 * Minimal (initial) PVA beacon period (in seconds).
	 */
	protected static final float EPICS_PVA_MIN_BEACON_PERIOD = 1.0f;

	/**
	 * Minimal PVA beacon count limit.
	 */
	protected static final float EPICS_PVA_MIN_BEACON_COUNT_LIMIT = 3;

	/**
	 * Timer.
	 */
	protected final Timer timer;

	/**
	 * Logger.
	 */
	protected final Logger logger;

	/**
	 * Protocol.
	 */
	protected final String protocol;

	/**
	 * Transport.
	 */
	protected final Transport transport;

	/**
	 * Beacon sequence ID.
	 */
	protected byte beaconSequenceID = 0;

	/**
	 * Fast (at startup) beacon period (in sec).
	 */
	protected final double fastBeaconPeriod;

	/**
	 * Slow (after beaconCountLimit is reached) beacon period (in sec).
	 */
	protected final double slowBeaconPeriod;

	/**
	 * Limit on number of beacons issued.
	 */
	protected final short beaconCountLimit;

	/**
	 * Server GUID.
	 */
	protected final byte[] guid;

	/**
	 * Server address.
	 */
	protected final InetAddress serverAddress;

	/**
	 * Server port.
	 */
	protected final int serverPort;

	/**
	 * Server status provider implementation (optional).
	 */
	private final BeaconServerStatusProvider serverStatusProvider;

	/**
	 * Timer task node.
	 */
	private final TimerNode timerNode;


	/**
	 * Constructor.
	 * @param protocol what protocol to "beacon".
	 * @param transport	transport to be used to send beacons.
	 * @param context PVA context.
	 */
	public BeaconEmitter(String protocol, Transport transport, ServerContextImpl context)
	{
		this.protocol = protocol;
		this.transport = transport;
		this.timer = context.getTimer();
		this.logger = context.getLogger();
		this.guid = context.getGUID();
		this.serverAddress = context.getServerInetAddress();
		this.serverPort = context.getServerPort();
		this.serverStatusProvider = context.getBeaconServerStatusProvider();
		this.fastBeaconPeriod = Math.max(context.getBeaconPeriod(), EPICS_PVA_MIN_BEACON_PERIOD);
		this.slowBeaconPeriod = Math.max(180.0, fastBeaconPeriod);	// TODO configurable
		this.beaconCountLimit = (short)Math.max(10, EPICS_PVA_MIN_BEACON_COUNT_LIMIT);	// TODO configurable
		this.timerNode = TimerFactory.createNode(this);

	}

	/**
	 * Start emitting.
	 */
	protected void start()
	{
		timer.scheduleAfterDelay(timerNode, 0);
	}

	/**
	 * Reschedule timer.
	 */
	protected void reschedule()
	{
		final double period = (beaconSequenceID >= beaconCountLimit) ? slowBeaconPeriod : fastBeaconPeriod;
		if (period > 0)
			timer.scheduleAfterDelay(timerNode, period);
	}

	/* (non-Javadoc)
	 * @see org.epics.pvdata.misc.Timer.TimerCallback#callback()
	 */
	public void callback() {
		transport.enqueueSendRequest(this);
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.impl.remote.TransportSender#lock()
	 */
	public void lock() {
		// noop
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.impl.remote.TransportSender#send(java.nio.ByteBuffer, org.epics.pvaccess.impl.remote.TransportSendControl)
	 */
	public void send(ByteBuffer buffer, TransportSendControl control) {
		// get server status
		PVField serverStatus = null;
		if (serverStatusProvider != null)
		{
			try
			{
				serverStatus = serverStatusProvider.getServerStatusData();
			}
			catch (Throwable th) {
				// we have to proctect internal code from external implementation...
				logger.log(Level.WARNING, "BeaconServerStatusProvider implementation thrown an exception.", th);
			}
		}

		// send beacon
		control.startMessage((byte)0, 12+2+2+16+2);

		buffer.put(guid);

		// TODO qosCode/flags (e.g. multicast/unicast)
		buffer.put((byte)0);

		buffer.put(beaconSequenceID);

		// TODO for now fixed changeCount
		buffer.putShort((short)0);

		// NOTE: is it possible (very likely) that address is any local address ::ffff:0.0.0.0
		InetAddressUtil.encodeAsIPv6Address(buffer, serverAddress);
		buffer.putShort((short)serverPort);

		SerializeHelper.serializeString(protocol, buffer, control);

		if (serverStatus != null)
		{
			// introspection interface + data
			serverStatus.getField().serialize(buffer, control);
			serverStatus.serialize(buffer, control);
		}
		else
			SerializationHelper.serializeNullField(buffer, control);

		control.flush(true);

		// increment beacon sequence ID
		beaconSequenceID++;

		reschedule();
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.impl.remote.TransportSender#unlock()
	 */
	public void unlock() {
		// noop
	}

	/* (non-Javadoc)
	 * @see org.epics.pvdata.misc.Timer.TimerCallback#timerStopped()
	 */
	public void timerStopped() {
		// noop
	}

	/**
	 * Destroy emitter.
	 */
	public void destroy()
	{
		timerNode.cancel();
	}

}
