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

package org.epics.ca.util.test;

import junit.framework.TestCase;

import org.epics.ca.util.CircularBuffer;

/**
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public class CircularBufferTest extends TestCase {

   	/**
	 * Constructor for InetAddressUtilTest.
	 * @param methodName
	 */
	public CircularBufferTest(String methodName) {
		super(methodName);
	}

	/**
	 * Conversion test.
	 */
	public void testBuffer()
	{
		final int CAPACITY = 10;
		CircularBuffer<Integer> cb = new CircularBuffer<Integer>(CAPACITY);
		
		assertEquals(CAPACITY, cb.capacity());
		
		// null test
		assertNull(cb.extract());
		
		// insert, get test
		cb.insert(1);
		assertEquals(1, cb.size());
		assertEquals(new Integer(1), cb.extract());
		assertNull(cb.extract());
		assertEquals(0, cb.size());
		
		for (int i = 0; i < 2*CAPACITY; i++) {
			cb.insert(i);
			assertEquals(Math.min(i+1, CAPACITY), cb.size());
		}
		assertEquals(CAPACITY, cb.size());

		// should contain elements from 10 to 19 only
		for (int i = CAPACITY; i < 2*CAPACITY; i++) {
			assertEquals(new Integer(i), cb.extract());
			assertEquals(Math.min(2*CAPACITY-i-1, CAPACITY), cb.size());
		}
		assertNull(cb.extract());
		assertEquals(0, cb.size());
	}
}
