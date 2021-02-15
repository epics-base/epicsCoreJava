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

package org.epics.pvaccess.server.impl.remote.handlers;

import java.nio.ByteBuffer;

import org.epics.pvaccess.PVFactory;
import org.epics.pvaccess.impl.remote.Transport;
import org.epics.pvaccess.impl.remote.TransportSendControl;
import org.epics.pvaccess.impl.remote.TransportSender;
import org.epics.pvaccess.server.impl.remote.ServerChannelImpl;
import org.epics.pvaccess.server.impl.remote.ServerContextImpl;
import org.epics.pvdata.misc.Destroyable;
import org.epics.pvdata.misc.SerializeHelper;
import org.epics.pvdata.pv.MessageType;
import org.epics.pvdata.pv.Requester;
import org.epics.pvdata.pv.Status;
import org.epics.pvdata.pv.Status.StatusType;
import org.epics.pvdata.pv.StatusCreate;

/**
 * Base requester class.
 * @author msekoranja
 */
abstract class BaseChannelRequester implements Requester, Destroyable {

    public static final StatusCreate statusCreate = PVFactory.getStatusCreate();

    public static final Status okStatus = statusCreate.getStatusOK();
    public static final Status badCIDStatus = statusCreate.createStatus(StatusType.ERROR, "bad channel id", null);
    public static final Status badIOIDStatus = statusCreate.createStatus(StatusType.ERROR, "bad request id", null);
    public static final Status noReadACLStatus = statusCreate.createStatus(StatusType.ERROR, "no read access", null);
    public static final Status noWriteACLStatus = statusCreate.createStatus(StatusType.ERROR, "no write access", null);
    public static final Status noProcessACLStatus = statusCreate.createStatus(StatusType.ERROR, "no process access", null);
    public static final Status otherRequestPendingStatus = statusCreate.createStatus(StatusType.ERROR, "other request pending", null);
    public static final Status notAChannelRequest = statusCreate.createStatus(StatusType.ERROR, "not a channel request", null);


	protected final ServerContextImpl context;
	protected final ServerChannelImpl channel;
	protected final int ioid;
	protected final Transport transport;

	private static final int NULL_REQUEST = -1;
	protected int pendingRequest = NULL_REQUEST;

	public BaseChannelRequester(ServerContextImpl context, ServerChannelImpl channel, int ioid, Transport transport) {
		this.context = context;
		this.channel = channel;
		this.ioid = ioid;
		this.transport = transport;
	}

	public boolean startRequest(int qos) {
		synchronized (this) {
			if (pendingRequest != NULL_REQUEST)
				return false;

			pendingRequest = qos;
			return true;
		}
	}

	public void stopRequest() {
		synchronized (this) {
			pendingRequest = NULL_REQUEST;
		}
	}

	public int getPendingRequest() {
		synchronized (this) {
			return pendingRequest;
		}
	}

	public String getRequesterName() {
		return transport + "/" + ioid;
	}

	public void message(final String message, final MessageType messageType) {
		message(transport, ioid, message, messageType);
	}

	/**
	 * Send message.
	 * @param transport transport to use.
	 * @param ioid request IO ID.
	 * @param message the message.
	 * @param messageType message type.
	 */
	public static void message(final Transport transport, final int ioid, final String message, final MessageType messageType) {
		transport.enqueueSendRequest(
				new TransportSender() {

					public void send(ByteBuffer buffer, TransportSendControl control) {
						control.startMessage((byte)18, Integer.SIZE/Byte.SIZE + 1);
						buffer.putInt(ioid);
						buffer.put((byte)messageType.ordinal());
						SerializeHelper.serializeString(message, buffer, control);
					}

					public void lock() {
						// noop
					}

					public void unlock() {
						// noop
					}

			});
	}

	/**
	 * Send failure message.
	 * @param command failed command.
	 * @param transport transport to use.
	 * @param ioid request IO ID.
	 * @param qos requested QoS.
	 * @param status completion status (failure).
	 */
	public static void sendFailureMessage(final byte command, final Transport transport, final int ioid, final byte qos, final Status status) {
		transport.enqueueSendRequest(
			new TransportSender() {

				public void send(ByteBuffer buffer, TransportSendControl control) {
					control.startMessage(command, Integer.SIZE/Byte.SIZE + 1);
					buffer.putInt(ioid);
					buffer.put(qos);
					status.serialize(buffer, control);
				}

				public void lock() {
					// noop
				}

				public void unlock() {
					// noop
				}

		});
	}

}
