/**
: * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata.factory;

import java.util.regex.Pattern;

import org.epics.pvdata.pv.BooleanArrayData;
import org.epics.pvdata.pv.ByteArrayData;
import org.epics.pvdata.pv.Convert;
import org.epics.pvdata.pv.DoubleArrayData;
import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.FloatArrayData;
import org.epics.pvdata.pv.IntArrayData;
import org.epics.pvdata.pv.LongArrayData;
import org.epics.pvdata.pv.PVArray;
import org.epics.pvdata.pv.PVBoolean;
import org.epics.pvdata.pv.PVBooleanArray;
import org.epics.pvdata.pv.PVByte;
import org.epics.pvdata.pv.PVByteArray;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVDouble;
import org.epics.pvdata.pv.PVDoubleArray;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVFloat;
import org.epics.pvdata.pv.PVFloatArray;
import org.epics.pvdata.pv.PVInt;
import org.epics.pvdata.pv.PVIntArray;
import org.epics.pvdata.pv.PVLong;
import org.epics.pvdata.pv.PVLongArray;
import org.epics.pvdata.pv.PVScalar;
import org.epics.pvdata.pv.PVScalarArray;
import org.epics.pvdata.pv.PVShort;
import org.epics.pvdata.pv.PVShortArray;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStringArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.PVStructureArray;
import org.epics.pvdata.pv.PVUByte;
import org.epics.pvdata.pv.PVUByteArray;
import org.epics.pvdata.pv.PVUInt;
import org.epics.pvdata.pv.PVUIntArray;
import org.epics.pvdata.pv.PVULong;
import org.epics.pvdata.pv.PVULongArray;
import org.epics.pvdata.pv.PVUShort;
import org.epics.pvdata.pv.PVUShortArray;
import org.epics.pvdata.pv.Scalar;
import org.epics.pvdata.pv.ScalarArray;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.ShortArrayData;
import org.epics.pvdata.pv.StringArrayData;
import org.epics.pvdata.pv.Structure;
import org.epics.pvdata.pv.StructureArray;
import org.epics.pvdata.pv.StructureArrayData;
import org.epics.pvdata.pv.Type;

/**
 * Factory to obtain the implementation of <i>Convert</i>
 * 
 * @author mrktestByteArrayCopy
 * 
 */
public final class ConvertFactory {
    /**
     * Implements <i>Convert</i>. The implementation ensures that a single
     * instance is created.
     * 
     * @return the implementation of <i>Convert</i>
     */
    public static Convert getConvert() {
        return ImplementConvert.getConvert();
    }

    private static final class ImplementConvert implements Convert {
        private static final Pattern separatorPattern = Pattern.compile("[,]");
        private static ImplementConvert singleImplementation = null;

        private static synchronized ImplementConvert getConvert() {
            if (singleImplementation == null) {
                singleImplementation = new ImplementConvert();
            }
            return singleImplementation;
        }

        private static final PVDataCreate pvDataCreate
            = PVDataFactory.getPVDataCreate();
        // The following are to prevent continually creation of new objects
        private static final BooleanArrayData booleanArrayData = new BooleanArrayData();
        private static final ByteArrayData byteArrayData = new ByteArrayData();
        private static final ShortArrayData shortArrayData = new ShortArrayData();
        private static final IntArrayData intArrayData = new IntArrayData();
        private static final LongArrayData longArrayData = new LongArrayData();
        private static final FloatArrayData floatArrayData = new FloatArrayData();
        private static final DoubleArrayData doubleArrayData = new DoubleArrayData();
        private static final StringArrayData stringArrayData = new StringArrayData();
        private static final StructureArrayData structureArrayData = new StructureArrayData();

        // Guarantee that ImplementConvert can only be created via getConvert
        private ImplementConvert()
        {}
        /* (non-Javadoc)
         * @see org.epics.pvdata.pv.Convert#getFullFieldName(java.lang.StringBuilder, org.epics.pvdata.pv.PVField)
         */
        @Override
        public void getFullFieldName(StringBuilder buf,PVField pvField) {
            buf.setLength(0);
            PVStructure parent = pvField.getParent();
            PVField pvNow = pvField;
            while(parent!=null) {
                PVField[] pvFields = parent.getPVFields();
                for(int i=0; i<pvFields.length; i++) {
                    if(pvFields[i]== pvNow) {
                        if(buf.length()>0) buf.insert(0, '.');
                        buf.insert(0,parent.getStructure().getFieldName(i));
                        pvNow = parent;
                        parent = parent.getParent();
                    }
                }
            }
        }
        /* (non-Javadoc)
         * @see org.epics.pvdata.pv.Convert#getString(java.lang.StringBuilder, org.epics.pvdata.pv.PVField, int)
         */
        @Override
        public void getString(StringBuilder buf, PVField pv, int indentLevel) {
            convertToString(buf,pv,indentLevel);
        }
        /* (non-Javadoc)
         * @see org.epics.pvdata.pv.Convert#getString(java.lang.StringBuilder, org.epics.pvdata.pv.PVField)
         */
        @Override
        public void getString(StringBuilder buf, PVField pv) {
            convertToString(buf,pv,0);
        }
        /*
         * (non-Javadoc)
         * 
         * @see
         * org.epics.pvdata.pv.Convert#fromString(org.epics.pvdata.pv.PVScalar,
         * java.lang.String)
         */
        @Override
        public void fromString(PVScalar pv, String from) {
            Scalar scalar = pv.getScalar();
            ScalarType scalarType = scalar.getScalarType();
            switch (scalarType) {
            case pvBoolean: {
                PVBoolean value = (PVBoolean) pv;
                value.put(Boolean.parseBoolean(from));
                break;
            }
            case pvByte: {
                PVByte value = (PVByte) pv;
                value.put((byte) (long) Long.decode(from));
                break;
            }
            case pvShort: {
                PVShort value = (PVShort) pv;
                value.put((short) (long) Long.decode(from));
                break;
            }
            case pvInt: {
                PVInt value = (PVInt) pv;
                value.put((int) (long) Long.decode(from));
                break;
            }
            case pvLong: {
                PVLong value = (PVLong) pv;
                value.put(Long.decode(from));
                break;
            }
            case pvUByte: {
                PVUByte pvvalue = (PVUByte) pv;
                long value = Long.decode(from);
                if(value>0) value = -value;
                pvvalue.put((byte)value);
                break;
            }
            case pvUShort: {
                PVUShort pvvalue = (PVUShort) pv;
                long value = Long.decode(from);
                if(value>0) value = -value;
                pvvalue.put((byte)value);
                break;
            }
            case pvUInt: {
                PVUInt pvvalue = (PVUInt) pv;
                long value = Long.decode(from);
                if(value>0) value = -value;
                pvvalue.put((byte)value);
                break;
            }
            case pvULong: {
                PVULong pvvalue = (PVULong) pv;
                long value = Long.decode(from);
                if(value>0) value = -value;
                pvvalue.put(value);
                break;
            }
            case pvFloat: {
                PVFloat value = (PVFloat) pv;
                value.put(Float.valueOf(from));
                break;
            }
            case pvDouble: {
                PVDouble value = (PVDouble) pv;
                value.put(Double.valueOf(from));
                break;
            }
            case pvString: {
                PVString value = (PVString) pv;
                value.put(from);
                break;
            }
            default:
                throw new IllegalArgumentException("Unknown scalarType  "
                        + scalarType.toString());
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.epics.pvdata.pv.Convert#fromString(org.epics.pvdata.pv.PVScalarArray
         * , java.lang.String)
         */
        @Override
        public int fromString(PVScalarArray pv, String from) {
            if ((from.charAt(0) == '[') && from.endsWith("]")) {
                int offset = from.lastIndexOf(']');
                from = from.substring(1, offset);
            }
            String[] values = separatorPattern.split(from);
            int num = fromStringArray(pv, 0, values.length, values, 0);
            int length = values.length;
            if (num < length)
                length = num;
            pv.setLength(length);
            return length;
        }

        /*
         * (non-Javadoc)
         * 
         * @seeorg.epics.pvdata.pv.Convert#fromStringArray(org.epics.pvdata.pv.
         * PVScalarArray, int, int, java.lang.String[], int)
         */
        @Override
        public int fromStringArray(PVScalarArray pv, int offset, int len,
                String[] from, int fromOffset) {
            return convertFromStringArray(pv, offset, len, from, fromOffset);
        }

        /*
         * (non-Javadoc)
         * 
         * @seeorg.epics.pvdata.pv.Convert#toStringArray(org.epics.pvdata.pv.
         * PVScalarArray, int, int, java.lang.String[], int)
         */
        @Override
        public int toStringArray(PVScalarArray pv, int offset, int len,
                String[] to, int toOffset) {
            return convertToStringArray(pv, offset, len, to, toOffset);
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.epics.pvdata.pv.Convert#isCopyCompatible(org.epics.pvdata.pv.
         * Field, org.epics.pvdata.pv.Field)
         */
        @Override
        public boolean isCopyCompatible(Field from, Field to) {
            if (from.getType() != to.getType())
                return false;
            switch (from.getType()) {
            case scalar:
                return isCopyScalarCompatible((Scalar) from, (Scalar) to);
            case scalarArray:
                return isCopyScalarArrayCompatible((ScalarArray) from,
                        (ScalarArray) to);
            case structure:
                return isCopyStructureCompatible((Structure) from,
                        (Structure) to);
            case structureArray:
                return isCopyStructureArrayCompatible((StructureArray) from,
                        (StructureArray) to);
            }
            throw new IllegalStateException(
                    "Logic error. Should never get here");
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.epics.pvdata.pv.Convert#copy(org.epics.pvdata.pv.PVField,
         * org.epics.pvdata.pv.PVField)
         */
        @Override
        public void copy(PVField from, PVField to) {
            switch (from.getField().getType()) {
            case scalar:
                copyScalar((PVScalar) from, (PVScalar) to);
                return;
            case scalarArray: {
                PVScalarArray fromArray = (PVScalarArray) from;
                PVScalarArray toArray = (PVScalarArray) to;
                int length = copyScalarArray(fromArray, 0, toArray, 0,
                        fromArray.getLength());
                if (toArray.getLength() != length)
                    toArray.setLength(length);
                return;
            }
            case structure:
                copyStructure((PVStructure) from, (PVStructure) to);
                return;
            case structureArray: {
                PVStructureArray fromArray = (PVStructureArray) from;
                PVStructureArray toArray = (PVStructureArray) to;
                copyStructureArray(fromArray, toArray);
                return;
            }
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.epics.pvdata.pv.Convert#isCopyScalarCompatible(org.epics.pvdata
         * .pv.Scalar, org.epics.pvdata.pv.Scalar)
         */
        @Override
        public boolean isCopyScalarCompatible(Scalar fromField, Scalar toField) {
            ScalarType fromScalarType = fromField.getScalarType();
            ScalarType toScalarType = toField.getScalarType();
            if (fromScalarType == toScalarType)
                return true;
            if (fromScalarType.isNumeric() && toScalarType.isNumeric())
                return true;
            if (fromScalarType == ScalarType.pvString)
                return true;
            if (toScalarType == ScalarType.pvString)
                return true;
            return false;
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.epics.pvdata.pv.Convert#copyScalar(org.epics.pvdata.pv.PVScalar,
         * org.epics.pvdata.pv.PVScalar)
         */
        @Override
        public void copyScalar(PVScalar from, PVScalar to) {
            if (to.isImmutable()) {
                if (from.equals(to))
                    return;
                throw new IllegalArgumentException(
                        "Convert.copyScalar destination is immutable");
            }
            ScalarType fromType = from.getScalar().getScalarType();
            ScalarType toType = to.getScalar().getScalarType();
            switch (fromType) {
            case pvBoolean: {
                if (toType != ScalarType.pvBoolean) {
                    if (toType != ScalarType.pvString) {
                        throw new IllegalArgumentException(
                                "Convert.copyScalar arguments are not compatible");
                    }
                }
                PVBoolean data = (PVBoolean) from;
                boolean value = data.get();
                if (toType == ScalarType.pvString) {
                    PVString dataTo = (PVString) to;
                    dataTo.put(((Boolean) value).toString());
                } else {
                    PVBoolean dataTo = (PVBoolean) to;
                    dataTo.put(value);
                }
                break;
            }
            case pvByte: {
                PVByte data = (PVByte) from;
                byte value = data.get();
                fromByte(to, value);
                break;
            }
            case pvShort: {
                PVShort data = (PVShort) from;
                short value = data.get();
                fromShort(to, value);
                break;
            }
            case pvInt: {
                PVInt data = (PVInt) from;
                int value = data.get();
                fromInt(to, value);
                break;
            }
            case pvLong: {
                PVLong data = (PVLong) from;
                long value = data.get();
                fromLong(to, value);
                break;
            }
            case pvUByte: {
                PVUByte data = (PVUByte) from;
                byte value = data.get();
                fromUByte(to,value);
                break;
            }
            case pvUShort: {
                PVUShort data = (PVUShort) from;
                short value = data.get();
                fromUShort(to,value);
                break;
            }
            case pvUInt: {
                PVUInt data = (PVUInt) from;
                int value = data.get();
                fromUInt(to,value);
                break;
            }
            case pvULong: {
                PVULong data = (PVULong) from;
                long value = data.get();
                fromULong(to, value);
                break;
            }
            case pvFloat: {
                PVFloat data = (PVFloat) from;
                float value = data.get();
                fromFloat(to, value);
                break;
            }
            case pvDouble: {
                PVDouble data = (PVDouble) from;
                double value = data.get();
                fromDouble(to, value);
                break;
            }
            case pvString: {
                PVString data = (PVString) from;
                String value = data.get();
                fromString(to, value);
                break;
            }
            default:
                throw new IllegalArgumentException(
                        "Convert.copyScalar arguments are not compatible");
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.epics.pvdata.pv.Convert#isCopyScalarArrayCompatible(org.epics
         * .pvData.pv.ScalarArray, org.epics.pvdata.pv.ScalarArray)
         */
        @Override
        public boolean isCopyScalarArrayCompatible(ScalarArray fromArray,
                ScalarArray toArray) {
            ScalarType fromType = fromArray.getElementType();
            ScalarType toType = toArray.getElementType();
            if (fromType == toType)
                return true;
            if (fromType.isNumeric() && toType.isNumeric())
                return true;
            if (toType == ScalarType.pvString)
                return true;
            if (fromType == ScalarType.pvString)
                return true;
            return false;
        }

        /*
         * (non-Javadoc)
         * 
         * @seeorg.epics.pvdata.pv.Convert#copyScalarArray(org.epics.pvdata.pv.
         * PVScalarArray, int, org.epics.pvdata.pv.PVScalarArray, int, int)
         */
        @Override
        public int copyScalarArray(PVScalarArray from, int offset,
                PVScalarArray to, int toOffset, int len) {
            if (to.isImmutable()) {
                if (from.equals(to))
                    return from.getLength();
                throw new IllegalArgumentException(
                        "Convert.copyArray destination is immutable");
            }
            ScalarType fromElementType = from.getScalarArray().getElementType();
            ScalarType toElementType = to.getScalarArray().getElementType();

            if (from.isImmutable() && (fromElementType == toElementType)) {
                if (offset == 0 && toOffset == 0 && len == from.getLength()) {
                    return copyArrayDataReference(from, to);
                }
            }

            int ncopy = 0;
            if (toElementType.isNumeric() && fromElementType.isNumeric()) {
                ncopy = copyNumericArray(from, offset, to, toOffset, len);
            } else if (toElementType == ScalarType.pvBoolean
                    && fromElementType == ScalarType.pvBoolean) {
                PVBooleanArray pvfrom = (PVBooleanArray) from;
                PVBooleanArray pvto = (PVBooleanArray) to;
                outer: while (len > 0) {
                    int num = 0;
                    boolean[] data = null;
                    int fromOffset = 0;
                    synchronized (booleanArrayData) {
                        num = pvfrom.get(offset, len, booleanArrayData);
                        data = booleanArrayData.data;
                        fromOffset = booleanArrayData.offset;
                    }
                    if (num <= 0)
                        break;
                    while (num > 0) {
                        int n = pvto.put(toOffset, num, data, fromOffset);
                        if (n <= 0)
                            break outer;
                        len -= n;
                        num -= n;
                        ncopy += n;
                        offset += n;
                        toOffset += n;
                    }
                }
            } else if (toElementType == ScalarType.pvString
                    && fromElementType == ScalarType.pvString) {
                PVStringArray pvfrom = (PVStringArray) from;
                PVStringArray pvto = (PVStringArray) to;
                outer: while (len > 0) {
                    int num = 0;
                    String[] data = null;
                    int fromOffset = 0;
                    synchronized (stringArrayData) {
                        num = pvfrom.get(offset, len, stringArrayData);
                        data = stringArrayData.data;
                        fromOffset = stringArrayData.offset;
                    }
                    if (num <= 0)
                        break;
                    while (num > 0) {
                        int n = pvto.put(toOffset, num, data, fromOffset);
                        if (n <= 0)
                            break outer;
                        len -= n;
                        num -= n;
                        ncopy += n;
                        offset += n;
                        toOffset += n;
                    }
                }
            } else if (toElementType == ScalarType.pvString) {
                PVStringArray pvto = (PVStringArray) to;
                ncopy = from.getLength();
                if (ncopy > len)
                    ncopy = len;
                int num = ncopy;
                String[] toData = new String[1];
                while (num > 0) {
                    toStringArray(from, offset, 1, toData, 0);
                    if (pvto.put(toOffset, 1, toData, 0) <= 0)
                        break;
                    num--;
                    offset++;
                    toOffset++;
                }
            } else if (fromElementType == ScalarType.pvString) {
                PVStringArray pvfrom = (PVStringArray) from;
                outer: while (len > 0) {
                    int num = 0;
                    String[] data = null;
                    int fromOffset = 0;
                    synchronized (stringArrayData) {
                        num = pvfrom.get(offset, len, stringArrayData);
                        data = stringArrayData.data;
                        fromOffset = stringArrayData.offset;
                    }
                    if (num <= 0)
                        break;
                    while (num > 0) {
                        int n = fromStringArray(to, toOffset, num, data,
                                fromOffset);
                        if (n <= 0)
                            break outer;
                        len -= n;
                        num -= n;
                        ncopy += n;
                        offset += n;
                        toOffset += n;
                    }
                }
            }
            return ncopy;
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.epics.pvdata.pv.Convert#isCopyStructureCompatible(org.epics.pvdata
         * .pv.Structure, org.epics.pvdata.pv.Structure)
         */
        @Override
        public boolean isCopyStructureCompatible(Structure fromStruct,
                Structure toStruct) {
            Field[] fromFields = fromStruct.getFields();
            Field[] toFields = toStruct.getFields();
            int length = fromFields.length;
            if (length != toFields.length)
                return false;
            for (int i = 0; i < length; i++) {
                Field from = fromFields[i];
                Field to = toFields[i];
                Type fromType = from.getType();
                Type toType = to.getType();
                if (fromType != toType)
                    return false;
                switch (fromType) {
                case scalar:
                    if (!isCopyScalarCompatible((Scalar) from, (Scalar) to))
                        return false;
                    break;
                case scalarArray:
                    if (!isCopyScalarArrayCompatible((ScalarArray) from,
                            (ScalarArray) to))
                        return false;
                    break;
                case structure:
                    if (!isCopyStructureCompatible((Structure) from,
                            (Structure) to))
                        return false;
                    break;
                case structureArray:
                    if (!isCopyStructureArrayCompatible((StructureArray) from,
                            (StructureArray) to))
                        return false;
                    break;
                }
            }
            return true;
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.epics.pvdata.pv.Convert#copyStructure(org.epics.pvdata.pv.PVStructure
         * , org.epics.pvdata.pv.PVStructure)
         */
        @Override
        public void copyStructure(PVStructure from, PVStructure to) {
            if (to.isImmutable()) {
                if (from.equals(to))
                    return;
                throw new IllegalArgumentException(
                        "Convert.copyStructure destination is immutable");
            }
            if (from == to)
                return;
            PVField[] fromDatas = from.getPVFields();
            PVField[] toDatas = to.getPVFields();
            if (fromDatas.length != toDatas.length) {
                throw new IllegalArgumentException("Illegal copyStructure");
            }
            
            if (fromDatas.length == 2) {
                String[] fromNames = from.getStructure().getFieldNames();
                String[] toNames = to.getStructure().getFieldNames();
                boolean isEnumerated = fromNames[0].equals("index") && toNames[0].equals("index");
                if(isEnumerated && fromNames[1].equals("choices") && toNames[0].equals("choices")) {
                    PVScalar pvScalar = (PVScalar) fromDatas[0];
                    PVScalarArray pvArray = (PVScalarArray) fromDatas[1];

                    PVScalarArray toArray = (PVScalarArray) toDatas[1];
                    copyScalarArray(pvArray, 0, toArray, 0, pvArray
                            .getLength());
                    PVScalar toScalar = (PVScalar) toDatas[0];
                    copyScalar(pvScalar, toScalar);
                    return;
                }
            }
            for (int i = 0; i < fromDatas.length; i++) {
                PVField fromData = fromDatas[i];
                PVField toData = toDatas[i];
                Type fromType = fromData.getField().getType();
                Type toType = toData.getField().getType();
                if (fromType != toType) {
                    throw new IllegalArgumentException("Illegal copyStructure");
                }
                switch (fromType) {
                case scalar:
                    copyScalar((PVScalar) fromData, (PVScalar) toData);
                    break;
                case scalarArray: {
                    PVScalarArray fromArray = (PVScalarArray) fromData;
                    PVScalarArray toArray = (PVScalarArray) toData;
                    int length = copyScalarArray(fromArray, 0, toArray, 0,
                            fromArray.getLength());
                    if (toArray.getLength() != length)
                        toArray.setLength(length);
                    break;
                }
                case structure:
                    copyStructure((PVStructure) fromData, (PVStructure) toData);
                    break;
                case structureArray: {
                    PVStructureArray fromArray = (PVStructureArray) fromData;
                    PVStructureArray toArray = (PVStructureArray) toData;
                    copyStructureArray(fromArray, toArray);
                    break;
                }
                }
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.epics.pvdata.pv.Convert#isCopyStructureArrayCompatible(org.epics
         * .pvData.pv.StructureArray, org.epics.pvdata.pv.StructureArray)
         */
        @Override
        public boolean isCopyStructureArrayCompatible(StructureArray from,
                StructureArray to) {
            return isCopyStructureCompatible(from.getStructure(), to
                    .getStructure());
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.epics.pvdata.pv.Convert#copyStructureArray(org.epics.pvdata.pv
         * .PVStructureArray, org.epics.pvdata.pv.PVStructureArray)
         */
        @Override
        public void copyStructureArray(PVStructureArray from,
                PVStructureArray to) {
            if (to.isImmutable()) {
                if (from.equals(to))
                    return;
                throw new IllegalArgumentException(
                        "Convert.copyStructureArray destination is immutable");
            }
            if (!isCopyStructureCompatible(from.getStructureArray()
                    .getStructure(), to.getStructureArray().getStructure())) {
                throw new IllegalArgumentException(
                        "Convert.copyStructureArray from and to are not compatible");
            }
            PVStructure[] fromArray = null;
            int length = from.getLength();
            synchronized (structureArrayData) {
                from.get(0, length, structureArrayData);
                fromArray = structureArrayData.data;
            }
            PVStructure[] toArray = null;
            if (to.getCapacity() < length)
                to.setCapacity(length);
            synchronized (structureArrayData) {
                to.get(0, length, structureArrayData);
                toArray = structureArrayData.data;
            }
            for (int i = 0; i < length; i++) {
                if (fromArray[i] == null) {
                    toArray[i] = null;
                } else {
                    if (toArray[i] == null) {
                        Structure structure = to.getStructureArray()
                                .getStructure();
                        toArray[i] = pvDataCreate.createPVStructure(structure);
                    }
                    copyStructure(fromArray[i], toArray[i]);
                }
            }
            to.setLength(length);
            to.postPut();
        }
        /*
         * (non-Javadoc)
         * 
         * @see org.epics.pvdata.pv.Convert#toByte(org.epics.pvdata.pv.PVScalar)
         */
        @Override
        public byte toByte(PVScalar pv) {
            ScalarType type = pv.getScalar().getScalarType();
            switch (type) {
            case pvBoolean:
                throw new IllegalArgumentException(
                        "boolean can not be converted to byte");
            case pvByte:
            {
                PVByte value = (PVByte) pv;
                return (byte) value.get();
            }
            case pvShort: 
            {
                PVShort value = (PVShort) pv;
                return (byte) value.get();
            }
            case pvInt: {
                PVInt value = (PVInt) pv;
                return (byte) value.get();
            }
            case pvLong:
            {
                PVLong value = (PVLong) pv;
                return (byte) value.get();
            }
            case pvUByte:
            {
                PVUByte value = (PVUByte) pv;
                return (byte) value.get();
            }
            case pvUShort:
            {
                PVUShort value = (PVUShort) pv;
                return (byte) value.get();
            }
            case pvUInt:
            {
                PVUInt value = (PVUInt) pv;
                return (byte) value.get();
            }
            case pvULong:
            {
                PVULong value = (PVULong) pv;
                return (byte) value.get();
            }
            case pvFloat: {
                PVFloat value = (PVFloat) pv;
                return (byte) value.get();
            }
            case pvDouble: {
                PVDouble value = (PVDouble) pv;
                return (byte) value.get();
            }
            case pvString:
                throw new IllegalArgumentException(
                        "string can not be converted to byte");
            }
            throw new IllegalStateException(
                    "Logic error. Should never get here");
        }
        /*
         * (non-Javadoc)
         * 
         * @see
         * org.epics.pvdata.pv.Convert#toShort(org.epics.pvdata.pv.PVScalar)
         */
        @Override
        public short toShort(PVScalar pv) {
            ScalarType type = pv.getScalar().getScalarType();
            switch (type) {
            case pvBoolean:
                throw new IllegalArgumentException(
                        "boolean can not be converted to short");
            case pvByte: {
                PVByte value = (PVByte) pv;
                return (short) value.get();
            }
            case pvShort: {
                PVShort value = (PVShort) pv;
                return (short) value.get();
            }
            case pvInt: {
                PVInt value = (PVInt) pv;
                return (short) value.get();
            }
            case pvLong: {
                PVLong value = (PVLong) pv;
                return (short) value.get();
            }
            case pvUByte: {
                PVUByte pvval = (PVUByte) pv;
                long value = pvval.get();
                if(value<0) value &= 0x0ffL;
                return (short)value;
            }
            case pvUShort: {
            	PVUShort pvval = (PVUShort) pv;
                return (short)pvval.get();
            }
            case pvUInt: {
            	PVUInt pvval = (PVUInt) pv;
                return (short)pvval.get();
            }
            case pvULong: {
                PVULong pvval = (PVULong) pv;
                return (short)pvval.get();
            }
            case pvFloat: {
                PVFloat value = (PVFloat) pv;
                return (short) value.get();
            }
            case pvDouble: {
                PVDouble value = (PVDouble) pv;
                return (short) value.get();
            }
            case pvString:
                throw new IllegalArgumentException(
                        "string can not be converted to short");
            }
            throw new IllegalStateException(
                    "Logic error. Should never get here");
        }
        /*
         * (non-Javadoc)
         * 
         * @see org.epics.pvdata.pv.Convert#toInt(org.epics.pvdata.pv.PVScalar)
         */
        @Override
        public int toInt(PVScalar pv) {
            ScalarType type = pv.getScalar().getScalarType();
            switch (type) {
            case pvBoolean:
                throw new IllegalArgumentException(
                        "boolean can not be converted to int");
            case pvByte: {
                PVByte value = (PVByte) pv;
                return (int) value.get();
            }
            case pvShort: {
                PVShort value = (PVShort) pv;
                return (int) value.get();
            }
            case pvInt: {
                PVInt value = (PVInt) pv;
                return (int) value.get();
            }
            case pvLong: {
                PVLong value = (PVLong) pv;
                return (int) value.get();
            }
            case pvUByte: {
            	PVUByte pvval = (PVUByte) pv;
            	long value = pvval.get();
                if(value<0) value &= 0x0ffL;
                return (int)value;
            }
            case pvUShort: {
            	PVUShort pvval = (PVUShort) pv;
            	long value = pvval.get();
                if(value<0) value &= 0x0ffffL;
                return (int)value;
            }
            case pvUInt: {
                PVUInt pvval = (PVUInt) pv;
                return (int)pvval.get();
            }
            case pvULong: {
                PVULong pvval = (PVULong) pv;
                return (int)pvval.get();
            }
            case pvFloat: {
                PVFloat value = (PVFloat) pv;
                return (int) value.get();
            }
            case pvDouble: {
                PVDouble value = (PVDouble) pv;
                return (int) value.get();
            }
            case pvString:
                throw new IllegalArgumentException(
                        "string can not be converted to int");
            }
            throw new IllegalStateException(
                    "Logic error. Should never get here");
        }
        /*
         * (non-Javadoc)
         * 
         * @see org.epics.pvdata.pv.Convert#toLong(org.epics.pvdata.pv.PVScalar)
         */
        @Override
        public long toLong(PVScalar pv) {
            ScalarType type = pv.getScalar().getScalarType();
            switch (type) {
            case pvBoolean:
                throw new IllegalArgumentException(
                        "boolean can not be converted to long");
            case pvByte: {
                PVByte value = (PVByte) pv;
                return (long) value.get();
            }
            case pvShort: {
                PVShort value = (PVShort) pv;
                return (long) value.get();
            }
            case pvInt: {
                PVInt value = (PVInt) pv;
                return (long) value.get();
            }
            case pvLong: {
                PVLong value = (PVLong) pv;
                return (long) value.get();
            }
            case pvUByte: {
                PVUByte pvval = (PVUByte) pv;
                long value = pvval.get();
                if(value<0) value &= 0x0ffL;
                return value;
            }
            case pvUShort: {
            	PVUShort pvval = (PVUShort) pv;
            	long value = pvval.get();
                if(value<0) value &= 0x0ffffL;
                return value;
            }
            case pvUInt: {
            	PVUInt pvval = (PVUInt) pv;
            	long value = pvval.get();
                if(value<0) value &= 0x0ffffffffL;
                return value;
            }
            case pvULong: {
                PVULong pvval = (PVULong) pv;
                return pvval.get();
            }
            case pvFloat: {
                PVFloat value = (PVFloat) pv;
                return (long) value.get();
            }
            case pvDouble: {
                PVDouble value = (PVDouble) pv;
                return (long) value.get();
            }
            case pvString:
                throw new IllegalArgumentException(
                        "string can not be converted to long");
            }
            throw new IllegalStateException(
                    "Logic error. Should never get here");
        }
        /*
         * (non-Javadoc)
         * 
         * @see
         * org.epics.pvdata.pv.Convert#toFloat(org.epics.pvdata.pv.PVScalar)
         */
        @Override
        public float toFloat(PVScalar pv) {
            ScalarType type = pv.getScalar().getScalarType();
            switch (type) {
            case pvBoolean:
                throw new IllegalArgumentException(
                        "boolean can not be converted to float");
            case pvByte: {
                PVByte value = (PVByte) pv;
                return (float) value.get();
            }
            case pvShort: {
                PVShort value = (PVShort) pv;
                return (float) value.get();
            }
            case pvInt: {
                PVInt value = (PVInt) pv;
                return (float) value.get();
            }
            case pvLong: {
                PVLong value = (PVLong) pv;
                return (float) value.get();
            }
            case pvUByte: {
                PVUByte pval = (PVUByte) pv;
                float value = pval.get();
                if(value<0) value = -value;
                return value;
            }
            case pvUShort: {
                PVUShort pval = (PVUShort) pv;
                float value = pval.get();
                if(value<0) value = -value;
                return value;
            }
            case pvUInt: {
                PVUInt pval = (PVUInt) pv;
                float value = pval.get();
                if(value<0) value = -value;
                return value;
            }
            case pvULong: {
                PVULong pval = (PVULong) pv;
                float value = pval.get();
                if(value<0) value = -value;
                return value;
            }
            case pvFloat: {
                PVFloat value = (PVFloat) pv;
                return (float) value.get();
            }
            case pvDouble: {
                PVDouble value = (PVDouble) pv;
                return (float) value.get();
            }
            case pvString:
                throw new IllegalArgumentException(
                        "string can not be converted to float");
            }
            throw new IllegalStateException(
                    "Logic error. Should never get here");
        }
        /*
         * (non-Javadoc)
         * 
         * @see
         * org.epics.pvdata.pv.Convert#toDouble(org.epics.pvdata.pv.PVScalar)
         */
        @Override
        public double toDouble(PVScalar pv) {
            ScalarType type = pv.getScalar().getScalarType();
            switch (type) {
            case pvBoolean:
                throw new IllegalArgumentException(
                        "boolean can not be converted to double");
            case pvByte: {
                PVByte value = (PVByte) pv;
                return (double) value.get();
            }
            case pvShort: {
                PVShort value = (PVShort) pv;
                return (double) value.get();
            }
            case pvInt: {
                PVInt value = (PVInt) pv;
                return (double) value.get();
            }
            case pvLong: {
                PVLong value = (PVLong) pv;
                return (double) value.get();
            }
            case pvUByte: {
                PVUByte pval = (PVUByte) pv;
                double value = pval.get();
                if(value<0) value = - value;
                return value;
            }
            case pvUShort: {
                PVUShort pval = (PVUShort) pv;
                double value = pval.get();
                if(value<0) value = - value;
                return value;
            }
            case pvUInt: {
                PVUInt pval = (PVUInt) pv;
                double value = pval.get();
                if(value<0) value = - value;
                return value;
            }
            case pvULong: {
                PVULong pval = (PVULong) pv;
                double value = pval.get();
                if(value<0) value = - value;
                return value;
            }
            case pvFloat: {
                PVFloat value = (PVFloat) pv;
                return (double) value.get();
            }
            case pvDouble: {
                PVDouble value = (PVDouble) pv;
                return (double) value.get();
            }
            case pvString:
                throw new IllegalArgumentException(
                        "string can not be converted to double");
            }
            throw new IllegalStateException(
                    "Logic error. Should never get here");
        }
        /*
         * (non-Javadoc)
         * 
         * @see
         * org.epics.pvdata.pv.Convert#fromByte(org.epics.pvdata.pv.PVScalar,
         * byte)
         */
        @Override
        public void fromByte(PVScalar pv, byte from) {
            ScalarType type = pv.getScalar().getScalarType();
            switch (type) {
            case pvBoolean:
                throw new IllegalArgumentException(
                        "byte can not be converted to boolean");
            case pvByte: {
                PVByte value = (PVByte) pv;
                value.put((byte) from); return;
            }
            case pvShort: {
                PVShort value = (PVShort) pv;
                value.put((short) from); return;
            }
            case pvInt: {
                PVInt value = (PVInt) pv;
                value.put((int) from); return;
            }
            case pvLong: {
                PVLong value = (PVLong) pv;
                value.put((long) from); return;
            }
            case pvUByte: {
                PVUByte pvval = (PVUByte) pv;
                long value = from;
                if(value<0) value = -value;
                pvval.put((byte)value); return;
            }
            case pvUShort: {
                PVUShort pvval = (PVUShort) pv;
                long value = from;
                if(value<0) value = -value;
                pvval.put((short)value); return;
            }
            case pvUInt: {
                PVUInt pvval = (PVUInt) pv;
                long value = from;
                if(value<0) value = -value;
                pvval.put((int)value); return;
            }
            case pvULong: {
                PVULong pvval = (PVULong) pv;
                long value = from;
                if(value<0) value = -value;
                pvval.put(value); return;
            }

            case pvFloat: {
                PVFloat value = (PVFloat) pv;
                value.put(from); return;
            }
            case pvDouble: {
                PVDouble value = (PVDouble) pv;
                value.put(from); return;
            }
            case pvString: {
                PVString value = (PVString) pv;
                value.put(String.valueOf(from)); return;
            }
            }
            throw new IllegalStateException(
                    "Logic error. Should never get here");
        }
        /*
         * (non-Javadoc)
         * 
         * @see
         * org.epics.pvdata.pv.Convert#fromShort(org.epics.pvdata.pv.PVScalar,
         * short)
         */
        @Override
        public void fromShort(PVScalar pv, short from) {
            ScalarType type = pv.getScalar().getScalarType();
            switch (type) {
            case pvBoolean:
                throw new IllegalArgumentException(
                        "short can not be converted to boolean");
            case pvByte: {
                PVByte value = (PVByte) pv;
                value.put((byte) from); return;
            }
            case pvShort: {
                PVShort value = (PVShort) pv;
                value.put((short) from); return;
            }
            case pvInt: {
                PVInt value = (PVInt) pv;
                value.put((int) from); return;
            }
            case pvLong: {
                PVLong value = (PVLong) pv;
                value.put((long) from); return;
            }
            case pvUByte: {
                PVUByte pvval = (PVUByte) pv;
                long value = from;
                if(value<0) value = -value;
                pvval.put((byte)value); return;
            }
            case pvUShort: {
                PVUShort pvval = (PVUShort) pv;
                long value = from;
                if(value<0) value = -value;
                pvval.put((short)value); return;
            }
            case pvUInt: {
                PVUInt pvval = (PVUInt) pv;
                long value = from;
                if(value<0) value = -value;
                pvval.put((int)value); return;
            }
            case pvULong: {
                PVULong pvval = (PVULong) pv;
                long value = from;
                if(value<0) value = -value;
                pvval.put(value); return;
            }
            case pvFloat: {
                PVFloat value = (PVFloat) pv;
                value.put(from); return;
            }
            case pvDouble: {
                PVDouble value = (PVDouble) pv;
                value.put(from); return;
            }
            case pvString: {
                PVString value = (PVString) pv;
                value.put(String.valueOf(from)); return;
            }
            }
            throw new IllegalStateException(
                    "Logic error. Should never get here");
        }
        /*
         * (non-Javadoc)
         * 
         * @see
         * org.epics.pvdata.pv.Convert#fromInt(org.epics.pvdata.pv.PVScalar,
         * int)
         */
        @Override
        public void fromInt(PVScalar pv, int from) {
            ScalarType type = pv.getScalar().getScalarType();
            switch (type) {
            case pvBoolean:
                throw new IllegalArgumentException(
                        "int can not be converted to boolean");
            case pvByte: {
                PVByte value = (PVByte) pv;
                value.put((byte) from); return;
            }
            case pvShort: {
                PVShort value = (PVShort) pv;
                value.put((short) from); return;
            }
            case pvInt: {
                PVInt value = (PVInt) pv;
                value.put((int) from); return;
            }
            case pvLong: {
                PVLong value = (PVLong) pv;
                value.put((long) from); return;
            }
            case pvUByte: {
                PVUByte pvval = (PVUByte) pv;
                long value = from;
                if(value<0) value = -value;
                pvval.put((byte)value); return;
            }
            case pvUShort: {
                PVUShort pvval = (PVUShort) pv;
                long value = from;
                if(value<0) value = -value;
                pvval.put((short)value); return;
            }
            case pvUInt: {
                PVUInt pvval = (PVUInt) pv;
                long value = from;
                if(value<0) value = -value;
                pvval.put((int)value); return;
            }
            case pvULong: {
                PVULong pvval = (PVULong) pv;
                long value = from;
                if(value<0) value = -value;
                pvval.put((long)value); return;
            }
            case pvFloat: {
                PVFloat value = (PVFloat) pv;
                value.put(from); return;
            }
            case pvDouble: {
                PVDouble value = (PVDouble) pv;
                value.put(from); return;
            }
            case pvString: {
                PVString value = (PVString) pv;
                value.put(String.valueOf(from)); return;
            }
            }
            throw new IllegalStateException(
                    "Logic error. Should never get here");
        }
        /*
         * (non-Javadoc)
         * 
         * @see
         * org.epics.pvdata.pv.Convert#fromLong(org.epics.pvdata.pv.PVScalar,
         * long)
         */
        @Override
        public void fromLong(PVScalar pv, long from) {
            ScalarType type = pv.getScalar().getScalarType();
            switch (type) {
            case pvBoolean:
                throw new IllegalArgumentException(
                        "long can not be converted to boolean");
            case pvByte: {
                PVByte value = (PVByte) pv;
                value.put((byte) from); return;
            }
            case pvShort: {
                PVShort value = (PVShort) pv;
                value.put((short) from); return;
            }
            case pvInt: {
                PVInt value = (PVInt) pv;
                value.put((int) from); return;
            }
            case pvLong: {
                PVLong value = (PVLong) pv;
                value.put((long) from); return;
            }
            case pvUByte: {
                PVUByte pvval = (PVUByte) pv;
                long value = from;
                if(value<0) value = -value;
                pvval.put((byte)value); return;
            }
            case pvUShort: {
                PVUShort pvval = (PVUShort) pv;
                long value = from;
                if(value<0) value = -value;
                pvval.put((short)value); return;
            }
            case pvUInt: {
                PVUInt pvval = (PVUInt) pv;
                long value = from;
                if(value<0) value = -value;
                pvval.put((int)value); return;
            }
            case pvULong: {
                PVULong pvval = (PVULong) pv;
                long value = from;
                if(value<0) value = -value;
                pvval.put((long)value); return;
            }
            case pvFloat: {
                PVFloat value = (PVFloat) pv;
                value.put((float) from); return;
            }
            case pvDouble: {
                PVDouble value = (PVDouble) pv;
                value.put((double) from); return;
            }
            case pvString: {
                PVString value = (PVString) pv;
                value.put(String.valueOf(from)); return;
            }
            }
            throw new IllegalStateException(
                    "Logic error. Should never get here");
        }
        @Override
        public void fromUByte(PVScalar pv, byte xxx)
        {
            short from = xxx;
            if(xxx<0) from = (short)-from;
            ScalarType type = pv.getScalar().getScalarType();
            switch (type) {
            case pvBoolean:
                throw new IllegalArgumentException(
                        "byte can not be converted to boolean");
            case pvByte: {
                PVByte value = (PVByte) pv;
                value.put((byte) from); return;
            }
            case pvShort: {
                PVShort value = (PVShort) pv;
                value.put((short)from); return;
            }
            case pvInt: {
                PVInt value = (PVInt) pv;
                value.put((int)from); return;
            }
            case pvLong: {
                PVLong value = (PVLong) pv;
                value.put((long) from); return;
            }
            case pvUByte: {
                PVUByte value = (PVUByte) pv;
                value.put((byte) from); return;
            }
            case pvUShort: {
                PVUShort value = (PVUShort) pv;
                value.put((short) from); return;
            }
            case pvUInt: {
                PVUInt value = (PVUInt) pv;
                value.put((int) from); return;
            }
            case pvULong: {
                PVULong value = (PVULong) pv;
                value.put((long) from); return;
            }

            case pvFloat: {
                PVFloat value = (PVFloat) pv;
                value.put((float) from); return;
            }
            case pvDouble: {
                PVDouble value = (PVDouble) pv;
                value.put((double) from); return;
            }
            case pvString: {
                PVString value = (PVString) pv;
                String val = Long.toString(from);
                if(from<0) val = val.substring(1);
                value.put(val); return;
            }
            }
            throw new IllegalStateException(
                    "Logic error. Should never get here");
        }
        @Override
        public void fromUShort(PVScalar pv, short xxx) {
            int from = xxx;
            if(xxx<0) from = -from;
            ScalarType type = pv.getScalar().getScalarType();
            switch (type) {
            case pvBoolean:
                throw new IllegalArgumentException(
                        "byte can not be converted to boolean");
            case pvByte: {
                PVByte value = (PVByte) pv;
                value.put((byte) from); return;
            }
            case pvShort: {
                PVShort value = (PVShort) pv;
                value.put((short)from); return;
            }
            case pvInt: {
                PVInt value = (PVInt) pv;
                value.put((int)from); return;
            }
            case pvLong: {
                PVLong value = (PVLong) pv;
                value.put((long) from); return;
            }
            case pvUByte: {
                PVUByte value = (PVUByte) pv;
                value.put((byte) from); return;
            }
            case pvUShort: {
                PVUShort value = (PVUShort) pv;
                value.put((short) from); return;
            }
            case pvUInt: {
                PVUInt value = (PVUInt) pv;
                value.put((int) from); return;
            }
            case pvULong: {
                PVULong value = (PVULong) pv;
                value.put((long) from); return;
            }

            case pvFloat: {
                PVFloat value = (PVFloat) pv;
                value.put((float) from); return;
            }
            case pvDouble: {
                PVDouble value = (PVDouble) pv;
                value.put((double) from); return;
            }
            case pvString: {
                PVString value = (PVString) pv;
                String val = Long.toString(from);
                if(from<0) val = val.substring(1);
                value.put(val); return;
            }
            }
            throw new IllegalStateException(
                    "Logic error. Should never get here");
        }
        @Override
        public void fromUInt(PVScalar pv, int xxx) {
            long from = xxx;
            if(xxx<0) from = -from;
            ScalarType type = pv.getScalar().getScalarType();
            switch (type) {
            case pvBoolean:
                throw new IllegalArgumentException(
                        "byte can not be converted to boolean");
            case pvByte: {
                PVByte value = (PVByte) pv;
                value.put((byte) from); return;
            }
            case pvShort: {
                PVShort value = (PVShort) pv;
                value.put((short)from); return;
            }
            case pvInt: {
                PVInt value = (PVInt) pv;
                value.put((int)from); return;
            }
            case pvLong: {
                PVLong value = (PVLong) pv;
                value.put((long) from); return;
            }
            case pvUByte: {
                PVUByte value = (PVUByte) pv;
                value.put((byte) from); return;
            }
            case pvUShort: {
                PVUShort value = (PVUShort) pv;
                value.put((short) from); return;
            }
            case pvUInt: {
                PVUInt value = (PVUInt) pv;
                value.put((int) from); return;
            }
            case pvULong: {
                PVULong value = (PVULong) pv;
                value.put((long) from); return;
            }

            case pvFloat: {
                PVFloat value = (PVFloat) pv;
                value.put((float) from); return;
            }
            case pvDouble: {
                PVDouble value = (PVDouble) pv;
                value.put((double) from); return;
            }
            case pvString: {
                PVString value = (PVString) pv;
                String val = Long.toString(from);
                if(from<0) val = val.substring(1);
                value.put(val); return;
            }
            }
            throw new IllegalStateException(
                    "Logic error. Should never get here");
        }
        @Override
        public void fromULong(PVScalar pv, long from) {
            ScalarType type = pv.getScalar().getScalarType();
            switch (type) {
            case pvBoolean:
                throw new IllegalArgumentException(
                        "byte can not be converted to boolean");
            case pvByte: {
                PVByte value = (PVByte) pv;
                value.put((byte) from); return;
            }
            case pvShort: {
                PVShort value = (PVShort) pv;
                value.put((short)from); return;
            }
            case pvInt: {
                PVInt value = (PVInt) pv;
                value.put((int)from); return;
            }
            case pvLong: {
                PVLong value = (PVLong) pv;
                value.put((long) from); return;
            }
            case pvUByte: {
                PVUByte value = (PVUByte) pv;
                value.put((byte) from); return;
            }
            case pvUShort: {
                PVUShort value = (PVUShort) pv;
                value.put((short) from); return;
            }
            case pvUInt: {
                PVUInt value = (PVUInt) pv;
                value.put((int) from); return;
            }
            case pvULong: {
                PVULong value = (PVULong) pv;
                value.put((long) from); return;
            }

            case pvFloat: {
                PVFloat value = (PVFloat) pv;
                value.put((float) from); return;
            }
            case pvDouble: {
                PVDouble value = (PVDouble) pv;
                value.put((double) from); return;
            }
            case pvString: {
                PVString value = (PVString) pv;
                String val = Long.toString(from);
                if(from<0) val = val.substring(1);
                value.put(val); return;
            }
            }
            throw new IllegalStateException(
                    "Logic error. Should never get here");
        }
        /*
         * (non-Javadoc)
         * 
         * @see
         * org.epics.pvdata.pv.Convert#fromFloat(org.epics.pvdata.pv.PVScalar,
         * float)
         */
        @Override
        public void fromFloat(PVScalar pv, float from) {
            ScalarType type = pv.getScalar().getScalarType();
            switch (type) {
            case pvBoolean:
                throw new IllegalArgumentException(
                        "float can not be converted to boolean");
            case pvByte: {
                PVByte value = (PVByte) pv;
                value.put((byte) from); return;
            }
            case pvShort: {
                PVShort value = (PVShort) pv;
                value.put((short) from); return;
            }
            case pvInt: {
                PVInt value = (PVInt) pv;
                value.put((int) from); return;
            }
            case pvLong: {
                PVLong value = (PVLong) pv;
                value.put((long) from); return;
            }
            case pvUByte: {
            	if(from<0) from = -from;
                PVUByte value = (PVUByte) pv;
                value.put((byte)from); return;
            }
            case pvUShort: {
                if(from<0) from = -from;
                PVUShort value = (PVUShort) pv;
                value.put((short)from); return;
            }
            case pvUInt: {
                if(from<0) from = -from;
                PVUInt value = (PVUInt) pv;
                value.put((int)from); return;
            }
            case pvULong: {
                if(from<0) from = -from;
                PVULong value = (PVULong) pv;
                value.put((long) from); return;
            }
            case pvFloat: {
                PVFloat value = (PVFloat) pv;
                value.put((float) from); return;
            }
            case pvDouble: {
                PVDouble value = (PVDouble) pv;
                value.put((double) from); return;
            }
            case pvString: {
                PVString value = (PVString) pv;
                value.put(String.valueOf(from)); return;
            }
            }
            throw new IllegalStateException(
                    "Logic error. Should never get here");
        }
        /*
         * (non-Javadoc)
         * 
         * @see
         * org.epics.pvdata.pv.Convert#fromDouble(org.epics.pvdata.pv.PVScalar,
         * double)
         */
        @Override
        public void fromDouble(PVScalar pv, double from) {
            ScalarType type = pv.getScalar().getScalarType();
            switch (type) {
            case pvBoolean:
                throw new IllegalArgumentException(
                        "double can not be converted to boolean");
            case pvByte: {
                PVByte value = (PVByte) pv;
                value.put((byte) from); return;
            }
            case pvShort: {
                PVShort value = (PVShort) pv;
                value.put((short) from); return;
            }
            case pvInt: {
                PVInt value = (PVInt) pv;
                value.put((int) from); return;
            }
            case pvLong: {
                PVLong value = (PVLong) pv;
                value.put((long) from); return;
            }
            case pvUByte: {
                if(from<0) from = -from;
                PVUByte value = (PVUByte) pv;
                value.put((byte)from); return;
            }
            case pvUShort: {
                if(from<0) from = -from;
                PVUShort value = (PVUShort) pv;
                value.put((short)from); return;
            }
            case pvUInt: {
                if(from<0) from = -from;
                PVUInt value = (PVUInt) pv;
                value.put((int)from); return;
            }
            case pvULong: {
                if(from<0) from = -from;
                PVULong value = (PVULong) pv;
                value.put((long) from); return;
            }
            case pvFloat: {
                PVFloat value = (PVFloat) pv;
                value.put((float) from); return;
            }
            case pvDouble: {
                PVDouble value = (PVDouble) pv;
                value.put((double) from); return;
            }
            case pvString: {
                PVString value = (PVString) pv;
                value.put(String.valueOf(from)); return;
            }
            }
            throw new IllegalStateException(
                    "Logic error. Should never get here");
        }
        /*
         * (non-Javadoc)
         * 
         * @see
         * org.epics.pvdata.pv.Convert#toByteArray(org.epics.pvdata.pv.PVScalarArray
         * , int, int, byte[], int)
         */
        @Override
        public int toByteArray(PVScalarArray pv, int offset, int len,
                byte[] to, int toOffset) {
            return convertToByteArray(pv, offset, len, to, toOffset);
        }
        /*
         * (non-Javadoc)
         * 
         * @seeorg.epics.pvdata.pv.Convert#toShortArray(org.epics.pvdata.pv.
         * PVScalarArray, int, int, short[], int)
         */
        @Override
        public int toShortArray(PVScalarArray pv, int offset, int len,
                short[] to, int toOffset) {
            return convertToShortArray(pv, offset, len, to, toOffset);
        }
        /*
         * (non-Javadoc)
         * 
         * @see
         * org.epics.pvdata.pv.Convert#toIntArray(org.epics.pvdata.pv.PVScalarArray
         * , int, int, int[], int)
         */
        @Override
        public int toIntArray(PVScalarArray pv, int offset, int len, int[] to,
                int toOffset) {
            return convertToIntArray(pv, offset, len, to, toOffset);
        }
        /*
         * (non-Javadoc)
         * 
         * @see
         * org.epics.pvdata.pv.Convert#toLongArray(org.epics.pvdata.pv.PVScalarArray
         * , int, int, long[], int)
         */
        @Override
        public int toLongArray(PVScalarArray pv, int offset, int len,
                long[] to, int toOffset) {
            return convertToLongArray(pv, offset, len, to, toOffset);
        }
        /*
         * (non-Javadoc)
         * 
         * @seeorg.epics.pvdata.pv.Convert#toFloatArray(org.epics.pvdata.pv.
         * PVScalarArray, int, int, float[], int)
         */
        @Override
        public int toFloatArray(PVScalarArray pv, int offset, int len,
                float[] to, int toOffset) {
            return convertToFloatArray(pv, offset, len, to, toOffset);
        }
        /*
         * (non-Javadoc)
         * 
         * @seeorg.epics.pvdata.pv.Convert#toDoubleArray(org.epics.pvdata.pv.
         * PVScalarArray, int, int, double[], int)
         */
        @Override
        public int toDoubleArray(PVScalarArray pv, int offset, int len,
                double[] to, int toOffset) {
            return convertToDoubleArray(pv, offset, len, to, toOffset);
        }
        /*
         * (non-Javadoc)
         * 
         * @seeorg.epics.pvdata.pv.Convert#fromByteArray(org.epics.pvdata.pv.
         * PVScalarArray, int, int, byte[], int)
         */
        @Override
        public int fromByteArray(PVScalarArray pv, int offset, int len,
                byte[] from, int fromOffset) {
            int num = convertFromByteArray(pv, offset, len, from, fromOffset);
            return num;
        }
        /*
         * (non-Javadoc)
         * 
         * @seeorg.epics.pvdata.pv.Convert#fromShortArray(org.epics.pvdata.pv.
         * PVScalarArray, int, int, short[], int)
         */
        @Override
        public int fromShortArray(PVScalarArray pv, int offset, int len,
                short[] from, int fromOffset) {
            int num = convertFromShortArray(pv, offset, len, from, fromOffset);
            return num;
        }
        /*
         * (non-Javadoc)
         * 
         * @seeorg.epics.pvdata.pv.Convert#fromIntArray(org.epics.pvdata.pv.
         * PVScalarArray, int, int, int[], int)
         */
        @Override
        public int fromIntArray(PVScalarArray pv, int offset, int len,
                int[] from, int fromOffset) {
            int num = convertFromIntArray(pv, offset, len, from, fromOffset);
            return num;
        }
        /*
         * (non-Javadoc)
         * 
         * @seeorg.epics.pvdata.pv.Convert#fromLongArray(org.epics.pvdata.pv.
         * PVScalarArray, int, int, long[], int)
         */
        @Override
        public int fromLongArray(PVScalarArray pv, int offset, int len,
                long[] from, int fromOffset) {
            int num = convertFromLongArray(pv, offset, len, from, fromOffset);
            return num;
        }
        /*
         * (non-Javadoc)
         * 
         * @seeorg.epics.pvdata.pv.Convert#fromUByteArray(org.epics.pvdata.pv.
         * PVScalarArray, int, int, byte[], int)
         */
        @Override
        public int fromUByteArray(PVScalarArray pv, int offset, int len,
                byte[] from, int fromOffset) {
            int num = convertFromUByteArray(pv, offset, len, from, fromOffset);
            return num;
        }
        /*
         * (non-Javadoc)
         * 
         * @seeorg.epics.pvdata.pv.Convert#fromUShortArray(org.epics.pvdata.pv.
         * PVScalarArray, int, int, short[], int)
         */
        @Override
        public int fromUShortArray(PVScalarArray pv, int offset, int len,
                short[] from, int fromOffset) {
            int num = convertFromUShortArray(pv, offset, len, from, fromOffset);
            return num;
        }
        /*
         * (non-Javadoc)
         * 
         * @seeorg.epics.pvdata.pv.Convert#fromUIntArray(org.epics.pvdata.pv.
         * PVScalarArray, int, int, int[], int)
         */
        @Override
        public int fromUIntArray(PVScalarArray pv, int offset, int len,
                int[] from, int fromOffset) {
            int num = convertFromUIntArray(pv, offset, len, from, fromOffset);
            return num;
        }
        /*
         * (non-Javadoc)
         * 
         * @seeorg.epics.pvdata.pv.Convert#fromULongArray(org.epics.pvdata.pv.
         * PVScalarArray, int, int, long[], int)
         */
        @Override
        public int fromULongArray(PVScalarArray pv, int offset, int len,
                long[] from, int fromOffset) {
            int num = convertFromULongArray(pv, offset, len, from, fromOffset);
            return num;
        }
        /*
         * (non-Javadoc)
         * 
         * @seeorg.epics.pvdata.pv.Convert#fromFloatArray(org.epics.pvdata.pv.
         * PVScalarArray, int, int, float[], int)
         */
        @Override
        public int fromFloatArray(PVScalarArray pv, int offset, int len,
                float[] from, int fromOffset) {
            int num = convertFromFloatArray(pv, offset, len, from, fromOffset);
            return num;
        }
        /*
         * (non-Javadoc)
         * 
         * @seeorg.epics.pvdata.pv.Convert#fromDoubleArray(org.epics.pvdata.pv.
         * PVScalarArray, int, int, double[], int)
         */
        @Override
        public int fromDoubleArray(PVScalarArray pv, int offset, int len,
                double[] from, int fromOffset) {
            int num = convertFromDoubleArray(pv, offset, len, from, fromOffset);
            return num;
        }
        /* (non-Javadoc)
         * @see org.epics.pvdata.pv.Convert#toString(org.epics.pvdata.pv.PVScalar)
         */
        @Override
        public String toString(PVScalar pv) {
            ScalarType type = pv.getScalar().getScalarType();
            switch (type) {
            case pvBoolean: {
                PVBoolean value = (PVBoolean)pv;
                boolean val = value.get();
                return val ? "true" : "false";
            }
            case pvByte: {
                PVByte value = (PVByte) pv;
                return String.valueOf(value.get());
            }
            case pvShort: {
                PVShort value = (PVShort) pv;
                return String.valueOf(value.get());
            }
            case pvInt: {
                PVInt value = (PVInt) pv;
                return String.valueOf(value.get());
            }
            case pvLong: {
                PVLong value = (PVLong) pv;
                return String.valueOf(value.get());
            }
            case pvUByte: {
                PVUByte arg = (PVUByte) pv;
                int value = arg.get();
                if(value<0) value = -value;
                return String.valueOf(value);
            }
            case pvUShort: {
                PVUShort arg = (PVUShort) pv;
                int value = arg.get();
                if(value<0) value = -value;
                return String.valueOf(value);
            }
            case pvUInt: {
                PVUInt arg = (PVUInt) pv;
                long value = arg.get();
                if(value<0) value = -value;
                return String.valueOf(value);
            }
            case pvULong: {
                PVULong pvLong = (PVULong) pv;
                long val = pvLong.get();
                String value = String.valueOf(val);
                if(val<0) value = value.substring(1);
                return value;
            }
            case pvFloat: {
                PVFloat value = (PVFloat) pv;
                return String.valueOf(value.get());
            }
            case pvDouble: {
                PVDouble value = (PVDouble) pv;
                return String.valueOf(value.get());
            }
            case pvString: {
                PVString value = (PVString)pv;
                return value.get();
            }
            }
            throw new IllegalStateException(
                    "Logic error. Should never get here");

        }
        
        private int convertFromByteArray(PVScalarArray pv, int offset, int len,
                byte[] from, int fromOffset) {
            ScalarType elemType = pv.getScalarArray().getElementType();
            int ntransfered = 0;
            switch (elemType) {
            case pvBoolean:
                throw new IllegalArgumentException(
                        "convert from byte[] to BooleanArray not legal");
            case pvByte: 
            {
                PVByteArray pvdata = (PVByteArray) pv;
                while (len > 0) {
                    int n = pvdata.put(offset, len, from, fromOffset);
                    if (n == 0)
                        break;
                    len -= n;
                    offset += n;
                    fromOffset += n;
                    ntransfered += n;
                }
                return ntransfered;
            }
            case pvShort: {
                PVShortArray pvdata = (PVShortArray) pv;
                short[] data = new short[1];
                while (len > 0) {
                    data[0] = (short) from[fromOffset];
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvInt: {
                PVIntArray pvdata = (PVIntArray) pv;
                int[] data = new int[1];
                while (len > 0) {
                    data[0] = (int) from[fromOffset];
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvLong: {
                PVLongArray pvdata = (PVLongArray) pv;
                long[] data = new long[1];
                while (len > 0) {
                    data[0] = (long) from[fromOffset];
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvUByte: 
            {
                PVUByteArray pvdata = (PVUByteArray) pv;
                while (len > 0) {
                    int n = pvdata.put(offset, len, from, fromOffset);
                    if (n == 0)
                        break;
                    len -= n;
                    offset += n;
                    fromOffset += n;
                    ntransfered += n;
                }
                return ntransfered;
            }
            case pvUShort: {
                PVUShortArray pvdata = (PVUShortArray) pv;
                short[] data = new short[1];
                while (len > 0) {
                	int value = from[fromOffset];
                	if(value<0) value = -value;
                    data[0] = (short)value;
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvUInt: {
                PVUIntArray pvdata = (PVUIntArray) pv;
                int[] data = new int[1];
                while (len > 0) {
                    int value = from[fromOffset];
                    if(value<0) value = -value;
                    data[0] = value;
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvULong: {
                PVULongArray pvdata = (PVULongArray) pv;
                long[] data = new long[1];
                while (len > 0) {
                    long value = from[fromOffset];
                    if(value<0) value = -value;
                    data[0] = value;
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvFloat: {
                PVFloatArray pvdata = (PVFloatArray) pv;
                float[] data = new float[1];
                while (len > 0) {
                    data[0] = from[fromOffset];
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvDouble: {
                PVDoubleArray pvdata = (PVDoubleArray) pv;
                double[] data = new double[1];
                while (len > 0) {
                    data[0] = from[fromOffset];
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvString:
                throw new IllegalArgumentException(
                        "convert from byte[] to StringArray not legal");
            }
            throw new IllegalStateException(
                    "Logic error. Should never get here");
        }
        
        private int convertFromUByteArray(PVScalarArray pv, int offset, int len,
                byte[] from, int fromOffset) {
            ScalarType elemType = pv.getScalarArray().getElementType();
            int ntransfered = 0;
            switch (elemType) {
            case pvBoolean:
                throw new IllegalArgumentException(
                        "convert from byte[] to BooleanArray not legal");
            case pvByte: 
            {
                PVByteArray pvdata = (PVByteArray) pv;
                while (len > 0) {
                    int n = pvdata.put(offset, len, from, fromOffset);
                    if (n == 0)
                        break;
                    len -= n;
                    offset += n;
                    fromOffset += n;
                    ntransfered += n;
                }
                return ntransfered;
            }
            case pvShort: {
                PVShortArray pvdata = (PVShortArray) pv;
                short[] data = new short[1];
                while (len > 0) {
                    short value = from[fromOffset];
                    if(value<0) value = (short)-value;
                    data[0] = value;
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvInt: {
                PVIntArray pvdata = (PVIntArray) pv;
                int[] data = new int[1];
                while (len > 0) {
                    int value = from[fromOffset];
                    if(value<0) value = -value;
                    data[0] = value;
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvLong: {
                PVLongArray pvdata = (PVLongArray) pv;
                long[] data = new long[1];
                while (len > 0) {
                    long value = from[fromOffset];
                    if(value<0) value = -value;
                    data[0] = value;
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvUByte: 
            {
                PVUByteArray pvdata = (PVUByteArray) pv;
                while (len > 0) {
                    int n = pvdata.put(offset, len, from, fromOffset);
                    if (n == 0)
                        break;
                    len -= n;
                    offset += n;
                    fromOffset += n;
                    ntransfered += n;
                }
                return ntransfered;
            }
            case pvUShort: {
                PVUShortArray pvdata = (PVUShortArray) pv;
                short[] data = new short[1];
                while (len > 0) {
                    byte val = from[fromOffset];
                    short value = val;
                    if(val<0) value &=0x0ff;
                    data[0] = value;
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvUInt: {
                PVUIntArray pvdata = (PVUIntArray) pv;
                int[] data = new int[1];
                while (len > 0) {
                    byte val = from[fromOffset];
                    short value = val;
                    if(val<0) value &=0x0ff;
                    data[0] = value;
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvULong: {
                PVULongArray pvdata = (PVULongArray) pv;
                long[] data = new long[1];
                while (len > 0) {
                    byte val = from[fromOffset];
                    short value = val;
                    if(val<0) value &=0x0ff;
                    data[0] = value;
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvFloat: {
                PVFloatArray pvdata = (PVFloatArray) pv;
                float[] data = new float[1];
                while (len > 0) {
                    float value = from[fromOffset];
                    if(value<0) value = -value;
                    data[0] = value;
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvDouble: {
                PVDoubleArray pvdata = (PVDoubleArray) pv;
                double[] data = new double[1];
                while (len > 0) {
                    double value = from[fromOffset];
                    if(value<0) value = -value;
                    data[0] = value;;
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvString:
                throw new IllegalArgumentException(
                        "convert from byte[] to StringArray not legal");
            }
            throw new IllegalStateException(
                    "Logic error. Should never get here");
        }
        
        private int convertToByteArray(PVScalarArray pv, int offset, int len,
                byte[] to, int toOffset) {
            ScalarType elemType = pv.getScalarArray().getElementType();
            int ntransfered = 0;
            switch (elemType) {
            case pvBoolean:
                throw new IllegalArgumentException(
                        "convert BooleanArray to byte[]] not legal");
            case pvByte: 
            {
                PVByteArray pvdata = (PVByteArray) pv;
                while (len > 0) {
                    int num = 0;
                    byte[] dataArray = null;
                    int dataOffset = 0;
                    synchronized (byteArrayData) {
                        num = pvdata.get(offset, len, byteArrayData);
                        dataArray = byteArrayData.data;
                        dataOffset = byteArrayData.offset;
                    }
                    if (num <= 0)
                        break;
                    System.arraycopy(dataArray, dataOffset, to, toOffset, num);
                    len -= num;
                    offset += num;
                    toOffset += num;
                    ntransfered += num;
                }
                return ntransfered;
            }
            case pvShort: 
            {
                PVShortArray pvdata = (PVShortArray) pv;
                ShortArrayData data = new ShortArrayData();
                while (len > 0) {
                    int num = pvdata.get(offset, len, data);
                    if (num == 0)
                        break;
                    short[] dataArray = data.data;
                    int dataOffset = data.offset;
                    for (int i = 0; i < num; i++)
                        to[i + toOffset] = (byte) dataArray[i + dataOffset];
                    len -= num;
                    offset += num;
                    toOffset += num;
                    ntransfered += num;
                }
                return ntransfered;
            }
            case pvInt: 
            {
                PVIntArray pvdata = (PVIntArray) pv;
                IntArrayData data = new IntArrayData();
                while (len > 0) {
                    int num = pvdata.get(offset, len, data);
                    if (num == 0)
                        break;
                    int[] dataArray = data.data;
                    int dataOffset = data.offset;
                    for (int i = 0; i < num; i++)
                        to[i + toOffset] = (byte) dataArray[i + dataOffset];
                    len -= num;
                    offset += num;
                    toOffset += num;
                    ntransfered += num;
                }
                return ntransfered;
            }
            case pvLong:
            {
                PVLongArray pvdata = (PVLongArray) pv;
                LongArrayData data = new LongArrayData();
                while (len > 0) {
                    int num = pvdata.get(offset, len, data);
                    if (num == 0)
                        break;
                    long[] dataArray = data.data;
                    int dataOffset = data.offset;
                    for (int i = 0; i < num; i++)
                        to[i + toOffset] = (byte) dataArray[i + dataOffset];
                    len -= num;
                    offset += num;
                    toOffset += num;
                    ntransfered += num;
                }
                return ntransfered;
            }
            case pvUByte:
            {
                PVUByteArray pvdata = (PVUByteArray) pv;
                while (len > 0) {
                    int num = 0;
                    byte[] dataArray = null;
                    int dataOffset = 0;
                    synchronized (byteArrayData) {
                        num = pvdata.get(offset, len, byteArrayData);
                        dataArray = byteArrayData.data;
                        dataOffset = byteArrayData.offset;
                    }
                    if (num <= 0)
                        break;
                    System.arraycopy(dataArray, dataOffset, to, toOffset, num);
                    len -= num;
                    offset += num;
                    toOffset += num;
                    ntransfered += num;
                }
                return ntransfered;
            }
            case pvUShort:
            {
                PVUShortArray pvdata = (PVUShortArray) pv;
                ShortArrayData data = new ShortArrayData();
                while (len > 0) {
                    int num = pvdata.get(offset, len, data);
                    if (num == 0)
                        break;
                    short[] dataArray = data.data;
                    int dataOffset = data.offset;
                    for (int i = 0; i < num; i++)
                        to[i + toOffset] = (byte) dataArray[i + dataOffset];
                    len -= num;
                    offset += num;
                    toOffset += num;
                    ntransfered += num;
                }
                return ntransfered;
            }
            case pvUInt:
            {
                PVUIntArray pvdata = (PVUIntArray) pv;
                IntArrayData data = new IntArrayData();
                while (len > 0) {
                    int num = pvdata.get(offset, len, data);
                    if (num == 0)
                        break;
                    int[] dataArray = data.data;
                    int dataOffset = data.offset;
                    for (int i = 0; i < num; i++)
                        to[i + toOffset] = (byte) dataArray[i + dataOffset];
                    len -= num;
                    offset += num;
                    toOffset += num;
                    ntransfered += num;
                }
                return ntransfered;
            }
            case pvULong:
            {
                PVULongArray pvdata = (PVULongArray) pv;
                LongArrayData data = new LongArrayData();
                while (len > 0) {
                    int num = pvdata.get(offset, len, data);
                    if (num == 0)
                        break;
                    long[] dataArray = data.data;
                    int dataOffset = data.offset;
                    for (int i = 0; i < num; i++)
                        to[i + toOffset] = (byte) dataArray[i + dataOffset];
                    len -= num;
                    offset += num;
                    toOffset += num;
                    ntransfered += num;
                }
                return ntransfered;
            }
            case pvFloat: {
                PVFloatArray pvdata = (PVFloatArray) pv;
                FloatArrayData data = new FloatArrayData();
                while (len > 0) {
                    int num = pvdata.get(offset, len, data);
                    if (num == 0)
                        break;
                    float[] dataArray = data.data;
                    int dataOffset = data.offset;
                    for (int i = 0; i < num; i++)
                        to[i + toOffset] = (byte) dataArray[i + dataOffset];
                    len -= num;
                    offset += num;
                    toOffset += num;
                    ntransfered += num;
                }
                return ntransfered;
            }
            case pvDouble: {
                PVDoubleArray pvdata = (PVDoubleArray) pv;
                DoubleArrayData data = new DoubleArrayData();
                while (len > 0) {
                    int num = pvdata.get(offset, len, data);
                    if (num == 0)
                        break;
                    double[] dataArray = data.data;
                    int dataOffset = data.offset;
                    for (int i = 0; i < num; i++)
                        to[i + toOffset] = (byte) dataArray[i + dataOffset];
                    len -= num;
                    offset += num;
                    toOffset += num;
                    ntransfered += num;
                }
                return ntransfered;
            }
            case pvString:
                throw new IllegalArgumentException(
                        "convert StringArray to byte[]] not legal");
            }
            throw new IllegalStateException(
                    "Logic error. Should never get here");

        }    
        
        private int convertFromShortArray(PVScalarArray pv, int offset,
                int len, short[] from, int fromOffset) {
            ScalarType elemType = pv.getScalarArray().getElementType();
            int ntransfered = 0;
            switch (elemType) {
            case pvBoolean:
                throw new IllegalArgumentException(
                        "convert from short[] to BooleanArray not legal");
            case pvByte: {
                PVByteArray pvdata = (PVByteArray) pv;
                byte[] data = new byte[1];
                while (len > 0) {
                    data[0] = (byte) from[fromOffset];
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvShort: {
                PVShortArray pvdata = (PVShortArray) pv;
                while (len > 0) {
                    int n = pvdata.put(offset, len, from, fromOffset);
                    if (n == 0)
                        break;
                    len -= n;
                    offset += n;
                    fromOffset += n;
                    ntransfered += n;
                }
                return ntransfered;
            }
            case pvInt: {
                PVIntArray pvdata = (PVIntArray) pv;
                int[] data = new int[1];
                while (len > 0) {
                    data[0] = (int) from[fromOffset];
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvLong: {
                PVLongArray pvdata = (PVLongArray) pv;
                long[] data = new long[1];
                while (len > 0) {
                    data[0] = (long) from[fromOffset];
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvUByte: {
                PVUByteArray pvdata = (PVUByteArray) pv;
                byte[] data = new byte[1];
                while (len > 0) {
                    data[0] = (byte) from[fromOffset];
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvUShort: {
                PVUShortArray pvdata = (PVUShortArray) pv;
                while (len > 0) {
                    int n = pvdata.put(offset, len, from, fromOffset);
                    if (n == 0)
                        break;
                    len -= n;
                    offset += n;
                    fromOffset += n;
                    ntransfered += n;
                }
                return ntransfered;
            }
            case pvUInt: {
                PVUIntArray pvdata = (PVUIntArray) pv;
                int[] data = new int[1];
                while (len > 0) {
                	short val = from[fromOffset];
                	int value = val;
                	if(val<0) value &=0x0ffff;
                    data[0] = value;
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvULong: {
                PVULongArray pvdata = (PVULongArray) pv;
                long[] data = new long[1];
                while (len > 0) {
                	short val = from[fromOffset];
                	int value = val;
                	if(val<0) value &=0x0ffff;
                    data[0] = value;
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvFloat: {
                PVFloatArray pvdata = (PVFloatArray) pv;
                float[] data = new float[1];
                while (len > 0) {
                    data[0] = from[fromOffset];
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvDouble: {
                PVDoubleArray pvdata = (PVDoubleArray) pv;
                double[] data = new double[1];
                while (len > 0) {
                	data[0] = from[fromOffset];
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvString:
                throw new IllegalArgumentException(
                        "convert from short[] to StringArray not legal");
            }
            throw new IllegalStateException(
                    "Logic error. Should never get here");
        }
        
        private int convertFromUShortArray(PVScalarArray pv, int offset,
                int len, short[] from, int fromOffset) {
            ScalarType elemType = pv.getScalarArray().getElementType();
            int ntransfered = 0;
            switch (elemType) {
            case pvBoolean:
                throw new IllegalArgumentException(
                        "convert from short[] to BooleanArray not legal");
            case pvByte: {
                PVByteArray pvdata = (PVByteArray) pv;
                byte[] data = new byte[1];
                while (len > 0) {
                    data[0] = (byte) from[fromOffset];
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvShort: {
                PVShortArray pvdata = (PVShortArray) pv;
                while (len > 0) {
                    int n = pvdata.put(offset, len, from, fromOffset);
                    if (n == 0)
                        break;
                    len -= n;
                    offset += n;
                    fromOffset += n;
                    ntransfered += n;
                }
                return ntransfered;
            }
            case pvInt: {
                PVIntArray pvdata = (PVIntArray) pv;
                int[] data = new int[1];
                while (len > 0) {
                    int value = from[fromOffset];
                    if(value<0) value = -value;
                    data[0] = value;
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvLong: {
                PVLongArray pvdata = (PVLongArray) pv;
                long[] data = new long[1];
                while (len > 0) {
                    long value = from[fromOffset];
                    if(value<0) value = -value;
                    data[0] = value;
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvUByte: {
                PVUByteArray pvdata = (PVUByteArray) pv;
                byte[] data = new byte[1];
                while (len > 0) {
                    data[0] = (byte) from[fromOffset];
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvUShort: {
                PVUShortArray pvdata = (PVUShortArray) pv;
                while (len > 0) {
                    int n = pvdata.put(offset, len, from, fromOffset);
                    if (n == 0)
                        break;
                    len -= n;
                    offset += n;
                    fromOffset += n;
                    ntransfered += n;
                }
                return ntransfered;
            }
            case pvUInt: {
                PVUIntArray pvdata = (PVUIntArray) pv;
                int[] data = new int[1];
                while (len > 0) {
                    short val = from[fromOffset];
                    int value = val;
                    if(val<0) value &=0x0ffff;
                    data[0] = value;
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvULong: {
                PVULongArray pvdata = (PVULongArray) pv;
                long[] data = new long[1];
                while (len > 0) {
                    short val = from[fromOffset];
                    int value = val;
                    if(val<0) value &=0x0ffff;
                    data[0] = value;
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvFloat: {
                PVFloatArray pvdata = (PVFloatArray) pv;
                float[] data = new float[1];
                while (len > 0) {
                    float value = from[fromOffset];
                    if(value<0) value = -value;
                    data[0] = value;
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvDouble: {
                PVDoubleArray pvdata = (PVDoubleArray) pv;
                double[] data = new double[1];
                while (len > 0) {
                    double value = from[fromOffset];
                    if(value<0) value = -value;
                    data[0] = value;;
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvString:
                throw new IllegalArgumentException(
                        "convert from short[] to StringArray not legal");
            }
            throw new IllegalStateException(
                    "Logic error. Should never get here");
        }
        
        private int convertToShortArray(PVScalarArray pv, int offset, int len,
                short[] to, int toOffset) {
            ScalarType elemType = pv.getScalarArray().getElementType();
            int ntransfered = 0;
            switch (elemType) {
            case pvBoolean:
                throw new IllegalArgumentException(
                        "convert BooleanArray to short[]] not legal");
            case pvByte: 
            {
                PVByteArray pvdata = (PVByteArray) pv;
                ByteArrayData data = new ByteArrayData();
                while (len > 0) {
                    int num = pvdata.get(offset, len, data);
                    if (num == 0)
                        break;
                    byte[] dataArray = data.data;
                    int dataOffset = data.offset;
                    for (int i = 0; i < num; i++)
                        to[i + toOffset] = (short) dataArray[i + dataOffset];
                    len -= num;
                    offset += num;
                    toOffset += num;
                    ntransfered += num;
                }
                return ntransfered;
            } 
           
            case pvShort:
            {
                PVShortArray pvdata = (PVShortArray) pv;
                while (len > 0) {
                    int num = 0;
                    short[] dataArray = null;
                    int dataOffset = 0;
                    synchronized (shortArrayData) {
                        num = pvdata.get(offset, len, shortArrayData);
                        dataArray = shortArrayData.data;
                        dataOffset = shortArrayData.offset;
                    }
                    if (num <= 0)
                        break;
                    System.arraycopy(dataArray, dataOffset, to, toOffset, num);
                    len -= num;
                    offset += num;
                    toOffset += num;
                    ntransfered += num;
                }
                return ntransfered;
            }
            case pvInt:
            {
                PVIntArray pvdata = (PVIntArray) pv;
                IntArrayData data = new IntArrayData();
                while (len > 0) {
                    int num = pvdata.get(offset, len, data);
                    if (num == 0)
                        break;
                    int[] dataArray = data.data;
                    int dataOffset = data.offset;
                    for (int i = 0; i < num; i++)
                        to[i + toOffset] = (short) dataArray[i + dataOffset];
                    len -= num;
                    offset += num;
                    toOffset += num;
                    ntransfered += num;
                }
                return ntransfered;
            }
            case pvLong:
            {
                PVLongArray pvdata = (PVLongArray) pv;
                LongArrayData data = new LongArrayData();
                while (len > 0) {
                    int num = pvdata.get(offset, len, data);
                    if (num == 0)
                        break;
                    long[] dataArray = data.data;
                    int dataOffset = data.offset;
                    for (int i = 0; i < num; i++)
                        to[i + toOffset] = (short) dataArray[i + dataOffset];
                    len -= num;
                    offset += num;
                    toOffset += num;
                    ntransfered += num;
                }
                return ntransfered;
            }
            case pvUByte:
            {
                PVUByteArray pvdata = (PVUByteArray) pv;
                ByteArrayData data = new ByteArrayData();
                while (len > 0) {
                    int num = pvdata.get(offset, len, data);
                    if (num == 0)
                        break;
                    byte[] dataArray = data.data;
                    int dataOffset = data.offset;
                    for (int i = 0; i < num; i++) {
                        byte val = dataArray[i + dataOffset];
                        short value = val;
                        if(val<0) value &= 0x0ff;
                        to[i + toOffset] = value;
                    }
                    len -= num;
                    offset += num;
                    toOffset += num;
                    ntransfered += num;
                }
                return ntransfered;
            }
            case pvUShort:
            {
                PVUShortArray pvdata = (PVUShortArray) pv;
                while (len > 0) {
                    int num = 0;
                    short[] dataArray = null;
                    int dataOffset = 0;
                    synchronized (shortArrayData) {
                        num = pvdata.get(offset, len, shortArrayData);
                        dataArray = shortArrayData.data;
                        dataOffset = shortArrayData.offset;
                    }
                    if (num <= 0)
                        break;
                    System.arraycopy(dataArray, dataOffset, to, toOffset, num);
                    len -= num;
                    offset += num;
                    toOffset += num;
                    ntransfered += num;
                }
                return ntransfered;
            }
            case pvUInt:
            {
                PVUIntArray pvdata = (PVUIntArray) pv;
                IntArrayData data = new IntArrayData();
                while (len > 0) {
                    int num = pvdata.get(offset, len, data);
                    if (num == 0)
                        break;
                    int[] dataArray = data.data;
                    int dataOffset = data.offset;
                    for (int i = 0; i < num; i++)
                        to[i + toOffset] = (short) dataArray[i + dataOffset];
                    len -= num;
                    offset += num;
                    toOffset += num;
                    ntransfered += num;
                }
                return ntransfered;
            }
            case pvULong:
            {
                PVULongArray pvdata = (PVULongArray) pv;
                LongArrayData data = new LongArrayData();
                while (len > 0) {
                    int num = pvdata.get(offset, len, data);
                    if (num == 0)
                        break;
                    long[] dataArray = data.data;
                    int dataOffset = data.offset;
                    for (int i = 0; i < num; i++)
                        to[i + toOffset] = (short) dataArray[i + dataOffset];
                    len -= num;
                    offset += num;
                    toOffset += num;
                    ntransfered += num;
                }
                return ntransfered;
            }
            case pvFloat: {
                PVFloatArray pvdata = (PVFloatArray) pv;
                FloatArrayData data = new FloatArrayData();
                while (len > 0) {
                    int num = pvdata.get(offset, len, data);
                    if (num == 0)
                        break;
                    float[] dataArray = data.data;
                    int dataOffset = data.offset;
                    for (int i = 0; i < num; i++)
                        to[i + toOffset] = (short) dataArray[i + dataOffset];
                    len -= num;
                    offset += num;
                    toOffset += num;
                    ntransfered += num;
                }
                return ntransfered;
            }
            case pvDouble: {
                PVDoubleArray pvdata = (PVDoubleArray) pv;
                DoubleArrayData data = new DoubleArrayData();
                while (len > 0) {
                    int num = pvdata.get(offset, len, data);
                    if (num == 0)
                        break;
                    double[] dataArray = data.data;
                    int dataOffset = data.offset;
                    for (int i = 0; i < num; i++)
                        to[i + toOffset] = (short) dataArray[i + dataOffset];
                    len -= num;
                    offset += num;
                    toOffset += num;
                    ntransfered += num;
                }
                return ntransfered;
            }
            case pvString:
                throw new IllegalArgumentException(
                        "convert StringArray to short[]] not legal");
            }
            throw new IllegalStateException(
                    "Logic error. Should never get here");
        }
        
        private int convertFromIntArray(PVScalarArray pv, int offset, int len,
                int[] from, int fromOffset) {
            ScalarType elemType = pv.getScalarArray().getElementType();
            int ntransfered = 0;
            switch (elemType) {
            case pvBoolean:
                throw new IllegalArgumentException(
                        "convert from int[] to BooleanArray not legal");
            case pvByte:
            {
                PVByteArray pvdata = (PVByteArray) pv;
                byte[] data = new byte[1];
                while (len > 0) {
                    data[0] = (byte) from[fromOffset];
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvShort:
            {
                PVShortArray pvdata = (PVShortArray) pv;
                short[] data = new short[1];
                while (len > 0) {
                    data[0] = (short) from[fromOffset];
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvInt:
            {
                PVIntArray pvdata = (PVIntArray) pv;
                while (len > 0) {
                    int n = pvdata.put(offset, len, from, fromOffset);
                    if (n == 0)
                        break;
                    len -= n;
                    offset += n;
                    fromOffset += n;
                    ntransfered += n;
                }
                return ntransfered;
            }
            case pvLong: {
                PVLongArray pvdata = (PVLongArray) pv;
                long[] data = new long[1];
                while (len > 0) {
                    data[0] = (long) from[fromOffset];
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvUByte:
            {
                PVUByteArray pvdata = (PVUByteArray) pv;
                byte[] data = new byte[1];
                while (len > 0) {
                    data[0] = (byte) from[fromOffset];
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvUShort:
            {
                PVUShortArray pvdata = (PVUShortArray) pv;
                short[] data = new short[1];
                while (len > 0) {
                    data[0] = (short) from[fromOffset];
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvUInt:
            {
                PVUIntArray pvdata = (PVUIntArray) pv;
                while (len > 0) {
                    int n = pvdata.put(offset, len, from, fromOffset);
                    if (n == 0)
                        break;
                    len -= n;
                    offset += n;
                    fromOffset += n;
                    ntransfered += n;
                }
                return ntransfered;
            }
            case pvULong: {
                PVULongArray pvdata = (PVULongArray) pv;
                long[] data = new long[1];
                while (len > 0) {
                	int val = from[fromOffset];
                	long value = val;
                	if(val<0) value &= 0x0ffffffffL;
                    data[0] = value;
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvFloat: {
                PVFloatArray pvdata = (PVFloatArray) pv;
                float[] data = new float[1];
                while (len > 0) {
                    data[0] = from[fromOffset];
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvDouble: {
                PVDoubleArray pvdata = (PVDoubleArray) pv;
                double[] data = new double[1];
                while (len > 0) {
                    data[0] = from[fromOffset];
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvString:
                throw new IllegalArgumentException(
                        "convert from int[] to StringArray not legal");
            }
            throw new IllegalStateException(
                    "Logic error. Should never get here");
        }
        
        private int convertFromUIntArray(PVScalarArray pv, int offset, int len,
                int[] from, int fromOffset) {
            ScalarType elemType = pv.getScalarArray().getElementType();
            int ntransfered = 0;
            switch (elemType) {
            case pvBoolean:
                throw new IllegalArgumentException(
                        "convert from int[] to BooleanArray not legal");
            case pvByte:
            {
                PVByteArray pvdata = (PVByteArray) pv;
                byte[] data = new byte[1];
                while (len > 0) {
                    data[0] = (byte) from[fromOffset];
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvShort:
            {
                PVShortArray pvdata = (PVShortArray) pv;
                short[] data = new short[1];
                while (len > 0) {
                    data[0] = (short) from[fromOffset];
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvInt:
            {
                PVIntArray pvdata = (PVIntArray) pv;
                while (len > 0) {
                    int n = pvdata.put(offset, len, from, fromOffset);
                    if (n == 0)
                        break;
                    len -= n;
                    offset += n;
                    fromOffset += n;
                    ntransfered += n;
                }
                return ntransfered;
            }
            case pvLong: {
                PVLongArray pvdata = (PVLongArray) pv;
                long[] data = new long[1];
                while (len > 0) {
                    long value = from[fromOffset];
                    if(value<0) value = -value;
                    data[0] = value;
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvUByte:
            {
                PVUByteArray pvdata = (PVUByteArray) pv;
                byte[] data = new byte[1];
                while (len > 0) {
                    data[0] = (byte) from[fromOffset];
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvUShort:
            {
                PVUShortArray pvdata = (PVUShortArray) pv;
                short[] data = new short[1];
                while (len > 0) {
                    data[0] = (short) from[fromOffset];
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvUInt:
            {
                PVUIntArray pvdata = (PVUIntArray) pv;
                while (len > 0) {
                    int n = pvdata.put(offset, len, from, fromOffset);
                    if (n == 0)
                        break;
                    len -= n;
                    offset += n;
                    fromOffset += n;
                    ntransfered += n;
                }
                return ntransfered;
            }
            case pvULong: {
                PVULongArray pvdata = (PVULongArray) pv;
                long[] data = new long[1];
                while (len > 0) {
                    int val = from[fromOffset];
                    long value = val;
                    if(val<0) value &= 0x0ffffffffL;
                    data[0] = value;
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvFloat: {
                PVFloatArray pvdata = (PVFloatArray) pv;
                float[] data = new float[1];
                while (len > 0) {
                    float value = from[fromOffset];
                    if(value<0) value = -value;
                    data[0] = value;
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvDouble: {
                PVDoubleArray pvdata = (PVDoubleArray) pv;
                double[] data = new double[1];
                while (len > 0) {
                    double value = from[fromOffset];
                    if(value<0) value = -value;
                    data[0] = value;;
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvString:
                throw new IllegalArgumentException(
                        "convert from int[] to StringArray not legal");
            }
            throw new IllegalStateException(
                    "Logic error. Should never get here");
        }
        
        private int convertToIntArray(PVScalarArray pv, int offset, int len,
                int[] to, int toOffset) {
            ScalarType elemType = pv.getScalarArray().getElementType();
            int ntransfered = 0;
            switch (elemType) {
            case pvBoolean:
                throw new IllegalArgumentException(
                        "convert BooleanArray to int[]] not legal");
            case pvByte: {
                PVByteArray pvdata = (PVByteArray) pv;
                ByteArrayData data = new ByteArrayData();
                while (len > 0) {
                    int num = pvdata.get(offset, len, data);
                    if (num == 0)
                        break;
                    byte[] dataArray = data.data;
                    int dataOffset = data.offset;
                    for (int i = 0; i < num; i++)
                        to[i + toOffset] = (int) dataArray[i + dataOffset];
                    len -= num;
                    offset += num;
                    toOffset += num;
                    ntransfered += num;
                }
                return ntransfered;
            }
            case pvShort: {
                PVShortArray pvdata = (PVShortArray) pv;
                ShortArrayData data = new ShortArrayData();
                while (len > 0) {
                    int num = pvdata.get(offset, len, data);
                    if (num == 0)
                        break;
                    short[] dataArray = data.data;
                    int dataOffset = data.offset;
                    for (int i = 0; i < num; i++)
                        to[i + toOffset] = (int) dataArray[i + dataOffset];
                    len -= num;
                    offset += num;
                    toOffset += num;
                    ntransfered += num;
                }
                return ntransfered;
            }
            case pvInt:
            {
                PVIntArray pvdata = (PVIntArray) pv;
                while (len > 0) {
                    int num = 0;
                    int[] dataArray = null;
                    int dataOffset = 0;
                    synchronized (intArrayData) {
                        num = pvdata.get(offset, len, intArrayData);
                        dataArray = intArrayData.data;
                        dataOffset = intArrayData.offset;
                    }
                    if (num <= 0)
                        break;
                    System.arraycopy(dataArray, dataOffset, to, toOffset, num);
                    len -= num;
                    offset += num;
                    toOffset += num;
                    ntransfered += num;
                }
                return ntransfered;
            }
            case pvLong:
            {
                PVLongArray pvdata = (PVLongArray) pv;
                LongArrayData data = new LongArrayData();
                while (len > 0) {
                    int num = pvdata.get(offset, len, data);
                    if (num == 0)
                        break;
                    long[] dataArray = data.data;
                    int dataOffset = data.offset;
                    for (int i = 0; i < num; i++)
                        to[i + toOffset] = (int) dataArray[i + dataOffset];
                    len -= num;
                    offset += num;
                    toOffset += num;
                    ntransfered += num;
                }
                return ntransfered;
            }
            case pvUByte: {
                PVUByteArray pvdata = (PVUByteArray) pv;
                ByteArrayData data = new ByteArrayData();
                while (len > 0) {
                    int num = pvdata.get(offset, len, data);
                    if (num == 0)
                        break;
                    byte[] dataArray = data.data;
                    int dataOffset = data.offset;
                    for (int i = 0; i < num; i++) {
                        byte val = dataArray[i + dataOffset];
                        short value = val;
                        if(val<0) value &= 0x0ff;
                        to[i + toOffset] = value;
                    }
                    len -= num;
                    offset += num;
                    toOffset += num;
                    ntransfered += num;
                }
                return ntransfered;
            }
            case pvUShort: {
                PVUShortArray pvdata = (PVUShortArray) pv;
                ShortArrayData data = new ShortArrayData();
                while (len > 0) {
                    int num = pvdata.get(offset, len, data);
                    if (num == 0)
                        break;
                    short[] dataArray = data.data;
                    int dataOffset = data.offset;
                    for (int i = 0; i < num; i++) {
                        short val = dataArray[i + dataOffset];
                        int value = val;
                        if(val<0) value &= 0x0ffff;
                        to[i + toOffset] = value;
                    }
                    len -= num;
                    offset += num;
                    toOffset += num;
                    ntransfered += num;
                }
                return ntransfered;
            }
            case pvUInt:
            {
                PVUIntArray pvdata = (PVUIntArray) pv;
                while (len > 0) {
                    int num = 0;
                    int[] dataArray = null;
                    int dataOffset = 0;
                    synchronized (intArrayData) {
                        num = pvdata.get(offset, len, intArrayData);
                        dataArray = intArrayData.data;
                        dataOffset = intArrayData.offset;
                    }
                    if (num <= 0)
                        break;
                    System.arraycopy(dataArray, dataOffset, to, toOffset, num);
                    len -= num;
                    offset += num;
                    toOffset += num;
                    ntransfered += num;
                }
                return ntransfered;
            }
            case pvULong:
            {
                PVULongArray pvdata = (PVULongArray) pv;
                LongArrayData data = new LongArrayData();
                while (len > 0) {
                    int num = pvdata.get(offset, len, data);
                    if (num == 0)
                        break;
                    long[] dataArray = data.data;
                    int dataOffset = data.offset;
                    for (int i = 0; i < num; i++)
                        to[i + toOffset] = (int) dataArray[i + dataOffset];
                    len -= num;
                    offset += num;
                    toOffset += num;
                    ntransfered += num;
                }
                return ntransfered;
            }
            case pvFloat: {
                PVFloatArray pvdata = (PVFloatArray) pv;
                FloatArrayData data = new FloatArrayData();
                while (len > 0) {
                    int num = pvdata.get(offset, len, data);
                    if (num == 0)
                        break;
                    float[] dataArray = data.data;
                    int dataOffset = data.offset;
                    for (int i = 0; i < num; i++)
                        to[i + toOffset] = (int) dataArray[i + dataOffset];
                    len -= num;
                    offset += num;
                    toOffset += num;
                    ntransfered += num;
                }
                return ntransfered;
            }
            case pvDouble: {
                PVDoubleArray pvdata = (PVDoubleArray) pv;
                DoubleArrayData data = new DoubleArrayData();
                while (len > 0) {
                    int num = pvdata.get(offset, len, data);
                    if (num == 0)
                        break;
                    double[] dataArray = data.data;
                    int dataOffset = data.offset;
                    for (int i = 0; i < num; i++)
                        to[i + toOffset] = (int) dataArray[i + dataOffset];
                    len -= num;
                    offset += num;
                    toOffset += num;
                    ntransfered += num;
                }
                return ntransfered;
            }
            case pvString:
                throw new IllegalArgumentException(
                        "convert StringArray to int[]] not legal");
            }
            throw new IllegalStateException(
                    "Logic error. Should never get here");
        }

        private int convertFromLongArray(PVScalarArray pv, int offset, int len,
                long[] from, int fromOffset) {
            ScalarType elemType = pv.getScalarArray().getElementType();
            int ntransfered = 0;
            switch (elemType) {
            case pvBoolean:
                throw new IllegalArgumentException(
                        "convert from long[] to BooleanArray not legal");
            case pvByte: 
            {
                PVByteArray pvdata = (PVByteArray) pv;
                byte[] data = new byte[1];
                while (len > 0) {
                    data[0] = (byte) from[fromOffset];
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvShort:
            {
                PVShortArray pvdata = (PVShortArray) pv;
                short[] data = new short[1];
                while (len > 0) {
                    data[0] = (short) from[fromOffset];
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvInt:
            {
                PVIntArray pvdata = (PVIntArray) pv;
                int[] data = new int[1];
                while (len > 0) {
                    data[0] = (int) from[fromOffset];
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvLong:
            {
                PVLongArray pvdata = (PVLongArray) pv;
                while (len > 0) {
                    int n = pvdata.put(offset, len, from, fromOffset);
                    if (n == 0)
                        break;
                    len -= n;
                    offset += n;
                    fromOffset += n;
                    ntransfered += n;
                }
                return ntransfered;
            }
            case pvUByte:
            {
                PVUByteArray pvdata = (PVUByteArray) pv;
                byte[] data = new byte[1];
                while (len > 0) {
                    data[0] = (byte) from[fromOffset];
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvUShort:
            {
                PVUShortArray pvdata = (PVUShortArray) pv;
                short[] data = new short[1];
                while (len > 0) {
                    data[0] = (short) from[fromOffset];
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvUInt:
            {
                PVUIntArray pvdata = (PVUIntArray) pv;
                int[] data = new int[1];
                while (len > 0) {
                    data[0] = (int) from[fromOffset];
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvULong:
            {
                PVULongArray pvdata = (PVULongArray) pv;
                while (len > 0) {
                    int n = pvdata.put(offset, len, from, fromOffset);
                    if (n == 0)
                        break;
                    len -= n;
                    offset += n;
                    fromOffset += n;
                    ntransfered += n;
                }
                return ntransfered;
            }
            case pvFloat: {
                PVFloatArray pvdata = (PVFloatArray) pv;
                float[] data = new float[1];
                while (len > 0) {
                    data[0] = (float) from[fromOffset];
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvDouble: {
                PVDoubleArray pvdata = (PVDoubleArray) pv;
                double[] data = new double[1];
                while (len > 0) {
                    data[0] = (double) from[fromOffset];
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvString:
                throw new IllegalArgumentException(
                        "convert from long[] to StringArray not legal");
            }
            throw new IllegalStateException(
                    "Logic error. Should never get here");
        }
        
        private int convertFromULongArray(PVScalarArray pv, int offset, int len,
                long[] from, int fromOffset) {
            ScalarType elemType = pv.getScalarArray().getElementType();
            int ntransfered = 0;
            switch (elemType) {
            case pvBoolean:
                throw new IllegalArgumentException(
                        "convert from long[] to BooleanArray not legal");
            case pvByte: 
            {
                PVByteArray pvdata = (PVByteArray) pv;
                byte[] data = new byte[1];
                while (len > 0) {
                    data[0] = (byte) from[fromOffset];
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvShort:
            {
                PVShortArray pvdata = (PVShortArray) pv;
                short[] data = new short[1];
                while (len > 0) {
                    data[0] = (short) from[fromOffset];
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvInt:
            {
                PVIntArray pvdata = (PVIntArray) pv;
                int[] data = new int[1];
                while (len > 0) {
                    data[0] = (int) from[fromOffset];
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvLong:
            {
                PVLongArray pvdata = (PVLongArray) pv;
                while (len > 0) {
                    int n = pvdata.put(offset, len, from, fromOffset);
                    if (n == 0)
                        break;
                    len -= n;
                    offset += n;
                    fromOffset += n;
                    ntransfered += n;
                }
                return ntransfered;
            }
            case pvUByte:
            {
                PVUByteArray pvdata = (PVUByteArray) pv;
                byte[] data = new byte[1];
                while (len > 0) {
                    data[0] = (byte) from[fromOffset];
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvUShort:
            {
                PVUShortArray pvdata = (PVUShortArray) pv;
                short[] data = new short[1];
                while (len > 0) {
                    data[0] = (short) from[fromOffset];
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvUInt:
            {
                PVUIntArray pvdata = (PVUIntArray) pv;
                int[] data = new int[1];
                while (len > 0) {
                    data[0] = (int) from[fromOffset];
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvULong:
            {
                PVULongArray pvdata = (PVULongArray) pv;
                while (len > 0) {
                    int n = pvdata.put(offset, len, from, fromOffset);
                    if (n == 0)
                        break;
                    len -= n;
                    offset += n;
                    fromOffset += n;
                    ntransfered += n;
                }
                return ntransfered;
            }
            case pvFloat: {
                PVFloatArray pvdata = (PVFloatArray) pv;
                float[] data = new float[1];
                while (len > 0) {
                    float value = from[fromOffset];
                    if(value<0) value = -value;
                    data[0] = value;
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvDouble: {
                PVDoubleArray pvdata = (PVDoubleArray) pv;
                double[] data = new double[1];
                while (len > 0) {
                    double value = from[fromOffset];
                    if(value<0) value = -value;
                    data[0] = value;;
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvString:
                throw new IllegalArgumentException(
                        "convert from long[] to StringArray not legal");
            }
            throw new IllegalStateException(
                    "Logic error. Should never get here");
        }
        
        private int convertToLongArray(PVScalarArray pv, int offset, int len,
                long[] to, int toOffset) {
            ScalarType elemType = pv.getScalarArray().getElementType();
            int ntransfered = 0;
            switch (elemType) {
            case pvBoolean:
                throw new IllegalArgumentException(
                        "convert BooleanArray to long[]] not legal");
            case pvByte: {
                PVByteArray pvdata = (PVByteArray) pv;
                ByteArrayData data = new ByteArrayData();
                while (len > 0) {
                    int num = pvdata.get(offset, len, data);
                    if (num == 0)
                        break;
                    byte[] dataArray = data.data;
                    int dataOffset = data.offset;
                    for (int i = 0; i < num; i++)
                        to[i + toOffset] = (long) dataArray[i + dataOffset];
                    len -= num;
                    offset += num;
                    toOffset += num;
                    ntransfered += num;
                }
                return ntransfered;
            }
            case pvShort: {
                PVShortArray pvdata = (PVShortArray) pv;
                ShortArrayData data = new ShortArrayData();
                while (len > 0) {
                    int num = pvdata.get(offset, len, data);
                    if (num == 0)
                        break;
                    short[] dataArray = data.data;
                    int dataOffset = data.offset;
                    for (int i = 0; i < num; i++)
                        to[i + toOffset] = (long) dataArray[i + dataOffset];
                    len -= num;
                    offset += num;
                    toOffset += num;
                    ntransfered += num;
                }
                return ntransfered;
            }
            case pvInt: {
                PVIntArray pvdata = (PVIntArray) pv;
                IntArrayData data = new IntArrayData();
                while (len > 0) {
                    int num = pvdata.get(offset, len, data);
                    if (num == 0)
                        break;
                    int[] dataArray = data.data;
                    int dataOffset = data.offset;
                    for (int i = 0; i < num; i++)
                        to[i + toOffset] = (long) dataArray[i + dataOffset];
                    len -= num;
                    offset += num;
                    toOffset += num;
                    ntransfered += num;
                }
                return ntransfered;
            }
            case pvLong:
            {
                PVLongArray pvdata = (PVLongArray) pv;
                while (len > 0) {
                    int num = 0;
                    long[] dataArray = null;
                    int dataOffset = 0;
                    synchronized (longArrayData) {
                        num = pvdata.get(offset, len, longArrayData);
                        dataArray = longArrayData.data;
                        dataOffset = longArrayData.offset;
                    }
                    if (num <= 0)
                        break;
                    System.arraycopy(dataArray, dataOffset, to, toOffset, num);
                    len -= num;
                    offset += num;
                    toOffset += num;
                    ntransfered += num;
                }
                return ntransfered;
            }
            case pvUByte: {
                PVUByteArray pvdata = (PVUByteArray) pv;
                ByteArrayData data = new ByteArrayData();
                while (len > 0) {
                    int num = pvdata.get(offset, len, data);
                    if (num == 0)
                        break;
                    byte[] dataArray = data.data;
                    int dataOffset = data.offset;
                    for (int i = 0; i < num; i++) {
                        byte val = dataArray[i + dataOffset];
                        short value = val;
                        if(val<0) value &= 0x0ff;
                        to[i + toOffset] = value;
                    }
                    len -= num;
                    offset += num;
                    toOffset += num;
                    ntransfered += num;
                }
                return ntransfered;
            }
            case pvUShort:
            {
                PVUShortArray pvdata = (PVUShortArray) pv;
                ShortArrayData data = new ShortArrayData();
                while (len > 0) {
                    int num = pvdata.get(offset, len, data);
                    if (num == 0)
                        break;
                    short[] dataArray = data.data;
                    int dataOffset = data.offset;
                    for (int i = 0; i < num; i++) {
                        short val = dataArray[i + dataOffset];
                        int value = val;
                        if(val<0) value &= 0x0ffff;
                        to[i + toOffset] = value;
                    }
                    len -= num;
                    offset += num;
                    toOffset += num;
                    ntransfered += num;
                }
                return ntransfered;
            }
            case pvUInt: {
                PVUIntArray pvdata = (PVUIntArray) pv;
                IntArrayData data = new IntArrayData();
                while (len > 0) {
                    int num = pvdata.get(offset, len, data);
                    if (num == 0)
                        break;
                    int[] dataArray = data.data;
                    int dataOffset = data.offset;
                    for (int i = 0; i < num; i++) {
                        int val = dataArray[i + dataOffset];
                        long value = val;
                        if(val<0) value &= 0x0ffffffffL;
                        to[i + toOffset] = value;
                    }
                    len -= num;
                    offset += num;
                    toOffset += num;
                    ntransfered += num;
                }
                return ntransfered;
            }
            case pvULong:
            {
                PVULongArray pvdata = (PVULongArray) pv;
                while (len > 0) {
                    int num = 0;
                    long[] dataArray = null;
                    int dataOffset = 0;
                    synchronized (longArrayData) {
                        num = pvdata.get(offset, len, longArrayData);
                        dataArray = longArrayData.data;
                        dataOffset = longArrayData.offset;
                    }
                    if (num <= 0)
                        break;
                    System.arraycopy(dataArray, dataOffset, to, toOffset, num);
                    len -= num;
                    offset += num;
                    toOffset += num;
                    ntransfered += num;
                }
                return ntransfered;
            }
            case pvFloat: {
                PVFloatArray pvdata = (PVFloatArray) pv;
                FloatArrayData data = new FloatArrayData();
                while (len > 0) {
                    int num = pvdata.get(offset, len, data);
                    if (num == 0)
                        break;
                    float[] dataArray = data.data;
                    int dataOffset = data.offset;
                    for (int i = 0; i < num; i++)
                        to[i + toOffset] = (long) dataArray[i + dataOffset];
                    len -= num;
                    offset += num;
                    toOffset += num;
                    ntransfered += num;
                }
                return ntransfered;
            }
            case pvDouble: {
                PVDoubleArray pvdata = (PVDoubleArray) pv;
                DoubleArrayData data = new DoubleArrayData();
                while (len > 0) {
                    int num = pvdata.get(offset, len, data);
                    if (num == 0)
                        break;
                    double[] dataArray = data.data;
                    int dataOffset = data.offset;
                    for (int i = 0; i < num; i++)
                        to[i + toOffset] = (long) dataArray[i + dataOffset];
                    len -= num;
                    offset += num;
                    toOffset += num;
                    ntransfered += num;
                }
                return ntransfered;
            }
            case pvString:
                throw new IllegalArgumentException(
                        "convert StringArray to long[]] not legal");
            }
            throw new IllegalStateException(
                    "Logic error. Should never get here");
        }
        
        private int convertFromFloatArray(PVScalarArray pv, int offset,
                int len, float[] from, int fromOffset) {
            ScalarType elemType = pv.getScalarArray().getElementType();
            int ntransfered = 0;
            switch (elemType) {
            case pvBoolean:
                throw new IllegalArgumentException(
                        "convert from float[] to BooleanArray not legal");
            case pvByte: {
                PVByteArray pvdata = (PVByteArray) pv;
                byte[] data = new byte[1];
                while (len > 0) {
                    data[0] = (byte) from[fromOffset];
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvShort: {
                PVShortArray pvdata = (PVShortArray) pv;
                short[] data = new short[1];
                while (len > 0) {
                    data[0] = (short) from[fromOffset];
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvInt: {
                PVIntArray pvdata = (PVIntArray) pv;
                int[] data = new int[1];
                while (len > 0) {
                    data[0] = (int) from[fromOffset];
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvLong: {
                PVLongArray pvdata = (PVLongArray) pv;
                long[] data = new long[1];
                while (len > 0) {
                    data[0] = (long) from[fromOffset];
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvFloat: {
                PVFloatArray pvdata = (PVFloatArray) pv;
                while (len > 0) {
                    int n = pvdata.put(offset, len, from, fromOffset);
                    if (n == 0)
                        break;
                    len -= n;
                    offset += n;
                    fromOffset += n;
                    ntransfered += n;
                }
                return ntransfered;
            }
            case pvDouble: {
                PVDoubleArray pvdata = (PVDoubleArray) pv;
                double[] data = new double[1];
                while (len > 0) {
                    data[0] = (double) from[fromOffset];
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvString:
                throw new IllegalArgumentException(
                        "convert from float[] to StringArray not legal");
            }
            throw new IllegalStateException(
                    "Logic error. Should never get here");
        }

        private int convertToFloatArray(PVScalarArray pv, int offset, int len,
                float[] to, int toOffset) {
            ScalarType elemType = pv.getScalarArray().getElementType();
            int ntransfered = 0;
            switch (elemType) {
            case pvBoolean:
                throw new IllegalArgumentException(
                        "convert BooleanArray to float[]] not legal");
            case pvByte: {
                PVByteArray pvdata = (PVByteArray) pv;
                ByteArrayData data = new ByteArrayData();
                while (len > 0) {
                    int num = pvdata.get(offset, len, data);
                    if (num == 0)
                        break;
                    byte[] dataArray = data.data;
                    int dataOffset = data.offset;
                    for (int i = 0; i < num; i++)
                        to[i + toOffset] = (float) dataArray[i + dataOffset];
                    len -= num;
                    offset += num;
                    toOffset += num;
                    ntransfered += num;
                }
                return ntransfered;
            }
            case pvShort: {
                PVShortArray pvdata = (PVShortArray) pv;
                ShortArrayData data = new ShortArrayData();
                while (len > 0) {
                    int num = pvdata.get(offset, len, data);
                    if (num == 0)
                        break;
                    short[] dataArray = data.data;
                    int dataOffset = data.offset;
                    for (int i = 0; i < num; i++)
                        to[i + toOffset] = (float) dataArray[i + dataOffset];
                    len -= num;
                    offset += num;
                    toOffset += num;
                    ntransfered += num;
                }
                return ntransfered;
            }
            case pvInt: {
                PVIntArray pvdata = (PVIntArray) pv;
                IntArrayData data = new IntArrayData();
                while (len > 0) {
                    int num = pvdata.get(offset, len, data);
                    if (num == 0)
                        break;
                    int[] dataArray = data.data;
                    int dataOffset = data.offset;
                    for (int i = 0; i < num; i++)
                        to[i + toOffset] = (float) dataArray[i + dataOffset];
                    len -= num;
                    offset += num;
                    toOffset += num;
                    ntransfered += num;
                }
                return ntransfered;
            }
            case pvLong: {
                PVLongArray pvdata = (PVLongArray) pv;
                LongArrayData data = new LongArrayData();
                while (len > 0) {
                    int num = pvdata.get(offset, len, data);
                    if (num == 0)
                        break;
                    long[] dataArray = data.data;
                    int dataOffset = data.offset;
                    for (int i = 0; i < num; i++)
                        to[i + toOffset] = (float) dataArray[i + dataOffset];
                    len -= num;
                    offset += num;
                    toOffset += num;
                    ntransfered += num;
                }
                return ntransfered;
            }
            case pvFloat: {
                PVFloatArray pvdata = (PVFloatArray) pv;
                while (len > 0) {
                    int num = 0;
                    float[] dataArray = null;
                    int dataOffset = 0;
                    synchronized (floatArrayData) {
                        num = pvdata.get(offset, len, floatArrayData);
                        dataArray = floatArrayData.data;
                        dataOffset = floatArrayData.offset;
                    }
                    if (num <= 0)
                        break;
                    System.arraycopy(dataArray, dataOffset, to, toOffset, num);
                    len -= num;
                    offset += num;
                    toOffset += num;
                    ntransfered += num;
                }
                return ntransfered;
            }
            case pvDouble: {
                PVDoubleArray pvdata = (PVDoubleArray) pv;
                DoubleArrayData data = new DoubleArrayData();
                while (len > 0) {
                    int num = pvdata.get(offset, len, data);
                    if (num == 0)
                        break;
                    double[] dataArray = data.data;
                    int dataOffset = data.offset;
                    for (int i = 0; i < num; i++)
                        to[i + toOffset] = (float) dataArray[i + dataOffset];
                    len -= num;
                    offset += num;
                    toOffset += num;
                    ntransfered += num;
                }
                return ntransfered;
            }
            case pvString:
                throw new IllegalArgumentException(
                        "convert StringArray to float[]] not legal");
            }
            throw new IllegalStateException(
                    "Logic error. Should never get here");
        }

        private int convertFromDoubleArray(PVScalarArray pv, int offset,
                int len, double[] from, int fromOffset) {
            ScalarType elemType = pv.getScalarArray().getElementType();
            int ntransfered = 0;
            switch (elemType) {
            case pvBoolean:
                throw new IllegalArgumentException(
                        "convert from double[] to BooleanArray not legal");
            case pvByte: {
                PVByteArray pvdata = (PVByteArray) pv;
                byte[] data = new byte[1];
                while (len > 0) {
                    data[0] = (byte) from[fromOffset];
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvShort: {
                PVShortArray pvdata = (PVShortArray) pv;
                short[] data = new short[1];
                while (len > 0) {
                    data[0] = (short) from[fromOffset];
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvInt: {
                PVIntArray pvdata = (PVIntArray) pv;
                int[] data = new int[1];
                while (len > 0) {
                    data[0] = (int) from[fromOffset];
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvLong: {
                PVLongArray pvdata = (PVLongArray) pv;
                long[] data = new long[1];
                while (len > 0) {
                    data[0] = (long) from[fromOffset];
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvFloat: {
                PVFloatArray pvdata = (PVFloatArray) pv;
                float[] data = new float[1];
                while (len > 0) {
                    data[0] = (float) from[fromOffset];
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvDouble: {
                PVDoubleArray pvdata = (PVDoubleArray) pv;
                while (len > 0) {
                    int n = pvdata.put(offset, len, from, fromOffset);
                    if (n == 0)
                        break;
                    len -= n;
                    offset += n;
                    fromOffset += n;
                    ntransfered += n;
                }
                return ntransfered;
            }
            case pvString:
                throw new IllegalArgumentException(
                        "convert from double[] to StringArray not legal");
            }
            throw new IllegalStateException(
                    "Logic error. Should never get here");
        }

        private int convertToDoubleArray(PVScalarArray pv, int offset, int len,
                double[] to, int toOffset) {
            ScalarType elemType = pv.getScalarArray().getElementType();
            int ntransfered = 0;
            switch (elemType) {
            case pvBoolean:
                throw new IllegalArgumentException(
                        "convert BooleanArray to double[]] not legal");
            case pvByte: {
                PVByteArray pvdata = (PVByteArray) pv;
                ByteArrayData data = new ByteArrayData();
                while (len > 0) {
                    int num = pvdata.get(offset, len, data);
                    if (num == 0)
                        break;
                    byte[] dataArray = data.data;
                    int dataOffset = data.offset;
                    for (int i = 0; i < num; i++)
                        to[i + toOffset] = (double) dataArray[i + dataOffset];
                    len -= num;
                    offset += num;
                    toOffset += num;
                    ntransfered += num;
                }
                return ntransfered;
            }
            case pvShort: {
                PVShortArray pvdata = (PVShortArray) pv;
                ShortArrayData data = new ShortArrayData();
                while (len > 0) {
                    int num = pvdata.get(offset, len, data);
                    if (num == 0)
                        break;
                    short[] dataArray = data.data;
                    int dataOffset = data.offset;
                    for (int i = 0; i < num; i++)
                        to[i + toOffset] = (double) dataArray[i + dataOffset];
                    len -= num;
                    offset += num;
                    toOffset += num;
                    ntransfered += num;
                }
                return ntransfered;
            }
            case pvInt: {
                PVIntArray pvdata = (PVIntArray) pv;
                IntArrayData data = new IntArrayData();
                while (len > 0) {
                    int num = pvdata.get(offset, len, data);
                    if (num == 0)
                        break;
                    int[] dataArray = data.data;
                    int dataOffset = data.offset;
                    for (int i = 0; i < num; i++)
                        to[i + toOffset] = (double) dataArray[i + dataOffset];
                    len -= num;
                    offset += num;
                    toOffset += num;
                    ntransfered += num;
                }
                return ntransfered;
            }
            case pvLong: {
                PVLongArray pvdata = (PVLongArray) pv;
                LongArrayData data = new LongArrayData();
                while (len > 0) {
                    int num = pvdata.get(offset, len, data);
                    if (num == 0)
                        break;
                    long[] dataArray = data.data;
                    int dataOffset = data.offset;
                    for (int i = 0; i < num; i++)
                        to[i + toOffset] = (double) dataArray[i + dataOffset];
                    len -= num;
                    offset += num;
                    toOffset += num;
                    ntransfered += num;
                }
                return ntransfered;
            }
            case pvFloat: {
                PVFloatArray pvdata = (PVFloatArray) pv;
                FloatArrayData data = new FloatArrayData();
                while (len > 0) {
                    int num = pvdata.get(offset, len, data);
                    if (num == 0)
                        break;
                    float[] dataArray = data.data;
                    int dataOffset = data.offset;
                    for (int i = 0; i < num; i++)
                        to[i + toOffset] = (double) dataArray[i + dataOffset];
                    len -= num;
                    offset += num;
                    toOffset += num;
                    ntransfered += num;
                }
                return ntransfered;
            }
            case pvDouble: {
                PVDoubleArray pvdata = (PVDoubleArray) pv;
                while (len > 0) {
                    int num = 0;
                    double[] dataArray = null;
                    int dataOffset = 0;
                    synchronized (doubleArrayData) {
                        num = pvdata.get(offset, len, doubleArrayData);
                        dataArray = doubleArrayData.data;
                        dataOffset = doubleArrayData.offset;
                    }
                    if (num <= 0)
                        break;
                    System.arraycopy(dataArray, dataOffset, to, toOffset, num);
                    len -= num;
                    offset += num;
                    toOffset += num;
                    ntransfered += num;
                }
                return ntransfered;
            }
            case pvString:
                throw new IllegalArgumentException(
                        "convert StringArray to double[]] not legal");
            }
            throw new IllegalStateException(
                    "Logic error. Should never get here");
        }

        private String removeWhiteSpace(String start) {
            int length = start.length();
            if (length < 1)
                return start;
            if (!Character.isWhitespace(start.charAt(0))
                    && !Character.isWhitespace(start.charAt(length - 1)))
                return start;
            StringBuilder builder = new StringBuilder(start);
            int lastWhite = -1;
            for (int i = 0; i < length; i++) {
                if (!Character.isWhitespace(start.charAt(i))) break;
                lastWhite = i;
            }
            if (lastWhite >= 0) {
                builder.delete(0, lastWhite + 1);
                length -= lastWhite + 1;
            }
            int firstWhite = builder.length();
            for (int i = firstWhite - 1; i >= 0; i--) {
                if (!Character.isWhitespace(builder.charAt(i)))
                    break;
                firstWhite = i;
            }
            if (firstWhite < length)
                builder.setLength(firstWhite);
            return builder.toString();
        }

        private int convertFromStringArray(PVScalarArray pv, int offset,
                int len, String[] from, int fromOffset) {
            ScalarType elemType = pv.getScalarArray().getElementType();
            int ntransfered = 0;
            switch (elemType) {
            case pvBoolean: {
                PVBooleanArray pvdata = (PVBooleanArray) pv;
                boolean[] data = new boolean[1];
                while (len > 0) {
                    String fromString = removeWhiteSpace(from[fromOffset]);
                    data[0] = Boolean.parseBoolean(fromString);
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvByte: {
                PVByteArray pvdata = (PVByteArray) pv;
                byte[] data = new byte[1];
                while (len > 0) {
                    String fromString = removeWhiteSpace(from[fromOffset]);
                    data[0] = Byte.decode(fromString);
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvShort: {
                PVShortArray pvdata = (PVShortArray) pv;
                short[] data = new short[1];
                while (len > 0) {
                    String fromString = removeWhiteSpace(from[fromOffset]);
                    data[0] = Short.decode(fromString);
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvInt: {
                PVIntArray pvdata = (PVIntArray) pv;
                int[] data = new int[1];
                while (len > 0) {
                    String fromString = removeWhiteSpace(from[fromOffset]);
                    data[0] = Integer.decode(fromString);
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvLong: {
                PVLongArray pvdata = (PVLongArray) pv;
                long[] data = new long[1];
                while (len > 0) {
                    String fromString = removeWhiteSpace(from[fromOffset]);
                    data[0] = Long.decode(fromString);
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvFloat: {
                PVFloatArray pvdata = (PVFloatArray) pv;
                float[] data = new float[1];
                while (len > 0) {
                    String fromString = removeWhiteSpace(from[fromOffset]);
                    data[0] = Float.valueOf(fromString);
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvDouble: {
                PVDoubleArray pvdata = (PVDoubleArray) pv;
                double[] data = new double[1];
                while (len > 0) {
                    String fromString = removeWhiteSpace(from[fromOffset]);
                    data[0] = Double.valueOf(fromString);
                    if (pvdata.put(offset, 1, data, 0) == 0)
                        return ntransfered;
                    --len;
                    ++ntransfered;
                    ++offset;
                    ++fromOffset;
                }
                return ntransfered;
            }
            case pvString:
                PVStringArray pvdata = (PVStringArray) pv;
                while (len > 0) {
                    int n = pvdata.put(offset, len, from, fromOffset);
                    if (n == 0)
                        break;
                    len -= n;
                    offset += n;
                    fromOffset += n;
                    ntransfered += n;
                }
                return ntransfered;
            }
            throw new IllegalStateException(
                    "Logic error. Should never get here");
        }

        private int convertToStringArray(PVScalarArray pv, int offset, int len,
                String[] to, int toOffset) {
            ScalarType elementType = pv.getScalarArray().getElementType();
            int ncopy = pv.getLength();
            if (ncopy > len)
                ncopy = len;
            int num = ncopy;
            switch (elementType) {
            case pvBoolean: {
                PVBooleanArray pvdata = (PVBooleanArray) pv;
                BooleanArrayData data = new BooleanArrayData();
                for (int i = 0; i < num; i++) {
                    if (pvdata.get(offset + i, 1, data) == 1) {
                        to[toOffset + i] = Boolean.toString(data.data[data.offset]);
                    } else {
                        to[toOffset + i] = "bad pv";
                    }
                }
            }
                break;
            case pvByte: {
                PVByteArray pvdata = (PVByteArray) pv;
                ByteArrayData data = new ByteArrayData();
                for (int i = 0; i < num; i++) {
                    if (pvdata.get(offset + i, 1, data) == 1) {
                        to[toOffset + i] = Byte.toString(data.data[data.offset]);
                    } else {
                        to[toOffset + i] = "bad pv";
                    }
                }
            }
                break;
            case pvShort: {
                PVShortArray pvdata = (PVShortArray) pv;
                ShortArrayData data = new ShortArrayData();
                for (int i = 0; i < num; i++) {
                    if (pvdata.get(offset + i, 1, data) == 1) {
                        to[toOffset + i] = Short.toString(data.data[data.offset]);
                    } else {
                        to[toOffset + i] = "bad pv";
                    }
                }
            }
                break;
            case pvInt: {
                PVIntArray pvdata = (PVIntArray) pv;
                IntArrayData data = new IntArrayData();
                for (int i = 0; i < num; i++) {
                    if (pvdata.get(offset + i, 1, data) == 1) {
                        to[toOffset + i] = Integer.toString(data.data[data.offset]);
                    } else {
                        to[toOffset + i] = "bad pv";
                    }
                }
            }
                break;
            case pvLong: {
                PVLongArray pvdata = (PVLongArray) pv;
                LongArrayData data = new LongArrayData();
                for (int i = 0; i < num; i++) {
                    if (pvdata.get(offset + i, 1, data) == 1) {
                        to[toOffset + i] = Long.toString(data.data[data.offset]);
                    } else {
                        to[toOffset + i] = "bad pv";
                    }
                }
            }
                break;
            case pvUByte: {
                PVUByteArray pvdata = (PVUByteArray) pv;
                ByteArrayData data = new ByteArrayData();
                for (int i = 0; i < num; i++) {
                    if (pvdata.get(offset + i, 1, data) == 1) {
                        byte value = data.data[i];
                        String svalue = Byte.toString(value);
                        if(value<0) svalue = svalue.substring(1);
                        to[toOffset + i] = svalue;
                    } else {
                        to[toOffset + i] = "bad pv";
                    }
                }
            }
                break;
            case pvUShort: {
                PVUShortArray pvdata = (PVUShortArray) pv;
                ShortArrayData data = new ShortArrayData();
                for (int i = 0; i < num; i++) {
                    if (pvdata.get(offset + i, 1, data) == 1) {
                        short value = data.data[i];
                        String svalue = Short.toString(value);
                        if(value<0) svalue = svalue.substring(1);
                        to[toOffset + i] = svalue;
                    } else {
                        to[toOffset + i] = "bad pv";
                    }
                }
            }
                break;
            case pvUInt: {
                PVUIntArray pvdata = (PVUIntArray) pv;
                IntArrayData data = new IntArrayData();
                for (int i = 0; i < num; i++) {
                    if (pvdata.get(offset + i, 1, data) == 1) {
                        int value = data.data[i];
                        String svalue = Integer.toString(value);
                        if(value<0) svalue = svalue.substring(1);
                        to[toOffset + i] = svalue;
                    } else {
                        to[toOffset + i] = "bad pv";
                    }
                }
            }
                break;
            case pvULong: {
                PVULongArray pvdata = (PVULongArray) pv;
                LongArrayData data = new LongArrayData();
                for (int i = 0; i < num; i++) {
                    if (pvdata.get(offset + i, 1, data) == 1) {
                        long value = data.data[i];
                        String svalue = Long.toString(value);
                        if(value<0) svalue = svalue.substring(1);
                        to[toOffset + i] = svalue;
                    } else {
                        to[toOffset + i] = "bad pv";
                    }
                }
            }
                break;
                
            case pvFloat: {
                PVFloatArray pvdata = (PVFloatArray) pv;
                FloatArrayData data = new FloatArrayData();
                for (int i = 0; i < num; i++) {
                    if (pvdata.get(offset + i, 1, data) == 1) {
                        to[toOffset + i] = Float.toString(data.data[data.offset]);
                    } else {
                        to[toOffset + i] = "bad pv";
                    }
                }
            }
                break;
            case pvDouble: {
                PVDoubleArray pvdata = (PVDoubleArray) pv;
                DoubleArrayData data = new DoubleArrayData();
                for (int i = 0; i < num; i++) {
                    if (pvdata.get(offset + i, 1, data) == 1) {
                        to[toOffset + i] = Double.toString(data.data[data.offset]);
                    } else {
                        to[toOffset + i] = "bad pv";
                    }
                }
            }
                break;
            case pvString: {
                PVStringArray pvdata = (PVStringArray) pv;
                while (num > 0) {
                    int numnow = 0;
                    String[] dataArray = null;
                    int dataOffset = 0;
                    synchronized (stringArrayData) {
                        numnow = pvdata.get(offset, num, stringArrayData);
                        dataArray = stringArrayData.data;
                        dataOffset = stringArrayData.offset;
                    }
                    if (numnow <= 0) {
                        for (int i = 0; i < num; i++)
                            to[toOffset + i] = "bad pv";
                        break;
                    }
                    System.arraycopy(dataArray, dataOffset, to, toOffset, num);
                    num -= numnow;
                    offset += numnow;
                    toOffset += numnow;
                }
            }
                break;
            default:
                throw new IllegalArgumentException(
                        "Illegal ScalarType. Must be scalar but it is "
                                + elementType.toString());
            }
            return ncopy;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.epics.pvdata.pv.Convert#newLine(java.lang.StringBuilder,
         * int)
         */
        public void newLine(StringBuilder builder, int indentLevel) {
            builder.append(String.format("%n"));
            for (int i = 0; i < indentLevel; i++)
                builder.append(indentString);
        }

        private static String indentString = "    ";

        private void convertToString(StringBuilder builder,PVField pv, int indentLevel) {
            Type type = pv.getField().getType();
            if (type == Type.scalarArray) {
                convertArray(builder,(PVScalarArray) pv, indentLevel);
                return;
            }
            if (type == Type.structure) {
                convertStructure(builder,(PVStructure) pv, indentLevel);
                return;
            }
            if (type == Type.structureArray) {
                convertStructureArray(builder,(PVStructureArray) pv, indentLevel);
                return;
            }
            PVScalar pvScalar = (PVScalar) pv;
            ScalarType scalarType = pvScalar.getScalar().getScalarType();
            builder.append(scalarType.toString() + " " + pv.getFieldName());
            builder.append(" ");
            switch (scalarType) {
            case pvBoolean: {
                PVBoolean data = (PVBoolean) pv;
                boolean value = data.get();
                if (value) {
                    builder.append("true");
                    return;
                } else {
                    builder.append("false");
                    return;
                }
            }
            case pvByte: {
                PVByte data = (PVByte) pv;
                builder.append(data.get());
                return;
            }
            case pvShort: {
                PVShort data = (PVShort) pv;
                builder.append(data.get());
                return;
            }
            case pvInt: {
                PVInt data = (PVInt) pv;
                builder.append(data.get());
                return;
            }
            case pvLong: {
                PVLong data = (PVLong) pv;
                builder.append(data.get());
                return;
            }
            case pvUByte: {
                PVUByte data = (PVUByte) pv;
                byte val = data.get();
                short value = val;
                value &=0x0ff;
                builder.append(value);
                return;
            }
            case pvUShort: {
                PVUShort data = (PVUShort) pv;
                short val = data.get();
                int value = val;
                value &=0x0ffff;
                builder.append(value);
                return;
            }
            case pvUInt: {
                PVUInt data = (PVUInt) pv;
                int val = data.get();
                long value = val;
                value &=0x0ffffffffL;
                builder.append(value);
                return;
            }
            case pvULong: {
                PVULong data = (PVULong) pv;
                long val = data.get();
                String value = Long.toString(val);
                if(val<0) {
                    if(value.startsWith("-", 0)) {
                        value = value.substring(1);
                    }
                }
                builder.append(value);
                return;
            }
            case pvFloat: {
                PVFloat data = (PVFloat) pv;
                builder.append(data.get());
                return;
            }
            case pvDouble: {
                PVDouble data = (PVDouble) pv;
                builder.append(data.get());
                return;
            }
            case pvString: {
                PVString data = (PVString) pv;
                builder.append(data.get());
                return;
            }
            default:
                builder.append("unknown ScalarType");
                return;
            }
        }

        private void convertStructure(StringBuilder buffer,PVStructure data, int indentLevel) {
            buffer.append("structure ");
            String id = data.getStructure().getID();
            if (!id.isEmpty()) buffer.append(id).append(' ');
            buffer.append(data.getFieldName());
            String extendsName = data.getExtendsStructureName();
            if(extendsName!=null && extendsName.length()>0) {
                buffer.append(" extends " +extendsName);
                buffer.append(" ");
            }
            PVField[] fieldsData = data.getPVFields();
            if (fieldsData != null) {
                int length = fieldsData.length;
                for(int i=0; i<length; i++) {
                    newLine(buffer, indentLevel + 1);
                    PVField fieldField = fieldsData[i];
                    fieldField.toString(buffer,indentLevel + 1);
                }
            }
        }

        private void convertArray(StringBuilder builder,PVScalarArray pv, int indentLevel) {
            ScalarArray array = pv.getScalarArray();
            ScalarType type = array.getElementType();
            builder.append(pv.getScalarArray().getElementType().toString());
            builder.append("[] ");
            PVStructure parent = pv.getParent();
            if(parent!=null) {
                PVField[] pvFields = parent.getPVFields();
                for(int i=0; i<pvFields.length; i++) {
                    if(pvFields[i]==pv) {
                        builder.append(" ");
                        builder.append(parent.getStructure().getFieldName(i));
                        break;
                    }
                }
            }
            builder.append(" ");
            switch (type) {
            case pvBoolean:
            {
                PVBooleanArray pvdata = (PVBooleanArray) pv;
                builder.append("[");
                int len = pvdata.getLength();
                int offset = 0;
                boolean firstElement = true;
                while (len > 0) {
                    int num = 0;
                    boolean[] data = null;
                    int fromOffset = 0;
                    synchronized (booleanArrayData) {
                        num = pvdata.get(offset, len, booleanArrayData);
                        data = booleanArrayData.data;
                        fromOffset = booleanArrayData.offset;
                    }
                    for (int i = 0; i < num; i++) {
                        if (!firstElement) {
                            builder.append(',');
                        } else {
                            firstElement = false;
                        }
                        builder.append(data[i + fromOffset] ? "true" : "false");
                    }
                    len -= num;
                }
                builder.append("]");
                break;
            }
            case pvByte:
            {
                PVByteArray pvdata = (PVByteArray) pv;
                builder.append("[");
                int len = pvdata.getLength();
                int offset = 0;
                boolean firstElement = true;
                while (len > 0) {
                    int num = 0;
                    byte[] data = null;
                    int fromOffset = 0;
                    synchronized (byteArrayData) {
                        num = pvdata.get(offset, len, byteArrayData);
                        data = byteArrayData.data;
                        fromOffset = byteArrayData.offset;
                    }
                    for (int i = 0; i < num; i++) {
                        if (!firstElement) {
                            builder.append(',');
                        } else {
                            firstElement = false;
                        }
                        builder.append(data[i + fromOffset]);
                    }
                    len -= num;
                }
                builder.append("]");
                break;
            }
            
            case pvShort:
            {
                PVShortArray pvdata = (PVShortArray) pv;
                builder.append("[");
                int len = pvdata.getLength();
                int offset = 0;
                boolean firstElement = true;
                while (len > 0) {
                    int num = 0;
                    short[] data = null;
                    int fromOffset = 0;
                    synchronized (shortArrayData) {
                        num = pvdata.get(offset, len, shortArrayData);
                        data = shortArrayData.data;
                        fromOffset = shortArrayData.offset;
                    }
                    for (int i = 0; i < num; i++) {
                        if (!firstElement) {
                            builder.append(',');
                        } else {
                            firstElement = false;
                        }
                        builder.append(data[i + fromOffset]);
                    }
                    len -= num;
                }
                builder.append("]");
                break;
            }
            case pvInt:
            {
                PVIntArray pvdata = (PVIntArray) pv;
                builder.append("[");
                int len = pvdata.getLength();
                int offset = 0;
                boolean firstElement = true;
                while (len > 0) {
                    int num = 0;
                    int[] data = null;
                    int fromOffset = 0;
                    synchronized (intArrayData) {
                        num = pvdata.get(offset, len, intArrayData);
                        data = intArrayData.data;
                        fromOffset = intArrayData.offset;
                    }
                    for (int i = 0; i < num; i++) {
                        if (!firstElement) {
                            builder.append(',');
                        } else {
                            firstElement = false;
                        }
                        builder.append(data[i + fromOffset]);
                    }
                    len -= num;
                }
                builder.append("]");
                break;
            }
            case pvLong:
            {
                PVLongArray pvdata = (PVLongArray) pv;
                builder.append("[");
                int len = pvdata.getLength();
                int offset = 0;
                boolean firstElement = true;
                while (len > 0) {
                    int num = 0;
                    long[] data = null;
                    int fromOffset = 0;
                    synchronized (longArrayData) {
                        num = pvdata.get(offset, len, longArrayData);
                        data = longArrayData.data;
                        fromOffset = longArrayData.offset;
                    }
                    for (int i = 0; i < num; i++) {
                        if (!firstElement) {
                            builder.append(',');
                        } else {
                            firstElement = false;
                        }
                        builder.append(data[i + fromOffset]);
                    }
                    len -= num;
                }
                builder.append("]");
                break;
            }
            case pvUByte:
            {
                PVUByteArray pvdata = (PVUByteArray) pv;
                builder.append("[");
                int len = pvdata.getLength();
                int offset = 0;
                boolean firstElement = true;
                while (len > 0) {
                    int num = 0;
                    byte[] data = null;
                    int fromOffset = 0;
                    synchronized (byteArrayData) {
                        num = pvdata.get(offset, len, byteArrayData);
                        data = byteArrayData.data;
                        fromOffset = byteArrayData.offset;
                    }
                    for (int i = 0; i < num; i++) {
                        if (!firstElement) {
                            builder.append(',');
                        } else {
                            firstElement = false;
                        }
                        byte val = data[i + fromOffset];
                        String value = Byte.toString(val);
                        if(val<0) value = value.substring(1);
                        builder.append(value);
                    }
                    len -= num;
                }
                builder.append("]");
                break;
            }
            case pvUShort:
            {
                PVUShortArray pvdata = (PVUShortArray) pv;
                builder.append("[");
                int len = pvdata.getLength();
                int offset = 0;
                boolean firstElement = true;
                while (len > 0) {
                    int num = 0;
                    short[] data = null;
                    int fromOffset = 0;
                    synchronized (shortArrayData) {
                        num = pvdata.get(offset, len, shortArrayData);
                        data = shortArrayData.data;
                        fromOffset = shortArrayData.offset;
                    }
                    for (int i = 0; i < num; i++) {
                        if (!firstElement) {
                            builder.append(',');
                        } else {
                            firstElement = false;
                        }
                        short val = data[i + fromOffset];
                        String value = Short.toString(val);
                        if(val<0) value = value.substring(1);
                        builder.append(value);
                    }
                    len -= num;
                }
                builder.append("]");
                break;
            }
            case pvUInt:
            {
                PVUIntArray pvdata = (PVUIntArray) pv;
                builder.append("[");
                int len = pvdata.getLength();
                int offset = 0;
                boolean firstElement = true;
                while (len > 0) {
                    int num = 0;
                    int[] data = null;
                    int fromOffset = 0;
                    synchronized (intArrayData) {
                        num = pvdata.get(offset, len, intArrayData);
                        data = intArrayData.data;
                        fromOffset = intArrayData.offset;
                    }
                    for (int i = 0; i < num; i++) {
                        if (!firstElement) {
                            builder.append(',');
                        } else {
                            firstElement = false;
                        }
                        int val = data[i + fromOffset];
                        String value = Integer.toString(val);
                        if(val<0) value = value.substring(1);
                        builder.append(value);
                    }
                    len -= num;
                }
                builder.append("]");
                break;
            }
            case pvULong:
            {
                PVULongArray pvdata = (PVULongArray) pv;
                builder.append("[");
                int len = pvdata.getLength();
                int offset = 0;
                boolean firstElement = true;
                while (len > 0) {
                    int num = 0;
                    long[] data = null;
                    int fromOffset = 0;
                    synchronized (longArrayData) {
                        num = pvdata.get(offset, len, longArrayData);
                        data = longArrayData.data;
                        fromOffset = longArrayData.offset;
                    }
                    for (int i = 0; i < num; i++) {
                        if (!firstElement) {
                            builder.append(',');
                        } else {
                            firstElement = false;
                        }
                        long val = data[i + fromOffset];
                        String value = Long.toString(val);
                        if(val<0) value = value.substring(1);
                        builder.append(value);
                    }
                    len -= num;
                }
                builder.append("]");
                break;
            }
            case pvFloat:
            {
                PVFloatArray pvdata = (PVFloatArray) pv;
                builder.append("[");
                int len = pvdata.getLength();
                int offset = 0;
                boolean firstElement = true;
                while (len > 0) {
                    int num = 0;
                    float[] data = null;
                    int fromOffset = 0;
                    synchronized (floatArrayData) {
                        num = pvdata.get(offset, len, floatArrayData);
                        data = floatArrayData.data;
                        fromOffset = floatArrayData.offset;
                    }
                    for (int i = 0; i < num; i++) {
                        if (!firstElement) {
                            builder.append(',');
                        } else {
                            firstElement = false;
                        }
                        builder.append(String
                                .format("%g", data[i + fromOffset]));
                    }
                    len -= num;
                }
                builder.append("]");
                break;
            }
            case pvDouble:
            {
                PVDoubleArray pvdata = (PVDoubleArray) pv;
                builder.append("[");
                int len = pvdata.getLength();
                int offset = 0;
                boolean firstElement = true;
                while (len > 0) {
                    int num = 0;
                    double[] data = null;
                    int fromOffset = 0;
                    synchronized (doubleArrayData) {
                        num = pvdata.get(offset, len, doubleArrayData);
                        data = doubleArrayData.data;
                        fromOffset = doubleArrayData.offset;
                    }
                    for (int i = 0; i < num; i++) {
                        if (!firstElement) {
                            builder.append(',');
                        } else {
                            firstElement = false;
                        }
                        builder.append(String.format("%g", data[i + fromOffset]));
                    }
                    len -= num;
                }
                builder.append("]");
                break;
            }
            case pvString:
            {
                PVStringArray pvdata = (PVStringArray) pv;
                StringArrayData data = new StringArrayData();
                builder.append("[");
                for (int i = 0; i < pvdata.getLength(); i++) {
                    if (i != 0)
                        builder.append(",");
                    int num = pvdata.get(i, 1, data);
                    String[] value = data.data;
                    if (num == 1 && value[data.offset] != null) {
                        builder.append(value[data.offset]);
                    } else {
                        builder.append("null");
                    }
                }
                builder.append("]");
                break;
            }
            default:
                builder.append(" array element is unknown ScalarType");
            }
            if (pv.isImmutable()) {
                builder.append(" immutable ");
            }
        }

        private void convertStructureArray(StringBuilder builder,PVStructureArray pvdata,int indentLevel) {
            builder.append("structure[] ");
            PVStructure parent = pvdata.getParent();
            if(parent!=null) {
                PVField[] pvFields = parent.getPVFields();
                for(int i=0; i<pvFields.length; i++) {
                    if(pvFields[i]==pvdata) {
                        builder.append(" ");
                        builder.append(parent.getStructure().getFieldName(i));
                        break;
                    }
                }
            }
            builder.append(" ");
            int length = pvdata.getLength();
            if(length<=0) {
                return;
            }
            StructureArrayData data = new StructureArrayData();
            pvdata.get(0, pvdata.getLength(), data);
            for (int i = 0; i < pvdata.getLength(); i++) {
                newLine(builder, indentLevel + 1);
                PVStructure pvStructure = data.data[i];
                if (pvStructure == null) {
                    builder.append("null");
                } else {
                    pvStructure.toString(builder, indentLevel+1);
                }
            }
        }

        private int copyArrayDataReference(PVScalarArray from, PVArray to) {
            ScalarType scalarType = from.getScalarArray().getElementType();
            switch (scalarType) {
            case pvBoolean: {
                PVBooleanArray pvfrom = (PVBooleanArray) from;
                PVBooleanArray pvto = (PVBooleanArray) to;
                boolean[] booleanArray = null;
                synchronized (booleanArrayData) {
                    pvfrom.get(0, pvfrom.getLength(), booleanArrayData);
                    booleanArray = booleanArrayData.data;
                }
                pvto.shareData(booleanArray);

                break;
            }
            case pvByte: {
                PVByteArray pvfrom = (PVByteArray) from;
                PVByteArray pvto = (PVByteArray) to;
                byte[] byteArray = null;
                synchronized (byteArrayData) {
                    pvfrom.get(0, pvfrom.getLength(), byteArrayData);
                    byteArray = byteArrayData.data;
                }
                pvto.shareData(byteArray);
                break;
            }
            case pvShort: {
                PVShortArray pvfrom = (PVShortArray) from;
                PVShortArray pvto = (PVShortArray) to;
                short[] shortArray = null;
                synchronized (shortArrayData) {
                    pvfrom.get(0, pvfrom.getLength(), shortArrayData);
                    shortArray = shortArrayData.data;
                }
                pvto.shareData(shortArray);
                break;
            }
            case pvInt: {
                PVIntArray pvfrom = (PVIntArray) from;
                PVIntArray pvto = (PVIntArray) to;
                int[] intArray = null;
                synchronized (intArrayData) {
                    pvfrom.get(0, pvfrom.getLength(), intArrayData);
                    intArray = intArrayData.data;
                }
                pvto.shareData(intArray);
                break;
            }
            case pvLong: {
                PVLongArray pvfrom = (PVLongArray) from;
                PVLongArray pvto = (PVLongArray) to;
                long[] longArray = null;
                synchronized (longArrayData) {
                    pvfrom.get(0, pvfrom.getLength(), longArrayData);
                    longArray = longArrayData.data;
                }
                pvto.shareData(longArray);
                break;
            }
            case pvFloat: {
                PVFloatArray pvfrom = (PVFloatArray) from;
                PVFloatArray pvto = (PVFloatArray) to;
                float[] floatArray = null;
                synchronized (floatArrayData) {
                    pvfrom.get(0, pvfrom.getLength(), floatArrayData);
                    floatArray = floatArrayData.data;
                }
                pvto.shareData(floatArray);
                break;
            }
            case pvDouble: {
                PVDoubleArray pvfrom = (PVDoubleArray) from;
                PVDoubleArray pvto = (PVDoubleArray) to;
                double[] doubleArray = null;
                synchronized (doubleArrayData) {
                    pvfrom.get(0, pvfrom.getLength(), doubleArrayData);
                    doubleArray = doubleArrayData.data;
                }
                pvto.shareData(doubleArray);
                break;
            }
            case pvString: {
                PVStringArray pvfrom = (PVStringArray) from;
                PVStringArray pvto = (PVStringArray) to;
                String[] stringArray = null;
                synchronized (stringArrayData) {
                    pvfrom.get(0, pvfrom.getLength(), stringArrayData);
                    stringArray = stringArrayData.data;
                }
                pvto.shareData(stringArray);
                break;
            }
            }
            to.setImmutable();
            return from.getLength();
        }

        private int copyNumericArray(PVScalarArray from, int offset,
                PVScalarArray to, int toOffset, int len) {
            ScalarType fromElementType = from.getScalarArray().getElementType();
            int ncopy = 0;
            switch (fromElementType) {
            case pvByte: {
                PVByteArray pvfrom = (PVByteArray) from;
                while (len > 0) {
                    int num = 0;
                    byte[] data = null;
                    int dataOffset = 0;
                    synchronized (byteArrayData) {
                        num = pvfrom.get(offset, len, byteArrayData);
                        data = byteArrayData.data;
                        dataOffset = byteArrayData.offset;
                    }
                    if (num <= 0)
                        break;
                    while (num > 0) {
                        int n = convertFromByteArray(to, toOffset, num, data,
                                dataOffset);
                        if (n <= 0)
                            break;
                        len -= n;
                        num -= n;
                        ncopy += n;
                        offset += n;
                        toOffset += n;
                    }
                }
                break;
            }
            case pvShort: {
                PVShortArray pvfrom = (PVShortArray) from;
                while (len > 0) {
                    int num = 0;
                    short[] data = null;
                    int dataOffset = 0;
                    synchronized (shortArrayData) {
                        num = pvfrom.get(offset, len, shortArrayData);
                        data = shortArrayData.data;
                        dataOffset = shortArrayData.offset;
                    }
                    if (num <= 0)
                        break;
                    while (num > 0) {
                        int n = convertFromShortArray(to, toOffset, num, data,
                                dataOffset);
                        if (n <= 0)
                            break;
                        len -= n;
                        num -= n;
                        ncopy += n;
                        offset += n;
                        toOffset += n;
                    }
                }
                break;
            }
            case pvInt: {
                PVIntArray pvfrom = (PVIntArray) from;
                while (len > 0) {
                    int num = 0;
                    int[] data = null;
                    int dataOffset = 0;
                    synchronized (intArrayData) {
                        num = pvfrom.get(offset, len, intArrayData);
                        data = intArrayData.data;
                        dataOffset = intArrayData.offset;
                    }
                    if (num <= 0)
                        break;
                    while (num > 0) {
                        int n = convertFromIntArray(to, toOffset, num, data,
                                dataOffset);
                        if (n <= 0)
                            break;
                        len -= n;
                        num -= n;
                        ncopy += n;
                        offset += n;
                        toOffset += n;
                    }
                }
                break;
            }
            case pvLong: {
                PVLongArray pvfrom = (PVLongArray) from;
                while (len > 0) {
                    int num = 0;
                    long[] data = null;
                    int dataOffset = 0;
                    synchronized (longArrayData) {
                        num = pvfrom.get(offset, len, longArrayData);
                        data = longArrayData.data;
                        dataOffset = longArrayData.offset;
                    }
                    if (num <= 0)
                        break;
                    while (num > 0) {
                        int n = convertFromLongArray(to, toOffset, num, data,
                                dataOffset);
                        if (n <= 0)
                            break;
                        len -= n;
                        num -= n;
                        ncopy += n;
                        offset += n;
                        toOffset += n;
                    }
                }
                break;
            }
            case pvUByte: {
                PVUByteArray pvfrom = (PVUByteArray) from;
                while (len > 0) {
                    int num = 0;
                    byte[] data = null;
                    int dataOffset = 0;
                    synchronized (byteArrayData) {
                        num = pvfrom.get(offset, len, byteArrayData);
                        data = byteArrayData.data;
                        dataOffset = byteArrayData.offset;
                    }
                    if (num <= 0)
                        break;
                    while (num > 0) {
                        int n = convertFromUByteArray(to, toOffset, num, data,
                                dataOffset);
                        if (n <= 0)
                            break;
                        len -= n;
                        num -= n;
                        ncopy += n;
                        offset += n;
                        toOffset += n;
                    }
                }
                break;
            }
            case pvUShort: {
                PVUShortArray pvfrom = (PVUShortArray) from;
                while (len > 0) {
                    int num = 0;
                    short[] data = null;
                    int dataOffset = 0;
                    synchronized (shortArrayData) {
                        num = pvfrom.get(offset, len, shortArrayData);
                        data = shortArrayData.data;
                        dataOffset = shortArrayData.offset;
                    }
                    if (num <= 0)
                        break;
                    while (num > 0) {
                        int n = convertFromUShortArray(to, toOffset, num, data,
                                dataOffset);
                        if (n <= 0)
                            break;
                        len -= n;
                        num -= n;
                        ncopy += n;
                        offset += n;
                        toOffset += n;
                    }
                }
                break;
            }
            case pvUInt: {
                PVUIntArray pvfrom = (PVUIntArray) from;
                while (len > 0) {
                    int num = 0;
                    int[] data = null;
                    int dataOffset = 0;
                    synchronized (intArrayData) {
                        num = pvfrom.get(offset, len, intArrayData);
                        data = intArrayData.data;
                        dataOffset = intArrayData.offset;
                    }
                    if (num <= 0)
                        break;
                    while (num > 0) {
                        int n = convertFromUIntArray(to, toOffset, num, data,
                                dataOffset);
                        if (n <= 0)
                            break;
                        len -= n;
                        num -= n;
                        ncopy += n;
                        offset += n;
                        toOffset += n;
                    }
                }
                break;
            }
            case pvULong: {
                PVULongArray pvfrom = (PVULongArray) from;
                while (len > 0) {
                    int num = 0;
                    long[] data = null;
                    int dataOffset = 0;
                    synchronized (longArrayData) {
                        num = pvfrom.get(offset, len, longArrayData);
                        data = longArrayData.data;
                        dataOffset = longArrayData.offset;
                    }
                    if (num <= 0)
                        break;
                    while (num > 0) {
                        int n = convertFromULongArray(to, toOffset, num, data,
                                dataOffset);
                        if (n <= 0)
                            break;
                        len -= n;
                        num -= n;
                        ncopy += n;
                        offset += n;
                        toOffset += n;
                    }
                }
                break;
            }
            case pvFloat: {
                PVFloatArray pvfrom = (PVFloatArray) from;
                while (len > 0) {
                    int num = 0;
                    float[] data = null;
                    int dataOffset = 0;
                    synchronized (floatArrayData) {
                        num = pvfrom.get(offset, len, floatArrayData);
                        data = floatArrayData.data;
                        dataOffset = floatArrayData.offset;
                    }
                    if (num <= 0)
                        break;
                    while (num > 0) {
                        int n = fromFloatArray(to, toOffset, num, data,
                                dataOffset);
                        if (n <= 0)
                            break;
                        len -= n;
                        num -= n;
                        ncopy += n;
                        offset += n;
                        toOffset += n;
                    }
                }
                break;
            }
            case pvDouble: {
                PVDoubleArray pvfrom = (PVDoubleArray) from;
                while (len > 0) {
                    int num = 0;
                    double[] data = null;
                    int dataOffset = 0;
                    synchronized (doubleArrayData) {
                        num = pvfrom.get(offset, len, doubleArrayData);
                        data = doubleArrayData.data;
                        dataOffset = doubleArrayData.offset;
                    }
                    if (num <= 0)
                        break;
                    while (num > 0) {
                        int n = fromDoubleArray(to, toOffset, num, data,
                                dataOffset);
                        if (n <= 0)
                            break;
                        len -= n;
                        num -= n;
                        ncopy += n;
                        offset += n;
                        toOffset += n;
                    }
                }
                break;
            }
            }
            return ncopy;
        }

    }
}
