/**
 * 
 */
package org.epics.ca.impl.remote.codec.test.perf;

import java.nio.ByteBuffer;

import org.epics.ca.PVFactory;
import org.epics.pvData.pv.DeserializableControl;
import org.epics.pvData.pv.Field;
import org.epics.pvData.pv.FieldCreate;
import org.epics.pvData.pv.PVDataCreate;
import org.epics.pvData.pv.PVDoubleArray;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.ScalarType;
import org.epics.pvData.pv.SerializableControl;

import com.sun.japex.JapexDriverBase;
import com.sun.japex.TestCase;

/**
 * @author msekoranja
 *
 */
public class ExampleStructureSerializationPerformance extends JapexDriverBase implements SerializableControl, DeserializableControl {

	public PVStructure pvField;
	
	final int DEFAULT_BUFFER_SIZE = 200000;
	final int ELEMENTS = 1000;

	public final ByteBuffer buffer = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE);
	
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
		
		PVDataCreate pvDataCreate = PVFactory.getPVDataCreate();
		
			PVStructure timeStampStructure;
			{
		        Field[] fields = new Field[3];
		        fields[0] = fieldCreate.createScalar("secondsPastEpoch", ScalarType.pvLong);
		        fields[1] = fieldCreate.createScalar("nanoSeconds", ScalarType.pvInt);
		        fields[2] = fieldCreate.createScalar("userTag", ScalarType.pvInt);
		        timeStampStructure = pvDataCreate.createPVStructure(null, "timeStamp", fields);
			}
		
			PVStructure alarmStructure;
			{
		        Field[] fields = new Field[3];
		        fields[0] = fieldCreate.createScalar("severity", ScalarType.pvInt);
		        fields[1] = fieldCreate.createScalar("status", ScalarType.pvInt);
		        fields[2] = fieldCreate.createScalar("message", ScalarType.pvString);
		        alarmStructure = pvDataCreate.createPVStructure(null, "alarm", fields);
			}
			
	        Field[] fields = new Field[3];
	        fields[0] = fieldCreate.createScalarArray("value", ScalarType.pvDouble);
	        fields[1] = timeStampStructure.getField();
	        fields[2] = alarmStructure.getField();
	        
	        pvField = pvDataCreate.createPVStructure(null, "", fields);
	        
	        PVDoubleArray ba = (PVDoubleArray)pvField.getSubField("value");
	        double[] toPut = new double[] { (byte)1.23, (byte)-0.456, (byte)333333.3 };
	        ba.put(0, toPut.length, toPut, 0);
	
	        timeStampStructure = pvField.getStructureField("timeStamp");
			timeStampStructure.getLongField("secondsPastEpoch").put(0x1122334455667788L);
			timeStampStructure.getIntField("nanoSeconds").put(0xAABBCCDD);
			timeStampStructure.getIntField("userTag").put(0xEEEEEEEE);

			alarmStructure = pvField.getStructureField("alarm");
			alarmStructure.getIntField("severity").put(0x11111111);
			alarmStructure.getIntField("status").put(0x22222222);
			alarmStructure.getStringField("message").put("Allo, Allo!");
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
				pvField.serialize(buffer, this);
			System.out.println("PVData size: " + buffer.position()/ELEMENTS);
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
