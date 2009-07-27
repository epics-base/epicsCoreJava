/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.factory;

import java.nio.ByteBuffer;

import org.epics.pvData.misc.BitSet;
import org.epics.pvData.pv.Array;
import org.epics.pvData.pv.Convert;
import org.epics.pvData.pv.Field;
import org.epics.pvData.pv.FieldCreate;
import org.epics.pvData.pv.MessageType;
import org.epics.pvData.pv.PVArray;
import org.epics.pvData.pv.PVBoolean;
import org.epics.pvData.pv.PVByte;
import org.epics.pvData.pv.PVDataCreate;
import org.epics.pvData.pv.PVDouble;
import org.epics.pvData.pv.PVField;
import org.epics.pvData.pv.PVFloat;
import org.epics.pvData.pv.PVInt;
import org.epics.pvData.pv.PVListener;
import org.epics.pvData.pv.PVLong;
import org.epics.pvData.pv.PVRecord;
import org.epics.pvData.pv.PVShort;
import org.epics.pvData.pv.PVString;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.Scalar;
import org.epics.pvData.pv.ScalarType;
import org.epics.pvData.pv.Structure;
import org.epics.pvData.pv.Type;

/**
 * Base class for a PVStructure.
 * @author mrk
 *
 */
public class BasePVStructure extends AbstractPVField implements PVStructure
{
    private static Convert convert = ConvertFactory.getConvert();
    private static PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
    private static FieldCreate fieldCreate = FieldFactory.getFieldCreate();
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
        		pvFields[i] = pvDataCreate.createPVScalar(this,field.getFieldName(),scalar.getScalarType());
        		break;
        	}
        	case scalarArray: {
        		Array array = (Array)field;
        		pvFields[i] = pvDataCreate.createPVArray(this,field.getFieldName(),array.getElementType());
        		break;
        	}
        	case structure: {
        		Structure struct = (Structure)field;
        		pvFields[i] = pvDataCreate.createPVStructure(this, field.getFieldName(), struct.getFields());
        	}
        	}
        }
        if(parent==null) computeOffset();
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.factory.AbstractPVField#removeEveryListener()
     */
    @Override
    protected void removeEveryListener() {
        super.removeEveryListener();
        for(PVField pvField :pvFields) {
            AbstractPVField abstractPVField = (AbstractPVField)pvField;
            abstractPVField.removeEveryListener();
        }
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.factory.AbstractPVField#removeListener(org.epics.pvData.pv.PVListener)
     */
    @Override
    public void removeListener(PVListener recordListener) {
        super.removeListener(recordListener);
        for(PVField pvField :pvFields) {
            AbstractPVField abstractPVField = (AbstractPVField)pvField;
            abstractPVField.removeListener(recordListener);
        }
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.factory.AbstractPVField#postPut()
     */
    @Override
    public void postPut() {
        super.postPut();
        for(PVField pvField : pvFields) {
            postPutNoParent((AbstractPVField)pvField);
        }
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVStructure#postPut(org.epics.pvData.pv.PVField)
     */
    @Override
    public void postPut(PVField subField) {
     // don't create iterator
        for(int index=0; index<pvListenerList.size(); index++) {
            PVListener pvListener = pvListenerList.get(index);
            if(pvListener!=null) pvListener.dataPut(this,subField);
        }
        PVStructure pvParent = super.getParent();
        if(pvParent!=null) pvParent.postPut(subField);
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
        if(fieldOffset<=super.getFieldOffset()) {
            if(fieldOffset==super.getFieldOffset()) return this;
            return null;
        }
        if(fieldOffset>super.getNextFieldOffset()) return null;
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
     * @see org.epics.pvData.factory.AbstractPVField#setRecord(org.epics.pvData.pv.PVRecord)
     */
    @Override
    public void setRecord(PVRecord record) {
        super.setRecord(record);
        for(PVField pvField : pvFields) {
            AbstractPVField abstractPVField = (AbstractPVField)pvField;
            abstractPVField.setRecord(record);
        }
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVStructure#replacePVField(java.lang.String, org.epics.pvData.pv.PVField)
     */
    @Override
    public void replacePVField(String fieldName, PVField newPVField) {
        Structure structure = (Structure)super.getField();
        Field[] origFields = structure.getFields();
        int index = -1;
        for(int i=0; i<origFields.length; i++) {
            Field origField = origFields[i];
            if(origField.getFieldName().equals(fieldName)) {
               index = i;
               break;
            } 
        }
        pvFields[index] = newPVField;
        if(origFields[index]!=newPVField.getField()) replaceStructure();
        computeOffset();
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
        if(pvField==null) return null;
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
        if(pvField==null) return null;
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
        if(pvField==null) return null;
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
        if(pvField==null) return null;
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
        if(pvField==null) return null;
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
        if(pvField==null) return null;
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
        if(pvField==null) return null;
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
        if(pvField==null) return null;
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
    public PVArray getArrayField(String fieldName, ScalarType elementType) {
        PVField pvField = findSubField(fieldName,this);
        if(pvField==null) return null;
        Field field = pvField.getField();
        Type type = field.getType();
        if(type!=Type.scalarArray) {
            super.message(
                "fieldName " + fieldName + " does not have type array ",
                MessageType.error);
            return null;
        }
        Array array = (Array)field;
        if(array.getElementType()!=elementType) {
            super.message(
                    "fieldName "
                    + fieldName + " is array but does not have elementType " + elementType.toString(),
                    MessageType.error);
                return null;
        }
        return (PVArray)pvField;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVStructure#appendPVField(org.epics.pvData.pv.PVField)
     */
    @Override
    public void appendPVField(PVField pvField) {
        int origLength = pvFields.length;
        PVField[] newPVFields = new PVField[origLength + 1];
        for(int i=0; i<origLength; i++) {
            newPVFields[i] = pvFields[i];
        }
        newPVFields[newPVFields.length-1] = pvField;
        pvFields = newPVFields;
        replaceStructure();
        computeOffset();
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
        int newIndex = 0;
        for(int i=0; i<origLength; i++) {
            if(pvFields[i]==pvField) continue;
            newPVFields[newIndex++] = pvFields[i];
        }
        pvFields = newPVFields;
        replaceStructure();
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
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String prefix = "structure " + super.getField().getFieldName();
        return toString(prefix,0);
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.factory.AbstractPVField#toString(int)
     */
    @Override
    public String toString(int indentLevel) {
        return toString("structure",indentLevel);
    }       
    /**
     * Called by BasePVRecord.
     * @param prefix A prefix for the generated stting.
     * @param indentLevel The indentation level.
     * @return String showing the PVStructure.
     */
    protected String toString(String prefix,int indentLevel) {
        return getString(prefix,indentLevel);
    }
    
    private void computeOffset() {
        PVStructure pvStructure = super.getParent();
        if(pvStructure!=null) {
            BasePVStructure base = (BasePVStructure)pvStructure;
            base.computeOffset();
            return;
        }
        int offset = 0;
        int nextOffset = 1;
        for(int i=0; i < pvFields.length; i++) {
            offset = nextOffset;
            PVField pvField = (AbstractPVField)pvFields[i];
            Field field = pvField.getField();
            switch(field.getType()) {
            case scalar:
            case scalarArray: {
                AbstractPVField pv = (AbstractPVField)pvField;
                nextOffset++;
                pv.setOffsets(offset, nextOffset);
                break;
            }
            case structure: {
                BasePVStructure pv = (BasePVStructure)pvField;
                pv.computeOffset(offset);
                nextOffset = pv.getNextFieldOffset();
            }
            }
        }
        super.setOffsets(0, nextOffset);
    }
    
    private void computeOffset(int offset) {
        int beginOffset = offset;
        int nextOffset = offset + 1;
        for(int i=0; i < pvFields.length; i++) {
            offset = nextOffset;
            PVField pvField = (AbstractPVField)pvFields[i];
            Field field = pvField.getField();
            switch(field.getType()) {
            case scalar:
            case scalarArray: {
                AbstractPVField pv = (AbstractPVField)pvField;
                nextOffset++;
                pv.setOffsets(offset, nextOffset);
                break;
            }
            case structure: {
                BasePVStructure pv = (BasePVStructure)pvField;
                pv.computeOffset(offset);
                nextOffset = pv.getNextFieldOffset();
            }
            }
        }
        super.setOffsets(beginOffset, nextOffset);
    }
    private void replaceStructure() {
        int length = pvFields.length;
        Field[] newFields = new Field[length];
        for(int i=0; i<length; i++) {
            newFields[i] = pvFields[i].getField();
        }
        Structure newStructure = fieldCreate.createStructure(super.getField().getFieldName(), newFields);
        super.replaceField(newStructure);
        PVStructure pvParent = super.getParent();
        if(pvParent!=null) {
            BasePVStructure basePVStructure = (BasePVStructure)pvParent;
            basePVStructure.replaceStructure();
        }
        
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
        PVField pvField = null;
        for(PVField pvf : pvFields) {
            if(pvf.getField().getFieldName().equals(name)) {
                pvField = pvf;
                break;
            }
        }
        if(pvField==null) return null;
        if(restOfName==null) return pvField;
        if(pvField.getField().getType()!=Type.structure) return null;
        return findSubField(restOfName,(PVStructure)pvField);
    }
    private String getString(String prefix,int indentLevel) {
        StringBuilder builder = new StringBuilder();
        builder.append(prefix);
        builder.append(super.toString(indentLevel));
        if(extendsStructureName!=null) {
            builder.append(" extends ");
            builder.append(extendsStructureName);
        }
        convert.newLine(builder,indentLevel);
        builder.append("{");
        for(int i=0, n= pvFields.length; i < n; i++) {
            convert.newLine(builder,indentLevel + 1);
            Field field = pvFields[i].getField();
            builder.append(field.getFieldName() + " = ");
            builder.append(pvFields[i].toString(indentLevel + 1));            
        }
        convert.newLine(builder,indentLevel);
        builder.append("}");
        return builder.toString();
    }
    
	/* (non-Javadoc)
	 * @see org.epics.pvData.pv.Serializable#serialize(java.nio.ByteBuffer)
	 */
	public void serialize(ByteBuffer buffer) {
        for (int i = 0; i < pvFields.length; i++)
        	pvFields[i].serialize(buffer);
	}
	/* (non-Javadoc)
	 * @see org.epics.pvData.pv.Serializable#deserialize(java.nio.ByteBuffer)
	 */
	public void deserialize(ByteBuffer buffer) {
        for (int i = 0; i < pvFields.length; i++)
        	pvFields[i].deserialize(buffer);
	}
	/* (non-Javadoc)
	 * @see org.epics.pvData.pvCopy.BitSetSerializable#deserialize(java.nio.ByteBuffer, org.epics.pvData.misc.BitSet)
	 */
	public void deserialize(ByteBuffer buffer, BitSet bitSet) {
        int offset = getFieldOffset();
        int numberFields = getNumberFields();
        int next = bitSet.nextSetBit(offset);
        
        // no more changes or no changes in this structure
        if (next<0 || next>=offset+numberFields) return;

        // entire structure
        if(offset==next)
        	deserialize(buffer);
        
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
            	pvField.deserialize(buffer);
            else
            	((PVStructure)pvField).deserialize(buffer, bitSet);
        }
	}
	/* (non-Javadoc)
	 * @see org.epics.pvData.pvCopy.BitSetSerializable#serialize(java.nio.ByteBuffer, org.epics.pvData.misc.BitSet)
	 */
	public void serialize(ByteBuffer buffer, BitSet bitSet) {
        int offset = getFieldOffset();
        int numberFields = getNumberFields();
        int next = bitSet.nextSetBit(offset);
        
        // no more changes or no changes in this structure
        if (next<0 || next>=offset+numberFields) return;

        // entire structure
        if(offset==next)
        	serialize(buffer);
        
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
            	pvField.serialize(buffer);
            else
            	((PVStructure)pvField).serialize(buffer, bitSet);
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
}
