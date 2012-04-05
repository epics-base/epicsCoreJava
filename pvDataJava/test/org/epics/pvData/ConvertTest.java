/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData;


import junit.framework.TestCase;

import org.epics.pvData.factory.ConvertFactory;
import org.epics.pvData.factory.PVDataFactory;
import org.epics.pvData.pv.Convert;
import org.epics.pvData.pv.PVByte;
import org.epics.pvData.pv.PVDataCreate;
import org.epics.pvData.pv.PVDouble;
import org.epics.pvData.pv.PVFloat;
import org.epics.pvData.pv.PVInt;
import org.epics.pvData.pv.PVLong;
import org.epics.pvData.pv.PVShort;
import org.epics.pvData.pv.PVUByte;
import org.epics.pvData.pv.PVUInt;
import org.epics.pvData.pv.PVULong;
import org.epics.pvData.pv.PVUShort;
import org.epics.pvData.pv.ScalarType;

/**
 * JUnit test for BitSet.
 * NOTE not complete.
 * @author mse
 *
 */
public class ConvertTest extends TestCase {
	private static Convert convert = ConvertFactory.getConvert();
	private static PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
	
	static private void print(String value) {System.out.println(value);}

	public void testConvertScalar() {
		PVByte pvByte = (PVByte)pvDataCreate.createPVScalar(null, ScalarType.pvByte);
		PVUByte pvUByte = (PVUByte)pvDataCreate.createPVScalar(null, ScalarType.pvUByte);
		PVShort pvShort = (PVShort)pvDataCreate.createPVScalar(null, ScalarType.pvShort);
		PVUShort pvUShort = (PVUShort)pvDataCreate.createPVScalar(null, ScalarType.pvUShort);
		PVInt pvInt = (PVInt)pvDataCreate.createPVScalar(null, ScalarType.pvInt);
		PVUInt pvUInt = (PVUInt)pvDataCreate.createPVScalar(null, ScalarType.pvUInt);
		PVLong pvLong = (PVLong)pvDataCreate.createPVScalar(null, ScalarType.pvLong);
		PVULong pvULong = (PVULong)pvDataCreate.createPVScalar(null, ScalarType.pvULong);
		PVFloat pvFloat = (PVFloat)pvDataCreate.createPVScalar(null, ScalarType.pvFloat);
		PVDouble pvDouble = (PVDouble)pvDataCreate.createPVScalar(null, ScalarType.pvDouble);
		print("");
		print("fromByte");
		byte bval = 0x7f;
		convert.fromByte(pvByte, bval);
		print("byte " +pvByte.toString());
		convert.fromByte(pvUByte, bval);
		print("ubyte " +pvUByte.toString());
		convert.fromByte(pvShort, bval);
		print("short " +pvShort.toString());
		convert.fromByte(pvUShort, bval);
		print("ushort " +pvUShort.toString());
		convert.fromByte(pvInt, bval);
		print("int " +pvInt.toString());
		convert.fromByte(pvUInt, bval);
		print("uint " +pvUInt.toString());
		convert.fromByte(pvLong, bval);
		print("long " +pvLong.toString());
		convert.fromByte(pvULong, bval);
		print("ulong " +pvULong.toString());
		convert.fromByte(pvFloat, bval);
		print("float " +pvFloat.toString());
		convert.fromByte(pvDouble, bval);
		print("double " +pvDouble.toString());
		bval++;
        convert.fromByte(pvByte, bval);
        print("byte " +pvByte.toString());
        convert.fromByte(pvUByte, bval);
        print("ubyte " +pvUByte.toString());
        convert.fromByte(pvShort, bval);
        print("short " +pvShort.toString());
        convert.fromByte(pvUShort, bval);
        print("ushort " +pvUShort.toString());
        convert.fromByte(pvInt, bval);
        print("int " +pvInt.toString());
        convert.fromByte(pvUInt, bval);
        print("uint " +pvUInt.toString());
        convert.fromByte(pvLong, bval);
        print("long " +pvLong.toString());
        convert.fromByte(pvULong, bval);
        print("ulong " +pvULong.toString());
        convert.fromByte(pvFloat, bval);
        print("float " +pvFloat.toString());
        convert.fromByte(pvDouble, bval);
        print("double " +pvDouble.toString());
		print("");
        print("fromShort");
		short sval = 0x7fff;
		print("value " + Short.toString(sval));
		convert.fromShort(pvByte, sval);
		print("byte " +pvByte.toString());
		convert.fromShort(pvUByte, sval);
		print("ubyte " +pvUByte.toString());
		convert.fromShort(pvShort, sval);
		print("short " +pvShort.toString());
		convert.fromShort(pvUShort, sval);
		print("ushort " +pvUShort.toString());
		convert.fromShort(pvInt, sval);
		print("int " +pvInt.toString());
		convert.fromShort(pvUInt, sval);
		print("uint " +pvUInt.toString());
		convert.fromShort(pvLong, sval);
		print("long " +pvLong.toString());
		convert.fromShort(pvULong, sval);
		print("ulong " +pvULong.toString());
		convert.fromShort(pvFloat, sval);
		print("float " +pvFloat.toString());
		convert.fromShort(pvDouble, sval);
		print("double " +pvDouble.toString());
		sval++;
		print("value " + Short.toString(sval));
        convert.fromShort(pvByte, sval);
        print("byte " +pvByte.toString());
        convert.fromShort(pvUByte, sval);
        print("ubyte " +pvUByte.toString());
        convert.fromShort(pvShort, sval);
        print("short " +pvShort.toString());
        convert.fromShort(pvUShort, sval);
        print("ushort " +pvUShort.toString());
        convert.fromShort(pvInt, sval);
        print("int " +pvInt.toString());
        convert.fromShort(pvUInt, sval);
        print("uint " +pvUInt.toString());
        convert.fromShort(pvLong, sval);
        print("long " +pvLong.toString());
        convert.fromShort(pvULong, sval);
        print("ulong " +pvULong.toString());
        convert.fromShort(pvFloat, sval);
        print("float " +pvFloat.toString());
        convert.fromShort(pvDouble, sval);
        print("double " +pvDouble.toString());
		print("");
        print("fromInt");
		int ival = 0x7fffffff;
		print("value " + Integer.toString(ival));
		convert.fromInt(pvByte, ival);
		print("byte " +pvByte.toString());
		convert.fromInt(pvUByte, ival);
		print("ubyte " +pvUByte.toString());
		convert.fromInt(pvShort, ival);
		print("short " +pvShort.toString());
		convert.fromInt(pvUShort, ival);
		print("ushort " +pvUShort.toString());
		convert.fromInt(pvInt, ival);
		print("int " +pvInt.toString());
		convert.fromInt(pvUInt, ival);
		print("uint " +pvUInt.toString());
		convert.fromInt(pvLong, ival);
		print("long " +pvLong.toString());
		convert.fromInt(pvULong, ival);
		print("ulong " +pvULong.toString());
		convert.fromInt(pvFloat, ival);
		print("float " +pvFloat.toString());
		convert.fromInt(pvDouble, ival);
		print("double " +pvDouble.toString());
		ival++;
		print("value " + Integer.toString(ival));
        convert.fromInt(pvByte, ival);
        print("byte " +pvByte.toString());
        convert.fromInt(pvUByte, ival);
        print("ubyte " +pvUByte.toString());
        convert.fromInt(pvShort, ival);
        print("short " +pvShort.toString());
        convert.fromInt(pvUShort, ival);
        print("ushort " +pvUShort.toString());
        convert.fromInt(pvInt, ival);
        print("int " +pvInt.toString());
        convert.fromInt(pvUInt, ival);
        print("uint " +pvUInt.toString());
        convert.fromInt(pvLong, ival);
        print("long " +pvLong.toString());
        convert.fromInt(pvULong, ival);
        print("ulong " +pvULong.toString());
        convert.fromInt(pvFloat, ival);
        print("float " +pvFloat.toString());
        convert.fromInt(pvDouble, ival);
        print("double " +pvDouble.toString());
		print("");
        print("fromLong");
		long lval = 0x7fffffffffffffffL;
		convert.fromLong(pvByte, lval);
		print("byte " +pvByte.toString());
		convert.fromLong(pvUByte, lval);
		print("ubyte " +pvUByte.toString());
		convert.fromLong(pvShort, lval);
		print("short " +pvShort.toString());
		convert.fromLong(pvUShort, lval);
		print("ushort " +pvUShort.toString());
		convert.fromLong(pvInt, lval);
		print("int " +pvInt.toString());
		convert.fromLong(pvUInt, lval);
		print("uint " +pvUInt.toString());
		convert.fromLong(pvLong, lval);
		print("long " +pvLong.toString());
		convert.fromLong(pvULong, lval);
		print("ulong " +pvULong.toString());
		convert.fromLong(pvFloat, lval);
		print("float " +pvFloat.toString());
		convert.fromLong(pvDouble, lval);
		print("double " +pvDouble.toString());
		lval++;
        convert.fromLong(pvByte, lval);
        print("byte " +pvByte.toString());
        convert.fromLong(pvUByte, lval);
        print("ubyte " +pvUByte.toString());
        convert.fromLong(pvShort, lval);
        print("short " +pvShort.toString());
        convert.fromLong(pvUShort, lval);
        print("ushort " +pvUShort.toString());
        convert.fromLong(pvInt, lval);
        print("int " +pvInt.toString());
        convert.fromLong(pvUInt, lval);
        print("uint " +pvUInt.toString());
        convert.fromLong(pvLong, lval);
        print("long " +pvLong.toString());
        convert.fromLong(pvULong, lval);
        print("ulong " +pvULong.toString());
        convert.fromLong(pvFloat, lval);
        print("float " +pvFloat.toString());
        convert.fromLong(pvDouble, lval);
        print("double " +pvDouble.toString());

	}
}
