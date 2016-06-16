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

package org.epics.pvaccess.util.test;

import junit.framework.TestCase;

import org.epics.pvaccess.util.GrowingCircularBuffer;

/**
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public class GrowingCircularBufferTest extends TestCase {

	public GrowingCircularBufferTest(String methodName) {
		super(methodName);
	}

	/**
	 * Conversion test.
	 */
	public void testBuffer()
	{
		final int CAPACITY = 10;
		GrowingCircularBuffer<Integer> cb = new GrowingCircularBuffer<Integer>(CAPACITY);
		
		assertEquals(CAPACITY, cb.capacity());
		
		// null test
		assertNull(cb.extract());
		
		// insert, get test
		boolean first = cb.insert(1);
		assertEquals(1, cb.size());
		assertEquals(new Integer(1), cb.extract());
		assertTrue(first);
		assertNull(cb.extract());
		assertEquals(0, cb.size());
		
		for (int i = 0; i < 2*CAPACITY; i++) {
			first = cb.insert(i);
			assertEquals(i+1, cb.size());
			assertEquals(cb.size() == 1, first);
		}
		assertEquals(2*CAPACITY, cb.size());

		for (int i = 0; i < 2*CAPACITY; i++) {
			assertEquals(new Integer(i), cb.extract());
			assertEquals(2*CAPACITY-i-1, cb.size());
		}
		assertNull(cb.extract());
		assertEquals(0, cb.size());
	}
}
