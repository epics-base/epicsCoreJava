/*
 *
 */
package org.epics.pvaccess.impl.remote.codec.test.perf;

import java.nio.ByteBuffer;

import org.epics.pvaccess.PVFactory;
import org.epics.pvdata.factory.StandardFieldFactory;
import org.epics.pvdata.pv.DeserializableControl;
import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVDoubleArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.SerializableControl;
import org.epics.pvdata.pv.StandardField;

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
	 * @see org.epics.pvdata.pv.DeserializableControl#ensureData(int)
	 */
	public void ensureData(int size) {
		// TODO Auto-generated method stub

	}


	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.DeserializableControl#alignData(int)
	 */
	public void alignData(int alignment) {
		// TODO Auto-generated method stub

	}


	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.SerializableControl#flushSerializeBuffer()
	 */
	public void flushSerializeBuffer() {
		// TODO Auto-generated method stub

	}


	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.SerializableControl#ensureBuffer(int)
	 */
	public void ensureBuffer(int size) {
		// TODO Auto-generated method stub

	}


	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.SerializableControl#alignBuffer(int)
	 */
	public void alignBuffer(int alignment) {
		// TODO Auto-generated method stub

	}

	public void cachedSerialize(Field field, ByteBuffer buffer) {
		// no cache
		field.serialize(buffer, this);
	}

	static final FieldCreate fieldCreate = PVFactory.getFieldCreate();

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
		// TODO access via PVFactory?
		StandardField standardField = StandardFieldFactory.getStandardField();

        Field[] fields = new Field[3];
        fields[0] = fieldCreate.createScalarArray(ScalarType.pvDouble);
        fields[1] = standardField.timeStamp();
        fields[2] = standardField.doubleAlarm();

        pvField = pvDataCreate.createPVStructure(fieldCreate.createStructure(new String[] { "value", "timeStamp", "alarm" }, fields)
        );

        PVDoubleArray ba = (PVDoubleArray)pvField.getSubField("value");
        double[] toPut = new double[] { (byte)1.23, (byte)-0.456, (byte)333333.3 };
        ba.put(0, toPut.length, toPut, 0);

        PVStructure timeStampStructure = pvField.getStructureField("timeStamp");
		timeStampStructure.getLongField("secondsPastEpoch").put(0x1122334455667788L);
		timeStampStructure.getIntField("nanoseconds").put(0xAABBCCDD);
		timeStampStructure.getIntField("userTag").put(0xEEEEEEEE);

        PVStructure alarmStructure = pvField.getStructureField("alarm");
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
