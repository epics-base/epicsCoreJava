/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata;

import java.nio.ByteBuffer;

import junit.framework.TestCase;

import org.epics.pvdata.misc.BitSet;
import org.epics.pvdata.pv.DeserializableControl;
import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.SerializableControl;

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

	public void testOrAndBitSet() {
	{
		BitSet b1 = new BitSet(16);
		assertEquals(0, b1.length());
		b1.set(0);
		assertEquals(1, b1.length());


		BitSet b2 = new BitSet(72);
		b2.set(70);
		b2.set(71);

		BitSet b3 = new BitSet(72);
		b3.set(71);

		// b1 will be expanded (reallocation)
		b1.or_and(b2, b3);
		assertEquals(2, b1.cardinality());
		assertEquals(72, b1.length());

		// b1 will be expanded (no reallocation needed)
		b1.clear();
		b1.or_and(b2, b3);
		assertEquals(1, b1.cardinality());
		assertEquals(72, b1.length());
	}

	}

	static class SerControl implements SerializableControl, DeserializableControl
	{

		public void ensureData(int size) {
		}

		public void alignData(int alignment) {
		}

		public void flushSerializeBuffer() {
		}

		public void ensureBuffer(int size) {
		}

		public void alignBuffer(int alignment) {
		}

		public Field cachedDeserialize(ByteBuffer buffer) {
			return null;
		}

		public void cachedSerialize(Field field, ByteBuffer buffer) {
		}
	}

	public void testBitSetSerialization()
	{
		ByteBuffer b = ByteBuffer.allocate(129+1);
		SerControl t = new SerControl();

		for (int i = -1; i <= 1024; i++)
		{

			b.clear();
			BitSet s1 = new BitSet();
			if (i >= 0) s1.set(i);
			s1.serialize(b, t);

			b.flip();

			BitSet s2 = new BitSet();
			s2.deserialize(b, t);

			assertEquals(s1, s2);
		}
	}
}
