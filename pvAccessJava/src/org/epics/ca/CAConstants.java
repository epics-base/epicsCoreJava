/*
 * Copyright (c) 2004 by Cosylab
 *
 * The full license specifying the redistribution, modification, usage and other
 * rights and obligations is included with the distribution of this project in
 * the file "LICENSE-CAJ". If the license is not included visit Cosylab web site,
 * <http://www.cosylab.com>.
 *
 * THIS SOFTWARE IS PROVIDED AS-IS WITHOUT WARRANTY OF ANY KIND, NOT EVEN THE
 * IMPLIED WARRANTY OF MERCHANTABILITY. THE AUTHOR OF THIS SOFTWARE, ASSUMES
 * _NO_ RESPONSIBILITY FOR ANY CONSEQUENCE RESULTING FROM THE USE, MODIFICATION,
 * OR REDISTRIBUTION OF THIS SOFTWARE.
 */

package org.epics.ca;

/**
 * CA constants.
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public interface CAConstants {

	/**
	 * CA protocol magic.
	 */
	public static final byte CA_MAGIC = (byte)0xCA;

	/**
	 * CA protocol major revision (implemented by this library).
	 */
	public static final byte CA_MAJOR_PROTOCOL_REVISION = 5;
	
	/**
	 * CA protocol minor revision (implemented by this library).
	 */
	public static final byte CA_MINOR_PROTOCOL_REVISION = 0;

	/**
	 * Unknown CA protocol minor revision.
	 */
	public static final byte CA_UNKNOWN_MINOR_PROTOCOL_REVISION = 0;

	/**
	 * CA version signature (e.g. 0x50).
	 */
	public static final byte CA_VERSION = 
	  CAConstants.CA_MAJOR_PROTOCOL_REVISION << 4 |
	  CAConstants.CA_MINOR_PROTOCOL_REVISION;

	/**
	 * CA protocol port base.
	 */
	public static final int CA_PORT_BASE = 5056;

	/**
	 * Default CA server port.
	 */
	public static final int CA_SERVER_PORT = CA_PORT_BASE + 2 * CA_MAJOR_PROTOCOL_REVISION;

	/**
	 * Default CA beacon port.
	 */
	public static final int CA_BROADCAST_PORT = CA_SERVER_PORT + 1;

	/**
	 * CA protocol message header size.
	 */
	public static final short CA_MESSAGE_HEADER_SIZE = 8;

	/**
	 * UDP maximum send message size.
	 * MAX_UDP: 1500 (max of ethernet and 802.{2,3} MTU) - 20/40(IPv4/IPv6) - 8(UDP) - some reserve 
	 * (the MTU of Ethernet is currently independent of its speed variant)
	 */
	public static final int MAX_UDP_SEND = 1440;

	/**
	 * UDP maximum receive message size.
	 */
	public static final int MAX_UDP_RECV = 0xFFFF + 16;

	/**
	 * TCP maximum receive message size.
	 */
	public static final int MAX_TCP_RECV = 1024 * 16;

	/**
	 * Maximum number of search requests in one search message.
	 */
	public static final int MAX_SEARCH_BATCH_COUNT = 0xFFFF;
		
	/**
	 * Default priority (corresponds to POSIX SCHED_OTHER)
	 */
	public static final short CA_DEFAULT_PRIORITY = 0;
	
	/**
	 * Unreasonable channel name length.
	 */
	public static final int UNREASONABLE_CHANNEL_NAME_LENGTH = 500;
	
	/**
	 * Invalid data type.
	 */
	public static final short CA_INVALID_DATA_TYPE = (short)0xFFFF;

    /**
     * Invalid IOID.
     */
    public static final int CA_INVALID_IOID = 0;
    
    /**
     * All messages must be aligned to 8-bytes (64-bit).
     */
    public static final int CA_ALIGNMENT = 8;

    /**
     * Default CA provider name.
     */
    public static final String CA_DEFAULT_PROVIDER = "local";

    /**
     * String value of the JVM property key to turn on debugging. 
     */
    public static final String PVACCESS_DEBUG = "PVACCESS_DEBUG";
}
