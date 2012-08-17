/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
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
    private final String id;
    private Field[] fields;
    private String[] fieldNames;
    private static final String DEFAULT_ID = "structure";
    /**
     * Constructor for a structure field.
     * @param fieldNames The field names for the subfields
     * @param fields The array of nodes definitions for the nodes of the structure.
     * @throws IllegalArgumentException if structureName is null;
     */
    public BaseStructure(String[] fieldNames,Field[] fields)
    {
    	this(DEFAULT_ID, fieldNames, fields);
    }
    
    /**
     * Constructor for a structure field.
     * @param id The identification string for the structure.
     * @param fieldNames The field names for the subfields
     * @param fields The array of nodes definitions for the nodes of the structure.
     * @throws IllegalArgumentException if structureName is null;
     */
    public BaseStructure(String id, String[] fieldNames,Field[] fields)
    {
    	super(Type.structure);
    	
    	if(id == null)
    		throw new IllegalArgumentException("id == null");
    	
    	if(fieldNames.length != fields.length)
    		throw new IllegalArgumentException("fieldNames has different length than fields");
    	
    	this.id = id;
    	this.fields = fields;
    	this.fieldNames = fieldNames;
    	for(int i=0; i<fields.length; i++) {
    		String fieldName = fieldNames[i];
    		if(fieldName==null) {
    		    throw new IllegalArgumentException(
                        "fieldName " + i
                        + " is null");
    		}
    		if(fieldName.length()<1) {
    		    throw new IllegalArgumentException(
                        "fieldName " + i
                        + " has length 0");
    		}
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
	 * @see org.epics.pvdata.pv.Field#getID()
	 */
	@Override
	public String getID() {
		return id;
	}
	/**
     * Called by FieldFactory
     * @param newFields new fields
     * @param newFieldNames new names
     */
    void clone(Field[] fields,String[] fieldNames) {
        this.fields = fields;
        this.fieldNames = fieldNames;
        int n = fieldNames.length;
        for(int i=0; i<n; i++) {
            if(fields[i].getType()==Type.structure) {
                BaseStructure sub = (BaseStructure)fields[i];
                String[] subNames = sub.getFieldNames();
                Field[] subFields = sub.getFields();
                int m = subNames.length;
                String[] newNames = new String[m];
                Field[] newFields = new Field[m];
                for(int j=0; j<m; j++) {
                    newNames[j] = subNames[j];
                    newFields[j] = subFields[j];
                }
                sub.clone(newFields, newNames);
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
        buf.append(getID());
        toStringCommon(buf,indentLevel+1);
    }
    private void toStringCommon(StringBuilder buf, int indentLevel) {
    	convert.newLine(buf,indentLevel);
        int length = fields.length;
        for(int i=0; i<length; i++) {
        	Field field = fields[i];
        	buf.append(field.getID() + " " + fieldNames[i]);
        	Type type = field.getType();
        	switch(type) {
        	case scalar:
        	case scalarArray:
                break;
        	case structure:
        		BaseStructure struct = (BaseStructure)field;
        		struct.toStringCommon(buf, indentLevel + 1);
        		break;
        	case structureArray:
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
		return id.hashCode() + PRIME *
			(PRIME * Arrays.hashCode(fieldNames) + Arrays.hashCode(fields));
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
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
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
		SerializeHelper.serializeString(structure.getID(), buffer, control);
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
		final String id = SerializeHelper.deserializeString(buffer, control);
		final int size = SerializeHelper.readSize(buffer, control);
		final Field[] fields = new Field[size];
		final String[] fieldNames = new String[size];
		for (int i = 0; i < size; i++)
		{
			fieldNames[i] = SerializeHelper.deserializeString(buffer, control);
			fields[i] = control.cachedDeserialize(buffer);
		}
		return new BaseStructure(id, fieldNames, fields);
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
