package org.epics.pvaccess.server.rpc.test;

import org.epics.pvaccess.PVAException;
import org.epics.pvaccess.server.rpc.RPCRequestException;
import org.epics.pvaccess.server.rpc.RPCServer;
import org.epics.pvaccess.server.rpc.RPCService;
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Structure;

public class RPCServiceExample {

	private final static FieldCreate fieldCreate = FieldFactory.getFieldCreate();
	
	private final static Structure resultStructure =
		fieldCreate.createStructure(
				new String[] { "c" },
				new Field[] { fieldCreate.createScalar(ScalarType.pvDouble) }
				);

	static class SumServiceImpl implements RPCService
	{
		@Override
		public PVStructure request(PVStructure args) throws RPCRequestException {
			// TODO error handling
			
			double a = Double.valueOf(args.getStringField("a").get());
			double b = Double.valueOf(args.getStringField("b").get());
			
			PVStructure result = PVDataFactory.getPVDataCreate().createPVStructure(resultStructure);
			result.getDoubleField("c").put(a+b);
			
			return result;
		}
	}
	
	public static void main(String[] args) throws PVAException
	{

		RPCServer server = new RPCServer();
		
		server.registerService("sum", new SumServiceImpl());
		// you can register as many services as you want here ...
		
		server.printInfo();
		server.run(0);
	}

}
