/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.ca.client.example;

import java.util.Arrays;

import org.epics.ca.CAException;
import org.epics.ca.client.Channel;
import org.epics.ca.client.Channel.ConnectionState;
import org.epics.ca.client.ChannelAccess;
import org.epics.ca.client.ChannelAccessFactory;
import org.epics.ca.client.ChannelGet;
import org.epics.ca.client.ChannelGetRequester;
import org.epics.ca.client.ChannelProvider;
import org.epics.ca.client.ChannelRequester;
import org.epics.ca.client.CreateRequestFactory;
import org.epics.pvData.misc.BitSet;
import org.epics.pvData.pv.DoubleArrayData;
import org.epics.pvData.pv.MessageType;
import org.epics.pvData.pv.PVArray;
import org.epics.pvData.pv.PVDoubleArray;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.ScalarType;
import org.epics.pvData.pv.Status;


/**
 * ChannelGet example
 * @author mrk
 */
public class ExampleChannelGet {

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
        client.waitUntilDone(1000000);
        org.epics.ca.ClientFactory.stop();
        System.exit(0);
    }
    
    private static final String providerName = "pvAccess";
    private static final ChannelAccess channelAccess = ChannelAccessFactory.getChannelAccess();
    //private static final PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
    
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
                pvRequest = CreateRequestFactory.createRequest("field(value)", this);
            //}
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
                channelGet =channel.createChannelGet(this, pvRequest);
            } else {
                message(connectionState.name(),MessageType.info);
                done = true;
            }
        }
        /* (non-Javadoc)
         * @see org.epics.ca.client.ChannelGetRequester#channelGetConnect(org.epics.pvData.pv.Status, org.epics.ca.client.ChannelGet, org.epics.pvData.pv.PVStructure, org.epics.pvData.misc.BitSet)
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
         * @see org.epics.ca.client.ChannelGetRequester#getDone(org.epics.pvData.pv.Status)
         */
        @Override
        public void getDone(Status status) {
            if(!status.isSuccess()) {
                message("getDone " + status.getMessage(),MessageType.error);
                done();
                return;
            }
            //
            //message("bitSet" + bitSet.toString() + pvStructure.toString(),MessageType.info);
            
            PVDoubleArray arr = (PVDoubleArray)pvStructure.getScalarArrayField("value", ScalarType.pvDouble);
            System.out.println("got array with n elements: " +arr.getLength());
            DoubleArrayData dad = new DoubleArrayData();
            arr.get(arr.getLength() - 10, 10, dad);
            System.out.println("lst values: " +dad.data[arr.getLength()-1]);
            
            //done();
            channelGet.get(false);
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
        
    }
}
