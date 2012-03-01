/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.ca.client.example;

import java.util.concurrent.atomic.AtomicInteger;

import org.epics.ca.CAException;
import org.epics.ca.client.Channel;
import org.epics.ca.client.Channel.ConnectionState;
import org.epics.ca.client.ChannelAccess;
import org.epics.ca.client.ChannelAccessFactory;
import org.epics.ca.client.ChannelProvider;
import org.epics.ca.client.ChannelRequester;
import org.epics.pvData.pv.MessageType;
import org.epics.pvData.pv.Status;


/**
 * ChannelGet example
 * @author mrk
 */
public class TestConnect {

    /**
     * main.
     * @param  args is a sequence of flags and filenames.
     */
    public static void main(String[] args) throws CAException {
        org.epics.ca.ClientFactory.start();
        Client client = new Client();
        client.waitUntilDone(1000000);
        org.epics.ca.ClientFactory.stop();
        System.exit(0);
    }
    
    private static final String providerName = "pvAccess";
    private static final ChannelAccess channelAccess = ChannelAccessFactory.getChannelAccess();
    //private static final PVDataCreate pvDataCreate = PVFactory.getPVDataCreate();
    
    private static class Client implements ChannelRequester {
        
        private final ChannelProvider channelProvider;
        private final AtomicInteger counter = new AtomicInteger(100000);
        Client() {
            channelProvider = channelAccess.getProvider(providerName);
            int totalCh = counter.get();
            for (int i = 0; i < totalCh; i++)
            	channelProvider.createChannel("test"+i, this, ChannelProvider.PRIORITY_DEFAULT);
        }
        
        
        public void waitUntilDone(long timeoutMs) {
        	System.out.println("waiting");
        	int i = 0;
        	synchronized (this) {
				while (counter.get() > 0) {
					try {
						this.wait(timeoutMs/1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if (i++ == 1000)
						break;
					System.out.println("to go:" + counter.get());
				}
			}
        }
        
        private void done() {
        	synchronized (this) {
        		this.notifyAll();
        	}
        }
        
        /* (non-Javadoc)
         * @see org.epics.ca.client.ChannelRequester#channelCreated(org.epics.pvData.pv.Status, org.epics.ca.client.Channel)
         */
        @Override
        public void channelCreated(Status status, Channel channel) {
            if(!status.isSuccess()) {
                return;
            }
        }
        /* (non-Javadoc)
         * @see org.epics.ca.client.ChannelRequester#channelStateChange(org.epics.ca.client.Channel, org.epics.ca.client.Channel.ConnectionState)
         */
        @Override
        public void channelStateChange(Channel c,ConnectionState connectionState) {
            if(connectionState==ConnectionState.CONNECTED) {
                if (counter.decrementAndGet() == 0)
                	done();
            } else {
                message(connectionState.name(),MessageType.info);
                // TODO
            }
        }

        /* (non-Javadoc)
         * @see org.epics.pvData.pv.Requester#getRequesterName()
         */
        @Override
        public String getRequesterName() {
            return "example";
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.Requester#message(java.lang.String, org.epics.pvData.pv.MessageType)
         */
        @Override
        public void message(String message, MessageType messageType) {
            if(messageType!=MessageType.info) {
               // System.err.println(messageType + " " + message);
            } else {
                System.out.println(message);
            }
        }
        
    }
}
