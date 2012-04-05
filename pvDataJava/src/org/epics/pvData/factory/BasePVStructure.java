/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.factory;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.epics.pvData.misc.BitSet;
import org.epics.pvData.pv.DeserializableControl;
import org.epics.pvData.pv.Field;
import org.epics.pvData.pv.MessageType;
import org.epics.pvData.pv.PVBoolean;
import org.epics.pvData.pv.PVByte;
import org.epics.pvData.pv.PVDataCreate;
import org.epics.pvData.pv.PVDouble;
import org.epics.pvData.pv.PVField;
import org.epics.pvData.pv.PVFloat;
import org.epics.pvData.pv.PVInt;
import org.epics.pvData.pv.PVLong;
import org.epics.pvData.pv.PVScalarArray;
import org.epics.pvData.pv.PVShort;
import org.epics.pvData.pv.PVString;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.PVStructureArray;
import org.epics.pvData.pv.Scalar;
import org.epics.pvData.pv.ScalarArray;
import org.epics.pvData.pv.ScalarType;
import org.epics.pvData.pv.SerializableControl;
import org.epics.pvData.pv.Structure;
import org.epics.pvData.pv.StructureArray;
import org.epics.pvData.pv.Type;

/**
 * Base class for a PVStructure.
 * @author mrk
 *
 */
public class BasePVStructure extends AbstractPVField implements PVStructure
{
    private static final PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
    private PVField[] pvFields;
    private String extendsStructureName = null;
    
    /**
     * Constructor.
     * @param parent The parent interface.
     * @param structure the reflection interface for the PVStructure data.
     */
    public BasePVStructure(PVStructure parent, Structure structure) {
        super(parent,structure);
    	Field[] fields = structure.getFields();
    	pvFields = new PVField[fields.length];
    	for(int i=0; i < pvFields.length; i++) {
    		Field field = fields[i];
    		switch(field.getType()) {
    		case scalar: {
    			Scalar scalar = (Scalar)field;
    			pvFields[i] = pvDataCreate.createPVScalar(this,scalar.getScalarType());
    			break;
    		}
    		case scalarArray: {
    			ScalarArray array = (ScalarArray)field;
    			ScalarType elementType = array.getElementType();
    			pvFields[i] = pvDataCreate.createPVScalarArray(this,elementType);
    			break;
    		}
    		case structure: {
    			Structure struct = (Structure)field;
    			pvFields[i] = pvDataCreate.createPVStructure(this,struct);
    			break;
    		}
    		case structureArray: {
    			StructureArray structArray = (StructureArray)field;
    			pvFields[i] = pvDataCreate.createPVStructureArray(this, structArray);
    			break;
    		}
    		}
    	}
    }
    /**
     * Constructor.
     * @param parent The parent interface.
     * @param structure the reflection interface for the PVStructure data.
     * @param pvFields The PVField array for the subfields.
     */
    public BasePVStructure(PVStructure parent, Structure structure,PVField[] pvFields)
    {
        super(parent,structure);
        this.pvFields = pvFields;
        for(int i=0; i<pvFields.length; i++) {
            AbstractPVField pvField = (AbstractPVField)(pvFields[i]);
            setParent(pvField,this);
        }
    }
    
    static private void setParent(AbstractPVField pvField,PVStructure parent)
    {
        pvField.setParent(parent);
        if(pvField.getField().getType()==Type.structure) {
            PVStructure subStructure = (PVStructure)pvField;
            PVField[] subFields = subStructure.getPVFields();
            for(int i=0; i<subFields.length; i++) {
                AbstractPVField subField = (AbstractPVField)(subFields[i]);
                setParent(subField,subStructure);
            }
        }
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVStructure#getSubField(java.lang.String)
     */
    @Override
    public PVField getSubField(String fieldName) {
        return findSubField(fieldName,this);
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVStructure#getSubField(int)
     */
    @Override
    public PVField getSubField(int fieldOffset) {
        if(fieldOffset<=getFieldOffset()) {
            if(fieldOffset==getFieldOffset()) return this;
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
     * @see org.epics.pvData.pv.PVStructure#getStructure()
     */
    @Override
    public Structure getStructure() {
        return (Structure)getField();
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVStructure#appendPVField(java.lang.String, org.epics.pvData.pv.PVField)
     */
    @Override
    public void appendPVField(String fieldName,PVField pvField) {
        int origLength = pvFields.length;
        PVField[] newPVFields = new PVField[origLength + 1];
        String[] newFieldNames = new String[origLength + 1];
        for(int i=0; i<origLength; i++) {
            newPVFields[i] = pvFields[i];
            newFieldNames[i] = this.getStructure().getFieldName(i);
        }
        newPVFields[newPVFields.length-1] = pvField;
        newFieldNames[newPVFields.length-1] = fieldName;
        pvFields = newPVFields;
        if(pvField.getParent()!=this) {
            throw new IllegalStateException("PVStructure.appendField: illegal parent");
        }
        super.replaceStructure(newFieldNames,this);
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVStructure#appendPVFields(java.lang.String[], org.epics.pvData.pv.PVField[])
     */
    @Override
	public void appendPVFields(String[] fieldNames,PVField[] pvFields) {
        String[] newFieldNames = null;
		if(this.pvFields.length==0) {
			this.pvFields = pvFields;
			newFieldNames = fieldNames;
		} else {
			int original = this.pvFields.length;
			int additional = pvFields.length;
			int length = original + additional;
			PVField[] newPVFields = new PVField[length];
			newFieldNames = new String[length];
	        for(int i=0; i<original; i++) {
	            newPVFields[i] = this.pvFields[i];
	            newFieldNames[i] = this.getStructure().getFieldName(i);
	        }
	        for(int i=0; i<additional; i++) {
	        	newPVFields[original +i] = pvFields[i];
	        	if(pvFields[i].getParent()!=this) {
	        	    throw new IllegalStateException("PVStructure.appendFields field " + i + " has illegal parent");
	        	}
 	        	newFieldNames[original +i] = fieldNames[i];
	        }
		}
		super.replaceStructure(newFieldNames,this);
	}
	/* (non-Javadoc)
     * @see org.epics.pvData.pv.PVStructure#removePVField(java.lang.String)
     */
    @Override
    public void removePVField(String fieldName) {
        PVField pvField = getSubField(fieldName);
        if(pvField==null) {
            super.message("removePVField " + fieldName + " does not exist", MessageType.error);
            return;
        }
        int origLength = pvFields.length;
        PVField[] newPVFields = new PVField[origLength - 1];
        String[] newFieldNames = new String[origLength - 1];
        int newIndex = 0;
        for(int i=0; i<origLength; i++) {
            if(pvFields[i]==pvField) continue;
            newFieldNames[newIndex++] = this.getStructure().getFieldName(i);
            newPVFields[newIndex++] = pvFields[i];
        }
        pvFields = newPVFields;
        replaceStructure(newFieldNames,this);
    }
	/* (non-Javadoc)
     * @see org.epics.pvData.pv.PVStructure#getPVFields()
     */
    @Override
    public PVField[] getPVFields() {
        return pvFields;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVStructure#getBooleanField(java.lang.String)
     */
    @Override
    public PVBoolean getBooleanField(String fieldName) {
        PVField pvField = findSubField(fieldName,this);
        if(pvField==null) {
        	super.message("fieldName " + fieldName + " does not exist",MessageType.error);
        	return null;
        }
        if(pvField.getField().getType()==Type.scalar) {
            Scalar scalar = (Scalar)pvField.getField();
            if(scalar.getScalarType()==ScalarType.pvBoolean) {
                return (PVBoolean)pvField;
            }
        }
        super.message("fieldName " + fieldName + " does not have type boolean ",
                MessageType.error);
        return null;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVStructure#getByteField(java.lang.String)
     */
    @Override
    public PVByte getByteField(String fieldName) {
        PVField pvField = findSubField(fieldName,this);
        if(pvField==null) {
        	super.message("fieldName " + fieldName + " does not exist",MessageType.error);
        	return null;
        }
        if(pvField.getField().getType()==Type.scalar) {
            Scalar scalar = (Scalar)pvField.getField();
            if(scalar.getScalarType()==ScalarType.pvByte) {
                return (PVByte)pvField;
            }
        }
        super.message("fieldName " + fieldName + " does not have type byte ",
                MessageType.error);
        return null;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVStructure#getShortField(java.lang.String)
     */
    @Override
    public PVShort getShortField(String fieldName) {
        PVField pvField = findSubField(fieldName,this);
        if(pvField==null) {
        	super.message("fieldName " + fieldName + " does not exist",MessageType.error);
        	return null;
        }
        if(pvField.getField().getType()==Type.scalar) {
            Scalar scalar = (Scalar)pvField.getField();
            if(scalar.getScalarType()==ScalarType.pvShort) {
                return (PVShort)pvField;
            }
        }
        super.message("fieldName " + fieldName + " does not have type short ",
                MessageType.error);
        return null;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVStructure#getIntField(java.lang.String)
     */
    @Override
    public PVInt getIntField(String fieldName) {
        PVField pvField = findSubField(fieldName,this);
        if(pvField==null) {
        	super.message("fieldName " + fieldName + " does not exist",MessageType.error);
        	return null;
        }
        if(pvField.getField().getType()==Type.scalar) {
            Scalar scalar = (Scalar)pvField.getField();
            if(scalar.getScalarType()==ScalarType.pvInt) {
                return (PVInt)pvField;
            }
        }
        super.message("fieldName " + fieldName + " does not have type int ",
                MessageType.error);
        return null;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVStructure#getLongField(java.lang.String)
     */
    @Override
    public PVLong getLongField(String fieldName) {
        PVField pvField = findSubField(fieldName,this);
        if(pvField==null) {
        	super.message("fieldName " + fieldName + " does not exist",MessageType.error);
        	return null;
        }
        if(pvField.getField().getType()==Type.scalar) {
            Scalar scalar = (Scalar)pvField.getField();
            if(scalar.getScalarType()==ScalarType.pvLong) {
                return (PVLong)pvField;
            }
        }
        super.message("fieldName " + fieldName + " does not have type long ",
                MessageType.error);
        return null;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVStructure#getFloatField(java.lang.String)
     */
    @Override
    public PVFloat getFloatField(String fieldName) {
        PVField pvField = findSubField(fieldName,this);
        if(pvField==null) {
        	super.message("fieldName " + fieldName + " does not exist",MessageType.error);
        	return null;
        }
        if(pvField.getField().getType()==Type.scalar) {
            Scalar scalar = (Scalar)pvField.getField();
            if(scalar.getScalarType()==ScalarType.pvFloat) {
                return (PVFloat)pvField;
            }
        }
        super.message("fieldName " + fieldName + " does not have type float ",
                MessageType.error);
        return null;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVStructure#getDoubleField(java.lang.String)
     */
    @Override
    public PVDouble getDoubleField(String fieldName) {
        PVField pvField = findSubField(fieldName,this);
        if(pvField==null) {
        	super.message("fieldName " + fieldName + " does not exist",MessageType.error);
        	return null;
        }
        if(pvField.getField().getType()==Type.scalar) {
            Scalar scalar = (Scalar)pvField.getField();
            if(scalar.getScalarType()==ScalarType.pvDouble) {
                return (PVDouble)pvField;
            }
        }
        super.message("fieldName " + fieldName + " does not have type double ",
                MessageType.error);
        return null;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVStructure#getStringField(java.lang.String)
     */
    @Override
    public PVString getStringField(String fieldName) {
        PVField pvField = findSubField(fieldName,this);
        if(pvField==null) {
            super.message("fieldName " + fieldName + " does not exist ",
                    MessageType.error);
            return null;
        }
        if(pvField.getField().getType()==Type.scalar) {
            Scalar scalar = (Scalar)pvField.getField();
            if(scalar.getScalarType()==ScalarType.pvString) {
                return (PVString)pvField;
            }
        }
        super.message("fieldName " + fieldName + " does not have type string ",
                MessageType.error);
        return null;
    }
	/* (non-Javadoc)
     * @see org.epics.pvData.pv.PVStructure#getStructureField(java.lang.String)
     */
    @Override
    public PVStructure getStructureField(String fieldName) {
        PVField pvField = findSubField(fieldName,this);
        if(pvField==null) {
        	super.message("fieldName " + fieldName + " does not exist",MessageType.error);
        	return null;
        }
        Field field = pvField.getField();
        Type type = field.getType();
        if(type!=Type.structure) {
            super.message(
                "fieldName " + fieldName + " does not have type structure ",
                MessageType.error);
            return null;
        }
        return (PVStructure)pvField;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVStructure#getArrayField(java.lang.String, org.epics.pvData.pv.ScalarType)
     */
    @Override
    public PVScalarArray getScalarArrayField(String fieldName, ScalarType elementType) {
        PVField pvField = findSubField(fieldName,this);
        if(pvField==null) {
        	super.message("fieldName " + fieldName + " does not exist",MessageType.error);
        	return null;
        }
        Field field = pvField.getField();
        Type type = field.getType();
        if(type!=Type.scalarArray) {
            super.message(
                "fieldName " + fieldName + " does not have type array ",
                MessageType.error);
            return null;
        }
        ScalarArray array = (ScalarArray)field;
        if(array.getElementType()!=elementType) {
            super.message(
                    "fieldName "
                    + fieldName + " is array but does not have elementType " + elementType.toString(),
                    MessageType.error);
                return null;
        }
        return (PVScalarArray)pvField;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVStructure#getStructureArrayField(java.lang.String)
     */
    @Override
	public PVStructureArray getStructureArrayField(String fieldName) {
    	PVField pvField = findSubField(fieldName,this);
        if(pvField==null) {
        	super.message("fieldName " + fieldName + " does not exist",MessageType.error);
        	return null;
        }
        Field field = pvField.getField();
        Type type = field.getType();
        if(type!=Type.structureArray) {
            super.message(
                "fieldName " + fieldName + " does not have type array ",
                MessageType.error);
            return null;
        }
        return (PVStructureArray)pvField;
	}

	/* (non-Javadoc)
     * @see org.epics.pvData.pv.PVStructure#getExtendStructure()
     */
    public String getExtendsStructureName() {
        return extendsStructureName;
    }
    @Override
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVStructure#putExtendStructure(org.epics.pvData.pv.PVStructure)
     */
    public boolean putExtendsStructureName(String extendsStructureName) {
        if(extendsStructureName==null || this.extendsStructureName!=null) return false;
        this.extendsStructureName = extendsStructureName;
        return true;
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
	 * @see org.epics.pvData.pv.Serializable#serialize(java.nio.ByteBuffer, org.epics.pvData.pv.SerializableControl)
	 */
	public void serialize(ByteBuffer buffer, SerializableControl flusher) {
        for (int i = 0; i < pvFields.length; i++)
        	pvFields[i].serialize(buffer, flusher);
	}
	/* (non-Javadoc)
	 * @see org.epics.pvData.pv.Serializable#deserialize(java.nio.ByteBuffer, org.epics.pvData.pv.DeserializableControl)
	 */
	public void deserialize(ByteBuffer buffer, DeserializableControl control) {
        for (int i = 0; i < pvFields.length; i++)
        	pvFields[i].deserialize(buffer, control);
	}
	/* (non-Javadoc)
	 * @see org.epics.pvData.pvCopy.BitSetSerializable#deserialize(java.nio.ByteBuffer, org.epics.pvData.pv.DeserializableControl, org.epics.pvData.misc.BitSet)
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
        
        for (int i = 0; i < pvFields.length; i++)
        {
        	final PVField pvField = pvFields[i];
            offset = pvField.getFieldOffset();
            numberFields = pvField.getNumberFields();
            next = bitSet.nextSetBit(offset);
            // no more changes
            if (next<0) return;
            //  no change in this pvField
            if (next>=offset+numberFields) continue;
            
            // serialize field or fields
            if (numberFields == 1)
            	pvField.deserialize(buffer, control);
            else
            	((PVStructure)pvField).deserialize(buffer, control, bitSet);
        }
	}
	/* (non-Javadoc)
	 * @see org.epics.pvData.pvCopy.BitSetSerializable#serialize(java.nio.ByteBuffer, org.epics.pvData.pv.SerializableControl, org.epics.pvData.misc.BitSet)
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
        
        for (int i = 0; i < pvFields.length; i++)
        {
        	final PVField pvField = pvFields[i];
            offset = pvField.getFieldOffset();
            numberFields = pvField.getNumberFields();
            next = bitSet.nextSetBit(offset);
            // no more changes
            if (next<0) return;
            //  no change in this pvField
            if (next>=offset+numberFields) continue;
            
            // serialize field or fields
            if (numberFields == 1)
            	pvField.serialize(buffer, flusher);
            else
            	((PVStructure)pvField).serialize(buffer, flusher, bitSet);
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
		result = prime * result
				+ ((extendsStructureName == null) ? 0 : extendsStructureName.hashCode());
		result = prime * result + Arrays.hashCode(pvFields);
		return result;
	}
}
