/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.misc;


import java.nio.ByteBuffer;

import org.epics.pvData.factory.AbstractPVArray;
import org.epics.pvData.factory.AbstractPVScalar;
import org.epics.pvData.factory.BasePVStructure;
import org.epics.pvData.pv.Array;
import org.epics.pvData.pv.Field;
import org.epics.pvData.pv.MessageType;
import org.epics.pvData.pv.PVField;
import org.epics.pvData.pv.PVInt;
import org.epics.pvData.pv.PVString;
import org.epics.pvData.pv.PVStringArray;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.Scalar;
import org.epics.pvData.pv.ScalarType;
import org.epics.pvData.pv.StringArrayData;
import org.epics.pvData.pv.Structure;
import org.epics.pvData.pv.Type;

/**
 * Factory for an enumerated structure.
 * @author mrk
 *
 */
public class EnumeratedFactory {
    /**
     * Replace the implementation of the subfields of pvField if it is an enumerated structure.
     * A put to the index causes the choice to also update. A put to the choice also causes the
     * index to update.
     * @param pvField The field. It must be an enumerated structure.
     */
    public static void replacePVField(PVField pvField) {
        if(pvField instanceof Enumerated) return;
        if(pvField.getField().getType()!=Type.structure) {
            pvField.message("field is not a structure", MessageType.error);
            return;
        }
        PVStructure pvStructure = (PVStructure)pvField;
        Structure structure = pvStructure.getStructure();
        Field[] fields = structure.getFields();
        Field field = fields[0];
        if(!field.getFieldName().equals("index") || field.getType()!=Type.scalar || ((Scalar)field).getScalarType()!=ScalarType.pvInt) {
            pvStructure.message("structure does not have field index of type int", MessageType.error);
            return;
        }
        field = fields[1];
        if(!field.getFieldName().equals("choice") || field.getType()!=Type.scalar || ((Scalar)field).getScalarType()!=ScalarType.pvString) {
            pvStructure.message("structure does not have field choice of type string", MessageType.error);
            return;
        }
        field = fields[2];
        if(!field.getFieldName().equals("choices") || field.getType()!=Type.scalarArray || ((Array)field).getElementType()!=ScalarType.pvString) {
            pvStructure.message("structure does not have field choices of type array", MessageType.error);
            return;
        }
        Array array = (Array)fields[2];
        if(array.getElementType()!=ScalarType.pvString) {
            pvStructure.message("elementType for choices is not string", MessageType.error);
            return;
        }
        PVField[] pvFields = pvStructure.getPVFields();
        new EnumeratedImpl(pvStructure,(PVInt)pvFields[0],(PVString)pvFields[1],(PVStringArray)pvFields[2]);
    }
    /**
     * getEnumerated.
     * @param pvField The pvField.
     * @return (null, Enumerated) if pvField (is not, is) an Enumerated structure.
     */
    public static Enumerated getEnumerated(PVField pvField) {
        if(pvField instanceof Enumerated) return (Enumerated)pvField;
        return null;
    }
   
    private static class EnumeratedImpl extends BasePVStructure implements Enumerated{
        private int index;
        private String[] choices;
        private Index pvIndex;
        private Choice pvChoice;
        private Choices pvChoices;

        /**
         * The constructor.
         * @param dbIndex The PVField for the index.
         * @param dbChoice The PVField for the choice.
         * @param dbChoices The PVField for the choices.
         */
        private EnumeratedImpl(PVStructure pvField,PVInt pvOldIndex, PVString pvOldChoice, PVStringArray pvOldChoices) {
            super(pvField.getParent(),pvField.getStructure());
            pvIndex = new Index(this,pvOldIndex.getScalar());
            pvChoice = new Choice(this,pvOldChoice.getScalar());      
            pvChoices = new Choices(this,pvOldChoices.getArray());
            if(pvOldChoices.getLength()>0) {
                StringArrayData stringArrayData = new StringArrayData();
                int len = pvOldChoices.getLength();
                len = pvOldChoices.get(0,len, stringArrayData);
                pvChoices.put(0, len, stringArrayData.data , 0);
            }        
            String choice = pvOldChoice.get();
            if(choice!=null) pvChoice.put(choice);
            pvField.replacePVField(this);
            PVField[] pvFields = super.getPVFields();
            pvFields[0].replacePVField(pvIndex);
            pvFields[1].replacePVField(pvChoice);
            pvFields[2].replacePVField(pvChoices);
        }       
        /* (non-Javadoc)
         * @see org.epics.pvData.misc.Enumerated#getPV()
         */
        public PVStructure getPV() {
            return this;
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.misc.Enumerated#getChoice()
         */
        public PVString getChoice() {
            return pvChoice;
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.misc.Enumerated#getChoices()
         */
        public PVStringArray getChoices() {
            return pvChoices;
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.misc.Enumerated#getIndex()
         */
        public PVInt getIndex() {
            return pvIndex;
        }

        private class Index extends AbstractPVScalar implements PVInt {
            private Index(PVStructure parent,Scalar scalar) {
                super(parent,scalar);
            }
            /* (non-Javadoc)
             * @see org.epics.pvData.pv.PVInt#get()
             */
            public int get() {
                if(index>=choices.length) return -1;
                return index;
            }
            /* (non-Javadoc)
             * @see org.epics.pvData.pv.PVInt#put(int)
             */
            public void put(int value) {
                if(!super.isMutable()) {
                    super.message("not isMutable", MessageType.error);
                    return;
                }
                if(value<0 || value>=choices.length) {
                    super.message("illegal choice " + value + " num choices " + choices.length, MessageType.error);
                    return;
                }
                if(index!=value) {
                    index = value;
                    pvChoice.postIt();
                }
            }
            private void postIt() {
                super.postPut();
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
			 * @see org.epics.pvData.pv.Serializable#serialize(java.nio.ByteBuffer)
			 */
			public void serialize(ByteBuffer buffer) {
		        buffer.putInt(index);
			}
			/* (non-Javadoc)
			 * @see org.epics.pvData.pv.Serializable#deserialize(java.nio.ByteBuffer)
			 */
			public void deserialize(ByteBuffer buffer) {
		        index = buffer.getInt();
			}
			// TODO equals
        }

        private class Choice extends AbstractPVScalar implements PVString {

            private Choice(PVStructure parent,Scalar scalar) {
                super(parent,scalar);
            }
            /* (non-Javadoc)
             * @see org.epics.pvData.pv.PVString#get()
             */
            public String get() {
                if(index>=choices.length) return null;
                return choices[index];
            }
            /* (non-Javadoc)
             * @see org.epics.pvData.pv.PVString#put(java.lang.String)
             */
            public void put(String value) {
                if(!super.isMutable()) {   
                    super.message("not isMutable", MessageType.error);
                }
                for(int i=0; i<choices.length; i++) {
                    if(value.equals(choices[i])) {
                        if(index!=i) {
                            index = i;
                            pvIndex.postIt();
                        }
                        return;
                    }
                }
                super.message("illegal choice", MessageType.error);
            }
            private void postIt() {
                super.postPut();
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
             * @see org.epics.pvData.pv.Serializable#serialize(java.nio.ByteBuffer)
             */
            public void serialize(ByteBuffer buffer) {
                AbstractPVArray.serializeString(get(), buffer);
            }
            /* (non-Javadoc)
             * @see org.epics.pvData.pv.Serializable#deserialize(java.nio.ByteBuffer)
             */
            public void deserialize(ByteBuffer buffer) {
                put(AbstractPVArray.deserializeString(buffer));
            }
            // TODO equals
        }

        private class Choices extends AbstractPVArray implements PVStringArray
        {
            private Choices(PVStructure parent,Array array)
            {
                super(parent,array);
                choices = new String[capacity];           
            }       
            /* (non-Javadoc)
             * @see org.epics.pvData.factory.AbstractPVField#toString(int)
             */
            public String toString(int indentLevel) {
                return convert.getString(this, indentLevel)
                + super.toString(indentLevel);
            }
            /* (non-Javadoc)
             * @see org.epics.pvData.factory.AbstractPVArray#setCapacity(int)
             */
            public void setCapacity(int len) {
                if(!capacityMutable) {
                    super.message("not capacityMutable", MessageType.error);
                    return;
                }
                if(length>len) length = len;
                String[]newarray = new String[len];
                if(length>0) System.arraycopy(choices,0,newarray,0,length);
                choices = newarray;
                capacity = len;
                if(index>=capacity) index = 0;
            }
            /* (non-Javadoc)
             * @see org.epics.pvData.pv.PVStringArray#get(int, int, org.epics.pvData.pv.StringArrayData)
             */
            public int get(int offset, int len, StringArrayData data) {
                int n = len;
                if(offset+len > length) n = length - offset;
                if(n<=0) return 0;
                data.data = choices;
                data.offset = offset;
                return n;    
            }
            /* (non-Javadoc)
             * @see org.epics.pvData.pv.PVStringArray#put(int, int, java.lang.String[], int)
             */
            public int put(int offset, int len, String[]from, int fromOffset) {
                if(!super.isMutable()) {
                    super.message("not isMutable", MessageType.error);
                    return 0;
                }
                if(offset+len > length) {
                    int newlength = offset + len;
                    if(newlength>capacity) {
                        setCapacity(newlength);
                        newlength = capacity;
                        len = newlength - offset;
                        if(len<=0) return 0;
                    }
                    length = newlength;
                }
                System.arraycopy(from,fromOffset,choices,offset,len);
                if(index>=length) {
                    index = 0;
                    pvIndex.postIt();
                    pvChoice.postIt();
                }
                return len;      
            }
        	/* (non-Javadoc)
        	 * @see org.epics.pvData.pv.Serializable#serialize(java.nio.ByteBuffer, int, int)
        	 */
        	public void serialize(ByteBuffer buffer, int offset, int count) {
        		// check bounds
        		if (offset < 0) offset = 0;
        		if (count < 0) count = length;
        		final int maxCount = length - offset;
        		if (count > maxCount)
        			count = maxCount;
        		// write
        		writeSize(count, buffer);
        		for (int i = 0; i < count; i++)
        			serializeString(choices[i], buffer);
        	}
        	/* (non-Javadoc)
        	 * @see org.epics.pvData.pv.Serializable#deserialize(java.nio.ByteBuffer)
        	 */
        	public void deserialize(ByteBuffer buffer) {
        		final int size = readSize(buffer);
        		if (size >= 0) {
        			// prepare array, if necessary
        			if (size > capacity)
        				setCapacity(size);
        			// retrieve value from the buffer
        			for (int i = 0; i < size; i++)
        				choices[i] = deserializeString(buffer);
        			// set new length
        			length = size;
        		}
        		// TODO null arrays (size == -1) not supported
        	}
        	// TODO equals
        }
    }

}
