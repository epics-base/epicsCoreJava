/*
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvaccess.client.example;

import org.epics.pvaccess.PVAException;
import org.epics.pvaccess.client.Channel;
import org.epics.pvaccess.client.Channel.ConnectionState;
import org.epics.pvaccess.client.ChannelProvider;
import org.epics.pvaccess.client.ChannelProviderRegistry;
import org.epics.pvaccess.client.ChannelProviderRegistryFactory;
import org.epics.pvaccess.client.ChannelPut;
import org.epics.pvaccess.client.ChannelPutRequester;
import org.epics.pvaccess.client.ChannelRequester;
import org.epics.pvdata.copy.CreateRequest;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.misc.BitSet;
import org.epics.pvdata.pv.MessageType;
import org.epics.pvdata.pv.PVDouble;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.Status;
import org.epics.pvdata.pv.Structure;

/**
 * TODO cleanup
 * ChannelGet example
 * @author mrk
 */
public class ExampleChannelPutDoubleValue {

    /**
     * main.
     * @param  args is a sequence of flags and filenames.
     * @throws PVAException PVA exception.
     */
    public static void main(String[] args) throws PVAException {
        org.epics.pvaccess.ClientFactory.start();
        int len = args.length;
        if(len<1 || len>2 || (len==1 && args[0].equals("?"))) {
            System.out.println("Usage: <channelName> <request>");
            return;
        }
        String channelName = args[0];
        String request = null;
        if(len==2) {
            request = args[1];
        }
        CreateRequest createRequest = CreateRequest.create();
    	if(request==null) request = "";
    	PVStructure pvRequest = createRequest.createRequest(request);
    	if(pvRequest==null) {
    		System.out.println("createRequest failed " + createRequest.getMessage());
    		return;
    	}
        Client client = new Client(channelName,pvRequest);
        client.waitUntilDone(10000);
        org.epics.pvaccess.ClientFactory.stop();
        System.exit(0);
    }

    private static final String providerName = org.epics.pvaccess.ClientFactory.PROVIDER_NAME;
    private static final ChannelProviderRegistry channelAccess = ChannelProviderRegistryFactory.getChannelProviderRegistry();

    private static class Client implements ChannelRequester, ChannelPutRequester {

        private final PVStructure pvRequest;
        private final ChannelProvider channelProvider;
        private final Channel channel;
        private boolean done = false;

        Client(String channelName,PVStructure pvRequest) {
        	this.pvRequest = pvRequest;
            channelProvider = channelAccess.getProvider(providerName);
            channel = channelProvider.createChannel(channelName, this, ChannelProvider.PRIORITY_DEFAULT);
        }

        public void waitUntilDone(long timeoutMs) {
        	System.out.println("waiting");
        	synchronized (this) {
				if (!done) {
					try {
						this.wait(timeoutMs);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
        }

        private void done() {
    		channel.destroy();
        	synchronized (this) {
        		done = true;
        		this.notifyAll();
        	}
        }

        /* (non-Javadoc)
         * @see org.epics.pvaccess.client.ChannelRequester#channelCreated(org.epics.pvdata.pv.Status, org.epics.pvaccess.client.Channel)
         */
        public void channelCreated(Status status, Channel channel) {
            if(!status.isSuccess()) {
                message("channelCreated " + status.getMessage(),MessageType.error);
                done();
                return;
            }
        }
        /* (non-Javadoc)
         * @see org.epics.pvaccess.client.ChannelRequester#channelStateChange(org.epics.pvaccess.client.Channel, org.epics.pvaccess.client.Channel.ConnectionState)
         */
        public void channelStateChange(Channel c,ConnectionState connectionState) {
            if(connectionState==ConnectionState.CONNECTED) {
                channel.createChannelPut(this, pvRequest);
            } else {
                message(connectionState.name(),MessageType.info);
                done = true;
            }
        }
        /* (non-Javadoc)
		 * @see org.epics.pvaccess.client.ChannelPutRequester#channelPutConnect(org.epics.pvdata.pv.Status, org.epics.pvaccess.client.ChannelPut, org.epics.pvdata.pv.PVStructure, org.epics.pvdata.misc.BitSet)
		 */
		public void channelPutConnect(Status status, ChannelPut channelPut,
				Structure structure) {
            if(!status.isSuccess()) {
                message("channelPutConnect " + status.getMessage(),MessageType.error);
                done();
                return;
            }


            PVStructure pvStructure = PVDataFactory.getPVDataCreate().createPVStructure(structure);
            BitSet bitSet = new BitSet(pvStructure.getNumberFields());

            PVDouble val = pvStructure.getDoubleField("value");
            bitSet.set(val.getFieldOffset());
            val.put(Math.random()*10);

            channelPut.lastRequest();
            channelPut.put(pvStructure, bitSet);
		}

		public void putDone(Status status, ChannelPut channelPut) {
            if(!status.isSuccess()) {
                message("putDone " + status.getMessage(),MessageType.error);
                done();
                return;
            }
            done();
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
                System.err.println(messageType + " " + message);
            } else {
                System.out.println(message);
            }
        }



		public void getDone(Status status, ChannelPut channelPut,
				PVStructure pvStructure, BitSet bitSet) {
			// not used
		}

    }
}
