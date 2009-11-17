/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.factory;

import java.util.regex.Pattern;

import org.epics.pvData.pv.Array;
import org.epics.pvData.pv.BooleanArrayData;
import org.epics.pvData.pv.ByteArrayData;
import org.epics.pvData.pv.Convert;
import org.epics.pvData.pv.DoubleArrayData;
import org.epics.pvData.pv.Field;
import org.epics.pvData.pv.FloatArrayData;
import org.epics.pvData.pv.IntArrayData;
import org.epics.pvData.pv.LongArrayData;
import org.epics.pvData.pv.PVArray;
import org.epics.pvData.pv.PVBoolean;
import org.epics.pvData.pv.PVBooleanArray;
import org.epics.pvData.pv.PVByte;
import org.epics.pvData.pv.PVByteArray;
import org.epics.pvData.pv.PVDouble;
import org.epics.pvData.pv.PVDoubleArray;
import org.epics.pvData.pv.PVField;
import org.epics.pvData.pv.PVFloat;
import org.epics.pvData.pv.PVFloatArray;
import org.epics.pvData.pv.PVInt;
import org.epics.pvData.pv.PVIntArray;
import org.epics.pvData.pv.PVLong;
import org.epics.pvData.pv.PVLongArray;
import org.epics.pvData.pv.PVScalar;
import org.epics.pvData.pv.PVShort;
import org.epics.pvData.pv.PVShortArray;
import org.epics.pvData.pv.PVString;
import org.epics.pvData.pv.PVStringArray;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.Scalar;
import org.epics.pvData.pv.ScalarType;
import org.epics.pvData.pv.ShortArrayData;
import org.epics.pvData.pv.StringArrayData;
import org.epics.pvData.pv.Structure;
import org.epics.pvData.pv.Type;

/**
 * Factory to obtain the implementation of <i>Convert</i>
 * @author mrktestByteArrayCopy
 *
 */
public final class ConvertFactory {
    /**
     * Implements <i>Convert</i>.
     * The implementation ensures that a single instance is created.
     * @return the implementation of <i>Convert</i>
     */
    public static Convert getConvert()
    {
    	return ImplementConvert.getConvert();
    }

    private static final class ImplementConvert implements Convert{
        private static final Pattern separatorPattern = Pattern.compile("[,]");
        private static ImplementConvert singleImplementation = null;
        private static synchronized ImplementConvert getConvert() {
                if (singleImplementation==null) {
                    singleImplementation = new ImplementConvert();
                }
                return singleImplementation;
        }
        // The following are to prevent continually creation of new objects
        static BooleanArrayData booleanArrayData = new BooleanArrayData();
        static ByteArrayData byteArrayData = new ByteArrayData();
        static ShortArrayData shortArrayData = new ShortArrayData();
        static IntArrayData intArrayData = new IntArrayData();
        static LongArrayData longArrayData = new LongArrayData();
        static FloatArrayData floatArrayData = new FloatArrayData();
        static DoubleArrayData doubleArrayData = new DoubleArrayData();
        static StringArrayData stringArrayData = new StringArrayData();
        // Guarantee that ImplementConvert can only be created via getConvert
        private ImplementConvert() {}
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.Convert#getString(org.epics.pvData.pv.PVField)
         */
        @Override
        public String getString(PVField pv) {
            return convertToString(pv,0);
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.Convert#getString(org.epics.pvData.pv.PVField, int)
         */
        @Override
        public String getString(PVField pv,int indentLevel) {
            return convertToString(pv,indentLevel);
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.Convert#fromString(org.epics.pvData.pv.PVScalar, java.lang.String)
         */
        @Override
        public void fromString(PVScalar pv, String from) {
            Field field = pv.getField();
            Type type = field.getType();
            if(field.getType()!=Type.scalar) {
                throw new IllegalArgumentException(
                        "Illegal PVType. Must be numeric but it is "
                        + type.toString()
                      );
            }
            Scalar scalar = (Scalar)field;
            ScalarType scalarType = scalar.getScalarType();
            switch(scalarType) {
                case pvBoolean: {
                        PVBoolean value = (PVBoolean)pv;
                        value.put(Boolean.parseBoolean(from));
                        break;
                    }
                case pvByte : {
                        PVByte value = (PVByte)pv;
                        value.put((byte)(long)Long.decode(from));
                        break;
                    }
                case pvShort : {
                        PVShort value = (PVShort)pv;
                        value.put((short)(long)Long.decode(from));
                        break;
                    }
                case pvInt : {
                        PVInt value = (PVInt)pv;
                        value.put((int)(long)Long.decode(from));
                        break;
                    }
                case pvLong : {
                        PVLong value = (PVLong)pv;
                        value.put(Long.decode(from));
                        break;
                    }
                case pvFloat : {
                        PVFloat value = (PVFloat)pv;
                        value.put(Float.valueOf(from));
                        break;
                    }
                case pvDouble : {
                        PVDouble value = (PVDouble)pv;
                        value.put(Double.valueOf(from));
                        break;
                    }
                case pvString: {
                        PVString value = (PVString)pv;
                        value.put(from);
                        break;
                    }
                default:
                    throw new IllegalArgumentException(
                      "Illegal PVType. Must be numeric but it is "
                      + type.toString()
                    );
            }
        }       
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.Convert#fromString(org.epics.pvData.pv.PVArray, java.lang.String)
         */
        @Override
        public int fromString(PVArray pv, String from) {
            if((from.charAt(0)=='[') && from.endsWith("]")) {
                int offset = from.lastIndexOf(']');
                from = from.substring(1, offset);
            }
            String[] values = separatorPattern.split(from);
            int num = fromStringArray(pv,0,values.length,values,0);
            int length = values.length;
            if(num<length) length = num;
            pv.setLength(length);
            return length;
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.Convert#fromStringArray(org.epics.pvData.pv.PVArray, int, int, java.lang.String[], int)
         */
        @Override
        public int fromStringArray(PVArray pv, int offset, int len,String[] from, int fromOffset)
        {
            return convertFromStringArray(pv,offset,len,from,fromOffset);
        }       
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.Convert#toStringArray(org.epics.pvData.pv.PVArray, int, int, java.lang.String[], int)
         */
        @Override
        public int toStringArray(PVArray pv, int offset, int len, String[] to, int toOffset) {
            return convertToStringArray(pv,offset,len,to,toOffset);
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.Convert#isCopyCompatible(org.epics.pvData.pv.Field, org.epics.pvData.pv.Field)
         */
        @Override
        public boolean isCopyCompatible(Field from, Field to) {
            if(from.getType()!=to.getType()) return false;
            switch(from.getType()) {
            case scalar: 
                return isCopyScalarCompatible((Scalar)from,(Scalar)to);
            case scalarArray:
                return isCopyArrayCompatible((Array)from,(Array)to);
            case structure:
                return isCopyStructureCompatible((Structure)from,(Structure)to);
            }
            throw new IllegalStateException("Logic error. Should never get here");
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.Convert#copy(org.epics.pvData.pv.PVField, org.epics.pvData.pv.PVField)
         */
        @Override
        public void copy(PVField from, PVField to) {
            switch(from.getField().getType()) {
            case scalar: 
                copyScalar((PVScalar)from,(PVScalar)to);
                return;
            case scalarArray:
                PVArray fromArray = (PVArray)from;
                PVArray toArray = (PVArray)to;
                int length = copyArray(fromArray,0,toArray,0,fromArray.getLength());
                if(toArray.getLength()!=length) toArray.setLength(length);
                return;
            case structure:
                copyStructure((PVStructure)from,(PVStructure)to);
                return;
            }
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.Convert#isCopyScalarCompatible(org.epics.pvData.pv.Scalar, org.epics.pvData.pv.Scalar)
         */
        @Override
        public boolean isCopyScalarCompatible(Scalar fromField, Scalar toField) {
            ScalarType fromScalarType = fromField.getScalarType();
            ScalarType toScalarType = toField.getScalarType();
            if(fromScalarType==toScalarType) return true;
            if(fromScalarType.isNumeric()&&toScalarType.isNumeric()) return true;
            if(fromScalarType==ScalarType.pvString) return true;
            if(toScalarType==ScalarType.pvString) return true;
            return false;
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.Convert#copyScalar(org.epics.pvData.pv.PVScalar, org.epics.pvData.pv.PVScalar)
         */
        @Override
        public void copyScalar(PVScalar from, PVScalar to) {
            if(to.isImmutable()) {
                if(from.equals(to)) return;
                throw new IllegalArgumentException("Convert.copyScalar destination is immutable");
            }
            ScalarType fromType = from.getScalar().getScalarType();
            ScalarType toType = to.getScalar().getScalarType();
            switch(fromType) {
            case pvBoolean: {
                    if(toType!=ScalarType.pvBoolean) {
                        if(toType!=ScalarType.pvString) {
                            throw new IllegalArgumentException("Convert.copyScalar arguments are not compatible");
                        }
                    }
                    PVBoolean data = (PVBoolean)from;
                    boolean value = data.get();
                    if(toType==ScalarType.pvString) {
                        PVString dataTo = (PVString)to;
                        dataTo.put(((Boolean)value).toString());
                    } else {
                        PVBoolean dataTo = (PVBoolean)to;
                        dataTo.put(value);
                    }
                    break;
                }
            case pvByte : {
                    PVByte data = (PVByte)from;
                    byte value = data.get();
                    fromByte(to,value);
                    break;
                }
            case pvShort : {
                    PVShort data = (PVShort)from;
                    short value = data.get();
                    fromShort(to,value);
                    break;
                } 
            case pvInt :{
                    PVInt data = (PVInt)from;
                    int value = data.get();
                    fromInt(to,value);
                    break;
                }    
            case pvLong : {
                    PVLong data = (PVLong)from;
                    long value = data.get();
                    fromLong(to,value);
                    break;
                }  
            case pvFloat : {
                    PVFloat data = (PVFloat)from;
                    float value = data.get();
                    fromFloat(to,value);
                    break;
                }     
            case pvDouble : {
                    PVDouble data = (PVDouble)from;
                    double value = data.get();
                    fromDouble(to,value);
                    break;
                }  
            case pvString: {
                    PVString data = (PVString)from;
                    String value = data.get();
                    fromString(to,value);
                    break;
                }
            default:
                throw new IllegalArgumentException(
                        "Convert.copyScalar arguments are not compatible");
            }
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.Convert#isCopyArrayCompatible(org.epics.pvData.pv.Array, org.epics.pvData.pv.Array)
         */
        @Override
        public boolean isCopyArrayCompatible(Array fromArray, Array toArray) {
            ScalarType fromType = fromArray.getElementType();
            ScalarType toType = toArray.getElementType();
            if(fromType==toType) return true;
            if(fromType.isNumeric() && toType.isNumeric()) return true;
            if(toType==ScalarType.pvString) return true;
            if(fromType==ScalarType.pvString) return true;
            return false;
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.Convert#copyArray(org.epics.pvData.pv.PVArray, int, org.epics.pvData.pv.PVArray, int, int)
         */
        @Override
        public int copyArray(PVArray from, int offset, PVArray to, int toOffset, int len)
        {
            if(to.isImmutable()) {
                if(from.equals(to)) return from.getLength();
                throw new IllegalArgumentException("Convert.copyArray destination is immutable");
            }
            ScalarType fromElementType = from.getArray().getElementType();
            ScalarType toElementType = to.getArray().getElementType();
            
            if(from.isImmutable() && (fromElementType==toElementType)) {
                if(offset==0 && toOffset==0 && len==from.getLength()) {
                    return copyArrayDataReference(from,to);
                }
            }
            
            int ncopy = 0;
            if(toElementType.isNumeric() && fromElementType.isNumeric()) {
                ncopy = CopyNumericArray(from,offset,to,toOffset,len);
            } else if(toElementType==ScalarType.pvBoolean && fromElementType==ScalarType.pvBoolean) {
                PVBooleanArray pvfrom = (PVBooleanArray)from;
                PVBooleanArray pvto = (PVBooleanArray)to;
                outer:
                while(len>0) {
                    int num = 0;
                    boolean[] data = null;
                    int fromOffset = 0;
                    synchronized(booleanArrayData) {
                        num = pvfrom.get(offset,len,booleanArrayData);
                        data = booleanArrayData.data;
                        fromOffset = booleanArrayData.offset;
                    }
                    if(num<=0) break;
                    while(num>0) {
                        int n = pvto.put(toOffset,num,data,fromOffset);
                        if(n<=0) break outer;
                        len -= n; num -= n; ncopy+=n; offset += n; toOffset += n; 
                    }
                }
            } else if(toElementType==ScalarType.pvString && fromElementType==ScalarType.pvString) {
                PVStringArray pvfrom = (PVStringArray)from;
                PVStringArray pvto = (PVStringArray)to;
                outer:
                while(len>0) {
                    int num = 0;
                    String[] data = null;
                    int fromOffset = 0;
                    synchronized(stringArrayData) {
                        num = pvfrom.get(offset,len,stringArrayData);
                        data = stringArrayData.data;
                        fromOffset = stringArrayData.offset;
                    }
                    if(num<=0) break;
                    while(num>0) {
                        int n = pvto.put(toOffset,num,data,fromOffset);
                        if(n<=0) break outer;
                        len -= n; num -= n; ncopy+=n; offset += n; toOffset += n; 
                    }
                }
            } else if(toElementType==ScalarType.pvString) {
                PVStringArray pvto = (PVStringArray)to;
                ncopy = from.getLength();
                if(ncopy>len) ncopy = len;
                int num = ncopy;
                String[] toData = new String[1];
                while(num>0) {
                    toStringArray(from,offset,1,toData,0);
                    if(pvto.put(toOffset,1,toData,0)<=0) break;
                    num--; offset++; toOffset++;
                }
            } else if(fromElementType==ScalarType.pvString) {
                PVStringArray pvfrom = (PVStringArray)from;
                outer:
                while(len>0) {
                    int num = 0;
                    String[] data = null;
                    int fromOffset = 0;
                    synchronized(stringArrayData) {
                        num = pvfrom.get(offset,len,stringArrayData);
                        data = stringArrayData.data;
                        fromOffset = booleanArrayData.offset;
                    }
                    if(num<=0) break;
                    while(num>0) {
                        int n = fromStringArray(to,toOffset,num,data,fromOffset);
                        if(n<=0) break outer;
                        len -= n; num -= n; ncopy+=n; offset += n; toOffset += n; 
                    }
                }
            }
            return ncopy;
        }        
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.Convert#isCopyStructureCompatible(org.epics.pvData.pv.Structure, org.epics.pvData.pv.Structure)
         */
        @Override
        public boolean isCopyStructureCompatible(Structure fromStruct, Structure toStruct) {
            Field[] fromFields = fromStruct.getFields();
            Field[] toFields = toStruct.getFields();
            int length = fromFields.length;
            if(length!=toFields.length) return false;
            for(int i=0; i<length; i++) {
                Field from = fromFields[i];
                Field to = toFields[i];
                Type fromType = from.getType();
                Type toType = to.getType();
                if(fromType!=toType) return false;
                switch(fromType) {
                case scalar:
                    if(!isCopyScalarCompatible((Scalar)from,(Scalar)to)) return false;
                    break;
                case scalarArray:
                    if(!isCopyArrayCompatible((Array)from,(Array)to)) return false;
                    break;
                case structure:
                    if(!isCopyStructureCompatible((Structure)from,(Structure)to)) return false;
                }
            }
            return true;
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.Convert#copyStructure(org.epics.pvData.pv.PVStructure, org.epics.pvData.pv.PVStructure)
         */
        @Override
        public void copyStructure(PVStructure from, PVStructure to) {
            if(to.isImmutable()) {
                if(from.equals(to)) return;
                throw new IllegalArgumentException("Convert.copyStructure destination is immutable");
            }
            PVField[] fromDatas = from.getPVFields();
            PVField[] toDatas = to.getPVFields();
            if(fromDatas.length!=toDatas.length) {
                throw new IllegalArgumentException("Illegal copyStructure");
            }
            if(fromDatas.length==2) { // look for enumerated structure and copy choices first
                if(fromDatas[0].getField().getFieldName().equals("index")){
                    Field fieldIndex = fromDatas[0].getField();
                    Field fieldChoices = fromDatas[1].getField();
                    if(fieldIndex.getType()==Type.scalar
                    && fieldChoices.getFieldName().equals("choices")
                    && fieldChoices.getType()==Type.scalarArray) {
                        PVScalar pvScalar = (PVScalar)fromDatas[0];
                        PVArray pvArray = (PVArray)fromDatas[1];
                        if((pvScalar.getScalar().getScalarType()==ScalarType.pvInt)
                        && (pvArray.getArray().getElementType()==ScalarType.pvString)) {
                           PVArray toArray = (PVArray)toDatas[1];
                           copyArray(pvArray,0,toArray,0,pvArray.getLength());
                           PVScalar toScalar = (PVScalar)toDatas[0];
                           copyScalar(pvScalar,toScalar);
                           return;
                        }
                    }
                }
            }
            for(int i=0; i < fromDatas.length; i++) {
                PVField fromData = fromDatas[i];
                PVField toData = toDatas[i];
                Type fromType = fromData.getField().getType();
                Type toType = toData.getField().getType();
                if(fromType!=toType) {
                    throw new IllegalArgumentException("Illegal copyStructure");
                }
                switch(fromType) {
                case scalar:
                    copyScalar((PVScalar)fromData,(PVScalar)toData);
                    break;
                case scalarArray:
                    PVArray fromArray = (PVArray)fromData;
                    PVArray toArray = (PVArray)toData;
                    int length = copyArray(fromArray,0,toArray,0,fromArray.getLength());
                    if(toArray.getLength()!=length) toArray.setLength(length);
                    break;
                case structure:
                    copyStructure((PVStructure)fromData,(PVStructure)toData);
                    break;
                }
            }
        }        
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.Convert#fromByte(org.epics.pvData.pv.PVScalar, byte)
         */
        @Override
        public void fromByte(PVScalar pv, byte from) {
            ScalarType type = pv.getScalar().getScalarType();
            switch(type) {
                case pvByte :
                    {PVByte value = (PVByte)pv; value.put((byte)from); break;}
                case pvShort :
                    {PVShort value = (PVShort)pv; value.put((short)from); break;}
                case pvInt :
                    {PVInt value = (PVInt)pv; value.put((int)from); break;}
                case pvLong :
                    {PVLong value = (PVLong)pv; value.put((long)from); break;}
                case pvFloat :
                    {PVFloat value = (PVFloat)pv; value.put((float)from); break;}
                case pvDouble :
                    {PVDouble value = (PVDouble)pv; value.put((double)from); break;}
                case pvString :
                    {PVString value = (PVString)pv; value.put(String.valueOf(from)); break;}
                default:
                    throw new IllegalArgumentException(
                      "Illegal PVType. Must be numeric but it is "
                      + type.toString()
                    );
            }
        }    
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.Convert#fromByteArray(org.epics.pvData.pv.PVArray, int, int, byte[], int)
         */
        @Override
        public int fromByteArray(PVArray pv, int offset, int len,byte[] from, int fromOffset) {
            int num = convertFromByteArray(pv,offset,len,from,fromOffset);
            return num;
        }    
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.Convert#fromDouble(org.epics.pvData.pv.PVScalar, double)
         */
        @Override
        public void fromDouble(PVScalar pv, double from) {
            ScalarType type = pv.getScalar().getScalarType();
            switch(type) {
                case pvByte :
                    {PVByte value = (PVByte)pv; value.put((byte)from); break;}
                case pvShort :
                    {PVShort value = (PVShort)pv; value.put((short)from); break;}
                case pvInt :
                    {PVInt value = (PVInt)pv; value.put((int)from); break;}
                case pvLong :
                    {PVLong value = (PVLong)pv; value.put((long)from); break;}
                case pvFloat :
                    {PVFloat value = (PVFloat)pv; value.put((float)from); break;}
                case pvDouble :
                    {PVDouble value = (PVDouble)pv; value.put((double)from); break;}
                case pvString :
                    {PVString value = (PVString)pv; value.put(String.valueOf(from)); break;}
                default:
                    throw new IllegalArgumentException(
                      "Illegal PVType. Must be numeric but it is "
                      + type.toString()
                    );
            }
        }    
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.Convert#fromDoubleArray(org.epics.pvData.pv.PVArray, int, int, double[], int)
         */
        @Override
        public int fromDoubleArray(PVArray pv, int offset, int len, double[] from, int fromOffset) {
            int num = convertFromDoubleArray(pv,offset,len,from,fromOffset);
            return num;
        }    
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.Convert#fromFloat(org.epics.pvData.pv.PVScalar, float)
         */
        @Override
        public void fromFloat(PVScalar pv, float from) {
            ScalarType type = pv.getScalar().getScalarType();
            switch(type) {
                case pvByte :
                    {PVByte value = (PVByte)pv; value.put((byte)from); break;}
                case pvShort :
                    {PVShort value = (PVShort)pv; value.put((short)from); break;}
                case pvInt :
                    {PVInt value = (PVInt)pv; value.put((int)from); break;}
                case pvLong :
                    {PVLong value = (PVLong)pv; value.put((long)from); break;}
                case pvFloat :
                    {PVFloat value = (PVFloat)pv; value.put((float)from); break;}
                case pvDouble :
                    {PVDouble value = (PVDouble)pv; value.put((double)from); break;}
                case pvString :
                    {PVString value = (PVString)pv; value.put(String.valueOf(from)); break;}
                default:
                    throw new IllegalArgumentException(
                      "Illegal PVType. Must be numeric but it is "
                      + type.toString()
                    );
            }
        }    
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.Convert#fromFloatArray(org.epics.pvData.pv.PVArray, int, int, float[], int)
         */
        @Override
        public int fromFloatArray(PVArray pv, int offset, int len, float[] from, int fromOffset) {
            int num = convertFromFloatArray(pv,offset,len,from,fromOffset);
            return num;
        }  
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.Convert#fromInt(org.epics.pvData.pv.PVScalar, int)
         */
        @Override
        public void fromInt(PVScalar pv, int from) {
            ScalarType type = pv.getScalar().getScalarType();
            switch(type) {
                case pvByte :
                    {PVByte value = (PVByte)pv; value.put((byte)from); break;}
                case pvShort :
                    {PVShort value = (PVShort)pv; value.put((short)from); break;}
                case pvInt :
                    {PVInt value = (PVInt)pv; value.put((int)from); break;}
                case pvLong :
                    {PVLong value = (PVLong)pv; value.put((long)from); break;}
                case pvFloat :
                    {PVFloat value = (PVFloat)pv; value.put((float)from); break;}
                case pvDouble :
                    {PVDouble value = (PVDouble)pv; value.put((double)from); break;}
                case pvString :
                    {PVString value = (PVString)pv; value.put(String.valueOf(from)); break;}
                default:
                    throw new IllegalArgumentException(
                      "Illegal PVType. Must be numeric but it is "
                      + type.toString()
                    );
            }
        }    
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.Convert#fromIntArray(org.epics.pvData.pv.PVArray, int, int, int[], int)
         */
        @Override
        public int fromIntArray(PVArray pv, int offset, int len,int[] from, int fromOffset) {
            int num = convertFromIntArray(pv,offset,len,from,fromOffset);
            return num;
        }    
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.Convert#fromLong(org.epics.pvData.pv.PVScalar, long)
         */
        @Override
        public void fromLong(PVScalar pv, long from) {
            ScalarType type = pv.getScalar().getScalarType();
            switch(type) {
                case pvByte :
                    {PVByte value = (PVByte)pv; value.put((byte)from); break;}
                case pvShort :
                    {PVShort value = (PVShort)pv; value.put((short)from); break;}
                case pvInt :
                    {PVInt value = (PVInt)pv; value.put((int)from); break;}
                case pvLong :
                    {PVLong value = (PVLong)pv; value.put((long)from); break;}
                case pvFloat :
                    {PVFloat value = (PVFloat)pv; value.put((float)from); break;}
                case pvDouble :
                    {PVDouble value = (PVDouble)pv; value.put((double)from); break;}
                case pvString :
                    {PVString value = (PVString)pv; value.put(String.valueOf(from)); break;}
                default:
                    throw new IllegalArgumentException(
                      "Illegal PVType. Must be numeric but it is "
                      + type.toString()
                    );
            }
        }    
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.Convert#fromLongArray(org.epics.pvData.pv.PVArray, int, int, long[], int)
         */
        @Override
        public int fromLongArray(PVArray pv, int offset, int len,long[] from, int fromOffset) {
            int num = convertFromLongArray(pv,offset,len,from,fromOffset);
            return num;
        }    
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.Convert#fromShort(org.epics.pvData.pv.PVScalar, short)
         */
        @Override
        public void fromShort(PVScalar pv, short from) {
            ScalarType type = pv.getScalar().getScalarType();
            switch(type) {
                case pvByte :
                    {PVByte value = (PVByte)pv; value.put((byte)from); break;}
                case pvShort :
                    {PVShort value = (PVShort)pv; value.put((short)from); break;}
                case pvInt :
                    {PVInt value = (PVInt)pv; value.put((int)from); break;}
                case pvLong :
                    {PVLong value = (PVLong)pv; value.put((long)from); break;}
                case pvFloat :
                    {PVFloat value = (PVFloat)pv; value.put((float)from); break;}
                case pvDouble :
                    {PVDouble value = (PVDouble)pv; value.put((double)from); break;}
                case pvString :
                    {PVString value = (PVString)pv; value.put(String.valueOf(from)); break;}
                default:
                    throw new IllegalArgumentException(
                      "Illegal PVType. Must be numeric but it is "
                      + type.toString()
                    );
            }
        }    
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.Convert#fromShortArray(org.epics.pvData.pv.PVArray, int, int, short[], int)
         */
        @Override
        public int fromShortArray(PVArray pv, int offset, int len, short[] from, int fromOffset) {
            int num = convertFromShortArray(pv,offset,len,from,fromOffset);
            return num;
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.Convert#toByte(org.epics.pvData.pv.PVScalar)
         */
        @Override
        public byte toByte(PVScalar pv) {
            ScalarType type = pv.getScalar().getScalarType();
            switch(type) {
                case pvByte :
                    {PVByte value = (PVByte)pv; return (byte)value.get();}
                case pvShort :
                    {PVShort value = (PVShort)pv; return (byte)value.get();}
                case pvInt :
                    {PVInt value = (PVInt)pv; return (byte)value.get();}
                case pvLong :
                    {PVLong value = (PVLong)pv; return (byte)value.get();}
                case pvFloat :
                    {PVFloat value = (PVFloat)pv; return (byte)value.get();}
                case pvDouble :
                    {PVDouble value = (PVDouble)pv; return (byte)value.get();}
                default:
                    throw new IllegalArgumentException(
                      "Illegal PVType. Must be numeric but it is "
                      + type.toString()
                    );
            }
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.Convert#toByteArray(org.epics.pvData.pv.PVArray, int, int, byte[], int)
         */
        @Override
        public int toByteArray(PVArray pv, int offset, int len, byte[] to, int toOffset) {
        	return convertToByteArray(pv,offset,len,to,toOffset);
        }   
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.Convert#toDouble(org.epics.pvData.pv.PVScalar)
         */
        @Override
        public double toDouble(PVScalar pv) {
            ScalarType type = pv.getScalar().getScalarType();
            switch(type) {
                case pvByte :
                    {PVByte value = (PVByte)pv; return (double)value.get();}
                case pvShort :
                    {PVShort value = (PVShort)pv; return (double)value.get();}
                case pvInt :
                    {PVInt value = (PVInt)pv; return (double)value.get();}
                case pvLong :
                    {PVLong value = (PVLong)pv; return (double)value.get();}
                case pvFloat :
                    {PVFloat value = (PVFloat)pv; return (double)value.get();}
                case pvDouble :
                    {PVDouble value = (PVDouble)pv; return (double)value.get();}
                default:
                    throw new IllegalArgumentException(
                      "Illegal PVType. Must be numeric but it is "
                      + type.toString()
                    );
            }
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.Convert#toDoubleArray(org.epics.pvData.pv.PVArray, int, int, double[], int)
         */
        @Override
        public int toDoubleArray(PVArray pv, int offset, int len, double[] to, int toOffset) {
        	return convertToDoubleArray(pv,offset,len,to,toOffset);
        }    
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.Convert#toFloat(org.epics.pvData.pv.PVScalar)
         */
        @Override
        public float toFloat(PVScalar pv) {
            ScalarType type = pv.getScalar().getScalarType();
            switch(type) {
                case pvByte :
                    {PVByte value = (PVByte)pv; return (float)value.get();}
                case pvShort :
                    {PVShort value = (PVShort)pv; return (float)value.get();}
                case pvInt :
                    {PVInt value = (PVInt)pv; return (float)value.get();}
                case pvLong :
                    {PVLong value = (PVLong)pv; return (float)value.get();}
                case pvFloat :
                    {PVFloat value = (PVFloat)pv; return (float)value.get();}
                case pvDouble :
                    {PVDouble value = (PVDouble)pv; return (float)value.get();}
                default:
                    throw new IllegalArgumentException(
                      "Illegal PVType. Must be numeric but it is "
                      + type.toString()
                    );
            }
        }    
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.Convert#toFloatArray(org.epics.pvData.pv.PVArray, int, int, float[], int)
         */
        @Override
        public int toFloatArray(PVArray pv, int offset, int len, float[] to, int toOffset) {
        	return convertToFloatArray(pv,offset,len,to,toOffset);
        }   
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.Convert#toInt(org.epics.pvData.pv.PVScalar)
         */
        @Override
        public int toInt(PVScalar pv) {
            ScalarType type = pv.getScalar().getScalarType();
            switch(type) {
                case pvByte :
                    {PVByte value = (PVByte)pv; return (int)value.get();}
                case pvShort :
                    {PVShort value = (PVShort)pv; return (int)value.get();}
                case pvInt :
                    {PVInt value = (PVInt)pv; return (int)value.get();}
                case pvLong :
                    {PVLong value = (PVLong)pv; return (int)value.get();}
                case pvFloat :
                    {PVFloat value = (PVFloat)pv; return (int)value.get();}
                case pvDouble :
                    {PVDouble value = (PVDouble)pv; return (int)value.get();}
                default:
                    throw new IllegalArgumentException(
                      "Illegal PVType. Must be numeric but it is "
                      + type.toString()
                    );
            }
        }    
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.Convert#toIntArray(org.epics.pvData.pv.PVArray, int, int, int[], int)
         */
        @Override
        public int toIntArray(PVArray pv, int offset, int len, int[] to, int toOffset) {
        	return convertToIntArray(pv,offset,len,to,toOffset);
        }    
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.Convert#toLong(org.epics.pvData.pv.PVScalar)
         */
        @Override
        public long toLong(PVScalar pv) {
            ScalarType type = pv.getScalar().getScalarType();
            switch(type) {
                case pvByte :
                    {PVByte value = (PVByte)pv; return (long)value.get();}
                case pvShort :
                    {PVShort value = (PVShort)pv; return (long)value.get();}
                case pvInt :
                    {PVInt value = (PVInt)pv; return (long)value.get();}
                case pvLong :
                    {PVLong value = (PVLong)pv; return (long)value.get();}
                case pvFloat :
                    {PVFloat value = (PVFloat)pv; return (long)value.get();}
                case pvDouble :
                    {PVDouble value = (PVDouble)pv; return (long)value.get();}
                default:
                    throw new IllegalArgumentException(
                      "Illegal PVType. Must be numeric but it is "
                      + type.toString()
                    );
            }
        }    
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.Convert#toLongArray(org.epics.pvData.pv.PVArray, int, int, long[], int)
         */
        @Override
        public int toLongArray(PVArray pv, int offset, int len, long[] to, int toOffset) {
        	return convertToLongArray(pv,offset,len,to,toOffset);
        }   
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.Convert#toShort(org.epics.pvData.pv.PVScalar)
         */
        @Override
        public short toShort(PVScalar pv) {
            ScalarType type = pv.getScalar().getScalarType();
            switch(type) {
                case pvByte :
                    {PVByte value = (PVByte)pv; return (short)value.get();}
                case pvShort :
                    {PVShort value = (PVShort)pv; return (short)value.get();}
                case pvInt :
                    {PVInt value = (PVInt)pv; return (short)value.get();}
                case pvLong :
                    {PVLong value = (PVLong)pv; return (short)value.get();}
                case pvFloat :
                    {PVFloat value = (PVFloat)pv; return (short)value.get();}
                case pvDouble :
                    {PVDouble value = (PVDouble)pv; return (short)value.get();}
                default:
                    throw new IllegalArgumentException(
                      "Illegal PVType. Must be numeric but it is "
                      + type.toString()
                    );
            }
        }    
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.Convert#toShortArray(org.epics.pvData.pv.PVArray, int, int, short[], int)
         */
        @Override
        public int toShortArray(PVArray pv, int offset, int len, short[] to, int toOffset) {
        	return convertToShortArray(pv,offset,len,to,toOffset);
        }
    
    
        private int convertFromByteArray(PVArray pv, int offset, int len,byte[]from, int fromOffset)
        {
            ScalarType elemType = pv.getArray().getElementType();
            int ntransfered = 0;
            switch(elemType) {
                case pvByte : {
                    PVByteArray pvdata = (PVByteArray)pv;
                    while(len>0) {
                        int n = pvdata.put(offset,len,from,fromOffset);
                        if(n==0) break;
                        len -= n; offset += n; fromOffset += n; ntransfered += n;
                    }
                    return ntransfered;
                } 
                case pvShort : {
                    PVShortArray pvdata = (PVShortArray)pv;
                    short[] data = new short[1];
                    while(len>0) {
                        data[0] = (short)from[fromOffset];
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                case pvInt : {
                    PVIntArray pvdata = (PVIntArray)pv;
                    int[] data = new int[1];
                    while(len>0) {
                        data[0] = (int)from[fromOffset];
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                case pvLong : {
                    PVLongArray pvdata = (PVLongArray)pv;
                    long[] data = new long[1];
                    while(len>0) {
                        data[0] = (long)from[fromOffset];
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                case pvFloat : {
                    PVFloatArray pvdata = (PVFloatArray)pv;
                    float[] data = new float[1];
                    while(len>0) {
                        data[0] = (float)from[fromOffset];
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                case pvDouble : {
                    PVDoubleArray pvdata = (PVDoubleArray)pv;
                    double[] data = new double[1];
                    while(len>0) {
                        data[0] = (double)from[fromOffset];
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                default:
                    throw new IllegalArgumentException(
                      "Illegal PVType. Must be numeric but it is "
                      + elemType.toString()
                    );
            }
        }
    
        private int convertToByteArray(PVArray pv, int offset, int len,byte[]to, int toOffset)
        {
            ScalarType elemType = pv.getArray().getElementType();
            int ntransfered = 0;
            switch(elemType) {
                case pvByte : {
                    PVByteArray pvdata = (PVByteArray)pv;
                    while(len>0) {
                        int num = 0;
                        byte[] dataArray = null;
                        int dataOffset = 0;
                        synchronized(byteArrayData) {
                            num = pvdata.get(offset,len,byteArrayData);
                            dataArray = byteArrayData.data;
                            dataOffset = byteArrayData.offset;
                        }
                        if(num<=0) break;
                        System.arraycopy(dataArray,dataOffset,to,toOffset,num);
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                case pvShort : {
                    PVShortArray pvdata = (PVShortArray)pv;
                    ShortArrayData data = new ShortArrayData();
                    while(len>0) {
                        int num = pvdata.get(offset,len,data);
                        if(num==0) break;
                        short[] dataArray = data.data;
                        int dataOffset = data.offset;
                        for(int i=0; i<num; i++)
                            to[i+toOffset] = (byte)dataArray[i+dataOffset];
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                case pvInt : {
                    PVIntArray pvdata = (PVIntArray)pv;
                    IntArrayData data = new IntArrayData();
                    while(len>0) {
                        int num = pvdata.get(offset,len,data);
                        if(num==0) break;
                        int[] dataArray = data.data;
                        int dataOffset = data.offset;
                        for(int i=0; i<num; i++)
                            to[i+toOffset] = (byte)dataArray[i+dataOffset];
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                case pvLong : {
                    PVLongArray pvdata = (PVLongArray)pv;
                    LongArrayData data = new LongArrayData();
                    while(len>0) {
                        int num = pvdata.get(offset,len,data);
                        if(num==0) break;
                        long[] dataArray = data.data;
                        int dataOffset = data.offset;
                        for(int i=0; i<num; i++)
                            to[i+toOffset] = (byte)dataArray[i+dataOffset];
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                case pvFloat : {
                    PVFloatArray pvdata = (PVFloatArray)pv;
                    FloatArrayData data = new FloatArrayData();
                    while(len>0) {
                        int num = pvdata.get(offset,len,data);
                        if(num==0) break;
                        float[] dataArray = data.data;
                        int dataOffset = data.offset;
                        for(int i=0; i<num; i++)
                            to[i+toOffset] = (byte)dataArray[i+dataOffset];
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                case pvDouble : {
                    PVDoubleArray pvdata = (PVDoubleArray)pv;
                    DoubleArrayData data = new DoubleArrayData();
                    while(len>0) {
                        int num = pvdata.get(offset,len,data);
                        if(num==0) break;
                        double[] dataArray = data.data;
                        int dataOffset = data.offset;
                        for(int i=0; i<num; i++)
                            to[i+toOffset] = (byte)dataArray[i+dataOffset];
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                default:
                    throw new IllegalArgumentException(
                      "Illegal PVType. Must be numeric but it is "
                      + elemType.toString()
                    );
            }
        }
    
        private int convertFromShortArray(PVArray pv, int offset, int len, short[]from, int fromOffset)
        {
            ScalarType elemType = pv.getArray().getElementType();
            int ntransfered = 0;
            switch(elemType) {
                case pvByte : {
                    PVByteArray pvdata = (PVByteArray)pv;
                    byte[] data = new byte[1];
                    while(len>0) {
                        data[0] = (byte)from[fromOffset];
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                case pvShort : {
                    PVShortArray pvdata = (PVShortArray)pv;
                    while(len>0) {
                        int n = pvdata.put(offset,len,from,fromOffset);
                        if(n==0) break;
                        len -= n; offset += n; fromOffset += n;  ntransfered += n;
                    }
                    return ntransfered;
                } 
                case pvInt : {
                    PVIntArray pvdata = (PVIntArray)pv;
                    int[] data = new int[1];
                    while(len>0) {
                        data[0] = (int)from[fromOffset];
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                case pvLong : {
                    PVLongArray pvdata = (PVLongArray)pv;
                    long[] data = new long[1];
                    while(len>0) {
                        data[0] = (long)from[fromOffset];
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                case pvFloat : {
                    PVFloatArray pvdata = (PVFloatArray)pv;
                    float[] data = new float[1];
                    while(len>0) {
                        data[0] = (float)from[fromOffset];
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                case pvDouble : {
                    PVDoubleArray pvdata = (PVDoubleArray)pv;
                    double[] data = new double[1];
                    while(len>0) {
                        data[0] = (double)from[fromOffset];
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                default:
                    throw new IllegalArgumentException(
                      "Illegal PVType. Must be numeric but it is "
                      + elemType.toString()
                    );
            }
        }
    
        private int convertToShortArray(PVArray pv, int offset, int len, short[]to, int toOffset)
        {
            ScalarType elemType = pv.getArray().getElementType();
            int ntransfered = 0;
            switch(elemType) {
                case pvByte : {
                    PVByteArray pvdata = (PVByteArray)pv;
                    ByteArrayData data = new ByteArrayData();
                    while(len>0) {
                        int num = pvdata.get(offset,len,data);
                        if(num==0) break;
                        byte[] dataArray = data.data;
                        int dataOffset = data.offset;
                        for(int i=0; i<num; i++)
                            to[i+toOffset] = (short)dataArray[i+dataOffset];
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                case pvShort : {
                    PVShortArray pvdata = (PVShortArray)pv;
                    while(len>0) {
                        int num = 0;
                        short[] dataArray = null;
                        int dataOffset = 0;
                        synchronized(shortArrayData) {
                            num = pvdata.get(offset,len,shortArrayData);
                            dataArray = shortArrayData.data;
                            dataOffset = shortArrayData.offset;
                        }
                        if(num<=0) break;
                        System.arraycopy(dataArray,dataOffset,to,toOffset,num);
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                case pvInt : {
                    PVIntArray pvdata = (PVIntArray)pv;
                    IntArrayData data = new IntArrayData();
                    while(len>0) {
                        int num = pvdata.get(offset,len,data);
                        if(num==0) break;
                        int[] dataArray = data.data;
                        int dataOffset = data.offset;
                        for(int i=0; i<num; i++)
                            to[i+toOffset] = (short)dataArray[i+dataOffset];
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                case pvLong : {
                    PVLongArray pvdata = (PVLongArray)pv;
                    LongArrayData data = new LongArrayData();
                    while(len>0) {
                        int num = pvdata.get(offset,len,data);
                        if(num==0) break;
                        long[] dataArray = data.data;
                        int dataOffset = data.offset;
                        for(int i=0; i<num; i++)
                            to[i+toOffset] = (short)dataArray[i+dataOffset];
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                case pvFloat : {
                    PVFloatArray pvdata = (PVFloatArray)pv;
                    FloatArrayData data = new FloatArrayData();
                    while(len>0) {
                        int num = pvdata.get(offset,len,data);
                        if(num==0) break;
                        float[] dataArray = data.data;
                        int dataOffset = data.offset;
                        for(int i=0; i<num; i++)
                            to[i+toOffset] = (short)dataArray[i+dataOffset];
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                case pvDouble : {
                    PVDoubleArray pvdata = (PVDoubleArray)pv;
                    DoubleArrayData data = new DoubleArrayData();
                    while(len>0) {
                        int num = pvdata.get(offset,len,data);
                        if(num==0) break;
                        double[] dataArray = data.data;
                        int dataOffset = data.offset;
                        for(int i=0; i<num; i++)
                            to[i+toOffset] = (short)dataArray[i+dataOffset];
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                default:
                    throw new IllegalArgumentException(
                      "Illegal PVType. Must be numeric but it is "
                      + elemType.toString()
                    );
            }
        }
    
        private int convertFromIntArray(PVArray pv, int offset, int len, int[]from, int fromOffset)
        {
            ScalarType elemType = pv.getArray().getElementType();
            int ntransfered = 0;
            switch(elemType) {
                case pvByte : {
                    PVByteArray pvdata = (PVByteArray)pv;
                    byte[] data = new byte[1];
                    while(len>0) {
                        data[0] = (byte)from[fromOffset];
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                case pvShort : {
                    PVShortArray pvdata = (PVShortArray)pv;
                    short[] data = new short[1];
                    while(len>0) {
                        data[0] = (short)from[fromOffset];
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                case pvInt : {
                    PVIntArray pvdata = (PVIntArray)pv;
                    while(len>0) {
                        int n = pvdata.put(offset,len,from,fromOffset);
                        if(n==0) break;
                        len -= n; offset += n; fromOffset += n;  ntransfered += n;
                    }
                    return ntransfered;
                } 
                case pvLong : {
                    PVLongArray pvdata = (PVLongArray)pv;
                    long[] data = new long[1];
                    while(len>0) {
                        data[0] = (long)from[fromOffset];
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                case pvFloat : {
                    PVFloatArray pvdata = (PVFloatArray)pv;
                    float[] data = new float[1];
                    while(len>0) {
                        data[0] = (float)from[fromOffset];
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                case pvDouble : {
                    PVDoubleArray pvdata = (PVDoubleArray)pv;
                    double[] data = new double[1];
                    while(len>0) {
                        data[0] = (double)from[fromOffset];
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                default:
                    throw new IllegalArgumentException(
                      "Illegal PVType. Must be numeric but it is "
                      + elemType.toString()
                    );
            }
        }
    
        private int convertToIntArray(PVArray pv, int offset, int len, int[]to, int toOffset)
        {
            ScalarType elemType = pv.getArray().getElementType();
            int ntransfered = 0;
            switch(elemType) {
                case pvByte : {
                    PVByteArray pvdata = (PVByteArray)pv;
                    ByteArrayData data = new ByteArrayData();
                    while(len>0) {
                        int num = pvdata.get(offset,len,data);
                        if(num==0) break;
                        byte[] dataArray = data.data;
                        int dataOffset = data.offset;
                        for(int i=0; i<num; i++)
                            to[i+toOffset] = (int)dataArray[i+dataOffset];
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                case pvShort : {
                    PVShortArray pvdata = (PVShortArray)pv;
                    ShortArrayData data = new ShortArrayData();
                    while(len>0) {
                        int num = pvdata.get(offset,len,data);
                        if(num==0) break;
                        short[] dataArray = data.data;
                        int dataOffset = data.offset;
                        for(int i=0; i<num; i++)
                            to[i+toOffset] = (int)dataArray[i+dataOffset];
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                case pvInt : {
                    PVIntArray pvdata = (PVIntArray)pv;
                    while(len>0) {
                        int num = 0;
                        int[] dataArray = null;
                        int dataOffset = 0;
                        synchronized(intArrayData) {
                            num = pvdata.get(offset,len,intArrayData);
                            dataArray = intArrayData.data;
                            dataOffset = intArrayData.offset;
                        }
                        if(num<=0) break;
                        System.arraycopy(dataArray,dataOffset,to,toOffset,num);
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                case pvLong : {
                    PVLongArray pvdata = (PVLongArray)pv;
                    LongArrayData data = new LongArrayData();
                    while(len>0) {
                        int num = pvdata.get(offset,len,data);
                        if(num==0) break;
                        long[] dataArray = data.data;
                        int dataOffset = data.offset;
                        for(int i=0; i<num; i++)
                            to[i+toOffset] = (int)dataArray[i+dataOffset];
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                case pvFloat : {
                    PVFloatArray pvdata = (PVFloatArray)pv;
                    FloatArrayData data = new FloatArrayData();
                    while(len>0) {
                        int num = pvdata.get(offset,len,data);
                        if(num==0) break;
                        float[] dataArray = data.data;
                        int dataOffset = data.offset;
                        for(int i=0; i<num; i++)
                            to[i+toOffset] = (int)dataArray[i+dataOffset];
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                case pvDouble : {
                    PVDoubleArray pvdata = (PVDoubleArray)pv;
                    DoubleArrayData data = new DoubleArrayData();
                    while(len>0) {
                        int num = pvdata.get(offset,len,data);
                        if(num==0) break;
                        double[] dataArray = data.data;
                        int dataOffset = data.offset;
                        for(int i=0; i<num; i++)
                            to[i+toOffset] = (int)dataArray[i+dataOffset];
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                default:
                    throw new IllegalArgumentException(
                      "Illegal PVType. Must be numeric but it is "
                      + elemType.toString()
                    );
            }
        }
    
        private int convertFromLongArray(PVArray pv, int offset, int len, long[]from, int fromOffset)
        {
            ScalarType elemType = pv.getArray().getElementType();
            int ntransfered = 0;
            switch(elemType) {
                case pvByte : {
                    PVByteArray pvdata = (PVByteArray)pv;
                    byte[] data = new byte[1];
                    while(len>0) {
                        data[0] = (byte)from[fromOffset];
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                case pvShort : {
                    PVShortArray pvdata = (PVShortArray)pv;
                    short[] data = new short[1];
                    while(len>0) {
                        data[0] = (short)from[fromOffset];
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                case pvInt : {
                    PVIntArray pvdata = (PVIntArray)pv;
                    int[] data = new int[1];
                    while(len>0) {
                        data[0] = (int)from[fromOffset];
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                case pvLong : {
                    PVLongArray pvdata = (PVLongArray)pv;
                    while(len>0) {
                        int n = pvdata.put(offset,len,from,fromOffset);
                        if(n==0) break;
                        len -= n; offset += n; fromOffset += n;  ntransfered += n;
                    }
                    return ntransfered;
                } 
                case pvFloat : {
                    PVFloatArray pvdata = (PVFloatArray)pv;
                    float[] data = new float[1];
                    while(len>0) {
                        data[0] = (float)from[fromOffset];
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                case pvDouble : {
                    PVDoubleArray pvdata = (PVDoubleArray)pv;
                    double[] data = new double[1];
                    while(len>0) {
                        data[0] = (double)from[fromOffset];
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                default:
                    throw new IllegalArgumentException(
                      "Illegal PVType. Must be numeric but it is "
                      + elemType.toString()
                    );
            }
        }
    
        private int convertToLongArray(PVArray pv, int offset, int len,long[]to, int toOffset)
        {
            ScalarType elemType = pv.getArray().getElementType();
            int ntransfered = 0;
            switch(elemType) {
                case pvByte : {
                    PVByteArray pvdata = (PVByteArray)pv;
                    ByteArrayData data = new ByteArrayData();
                    while(len>0) {
                        int num = pvdata.get(offset,len,data);
                        if(num==0) break;
                        byte[] dataArray = data.data;
                        int dataOffset = data.offset;
                        for(int i=0; i<num; i++)
                            to[i+toOffset] = (long)dataArray[i+dataOffset];
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                case pvShort : {
                    PVShortArray pvdata = (PVShortArray)pv;
                    ShortArrayData data = new ShortArrayData();
                    while(len>0) {
                        int num = pvdata.get(offset,len,data);
                        if(num==0) break;
                        short[] dataArray = data.data;
                        int dataOffset = data.offset;
                        for(int i=0; i<num; i++)
                            to[i+toOffset] = (long)dataArray[i+dataOffset];
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                case pvInt : {
                    PVIntArray pvdata = (PVIntArray)pv;
                    IntArrayData data = new IntArrayData();
                    while(len>0) {
                        int num = pvdata.get(offset,len,data);
                        if(num==0) break;
                        int[] dataArray = data.data;
                        int dataOffset = data.offset;
                        for(int i=0; i<num; i++)
                            to[i+toOffset] = (long)dataArray[i+dataOffset];
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                case pvLong : {
                    PVLongArray pvdata = (PVLongArray)pv;
                    while(len>0) {
                        int num = 0;
                        long[] dataArray = null;
                        int dataOffset = 0;
                        synchronized(longArrayData) {
                            num = pvdata.get(offset,len,longArrayData);
                            dataArray = longArrayData.data;
                            dataOffset = longArrayData.offset;
                        }
                        if(num<=0) break;
                        System.arraycopy(dataArray,dataOffset,to,toOffset,num);
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                case pvFloat : {
                    PVFloatArray pvdata = (PVFloatArray)pv;
                    FloatArrayData data = new FloatArrayData();
                    while(len>0) {
                        int num = pvdata.get(offset,len,data);
                        if(num==0) break;
                        float[] dataArray = data.data;
                        int dataOffset = data.offset;
                        for(int i=0; i<num; i++)
                            to[i+toOffset] = (long)dataArray[i+dataOffset];
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                case pvDouble : {
                    PVDoubleArray pvdata = (PVDoubleArray)pv;
                    DoubleArrayData data = new DoubleArrayData();
                    while(len>0) {
                        int num = pvdata.get(offset,len,data);
                        if(num==0) break;
                        double[] dataArray = data.data;
                        int dataOffset = data.offset;
                        for(int i=0; i<num; i++)
                            to[i+toOffset] = (long)dataArray[i+dataOffset];
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                default:
                    throw new IllegalArgumentException(
                      "Illegal PVType. Must be numeric but it is "
                      + elemType.toString()
                    );
            }
        }
    
        private int convertFromFloatArray(PVArray pv, int offset, int len, float[]from, int fromOffset)
        {
            ScalarType elemType = pv.getArray().getElementType();
            int ntransfered = 0;
            switch(elemType) {
                case pvByte : {
                    PVByteArray pvdata = (PVByteArray)pv;
                    byte[] data = new byte[1];
                    while(len>0) {
                        data[0] = (byte)from[fromOffset];
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                case pvShort : {
                    PVShortArray pvdata = (PVShortArray)pv;
                    short[] data = new short[1];
                    while(len>0) {
                        data[0] = (short)from[fromOffset];
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                case pvInt : {
                    PVIntArray pvdata = (PVIntArray)pv;
                    int[] data = new int[1];
                    while(len>0) {
                        data[0] = (int)from[fromOffset];
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                case pvLong : {
                    PVLongArray pvdata = (PVLongArray)pv;
                    long[] data = new long[1];
                    while(len>0) {
                        data[0] = (long)from[fromOffset];
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                case pvFloat : {
                    PVFloatArray pvdata = (PVFloatArray)pv;
                    while(len>0) {
                        int n = pvdata.put(offset,len,from,fromOffset);
                        if(n==0) break;
                        len -= n; offset += n; fromOffset += n;  ntransfered += n;
                    }
                    return ntransfered;
                } 
                case pvDouble : {
                    PVDoubleArray pvdata = (PVDoubleArray)pv;
                    double[] data = new double[1];
                    while(len>0) {
                        data[0] = (double)from[fromOffset];
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                default:
                    throw new IllegalArgumentException(
                      "Illegal PVType. Must be numeric but it is "
                      + elemType.toString()
                    );
            }
        }
    
        private int convertToFloatArray(PVArray pv, int offset, int len, float[]to, int toOffset)
        {
            ScalarType elemType = pv.getArray().getElementType();
            int ntransfered = 0;
            switch(elemType) {
                case pvByte : {
                    PVByteArray pvdata = (PVByteArray)pv;
                    ByteArrayData data = new ByteArrayData();
                    while(len>0) {
                        int num = pvdata.get(offset,len,data);
                        if(num==0) break;
                        byte[] dataArray = data.data;
                        int dataOffset = data.offset;
                        for(int i=0; i<num; i++)
                            to[i+toOffset] = (float)dataArray[i+dataOffset];
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                case pvShort : {
                    PVShortArray pvdata = (PVShortArray)pv;
                    ShortArrayData data = new ShortArrayData();
                    while(len>0) {
                        int num = pvdata.get(offset,len,data);
                        if(num==0) break;
                        short[] dataArray = data.data;
                        int dataOffset = data.offset;
                        for(int i=0; i<num; i++)
                            to[i+toOffset] = (float)dataArray[i+dataOffset];
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                case pvInt : {
                    PVIntArray pvdata = (PVIntArray)pv;
                    IntArrayData data = new IntArrayData();
                    while(len>0) {
                        int num = pvdata.get(offset,len,data);
                        if(num==0) break;
                        int[] dataArray = data.data;
                        int dataOffset = data.offset;
                        for(int i=0; i<num; i++)
                            to[i+toOffset] = (float)dataArray[i+dataOffset];
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                case pvLong : {
                    PVLongArray pvdata = (PVLongArray)pv;
                    LongArrayData data = new LongArrayData();
                    while(len>0) {
                        int num = pvdata.get(offset,len,data);
                        if(num==0) break;
                        long[] dataArray = data.data;
                        int dataOffset = data.offset;
                        for(int i=0; i<num; i++)
                            to[i+toOffset] = (float)dataArray[i+dataOffset];
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                case pvFloat : {
                    PVFloatArray pvdata = (PVFloatArray)pv;
                    while(len>0) {
                        int num = 0;
                        float[] dataArray = null;
                        int dataOffset = 0;
                        synchronized(floatArrayData) {
                            num = pvdata.get(offset,len,floatArrayData);
                            dataArray = floatArrayData.data;
                            dataOffset = floatArrayData.offset;
                        }
                        if(num<=0) break;
                        System.arraycopy(dataArray,dataOffset,to,toOffset,num);
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                case pvDouble : {
                    PVDoubleArray pvdata = (PVDoubleArray)pv;
                    DoubleArrayData data = new DoubleArrayData();
                    while(len>0) {
                        int num = pvdata.get(offset,len,data);
                        if(num==0) break;
                        double[] dataArray = data.data;
                        int dataOffset = data.offset;
                        for(int i=0; i<num; i++)
                            to[i+toOffset] = (float)dataArray[i+dataOffset];
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                default:
                    throw new IllegalArgumentException(
                      "Illegal PVType. Must be numeric but it is "
                      + elemType.toString()
                    );
            }
        }
    
        private int convertFromDoubleArray(PVArray pv, int offset, int len,double[]from, int fromOffset)
        {
            ScalarType elemType = pv.getArray().getElementType();
            int ntransfered = 0;
            switch(elemType) {
                case pvByte : {
                    PVByteArray pvdata = (PVByteArray)pv;
                    byte[] data = new byte[1];
                    while(len>0) {
                        data[0] = (byte)from[fromOffset];
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                case pvShort : {
                    PVShortArray pvdata = (PVShortArray)pv;
                    short[] data = new short[1];
                    while(len>0) {
                        data[0] = (short)from[fromOffset];
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                case pvInt : {
                    PVIntArray pvdata = (PVIntArray)pv;
                    int[] data = new int[1];
                    while(len>0) {
                        data[0] = (int)from[fromOffset];
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                case pvLong : {
                    PVLongArray pvdata = (PVLongArray)pv;
                    long[] data = new long[1];
                    while(len>0) {
                        data[0] = (long)from[fromOffset];
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                case pvFloat : {
                    PVFloatArray pvdata = (PVFloatArray)pv;
                    float[] data = new float[1];
                    while(len>0) {
                        data[0] = (float)from[fromOffset];
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                case pvDouble : {
                    PVDoubleArray pvdata = (PVDoubleArray)pv;
                    while(len>0) {
                        int n = pvdata.put(offset,len,from,fromOffset);
                        if(n==0) break;
                        len -= n; offset += n; fromOffset += n;  ntransfered += n;
                    }
                    return ntransfered;
                } 
                default:
                    throw new IllegalArgumentException(
                      "Illegal PVType. Must be numeric but it is "
                      + elemType.toString()
                    );
            }
        }
    
        private int convertToDoubleArray(PVArray pv, int offset, int len, double[]to, int toOffset)
        {
            ScalarType elemType = pv.getArray().getElementType();
            int ntransfered = 0;
            switch(elemType) {
                case pvByte : {
                    PVByteArray pvdata = (PVByteArray)pv;
                    ByteArrayData data = new ByteArrayData();
                    while(len>0) {
                        int num = pvdata.get(offset,len,data);
                        if(num==0) break;
                        byte[] dataArray = data.data;
                        int dataOffset = data.offset;
                        for(int i=0; i<num; i++)
                            to[i+toOffset] = (double)dataArray[i+dataOffset];
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                case pvShort : {
                    PVShortArray pvdata = (PVShortArray)pv;
                    ShortArrayData data = new ShortArrayData();
                    while(len>0) {
                        int num = pvdata.get(offset,len,data);
                        if(num==0) break;
                        short[] dataArray = data.data;
                        int dataOffset = data.offset;
                        for(int i=0; i<num; i++)
                            to[i+toOffset] = (double)dataArray[i+dataOffset];
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                case pvInt : {
                    PVIntArray pvdata = (PVIntArray)pv;
                    IntArrayData data = new IntArrayData();
                    while(len>0) {
                        int num = pvdata.get(offset,len,data);
                        if(num==0) break;
                        int[] dataArray = data.data;
                        int dataOffset = data.offset;
                        for(int i=0; i<num; i++)
                            to[i+toOffset] = (double)dataArray[i+dataOffset];
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                case pvLong : {
                    PVLongArray pvdata = (PVLongArray)pv;
                    LongArrayData data = new LongArrayData();
                    while(len>0) {
                        int num = pvdata.get(offset,len,data);
                        if(num==0) break;
                        long[] dataArray = data.data;
                        int dataOffset = data.offset;
                        for(int i=0; i<num; i++)
                            to[i+toOffset] = (double)dataArray[i+dataOffset];
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                case pvFloat : {
                    PVFloatArray pvdata = (PVFloatArray)pv;
                    FloatArrayData data = new FloatArrayData();
                    while(len>0) {
                        int num = pvdata.get(offset,len,data);
                        if(num==0) break;
                        float[] dataArray = data.data;
                        int dataOffset = data.offset;
                        for(int i=0; i<num; i++)
                            to[i+toOffset] = (double)dataArray[i+dataOffset];
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                case pvDouble : {
                    PVDoubleArray pvdata = (PVDoubleArray)pv;
                    while(len>0) {
                        int num = 0;
                        double[] dataArray = null;
                        int dataOffset = 0;
                        synchronized(doubleArrayData) {
                            num = pvdata.get(offset,len,doubleArrayData);
                            dataArray = doubleArrayData.data;
                            dataOffset = doubleArrayData.offset;
                        }
                        if(num<=0) break;
                        System.arraycopy(dataArray,dataOffset,to,toOffset,num);
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                default:
                    throw new IllegalArgumentException(
                      "Illegal PVType. Must be numeric but it is "
                      + elemType.toString()
                    );
            }
        }
        
        private int convertFromStringArray(PVArray pv, int offset, int len, String[]from, int fromOffset)
        {
            ScalarType elemType = pv.getArray().getElementType();
            int ntransfered = 0;
            switch(elemType) {
                case pvBoolean: {
                    PVBooleanArray pvdata = (PVBooleanArray)pv;
                    boolean[] data = new boolean[1];
                    while(len>0) {
                        String fromString = from[fromOffset];
                        for(int i=fromString.length()-1; i>=0; i--) {
                            if(Character.isWhitespace(fromString.charAt(i))) {
                                if(i==fromString.length()-1) {
                                    fromString = fromString.substring(0, i);
                                } else {
                                    fromString = fromString.substring(0, i) + fromString.substring(i+1);
                                }
                            }
                        }
                        data[0] = Boolean.parseBoolean(fromString);
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                }
                case pvByte : {
                    PVByteArray pvdata = (PVByteArray)pv;
                    byte[] data = new byte[1];
                    while(len>0) {
                        String fromString = from[fromOffset];
                        for(int i=fromString.length()-1; i>=0; i--) {
                            if(Character.isWhitespace(fromString.charAt(i))) {
                                if(i==fromString.length()-1) {
                                    fromString = fromString.substring(0, i);
                                } else {
                                    fromString = fromString.substring(0, i) + fromString.substring(i+1);
                                }
                            }
                        }
                        data[0] = Byte.decode(fromString);
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                case pvShort : {
                    PVShortArray pvdata = (PVShortArray)pv;
                    short[] data = new short[1];
                    while(len>0) {
                        String fromString = from[fromOffset];
                        for(int i=fromString.length()-1; i>=0; i--) {
                            if(Character.isWhitespace(fromString.charAt(i))) {
                                if(i==fromString.length()-1) {
                                    fromString = fromString.substring(0, i);
                                } else {
                                    fromString = fromString.substring(0, i) + fromString.substring(i+1);
                                }
                            }
                        }
                        data[0] = Short.decode(fromString);
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                case pvInt : {
                    PVIntArray pvdata = (PVIntArray)pv;
                    int[] data = new int[1];
                    while(len>0) {
                        String fromString = from[fromOffset];
                        for(int i=fromString.length()-1; i>=0; i--) {
                            if(Character.isWhitespace(fromString.charAt(i))) {
                                if(i==fromString.length()-1) {
                                    fromString = fromString.substring(0, i);
                                } else {
                                    fromString = fromString.substring(0, i) + fromString.substring(i+1);
                                }
                            }
                        }
                        data[0] = Integer.decode(fromString);
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                case pvLong : {
                    PVLongArray pvdata = (PVLongArray)pv;
                    long[] data = new long[1];
                    while(len>0) {
                        String fromString = from[fromOffset];
                        for(int i=fromString.length()-1; i>=0; i--) {
                            if(Character.isWhitespace(fromString.charAt(i))) {
                                if(i==fromString.length()-1) {
                                    fromString = fromString.substring(0, i);
                                } else {
                                    fromString = fromString.substring(0, i) + fromString.substring(i+1);
                                }
                            }
                        }
                        data[0] = Long.decode(fromString);
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                case pvFloat : {
                    PVFloatArray pvdata = (PVFloatArray)pv;
                    float[] data = new float[1];
                    while(len>0) {
                        String fromString = from[fromOffset];
                        for(int i=fromString.length()-1; i>=0; i--) {
                            if(Character.isWhitespace(fromString.charAt(i))) {
                                if(i==fromString.length()-1) {
                                    fromString = fromString.substring(0, i);
                                } else {
                                    fromString = fromString.substring(0, i) + fromString.substring(i+1);
                                }
                            }
                        }
                        data[0] = Float.valueOf(fromString);
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                case pvDouble : {
                    PVDoubleArray pvdata = (PVDoubleArray)pv;
                    double[]data = new double[1];
                    while(len>0) {
                        String fromString = from[fromOffset];
                        for(int i=fromString.length()-1; i>=0; i--) {
                            if(Character.isWhitespace(fromString.charAt(i))) {
                                if(i==fromString.length()-1) {
                                    fromString = fromString.substring(0, i);
                                } else {
                                    fromString = fromString.substring(0, i) + fromString.substring(i+1);
                                }
                            }
                        }
                        data[0] = Double.valueOf(fromString);
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                case pvString:
                    PVStringArray pvdata = (PVStringArray)pv;
                    while(len>0) {
                        int n = pvdata.put(offset,len,from,fromOffset);
                        if(n==0) break;
                        len -= n; offset += n; fromOffset += n; ntransfered += n;
                    }
                    return ntransfered;
                default:
                    throw new IllegalArgumentException(
                      "Illegal PVType. Must be scalar but it is "
                      + elemType.toString()
                    );
            }
        }
        
        private int convertToStringArray(PVArray pv, int offset, int len, String[]to, int toOffset)
        {
            ScalarType elementType = pv.getArray().getElementType();
            int ncopy = pv.getLength();
            if(ncopy>len) ncopy = len;
            int num = ncopy;
            switch(elementType) {
            case pvBoolean: {
                    PVBooleanArray pvdata = (PVBooleanArray)pv;
                    BooleanArrayData data = new BooleanArrayData();
                    for(int i=0; i<num; i++) {
                        if(pvdata.get(offset+i,1,data)==1) {
                            boolean[] dataArray = data.data;
                            Boolean value = Boolean.valueOf(dataArray[data.offset]);
                            to[toOffset+i] = value.toString();
                        } else {
                            to[toOffset+i] = "bad pv";
                        }
                    }
                }
                break;
            case pvByte: {
                    PVByteArray pvdata = (PVByteArray)pv;
                    ByteArrayData data = new ByteArrayData();
                    for(int i=0; i<num; i++) {
                        if(pvdata.get(offset+i,1,data)==1) {
                            byte[] dataArray = data.data;
                            Byte value = Byte.valueOf(dataArray[data.offset]);
                            to[toOffset+i] = value.toString();
                        } else {
                            to[toOffset+i] = "bad pv";
                        }
                    }
                }
                break;
            case pvShort: {
                    PVShortArray pvdata = (PVShortArray)pv;
                    ShortArrayData data = new ShortArrayData();
                    for(int i=0; i<num; i++) {
                        if(pvdata.get(offset+i,1,data)==1) {
                            short[] dataArray = data.data;
                            Short value = Short.valueOf(dataArray[data.offset]);
                            to[toOffset+i] = value.toString();
                        } else {
                            to[toOffset+i] = "bad pv";
                        }
                    }
                }
                break;
            case pvInt: {
                    PVIntArray pvdata = (PVIntArray)pv;
                    IntArrayData data = new IntArrayData();
                    for(int i=0; i<num; i++) {
                        if(pvdata.get(offset+i,1,data)==1) {
                            int[] dataArray = data.data;
                            Integer value = Integer.valueOf(dataArray[data.offset]);
                            to[toOffset+i] = value.toString();
                        } else {
                            to[toOffset+i] = "bad pv";
                        }
                    }
                }
                break;
            case pvLong: {
                    PVLongArray pvdata = (PVLongArray)pv;
                    LongArrayData data = new LongArrayData();
                    for(int i=0; i<num; i++) {
                        if(pvdata.get(offset+i,1,data)==1) {
                            long[] dataArray = data.data;
                            Long value = Long.valueOf(dataArray[data.offset]);
                            to[toOffset+i] = value.toString();
                        } else {
                            to[toOffset+i] = "bad pv";
                        }
                    }
                }
                break;
            case pvFloat: {
                    PVFloatArray pvdata = (PVFloatArray)pv;
                    FloatArrayData data = new FloatArrayData();
                    for(int i=0; i<num; i++) {
                        if(pvdata.get(offset+i,1,data)==1) {
                            float[] dataArray = data.data;
                            Float value = new Float(dataArray[data.offset]);
                            to[toOffset+i] = value.toString();
                        } else {
                            to[toOffset+i] = "bad pv";
                        }
                    }
                }
                break;
            case pvDouble: {
                    PVDoubleArray pvdata = (PVDoubleArray)pv;
                    DoubleArrayData data = new DoubleArrayData();
                    for(int i=0; i<num; i++) {
                        if(pvdata.get(offset+i,1,data)==1) {
                            double[] dataArray = data.data;
                            Double value = new Double(dataArray[data.offset]);
                            to[toOffset+i] = value.toString();
                        } else {
                            to[toOffset+i] = "bad pv";
                        }
                    }
                }
                break;
            case pvString: {
                    PVStringArray pvdata = (PVStringArray)pv;
                    while(num>0) {
                        int numnow = 0;
                        String[] dataArray = null;
                        int dataOffset = 0;
                        synchronized(stringArrayData) {
                            numnow = pvdata.get(offset,num,stringArrayData);
                            dataArray = stringArrayData.data;
                            dataOffset = stringArrayData.offset;
                        }
                        if(numnow<=0) {
                            for(int i=0; i<num; i++) to[toOffset+i] = "bad pv";
                            break;
                        }
                        System.arraycopy(dataArray,dataOffset,to,toOffset,num);
                        num -= numnow; offset += numnow; toOffset += numnow;;
                    }
                }
                break;
            default:    
                throw new IllegalArgumentException(
                        "Illegal PVType. Must be scalar but it is "
                        + elementType.toString()
                      );
            }
            return ncopy;
        }    
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.Convert#newLine(java.lang.StringBuilder, int)
         */
        public void newLine(StringBuilder builder, int indentLevel) {
            builder.append(String.format("%n"));
            for (int i=0; i <indentLevel; i++) builder.append(indentString);
        }
        private static String indentString = "    ";

        private String convertToString(PVField pv,int indentLevel) {
            Type type = pv.getField().getType();
            if(type==Type.scalarArray) {
                return convertArray((PVArray)pv,indentLevel);
            }
            if(type==Type.structure) {
                return convertStructure((PVStructure)pv,indentLevel);
            }
            PVScalar pvScalar = (PVScalar)pv;
            switch(pvScalar.getScalar().getScalarType()) {
            case pvBoolean: {
                    PVBoolean data = (PVBoolean)pv;
                    boolean value = data.get();
                    if(value) {
                        return "true";
                    } else {
                        return "false";
                    }
                }
            case pvByte: {
                    PVByte data = (PVByte)pv;
                    return String.format("%d",data.get());
                }
            case pvShort: {
                    PVShort data = (PVShort)pv;
                    return String.format("%d",data.get());
                }
            case pvInt: {
                    PVInt data = (PVInt)pv;
                    return String.format("%d",data.get());
                }
            case pvLong: {
                    PVLong data = (PVLong)pv;
                    return String.format("%d",data.get());
                }
            case pvFloat: {
                    PVFloat data = (PVFloat)pv;
                    return String.format("%g",data.get());
                }
            case pvDouble: {
                    PVDouble data = (PVDouble)pv;
                    return String.format("%g",data.get());
                }
            case pvString: {
                    PVString data = (PVString)pv;
                    return data.get();
                }
            default:
                return "unknown PVType";
            }
        }
    
        private String convertStructure(PVStructure data,int indentLevel) {
            StringBuilder builder = new StringBuilder();
            newLine(builder,indentLevel);
            builder.append(String.format("structure {"));
            PVField[] fieldsData = data.getPVFields();
            if(fieldsData!=null) for(PVField fieldField : fieldsData) {
                Field fieldnow = fieldField.getField();
                newLine(builder,indentLevel+1);
                builder.append(String.format("%s = ", fieldnow.getFieldName()));
                builder.append(convertToString(fieldField,indentLevel+1));
            }
            newLine(builder,indentLevel);
            builder.append("}");
            return builder.toString();
        }
    
        private String convertArray(PVArray pv,int indentLevel) {
            Array array = pv.getArray();
            ScalarType type = array.getElementType();
            StringBuilder builder = new StringBuilder();
            switch(type) {
            case pvBoolean: {
                    PVBooleanArray pvdata = (PVBooleanArray)pv;
                    BooleanArrayData data = new BooleanArrayData();
                    builder.append("[");
                    for(int i=0; i < pvdata.getLength(); i++) {
                        if(i!=0) builder.append(',');
                        int num = pvdata.get(i,1,data);
                        if(num==1) {
                             boolean[] value = data.data;
                             if(value[data.offset]) {
                                 builder.append("true");
                             } else {
                                 builder.append("false");
                             }
                        } else {
                             builder.append("???? ");
                        }
                    }
                    builder.append("]");
                    break;
                }
            case pvByte: {
                    PVByteArray pvdata = (PVByteArray)pv;
                    ByteArrayData data = new ByteArrayData();
                    builder.append("[");
                    for(int i=0; i < pvdata.getLength(); i++) {
                        if(i!=0) builder.append(',');
                        int num = pvdata.get(i,1,data);
                        if(num==1) {
                             byte[] value = data.data;
                             builder.append(String.format("%d",value[data.offset]));
                        } else {
                             builder.append("???? ");
                        }
                    }
                    builder.append("]");
                    break;
                }
            case pvShort: {
                    PVShortArray pvdata = (PVShortArray)pv;
                    ShortArrayData data = new ShortArrayData();
                    builder.append("[");
                    for(int i=0; i < pvdata.getLength(); i++) {
                        if(i!=0) builder.append(',');
                        int num = pvdata.get(i,1,data);
                        if(num==1) {
                             short[] value = data.data;
                             builder.append(String.format("%d",value[data.offset]));
                        } else {
                             builder.append("???? ");
                        }
                    }
                    builder.append("]");
                    break;
                }
            case pvInt: {
                    PVIntArray pvdata = (PVIntArray)pv;
                    IntArrayData data = new IntArrayData();
                    builder.append("[");
                    for(int i=0; i < pvdata.getLength(); i++) {
                        if(i!=0) builder.append(',');
                        int num = pvdata.get(i,1,data);
                        if(num==1) {
                             int[] value = data.data;
                             builder.append(String.format("%d",value[data.offset]));
                        } else {
                             builder.append("???? ");
                        }
                    }
                    builder.append("]");
                    break;
                }
            case pvLong: {
                    PVLongArray pvdata = (PVLongArray)pv;
                    LongArrayData data = new LongArrayData();
                    builder.append("[");
                    for(int i=0; i < pvdata.getLength(); i++) {
                        if(i!=0) builder.append(',');
                        int num = pvdata.get(i,1,data);
                        if(num==1) {
                             long[] value = data.data;
                             builder.append(String.format("%d",value[data.offset]));
                        } else {
                             builder.append("???? ");
                        }
                    }
                    builder.append("]");
                    break;
                }
            case pvFloat: {
                    PVFloatArray pvdata = (PVFloatArray)pv;
                    FloatArrayData data = new FloatArrayData();
                    builder.append("[");
                    for(int i=0; i < pvdata.getLength(); i++) {
                        if(i!=0) builder.append(',');
                        int num = pvdata.get(i,1,data);
                        if(num==1) {
                             float[] value = data.data;
                             builder.append(String.format("%g",value[data.offset]));
                        } else {
                             builder.append(indentString + "???? ");
                        }
                    }
                    builder.append("]");
                    break;
                }
            case pvDouble: {
                    PVDoubleArray pvdata = (PVDoubleArray)pv;
                    DoubleArrayData data = new DoubleArrayData();
                    builder.append("[");
                    for(int i=0; i < pvdata.getLength(); i++) {
                        if(i!=0) builder.append(',');
                        int num = pvdata.get(i,1,data);
                        if(num==1) {
                             double[] value = data.data;
                             builder.append(String.format("%g",value[data.offset]));
                        } else {
                             builder.append("???? ");
                        }
                    }
                    builder.append("]");
                    break;
                }
            case pvString: {
                    PVStringArray pvdata = (PVStringArray)pv;
                    StringArrayData data = new StringArrayData();
                    builder.append("[");
                    for(int i=0; i < pvdata.getLength(); i++) {
                        if(i!=0) builder.append(",");
                        int num = pvdata.get(i,1,data);
                        String[] value = data.data;
                        if(num==1 && value[data.offset]!=null) {
                            builder.append(value[data.offset]);
                        } else {
                            builder.append("null");
                        }
                  }
                  builder.append("]");
                    break;
                }
            default:
                builder.append(" array element is unknown PVType");
            }
            if(pv.isImmutable()) {
                builder.append(" immutable ");
            }
            return builder.toString();
        }

        private int copyArrayDataReference(PVArray from,PVArray to) {
            ScalarType scalarType = from.getArray().getElementType();
            switch(scalarType) {
            case pvBoolean: {
                PVBooleanArray pvfrom = (PVBooleanArray)from;
                PVBooleanArray pvto = (PVBooleanArray)to;
                boolean[] booleanArray = null;
                synchronized(booleanArrayData) {
                    pvfrom.get(0, pvfrom.getLength(), booleanArrayData);
                    booleanArray = booleanArrayData.data;
                }
                pvto.shareData(booleanArray);
                
                break;
            }
            case pvByte: {
                PVByteArray pvfrom = (PVByteArray)from;
                PVByteArray pvto = (PVByteArray)to;
                byte[] byteArray = null;
                synchronized(byteArrayData) {
                    pvfrom.get(0, pvfrom.getLength(), byteArrayData);
                    byteArray = byteArrayData.data;
                }
                pvto.shareData(byteArray);
                break;
            }
            case pvShort: {
                PVShortArray pvfrom = (PVShortArray)from;
                PVShortArray pvto = (PVShortArray)to;
                short[] shortArray = null;
                synchronized(shortArrayData) {
                    pvfrom.get(0, pvfrom.getLength(), shortArrayData);
                    shortArray = shortArrayData.data;
                }
                pvto.shareData(shortArray);
                break;
            }
            case pvInt: {
                PVIntArray pvfrom = (PVIntArray)from;
                PVIntArray pvto = (PVIntArray)to;
                int[] intArray = null;
                synchronized(intArrayData) {
                    pvfrom.get(0, pvfrom.getLength(), intArrayData);
                    intArray = intArrayData.data;
                }
                pvto.shareData(intArray);
                break;
            }
            case pvLong: {
                PVLongArray pvfrom = (PVLongArray)from;
                PVLongArray pvto = (PVLongArray)to;
                long[] longArray = null;
                synchronized(longArrayData) {
                    pvfrom.get(0, pvfrom.getLength(), longArrayData);
                    longArray = longArrayData.data;
                }
                pvto.shareData(longArray);
                break;
            }
            case pvFloat: {
                PVFloatArray pvfrom = (PVFloatArray)from;
                PVFloatArray pvto = (PVFloatArray)to;
                float[] floatArray = null;
                synchronized(floatArrayData) {
                    pvfrom.get(0, pvfrom.getLength(), floatArrayData);
                    floatArray = floatArrayData.data;
                }
                pvto.shareData(floatArray);
                break;
            }
            case pvDouble: {
                PVDoubleArray pvfrom = (PVDoubleArray)from;
                PVDoubleArray pvto = (PVDoubleArray)to;
                double[] doubleArray = null;
                synchronized(doubleArrayData) {
                    pvfrom.get(0, pvfrom.getLength(), doubleArrayData);
                    doubleArray = doubleArrayData.data;
                }
                pvto.shareData(doubleArray);
                break;
            }
            case pvString: {
                PVStringArray pvfrom = (PVStringArray)from;
                PVStringArray pvto = (PVStringArray)to;
                String[] stringArray = null;
                synchronized(stringArrayData) {
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
        
        private int CopyNumericArray(PVArray from, int offset, PVArray to, int toOffset, int len)
        {
            ScalarType fromElementType = ((Array)from.getField()).getElementType();
            int ncopy = 0;
            switch(fromElementType) {
            case pvByte: {
                    PVByteArray pvfrom = (PVByteArray)from;
                    while(len>0) {
                        int num = 0;
                        byte[] data = null;
                        int dataOffset = 0;
                        synchronized(byteArrayData) {
                            num = pvfrom.get(offset,len,byteArrayData);
                            data = byteArrayData.data;
                            dataOffset = byteArrayData.offset;
                        }
                        if(num<=0) break;
                        while(num>0) {
                            int n = fromByteArray(to,toOffset,num,data,dataOffset);
                            if(n<=0) break;
                            len -= n; num -= n; ncopy+=n; offset += n; toOffset += n; 
                        }
                    }
                    break;
                }
            case pvShort: {
                    PVShortArray pvfrom = (PVShortArray)from;
                    while(len>0) {
                        int num = 0;
                        short[] data = null;
                        int dataOffset = 0;
                        synchronized(shortArrayData) {
                            num = pvfrom.get(offset,len,shortArrayData);
                            data = shortArrayData.data;
                            dataOffset = shortArrayData.offset;
                        }
                        if(num<=0) break;
                        while(num>0) {
                            int n = fromShortArray(to,toOffset,num,data,dataOffset);
                            if(n<=0) break;
                            len -= n; num -= n; ncopy+=n; offset += n; toOffset += n; 
                        }
                    }
                    break;
                }
            case pvInt: {
                    PVIntArray pvfrom = (PVIntArray)from;
                    while(len>0) {
                        int num = 0;
                        int[] data = null;
                        int dataOffset = 0;
                        synchronized(intArrayData) {
                            num = pvfrom.get(offset,len,intArrayData);
                            data = intArrayData.data;
                            dataOffset = intArrayData.offset;
                        }
                        if(num<=0) break;
                        while(num>0) {
                            int n = fromIntArray(to,toOffset,num,data,dataOffset);
                            if(n<=0) break;
                            len -= n; num -= n; ncopy+=n; offset += n; toOffset += n; 
                        }
                    }
                    break;
                }
            case pvLong: {
                    PVLongArray pvfrom = (PVLongArray)from;
                    while(len>0) {
                        int num = 0;
                        long[] data = null;
                        int dataOffset = 0;
                        synchronized(longArrayData) {
                            num = pvfrom.get(offset,len,longArrayData);
                            data = longArrayData.data;
                            dataOffset = longArrayData.offset;
                        }
                        if(num<=0) break;
                        while(num>0) {
                            int n = fromLongArray(to,toOffset,num,data,dataOffset);
                            if(n<=0) break;
                            len -= n; num -= n; ncopy+=n; offset += n; toOffset += n; 
                        }
                    }
                    break;
                }
            case pvFloat: {
                    PVFloatArray pvfrom = (PVFloatArray)from;
                    while(len>0) {
                        int num = 0;
                        float[] data = null;
                        int dataOffset = 0;
                        synchronized(floatArrayData) {
                            num = pvfrom.get(offset,len,floatArrayData);
                            data = floatArrayData.data;
                            dataOffset = floatArrayData.offset;
                        }
                        if(num<=0) break;
                        while(num>0) {
                            int n = fromFloatArray(to,toOffset,num,data,dataOffset);
                            if(n<=0) break;
                            len -= n; num -= n; ncopy+=n; offset += n; toOffset += n; 
                        }
                    }
                    break;
                }
            case pvDouble: {
                    PVDoubleArray pvfrom = (PVDoubleArray)from;
                    while(len>0) {
                        int num = 0;
                        double[] data = null;
                        int dataOffset = 0;
                        synchronized(doubleArrayData) {
                            num = pvfrom.get(offset,len,doubleArrayData);
                            data = doubleArrayData.data;
                            dataOffset = doubleArrayData.offset;
                        }
                        if(num<=0) break;
                        while(num>0) {
                            int n = fromDoubleArray(to,toOffset,num,data,dataOffset);
                            if(n<=0) break;
                            len -= n; num -= n; ncopy+=n; offset += n; toOffset += n; 
                        }
                    }
                    break;
                }
            }
            return ncopy;
        }
        
    }  
}
