/**
 * 
 */
package org.epics.pvaccess.client.rpc;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.epics.pvaccess.client.Channel;
import org.epics.pvaccess.client.Channel.ConnectionState;
import org.epics.pvaccess.client.ChannelAccessFactory;
import org.epics.pvaccess.client.ChannelProvider;
import org.epics.pvaccess.client.ChannelRPC;
import org.epics.pvaccess.client.ChannelRPCRequester;
import org.epics.pvaccess.client.ChannelRequester;
import org.epics.pvaccess.server.rpc.RPCRequestException;
import org.epics.pvdata.factory.StatusFactory;
import org.epics.pvdata.pv.MessageType;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.Status;
import org.epics.pvdata.pv.Status.StatusType;

/**
 * @author msekoranja
 *
 */
public class RPCClientImpl implements RPCClient, ChannelRequester, ChannelRPCRequester {

    private static final Logger logger = Logger.getLogger(RPCClientImpl.class.getName());

    private final RPCClientRequester serviceRequester;
	private final Channel channel;
	private final CountDownLatch connectedSignaler = new CountDownLatch(1);
	
	private final AtomicBoolean requestPending = new AtomicBoolean(false);

	private volatile ChannelRPC channelRPC = null;
	
	private final Object resultMonitor = new Object();
	private Status status = null;
	private PVStructure result = null;

    public RPCClientImpl(String serviceName) {
		this(serviceName, null);
	}

	public RPCClientImpl(String serviceName, RPCClientRequester requester) {
		
		this.serviceRequester = requester;

		org.epics.pvaccess.ClientFactory.start();
        
        ChannelProvider channelProvider =
        	ChannelAccessFactory.getChannelAccess()
        		.getProvider(org.epics.pvaccess.ClientFactory.PROVIDER_NAME);
 
        this.channel = channelProvider.createChannel(serviceName, this, ChannelProvider.PRIORITY_DEFAULT);
		channel.createChannelRPC(this, null);
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.client.rpc.RPCClient#destroy()
	 */
	@Override
	public void destroy() {
		channel.destroy();
		//org.epics.pvaccess.ClientFactory.stop();
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.client.rpc.RPCClient#waitConnect(double)
	 */
	@Override
	public boolean waitConnect(double timeout) {
		try {
			return connectedSignaler.await((long)(timeout*1000), TimeUnit.MILLISECONDS) && channelRPC != null;
		} catch (InterruptedException e) {
			return false;
		}
	}
	
	private ChannelRPC checkConnectAndPending(double timeout)
	{
		ChannelRPC rpc;
		while ((rpc = channelRPC) == null)
		{
			if (timeout == 0 || !waitConnect(timeout))
				throw new IllegalStateException("ChannelRPC never connected.");
		}
		
		// check pending
		if (requestPending.getAndSet(true))
			throw new IllegalStateException("one request already pending");
		
		return rpc;
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.client.rpc.RPCClient#request(org.epics.pvdata.pv.PVStructure, double)
	 */
	@Override
	public PVStructure request(PVStructure pvArgument, double timeout)
			throws RPCRequestException {

		long startTime = System.currentTimeMillis();
		long timeoutMs = (long)(timeout*1000);
		
		synchronized (resultMonitor) {
			sendRequestInternal(timeout, pvArgument);
			long timeLeft = Math.max(timeoutMs - (System.currentTimeMillis() - startTime), 0);
			if (waitResponse(timeLeft))
			{
				if (status.isSuccess())
					return result;
				else
				{
					if (status.getStackDump() == null)
						throw new RPCRequestException(status.getType(), status.getMessage());
					else
						throw new RPCRequestException(status.getType(), status.getMessage() + ", cause:\n" + status.getStackDump());
				}
			}
			else
			{
				// timeout
				throw new RPCRequestException(StatusType.ERROR, "timeout");
			}
			
		}
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.client.rpc.RPCClient#sendRequest(org.epics.pvdata.pv.PVStructure)
	 */
	@Override
	public void sendRequest(PVStructure pvArgument) {
		sendRequestInternal(0, pvArgument);
	}
	
	private void sendRequestInternal(double connectTimeout, PVStructure pvArgument)
	{
		ChannelRPC rpc = checkConnectAndPending(connectTimeout);
		
		synchronized (resultMonitor) {
			status = null;
			result = null;
		}
		
		try {
			rpc.request(pvArgument, false);
		} catch (Throwable th) {
			requestDone(
					StatusFactory.getStatusCreate().createStatus(
							StatusType.ERROR, "failed to send a RPC request", th),
							null
						);
		}
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.client.rpc.RPCClient#waitResponse(double)
	 */
	@Override
	public boolean waitResponse(double timeout) {
		synchronized (resultMonitor) {
			long timeoutMs = (long)(timeout*1000);
			// NOTE: spurious wakeup proof code
			long startTime = System.currentTimeMillis();
			long diff;
			while (status == null && (diff = (System.currentTimeMillis() - startTime)) < timeoutMs)
			{
				try {
					resultMonitor.wait(timeoutMs - diff);
				} catch (InterruptedException e) {
					return false;
				}
			}
			return status != null;
		}
	}

	@Override
	public String getRequesterName() {
		return serviceRequester != null ? serviceRequester.getRequesterName() : getClass().getName();
	}

	@Override
	public void message(String message, MessageType messageType) {
		if (serviceRequester != null)
			serviceRequester.message(message, messageType);
		else
			logger.finer(getRequesterName() + ": [" +  messageType + "] " + message);
	}

	@Override
	public void channelCreated(Status status, Channel channel) {
		logger.finer("Channel '" + channel.getChannelName() + "' created with status: " + status + ".");
	}
	
	@Override
	public void channelStateChange(Channel channel, ConnectionState connectionState) {
		logger.finer("Channel '" + channel.getChannelName() + "' " + connectionState + ".");

		if (connectionState != ConnectionState.CONNECTED && requestPending.get())
			requestDone(
					StatusFactory.getStatusCreate().createStatus(
							StatusType.ERROR, "channel " + connectionState, null),
							null
							);
	}
	
	@Override
	public void channelRPCConnect(Status status, ChannelRPC channelRPC) {
		logger.finer("ChannelRPC for '" + channel.getChannelName() + "' connected with status: " + status + ".");
		this.channelRPC = channelRPC;

		connectedSignaler.countDown();
		
		if (serviceRequester != null)
		{
			try {
				serviceRequester.connectResult(this, status);
			} catch (Throwable th) {
				logger.log(Level.SEVERE, "Unhandled exception in RPCClientRequester.connectResult().", th);
			}
		}
	}

	@Override
	public void requestDone(Status status, PVStructure result) {
		logger.finer("requestDone for '" + channel.getChannelName() + "' called with status: " + status + ".");

		requestPending.set(false);

		synchronized (resultMonitor) {
			this.status = status;
			this.result = result;
			resultMonitor.notifyAll();
		}

		if (serviceRequester != null)
		{
			try {
				serviceRequester.requestResult(this, status, result);
			} catch (Throwable th) {
				logger.log(Level.SEVERE, "Unhandled exception in RPCClientRequester.requestResult().", th);
			}
		}
	}

}
