/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvaccess.client.example;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.epics.pvaccess.client.Channel;
import org.epics.pvaccess.client.Channel.ConnectionState;
import org.epics.pvaccess.client.ChannelAccessFactory;
import org.epics.pvaccess.client.ChannelGet;
import org.epics.pvaccess.client.ChannelGetRequester;
import org.epics.pvaccess.client.ChannelProvider;
import org.epics.pvaccess.client.ChannelRequester;
import org.epics.pvaccess.client.CreateRequest;
import org.epics.pvaccess.util.logging.ConsoleLogHandler;
import org.epics.pvaccess.util.logging.LoggingUtils;
import org.epics.pvdata.misc.BitSet;
import org.epics.pvdata.pv.MessageType;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.Status;

/**
 * ChannelGet example
 * @author mse
 */
public class ExampleChannelV3Get {

    public static void main(String[] args) throws Throwable {
        
    	int len = args.length;
        if (len == 0 || len > 2)
        {
            System.out.println("Usage: <channelName> <pvRequest>");
            return;
        }
        
        final String channelName = args[0];
        final String request = args[1];
        
        ConsoleLogHandler.defaultConsoleLogging(Level.INFO);
        Logger logger = Logger.getLogger(ExampleChannelGet.class.getName());
        logger.setLevel(Level.ALL);

        org.epics.ca.ClientFactory.start();
        
        ChannelProvider channelProvider =
        	ChannelAccessFactory.getChannelAccess()
        		.getProvider(org.epics.ca.ClientFactory.PROVIDER_NAME);
        
        CountDownLatch doneSignal = new CountDownLatch(1);

        ChannelRequesterImpl channelRequester = new ChannelRequesterImpl(logger);
        Channel channel = channelProvider.createChannel(channelName, channelRequester, ChannelProvider.PRIORITY_DEFAULT);
        
        ChannelGetRequester channelGetRequester = new ChannelGetRequesterImpl(logger, channel, doneSignal);
        CreateRequest createRequest = CreateRequest.create();
    	PVStructure pvRequest = createRequest.createRequest(request);
    	if(pvRequest==null) {
    		System.out.println("createRequest failed " + createRequest.getMessage());
    		return;
    	}
		channel.createChannelGet(
				channelGetRequester,
				pvRequest
				);

		if (!doneSignal.await(3, TimeUnit.SECONDS))
			logger.info("Failed to get value (timeout condition).");
        
        org.epics.ca.ClientFactory.stop();
    }
    
    static class ChannelRequesterImpl implements ChannelRequester
    {
    	private final Logger logger;
    	public ChannelRequesterImpl(Logger logger)
    	{
    		this.logger = logger;
    	}

		@Override
		public String getRequesterName() {
			return getClass().getName();
		}

		@Override
		public void message(String message, MessageType messageType) {
			logger.log(LoggingUtils.toLevel(messageType), message);
		}

		@Override
		public void channelCreated(Status status, Channel channel) {
			logger.info("Channel '" + channel.getChannelName() + "' created with status: " + status + ".");
		}
		
		@Override
		public void channelStateChange(Channel channel, ConnectionState connectionState) {
			logger.info("Channel '" + channel.getChannelName() + "' " + connectionState + ".");
		}
    	
    }
    
    static class ChannelGetRequesterImpl implements ChannelGetRequester
    {
    	private final Logger logger;
    	private final Channel channel;
    	private final CountDownLatch doneSignaler;
    	
		private volatile PVStructure pvStructure = null;
   	
    	public ChannelGetRequesterImpl(Logger logger, Channel channel, CountDownLatch doneSignaler)
    	{
    		this.logger = logger;
    		this.channel = channel;
    		this.doneSignaler = doneSignaler;
    	}

		@Override
		public String getRequesterName() {
			return getClass().getName();
		}

		@Override
		public void message(String message, MessageType messageType) {
			logger.log(LoggingUtils.toLevel(messageType), message);
		}
		
		@Override
		public void channelGetConnect(Status status, ChannelGet channelGet,
				PVStructure pvStructure, BitSet bitSet) {
			logger.info("ChannelGet for '" + channel.getChannelName() + "' connected with status: " + status + ".");
			if (status.isSuccess())
			{
				this.pvStructure = pvStructure;
				channelGet.get(true);
			}
			else
				doneSignaler.countDown();
		}

		@Override
		public void getDone(Status status) {
			logger.info("getDone for '" + channel.getChannelName() + "' called with status: " + status + ".");

			if (status.isSuccess())
			{
				// NOTE: no need to call channelGet.lock()/unlock() since we read pvStructure in the same thread (i.e. in the callback)
				System.out.println(pvStructure.toString());
			}	
			
			doneSignaler.countDown();
		}
    }
    
}
