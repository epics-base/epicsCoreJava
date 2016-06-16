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

package org.epics.pvaccess.impl.remote.utils.test;

import junit.framework.TestCase;

import org.epics.pvaccess.impl.remote.utils.ServerStatusMonitor;

/**
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 */
public class ServerStatusMonitorTest extends TestCase {

	public ServerStatusMonitorTest(String methodName) {
		super(methodName);
	}

	public void testServerStatusMonitor() throws Throwable
	{
		ServerStatusMonitor ssm = new ServerStatusMonitor();
		ssm.execute();
		Thread.sleep(5000);
		ssm.destroy();
	}
}
