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

import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.epics.pvaccess.PVFactory;
import org.epics.pvaccess.client.ChannelFind;
import org.epics.pvaccess.client.ChannelListRequester;
import org.epics.pvaccess.server.impl.remote.ServerContextImpl;
import org.epics.pvaccess.server.rpc.RPCRequestException;
import org.epics.pvaccess.server.rpc.RPCService;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStringArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Status;
import org.epics.pvdata.pv.Status.StatusType;
import org.epics.pvdata.pv.Structure;


// TODO make pluggable, replaceable
public class ServerRPCService implements RPCService {

	private static final int TIMEOUT_SEC = 3;
	
	private static final Structure helpStructure =
		PVFactory.getFieldCreate()
			.createFieldBuilder()
				.setId("uri:ev4:nt/2012/pwd:NTScalar")
				.add("value", ScalarType.pvString)
				.createStructure();
	
	private static final Structure channelListStructure =
		PVFactory.getFieldCreate()
			.createFieldBuilder()
				.setId("uri:ev4:nt/2012/pwd:NTScalarArray")
				.addArray("value", ScalarType.pvString)
				.createStructure();

	private static final String helpString = 
		"pvAccess server RPC service.\n" +
		"arguments:\n" +
		"\tstring op\toperation to execute\n" +
		"\n" +
		"\toperations:\n" +
		"\t\tchannels\treturns a list of 'static' channels the server can provide\n" +
		"\t\t\t (no arguments)\n" +
		"\n";
	
	
	private static class ChannelListRequesterImpl implements ChannelListRequester {
		
		private final CountDownLatch doneCondition = new CountDownLatch(1);
		
		volatile Status status;
		volatile Set<String> channelNames;
		
		@Override
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
	};
	
	
	protected final ServerContextImpl serverContext; 
	
	public ServerRPCService(ServerContextImpl serverContext) {
		this.serverContext = serverContext;
	}

	@Override
	public PVStructure request(PVStructure args) throws RPCRequestException {
		
		// NTURI support
		if (args.getStructure().getID().equals("uri:ev4:nt/2012/pwd:NTURI"))
			args = args.getStructureField("query");
			
		// help support
		if (args.getSubField("help") != null)
		{
			PVStructure help =
				PVFactory.getPVDataCreate().createPVStructure(helpStructure);
			help.getStringField("value").put(helpString);
			return help;
		}
		
		PVString opField = args.getStringField("op");
		if (opField == null)
			throw new RPCRequestException(StatusType.ERROR, "unspecified 'string op' field");
		
		String op = opField.get();
		if (op.equals("channels"))
		{
			ChannelListRequesterImpl listListener = new ChannelListRequesterImpl();
			serverContext.getChannelProvider().channelList(listListener);
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
			String[] names = channelNames.toArray(new String[channelNames.size()]);
			
			PVStructure result =
				PVFactory.getPVDataCreate().createPVStructure(channelListStructure);
			result.getSubField(PVStringArray.class, "value").put(0, names.length, names, 0);
			
			return result; 
		}
		else
			throw new RPCRequestException(StatusType.ERROR, "unsupported operation '" + op + "'.");
		
	}

}
