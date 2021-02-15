package org.epics.pvaccess.server.rpc.test;

import org.epics.pvaccess.PVAException;
import org.epics.pvaccess.server.rpc.RPCRequestException;
import org.epics.pvaccess.server.rpc.RPCServer;
import org.epics.pvaccess.server.rpc.RPCService;
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Status.StatusType;
import org.epics.pvdata.pv.Structure;

public class RPCWildcardServiceExample {

	private final static FieldCreate fieldCreate = FieldFactory.getFieldCreate();

	private final static Structure resultStructure =
			fieldCreate.createFieldBuilder().
				add("channelName", ScalarType.pvString).
				createStructure();

	static class WildcardServiceImpl implements RPCService
	{
		public PVStructure request(PVStructure args) throws RPCRequestException {

	        // NTURI support
			if (!args.getStructure().getID().startsWith("epics:nt/NTURI:1."))
	            throw new RPCRequestException(StatusType.ERROR, "RPC argument must be a NTURI normative type");

			// this is a wildcard service, get the actual channel name
			String channelName = args.getStringField("path").get();

			PVStructure result = PVDataFactory.getPVDataCreate().createPVStructure(resultStructure);
			result.getStringField("channelName").put(channelName);

			return result;
		}
	}

	public static void main(String[] args) throws PVAException
	{

		RPCServer server = new RPCServer();

		server.registerService("wild*", new WildcardServiceImpl());
		// you can register as many services as you want here ...

		server.printInfo();
		server.run(0);
	}

}
