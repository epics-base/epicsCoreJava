/*
 *
 */
package org.epics.pvaccess.server.rpc;

import org.epics.pvdata.pv.PVStructure;

/**
 * Interface defining a service.
 * @author msekoranja
 */
public interface RPCService extends Service {

	/**
	 * RPC call request.
	 * Implementation of this method should read arguments encoded in a <code>PVStructure</code>
	 * and return a result also in a form of <code>PVStructure</code>. In case of an error
	 * a <code>RPCRequestException</code> exception should be thrown.
	 * @param args RPC request arguments.
	 * @return RPC request result.
	 * @throws RPCRequestException thrown in a case of an error.
	 */
	PVStructure request(PVStructure args) throws RPCRequestException;
}
