package org.epics.pvaccess.plugins.impl.client;

import java.net.InetSocketAddress;

import org.epics.pvaccess.plugins.SecurityPlugin;
import org.epics.pvaccess.util.InetAddressUtil;
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.factory.StatusFactory;
import org.epics.pvdata.misc.BitSet;
import org.epics.pvdata.pv.PVArray;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Status;
import org.epics.pvdata.pv.Structure;

public class CAClientSecurityPlugin implements SecurityPlugin, SecurityPlugin.SecuritySession, SecurityPlugin.ChannelSecuritySession {

	private static Status statusOk = StatusFactory.getStatusCreate().getStatusOK();

	private static PVStructure userAndHost;
	
	static {

		String userName = System.getProperty("user.name", "nobody");
		String hostName = InetAddressUtil.getHostName();
		
		Structure userAndHostStructure =
				FieldFactory.getFieldCreate().createFieldBuilder().
				add("user", ScalarType.pvString).
				add("host", ScalarType.pvString).
				createStructure();
		
		userAndHost = PVDataFactory.getPVDataCreate().
				createPVStructure(userAndHostStructure);
		
		userAndHost.getStringField("user").put(userName);
		userAndHost.getStringField("host").put(hostName);
	}
	
	public CAClientSecurityPlugin()
	{
	}
	
	@Override
	public String getId() {
		return "ca";
	}

	@Override
	public String getDescription() {
		return "CA client security plugin";
	}

	@Override
	public boolean isValidFor(InetSocketAddress remoteAddress) {
		return true;
	}

	@Override
	public PVField initializationData() {
		return userAndHost;
	}

	@Override
	public SecuritySession createSession(InetSocketAddress remoteAddress,
			SecurityPluginControl control, PVField data)
			throws SecurityException {
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
