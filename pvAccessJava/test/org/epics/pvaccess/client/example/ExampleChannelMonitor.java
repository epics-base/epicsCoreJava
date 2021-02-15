/*
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvaccess.client.example;

import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.epics.pvaccess.client.Channel;
import org.epics.pvaccess.client.Channel.ConnectionState;
import org.epics.pvaccess.client.ChannelProvider;
import org.epics.pvaccess.client.ChannelProviderRegistryFactory;
import org.epics.pvaccess.client.ChannelRequester;
import org.epics.pvaccess.util.logging.ConsoleLogHandler;
import org.epics.pvaccess.util.logging.LoggingUtils;
import org.epics.pvdata.copy.CreateRequest;
import org.epics.pvdata.monitor.Monitor;
import org.epics.pvdata.monitor.MonitorElement;
import org.epics.pvdata.monitor.MonitorRequester;
import org.epics.pvdata.pv.MessageType;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.Status;
import org.epics.pvdata.pv.Structure;

/**
 * ExampleChannelMonitor example
 * @author mse
 */
public class ExampleChannelMonitor {

	public static void main(String[] args) throws Throwable {

		int len = args.length;
		if (len == 0 || len > 2)
		{
			System.out.println("Usage: <channelName> <pvRequest>");
			return;
		}


		final String channelName = args[0];
		final String pvRequestString = args[1];

		ConsoleLogHandler.defaultConsoleLogging(Level.INFO);
		Logger logger = Logger.getLogger(ExampleChannelMonitor.class.getName());
		logger.setLevel(Level.ALL);

		org.epics.pvaccess.ClientFactory.start();

		ChannelProvider channelProvider =
				ChannelProviderRegistryFactory.getChannelProviderRegistry()
				.getProvider(org.epics.pvaccess.ClientFactory.PROVIDER_NAME);

		CountDownLatch doneSignal = new CountDownLatch(1);

		ChannelRequesterImpl channelRequester = new ChannelRequesterImpl(logger);
		Channel channel = channelProvider.createChannel(channelName, channelRequester, ChannelProvider.PRIORITY_DEFAULT);

		MonitorRequesterImpl monitorRequester = new MonitorRequesterImpl(logger, channel, doneSignal);
		CreateRequest createRequest = CreateRequest.create();
		PVStructure pvRequest = createRequest.createRequest(pvRequestString);
		if(pvRequest==null) {
			monitorRequester.message(createRequest.getMessage(), MessageType.error);
		} else {
			channel.createMonitor(
					monitorRequester,
					createRequest.createRequest(pvRequestString)
					);
			// wait forever
			doneSignal.await();
		}

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

    static class MonitorRequesterImpl implements MonitorRequester
    {
    	private final Logger logger;
    	private final Channel channel;
    	private final CountDownLatch doneSignaler;

    	public MonitorRequesterImpl(Logger logger, Channel channel, CountDownLatch doneSignaler)
    	{
    		this.logger = logger;
    		this.channel = channel;
    		this.doneSignaler = doneSignaler;
    	}

		public String getRequesterName() {
			return getClass().getName();
		}

		public void message(String message, MessageType messageType) {
			logger.log(LoggingUtils.toLevel(messageType), message);
		}

		public void monitorConnect(Status status, Monitor monitor,
				Structure structure) {
			logger.info("ChannelMonitor for '" + channel.getChannelName() + "' connected with status: " + status + ".");

			if (status.isSuccess())
			{
				status = monitor.start();
				logger.info("Monitor.start() status: " + status + ".");

				if (!status.isSuccess())
					doneSignaler.countDown();
			}
			else
				doneSignaler.countDown();
		}

		public void monitorEvent(Monitor monitor) {

			MonitorElement element;
			while ((element = monitor.poll()) != null)
			{
				System.out.println("Changed: " + element.getChangedBitSet());
				System.out.println("Overrun: " + element.getOverrunBitSet());
				System.out.println(element.getPVStructure());
				System.out.println();
				monitor.release(element);
			}

		}

		public void unlisten(Monitor monitor) {
			logger.info("ChannelMonitor for '" + channel.getChannelName() + "' unlisten called.");
		}
    }

}
