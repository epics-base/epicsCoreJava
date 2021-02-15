/*
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvaccess.client.example;

import java.util.concurrent.atomic.AtomicInteger;

import org.epics.pvaccess.PVAException;
import org.epics.pvaccess.client.Channel;
import org.epics.pvaccess.client.ChannelProviderRegistry;
import org.epics.pvaccess.client.ChannelProviderRegistryFactory;
import org.epics.pvaccess.client.ChannelProvider;
import org.epics.pvaccess.client.ChannelRequester;
import org.epics.pvaccess.client.Channel.ConnectionState;
import org.epics.pvdata.pv.MessageType;
import org.epics.pvdata.pv.Status;


/**
 * ChannelGet example
 * @author mrk
 */
public class TestConnect {

    public static void main(String[] args) throws PVAException {
        org.epics.pvaccess.ClientFactory.start();
        Client client = new Client();
        client.waitUntilDone(1000000);
        org.epics.pvaccess.ClientFactory.stop();
        System.exit(0);
    }

    private static final String providerName = org.epics.pvaccess.ClientFactory.PROVIDER_NAME;
    private static final ChannelProviderRegistry channelAccess = ChannelProviderRegistryFactory.getChannelProviderRegistry();
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
         * @see org.epics.pvaccess.client.ChannelRequester#channelCreated(org.epics.pvdata.pv.Status, org.epics.pvaccess.client.Channel)
         */
        public void channelCreated(Status status, Channel channel) {
            if(!status.isSuccess()) {
                return;
            }
        }
        /* (non-Javadoc)
         * @see org.epics.pvaccess.client.ChannelRequester#channelStateChange(org.epics.pvaccess.client.Channel, org.epics.pvaccess.client.Channel.ConnectionState)
         */
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
         * @see org.epics.pvdata.pv.Requester#getRequesterName()
         */
        public String getRequesterName() {
            return "example";
        }
        /* (non-Javadoc)
         * @see org.epics.pvdata.pv.Requester#message(java.lang.String, org.epics.pvdata.pv.MessageType)
         */
        public void message(String message, MessageType messageType) {
            if(messageType!=MessageType.info) {
               // System.err.println(messageType + " " + message);
            } else {
                System.out.println(message);
            }
        }

    }
}
