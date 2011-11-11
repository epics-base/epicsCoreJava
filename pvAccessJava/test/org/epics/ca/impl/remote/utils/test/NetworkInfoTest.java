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

package org.epics.ca.impl.remote.utils.test;

import junit.framework.TestCase;

import org.epics.ca.impl.remote.utils.NetworkInfo;

/**
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public class NetworkInfoTest extends TestCase {

   	/**
	 * Constructor for InetAddressUtilTest.
	 * @param methodName
	 */
	public NetworkInfoTest(String methodName) {
		super(methodName);
	}

	/**
	 * Conversion test.
	 */
	public void testNetworkInfo() throws Throwable
	{
		NetworkInfo.main(new String[0]);
	}
}
