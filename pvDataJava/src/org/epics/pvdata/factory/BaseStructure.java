/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata.factory;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.epics.pvdata.misc.SerializeHelper;
import org.epics.pvdata.pv.Convert;
import org.epics.pvdata.pv.DeserializableControl;
import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.SerializableControl;
import org.epics.pvdata.pv.Structure;
import org.epics.pvdata.pv.Type;

/**
 * Base interface for a Structure.
 * It is also a complete implementation.
 * @author mrk
 *
 */
public class BaseStructure extends BaseField implements Structure {
    private static Convert convert = ConvertFactory.getConvert();
    private final Field[] fields;
    private final String[] fieldNames;
    
    /**
     * Constructor for a structure field.
     * @param fieldNames The field names for the subfields
     * @param fields The array of nodes definitions for the nodes of the structure.
     * @throws IllegalArgumentException if structureName is null;
     */
    public BaseStructure(String[] fieldNames,Field[] fields)
    {
    	super(Type.structure);
    	if(fieldNames.length != fields.length) {
    		throw new IllegalArgumentException("fieldNames has different length than fields");
    	}
    	this.fields = fields;
    	this.fieldNames = fieldNames;
    	for(int i=0; i<fields.length; i++) {
    		String fieldName = fieldNames[i];
    		for(int j=i+1; j<fields.length; j++) {
    			if(fieldName.equals(fieldNames[j])) {
    				throw new IllegalArgumentException(
    						"fieldName " + fieldName
    						+ " appears more than once");
    			}
    		}
    	}
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.Structure#getField(java.lang.String)
     */
	@Override
    public Field getField(String name) {
		for(int i=0; i<fields.length; i++) {
			if(name.equals(fieldNames[i])) {
				return fields[i];
			}
		}
        return null;
    }
	/* (non-Javadoc)
     * @see org.epics.pvdata.pv.Structure#getFieldIndex(java.lang.String)
     */
	@Override
    public int getFieldIndex(String name) {
		for(int i=0; i<fields.length; i++) {
			if(name.equals(fieldNames[i])) {
				return i;
			}
		}
        return -1;
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.Structure#getField(int)
     */
    @Override
    public Field getField(int fieldIndex) {
	    return fields[fieldIndex];
    }
	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.Structure#getFieldNames()
	 */
	@Override
    public String[] getFieldNames() {
	    return fieldNames;
    }
	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.Structure#getFieldName(int)
	 */
	@Override
    public String getFieldName(int fieldIndex) {
	    return fieldNames[fieldIndex];
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.Structure#getFields()
     */
    @Override
    public Field[] getFields() {
        return fields;
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.factory.BaseField#toString(java.lang.StringBuilder, int)
     */
    @Override
    public void toString(StringBuilder buf, int indentLevel) {
        buf.append("structure");
        toStringCommon(buf,indentLevel+1);
    }
    private void toStringCommon(StringBuilder buf, int indentLevel) {
    	convert.newLine(buf,indentLevel);
        int length = fields.length;
        for(int i=0; i<length; i++) {
        	Field field = fields[i];
        	Type type = field.getType();
        	switch(type) {
        	case scalar:
        	case scalarArray:
        		field.toString(buf, indentLevel);
                buf.append(" " + fieldNames[i]);
                break;
        	case structure:
        		BaseStructure struct = (BaseStructure)field;
        		buf.append("structure " + fieldNames[i]);
        		struct.toStringCommon(buf, indentLevel + 1);
        		break;
        	case structureArray:
        		buf.append("structure[] " + fieldNames[i]);
        		convert.newLine(buf,indentLevel+1);
        		field.toString(buf, indentLevel+1);
        		break;
        	}
        	if(i<length-1) convert.newLine(buf,indentLevel);
        }
    }
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int PRIME = 31;
		return PRIME * Arrays.hashCode(fieldNames) + Arrays.hashCode(fields);
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		final BaseStructure other = (BaseStructure) obj;
		if (!Arrays.equals(fieldNames, other.fieldNames))
			return false;
		if (!Arrays.equals(fields, other.fields))
			return false;
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.Serializable#serialize(java.nio.ByteBuffer, org.epics.pvdata.pv.SerializableControl)
	 */
	@Override
	public void serialize(ByteBuffer buffer, SerializableControl control) {
		control.ensureBuffer(1);
		buffer.put((byte)0x80);
		serializeStructureField(this, buffer, control);
	}

	static void serializeStructureField(final Structure structure, ByteBuffer buffer, 
			SerializableControl control) {
		final Field[] fields = structure.getFields();
		final String[] fieldNames = structure.getFieldNames();
		SerializeHelper.writeSize(fields.length, buffer, control);
		for (int i = 0; i < fields.length; i++)
		{
			SerializeHelper.serializeString(fieldNames[i], buffer, control);
			control.cachedSerialize(fields[i], buffer);
		}
	}
	
	static final Structure deserializeStructureField(ByteBuffer buffer, DeserializableControl control) {
		final int size = SerializeHelper.readSize(buffer, control);
		final Field[] fields = new Field[size];
		final String[] fieldNames = new String[size];
		for (int i = 0; i < size; i++)
		{
			fieldNames[i] = SerializeHelper.deserializeString(buffer, control);
			fields[i] = control.cachedDeserialize(buffer);
		}
		return new BaseStructure(fieldNames, fields);
	}

	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.Serializable#deserialize(java.nio.ByteBuffer, org.epics.pvdata.pv.DeserializableControl)
	 */
	@Override
	public void deserialize(ByteBuffer buffer, DeserializableControl control) {
		// must be done via FieldCreate
		throw new RuntimeException("not valid operation, use FieldCreate.deserialize instead");
	}
	
}
