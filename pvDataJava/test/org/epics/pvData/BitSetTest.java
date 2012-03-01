/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData;

import java.nio.ByteBuffer;

import junit.framework.TestCase;

import org.epics.pvData.misc.BitSet;
import org.epics.pvData.pv.DeserializableControl;
import org.epics.pvData.pv.Field;
import org.epics.pvData.pv.SerializableControl;

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
	
	static class SerControl implements SerializableControl, DeserializableControl
	{

		@Override
		public void ensureData(int size) {
		}

		@Override
		public void alignData(int alignment) {
		}

		@Override
		public void flushSerializeBuffer() {
		}

		@Override
		public void ensureBuffer(int size) {
		}

		@Override
		public void alignBuffer(int alignment) {
		}

		@Override
		public Field cachedDeserialize(ByteBuffer buffer) {
			return null;
		}

		@Override
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
