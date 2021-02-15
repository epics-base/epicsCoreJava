/*
 *
 */package org.epics.pvaccess.server.rpc;

import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.Status;

/**
 * Response callback used by RPCServiceAsync to report completion.
 * @author msekoranja
 */
public interface RPCResponseCallback {
    /*
     * The RPC request is done (or failed).
     * @param status Completion status.
     * @param result The response data for the RPC request or <code>null</code> if the request failed.
     */
	void requestDone(Status status, PVStructure result);
}
