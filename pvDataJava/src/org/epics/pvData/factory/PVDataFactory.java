/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.factory;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Set;

import org.epics.pvData.pv.Array;
import org.epics.pvData.pv.Convert;
import org.epics.pvData.pv.Field;
import org.epics.pvData.pv.FieldCreate;
import org.epics.pvData.pv.MessageType;
import org.epics.pvData.pv.PVArray;
import org.epics.pvData.pv.PVAuxInfo;
import org.epics.pvData.pv.PVBoolean;
import org.epics.pvData.pv.PVByte;
import org.epics.pvData.pv.PVDataCreate;
import org.epics.pvData.pv.PVDatabase;
import org.epics.pvData.pv.PVDouble;
import org.epics.pvData.pv.PVField;
import org.epics.pvData.pv.PVFloat;
import org.epics.pvData.pv.PVInt;
import org.epics.pvData.pv.PVLong;
import org.epics.pvData.pv.PVRecord;
import org.epics.pvData.pv.PVScalar;
import org.epics.pvData.pv.PVShort;
import org.epics.pvData.pv.PVString;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.Scalar;
import org.epics.pvData.pv.ScalarType;
import org.epics.pvData.pv.Structure;
import org.epics.pvData.pv.Type;



/**
 * Factory to create default implementations for PVField objects.
 * The PVField instances are created via interface PVDataCreate,
 * which is obtained via a call to <i>PVDataCreateFactory.getPVDataCreate</i>.
 * @author mrk
 *
 */
public class PVDataFactory {
    private PVDataFactory() {} // don't create
    private static PVDataCreateImpl pvdataCreate = new PVDataCreateImpl();
    /**
     * Get the interface for PVDataCreate.
     * @return The interface.
     */
    public static PVDataCreate getPVDataCreate() {
        return pvdataCreate;
    }
    
    private static final class PVDataCreateImpl implements PVDataCreate{
        private static FieldCreate fieldCreate = FieldFactory.getFieldCreate();
        private static Convert convert = ConvertFactory.getConvert();
        
        
        /* (non-Javadoc)
		 * @see org.epics.pvData.pv.PVDataCreate#createPVField(org.epics.pvData.pv.PVStructure, org.epics.pvData.pv.Field)
		 */
		public PVField createPVField(PVStructure parent, Field field) {
			switch(field.getType()) {
			case scalar: 	  return createPVScalar(parent, (Scalar)field); 
			case scalarArray: return createPVArray(parent, (Array)field); 
			case structure:   return new BasePVStructure(parent,(Structure)field);
			}
            throw new IllegalArgumentException("Illegal Type");
		}
		
		private final PVScalar createPVScalar(PVStructure parent, Scalar scalar)
		{
           switch(scalar.getScalarType()) {
            case pvBoolean: return new BooleanData(parent,scalar);
            case pvByte:    return new ByteData(parent,scalar);
            case pvShort:   return new ShortData(parent,scalar);
            case pvInt:     return new IntData(parent,scalar);
            case pvLong:    return new LongData(parent,scalar);
            case pvFloat:   return new FloatData(parent,scalar);
            case pvDouble:  return new DoubleData(parent,scalar);
            case pvString:  return new StringData(parent,scalar);
            }
            throw new IllegalArgumentException(
                "Illegal Type. Must be pvBoolean,...,pvStructure");
	 	}
		/* (non-Javadoc)
         * @see org.epics.pvData.pv.PVDataCreate#createPVScalar(org.epics.pvData.pv.PVStructure, java.lang.String, org.epics.pvData.pv.ScalarType)
         */
        public PVScalar createPVScalar(PVStructure parent,String fieldName,ScalarType scalarType)
        {
        	return createPVScalar(parent, fieldCreate.createScalar(fieldName, scalarType));
        }

        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVDataCreate#createPVArray(org.epics.pvData.pv.PVStructure, java.lang.String, org.epics.pvData.pv.ScalarType)
         */
        public final PVArray createPVArray(PVStructure parent,Array array)
        {
        	switch(array.getElementType()) {
            case pvBoolean: return new BasePVBooleanArray(parent,array);
            case pvByte:    return new BasePVByteArray(parent,array);
            case pvShort:   return new BasePVShortArray(parent,array);
            case pvInt:     return new BasePVIntArray(parent,array);
            case pvLong:    return new BasePVLongArray(parent,array);
            case pvFloat:   return new BasePVFloatArray(parent,array);
            case pvDouble:  return new BasePVDoubleArray(parent,array);
            case pvString:  return new BasePVStringArray(parent,array);
            }
            throw new IllegalArgumentException("Illegal Type. Logic error");
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVDataCreate#createPVArray(org.epics.pvData.pv.PVStructure, java.lang.String, org.epics.pvData.pv.ScalarType)
         */
        public PVArray createPVArray(PVStructure parent,String fieldName,ScalarType scalarType)
        {
        	return createPVArray(parent, fieldCreate.createArray(fieldName, scalarType));
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVDataCreate#createPVStructure(org.epics.pvData.pv.PVStructure, java.lang.String, org.epics.pvData.pv.Field[])
         */
        public PVStructure createPVStructure(PVStructure parent,String fieldName,Field[] fields) {
        	Structure structure = fieldCreate.createStructure(fieldName, fields);
            return new BasePVStructure(parent,structure);
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVDataCreate#createPVStructure(org.epics.pvData.pv.PVStructure, java.lang.String, org.epics.pvData.pv.PVStructure)
         */
        public PVStructure createPVStructure(PVStructure parent,String fieldName,PVStructure structToClone)
        {
        	Field[] fields = null;
        	if(structToClone==null) {
        	    fields = new Field[0];	
        	} else {
        	    fields = structToClone.getStructure().getFields();
        	}
        	Structure structure = fieldCreate.createStructure(fieldName,fields);
        	PVStructure pvStructure = new BasePVStructure(parent,structure);
        	if(structToClone!=null) copyStructure(structToClone,pvStructure);
        	return pvStructure;
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVDataCreate#createPVStructure(org.epics.pvData.pv.PVStructure, java.lang.String, org.epics.pvData.pv.PVDatabase, java.lang.String)
         */
        public PVStructure createPVStructure(PVStructure parent,String fieldName,PVDatabase pvDatabase,String structureName)
        {
        	PVStructure pvSource = pvDatabase.findStructure(structureName);
        	if(pvSource==null) {
        		pvDatabase.message("clonePVStructure structureName " + structureName + " not found",
        				MessageType.error);
        		return null;
        	}
        	return createPVStructure(parent,fieldName,pvSource);
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVDataCreate#createPVRecord(java.lang.String, org.epics.pvData.pv.Field[])
         */
        public PVRecord createPVRecord(String recordName,Field[] fields) {
            Structure structure = fieldCreate.createStructure("", fields);
            PVRecord pvRecord = new BasePVRecord(recordName,structure);
            return pvRecord;
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVDataCreate#createPVRecord(java.lang.String, org.epics.pvData.pv.PVStructure)
         */
        public PVRecord createPVRecord(String recordName,PVStructure structToClone) {
        	Field[] fields = null;
        	if(structToClone==null) {
        		fields = new Field[0];
        	} else {
        	    fields  = structToClone.getStructure().getFields();
        	}
        	Structure structure = fieldCreate.createStructure("", fields);
            PVRecord pvRecord = new BasePVRecord(recordName,structure);
            if(structToClone!=null) copyStructure(structToClone,pvRecord.getPVStructure());
            return pvRecord;
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVDataCreate#createPVRecord(java.lang.String, org.epics.pvData.pv.PVDatabase, java.lang.String)
         */
        public PVRecord createPVRecord(String recordName,PVDatabase pvDatabase,String structureName) {
        	PVStructure structToClone = pvDatabase.findStructure(structureName);
        	if(structToClone==null) {
        		pvDatabase.message("createPVRecord structureName " + structureName + " not found",
        				MessageType.error);
        		return null;
        	}
        	return createPVRecord(recordName,structToClone);
        }
        
        private void copyStructure(PVStructure from,PVStructure to)  {
        	Map<String,PVScalar> attributes = from.getPVAuxInfo().getInfos();
        	PVAuxInfo pvAttribute = to.getPVAuxInfo();
        	Set<String> keys = attributes.keySet();
            for(String key : keys) {
                PVScalar fromAttribute = attributes.get(key);
                PVScalar toAttribute = pvAttribute.createInfo(key, fromAttribute.getScalar().getScalarType());
                convert.copyScalar(fromAttribute, toAttribute);
            }
            PVField[] fromFields = from.getPVFields();
            PVField[] toFields = to.getPVFields();
            for(int i=0; i<fromFields.length; i++) {
            	PVField fromPV = fromFields[i];
            	PVField toPV = toFields[i];
            	Type type = fromPV.getField().getType();
            	if(type==Type.structure) {
            		copyStructure((PVStructure)fromPV,(PVStructure)toPV);
            		continue;
            	}
            	attributes = fromPV.getPVAuxInfo().getInfos();
            	pvAttribute = toPV.getPVAuxInfo();
            	keys = attributes.keySet();
                for(String key : keys) {
                    PVScalar fromAttribute = attributes.get(key);
                    PVScalar toAttribute = pvAttribute.createInfo(key, fromAttribute.getScalar().getScalarType());
                    convert.copyScalar(fromAttribute, toAttribute);
                }
                if(type==Type.scalar) {
                	convert.copyScalar((PVScalar)fromPV, (PVScalar)toPV);
                } else {
                	PVArray fromPVArray = (PVArray)fromPV;
                	PVArray toPVArray = (PVArray)toPV;
                	convert.copyArray(fromPVArray,0,toPVArray, 0, fromPVArray.getLength());
                }
            }
        }
    }
    
    private static class BooleanData extends AbstractPVScalar
        implements PVBoolean
    {
        private boolean value = false;

        private BooleanData(PVStructure parent,Scalar scalar) {
            super(parent,scalar);
        }        
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVBoolean#get()
         */
        public boolean get() {
            return value;
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVBoolean#put(boolean)
         */
        public void put(boolean value) {
            if(super.isMutable()) {
                this.value = value;
                super.postPut();
                return ;
            }
            super.message("not isMutable", MessageType.error);
        }       
        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        public String toString() {
            return toString(0);
        }       
        /* (non-Javadoc)
         * @see org.epics.pvData.factory.AbstractPVField#toString(int)
         */
        public String toString(int indentLevel) {
            return convert.getString(this, indentLevel)
            + super.toString(indentLevel);
        }
		/* (non-Javadoc)
		 * @see org.epics.pvData.pv.Serializable#getSerializationSize()
		 */
		public int getSerializationSize() {
			return 1;
		}
		/* (non-Javadoc)
		 * @see org.epics.pvData.pv.Serializable#serialize(java.nio.ByteBuffer)
		 */
		public void serialize(ByteBuffer buffer) {
			buffer.put(value ? (byte)1 : (byte)0);
		}
		/* (non-Javadoc)
		 * @see org.epics.pvData.pv.Serializable#deserialize(java.nio.ByteBuffer)
		 */
		public void deserialize(ByteBuffer buffer) {
			value = buffer.get() != 0;
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			// TODO anything else?
			if (obj instanceof PVBoolean) {
				PVBoolean b = (PVBoolean)obj;
				return b.get() == value;
			}
			else
				return false;
		}
    }

    private static class ByteData extends AbstractPVScalar implements PVByte {
        private byte value;
        
        private ByteData(PVStructure parent,Scalar scalar) {
            super(parent,scalar);
            value = 0;
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVByte#get()
         */
        public byte get() {
            return value;
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVByte#put(byte)
         */
        public void put(byte value) {
            if(super.isMutable()) {
                this.value = value;
                super.postPut();
                return ;
            }
            super.message("not isMutable", MessageType.error);
        }
        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        public String toString() {
            return toString(0);
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.factory.AbstractPVField#toString(int)
         */
        public String toString(int indentLevel) {
            return convert.getString(this, indentLevel)
            + super.toString(indentLevel);
        }
        /* (non-Javadoc)
		 * @see org.epics.pvData.pv.Serializable#getSerializationSize()
		 */
		public int getSerializationSize() {
			return 1;
		}
		/* (non-Javadoc)
		 * @see org.epics.pvData.pv.Serializable#serialize(java.nio.ByteBuffer)
		 */
		public void serialize(ByteBuffer buffer) {
			buffer.put(value);
		}
		/* (non-Javadoc)
		 * @see org.epics.pvData.pv.Serializable#deserialize(java.nio.ByteBuffer)
		 */
		public void deserialize(ByteBuffer buffer) {
			value = buffer.get();
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			// TODO anything else?
			if (obj instanceof PVByte) {
				PVByte b = (PVByte)obj;
				return b.get() == value;
			}
			else
				return false;
		}
    }

    private static class ShortData extends AbstractPVScalar implements PVShort {
        private short value;
        
        private ShortData(PVStructure parent,Scalar scalar) {
            super(parent,scalar);
            value = 0;
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVShort#get()
         */
        public short get() {
            return value;
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVShort#put(short)
         */
        public void put(short value) {
            if(super.isMutable()) {
                this.value = value;
                super.postPut();
                return ;
            }
            super.message("not isMutable", MessageType.error);
        }
        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        public String toString() {
            return toString(0);
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.factory.AbstractPVField#toString(int)
         */
        public String toString(int indentLevel) {
            return convert.getString(this, indentLevel)
            + super.toString(indentLevel);
        }
		/* (non-Javadoc)
		 * @see org.epics.pvData.pv.Serializable#getSerializationSize()
		 */
		public int getSerializationSize() {
			return 2;
		}
		/* (non-Javadoc)
		 * @see org.epics.pvData.pv.Serializable#serialize(java.nio.ByteBuffer)
		 */
		public void serialize(ByteBuffer buffer) {
			buffer.putShort(value);
		}
		/* (non-Javadoc)
		 * @see org.epics.pvData.pv.Serializable#deserialize(java.nio.ByteBuffer)
		 */
		public void deserialize(ByteBuffer buffer) {
			value = buffer.getShort();
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			// TODO anything else?
			if (obj instanceof PVShort) {
				PVShort b = (PVShort)obj;
				return b.get() == value;
			}
			else
				return false;
		}
    }

    private static class IntData extends AbstractPVScalar implements PVInt {
        private int value;

        private IntData(PVStructure parent,Scalar scalar) {
            super(parent,scalar);
            value = 0;
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVInt#get()
         */
        public int get() {
            return value;
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVInt#put(int)
         */
        public void put(int value) {
            if(super.isMutable()) {
                this.value = value;
                super.postPut();
                return ;
            }
            super.message("not isMutable", MessageType.error);
        }
        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        public String toString() {
            return toString(0);
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.factory.AbstractPVField#toString(int)
         */
        public String toString(int indentLevel) {
            return convert.getString(this, indentLevel)
            + super.toString(indentLevel);
        }
		/* (non-Javadoc)
		 * @see org.epics.pvData.pv.Serializable#getSerializationSize()
		 */
		public int getSerializationSize() {
			return 4;
		}
		/* (non-Javadoc)
		 * @see org.epics.pvData.pv.Serializable#serialize(java.nio.ByteBuffer)
		 */
		public void serialize(ByteBuffer buffer) {
			buffer.putInt(value);
		}
		/* (non-Javadoc)
		 * @see org.epics.pvData.pv.Serializable#deserialize(java.nio.ByteBuffer)
		 */
		public void deserialize(ByteBuffer buffer) {
			value = buffer.getInt();
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			// TODO anything else?
			if (obj instanceof PVInt) {
				PVInt b = (PVInt)obj;
				return b.get() == value;
			}
			else
				return false;
		}
    }

    private static class LongData extends AbstractPVScalar implements PVLong {
        private long value;
        
        private LongData(PVStructure parent,Scalar scalar) {
            super(parent,scalar);
            value = 0;
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVLong#get()
         */
        public long get() {
            return value;
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVLong#put(long)
         */
        public void put(long value) {
            if(super.isMutable()) {
                this.value = value;
                super.postPut();
                return ;
            }
            super.message("not isMutable", MessageType.error);
        }
        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        public String toString() {
            return toString(0);
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.factory.AbstractPVField#toString(int)
         */
        public String toString(int indentLevel) {
            return convert.getString(this, indentLevel)
            + super.toString(indentLevel);
        }
		/* (non-Javadoc)
		 * @see org.epics.pvData.pv.Serializable#getSerializationSize()
		 */
		public int getSerializationSize() {
			return 8;
		}
		/* (non-Javadoc)
		 * @see org.epics.pvData.pv.Serializable#serialize(java.nio.ByteBuffer)
		 */
		public void serialize(ByteBuffer buffer) {
			buffer.putLong(value);
		}
		/* (non-Javadoc)
		 * @see org.epics.pvData.pv.Serializable#deserialize(java.nio.ByteBuffer)
		 */
		public void deserialize(ByteBuffer buffer) {
			value = buffer.getLong();
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			// TODO anything else?
			if (obj instanceof PVLong) {
				PVLong b = (PVLong)obj;
				return b.get() == value;
			}
			else
				return false;
		}
    }

    private static class FloatData extends AbstractPVScalar implements PVFloat {
        private float value;
        
        private FloatData(PVStructure parent,Scalar scalar) {
            super(parent,scalar);
            value = 0;
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVFloat#get()
         */
        public float get() {
            return value;
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVFloat#put(float)
         */
        public void put(float value) {
            if(super.isMutable()) {
                this.value = value;
                super.postPut();
                return ;
            }
            super.message("not isMutable", MessageType.error);
        }
        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        public String toString() {
            return toString(0);
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.factory.AbstractPVField#toString(int)
         */
        public String toString(int indentLevel) {
            return convert.getString(this, indentLevel)
            + super.toString(indentLevel);
        }
		/* (non-Javadoc)
		 * @see org.epics.pvData.pv.Serializable#getSerializationSize()
		 */
		public int getSerializationSize() {
			return 4;
		}
		/* (non-Javadoc)
		 * @see org.epics.pvData.pv.Serializable#serialize(java.nio.ByteBuffer)
		 */
		public void serialize(ByteBuffer buffer) {
			buffer.putFloat(value);
		}
		/* (non-Javadoc)
		 * @see org.epics.pvData.pv.Serializable#deserialize(java.nio.ByteBuffer)
		 */
		public void deserialize(ByteBuffer buffer) {
			value = buffer.getFloat();
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			// TODO anything else?
			if (obj instanceof PVFloat) {
				PVFloat b = (PVFloat)obj;
				return b.get() == value;
			}
			else
				return false;
		}
    }

    private static class DoubleData extends AbstractPVScalar implements PVDouble {
        private double value;
        
        private DoubleData(PVStructure parent,Scalar scalar) {
            super(parent,scalar);
            value = 0;
        }        
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVDouble#get()
         */
        public double get() {
            return value;
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVDouble#put(double)
         */
        public void put(double value) {
            if(super.isMutable()) {
                this.value = value;
                super.postPut();
                return ;
            }
            super.message("not isMutable", MessageType.error);
        }        
        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        public String toString() {
            return toString(0);
        }        
        /* (non-Javadoc)
         * @see org.epics.pvData.factory.AbstractPVField#toString(int)
         */
        public String toString(int indentLevel) {
            return convert.getString(this, indentLevel)
            + super.toString(indentLevel);
        }
		/* (non-Javadoc)
		 * @see org.epics.pvData.pv.Serializable#getSerializationSize()
		 */
		public int getSerializationSize() {
			return 8;
		}
		/* (non-Javadoc)
		 * @see org.epics.pvData.pv.Serializable#serialize(java.nio.ByteBuffer)
		 */
		public void serialize(ByteBuffer buffer) {
			buffer.putDouble(value);
		}
		/* (non-Javadoc)
		 * @see org.epics.pvData.pv.Serializable#deserialize(java.nio.ByteBuffer)
		 */
		public void deserialize(ByteBuffer buffer) {
			value = buffer.getDouble();
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			// TODO anything else?
			if (obj instanceof PVDouble) {
				PVDouble b = (PVDouble)obj;
				return b.get() == value;
			}
			else
				return false;
		}
    }

    private static class StringData extends AbstractPVScalar implements PVString {
        private String value;
        
        private StringData(PVStructure parent,Scalar scalar) {
            super(parent,scalar);
            value = null;
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVString#get()
         */
        public String get() {
            return value;
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVString#put(java.lang.String)
         */
        public void put(String value) {
            if(super.isMutable()) {
                this.value = value;
                super.postPut();
                return ;
            }
            super.message("not isMutable", MessageType.error);
        }
        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        public String toString() {
            return toString(0);
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.factory.AbstractPVField#toString(int)
         */
        public String toString(int indentLevel) {
            return convert.getString(this, indentLevel)
            + super.toString(indentLevel);
        }
		/* (non-Javadoc)
		 * @see org.epics.pvData.pv.Serializable#getSerializationSize()
		 */
		public int getSerializationSize() {
			return AbstractPVArray.getStringSerializationSize(get());
		}
		/* (non-Javadoc)
		 * @see org.epics.pvData.pv.Serializable#serialize(java.nio.ByteBuffer)
		 */
		public void serialize(ByteBuffer buffer) {
			AbstractPVArray.serializeString(get(), buffer);
		}
		/* (non-Javadoc)
		 * @see org.epics.pvData.pv.Serializable#deserialize(java.nio.ByteBuffer)
		 */
		public void deserialize(ByteBuffer buffer) {
			value = AbstractPVArray.deserializeString(buffer);
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			// TODO anything else?
			if (obj instanceof PVString) {
				PVString b = (PVString)obj;
				final String bv = b.get();
				if (bv != null)
					return bv.equals(value);
				else
					return bv == value;
			}
			else
				return false;
		}
    }
}
