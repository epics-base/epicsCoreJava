/*
 * Copyright (c) 2009 by Cosylab
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

package org.epics.pvaccess.plugins;

import java.net.InetSocketAddress;

import org.epics.pvdata.misc.BitSet;
import org.epics.pvdata.pv.PVArray;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.Status;

/**
 * Security plugin interface.
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 */
public interface SecurityPlugin {
	
	/** System variable name that holds a comma separated list of SecurityPlugin classes for the client. */
	public static final String SECURITY_PLUGINS_CLIENT_KEY = SecurityPlugin.class.getName() + ".client";
	/** System variable name that holds a comma separated list of SecurityPlugin classes for the server. */
	public static final String SECURITY_PLUGINS_SERVER_KEY = SecurityPlugin.class.getName() + ".server";

	/**
	 * Short, unique name for the plug-in, used to identify the plugin.
	 * @return the ID.
	 */
	public String getId();
	
	/**
	 * Description of the security plug-in.
	 * @return the description string.
	 */
	public String getDescription();
	
	/**
	 * Check whether the remote instance with given network address is 
	 * valid to use this security plug-in to authNZ.
	 * @param remoteAddress
	 * @return <code>true</code> if this security plugin can be used for remote instance.
	 */
	public boolean isValidFor(InetSocketAddress remoteAddress);
	
	/**
	 * 
	 * @param remoteAddress
	 * @return a new session.
	 * @throws SecurityException
	 */
	// authentication must be done immediately when connection is established (timeout 3seconds),
	// later on authentication process can be repeated
	// the server and the client can exchange (arbitrary number) of messages using SecurityPluginControl.sendMessage()
	// the process completion must be notified by calling AuthenticationControl.completed()
	public SecuritySession createSession(InetSocketAddress remoteAddress, SecurityPluginControl control, PVField data) throws SecurityException;
	
	public interface SecurityPluginControl
	{
		// can be called any time, for any reason
		public void sendSecurityPluginMessage(PVField data);
		
		// if Status.isSuccess() == false,
		// pvAccess will send status to the client and close the connection
		// can be called more then once (in case of re-authentication process)
		public void authenticationCompleted(Status status);
	}
	
	public interface SecuritySession
	{
		// optional (can be null) initialization data for the remote party
		// client to server
		public PVField initializationData();
		
		// get parent
		public SecurityPlugin getSecurityPlugin();
		
		// can be called any time, for any reason
		public void messageReceived(PVField data);

		/// closes this session
		public void close();

		// notification to the client on allowed requests (bitSet, a bit per request)
		public ChannelSecuritySession createChannelSession(String channelName) throws SecurityException;
	}

	// notify client only on demand, configurable via pvRequest
	// add the following method to ChannelRequest:
	// void credentialsChanged(std::vector<BitSet> credentials);
	
	
	// pvAccess message: channel client id, ioid (if invalid then it's for channel) and array of bitSets
	// or leave to the plugin?
	
	
	// when clients gets initial credentialsChanged call before create is called
	// and then on each change
	
	public interface ChannelSecuritySession
	{
		/// closes this session
		public void close();
		
		// for every authroizeCreate... a release() must be called
		public void release(int ioid);

		// bitSet w/ one bit
		public Status authorizeCreateChannelProcess(int ioid, PVStructure pvRequest);
		public Status authorizeProcess(int ioid);

		// bitSet w/ one bit (allowed, not allowed) and rest of the bit per field
		public Status authorizeCreateChannelGet(int ioid, PVStructure pvRequest);
		public Status authorizeGet(int ioid);

		// read: bitSet w/ one bit (allowed, not allowed) and rest of the bit per field
		// write: bitSet w/ one bit (allowed, not allowed) and rest of the bit per field
		public Status authorizeCreateChannelPut(int ioid, PVStructure pvRequest);
		public Status authorizePut(int ioid, PVStructure dataToPut, BitSet fieldsToPut);
		
		// read: bitSet w/ one bit (allowed, not allowed) and rest of the bit per field
		// write: bitSet w/ one bit (allowed, not allowed) and rest of the bit per field
		// process: bitSet w/ one bit (allowed, not allowed)
		public Status authorizeCreateChannelPutGet(int ioid, PVStructure pvRequest);
		public Status authorizePutGet(int ioid, PVStructure dataToPut, BitSet fieldsToPut);
		
		// bitSet w/ one bit
		public Status authorizeCreateChannelRPC(int ioid, PVStructure pvRequest);
		// one could authorize per operation basis
		public Status authorizeRPC(int ioid, PVStructure arguments);

		// read: bitSet w/ one bit (allowed, not allowed) and rest of the bit per field
		public Status authorizeCreateMonitor(int ioid, PVStructure pvRequest);
		public Status authorizeMonitor(int ioid);
		
		// read: bitSet w/ one bit (allowed, not allowed) and rest put/get/set length
		public Status authorizeCreateChannelArray(int ioid, PVStructure pvRequest);
		// use authorizeGet
		public Status authorizePut(int ioid, PVArray dataToPut);
		public Status authorizeSetLength(int ioid);
		
		
		// introspection authorization
		public Status authorizeGetField(int ioid, String subField);
	}	
	
	
}
