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

package org.epics.pvaccess;

/**
 * PVA constants.
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public interface PVAConstants {

	/**
	 * PVA protocol magic.
	 */
	public static final byte PVA_MAGIC = (byte)0xCA;

	/**
	 * PVA protocol revision (implemented by this library).
	 */
	public static final byte PVA_PROTOCOL_REVISION = 1;

	/**
	 * PVA version signature used to report this implementation version in header.
	 */
	public static final byte PVA_VERSION = PVA_PROTOCOL_REVISION;

	/**
	 * Default PVA server port.
	 */
	public static final int PVA_SERVER_PORT = 5075;

	/**
	 * Default PVA beacon port.
	 */
	public static final int PVA_BROADCAST_PORT = 5076;


	/**
	 * PVA protocol message header size.
	 */
	public static final short PVA_MESSAGE_HEADER_SIZE = 8;

    /**
     * All messages must be aligned to 8-bytes (64-bit).
     * MUST be 1. Code does not handle well alignment in some situations (e.g. direct deserialize).
     * Alignment is not worth additional code complexity. 
     */
    public static final int PVA_ALIGNMENT = 1;

    /**
	 * UDP maximum send message size (for sending search requests).
	 * MAX_UDP: 1500 (max of ethernet and 802.{2,3} MTU) - 20/40(IPv4/IPv6) - 8(UDP) - some reserve (e.g. IPSEC)
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
	public static final short PVA_DEFAULT_PRIORITY = 0;
	
	/**
	 * Max channel name length.
	 */
	public static final int MAX_CHANNEL_NAME_LENGTH = 500;
	
    /**
     * Invalid IOID.
     */
    public static final int PVA_INVALID_IOID = 0;
    
    /**
     * Default PVA provider name.
     */
    public static final String PVA_DEFAULT_PROVIDER = "local";

    /**
     * "All-providers registered" providers name.
     */
    public static final String PVA_ALL_PROVIDERS = "<all>";
    
    /**
     * String value of the JVM property key to turn on debugging.
	 * (0 - none, 1 - debug, 2 - more debug, 3 - dump messages)
     */
    public static final String PVACCESS_DEBUG = "EPICS_PVA_DEBUG";
}
