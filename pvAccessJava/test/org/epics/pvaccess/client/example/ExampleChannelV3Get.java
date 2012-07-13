/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvaccess.client.example;

import org.epics.pvaccess.CAException;
import org.epics.pvaccess.client.Channel;
import org.epics.pvaccess.client.Channel.ConnectionState;
import org.epics.pvaccess.client.ChannelAccess;
import org.epics.pvaccess.client.ChannelAccessFactory;
import org.epics.pvaccess.client.ChannelGet;
import org.epics.pvaccess.client.ChannelGetRequester;
import org.epics.pvaccess.client.ChannelProvider;
import org.epics.pvaccess.client.ChannelRequester;
import org.epics.pvaccess.client.CreateRequestFactory;
import org.epics.pvdata.misc.BitSet;
import org.epics.pvdata.pv.MessageType;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.Status;


/**
 * ChannelGet example
 * @author mrk
 */
public class ExampleChannelV3Get {

    /**
     * main.
     * @param  args is a sequence of flags and filenames.
     */
    public static void main(String[] args) throws CAException {
    	
    	org.epics.caV3.ClientFactory.start();
        
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
        client.waitUntilDone(1000000);
        //org.epics.caV3.ClientFactory.stop();
        System.exit(0);
    }
    
    private static final ChannelAccess channelAccess = ChannelAccessFactory.getChannelAccess();
    //private static final PVDataCreate pvDataCreate = PVFactory.getPVDataCreate();
    
    private static class Client implements ChannelRequester, ChannelGetRequester {
        
        private final PVStructure pvRequest;
        private final ChannelProvider channelProvider;
        private final Channel channel;
        private boolean done = false;
        private ChannelGet channelGet = null;
        private PVStructure pvStructure = null;
        private BitSet bitSet = null;

        Client(String channelName,String request) {
            //if(request==null) {
            //    pvRequest = pvDataCreate.createPVStructure(null, "example", new Field[0]);
            //} else {
                pvRequest = CreateRequestFactory.createRequest("field(value,alarm,timeStamp)", this);
            //}
            channelProvider = channelAccess.getProvider(org.epics.caV3.ClientFactory.PROVIDER_NAME);
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
                channelGet =channel.createChannelGet(this, pvRequest);
            } else {
                message(connectionState.name(),MessageType.info);
                done = true;
            }
        }
        /* (non-Javadoc)
         * @see org.epics.pvaccess.client.ChannelGetRequester#channelGetConnect(org.epics.pvdata.pv.Status, org.epics.pvaccess.client.ChannelGet, org.epics.pvdata.pv.PVStructure, org.epics.pvdata.misc.BitSet)
         */
        @Override
        public void channelGetConnect(Status status, ChannelGet channelGet,PVStructure pvStructure, BitSet bitSet) {
            if(!status.isSuccess()) {
                message("channelGetConnect " + status.getMessage(),MessageType.error);
                done();
                return;
            }
            synchronized(this) {
                this.channelGet = channelGet;
                this.pvStructure = pvStructure;
                this.bitSet = bitSet;
            }
            this.channelGet.get(false);
        }
        /* (non-Javadoc)
         * @see org.epics.pvaccess.client.ChannelGetRequester#getDone(org.epics.pvdata.pv.Status)
         */
        @Override
        public void getDone(Status status) {
            if(!status.isSuccess()) {
                message("getDone " + status.getMessage(),MessageType.error);
                done();
                return;
            }
            
            message("bitSet" + bitSet.toString() + pvStructure.toString(),MessageType.info);
            done();

            /*
             * cyclic array get test
            PVDoubleArray arr = (PVDoubleArray)pvStructure.getScalarArrayField("value", ScalarType.pvDouble);
            System.out.println("got array with n elements: " +arr.getLength());
            DoubleArrayData dad = new DoubleArrayData();
            arr.get(arr.getLength() - 10, 10, dad);
            System.out.println("lst values: " +dad.data[arr.getLength()-1]);
            
            channelGet.get(false);
            */
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
        
    }
}
