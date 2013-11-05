/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata.factory;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.epics.pvdata.misc.BitSet;
import org.epics.pvdata.pv.DeserializableControl;
import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.MessageType;
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
import org.epics.pvdata.pv.Scalar;
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
    private static final FieldCreate fieldCreate = FieldFactory.getFieldCreate();
    private PVField[] pvFields;
    private String extendsStructureName = null;
    
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
    	    if(fields[i].getType()==Type.structure) {
    	        pvFields[i] = new BasePVStructure((Structure)fields[i]);
    	    } else {
    		    pvFields[i] = pvDataCreate.createPVField(fields[i]);
    	    }
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
    @Override
    public void setImmutable() {
        super.setImmutable();
        for(int i=0; i < pvFields.length; i++) {
            pvFields[i].setImmutable();
        }
        super.setImmutable();
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVStructure#getSubField(java.lang.String)
     */
    @Override
    public PVField getSubField(String fieldName) {
        return findSubField(fieldName,this);
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVStructure#getSubField(int)
     */
    @Override
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
    @Override
    public Structure getStructure() {
        return (Structure)getField();
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVStructure#appendPVField(java.lang.String, org.epics.pvdata.pv.PVField)
     */
    @Override
    public void appendPVField(String fieldName,PVField pvField) {
        int origLength = pvFields.length;
        String[] origNames = getStructure().getFieldNames();
        Field[] origFields = getStructure().getFields();
        String origID = getStructure().getID();
        Structure structure = fieldCreate.createStructure(origID, origNames, origFields);
        structure = fieldCreate.appendField(structure, fieldName,pvField.getField());
        int newLength = origLength + 1;
        PVField[] newPVFields = new PVField[newLength];
        for(int i=0; i<origLength; i++) {
            newPVFields[i] = pvFields[i];
        }
        newPVFields[origLength] = pvField;
        pvFields = newPVFields;
        PVStructure pvParent = getParent();
        if(pvParent!=null) {
            Structure parentStructure = pvParent.getStructure();
            PVField[] parentPVFields  = pvParent.getPVFields();
            int index = -1;
            for(int i=0;i<parentPVFields.length; i++) {
                if(parentPVFields[i]==this) {
                    index = i;
                }
            }
            if(index==-1) {
                throw new IllegalStateException("PVStructure Logic error");
            }
            parentStructure.getFields()[index] = structure;
        }
        super.changeField(structure);
        setParentAndName();
    }
    @Override
	public void appendPVFields(String[] fieldNames,PVField[] extraPVFields)
    {
        int origLength = pvFields.length;
        String[] origNames = getStructure().getFieldNames();
        Field[] origFields = getStructure().getFields();
        String origID = getStructure().getID();
        int extra = fieldNames.length;
        if(extra==0) return;
        Field[] extraFields = new Field[extra];
        for(int i=0; i<extra; i++) {
            extraFields[i] = extraPVFields[i].getField();
        }
        Structure structure = fieldCreate.createStructure(origID, origNames, origFields);
        structure = fieldCreate.appendFields(structure, fieldNames, extraFields);
        int newLength = origLength + extra;
        PVField[] newPVFields = new PVField[newLength];
        for(int i=0; i<origLength; i++) {
            newPVFields[i] = pvFields[i];
        }
        for(int i=0; i<extra; i++) {
            newPVFields[origLength + i] = extraPVFields[i];
        }
        pvFields = newPVFields;
        PVStructure pvParent = getParent();
        if(pvParent!=null) {
            Structure parentStructure = pvParent.getStructure();
            PVField[] parentPVFields  = pvParent.getPVFields();
            int index = -1;
            for(int i=0;i<parentPVFields.length; i++) {
                if(parentPVFields[i]==this) {
                    index = i;
                }
            }
            if(index==-1) {
                throw new IllegalStateException("PVStructure Logic error");
            }
            parentStructure.getFields()[index] = structure;
        }
        super.changeField(structure);
        setParentAndName();
    }
	/* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVStructure#removePVField(java.lang.String)
     */
    @Override
    public void removePVField(String fieldName) {
        PVField pvField = getSubField(fieldName);
        if(pvField==null) {
            super.message("removePVField " + fieldName + " does not exist", MessageType.error);
            return;
        }
        int origLength = pvFields.length;
        int newLength = origLength - 1;
        PVField[] newPVFields = new PVField[newLength];
        String[] newNames = new String[newLength];
        Field[] newFields = new Field[newLength];
        int newIndex = 0;
        for(int i=0; i<origLength; i++) {
            if(pvFields[i]==pvField) continue;
            newNames[newIndex] = getStructure().getFieldName(i);
            newFields[newIndex] = getStructure().getField(i);
            newPVFields[newIndex++] = pvFields[i];
        }
        pvFields = newPVFields;
        Structure structure = fieldCreate.createStructure(getStructure().getID(),newNames, newFields);
        PVStructure pvParent = getParent();
        if(pvParent!=null) {
            Structure parentStructure = pvParent.getStructure();
            PVField[] parentPVFields  = pvParent.getPVFields();
            int index = -1;
            for(int i=0;i<parentPVFields.length; i++) {
                if(parentPVFields[i]==this) {
                    index = i;
                }
            }
            if(index==-1) {
                throw new IllegalStateException("PVStructure Logic error");
            }
            parentStructure.getFields()[index] = structure;
        }
        super.changeField(structure);
        setParentAndName();
    }
	@Override
    public void replacePVField(PVField oldPVField, PVField newPVField) {
        int length = pvFields.length;
        Field[] oldFields = getStructure().getFields();
        for(int i=0; i<length; i++) {
            if(pvFields[i]==oldPVField) {
                pvFields[i] = newPVField;
                oldFields[i] = newPVField.getField();
            }
        }
        setParentAndName();
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVStructure#getPVFields()
     */
    @Override
    public PVField[] getPVFields() {
        return pvFields;
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVStructure#getBooleanField(java.lang.String)
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
     * @see org.epics.pvdata.pv.PVStructure#getByteField(java.lang.String)
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
     * @see org.epics.pvdata.pv.PVStructure#getShortField(java.lang.String)
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
     * @see org.epics.pvdata.pv.PVStructure#getIntField(java.lang.String)
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
    
	public <T> T getSubField(Class<T> c, String fieldName)
	{
		PVField pv = findSubField(fieldName, this);
		if (c.isInstance(pv))
			return c.cast(pv);
		else
			return null;
	}
	
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVStructure#getLongField(java.lang.String)
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
     * @see org.epics.pvdata.pv.PVStructure#getFloatField(java.lang.String)
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
     * @see org.epics.pvdata.pv.PVStructure#getDoubleField(java.lang.String)
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
     * @see org.epics.pvdata.pv.PVStructure#getStringField(java.lang.String)
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
     * @see org.epics.pvdata.pv.PVStructure#getStructureField(java.lang.String)
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
     * @see org.epics.pvdata.pv.PVStructure#getUnionField(java.lang.String)
     */
    @Override
    public PVUnion getUnionField(String fieldName) {
        PVField pvField = findSubField(fieldName,this);
        if(pvField==null) {
        	super.message("fieldName " + fieldName + " does not exist",MessageType.error);
        	return null;
        }
        Field field = pvField.getField();
        Type type = field.getType();
        if(type!=Type.union) {
            super.message(
                "fieldName " + fieldName + " does not have type union ",
                MessageType.error);
            return null;
        }
        return (PVUnion)pvField;
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVStructure#getArrayField(java.lang.String, org.epics.pvdata.pv.ScalarType)
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
     * @see org.epics.pvdata.pv.PVStructure#getStructureArrayField(java.lang.String)
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
                "fieldName " + fieldName + " does not have type structureArray ",
                MessageType.error);
            return null;
        }
        return (PVStructureArray)pvField;
	}
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVStructure#getUnionArrayField(java.lang.String)
     */
    @Override
	public PVUnionArray getUnionArrayField(String fieldName) {
    	PVField pvField = findSubField(fieldName,this);
        if(pvField==null) {
        	super.message("fieldName " + fieldName + " does not exist",MessageType.error);
        	return null;
        }
        Field field = pvField.getField();
        Type type = field.getType();
        if(type!=Type.unionArray) {
            super.message(
                "fieldName " + fieldName + " does not have type unionArray ",
                MessageType.error);
            return null;
        }
        return (PVUnionArray)pvField;
	}

	/* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVStructure#getExtendStructure()
     */
    public String getExtendsStructureName() {
        return extendsStructureName;
    }
    @Override
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVStructure#putExtendStructure(org.epics.pvdata.pv.PVStructure)
     */
    public boolean putExtendsStructureName(String extendsStructureName) {
        if(extendsStructureName==null || this.extendsStructureName!=null) return false;
        this.extendsStructureName = extendsStructureName;
        return true;
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
    
    @Override
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
        for (int i = 0; i < pvFields.length; i++)
        	pvFields[i].serialize(buffer, flusher);
	}
	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.Serializable#deserialize(java.nio.ByteBuffer, org.epics.pvdata.pv.DeserializableControl)
	 */
	public void deserialize(ByteBuffer buffer, DeserializableControl control) {
        for (int i = 0; i < pvFields.length; i++)
        	pvFields[i].deserialize(buffer, control);
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
