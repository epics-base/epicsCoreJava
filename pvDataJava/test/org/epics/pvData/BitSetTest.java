/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData;

import junit.framework.TestCase;

import org.epics.pvData.misc.BitSet;

/**
 * JUnit test for BitSet.
 * NOTE not complete.
 * @author mse
 *
 */
public class BitSetTest extends TestCase {

	public void testSetBitSet() {
		BitSet src = new BitSet();
		
		BitSet dest = new BitSet(2);
		dest.set(10);

		// dest larger
		dest.set(src);
		assertEquals(src, dest);
		
		// src larger
		src.set(31);
		dest.set(src);
		assertEquals(src, dest);

		// dest larger again
		src.clear();
		src.set(1);
		dest.set(src);
		assertEquals(src, dest);
	}
}
