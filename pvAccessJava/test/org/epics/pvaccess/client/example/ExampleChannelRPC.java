/*
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvaccess.client.example;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.epics.pvaccess.client.Channel;
import org.epics.pvaccess.client.Channel.ConnectionState;
import org.epics.pvaccess.client.ChannelProviderRegistryFactory;
import org.epics.pvaccess.client.ChannelProvider;
import org.epics.pvaccess.client.ChannelRPC;
import org.epics.pvaccess.client.ChannelRPCRequester;
import org.epics.pvaccess.client.ChannelRequester;
import org.epics.pvaccess.util.logging.ConsoleLogHandler;
import org.epics.pvaccess.util.logging.LoggingUtils;
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.MessageType;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Status;
import org.epics.pvdata.pv.Structure;

/**
 * ChannelRPC example, use against org.epics.pvaccess.server.rpc.test.RPCServiceExample service.
 * @author mse
 */
public class ExampleChannelRPC {

	private final static FieldCreate fieldCreate = FieldFactory.getFieldCreate();

	private final static Structure requestStructure =
		fieldCreate.createStructure(
				new String[] { "a", "b" },
				new Field[] { fieldCreate.createScalar(ScalarType.pvString),
							  fieldCreate.createScalar(ScalarType.pvString) }
				);
    public static void main(String[] args) throws Throwable {

        ConsoleLogHandler.defaultConsoleLogging(Level.INFO);
        Logger logger = Logger.getLogger(ExampleChannelRPC.class.getName());
        logger.setLevel(Level.ALL);

        org.epics.pvaccess.ClientFactory.start();

        ChannelProvider channelProvider =
        	ChannelProviderRegistryFactory.getChannelProviderRegistry()
        		.getProvider(org.epics.pvaccess.ClientFactory.PROVIDER_NAME);

        ChannelRequesterImpl channelRequester = new ChannelRequesterImpl(logger);
        Channel channel = channelProvider.createChannel("sum", channelRequester, ChannelProvider.PRIORITY_DEFAULT);

        ChannelRPCRequesterImpl channelRPCRequester = new ChannelRPCRequesterImpl(logger);
		channel.createChannelRPC(
				channelRPCRequester,
				null
				);

		if (channelRPCRequester.waitUntilConnected(3, TimeUnit.SECONDS))
		{
			PVStructure request = PVDataFactory.getPVDataCreate().createPVStructure(requestStructure);
			request.getStringField("a").put("12.3");
			request.getStringField("b").put("45.6");

			PVStructure result = channelRPCRequester.request(request);
			System.out.println(result);
		}
		else
			logger.info("Failed to do a RPC (timeout condition).");

        org.epics.pvaccess.ClientFactory.stop();
    }

    static class ChannelRequesterImpl implements ChannelRequester
    {
    	private final Logger logger;
    	public ChannelRequesterImpl(Logger logger)
    	{
    		this.logger = logger;
    	}

		public String getRequesterName() {
			return getClass().getName();
		}

		public void message(String message, MessageType messageType) {
			logger.log(LoggingUtils.toLevel(messageType), message);
		}

		public void channelCreated(Status status, Channel channel) {
			logger.info("Channel '" + channel.getChannelName() + "' created with status: " + status + ".");
		}

		public void channelStateChange(Channel channel, ConnectionState connectionState) {
			logger.info("Channel '" + channel.getChannelName() + "' " + connectionState + ".");
		}

    }

    static class ChannelRPCRequesterImpl implements ChannelRPCRequester
    {
    	private final Logger logger;
    	private final CountDownLatch connectedSignaler = new CountDownLatch(1);
    	private final Semaphore doneSemaphore = new Semaphore(0);

    	private volatile ChannelRPC channelRPC = null;
    	private volatile PVStructure result = null;

    	public ChannelRPCRequesterImpl(Logger logger)
    	{
    		this.logger = logger;
   	}

		public String getRequesterName() {
			return getClass().getName();
		}

		public void message(String message, MessageType messageType) {
			logger.log(LoggingUtils.toLevel(messageType), message);
		}

		public void channelRPCConnect(Status status, ChannelRPC channelRPC) {
			logger.info("ChannelRPC for '" + channelRPC.getChannel().getChannelName() + "' connected with status: " + status + ".");
			boolean reconnect = (this.channelRPC != null);
			this.channelRPC = channelRPC;

			connectedSignaler.countDown();

			// in case of reconnect, issued request was lost
			if (reconnect)
			{
				this.result = null;
				doneSemaphore.release();
			}
		}

		/**
		 * Wait until channel RPC is connected.
         * @param timeout  the maximum time to wait
		 * @param unit the time unit of the timeout argument
		 * @return true on success.
		 * @throws InterruptedException interrupted exception.
		 */
		public boolean waitUntilConnected(long timeout, TimeUnit unit) throws InterruptedException
		{
			return connectedSignaler.await(timeout, unit) && channelRPC != null;
		}

		public void requestDone(Status status, ChannelRPC channelRPC, PVStructure result) {
			logger.info("requestDone for '" + channelRPC.getChannel().getChannelName() + "' called with status: " + status + ".");

			this.result = result;
			doneSemaphore.release();
		}

		/**
		 * Issue an RPC request (blocking, one-at-the time).
		 * @param pvArgument RPC arguments.
		 * @return RPC result, <code>null</code> in case of error.
		 * @throws InterruptedException interrupted exception.
		 */
		public synchronized PVStructure request(PVStructure pvArgument) throws InterruptedException
		{
			ChannelRPC rpc = channelRPC;
			if (rpc == null)
				throw new IllegalStateException("ChannelRPC never connected.");

			rpc.request(pvArgument);
			// use tryAcquire if you need timeout support
			doneSemaphore.acquire(1);
			return result;
		}
    }

}
