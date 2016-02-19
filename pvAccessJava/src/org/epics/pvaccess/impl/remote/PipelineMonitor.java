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

package org.epics.pvaccess.impl.remote;

import org.epics.pvdata.monitor.Monitor;

/**
 * Pipeline (streaming) support API (optional).
 * This is used by pvAccess to implement pipeline (streaming) monitors.
 */
public interface PipelineMonitor extends Monitor {

	/**
	 * Report remote queue status.
	 * @param freeElements number of free elements.
	 */
	void reportRemoteQueueStatus(int freeElements);

}
