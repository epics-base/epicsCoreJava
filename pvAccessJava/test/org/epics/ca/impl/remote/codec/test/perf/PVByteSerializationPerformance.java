/**
 * 
 */
package org.epics.ca.impl.remote.codec.test.perf;

import java.nio.ByteBuffer;

import org.epics.ca.PVFactory;
import org.epics.pvData.pv.DeserializableControl;
import org.epics.pvData.pv.Field;
import org.epics.pvData.pv.FieldCreate;
import org.epics.pvData.pv.PVByte;
import org.epics.pvData.pv.ScalarType;
import org.epics.pvData.pv.SerializableControl;

import com.sun.japex.JapexDriverBase;
import com.sun.japex.TestCase;

/**
 * @author msekoranja
 *
 */
public class PVByteSerializationPerformance extends JapexDriverBase implements SerializableControl, DeserializableControl {

	PVByte pvField;
	
	final int DEFAULT_BUFFER_SIZE = 102400;
	final int ELEMENTS = DEFAULT_BUFFER_SIZE/8;

	final ByteBuffer buffer = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE);
	
	
	
	/* (non-Javadoc)
	 * @see org.epics.pvData.pv.DeserializableControl#ensureData(int)
	 */
	@Override
	public void ensureData(int size) {
		// TODO Auto-generated method stub
		
	}


	/* (non-Javadoc)
	 * @see org.epics.pvData.pv.DeserializableControl#alignData(int)
	 */
	@Override
	public void alignData(int alignment) {
		// TODO Auto-generated method stub
		
	}


	/* (non-Javadoc)
	 * @see org.epics.pvData.pv.SerializableControl#flushSerializeBuffer()
	 */
	@Override
	public void flushSerializeBuffer() {
		// TODO Auto-generated method stub
		
	}


	/* (non-Javadoc)
	 * @see org.epics.pvData.pv.SerializableControl#ensureBuffer(int)
	 */
	@Override
	public void ensureBuffer(int size) {
		// TODO Auto-generated method stub
		
	}


	/* (non-Javadoc)
	 * @see org.epics.pvData.pv.SerializableControl#alignBuffer(int)
	 */
	@Override
	public void alignBuffer(int alignment) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void cachedSerialize(Field field, ByteBuffer buffer) {
		// no cache
		field.serialize(buffer, this);
	}
	
	static final FieldCreate fieldCreate = PVFactory.getFieldCreate();
	
	@Override
	public Field cachedDeserialize(ByteBuffer buffer) {
		// no cache
		return fieldCreate.deserialize(buffer, this);
	}

	/* (non-Javadoc)
	 * @see com.sun.japex.JapexDriverBase#initializeDriver()
	 */
	@Override
	public void initializeDriver() {
		super.initializeDriver();
		
		Field field = fieldCreate.createScalar("value", ScalarType.pvByte);
		pvField = (PVByte)PVFactory.getPVDataCreate().createPVField(null, field);
	}
	
	int index;
	boolean testSerialization;
	
	/* (non-Javadoc)
	 * @see com.sun.japex.JapexDriverBase#prepare(com.sun.japex.TestCase)
	 */
	@Override
	public void prepare(TestCase testCase) {
		testSerialization = testCase.getBooleanParam("testSerialization");
		if (testSerialization)
		{
			buffer.clear();
		}
		else
		{
			buffer.clear();
			for (int i = 0; i < ELEMENTS; i++)
			{
				pvField.put((byte)i);
				pvField.serialize(buffer, this);
			}
			buffer.flip();
			index = 0;
		}
	}

	
	/* (non-Javadoc)
	 * @see com.sun.japex.JapexDriverBase#run(com.sun.japex.TestCase)
	 */
	@Override
	public void run(TestCase testCase) {
		if (testSerialization)
		{
			if (index++ == ELEMENTS)
			{
				buffer.clear();
				index = 1;
			}
			pvField.put((byte)index);
			pvField.serialize(buffer, this);
			
		}
		else
		{
			if (index++ == ELEMENTS)
			{
				buffer.position(0);
				index = 1;
			}
			pvField.deserialize(buffer, this);
		}
	}

}
