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

	public String getId() {
		return "ca";
	}

	public String getDescription() {
		return "CA client security plugin";
	}

	public boolean isValidFor(InetSocketAddress remoteAddress) {
		return true;
	}

	public PVField initializationData() {
		return userAndHost;
	}

	public SecuritySession createSession(InetSocketAddress remoteAddress,
			SecurityPluginControl control, PVField data)
			throws SecurityException {
		control.authenticationCompleted(StatusFactory.getStatusCreate().getStatusOK());
		return this;
	}

	public SecurityPlugin getSecurityPlugin() {
		return this;
	}

	public void messageReceived(PVField data) {
		// noop
	}

	public void close() {
		// noop
	}

	public ChannelSecuritySession createChannelSession(String channelName)
			throws SecurityException {
		return this;
	}

	public Status authorizeCreateChannelProcess(int ioid, PVStructure pvRequest) {
		return statusOk;
	}

	public Status authorizeProcess(int ioid) {
		return statusOk;
	}

	public Status authorizeCreateChannelGet(int ioid, PVStructure pvRequest) {
		return statusOk;
	}

	public Status authorizeGet(int ioid) {
		return statusOk;
	}

	public Status authorizeCreateChannelPut(int ioid, PVStructure pvRequest) {
		return statusOk;
	}

	public Status authorizePut(int ioid, PVStructure dataToPut,
			BitSet fieldsToPut) {
		return statusOk;
	}

	public Status authorizeCreateChannelPutGet(int ioid, PVStructure pvRequest) {
		return statusOk;
	}

	public Status authorizePutGet(int ioid, PVStructure dataToPut,
			BitSet fieldsToPut) {
		return statusOk;
	}

	public Status authorizeCreateChannelRPC(int ioid, PVStructure pvRequest) {
		return statusOk;
	}

	public Status authorizeRPC(int ioid, PVStructure arguments) {
		return statusOk;
	}

	public Status authorizeCreateMonitor(int ioid, PVStructure pvRequest) {
		return statusOk;
	}

	public Status authorizeMonitor(int ioid) {
		return statusOk;
	}

	public Status authorizeCreateChannelArray(int ioid, PVStructure pvRequest) {
		return statusOk;
	}

	public Status authorizeSetLength(int ioid) {
		return statusOk;
	}

	public Status authorizePut(int ioid, PVArray dataToPut) {
		return statusOk;
	}

	public Status authorizeGetField(int ioid, String subField) {
		return statusOk;
	}

	public void release(int ioid) {
		// noop
	}

}
