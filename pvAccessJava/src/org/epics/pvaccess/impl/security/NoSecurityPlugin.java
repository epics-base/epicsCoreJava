package org.epics.pvaccess.impl.security;

import java.net.InetSocketAddress;

import org.epics.pvaccess.plugins.SecurityPlugin;
import org.epics.pvdata.factory.StatusFactory;
import org.epics.pvdata.misc.BitSet;
import org.epics.pvdata.pv.PVArray;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.Status;

public class NoSecurityPlugin implements SecurityPlugin, SecurityPlugin.SecuritySession, SecurityPlugin.ChannelSecuritySession {

	private static Status statusOk = StatusFactory.getStatusCreate().getStatusOK();

	// TODO make it loggable (traces all the ops) - need to refactor the code to become statefull
	//private static Logger logger = Logger.getLogger(NoSecurityPlugin.class.getName());

	public static NoSecurityPlugin INSTANCE = new NoSecurityPlugin();

	protected NoSecurityPlugin()
	{
	}

	public String getId() {
		return "none";
	}

	public String getDescription() {
		return "No security plugin";
	}

	public boolean isValidFor(InetSocketAddress remoteAddress) {
		return true;
	}

	public PVField initializationData() {
		return null;
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
