/**
 * 
 */
package org.epics.pvaccess.client.pvds;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * @author msekoranja
 *
 */
public interface Protocol {

	
	/**
	 * Protocol ID.
	 * @author msekoranja
	 */
	static class ProtocolId {
		// octet[4]
		//public static final byte[] VALUE = { 70, 76, 44, 53 }; 	// pvDS
		//public static final byte[] RTPS_VALUE = { 52, 54, 50, 53 }; 	// RTPS

		public static final int VALUE = 0x70764453; 	// pvDS
	}
	
	/**
	 * Protocol version.
	 * @author msekoranja
	 */
	static class ProtocolVersion {
		// octet major, octet minor
		//public static final byte[] PROTOCOLVERSION_2_1 = { 2, 1 };
		
		public static final short PROTOCOLVERSION_2_1 = 0x0201;
	}

	static class VendorId {
		// octet[2]
		//private final byte[] value = new byte[2];
		//public static final byte[] VENDORID_UNKNOWN = { 0, 0 };

		public static final short VENDORID_UNKNOWN = 0x0000;
		
		public static final short PVDS_VENDORID = 0x01CA;		// ficional; not confirmed by OMG
	}

	/**
	 * 12-byte GUID prefix.
	 * GUID consists of 12-byte prefix and 4-byte EntityId (process-wide unique).
	 * @author msekoranja
	 */
	static class GUIDPrefix {
		// octet[12]
		private final byte[] value;
		
		public static final byte[] GUIDPREFIX_UNKNOWN_VALUE = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		
		private GUIDPrefix() {
			value = new byte[12];
			
			// put first 12-bytes of UUID into the prefix byte-array
			UUID uuid4 = UUID.randomUUID();
			ByteBuffer bb = ByteBuffer.wrap(value);
			bb.putLong(uuid4.getMostSignificantBits());
			bb.putInt((int)(uuid4.getLeastSignificantBits() >>> 32));
		}
		
		private GUIDPrefix(byte[] value) {
			this.value = value;
		}
		
		public static final GUIDPrefix GUIDPREFIX_UNKNOWN = new GUIDPrefix(GUIDPREFIX_UNKNOWN_VALUE);
		
		public static GUIDPrefix generateGUIDPrefix()
		{
			return new GUIDPrefix();
		}
		
	}
	
	public static final int PARTICIPANTID_MAX = 119;
	
	static class EntityId {
		// octet[3] entityKey, octet entityKind
		//private final byte[] value = new byte[4];
		//public static final byte[] ENTITYID_UNKNOWN = { 0, 0, 0, 0 };
		
		private final int value;
		
		
		// first two MSB
		private static byte ENTITYKIND_USERDEFINED_MASK = 0x00;
		private static byte ENTITYKIND_BUILDIN_MASK = (byte)0xc0;
		private static byte ENTITYKIND_VENDORSPECIFIC_MASK = 0x40;
		
		// last 6 LSB
		private static byte ENTITYKIND_UNKNOWN_MASK = 0x00;
		private static byte ENTITYKIND_PARTICIPANT_MASK = 0x01;	// always build-in
		private static byte ENTITYKIND_WRITER_MASK = 0x02;
		private static byte ENTITYKIND_WRITERKEY_MASK = 0x03;
		private static byte ENTITYKIND_READER_MASK = 0x04;
		private static byte ENTITYKIND_READERKEY_MASK = 0x07;

		private EntityId(int entityKey, byte entityKind)
		{
			if (entityKey < 0 || entityKey > 0x00FFFFFF)
				throw new IllegalArgumentException("entityKey < 0 || entityKey > 0x00FFFFFF");
			
			value = entityKey << 8 | entityKind;
		}
		
		public static final EntityId ENTITYID_UNKNOWN = new EntityId(0, (byte)0);
		
		public static EntityId generateParticipantEntityId(int participantId)
		{
			if (participantId > PARTICIPANTID_MAX)
				throw new IllegalArgumentException("participantId > PARTICIPANTID_MAX");
			
			return new EntityId(participantId,
					(byte)(ENTITYKIND_BUILDIN_MASK | ENTITYKIND_PARTICIPANT_MASK));
		}
	}
	
	static class GUID
	{
		private final GUIDPrefix prefix;
		private final EntityId entityId;

		/**
		 * @param prefix
		 * @param entityId
		 */
		public GUID(GUIDPrefix prefix, EntityId entityId) {
			this.prefix = prefix;
			this.entityId = entityId;
		}
		
	}
	
	// max UDP payload 65507 bytes for IPv4 and 65487 bytes for IPv6
	// some OS limit to 8k?!
	// MTU UDP 1440 (w/ IPSEC)
	
	public static final long HEADER_NO_GUID =
		(long)ProtocolId.VALUE << 32 |
		ProtocolVersion.PROTOCOLVERSION_2_1 << 16 |
		VendorId.PVDS_VENDORID;
/*	
	static class Message {
		ProtocolId pi;
		ProtocolVersion pv;
		VendorId vi;
		GUIDPrefix gp;
	}

	static class SubmessageHeader {
		byte submessageId;	// Submessages with ID's 0x80 to 0xff (inclusive) are vendor-specific;
		byte flags;	// LSB = endian 	// E = SubmessageHeader.flags & 0x01 (0 = big, 1 = little)
		ushort octetsToNextHeader;
		
	}
	*/
	/*
The representation of this field is a CDR unsigned short (ushort).
In case octetsToNextHeader > 0, it is the number of octets from the first octet of the contents of the Submessage until the first octet of the header of the next Submessage (in case the Submessage is not the last Submessage in the Message) OR it is the number of octets remaining in the Message (in case the Submessage is the last Submessage in the Message). An interpreter of the Message can distinguish these two cases as it knows the total length of the Message.
In case octetsToNextHeader==0 and the kind of Submessage is NOT PAD or INFO_TS, the Submessage is the last Submessage in the Message and extends up to the end of the Message. This makes it possible to send Submessages larger than 64k (the size that can be stored in the octetsToNextHeader field), provided they are the last Submessage in the Message.
In case the octetsToNextHeader==0 and the kind of Submessage is PAD or INFO_TS, the next Submessage header starts immediately after the current Submessage header OR the PAD or INFO_TS is the last Submessage in the Message.
*/
	
	

}