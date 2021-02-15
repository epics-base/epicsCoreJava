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

package org.epics.pvaccess.client.impl.remote.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.epics.pvaccess.impl.remote.Context;
import org.epics.pvaccess.impl.remote.SerializationHelper;
import org.epics.pvaccess.impl.remote.Transport;
import org.epics.pvaccess.impl.remote.TransportClient;
import org.epics.pvaccess.impl.remote.TransportSendControl;
import org.epics.pvaccess.impl.remote.TransportSender;
import org.epics.pvaccess.impl.remote.io.Poller;
import org.epics.pvaccess.impl.remote.request.ResponseHandler;
import org.epics.pvaccess.impl.remote.tcp.NonBlockingTCPTransport;
import org.epics.pvaccess.impl.security.SecurityPluginMessageTransportSender;
import org.epics.pvaccess.plugins.SecurityPlugin;
import org.epics.pvaccess.plugins.SecurityPlugin.SecurityPluginControl;
import org.epics.pvaccess.plugins.SecurityPlugin.SecuritySession;
import org.epics.pvdata.misc.SerializeHelper;
import org.epics.pvdata.misc.Timer.TimerCallback;
import org.epics.pvdata.misc.Timer.TimerNode;
import org.epics.pvdata.misc.TimerFactory;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.Status;

/**
 * Client TCP transport implementation.
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public class NonBlockingClientTCPTransport extends NonBlockingTCPTransport
	implements Transport, TimerCallback, TransportSender, SecurityPluginControl {

	/**
	 * Owners (users) of the transport.
	 */
	private Set<TransportClient> owners;

	/**
	 * Connection timeout (no-traffic) flag.
	 */
	private long connectionTimeout;

	/**
	 * Unresponsive transport flag.
	 */
	private volatile boolean unresponsiveTransport = false;

	/**
	 * Timer task node.
	 */
	private TimerNode timerNode;

	/**
	 * Timestamp of last "live" event on this transport.
	 */
	private volatile long aliveTimestamp;

	/**
	 * Client TCP transport constructor.
	 * @param context context where transport lives in.
	 * @param poller poller to use.
	 * @param channel used socker channel.
	 * @param responseHandler response handler used to process PVA headers.
	 * @param receiveBufferSize receive buffer size.
	 * @param client transport client (owner, requestor).
	 * @param remoteTransportRevision remote transport revision.
	 * @param heartbeatInterval heartbeat interval in seconds.
	 * @param priority transport priority.
	 * @throws SocketException thrown on any socket exception.
	 */
	public NonBlockingClientTCPTransport(Context context, /* TODO */ Poller poller, SocketChannel channel,
					ResponseHandler responseHandler, int receiveBufferSize,
					TransportClient client, short remoteTransportRevision,
					float heartbeatInterval, short priority) throws SocketException {
		super(context, poller, channel, responseHandler, receiveBufferSize, priority);

		// initialize owners list, send queue
		owners = new HashSet<TransportClient>();
		acquire(client);

		// use immediate for clients
		//setSendQueueFlushStrategy(SendQueueFlushStrategy.IMMEDIATE);

		// setup connection timeout timer (watchdog)
		connectionTimeout = (long)(heartbeatInterval * 1000);
		aliveTimestamp = System.currentTimeMillis();
		timerNode = TimerFactory.createNode(this);
		context.getTimer().schedulePeriodic(timerNode, heartbeatInterval, heartbeatInterval);

		//start();
	}

	/**
	 * @see org.epics.pvaccess.impl.remote.tcp.NonBlockingTCPTransport#internalClose()
	 */
	@Override
	protected void internalClose() {
		super.internalClose();

		timerNode.cancel();

		closedNotifyClients();
	}

	final Object sendBufferFreed = new Object();

	/**
	 * Notifies clients about disconnect.
	 */
	private void closedNotifyClients() {
		synchronized (owners)
		{
			// check if still acquired
			int refs = owners.size();
			if (refs > 0)
			{
				context.getLogger().fine("Transport to " + socketAddress + " still has " + refs + " client(s) active and closing...");
				TransportClient[] clients = new TransportClient[refs];
				owners.toArray(clients);
				for (int i = 0; i < clients.length; i++)
				{
					try
					{
						clients[i].transportClosed();
					}
					catch (Throwable th)
					{
						// TODO remove
						th.printStackTrace();
					}
				}
			}

			owners.clear();
		}
	}

	/**
	 * Acquires transport.
	 * @param client client (channel) acquiring the transport
	 * @return <code>true</code> if transport was granted, <code>false</code> otherwise.
	 */
	public synchronized boolean acquire(TransportClient client) {

		if (!isOpen())
			return false;

		context.getLogger().finer("Acquiring transport to " + socketAddress + ".");

		synchronized (owners)
		{
			if (!isOpen())
				return false;

			owners.add(client);
		}

		return true;
	}

	/**
	 * Releases transport.
	 * @param client client (channel) releasing the transport
	 */
	public synchronized void release(TransportClient client) {

		if (!isOpen())
			return;

		context.getLogger().finer("Releasing transport to " + socketAddress + ".");

		synchronized (owners)
		{
			owners.remove(client);

			// not used anymore
			// TODO consider delayed destruction (can improve performance!!!)
			if (owners.size() == 0)
			{
				try {
					close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Alive notification.
	 * This method needs to be called (by newly received data or beacon)
	 * at least once in this period, if not echo will be issued
	 * and if there is not reponse to it, transport will be considered as unresponsive.
	 */
	public final void aliveNotification()
	{
		aliveTimestamp = System.currentTimeMillis();
		if (unresponsiveTransport)
			responsiveTransport();
	}

	/* (non-Javadoc)
	 * @see org.epics.pvdata.misc.Timer.TimerCallback#timerStopped()
	 */
	public void timerStopped() {
		// noop
	}

	/**
	 * Live-check timer.
	 * @see org.epics.pvdata.misc.Timer.TimerCallback#callback()
	 */
	public void callback()
	{
		final long diff = System.currentTimeMillis() - aliveTimestamp;
		if (diff > ((3*connectionTimeout)/2))
		{
			unresponsiveTransport();
		}
		// use some k (3/4) to handle "jitter"
		else if (diff >= ((3*connectionTimeout)/4))
		{
			// send echo
			enqueueSendRequest(this);
		}
	}

	/**
	 * Responsive transport notify.
	 */
	private void responsiveTransport()
	{
		if (unresponsiveTransport)
		{
		    unresponsiveTransport = false;
			synchronized (owners)
			{
				for (TransportClient client : owners)
				{
					try
					{
						client.transportResponsive(this);
					}
					catch (Throwable th)
					{
						// TODO remove
						th.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * Unresponsive transport notify.
	 */
	private void unresponsiveTransport()
	{
		if (!unresponsiveTransport)
		{
		    unresponsiveTransport = true;

			synchronized (owners)
			{
				for (TransportClient client : owners)
				{
					try
					{
						client.transportUnresponsive();
					}
					catch (Throwable th)
					{
						// TODO remove
						th.printStackTrace();
					}
				}
			}
		}
	}


	/**
	 * Changed transport (server restared) notify.
	 */
	@Override
	public void changedTransport()
	{
		outgoingIR.reset();

		synchronized (owners)
		{
			for (TransportClient client : owners)
			{
				try
				{
					client.transportChanged();
				}
				catch (Throwable th)
				{
					// TODO remove
					th.printStackTrace();
				}
			}
		}
	}


	/* (non-Javadoc)
	 * @see org.epics.pvaccess.impl.remote.TransportSender#lock()
	 */
	public void lock() {
		// noop
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.impl.remote.TransportSender#unlock()
	 */
	public void unlock() {
		// noop
	}

	// always called from the same thread, therefore no sync needed
	private boolean verifyOrEcho = true;

	/**
	 * PVA connection validation response
	 */
	public void send(ByteBuffer buffer, TransportSendControl control) {

		if (verifyOrEcho)
		{
			//
			// send verification response message
			//

			control.startMessage((byte)1, 4+2+2);

			// receive buffer size
			buffer.putInt(getReceiveBufferSize());

			// max introspection registry size
			// TODO
			buffer.putShort(Short.MAX_VALUE);

			// QoS (aka connection priority(
			buffer.putShort(getPriority());

			// selected authNZ plug-in name
			String securityPluginName = (securitySession != null) ? securitySession.getSecurityPlugin().getId() : "";
			SerializeHelper.serializeString(securityPluginName, buffer, control);

			// optional authNZ plug-in initialization data
			if (securitySession != null)
				SerializationHelper.serializeFull(buffer, control, securitySession.initializationData());
			else
				SerializationHelper.serializeNullField(buffer, control);

			// send immediately
			control.flush(true);

			verifyOrEcho = false;
		}
		else
		{
			control.startMessage((byte)2, 0);
			// send immediately
			control.flush(true);
		}
	}

	public void authNZInitialize(Object data) {

		@SuppressWarnings("unchecked")
		List<String> offeredSecurityPlugins = (List<String>)(data);
		if (!offeredSecurityPlugins.isEmpty())
		{
			InetSocketAddress remoteAddress = (InetSocketAddress)channel.socket().getRemoteSocketAddress();
			Map<String, SecurityPlugin> availableSecurityPlugins = context.getSecurityPlugins();

			for (String offeredSPName : offeredSecurityPlugins)
			try
			{
				SecurityPlugin securityPlugin = availableSecurityPlugins.get(offeredSPName);
				if (securityPlugin != null && securityPlugin.isValidFor(remoteAddress))
				{
					// create session
					securitySession = securityPlugin.createSession(remoteAddress, this, null);
				}
			} catch (Throwable th) {
				context.getLogger().log(Level.SEVERE, "Unexpected exception caught while calling SecurityPluin.isValidFor(InetAddress) methods.", th);
			}
		}

		enqueueSendRequest(this);

	}

	private volatile SecuritySession securitySession = null;

	public void authNZMessage(PVField data) {
		SecuritySession ss = securitySession;
		if (ss != null)
			ss.messageReceived(data);
		else
			context.getLogger().warning("authNZ message received but no security plug-in session active");
	}

	public void sendSecurityPluginMessage(PVField data) {
		// TODO not optimal since it allocates a new object every time
		enqueueSendRequest(new SecurityPluginMessageTransportSender(data));
	}

	public void authenticationCompleted(Status status) {
		// noop for client side (server will send ConnectionValidation message)
	}

	// TODO move to proper place
	@Override
	public void close() throws IOException {

		if (securitySession != null)
		{
			try {
				securitySession.close();
			} catch (Throwable th) {
				context.getLogger().log(Level.WARNING, "Unexpection exception caight while closing secutiry session.", th);
			}

			securitySession = null;
		}

		super.close();
	}

	public SecuritySession getSecuritySession() {
		return securitySession;
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.impl.remote.codec.impl.NonBlockingAbstractCodec#ready()
	 */
	@Override
	protected void ready() {
		// noop
	}

}
