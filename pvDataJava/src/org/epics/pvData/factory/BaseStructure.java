/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.factory;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.epics.pvData.misc.SerializeHelper;
import org.epics.pvData.pv.Convert;
import org.epics.pvData.pv.DeserializableControl;
import org.epics.pvData.pv.Field;
import org.epics.pvData.pv.SerializableControl;
import org.epics.pvData.pv.Structure;
import org.epics.pvData.pv.Type;

/**
 * Base interface for a Structure.
 * It is also a complete implementation.
 * @author mrk
 *
 */
public class BaseStructure extends BaseField implements Structure {
    private static Convert convert = ConvertFactory.getConvert();
    private Field[] fields;
    
    /**
     * Constructor for a structure field.
     * @param fieldName The field name.
     * @param fields The array of nodes definitions for the nodes of the structure.
     * @throws IllegalArgumentException if structureName is null;
     */
    public BaseStructure(String fieldName,Field[] fields)
    {
        super(fieldName, Type.structure);  
        initializeFields(fields);
    }
	/**
	 * Initialize nodes.
	 * @param field
	 * @throws IllegalArgumentException
	 */
	private void initializeFields(Field[] field) throws IllegalArgumentException {
		if(field==null) field = new Field[0];
        this.fields = field;
        for(int i=0; i<fields.length; i++) {
        	String fieldName = fields[i].getFieldName();
        	for(int j=i+1; j<fields.length; j++) {
        		if(fieldName.equals(fields[j].getFieldName())) {
        			throw new IllegalArgumentException(
                            "fieldName " + fieldName
                            + " appears more than once");
        		}
        	}
        }
	}    
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.Structure#getField(java.lang.String)
     */
	@Override
    public Field getField(String name) {
		for(int i=0; i<fields.length; i++) {
			if(name.equals(fields[i].getFieldName())) {
				return fields[i];
			}
		}
        return null;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.Structure#getFieldIndex(java.lang.String)
     */
	@Override
    public int getFieldIndex(String name) {
		for(int i=0; i<fields.length; i++) {
			if(name.equals(fields[i].getFieldName())) {
				return i;
			}
		}
        return -1;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.Structure#getFields()
     */
    @Override
    public Field[] getFields() {
        return fields;
    }
    
    @Override
    public void toString(StringBuilder buf, int indentLevel) {
        buf.append("structure ");
        super.toString(buf, indentLevel);
        convert.newLine(buf,indentLevel+1);
        int length = fields.length;
        for(int i=0; i<length; i++) {
            fields[i].toString(buf, indentLevel+1);
            if(i<length-1) convert.newLine(buf,indentLevel+1);

        }
    }
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = super.hashCode();
		result = PRIME * result + Arrays.hashCode(fields);
		return result;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		final BaseStructure other = (BaseStructure) obj;
		if (!Arrays.equals(fields, other.fields))
			return false;
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.epics.pvData.pv.Serializable#serialize(java.nio.ByteBuffer, org.epics.pvData.pv.SerializableControl)
	 */
	@Override
	public void serialize(ByteBuffer buffer, SerializableControl control) {
		control.ensureBuffer(1);
		buffer.put((byte)(Type.structure.ordinal() << 4));
		serializeStructureField(this, buffer, control);
	}

	static void serializeStructureField(final Structure structure, ByteBuffer buffer, 
			SerializableControl control) {
		SerializeHelper.serializeString(structure.getFieldName(), buffer, control);
		final Field[] fields = structure.getFields();
		SerializeHelper.writeSize(fields.length, buffer, control);
		for (int i = 0; i < fields.length; i++)
			control.cachedSerialize(fields[i], buffer);
	}
	
	static final Structure deserializeStructureField(ByteBuffer buffer, DeserializableControl control) {
		final String structureFieldName = SerializeHelper.deserializeString(buffer, control);
		final int size = SerializeHelper.readSize(buffer, control);
		Field[] fields = null;
		if (size > 0)
		{
			fields = new Field[size];
			for (int i = 0; i < size; i++)
				fields[i] = control.cachedDeserialize(buffer);
		}
		return new BaseStructure(structureFieldName, fields);
	}

	/* (non-Javadoc)
	 * @see org.epics.pvData.pv.Serializable#deserialize(java.nio.ByteBuffer, org.epics.pvData.pv.DeserializableControl)
	 */
	@Override
	public void deserialize(ByteBuffer buffer, DeserializableControl control) {
		// TODO Auto-generated method stub
		
	}
	
	
}
