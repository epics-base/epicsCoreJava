package org.epics.pvaccess.client.example;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.epics.pvaccess.PVFactory;
import org.epics.pvaccess.impl.remote.IntrospectionRegistry;
import org.epics.pvaccess.util.HexDump;
import org.epics.pvdata.factory.StandardFieldFactory;
import org.epics.pvdata.misc.BitSet;
import org.epics.pvdata.pv.DeserializableControl;
import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.PVByteArray;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVInt;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.SerializableControl;
import org.epics.pvdata.pv.StandardField;
import org.epics.pvdata.pv.Status;
import org.epics.pvdata.pv.Status.StatusType;
import org.epics.pvdata.pv.StatusCreate;
import org.epics.pvdata.pv.Structure;
import org.epics.pvdata.pv.StructureArray;

public class SerializationExamples {

	static class SerDeSerControl implements SerializableControl, DeserializableControl
	{
		final IntrospectionRegistry incomingIR;
		final IntrospectionRegistry outgoingIR;

		public SerDeSerControl()
		{
			this.incomingIR = new IntrospectionRegistry();
			this.outgoingIR = new IntrospectionRegistry();
		}

		public SerDeSerControl(IntrospectionRegistry incomingIR, IntrospectionRegistry outgoingIR)
		{
			this.incomingIR = incomingIR;
			this.outgoingIR = outgoingIR;
		}

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
			return incomingIR.deserialize(buffer, this);
		}

		public void cachedSerialize(Field field, ByteBuffer buffer) {
			outgoingIR.serialize(field, buffer, this);
		}

	}

	static final SerDeSerControl control = new SerDeSerControl();

	static void bitSetExample(BitSet bitSet)
	{
		ByteBuffer bb = ByteBuffer.allocate(1024); bb.order(ByteOrder.LITTLE_ENDIAN);
		bitSet.serialize(bb, control);
		HexDump.hexDump(bitSet.toString(), bb.array(), bb.position());
		System.out.println();
	}

	static void bitSetExamples()
	{
		BitSet bitSet = new BitSet();
		bitSetExample(bitSet);

		int[] ixes = new int[] { 0, 1, 7, 8, 15, 55, 56, 63, 64, 65 };
		for (int ix : ixes)
		{
			bitSet.clear();
			bitSet.set(ix);
			bitSetExample(bitSet);
		}

		bitSet.clear();
		bitSet.set(0);
		bitSet.set(1);
		bitSet.set(2);
		bitSet.set(4);
		bitSetExample(bitSet);

		bitSet.set(8);
		bitSetExample(bitSet);

		bitSet.clear();
		bitSet.set(8);
		bitSet.set(17);
		bitSet.set(24);
		bitSet.set(25);
		bitSet.set(34);
		bitSet.set(40);
		bitSet.set(42);
		bitSet.set(49);
		bitSet.set(50);
		bitSetExample(bitSet);

		bitSet.set(56);
		bitSet.set(57);
		bitSet.set(58);
		bitSetExample(bitSet);

		bitSet.set(67);
		bitSetExample(bitSet);

		bitSet.set(72);
		bitSet.set(75);
		bitSetExample(bitSet);

		bitSet.set(81);
		bitSet.set(83);
		bitSetExample(bitSet);
	}

	static void statusExample(String description, Status status)
	{
		ByteBuffer bb = ByteBuffer.allocate(1024);
		status.serialize(bb, control);
		HexDump.hexDump(description, bb.array(), bb.position());
		System.out.println();
	}

	static void statusExamples()
	{
		StatusCreate statusCreate = PVFactory.getStatusCreate();
		statusExample("Status OK", statusCreate.getStatusOK());
		statusExample("WARNING, \"Low memory\", \"\"",
				statusCreate.createStatus(StatusType.WARNING, "Low memory", null));
		statusExample("ERROR, \"Failed to get, due to unexpected exception\", (stack dump)",
				statusCreate.createStatus(StatusType.ERROR, "Failed to get, due to unexpected exception", new RuntimeException()));
	}

	static void structureExample()
	{
		FieldCreate fieldCreate = PVFactory.getFieldCreate();
		PVDataCreate pvDataCreate = PVFactory.getPVDataCreate();

		ByteBuffer bb = ByteBuffer.allocate(10240);

		// TODO access via PVFactory?
		StandardField standardField = StandardFieldFactory.getStandardField();

		Structure structure = fieldCreate.createFieldBuilder().
				setId("exampleStructure").
				addArray("value", ScalarType.pvByte).
				addBoundedArray("boundedSizeArray", ScalarType.pvByte, 16).
				addFixedArray("fixedSizeArray", ScalarType.pvByte, 4).
				add("timeStamp", standardField.timeStamp()).
				add("alarm", standardField.alarm()).
				addNestedUnion("valueUnion").
					add("stringValue", ScalarType.pvString).
					add("intValue", ScalarType.pvInt).
					add("doubleValue", ScalarType.pvDouble).
					endNested().
				add("variantUnion", fieldCreate.createVariantUnion()).
				createStructure();

        PVStructure pvStructure = pvDataCreate.createPVStructure(structure);

        PVByteArray ba = (PVByteArray)pvStructure.getSubField("value");
        byte[] toPut = new byte[] { (byte)1, (byte)2, (byte)3 };
        ba.put(0, toPut.length, toPut, 0);

        ba = (PVByteArray)pvStructure.getSubField("boundedSizeArray");
        toPut = new byte[] { (byte)4, (byte)5, (byte)6, (byte)7, (byte)8  };
        ba.put(0, toPut.length, toPut, 0);

        ba = (PVByteArray)pvStructure.getSubField("fixedSizeArray");
        toPut = new byte[] { (byte)9, (byte)10, (byte)11, (byte)12 };
        ba.put(0, toPut.length, toPut, 0);

        PVStructure timeStampStructure = pvStructure.getStructureField("timeStamp");
		timeStampStructure.getLongField("secondsPastEpoch").put(0x1122334455667788L);
		timeStampStructure.getIntField("nanoseconds").put(0xAABBCCDD);
		timeStampStructure.getIntField("userTag").put(0xEEEEEEEE);

        PVStructure alarmStructure = pvStructure.getStructureField("alarm");
		alarmStructure.getIntField("severity").put(0x11111111);
		alarmStructure.getIntField("status").put(0x22222222);
		alarmStructure.getStringField("message").put("Allo, Allo!");

		((PVInt)pvStructure.getUnionField("valueUnion").select("intValue")).put(0x33333333);

		PVString pvString = (PVString)pvDataCreate.createPVScalar(ScalarType.pvString);
		pvString.put("String inside variant union.");
		pvStructure.getUnionField("variantUnion").set(pvString);

			control.outgoingIR.serialize(pvStructure.getStructure(), bb, control);

	        System.out.println(pvStructure.getStructure());
	        System.out.println();

		HexDump.hexDump("Serialized structure IF", bb.array(), bb.position());
		System.out.println();





			bb.clear();

	        pvStructure.serialize(bb, control);

	        System.out.println(pvStructure);
	        System.out.println();

		HexDump.hexDump("Serialized structure", bb.array(), bb.position());
		System.out.println();
	}

	static void structureAbbotExample()
	{
		FieldCreate fieldCreate = PVFactory.getFieldCreate();
		//PVDataCreate pvDataCreate = PVFactory.getPVDataCreate();

		ByteBuffer bb = ByteBuffer.allocate(10240);

        Structure blahStructure =
        		fieldCreate.createStructure(
        				"blah",
        				new String[] {
        						"x",
        						"y"
        				},
        				new Field[] {
        						fieldCreate.createScalar(ScalarType.pvFloat),
        						fieldCreate.createScalar(ScalarType.pvFloat)
        				}
        			);

        StructureArray blahStructureArray = fieldCreate.createStructureArray(blahStructure);


		control.cachedSerialize(blahStructure, bb);

		HexDump.hexDump("blah Structure", bb.array(), bb.position());
		System.out.println();
		bb.clear();

		control.cachedSerialize(blahStructureArray, bb);
		HexDump.hexDump("blah[] Structure", bb.array(), bb.position());
		System.out.println();
	}

	public static void main(String[] args) {
		//bitSetExamples();
		//statusExamples();
		structureExample();
		//structureAbbotExample();
	}

}
