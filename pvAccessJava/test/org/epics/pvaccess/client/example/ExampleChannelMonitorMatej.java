/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvaccess.client.example;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.epics.pvaccess.client.Channel;
import org.epics.pvaccess.client.Channel.ConnectionState;
import org.epics.pvaccess.client.ChannelAccessFactory;
import org.epics.pvaccess.client.ChannelProvider;
import org.epics.pvaccess.client.ChannelRequester;
import org.epics.pvaccess.client.CreateRequestFactory;
import org.epics.pvaccess.util.logging.ConsoleLogHandler;
import org.epics.pvdata.monitor.Monitor;
import org.epics.pvdata.monitor.MonitorElement;
import org.epics.pvdata.monitor.MonitorRequester;
import org.epics.pvdata.pv.MessageType;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.Status;
import org.epics.pvdata.pv.Structure;

/**
 * ChannelGet example
 * @author mse
 */
public class ExampleChannelMonitorMatej {

    public static void main(String[] args) throws Throwable {
        int len = args.length;
        if(len<1 || len>2 || (len==1 && args[0].equals("?"))) {
            System.out.println("Usage: channelName request");
            return;
        }
        
        final String channelName = args[0];
        final String pvRequestString = args[1];
        
        ConsoleLogHandler.defaultConsoleLogging(Level.INFO);
        Logger logger = Logger.getLogger(ExampleChannelMonitorMatej.class.getName());
        logger.setLevel(Level.ALL);

        org.epics.pvaccess.ClientFactory.start();
        
        ChannelProvider channelProvider =
        	ChannelAccessFactory.getChannelAccess()
        		.getProvider(org.epics.pvaccess.ClientFactory.PROVIDER_NAME);
        
        ChannelRequesterImpl channelRequester = new ChannelRequesterImpl(logger, pvRequestString);
        channelProvider.createChannel(channelName, channelRequester, ChannelProvider.PRIORITY_DEFAULT);

        // TODO
        Thread.sleep(Long.MAX_VALUE);
        
        org.epics.pvaccess.ClientFactory.stop();
    }
    
    static class ChannelRequesterImpl implements ChannelRequester
    {
    	private final Logger logger;
    	private final PVStructure pvRequest;
    	public ChannelRequesterImpl(Logger logger, String pvRequestString)
    	{
    		this.logger = logger;
    		this.pvRequest = CreateRequestFactory.createRequest(pvRequestString, this);
    		if (pvRequest == null)
    			throw new IllegalArgumentException();
    	}

		@Override
		public String getRequesterName() {
			return getClass().getName();
		}

		@Override
		public void message(String message, MessageType messageType) {
			logger.log(toLoggerLevel(messageType), message);
		}

		@Override
		public void channelCreated(Status status, Channel channel) {
			logger.info("Channel '" + channel.getChannelName() + "' created with status: " + status);
		}
		
		private final AtomicBoolean first = new AtomicBoolean(false);

		@Override
		public void channelStateChange(Channel channel, ConnectionState connectionState) {
			logger.info("Channel '" + channel.getChannelName() + "' " + connectionState);
			
			if (connectionState == ConnectionState.CONNECTED && !first.getAndSet(true))
				channel.createMonitor(
						new MonitorRequesterImpl(logger, channel),
						pvRequest
						);
		}
    	
    }
    
    static class MonitorRequesterImpl implements MonitorRequester
    {
    	private final Logger logger;
    	//private final Channel channel;
    	
    	public MonitorRequesterImpl(Logger logger, Channel channel)
    	{
    		this.logger = logger;
    		//this.channel = channel;
    	}

		@Override
		public String getRequesterName() {
			return getClass().getName();
		}

		@Override
		public void message(String message, MessageType messageType) {
			logger.log(toLoggerLevel(messageType), message);
		}
		
		@Override
		public void monitorConnect(Status status, Monitor monitor,
				Structure structure) {

			status = monitor.start();
			if (!status.isSuccess())
			{
				// TODO
				System.err.println(status);
			}
		}

		@Override
		public void monitorEvent(Monitor monitor) {
			
			MonitorElement element;
			while ((element = monitor.poll()) != null)
			{
				System.out.println("Changed: " + element.getChangedBitSet());
				System.out.println("Overrun: " + element.getOverrunBitSet());
				System.out.println(element.getPVStructure());
				// TODO bug in pvAccess if this is not called
				monitor.release(element);
			}
			
		}

		@Override
		public void unlisten(Monitor monitor) {
			System.out.println("unlisten");
		}
    }

	public static Level toLoggerLevel(MessageType messageType) {
		switch (messageType)
		{
			case info:
				return Level.INFO;
			case warning:
				return Level.WARNING;
			case error:
			case fatalError:
				return Level.SEVERE;
			default:
				return Level.INFO;
		}
	}

}
