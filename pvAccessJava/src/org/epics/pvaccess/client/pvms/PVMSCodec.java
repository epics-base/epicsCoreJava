package org.epics.pvaccess.client.pvms;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

import org.epics.pvdata.misc.SerializeHelper;

public class PVMSCodec {

	private final AtomicInteger packetSequenceNumber = new AtomicInteger(0);
	private final AtomicInteger messageSequenceNumber = new AtomicInteger(0);
	
	private final long timeStamp = System.currentTimeMillis();

    /**
     * Atomically increments packet (31-bit) sequence number.
     * @return the previous value
     */
    public final int incrementPacketSeqNum() {
        for (;;) {
            int current = packetSequenceNumber.get();
            int next = (current + 1) & 0x7FFFFFFF;
            if (packetSequenceNumber.compareAndSet(current, next))
                return current;
        }
    }
	
    /**
     * Atomically increments message (29-bit) sequence number.
     * @return the previous value
     */
    public final int incrementMessageSeqNum() {
        for (;;) {
            int current = messageSequenceNumber.get();
            int next = (current + 1) & 0x1FFFFFFF;
            if (messageSequenceNumber.compareAndSet(current, next))
                return current;
        }
    }

    public static enum PacketPosition
    {
    	FIRST(0x80000000), LAST(0x40000000), SOLO(0xC0000000), MIDDLE(0x00000000);
    	
    	private final int maskValue;
    	
    	private PacketPosition(int maskValue)
    	{
    		this.maskValue = maskValue;
    	}

		public final int getMaskValue() {
			return maskValue;
		}
    }
    
/*
UDT has two kinds of packets: the data packets and the control 
packets. They are distinguished by the 1st bit (flag bit) of the 
packet header. 

The data packet header structure is as following. 

0                   1                   2                   3 
0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ 
|0|                     Packet Sequence Number                  | 
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ 
|FF |O|                     Message Number                      | 
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ 
|                          Time Stamp                           | 
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ 
|                    Destination Socket ID                      | 
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ 
|                                                               | 
~                            Payload                            ~ 
|                                                               | 
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ 

The data packet header starts with 0. Packet sequence number uses the 
following 31 bits after the flag bit. UDT uses packet based 
sequencing, i.e., the sequence number is increased by 1 for each sent 
data packet in the order of packet sending. Sequence number is 
wrapped after it is increased to the maximum number (2^31 - 1). 

The next 32-bit field in the header is for the messaging. The first 
two bits "FF" flags the position of the packet is a message. "10" is 
the first packet, "01" is the last one, "11" is the only packet, and 
"00" is any packets in the middle. The third bit "O" means if the 
message should be delivered in order (1) or not (0). A message to be 
delivered in order requires that all previous messages must be either 
delivered or dropped. The rest 29 bits is the message number, similar 
to packet sequence number (but independent). A UDT message may 
contain multiple UDT packets. 

Following are the 32-bit time stamp when the packet is sent and the 
destination socket ID. The time stamp is a relative value starting 
from the time when the connection is set up. The time stamp 
information is not required by UDT or its native control algorithm. 
It is included only in case that a user defined control algorithm may 
require the information. 

The Destination ID is used for UDP multiplexer. Multiple UDT socket 
can be bound on the same UDP port and this UDT socket ID is used to 
differentiate the UDT connections.

PVMS sends 64-bit time stamp (sender startup time, in ms, since Unix epoch) instead of 
(Time Stamp, Destination Socket ID). This time stamp must be globally unique for
given socket address (IP, port number). This implies that Destination Socket ID
matched UDT structure definition. Note that UDT Time Stamp field
is not required by the UDT.   
*/
    
	protected void udtDataHeader(ByteBuffer buffer, int messageSeqNumber, PacketPosition packetPosition)
	{
		int seqNum = incrementPacketSeqNum();
		buffer.putInt(seqNum);
		
		// 10 - first packet of a message (0x80000000)
		// 01 - last packet of a message (0x40000000)
		// 11 - solo message packet (0xC0000000)
		// 00 - packet in the middle of a message (0xC0000000)
		final int positionFlags = packetPosition.getMaskValue();
		
		// 0 (in order delivery not required) / 1 (in order delivery required) of messages
		final int inOrderDeliveryRequiredFlag = 0x20000000;
		
		buffer.putInt(positionFlags | inOrderDeliveryRequiredFlag | messageSeqNumber);
		
		// timestamp in ms since 1.1.1970 when "connection" was established
		// must be unique for the one socket (address, port)
		// to be used to uniquely identify source (also for UDP multiplexing)
		buffer.putLong(timeStamp);
	}
	
/*
If the flag bit of a UDT packet is 1, then it is a control packet and 
parsed according to the following structure. 

0                   1                   2                   3 
0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ 
|1|             Type            |            Reserved           | 
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ 
|                        Additional Info                        | 
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ 
|                            Time Stamp                         | 
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ 
|                    Destination Socket ID                      | 
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ 
|                                                               | 
~                 Control Information Field                     ~ 
|                                                               | 
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ 

There are 8 types of control packets in UDT and the type information 
is put in bit field 1 - 15 of the header. The contents of the 
following fields depend on the packet type. The first 128 bits must 
exist in the packet header, whereas there may be an empty control 
information field, depending on the packet type. 

UDT control types used by PVMS:
- 1: Keep-alive (expiration time in seconds (0 means never), no control info)
- 5: Shutdown (undefined additional info, no control info)
- 0x7FFF, 0xCACA: Subscribe (expiration time in seconds (0 means never), topic ID string as control info)
*/
	private void udtControlHeader(ByteBuffer buffer, int type, int reserved, int additionalInfo)
	{
		int controlTypeField =  0x80000000 | (type << 16) | reserved;
		buffer.putInt(controlTypeField);

		// additional info
		buffer.putInt(additionalInfo);	

		// timestamp in ms since 1.1.1970 when "connection" was established
		// must be unique for the one socket (address, port)
		// to be used to uniquely identify source (also for UDP multiplexing)
		buffer.putLong(timeStamp);
	}

	protected void pmsShutdownControlMessage(ByteBuffer buffer)
	{
		udtControlHeader(buffer, 5, 0, 0);
	}

	protected void pmsKeepAliveControlMessage(ByteBuffer buffer, int expirationTimeSec)
	{
		udtControlHeader(buffer, 1, 0, expirationTimeSec);
	}
	
	protected void pmsSubscribeControlMessage(ByteBuffer buffer, int expirationTimeSec, String topicID)
	{
		udtControlHeader(buffer, 0x7FFF, 0xCACA, expirationTimeSec);
		SerializeHelper.serializeString(topicID, buffer);
	}

	protected void handleControlPacket(ByteBuffer buffer,
			InetSocketAddress socketAddress, int typeReserved, int additionalInfo, long id)
	{
		int type = (typeReserved & 0x7FFFFFFF) >> 16; 
		switch (type)
		{
		case 1: handleKeepAlive(socketAddress, id, additionalInfo); break;
		case 5: handleShutdown(socketAddress, id); break;
		case 0x7FFF:
			if (typeReserved == 0x7FFFCACA)
			{
				String topicId = SerializeHelper.deserializeString(buffer);
				handleSubscribe(socketAddress, id, additionalInfo, topicId);
				break;
			}
		default:
			throw new IllegalStateException("unsupported control message: 0x" + Integer.toHexString(typeReserved));
		}
	}

	protected void handleKeepAlive(InetSocketAddress socketAddress, long id, int expirationTimeSec)
	{
		// noop
	}

	protected void handleShutdown(InetSocketAddress socketAddress, long id)
	{
		// noop
	}

	protected void handleSubscribe(InetSocketAddress socketAddress, long id, int expirationTimeSec, String topicId)
	{
		// noop
	}
}
