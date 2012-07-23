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
import org.epics.pvaccess.client.ChannelGet;
import org.epics.pvaccess.client.ChannelGetRequester;
import org.epics.pvaccess.client.ChannelProvider;
import org.epics.pvaccess.client.ChannelRequester;
import org.epics.pvaccess.client.CreateRequestFactory;
import org.epics.pvaccess.util.logging.ConsoleLogHandler;
import org.epics.pvdata.misc.BitSet;
import org.epics.pvdata.pv.MessageType;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.Status;

/**
 * ChannelGet example
 * @author mse
 */
public class ExampleChannelGetMatej {

    public static void main(String[] args) throws Throwable {
        int len = args.length;
        if(len<1 || len>2 || (len==1 && args[0].equals("?"))) {
            System.out.println("Usage: channelName request");
            return;
        }
        
        final String channelName = args[0];
        final String pvRequestString = args[1];
        
        ConsoleLogHandler.defaultConsoleLogging(Level.INFO);
        Logger logger = Logger.getLogger(ExampleChannelGetMatej.class.getName());
        logger.setLevel(Level.ALL);

        org.epics.pvaccess.ClientFactory.start();
        
        ChannelProvider channelProvider =
        	ChannelAccessFactory.getChannelAccess()
        		.getProvider(org.epics.pvaccess.ClientFactory.PROVIDER_NAME);
        
        Object monitor = new Object();

        ChannelRequesterImpl channelRequester = new ChannelRequesterImpl(logger, monitor, pvRequestString);
        channelProvider.createChannel(channelName, channelRequester, ChannelProvider.PRIORITY_DEFAULT);
        
        // TODO spurious wakup check
        synchronized (monitor) {
        	monitor.wait(3000);
		}
        
        org.epics.pvaccess.ClientFactory.stop();
    }
    
    static class ChannelRequesterImpl implements ChannelRequester
    {
    	private final Logger logger;
    	private final Object monitor;
    	private final PVStructure pvRequest;
    	public ChannelRequesterImpl(Logger logger, Object monitor, String pvRequestString)
    	{
    		this.logger = logger;
    		this.monitor = monitor;
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
				channel.createChannelGet(
						new ChannelGetRequesterImpl(logger, channel, monitor),
						pvRequest
						);
		}
    	
    }
    
    static class ChannelGetRequesterImpl implements ChannelGetRequester
    {
    	private final Logger logger;
    	private final Channel channel;
    	private final Object monitor;
    	
		private volatile PVStructure pvStructure = null;
   	
    	public ChannelGetRequesterImpl(Logger logger, Channel channel, Object monitor)
    	{
    		this.logger = logger;
    		this.channel = channel;
    		this.monitor = monitor;
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
		public void channelGetConnect(Status status, ChannelGet channelGet,
				PVStructure pvStructure, BitSet bitSet) {
			logger.info("ChannelGet for '" + channel.getChannelName() + "' connected with status: " + status);
			
			if (status.isSuccess())
			{
				this.pvStructure = pvStructure;
				channelGet.get(false);
			}
		}

		@Override
		public void getDone(Status status) {
			logger.info("getDone for '" + channel.getChannelName() + "' called with status: " + status);

			if (status.isSuccess())
			{
				// NOTE: no need to call channelGet.lock()/unlock() since we read pvStructure in the same thread (i.e. in the callback)
				System.out.println(pvStructure.toString());
			}	
			
			synchronized (monitor) {
				monitor.notifyAll();
			}
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
