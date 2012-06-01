/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvaccess.client.example;

import org.epics.pvaccess.CAException;
import org.epics.pvaccess.PVFactory;
import org.epics.pvaccess.client.Channel;
import org.epics.pvaccess.client.ChannelAccess;
import org.epics.pvaccess.client.ChannelAccessFactory;
import org.epics.pvaccess.client.ChannelProvider;
import org.epics.pvaccess.client.ChannelPut;
import org.epics.pvaccess.client.ChannelPutRequester;
import org.epics.pvaccess.client.ChannelRequester;
import org.epics.pvaccess.client.CreateRequestFactory;
import org.epics.pvaccess.client.Channel.ConnectionState;
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.misc.BitSet;
import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.MessageType;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVDouble;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.Status;

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
        org.epics.pvaccess.ClientFactory.start();
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
        org.epics.pvaccess.ClientFactory.stop();
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
                pvRequest = pvDataCreate.createPVStructure(FieldFactory.getFieldCreate().createStructure(new String[0], new Field[0]));
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
         * @see org.epics.pvaccess.client.ChannelRequester#channelCreated(org.epics.pvdata.pv.Status, org.epics.pvaccess.client.Channel)
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
         * @see org.epics.pvaccess.client.ChannelRequester#channelStateChange(org.epics.pvaccess.client.Channel, org.epics.pvaccess.client.Channel.ConnectionState)
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
		 * @see org.epics.pvaccess.client.ChannelPutRequester#channelPutConnect(org.epics.pvdata.pv.Status, org.epics.pvaccess.client.ChannelPut, org.epics.pvdata.pv.PVStructure, org.epics.pvdata.misc.BitSet)
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
		 * @see org.epics.pvaccess.client.ChannelPutRequester#putDone(org.epics.pvdata.pv.Status)
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
         * @see org.epics.pvdata.pv.Requester#getRequesterName()
         */
        @Override
        public String getRequesterName() {
            return "example";
        }
        /* (non-Javadoc)
         * @see org.epics.pvdata.pv.Requester#message(java.lang.String, org.epics.pvdata.pv.MessageType)
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
