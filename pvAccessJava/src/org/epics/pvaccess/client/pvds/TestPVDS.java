package org.epics.pvaccess.client.pvds;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.logging.Logger;

import org.epics.pvaccess.client.pvds.Protocol.GUIDPrefix;
import org.epics.pvaccess.client.pvds.Protocol.Locator;
import org.epics.pvaccess.client.pvds.Protocol.ProtocolId;
import org.epics.pvaccess.client.pvds.Protocol.ProtocolVersion;
import org.epics.pvaccess.client.pvds.Protocol.VendorId;
import org.epics.pvaccess.client.pvds.util.BloomFilter;
import org.epics.pvaccess.client.pvds.util.StringToByteArraySerializator;

import com.cosylab.epics.caj.util.HexDump;

public class TestPVDS {

	private static final int RTPS_HEADER_SIZE = 20;
	private static final int RTPS_SUBMESSAGE_ALIGNMENT = 4;
	private static final int RTPS_SUBMESSAGE_HEADER_SIZE = 4;
	private static final int RTPS_SUBMESSAGE_SIZE_MIN = 8;

    public static final int RTPS_PAD            = 0x01;
    public static final int RTPS_ACK            = 0x06; // RTPS_ACKNACK
    public static final int RTPS_HEARTBEAT      = 0x07;
    public static final int RTPS_GAP            = 0x08;
    public static final int RTPS_INFO_TS        = 0x09;
    public static final int RTPS_INFO_SRC       = 0x0c;
    public static final int RTPS_INFO_REPLY_IP4 = 0x0d;
    public static final int RTPS_INFO_DST       = 0x0e;
    public static final int RTPS_INFO_REPLY     = 0x0f;
    public static final int RTPS_NACK_FRAG      = 0x12;
    public static final int RTPS_HEARTBEAT_FRAG = 0x13;
    public static final int RTPS_DATA 	  	    = 0x15;
    public static final int RTPS_DATA_FRAG_X    = 0x16; // RTPS_DATA_FRAG

	static class MessageReceiver 
	{
		short sourceVersion;
		short sourceVendorId;
		byte[] sourceGuidPrefix = new byte[12];
		short submessageId;
		byte submessageFlags;
		int submessageSize;
		//byte[] destGuidPrefix = new byte[12];
		//unicastReplyLocatorList;
		//multicastReplyLocatorList;
		//boolean haveTimestamp;
		//Time_t timestamp;
		
		public void reset() 
		{
			// it does not makes sence to reset
			// sourceVersion, sourceVendorId, sourceGuidPrefix
			// since they are reset by every message header 
		}
	}

	static class MessageReceiverStatistics {
	    int[] submessageType = new int[255];
	    int messageToSmall;
	    int nonRTPSMessage;
	    int versionMismatch;
	    int vendorMismatch;

	    int invalidSubmessageSize;
	    int submesssageAlignmentMismatch;
	    
	    int unknownSubmessage;
	    int validMessage;
	};
	
	static Logger log = Logger.getGlobal();

	// TODO move to proper place
	final static GUIDPrefix guidPrefix = GUIDPrefix.generateGUIDPrefix();
	
	static class RTPSMessageReceiver
	{
	    private final MessageReceiver receiver = new MessageReceiver();
	    private final MessageReceiverStatistics stats = new MessageReceiverStatistics();
	    
		public boolean processMessage(ByteBuffer buffer)
		{
			receiver.reset();
			
			if (buffer.remaining() < RTPS_HEADER_SIZE)
			{
				stats.messageToSmall++;
				return false;
			}
			
			// read header
			int protocolId = buffer.getInt();
			receiver.sourceVersion = buffer.getShort();
			receiver.sourceVendorId = buffer.getShort();
			buffer.get(receiver.sourceGuidPrefix);
	
			// check protocolId
			//if (protocolId != Protocol.ProtocolId.VALUE)
			if (protocolId != Protocol.ProtocolId.RTPS_VALUE)
			{
				stats.nonRTPSMessage++;
				return false;
			}
	
			// check version
			if (receiver.sourceVersion != ProtocolVersion.PROTOCOLVERSION_2_1)
			{
				stats.versionMismatch++;
				return false;
			}
			
			// check vendor
			if (receiver.sourceVendorId != VendorId.PVDS_VENDORID)
			{
				stats.vendorMismatch++;
				return false;
			}
			
			// process submessages
			int remaining;
			while ((remaining = buffer.remaining()) > 0)
			{
				if (remaining < RTPS_SUBMESSAGE_HEADER_SIZE)
				{
					stats.invalidSubmessageSize++;
					return false;
				}
					
				// check aligment
				if (buffer.position() % RTPS_SUBMESSAGE_ALIGNMENT != 0)
				{
					stats.submesssageAlignmentMismatch++;
					return false;
				}
				
				// read submessage header
				receiver.submessageId = (short)(buffer.get() & 0xFF);
				receiver.submessageFlags = buffer.get();
				
				// apply endianess
				boolean littleEndian = (receiver.submessageFlags & 0x01) == 1;
				buffer.order(littleEndian ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
				
				// read submessage size (octetsToNextHeader)
				receiver.submessageSize = buffer.getShort() & 0xFFFF;
	
		        // "jumbogram" condition: octetsToNextHeader == 0 for all except PAD and INFO_TS
		        if (receiver.submessageSize == 0 &&
		        	(receiver.submessageId != RTPS_INFO_TS &&
		        	 receiver.submessageId != RTPS_PAD))
		        {
		        	receiver.submessageSize = buffer.remaining();
		        }
		        else if (buffer.remaining() < receiver.submessageSize)
		        {
		        	stats.invalidSubmessageSize++;
		        	return false;
		        }
		        
		        int submessageStartPosition = buffer.position();
	
		        stats.submessageType[receiver.submessageId]++;
	
				switch (receiver.submessageId) {
	
				case RTPS_ACK:
				case RTPS_NACK_FRAG:
				case RTPS_HEARTBEAT:
				case RTPS_HEARTBEAT_FRAG:
				case RTPS_GAP:
				case RTPS_DATA_FRAG_X:
				case RTPS_DATA:
	
					// min submessage size check
					if (buffer.remaining() < RTPS_SUBMESSAGE_SIZE_MIN) {
						stats.invalidSubmessageSize++;
						return false;
					}
	
					boolean isDataOrDataFrag =
						      (receiver.submessageId == RTPS_DATA
							|| receiver.submessageId == RTPS_DATA_FRAG_X);
	
					break;
				/*
						     if (isDataOrDataFrag) {
						         receiver.dataExtraFlags = ntohs(*(MIGRtpsDataFlags *)data);
						         data += 2;
	
						         MIGInterpreterContext_deserializeUnsignedShort(
						             context, data, receiver.dataOctetsToInlineQos); 
						     }
	
						     // Get the contents of the submessage. 
						     receiver.readerObjectId = ntohl(*(MIGRtpsObjectId *)data);
						     data += 4;
						     receiver.writerObjectId = ntohl(*(MIGRtpsObjectId *)data);
						     data += 4;
						     receiver.data.pointer = data;
	
						     // reduce octetsToInlineQos by two object IDs 
						     if (isDataOrDataFrag) {
						         receiver.dataOctetsToInlineQos -= 8;
						         receiver.data.length -= 4;
						     }
	
						     // Reduce submessage length to what is supposedly left.
						     receiver.data.length -= 8;
	
						     if (!validDestination) {
						         break;
						     }
						     // Find the callback in the table and call it 
						     keyToMatch.guid.prefix.hostId = receiver.sourceHostId;
						     keyToMatch.guid.prefix.appId = receiver.sourceAppId;
						     keyToMatch.guid.prefix.instanceId = receiver.sourceInstanceId; 
	
						     if (receiver.submessageId == RTPS_ACK ||
						         receiver.submessageId == RTPS_ACK_BATCH || 
						         receiver.submessageId == RTPS_ACK_SESSION ||
						         receiver.submessageId == RTPS_NACK_FRAG ||
						         receiver.submessageId == RTPS_APP_ACK) {
						         keyToMatch.guid.objectId = receiver.readerObjectId;
						         keyToMatch.oid = receiver.writerObjectId;
						     } else {
						         keyToMatch.guid.objectId = receiver.writerObjectId;
						         keyToMatch.oid = receiver.readerObjectId;
						     }
	
						     if (keyToMatch.oid == RTPS_OBJECT_ID_UNKNOWN) {
						         // wild card; go to the right hash bin for the GUID
						         //   (OID not considered) and the first matching callback 
						         key = REDACursor_gotoKeyLargerOrEqual(receiver._cursor, NULL,
						                                               &keyToMatch) ?
						             (const struct MIGInterpreterListenerKey *)
						             REDACursor_getKey(receiver._cursor) : NULL; 
						         // If the cursor not valid, or cursor's key has different guid,
						         //   try to go to (HOST_ANY, APP_ANY, WRITER_OID) + (READER_OID)
						         
						         
						         if ((key == NULL) ||
						             !MIGRtpsGuid_equals(&key->guid, &keyToMatch.guid)) {  
						           
						             keyToMatch.guid.prefix = guidPrefixUnknown; 
	
						             if (!REDACursor_gotoKeyLargerOrEqual(receiver._cursor, NULL,
						                                                  &keyToMatch)) {
						                 break; 
						                 // no callback? TODO: increment counter 
						            }
						         }
						     } else if (!REDACursor_gotoKeyEqual(receiver._cursor, NULL,
						                                         &keyToMatch)) {
						         keyToMatch.guid.prefix = guidPrefixUnknown;
						         if (!REDACursor_gotoKeyEqual(receiver._cursor, NULL,
						                                      &keyToMatch)) {
						             break; // no callback? TODO: increment counter 
						         }
						     }
	
						     do {
						         struct MIGInterpreterListenerRO *ro;
						         key = (const struct MIGInterpreterListenerKey *)
						             REDACursor_getKey(receiver._cursor);
	
						         if (keyToMatch.oid == RTPS_OBJECT_ID_UNKNOWN) {
						             if (!MIGRtpsGuid_equals(&key->guid, &keyToMatch.guid)) {
						                 break;
						             }
						         } else if ((keyToMatch.oid != key->oid) ||
						             !MIGRtpsGuid_equals(&key->guid, &keyToMatch.guid)) {
						             break;
						         }
	
						         ro = (struct MIGInterpreterListenerRO *)
						             REDACursor_getReadOnlyArea(receiver._cursor);
	
						         if (MIGLog_workerStatEnabled() && workerStat != NULL) {
						             MIGInterpreter_getTime(me,
						                                    &workerStat->timeCalledBack);
						         }
						         // TODO: based on listener->priority,
						         //   receiver->disownEntryport() 
						         ro->_listener->onMatch(
						             ro->_listener, context, &msg->timestamp,
						             &ro->_storage, worker);
						     } while (REDACursor_gotoNext(receiver._cursor));
	
						     break;
						     // messages from here on do not involve user callback 
						 case RTPS_INFO_TS:
						     // Get the contents of the submessage. 
						     if ((receiver.submessageFlags & RTPS_INFO_TS_I_FLAG) != 0) {
						         receiver.haveTimestamp = RTI_FALSE;
						     } else { // Check that the given submessage length is valid. 
						         if (receiver.data.length < 8) {
						             MIGLog_warn(METHOD_NAME,
						                         &RTI_LOG_INSUFFICIENT_SPACE_FAILURE_dd,
						                         receiver.data.length, 8);
						             ++stat->submessageDataLengthFailedCount;
						             goto done;
						         }
						         MIGInterpreterContext_deserializeNtpTime(context,
						                                                  &receiver.timestamp,
						                                                  &data);
						         receiver.haveTimestamp = RTI_TRUE;
	
						         // Reduce submessage length to what is supposedly left. 
						         receiver.data.length -= 8;
						     }
						     break;
						 case RTPS_INFO_SRC:
						     #define RTPS_INFO_SRC_PAYLOAD_SIZE (20)
						     if (receiver.data.length < RTPS_INFO_SRC_PAYLOAD_SIZE) { 
						         MIGLog_warn(METHOD_NAME, &RTI_LOG_INSUFFICIENT_SPACE_FAILURE_dd,
						                     receiver.data.length, RTPS_INFO_SRC_PAYLOAD_SIZE);
						         ++stat->submessageDataLengthFailedCount;
						         goto done;
						     }
						     // TODO: deserialize IPv6 
						     MIGInterpreterContext_deserializeIpv4Address(
						         context, &receiver.unicastReplyIp, &data);
						     receiver.version = ntohs(*(MIGRtpsProtocolVersion *)data);data += 2;
						     receiver.vendorId = ntohs(*(MIGRtpsVendorId *)data);      data += 2;
						     receiver.sourceHostId = ntohl(*(MIGRtpsHostId *)data);    data += 4;
						     receiver.sourceAppId = ntohl(*(MIGRtpsAppId *)data);      data += 4;
						     receiver.sourceInstanceId = ntohl(*(MIGRtpsInstanceId *)data);  data += 4;
	
						     receiver.unicastReplyPort = RTPS_PORT_INVALID;
						     receiver.multicastReplyIp = INVALID_ADDRESS;
						     receiver.multicastReplyPort = RTPS_PORT_INVALID;
						     receiver.haveTimestamp = RTI_FALSE;
	
						     // Check that major version is not greater than implementation. 
						     if (MIGRtpsProtocolVersion_getMajor(&(receiver.version)) != 
						         RTPS_PROTOCOL_VERSION_MAJOR) {
						         MIGLog_warn(METHOD_NAME, &RTI_LOG_ANY_FAILURE_s, "version");
						         ++stat->versionMismatchCount;
						         goto done;
						     }
	
						     // Reduce submessage length to what is supposedly left. 
						     receiver.data.length -= RTPS_INFO_SRC_PAYLOAD_SIZE;
						     break;
						 case RTPS_INFO_REPLY:
						     #define RTPS_INFO_REPLY_LOCATOR_SIZE (32)
						     if (receiver.data.length < RTPS_INFO_REPLY_LOCATOR_SIZE) {
						         MIGLog_warn(METHOD_NAME, &RTI_LOG_INSUFFICIENT_SPACE_FAILURE_dd,
						                     receiver.data.length, RTPS_INFO_REPLY_LOCATOR_SIZE);
						         ++stat->submessageDataLengthFailedCount;
						         goto done;
						     }
	
						     // Get the contents of the submessage. 
						     savedReplyAddress = receiver.unicastReplyIp;
						     // RTPS.2.0: deserialize IPv6 
						     MIGInterpreterContext_deserializeIpv6Address(
						         context, &receiver.unicastReplyIp, &data);
						   
						     MIGInterpreterContext_deserializePort(
						         context, &receiver.unicastReplyPort, &data);
						     // Reduce submessage length to what is supposedly left. 
	
						     receiver.data.length -= RTPS_INFO_REPLY_LOCATOR_SIZE;
	
						     if ((receiver.submessageFlags & RTPS_INFO_REPLY_M_FLAG) != 0) {
						         if (receiver.data.length < RTPS_INFO_REPLY_LOCATOR_SIZE) {
						             MIGLog_warn(METHOD_NAME, &RTI_LOG_INSUFFICIENT_SPACE_FAILURE_dd,
						                         receiver.data.length, RTPS_INFO_REPLY_LOCATOR_SIZE);
						             ++stat->submessageDataLengthFailedCount;
						             goto done;
						         }
						         
						         MIGInterpreterContext_deserializeIpv6Address(
						             context, &receiver.multicastReplyIp, &data);
						   
						         MIGInterpreterContext_deserializePort(
						             context, &receiver.multicastReplyPort, &data);
	
						         // Reduce submessage length to what is supposedly left. 
						         receiver.data.length -= RTPS_INFO_REPLY_LOCATOR_SIZE;
						     } else {
						         receiver.multicastReplyIp = INVALID_ADDRESS;
						         receiver.multicastReplyPort = RTPS_PORT_INVALID;
						     }
	
						     if (NDDS_Transport_Address_is_equal(&receiver.unicastReplyIp,
						                                 &INVALID_ADDRESS)) {
						         receiver.unicastReplyIp = savedReplyAddress;
						     }
						     break;
						 case RTPS_INFO_REPLY_IP4: 
						     #define RTPS_INFO_REPLY_IP4_LOCATOR_SIZE (8)
						     if (receiver.data.length < RTPS_INFO_REPLY_IP4_LOCATOR_SIZE) {
						         MIGLog_warn(METHOD_NAME, &RTI_LOG_INSUFFICIENT_SPACE_FAILURE_dd,
						                     receiver.data.length, RTPS_INFO_REPLY_LOCATOR_SIZE);
						         ++stat->submessageDataLengthFailedCount;
						         goto done;
						     }
	
						     // Get the contents of the submessage. 
						     savedReplyAddress = receiver.unicastReplyIp;
						     // RTPS.2.0: deserialize IPv6 and IPv4 
						     MIGInterpreterContext_deserializeIpv4Address(
						         context, &receiver.unicastReplyIp, &data);
						   
						     MIGInterpreterContext_deserializePort(
						         context, &receiver.unicastReplyPort, &data);
						     // Reduce submessage length to what is supposedly left. 
	
						     receiver.data.length -= RTPS_INFO_REPLY_IP4_LOCATOR_SIZE;
	
						     if ((receiver.submessageFlags & RTPS_INFO_REPLY_M_FLAG) != 0) {
						         if (receiver.data.length < RTPS_INFO_REPLY_IP4_LOCATOR_SIZE) {
						             MIGLog_warn(METHOD_NAME, &RTI_LOG_INSUFFICIENT_SPACE_FAILURE_dd,
						                         receiver.data.length, RTPS_INFO_REPLY_LOCATOR_SIZE);
						             ++stat->submessageDataLengthFailedCount;
						             goto done;
						         }
						         
						         MIGInterpreterContext_deserializeIpv4Address(
						             context, &receiver.multicastReplyIp, &data);
						   
						         MIGInterpreterContext_deserializePort(
						             context, &receiver.multicastReplyPort, &data);
	
						         // Reduce submessage length to what is supposedly left. 
						         receiver.data.length -= RTPS_INFO_REPLY_IP4_LOCATOR_SIZE;
						     } else {
						         receiver.multicastReplyIp = INVALID_ADDRESS;
						         receiver.multicastReplyPort = RTPS_PORT_INVALID;
						     }
	
						     if (NDDS_Transport_Address_is_equal(&receiver.unicastReplyIp,
						                                 &INVALID_ADDRESS)) {
						         receiver.unicastReplyIp = savedReplyAddress;
						     }
						     break;
	
						 case RTPS_INFO_DST:
						     #define RTPS_INFO_DST_PAYLOAD_SIZE (12)
						     if (receiver.data.length < RTPS_INFO_DST_PAYLOAD_SIZE) {
						         MIGLog_warn(METHOD_NAME, &RTI_LOG_INSUFFICIENT_SPACE_FAILURE_dd,
						                     receiver.data.length, RTPS_INFO_DST_PAYLOAD_SIZE);
						         ++stat->submessageDataLengthFailedCount;
						         goto done;
						     }
	
						     // Get the contents of the submessage. 
						     receiver.destinationHostId = ntohl(*(MIGRtpsHostId*)data);
						     data += 4;
						     receiver.destinationAppId = ntohl(*(MIGRtpsAppId *)data);
						     data += 4;
						     receiver.destinationInstanceId = ntohl(*(MIGRtpsInstanceId *)data); 
						     data += 4;
						     
						     if (receiver.destinationHostId == RTPS_HOST_ID_UNKNOWN) {
						         receiver.destinationHostId = receiver._myHostId;
						     }
						     if (receiver.destinationAppId == RTPS_APP_ID_UNKNOWN) {
						         receiver.destinationAppId = receiver._myAppId;
						     }
						     if (receiver.destinationInstanceId == RTPS_INSTANCE_ID_UNKNOWN) { 
						         receiver.destinationInstanceId = receiver._myInstanceId;
						     }
						     validDestination =
						         (receiver.destinationHostId == receiver._myHostId &&
						          receiver.destinationAppId == receiver._myAppId &&
						          receiver.destinationInstanceId == receiver._myInstanceId) ?
						         RTI_TRUE : RTI_FALSE;
						     // Reduce submessage length to what is supposedly left. 
						     receiver.data.length -= RTPS_INFO_DST_PAYLOAD_SIZE;
						     break;
						     */
				default:
					stats.unknownSubmessage++;
					return false;
				}
	
		        // jump to next submessage position
		        buffer.position(submessageStartPosition + receiver.submessageSize);	// exception?
			}
			
			stats.validMessage++;
			
			return true;
		}

	}
	
	

	static class RTPSMessageTransmitter {

		// TODO to be configurable
		// also socket receiver, send buffers
	    final int DEFAULT_MAX_UDP_SEND = 8192;		// TODO better default
		
	    
	    final private ByteBuffer buffer = ByteBuffer.allocate(DEFAULT_MAX_UDP_SEND);
	    
	    public RTPSMessageTransmitter()
	    {
	    	initializeSendBuffer();
	    }
	    
	    private void initializeSendBuffer()
	    {
	    	buffer.clear();
	    	
		    // MessageHeader
	    	//buffer.putInt(ProtocolId.VALUE);
		    buffer.putInt(ProtocolId.RTPS_VALUE);
		    buffer.putShort(ProtocolVersion.PROTOCOLVERSION_2_1);
		    buffer.putShort(VendorId.PVDS_VENDORID);
		    buffer.put(guidPrefix.value);
	    }
	    
	    public void reset()
	    {
	    	// MessageHeader is static (fixed)
	    	buffer.position(RTPS_HEADER_SIZE);
	    	buffer.limit(buffer.capacity());
	    }
	    
	    // TODO remove
	    public ByteBuffer getBuffer()
	    {
	    	return buffer;
	    }
	    
	    public void addSubmessageHeader(byte submessageId, byte submessageFlags, int octetsToNextHeaderPos)
	    {
	    	buffer.put(submessageId);
		    // E = SubmessageHeader.flags & 0x01 (0 = big, 1 = little)
	    	buffer.put(submessageFlags);
	    	buffer.putShort((short)octetsToNextHeaderPos);
	    }
	    
	    // TODO tmp
	    public void addAnnounceSubmessage(int changeCount, Locator unicastEndpoint,
	    		int entitiesCount, BloomFilter<String> filter)
	    {
		    // Submessages with ID's 0x80 to 0xff (inclusive) are vendor-specific
	    	final byte VENDOR_SUBMESSAGEID_ANNOUNCE = (byte)0x80;

	    	addSubmessageHeader(VENDOR_SUBMESSAGEID_ANNOUNCE, (byte)0x00, 0x0000);
		    int octetsToNextHeaderPos = buffer.position() - 2;
		    
		    // unicast discovery locator
		    unicastEndpoint.serialize(buffer);
		    
		    // change count 
		    buffer.putInt(changeCount);

		    // service locators // TODO
		    // none for now
		    buffer.putInt(0);
		    
		    // # of discoverable entities (-1 not supported, or dynamic)
		    buffer.putInt(entitiesCount);
		    
		    if (entitiesCount > 0)
		    {
			    buffer.putInt(filter.k());
			    buffer.putInt(filter.m());
			    long[] bitArray = filter.bitSet().getBitArray();
			    buffer.asLongBuffer().put(bitArray);
			    buffer.position(buffer.position() + bitArray.length * 8);
		    }
		    
		    // set message size (generic code) for now
		    int octetsToNextHeader = buffer.position() - octetsToNextHeaderPos - 2;
		    buffer.putShort(octetsToNextHeaderPos, (short)(octetsToNextHeader & 0xFFFF));
	    	
	    }
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Throwable {
		
        //ExecutorService actorsThreadPool = Executors.newCachedThreadPool();
        //Actors actors = new Actors(actorsThreadPool);

        //final PollerImpl reactor = new PollerImpl();
        //reactor.start();
        //actorsThreadPool.execute(reactor);

        //ActorThread actorThread = actors.startActorThread();

        
		NetworkInterface nif = NetworkInterface.getByName("en1");

		// fallback to loopback
		if (nif == null)
		{
			Enumeration<NetworkInterface> nifs = NetworkInterface.getNetworkInterfaces();
			if (nifs == null)
				throw new IOException("no network interfaces available");

			for (NetworkInterface ni : Collections.list(nifs))
			{
				try
				{
					if (ni.isLoopback())
					{
						nif = ni;
						break;
					}
				} catch (Throwable th) {
					// implementation vaies from OS to OS and
					// can throw exceptions, this is a guard
				}
			}
		}
		
		if (nif == null)
			throw new IOException("no network interface available");
		
		System.out.println("NIF: " + nif.getDisplayName());

		final int domainId = 0;
		final int MAX_DOMAIN_ID = 232;
		final int MAX_PARTICIPANT_ID = 119;
		
		if (domainId > MAX_DOMAIN_ID)
			throw new IllegalArgumentException("domainId >= " + String.valueOf(MAX_DOMAIN_ID));
		
		final int PB = 7400;
		final int DG = 250;
		final int PG = 2;
		final int d0 = 0;
		final int d1 = 10;
		final int d2 = 1;
		final int d3 = 11;
		
		
        InetAddress discoveryMulticastGroup =
        	InetAddress.getByName("239.255." + String.valueOf(domainId) + ".1");
        int discoveryMulticastPort = PB + domainId * DG + d0;
        
        DatagramChannel discoveryMulticastChannel = DatagramChannel.open(StandardProtocolFamily.INET)
        	.setOption(StandardSocketOptions.SO_REUSEADDR, true)
        	.bind(new InetSocketAddress(discoveryMulticastPort));
        
        discoveryMulticastChannel.join(discoveryMulticastGroup, nif);
        discoveryMulticastChannel.configureBlocking(false);

        
        DatagramChannel discoveryUnicastChannel = DatagramChannel.open(StandardProtocolFamily.INET)
    		.setOption(StandardSocketOptions.SO_REUSEADDR, false);
        
        // TODO do we really need to specify unicast discovery port?
        int participantId;
        int unicastDiscoveryPort;
        for (participantId = 0; participantId < MAX_PARTICIPANT_ID; participantId++)
        {
        	unicastDiscoveryPort = PB + domainId * DG + participantId * PG + d1;
        	try {
        		discoveryUnicastChannel.bind(new InetSocketAddress(unicastDiscoveryPort));
        		break;
        	} catch (Throwable th) {
        		// noop
        	}
        }
        
        if (participantId > MAX_PARTICIPANT_ID)
        	throw new RuntimeException("maximum number of participants on this host reached");
        	
    
	    discoveryUnicastChannel.configureBlocking(false);
	    
	    System.out.println("pvDS started: domainId = " + domainId + ", participantId = " + participantId);
	    System.out.println("pvDS GUID prefix: " + Arrays.toString(guidPrefix.value));


	    RTPSMessageTransmitter transmitter = new RTPSMessageTransmitter();
	    
	    // unicast enpoint, can be "any local address" (0.0.0.0)
	    Locator unicastEndpoint =
	    	Locator.generateUDPLocator((InetSocketAddress)(discoveryUnicastChannel.getLocalAddress()));

	    transmitter.reset();
	    
	    // starts from 1
	    int changeCount = 1;
	    int entitiesCount = 1000;
	    BloomFilter<String> filter = new BloomFilter<String>(StringToByteArraySerializator.INSTANCE, 8, 1024);
	    for (int i = 0; i < entitiesCount; i++)
	    	filter.add(String.valueOf(i));
	    transmitter.addAnnounceSubmessage(changeCount, unicastEndpoint, entitiesCount, filter);
	    
	    ByteBuffer buffer = transmitter.getBuffer();
	    
	    HexDump.hexDump("announce", buffer.array(), 0, buffer.position());
	    
	    
	    /*
		InetAddress ia = InetAddress.getByName("172.16.240.255");
		DatagramSocket ms = new DatagramSocket();
		DatagramPacket dp = new DatagramPacket(buffer.array(), buffer.position(), ia, 5678);
		ms.send(dp);
	     */
	    
	    RTPSMessageReceiver rtpsReceiver = new RTPSMessageReceiver();
	    
	    buffer.flip();
	    boolean successfulyProcessed = rtpsReceiver.processMessage(buffer);
	    System.out.println("successfulyProcessed: " + successfulyProcessed);
	    
	    System.exit(1);
	}

}
