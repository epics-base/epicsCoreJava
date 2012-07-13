/**
 * 
 */
package org.epics.pvaccess.server.rpc;

import org.epics.pvdata.pv.PVStructure;

/**
 * @author msekoranja
 */
public interface RPCService {
	PVStructure request(PVStructure args) throws RPCRequestException;
}
