/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
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
	 * @param methodName method name
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
		t2.put(t1.getSecondsPastEpoch(), t1.getNanoseconds());
		assertEquals(t1.getMilliSeconds(), t2.getMilliSeconds());
		assertEquals(t1.getSecondsPastEpoch(), t2.getSecondsPastEpoch());
		assertEquals(t1.getNanoseconds(), t2.getNanoseconds());
		assertTrue(t1.equals(t2));
	}
}
