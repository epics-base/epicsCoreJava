package org.epics.pvaccess.client.rpc.test;

import org.epics.pvaccess.client.rpc.RPCClient;
import org.epics.pvaccess.client.rpc.RPCClientImpl;
import org.epics.pvaccess.client.rpc.RPCClientRequester;
import org.epics.pvaccess.server.rpc.RPCRequestException;
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.pv.*;

import java.util.logging.Logger;

public class RPCServiceClientExample {

	private final static FieldCreate fieldCreate = FieldFactory.getFieldCreate();

	private final static Structure requestStructure =
			fieldCreate.createFieldBuilder().
				add("a", ScalarType.pvString).
				add("b", ScalarType.pvString).
				createStructure();

	public static void main(String[] args) throws Throwable {
		try
		{
			PVStructure arguments = PVDataFactory.getPVDataCreate().createPVStructure(requestStructure);
			arguments.getStringField("a").put("12.3");
			arguments.getStringField("b").put("45.6");

			//
			// sync example
			//
			{
				RPCClientImpl client = new RPCClientImpl("sum");
				try
				{
					PVStructure result = client.request(arguments, 3.0);
					System.out.println(result);
				} catch (RPCRequestException rre) {
					System.out.println(rre);
				}
				client.destroy();
			}

			//
			// async example
			//
			{
				ServiceClientRequesterImpl requester = new ServiceClientRequesterImpl();
				RPCClientImpl client = new RPCClientImpl("sum", requester);
				// we could sendRequest asynchronously, but this is soo much easier
				if (!client.waitConnect(3.0))
					throw new RuntimeException("connection timeout");

				client.sendRequest(arguments);
				if (!client.waitResponse(3.0))
					throw new RuntimeException("response timeout");

				Status status = requester.getStatus();
				if (status.isSuccess())
					System.out.println(requester.getResult());
				else
					System.out.println(status);
			}
		}
		finally
		{
			org.epics.pvaccess.ClientFactory.stop();
		}
	}

	private static class ServiceClientRequesterImpl implements RPCClientRequester
	{
	    private static final Logger logger = Logger.getLogger(ServiceClientRequesterImpl.class.getName());

	    private volatile Status status;
	    private volatile PVStructure result;

		public String getRequesterName() {
			return getClass().getName();
		}

		public void message(String message, MessageType messageType) {
			logger.finer(getRequesterName() + ": [" +  messageType + "] " + message);
		}

		public void connectResult(RPCClient client, Status status) {
			// noop
		}

		public void requestResult(RPCClient client, Status status, PVStructure pvResult) {
			this.status = status;
			this.result = pvResult;
		}

		/**
		 * @return the status
		 */
		public Status getStatus() {
			return status;
		}

		/**
		 * @return the result
		 */
		public PVStructure getResult() {
			return result;
		}

	}

}
