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

package org.epics.pvaccess.server.impl.remote.rpc;

import org.epics.pvaccess.PVAVersion;
import org.epics.pvaccess.PVFactory;
import org.epics.pvaccess.client.*;
import org.epics.pvaccess.impl.remote.Transport;
import org.epics.pvaccess.impl.remote.server.ChannelHostingTransport;
import org.epics.pvaccess.impl.remote.server.ServerChannel;
import org.epics.pvaccess.server.impl.remote.ServerContextImpl;
import org.epics.pvaccess.server.rpc.RPCRequestException;
import org.epics.pvaccess.server.rpc.RPCService;
import org.epics.pvaccess.util.InetAddressUtil;
import org.epics.pvdata.misc.Destroyable;
import org.epics.pvdata.monitor.MonitorRequester;
import org.epics.pvdata.pv.*;
import org.epics.pvdata.pv.Status.StatusType;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

// TODO make pluggable, replaceable
public class ServerRPCService implements RPCService {

	private static final int TIMEOUT_SEC = 3;

	private static final PVDataCreate pvDataCreate = PVFactory.getPVDataCreate();
	private static final FieldCreate fieldCreate = PVFactory.getFieldCreate();

	private static final Structure helpStructure =
		fieldCreate
			.createFieldBuilder()
				.setId("epics:nt/NTScalar:1.0")
				.add("value", ScalarType.pvString)
				.createStructure();

	private static final Structure channelListStructure =
		fieldCreate
			.createFieldBuilder()
				.setId("epics:nt/NTScalarArray:1.0")
				.addArray("value", ScalarType.pvString)
				.createStructure();

	private static final Structure infoStructure =
			fieldCreate
				.createFieldBuilder()
					.add("process", ScalarType.pvString)
					.add("startTime", ScalarType.pvString)
					.add("version", ScalarType.pvString)
					.add("implLang", ScalarType.pvString)
					.add("host", ScalarType.pvString)
					.add("os", ScalarType.pvString)
					.add("arch", ScalarType.pvString)
					.add("CPUs", ScalarType.pvInt)
					.createStructure();

	private static final Structure statusStructure =
			fieldCreate
				.createFieldBuilder()
					.add("connections", ScalarType.pvInt)
					.add("allocatedMemory", ScalarType.pvLong)
					.add("freeMemory", ScalarType.pvLong)
					.add("threads", ScalarType.pvInt)
					.add("deadlocks", ScalarType.pvInt)
					.add("averageSystemLoad", ScalarType.pvDouble)
					.createStructure();

	private static final Structure clientsStructure =
			fieldCreate
				.createFieldBuilder()
					.addNestedStructureArray("value")
						.add("remoteAddress", ScalarType.pvString)
						.add("securityPlugin", ScalarType.pvString)
							.addNestedStructureArray("channel")
								.add("name", ScalarType.pvString)
								.addNestedStructureArray("request")
									.add("name", ScalarType.pvString)
									.add("count", ScalarType.pvInt)
									.endNested()
								.endNested()
							.endNested()
						.createStructure();

	private static final Structure dumpStructure =
			fieldCreate
				.createFieldBuilder()
					.addArray("name", ScalarType.pvString)
					.add("value", fieldCreate.createVariantUnionArray())
					.createStructure();

	private static final String helpString =
		"pvAccess server RPC service.\n" +
		"arguments:\n" +
		"\tstring op\toperation to execute\n" +
		"\n" +
		"\toperations:\n" +
		"\t\tinfo\t\treturns some information about the server\n" +
		"\t\tstatus\t\treturns current server status\n" +
		"\t\tclients\t\treturns a list of connected clients and channels they use\n" +
		"\t\tchannels\treturns a list of 'static' channels the server can provide\n" +
		"\t\tdump\t\tdumps entire server status\n" +
//		"\t\t\t (no arguments)\n" +
		"\n";


	private static class ChannelListRequesterImpl implements ChannelListRequester {

		private final CountDownLatch doneCondition = new CountDownLatch(1);

		volatile Status status;
		volatile Set<String> channelNames;

		public void channelListResult(Status status, ChannelFind channelFind,
				Set<String> channelNames, boolean hasDynamic) {
			this.status = status;
			this.channelNames = channelNames;

			doneCondition.countDown();
		}

		public boolean waitForCompletion(int timeoutSec)
		{
			try {
				return doneCondition.await(timeoutSec, TimeUnit.SECONDS);
			}
			catch (InterruptedException ie) {
				return false;
			}
		}
	}

	protected final ServerContextImpl serverContext;
	protected final Map<String, Op> ops = new TreeMap<String, Op>();

	public ServerRPCService(ServerContextImpl serverContext) {
		this.serverContext = serverContext;

		// NOTE: do not forget to update help string
		addOp(new OpInfo());
		addOp(new OpChannels());
		addOp(new OpDump());
		addOp(new OpStatus());
		addOp(new OpClients());
	}

	private void addOp(Op op)
	{
		ops.put(op.getName(), op);
	}

	private interface Op
	{
		String getName();
		PVStructure execute() throws RPCRequestException;
	}

	public PVStructure request(PVStructure args) throws RPCRequestException {

		// NTURI support
		if (args.getStructure().getID().startsWith("epics:nt/NTURI:1."))
			args = args.getStructureField("query");

		// help support
		if (args.getSubField("help") != null)
		{
			PVStructure help =
				pvDataCreate.createPVStructure(helpStructure);
			help.getStringField("value").put(helpString);
			return help;
		}

		PVString opField = args.getStringField("op");
		if (opField == null)
			throw new RPCRequestException(StatusType.ERROR, "unspecified 'string op' field");

		Op op = ops.get(opField.get());
		if (op != null)
		{
			return op.execute();
		}
		else
			throw new RPCRequestException(StatusType.ERROR, "unsupported operation '" + opField + "'.");

	}

	private class OpInfo implements Op {

		public String getName() {
			return "info";
		}

		public PVStructure execute() throws RPCRequestException {
			PVStructure result =
					pvDataCreate.createPVStructure(infoStructure);

			String version =
			    + PVAVersion.VERSION_MAJOR
			    + "."
			    + PVAVersion.VERSION_MINOR
			    + "."
			    + PVAVersion.VERSION_MAINTENANCE;
			if (PVAVersion.VERSION_DEVELOPMENT)
				version += "-SNAPSHOT";

			result.getStringField("version").put(version);
			result.getStringField("implLang").put("java");
			result.getStringField("host").put(InetAddressUtil.getHostName());

			OperatingSystemMXBean osMBean = ManagementFactory.getOperatingSystemMXBean();
			result.getStringField("os").put(osMBean.getName() + " " + osMBean.getVersion());
			result.getStringField("arch").put(osMBean.getArch());
			result.getIntField("CPUs").put(osMBean.getAvailableProcessors());

			RuntimeMXBean runtimeMBean = ManagementFactory.getRuntimeMXBean();
			result.getStringField("process").put(runtimeMBean.getName());
			result.getStringField("startTime").put(timeFormatter.format(new Date(runtimeMBean.getStartTime())));

			return result;
		}

	}

	private class OpChannels implements Op {

		public String getName() {
			return "channels";
		}

		public PVStructure execute() throws RPCRequestException {

			ChannelListRequesterImpl listListener = new ChannelListRequesterImpl();
			serverContext.getChannelProviders().get(0).channelList(listListener);
			if (!listListener.waitForCompletion(TIMEOUT_SEC))
				throw new RPCRequestException(StatusType.ERROR, "failed to fetch channel list due to timeout");

			Status status = listListener.status;
			if (!status.isSuccess())
			{
				String errorMessage = "failed to fetch channel list: " + status.getMessage();
				if (status.getStackDump() != null)
					errorMessage += "\n" + status.getStackDump();
				throw new RPCRequestException(StatusType.ERROR, errorMessage);
			}

			Set<String> channelNames = listListener.channelNames;
			String[] names = channelNames.toArray(new String[0]);

			PVStructure result =
					pvDataCreate.createPVStructure(channelListStructure);
			result.getSubField(PVStringArray.class, "value").put(0, names.length, names, 0);

			return result;
		}

	}

	private class OpDump implements Op {

		public String getName() {
			return "dump";
		}

		public PVStructure execute() throws RPCRequestException {
			PVStructure result =
					pvDataCreate.createPVStructure(dumpStructure);

			int ix = 0;
			String[] name = new String[ops.size()];
			PVUnion[] data = new PVUnion[ops.size()];

			for (Op op : ops.values())
			{
				// skip itself
				if (op == this)
					continue;

				PVStructure rs = op.execute();
				PVUnion d = pvDataCreate.createPVUnion(
						fieldCreate.createVariantUnion()
					);
				d.set(rs);
				name[ix] = op.getName();
				data[ix++] = d;
			}

			result.getUnionArrayField("value").put(0, ix, data, 0);
			result.getSubField(PVStringArray.class, "name").put(0, ix, name, 0);

			return result;
		}
	}

	private static SimpleDateFormat timeFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

	private class OpStatus implements Op {

		public String getName() {
			return "status";
		}

		public PVStructure execute() throws RPCRequestException {
			PVStructure status =
					pvDataCreate.createPVStructure(statusStructure);

			status.getIntField("connections").put(serverContext.getTransportRegistry().numberOfActiveTransports());
			status.getLongField("allocatedMemory").put(Runtime.getRuntime().totalMemory());
			status.getLongField("freeMemory").put(Runtime.getRuntime().freeMemory());

			ThreadMXBean threadMBean = ManagementFactory.getThreadMXBean();
			status.getIntField("threads").put(threadMBean.getThreadCount());

			// Not supported in java 5 so force
//			final long[] deadlocks = threadMBean.isSynchronizerUsageSupported() ?
//		    	threadMBean.findDeadlockedThreads() :
//		    	threadMBean.findMonitorDeadlockedThreads();
			final long[] deadlocks = threadMBean.findMonitorDeadlockedThreads();
			status.getIntField("deadlocks").put((deadlocks != null) ? deadlocks.length : 0);

		    // Not load average not available for java 5 so return not-available result
//			OperatingSystemMXBean osMBean = ManagementFactory.getOperatingSystemMXBean();
//			status.getDoubleField("averageSystemLoad").put(osMBean.getSystemLoadAverage());
			status.getDoubleField("averageSystemLoad").put(-1);

			return status;
		}
	}

	private class OpClients implements Op {

		public String getName() {
			return "clients";
		}

		public PVStructure execute() throws RPCRequestException {
			PVStructure result =
					pvDataCreate.createPVStructure(clientsStructure);

			class RequestType
			{
				final Class<?> type;
				final String name;
				int count;

				public RequestType(Class<?> type, String name)
				{
					this.type = type;
					this.name = name;
					this.count = 0;
				}
			}
			ArrayList<RequestType> types = new ArrayList<RequestType>();
			types.add(new RequestType(ChannelGetRequester.class, "get"));
			types.add(new RequestType(MonitorRequester.class, "monitor"));
			types.add(new RequestType(ChannelRPCRequester.class, "rpc"));
			types.add(new RequestType(ChannelPutRequester.class, "put"));
			types.add(new RequestType(ChannelPutGetRequester.class, "put-get"));
			types.add(new RequestType(ChannelProcessRequester.class, "process"));
			types.add(new RequestType(ChannelArrayRequester.class, "array"));
			types.add(new RequestType(GetFieldRequester.class, "getField"));


			Transport[] transports = serverContext.getTransportRegistry().toArray();

			PVStructureArray pvValue = result.getStructureArrayField("value");
			PVStructure[] pvValueData = new PVStructure[transports.length];
			int ti = 0;

			for (Transport transport : transports)
			{
				PVStructure transportData = pvDataCreate.createPVStructure(
						pvValue.getStructureArray().getStructure()
						);
				// TODO possible null?
				transportData.getStringField("remoteAddress").put(transport.getRemoteAddress().toString());
				transportData.getStringField("securityPlugin").put(transport.getSecuritySession().getSecurityPlugin().getId());

				if (transport instanceof ChannelHostingTransport)
				{
					ChannelHostingTransport cht = (ChannelHostingTransport)transport;
					ServerChannel[] channels = cht.getChannels();

					PVStructureArray pvChannel = transportData.getStructureArrayField("channel");
					PVStructure[] pvChannelData = new PVStructure[channels.length];
					int ci = 0;

					for (ServerChannel channel : channels)
					{
						PVStructure channelData =
								pvDataCreate.createPVStructure(pvChannel.getStructureArray().getStructure());
						channelData.getStringField("name").put(channel.getChannel().getChannelName());

						// reset
						for (RequestType type : types)
							type.count = 0;

						Destroyable[] requests = channel.getRequests();
						for (Destroyable request : requests)
						{
							for (RequestType type : types)
								if (type.type.isInstance(request))
									type.count++;
						}

						PVStructureArray pvRequest = channelData.getStructureArrayField("request");
						PVStructure[] pvRequestsData = new PVStructure[types.size()];
						int i = 0;
						for (RequestType type : types)
						{
							PVStructure reqData =
									pvDataCreate.createPVStructure(pvRequest.getStructureArray().getStructure());
							reqData.getStringField("name").put(type.name);
							reqData.getIntField("count").put(type.count);
							pvRequestsData[i++] = reqData;
						}
						pvRequest.put(0,  pvRequestsData.length, pvRequestsData, 0);

						pvChannelData[ci++] = channelData;
					}

					pvChannel.put(0,  pvChannelData.length, pvChannelData, 0);
				}

				pvValueData[ti++] = transportData;
			}

			pvValue.put(0,  pvValueData.length, pvValueData, 0);

			return result;
		}
	}

}
