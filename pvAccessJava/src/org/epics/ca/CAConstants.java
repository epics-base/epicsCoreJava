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
	 * CA protocol minor revision (implemented by this library).
	 */
	public static final byte CA_PROTOCOL_REVISION = 0;

	/**
	 * CA version signature used to report this implementation version in header.
	 */
	public static final byte CA_VERSION = CA_PROTOCOL_REVISION;

	/**
	 * Default CA server port.
	 */
	public static final int CA_SERVER_PORT = 5075;

	/**
	 * Default CA beacon port.
	 */
	public static final int CA_BROADCAST_PORT = 5076;

	/**
	 * CA protocol message header size.
	 */
	public static final short CA_MESSAGE_HEADER_SIZE = 8;

    /**
     * All messages must be aligned to 8-bytes (64-bit).
     */
    public static final int CA_ALIGNMENT = 1;	// TODO

    /**
	 * UDP maximum send message size (for sending search requests).
	 * MAX_UDP: 1500 (max of ethernet and 802.{2,3} MTU) - 20/40(IPv4/IPv6) - 8(UDP) - some reserve 
	 * (the MTU of Ethernet is currently independent of its speed variant)
	 */
	public static final int MAX_UDP_UNFRAGMENTED_SEND = 1440;

    /**
	 * UDP maximum receive message size.
	 * MAX_UDP: 65535 (max UDP packet size) - 20/40(IPv4/IPv6) - 8(UDP) 
	 */
	public static final int MAX_UDP_PACKET = 65487;

	/**
	 * TCP maximum receive message size.
	 */
	public static final int MAX_TCP_RECV = 1024 * 16;

	/**
	 * Maximum number of search requests in one search message.
	 */
	public static final int MAX_SEARCH_BATCH_COUNT = Short.MAX_VALUE;  // 32767
		
	/**
	 * Default priority.
	 */
	public static final short CA_DEFAULT_PRIORITY = 0;
	
	/**
	 * Max channel name length.
	 */
	public static final int MAX_CHANNEL_NAME_LENGTH = 500;
	
	/**
	 * Invalid data type.
	 */
	public static final short CA_INVALID_DATA_TYPE = (short)0xFFFF;

    /**
     * Invalid IOID.
     */
    public static final int CA_INVALID_IOID = 0;
    
    /**
     * Default CA provider name.
     */
    public static final String CA_DEFAULT_PROVIDER = "local";

    /**
     * String value of the JVM property key to turn on debugging. 
     */
    public static final String PVACCESS_DEBUG = "PVACCESS_DEBUG";
}
