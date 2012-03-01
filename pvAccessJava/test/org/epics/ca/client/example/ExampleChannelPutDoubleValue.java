/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.ca.client.example;

import org.epics.ca.CAException;
import org.epics.ca.PVFactory;
import org.epics.ca.client.Channel;
import org.epics.ca.client.Channel.ConnectionState;
import org.epics.ca.client.ChannelAccess;
import org.epics.ca.client.ChannelAccessFactory;
import org.epics.ca.client.ChannelProvider;
import org.epics.ca.client.ChannelPut;
import org.epics.ca.client.ChannelPutRequester;
import org.epics.ca.client.ChannelRequester;
import org.epics.ca.client.CreateRequestFactory;
import org.epics.pvData.misc.BitSet;
import org.epics.pvData.pv.Field;
import org.epics.pvData.pv.MessageType;
import org.epics.pvData.pv.PVDataCreate;
import org.epics.pvData.pv.PVDouble;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.Status;

/**
 * ChannelGet example
 * @author mrk
 */
public class ExampleChannelPutDoubleValue {

    /**
     * main.
     * @param  args is a sequence of flags and filenames.
     */
    public static void main(String[] args) throws CAException {
        org.epics.ca.ClientFactory.start();
        int len = args.length;
        if(len<1 || len>2 || (len==1 && args[0].equals("?"))) {
            System.out.println("Usage: channelName request");
            return;
        }
        String channelName = args[0];
        String request = null;
        if(len==2) {
            request = args[1];
        }
        
        Client client = new Client(channelName,request);
        client.waitUntilDone(10000);
        org.epics.ca.ClientFactory.stop();
        System.exit(0);
    }
    
    private static final String providerName = "pvAccess";
    private static final ChannelAccess channelAccess = ChannelAccessFactory.getChannelAccess();
    private static final PVDataCreate pvDataCreate = PVFactory.getPVDataCreate();
    
    private static class Client implements ChannelRequester, ChannelPutRequester {
        
        private final PVStructure pvRequest;
        private final ChannelProvider channelProvider;
        private final Channel channel;
        private boolean done = false;
        private ChannelPut channelPut = null;
        private PVStructure pvStructure = null;
        private BitSet bitSet = null;

        Client(String channelName,String request) {
            if(request==null) {
                pvRequest = pvDataCreate.createPVStructure(null, "example", new Field[0]);
            } else {
                pvRequest = CreateRequestFactory.createRequest(request, this);
            }
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
         * @see org.epics.ca.client.ChannelRequester#channelCreated(org.epics.pvData.pv.Status, org.epics.ca.client.Channel)
         */
        @Override
        public void channelCreated(Status status, Channel channel) {
            if(!status.isSuccess()) {
                message("channelCreated " + status.getMessage(),MessageType.error);
                done();
                return;
            }
        }
        /* (non-Javadoc)
         * @see org.epics.ca.client.ChannelRequester#channelStateChange(org.epics.ca.client.Channel, org.epics.ca.client.Channel.ConnectionState)
         */
        @Override
        public void channelStateChange(Channel c,ConnectionState connectionState) {
            if(connectionState==ConnectionState.CONNECTED) {
                channel.createChannelPut(this, pvRequest);
            } else {
                message(connectionState.name(),MessageType.info);
                done = true;
            }
        }
        /* (non-Javadoc)
		 * @see org.epics.ca.client.ChannelPutRequester#channelPutConnect(org.epics.pvData.pv.Status, org.epics.ca.client.ChannelPut, org.epics.pvData.pv.PVStructure, org.epics.pvData.misc.BitSet)
		 */
		@Override
		public void channelPutConnect(Status status, ChannelPut channelPut,
				PVStructure pvStructure, BitSet bitSet) {
            if(!status.isSuccess()) {
                message("channelPutConnect " + status.getMessage(),MessageType.error);
                done();
                return;
            }
            synchronized(this) {
                this.channelPut = channelPut;
                this.pvStructure = pvStructure;
                this.bitSet = bitSet;
            }
            
            PVDouble val = this.pvStructure.getDoubleField("value");
            channelPut.lock();
            try {
	            this.bitSet.set(val.getFieldOffset());
	            val.put(Math.random()*10);
            } finally {
            	channelPut.unlock();
            }
            this.channelPut.put(true);
		}



		/* (non-Javadoc)
		 * @see org.epics.ca.client.ChannelPutRequester#putDone(org.epics.pvData.pv.Status)
		 */
		@Override
		public void putDone(Status status) {
            if(!status.isSuccess()) {
                message("putDone " + status.getMessage(),MessageType.error);
                done();
                return;
            }
            done();
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
                System.err.println(messageType + " " + message);
            } else {
                System.out.println(message);
            }
        }

		@Override
		public void getDone(Status status) {
			// TODO Auto-generated method stub
		}
        
    }
}
