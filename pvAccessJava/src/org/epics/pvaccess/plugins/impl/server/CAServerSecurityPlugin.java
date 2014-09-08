package org.epics.pvaccess.plugins.impl.server;

import java.net.InetSocketAddress;

import org.epics.pvaccess.plugins.SecurityPlugin;
import org.epics.pvdata.factory.StatusFactory;
import org.epics.pvdata.misc.BitSet;
import org.epics.pvdata.pv.PVArray;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.Status;

public class CAServerSecurityPlugin implements SecurityPlugin, SecurityPlugin.SecuritySession, SecurityPlugin.ChannelSecuritySession {

	private static Status statusOk = StatusFactory.getStatusCreate().getStatusOK();

	public CAServerSecurityPlugin()
	{
	}
	
	@Override
	public String getId() {
		return "ca";
	}

	@Override
	public String getDescription() {
		return "CA server security plugin";
	}

	@Override
	public boolean isValidFor(InetSocketAddress remoteAddress) {
		return true;
	}

	@Override
	public PVField initializationData() {
		return null;
	}

	@Override
	public SecuritySession createSession(InetSocketAddress remoteAddress,
			SecurityPluginControl control, PVField data)
			throws SecurityException {
		
		if (data instanceof PVStructure)
		{
			PVStructure s = (PVStructure)data;
			PVString pvUser = s.getStringField("user");
			PVString pvHost = s.getStringField("host");
			if (pvUser == null)
				throw new SecurityException("client must provide a structure with 'user' string field.");
			if (pvHost == null)
				throw new SecurityException("client must provide a structure with 'host' string field.");
			
			// TODO implement the plugin, now the plugin allows all once one provides user and host
		}
		else
			throw new SecurityException("client must provide a structure with 'user' and 'host' string fields.");
		
		control.authenticationCompleted(StatusFactory.getStatusCreate().getStatusOK());
		return this;
	}

	@Override
	public SecurityPlugin getSecurityPlugin() {
		return this;
	}

	@Override
	public void messageReceived(PVField data) {
		// noop
	}

	@Override
	public void close() {
		// noop
	}

	@Override
	public ChannelSecuritySession createChannelSession(String channelName)
			throws SecurityException {
		return this;
	}

	@Override
	public Status authorizeCreateChannelProcess(int ioid, PVStructure pvRequest) {
		return statusOk;
	}

	@Override
	public Status authorizeProcess(int ioid) {
		return statusOk;
	}

	@Override
	public Status authorizeCreateChannelGet(int ioid, PVStructure pvRequest) {
		return statusOk;
	}

	@Override
	public Status authorizeGet(int ioid) {
		return statusOk;
	}

	@Override
	public Status authorizeCreateChannelPut(int ioid, PVStructure pvRequest) {
		return statusOk;
	}

	@Override
	public Status authorizePut(int ioid, PVStructure dataToPut,
			BitSet fieldsToPut) {
		return statusOk;
	}

	@Override
	public Status authorizeCreateChannelPutGet(int ioid, PVStructure pvRequest) {
		return statusOk;
	}

	@Override
	public Status authorizePutGet(int ioid, PVStructure dataToPut,
			BitSet fieldsToPut) {
		return statusOk;
	}

	@Override
	public Status authorizeCreateChannelRPC(int ioid, PVStructure pvRequest) {
		return statusOk;
	}

	@Override
	public Status authorizeRPC(int ioid, PVStructure arguments) {
		return statusOk;
	}

	@Override
	public Status authorizeCreateMonitor(int ioid, PVStructure pvRequest) {
		return statusOk;
	}

	@Override
	public Status authorizeMonitor(int ioid) {
		return statusOk;
	}

	@Override
	public Status authorizeCreateChannelArray(int ioid, PVStructure pvRequest) {
		return statusOk;
	}

	@Override
	public Status authorizeSetLength(int ioid) {
		return statusOk;
	}

	@Override
	public Status authorizePut(int ioid, PVArray dataToPut) {
		return statusOk;
	}

	@Override
	public Status authorizeGetField(int ioid, String subField) {
		return statusOk;
	}

	@Override
	public void release(int ioid) {
		// noop
	}

}
