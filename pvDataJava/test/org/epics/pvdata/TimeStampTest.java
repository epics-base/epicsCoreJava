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

package org.epics.pvdata;

import junit.framework.TestCase;

import org.epics.pvdata.property.TimeStamp;
import org.epics.pvdata.property.TimeStampFactory;

/**
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public class TimeStampTest extends TestCase {

   	/**
	 * Constructor for InetAddressUtilTest.
	 * @param methodName
	 */
	public TimeStampTest(String methodName) {
		super(methodName);
	}

	/**
	 * Conversion test.
	 */
	public void testConversion()
	{
		TimeStamp t1 = TimeStampFactory.create();
		TimeStamp t2 = TimeStampFactory.create();
		t1.getCurrentTime();
		t2.put(t1.getSecondsPastEpoch(), t1.getNanoSeconds());
		assertEquals(t1.getMilliSeconds(), t2.getMilliSeconds());
		assertEquals(t1.getSecondsPastEpoch(), t2.getSecondsPastEpoch());
		assertEquals(t1.getNanoSeconds(), t2.getNanoSeconds());
		assertTrue(t1.equals(t2));
	}
}
