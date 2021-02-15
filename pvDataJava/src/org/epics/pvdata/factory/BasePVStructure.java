/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.factory;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.epics.pvdata.misc.BitSet;
import org.epics.pvdata.pv.DeserializableControl;
import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.PVBoolean;
import org.epics.pvdata.pv.PVByte;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVDouble;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVFloat;
import org.epics.pvdata.pv.PVInt;
import org.epics.pvdata.pv.PVLong;
import org.epics.pvdata.pv.PVScalarArray;
import org.epics.pvdata.pv.PVShort;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.PVStructureArray;
import org.epics.pvdata.pv.PVUnion;
import org.epics.pvdata.pv.PVUnionArray;
import org.epics.pvdata.pv.ScalarArray;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.SerializableControl;
import org.epics.pvdata.pv.Structure;
import org.epics.pvdata.pv.Type;

/**
 * Base class for a PVStructure.
 * @author mrk
 *
 */
public class BasePVStructure extends AbstractPVField implements PVStructure
{
    private static final PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
    private PVField[] pvFields;

    private void setParentAndName() {
        String[] fieldNames = getStructure().getFieldNames();
        Field[] fields = getStructure().getFields();
        int length = pvFields.length;
        if(fieldNames.length!=length) {
            throw new IllegalStateException("PVStructure Logic error");
        }
        for(int i=0; i<length; i++) {
            AbstractPVField xxx = (AbstractPVField)pvFields[i];
            xxx.setData(fields[i],this,fieldNames[i]);
            if(xxx.getField().getType()==Type.structure) {
                BasePVStructure yyy = (BasePVStructure)xxx;
                yyy.setParentAndName();
            }
        }
    }

    /**
     * Constructor.
     * @param structure the reflection interface for the PVStructure data.
     */
    public BasePVStructure(Structure structure) {
        super(structure);
    	Field[] fields = structure.getFields();
    	pvFields = new PVField[fields.length];
    	for(int i=0; i < pvFields.length; i++) {
    	    pvFields[i] = pvDataCreate.createPVField(fields[i]);
    	}
    	setParentAndName();
    }
    /**
     * Constructor.
     * @param structure the reflection interface for the PVStructure data.
     * @param pvFields The PVField array for the subfields.
     */
    public BasePVStructure(Structure structure, PVField[] pvFields)
    {
        super(structure);
        this.pvFields = pvFields;
        setParentAndName();
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.factory.AbstractPVField#setImmutable()
     */
    @Override
    public void setImmutable() {
        super.setImmutable();
        for (PVField pvField : pvFields) {
            pvField.setImmutable();
        }
        super.setImmutable();
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVStructure#getSubField(java.lang.String)
     */
    public PVField getSubField(String fieldName) {
        return findSubField(fieldName,this);
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVStructure#getSubField(int)
     */
    public PVField getSubField(int fieldOffset) {
        if(fieldOffset<=getFieldOffset()) {
            return null;
        }
        if(fieldOffset>getNextFieldOffset()) return null;
        for(PVField pvField: pvFields) {
            if(pvField.getFieldOffset()==fieldOffset) return pvField;
            if(pvField.getNextFieldOffset()<=fieldOffset) continue;
            if(pvField.getField().getType()==Type.structure) {
                return ((PVStructure)pvField).getSubField(fieldOffset);
            }
        }
        throw new IllegalStateException("PVStructure.getSubField: Logic error");
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVStructure#getStructure()
     */
    public Structure getStructure() {
        return (Structure)getField();
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVStructure#getPVFields()
     */
    public PVField[] getPVFields() {
        return pvFields;
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVStructure#getBooleanField(java.lang.String)
     */
    //@Deprecated
    public PVBoolean getBooleanField(String fieldName) {
    	return getSubField(PVBoolean.class, fieldName);
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVStructure#getByteField(java.lang.String)
     */
    //@Deprecated
    public PVByte getByteField(String fieldName) {
    	return getSubField(PVByte.class, fieldName);
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVStructure#getShortField(java.lang.String)
     */
    //@Deprecated
    public PVShort getShortField(String fieldName) {
    	return getSubField(PVShort.class, fieldName);
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVStructure#getIntField(java.lang.String)
     */
    //@Deprecated
    public PVInt getIntField(String fieldName) {
    	return getSubField(PVInt.class, fieldName);
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVStructure#getSubField(java.lang.Class, java.lang.String)
     */
	public <T extends PVField> T getSubField(Class<T> c, String fieldName)
	{
		PVField pv = findSubField(fieldName, this);
		if (c.isInstance(pv))
			return c.cast(pv);
		else
			return null;
	}

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVStructure#getSubField(java.lang.Class, int)
     */
	public <T extends PVField> T getSubField(Class<T> c, int fieldOffset)
	{
		PVField pv = getSubField(fieldOffset);
		if (c.isInstance(pv))
			return c.cast(pv);
		else
			return null;
	}

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVStructure#getLongField(java.lang.String)
     */
    //@Deprecated
    public PVLong getLongField(String fieldName) {
    	return getSubField(PVLong.class, fieldName);
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVStructure#getFloatField(java.lang.String)
     */
    //@Deprecated
    public PVFloat getFloatField(String fieldName) {
    	return getSubField(PVFloat.class, fieldName);
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVStructure#getDoubleField(java.lang.String)
     */
    //@Deprecated
    public PVDouble getDoubleField(String fieldName) {
    	return getSubField(PVDouble.class, fieldName);
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVStructure#getStringField(java.lang.String)
     */
    //@Deprecated
    public PVString getStringField(String fieldName) {
    	return getSubField(PVString.class, fieldName);
    }
	/* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVStructure#getStructureField(java.lang.String)
     */
    //@Deprecated
    public PVStructure getStructureField(String fieldName) {
    	return getSubField(PVStructure.class, fieldName);
    }
	/* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVStructure#getUnionField(java.lang.String)
     */
    //@Deprecated
    public PVUnion getUnionField(String fieldName) {
    	return getSubField(PVUnion.class, fieldName);
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVStructure#getArrayField(java.lang.String, org.epics.pvdata.pv.ScalarType)
     */
    //@Deprecated
    public PVScalarArray getScalarArrayField(String fieldName, ScalarType elementType) {
        PVField pvField = findSubField(fieldName,this);
        if(pvField==null) {
        	return null;
        }
        Field field = pvField.getField();
        Type type = field.getType();
        if(type!=Type.scalarArray) {
            return null;
        }
        ScalarArray array = (ScalarArray)field;
        if(array.getElementType()!=elementType) {
            return null;
        }
        return (PVScalarArray)pvField;
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVStructure#getStructureArrayField(java.lang.String)
     */
    //@Deprecated
	public PVStructureArray getStructureArrayField(String fieldName) {
    	return getSubField(PVStructureArray.class, fieldName);
	}
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVStructure#getUnionArrayField(java.lang.String)
     */
    //@Deprecated
	public PVUnionArray getUnionArrayField(String fieldName) {
    	return getSubField(PVUnionArray.class, fieldName);
    }

    private static boolean checkValid(PVStructure pvStructure,String fullName) {
        boolean result = true;
        PVField[] pvFields = pvStructure.getPVFields();
        Structure structure = pvStructure.getStructure();
        Field[] fields = structure.getFields();
        String[] fieldNames = structure.getFieldNames();
        int length = pvFields.length;
        if(length!=fieldNames.length) {
            result = false;
            System.err.printf("checkValid pvFields.length %d fieldNames.length %d%n",length,fieldNames.length);
            if(length>fieldNames.length) length = fieldNames.length;
        }
        if(length!=fields.length) {
            result = false;
            System.err.printf("checkValid pvFields.length %d fields.length %d%n",length,fields.length);
            if(length>fields.length) length = fields.length;
        }
        String name = fullName;
        if(fullName.length()>0) name += ".";
        for(int i=0; i<length; i++) {
            PVField pvField = pvFields[i];
            String nnn = name + fieldNames[i];
            if(pvField.getParent()!=pvStructure) {
               result = false;
               System.err.printf("%s pvParent %s bad%n",nnn,pvField.getParent());
            }
            if(pvField.getField()!=fields[i]) {
                result = false;
                System.err.printf("%s pvField.getField()!=fields[%d]%n",nnn,i);
                System.err.printf("pvField.getField()%n%s%n",pvField.getField());
                System.err.printf("fields[i]%n%s%n", fields[i]);
            }
            if(pvField.getFieldName()!=fieldNames[i]) {
                result = false;
                System.err.printf("%s pvField.getFieldName()!=fieldNames[%d]%n",nnn,i);
                System.err.printf("pvField.getFieldName() %s%n",pvField.getFieldName());
                System.err.printf("fieldNames[i] %s%n",fieldNames[i]);
            }
            if(pvField.getField().getType()==Type.structure) {
                boolean yyy =checkValid((PVStructure)pvField,nnn);
                if(result) result = yyy;
            }
        }
        return result;
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVStructure#checkValid()
     */
    public boolean checkValid() {
        PVStructure xxx = this;
        while(xxx.getParent()!=null) {
            xxx = xxx.getParent();
        }
        return checkValid(xxx,"");
    }

    private PVField findSubField(String fieldName,PVStructure pvStructure) {
        if(fieldName==null || fieldName.length()<1) return null;
        int index = fieldName.indexOf('.');
        String name = fieldName;
        String restOfName = null;
        if(index>0) {
            name = fieldName.substring(0, index);
            if(fieldName.length()>index) {
                restOfName = fieldName.substring(index+1);
            }
        }
        PVField[] pvFields = pvStructure.getPVFields();
        String[] fieldNames = pvStructure.getStructure().getFieldNames();
        PVField pvField = null;
        for(int i=0; i<pvFields.length; i++) {
            if(fieldNames[i].equals(name)) {
                pvField = pvFields[i];
                break;
            }
        }
        if(pvField==null) return null;
        if(restOfName==null) return pvField;
        if(pvField.getField().getType()!=Type.structure) return null;
        return findSubField(restOfName,(PVStructure)pvField);
    }

	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.Serializable#serialize(java.nio.ByteBuffer, org.epics.pvdata.pv.SerializableControl)
	 */
	public void serialize(ByteBuffer buffer, SerializableControl flusher) {
        for (PVField pvField : pvFields) pvField.serialize(buffer, flusher);
	}
	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.Serializable#deserialize(java.nio.ByteBuffer, org.epics.pvdata.pv.DeserializableControl)
	 */
	public void deserialize(ByteBuffer buffer, DeserializableControl control) {
        for (PVField pvField : pvFields) pvField.deserialize(buffer, control);
	}
	/* (non-Javadoc)
	 * @see org.epics.pvdata.pvCopy.BitSetSerializable#deserialize(java.nio.ByteBuffer, org.epics.pvdata.pv.DeserializableControl, org.epics.pvdata.misc.BitSet)
	 */
	public void deserialize(ByteBuffer buffer, DeserializableControl control, BitSet bitSet) {
        int offset = getFieldOffset();
        int numberFields = getNumberFields();
        int next = bitSet.nextSetBit(offset);

        // no more changes or no changes in this structure
        if (next<0 || next>=offset+numberFields) return;

        // entire structure
        if(offset==next) {
        	deserialize(buffer, control);
        	return;
        }

        for (final PVField pvField : pvFields) {
            offset = pvField.getFieldOffset();
            numberFields = pvField.getNumberFields();
            next = bitSet.nextSetBit(offset);
            // no more changes
            if (next < 0) return;
            //  no change in this pvField
            if (next >= offset + numberFields) continue;

            // serialize field or fields
            if (numberFields == 1)
                pvField.deserialize(buffer, control);
            else
                ((PVStructure) pvField).deserialize(buffer, control, bitSet);
        }
	}
	/* (non-Javadoc)
	 * @see org.epics.pvdata.pvCopy.BitSetSerializable#serialize(java.nio.ByteBuffer, org.epics.pvdata.pv.SerializableControl, org.epics.pvdata.misc.BitSet)
	 */
	public void serialize(ByteBuffer buffer, SerializableControl flusher, BitSet bitSet) {
        int offset = getFieldOffset();
        int numberFields = getNumberFields();
        int next = bitSet.nextSetBit(offset);

        // no more changes or no changes in this structure
        if (next<0 || next>=offset+numberFields) return;

        // entire structure
        if(offset==next) {
        	serialize(buffer, flusher);
        	return;
        }

        for (final PVField pvField : pvFields) {
            offset = pvField.getFieldOffset();
            numberFields = pvField.getNumberFields();
            next = bitSet.nextSetBit(offset);
            // no more changes
            if (next < 0) return;
            //  no change in this pvField
            if (next >= offset + numberFields) continue;

            // serialize field or fields
            if (numberFields == 1)
                pvField.serialize(buffer, flusher);
            else
                ((PVStructure) pvField).serialize(buffer, flusher, bitSet);
        }
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		// TODO anything else?
		if (obj instanceof PVStructure) {
			PVStructure b = (PVStructure)obj;
			if (!getStructure().equals(b.getStructure()))
			    return false;

			final PVField[] bfields = b.getPVFields();
			if (bfields.length == pvFields.length)
			{
		        for (int i = 0; i < pvFields.length; i++)
		        	if (!pvFields[i].equals(bfields[i]))
		        		return false;

		        return true;
			}
			else
				return false;
		}
		else
			return false;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result;
		result = prime * result + Arrays.hashCode(pvFields);
		return result;
	}
}
