/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.factory;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.epics.pvData.pv.Convert;
import org.epics.pvData.pv.Field;
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
    private String[] fieldNames;
    private List<String> sortedFieldNameList;
    private int[] fieldIndex;
    
    /**
     * Constructor for a structure field.
     * @param fieldName The field name.
     * @param field The array of fields definitions for the fields of the structure.
     * @throws IllegalArgumentException if structureName is null;
     */
    public BaseStructure(String fieldName,Field[] field)
    {
        super(fieldName, Type.structure);  
        initializeFields(field);
    }
	/**
	 * Initialize fields.
	 * @param field
	 * @throws IllegalArgumentException
	 */
	private void initializeFields(Field[] field) throws IllegalArgumentException {
		if(field==null) field = new Field[0];
        this.fields = field;
        fieldNames = new String[field.length];
        sortedFieldNameList = new ArrayList<String>(field.length);
        sortedFieldNameList.clear();
        for(int i = 0; i <field.length; i++) {
            fieldNames[i] = field[i].getFieldName();
            sortedFieldNameList.add(fieldNames[i]);
        }
        Collections.sort(sortedFieldNameList);
        // look for duplicates
        for(int i=0; i<field.length-1; i++) {
            if(sortedFieldNameList.get(i).equals(sortedFieldNameList.get(i+1))) {
                throw new IllegalArgumentException(
                        "fieldNames " + sortedFieldNameList.get(i)
                        + " appears more than once");
            }
        }
        fieldIndex = new int[field.length];
        for(int i=0; i<field.length; i++) {
            String value = sortedFieldNameList.get(i);
            for(int j=0; j<field.length; j++) {
                if(value.equals(this.fieldNames[j])) {
                    fieldIndex[i] = j;
                }
            }
        }
	}    
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.Structure#getField(java.lang.String)
     */
    public Field getField(String name) {
        int i = Collections.binarySearch(sortedFieldNameList,name);
        if(i>=0) {
            return fields[fieldIndex[i]];
        }
        return null;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.Structure#getFieldIndex(java.lang.String)
     */
    public int getFieldIndex(String name) {
        int i = Collections.binarySearch(sortedFieldNameList,name);
        if(i>=0) return fieldIndex[i];
        return -1;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.Structure#getFieldNames()
     */
    public String[] getFieldNames() {
        return fieldNames;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.Structure#getFields()
     */
    public Field[] getFields() {
        return fields;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.factory.BaseField#toString()
     */
    public String toString() { return getString(0);}
    /* (non-Javadoc)
     * @see org.epics.pvData.factory.BaseField#toString(int)
     */
    public String toString(int indentLevel) {
        return getString(indentLevel);
    }

    private String getString(int indentLevel) {
        StringBuilder builder = new StringBuilder();
        builder.append(super.toString(indentLevel));
        convert.newLine(builder,indentLevel);
        builder.append(String.format("structure  {"));
        for(int i=0, n= fields.length; i < n; i++) {
            builder.append(fields[i].toString(indentLevel + 1));
        }
        convert.newLine(builder,indentLevel);
        builder.append("}");
        return builder.toString();
    }
	/* (non-Javadoc)
	 * @see org.epics.pvData.pv.Serializable#getSerializationSize()
	 */
	public int getSerializationSize() {
		int size = AbstractPVArray.getStringSerializationSize(getFieldName());
		size += AbstractPVArray.getSerializedSizeSize(fields.length);
		size += fields.length;
		for (int i = 0; i < fields.length; i++)
			size += fields[i].getSerializationSize();
		return size;
	}
	/* (non-Javadoc)
	 * @see org.epics.pvData.pv.Serializable#serialize(java.nio.ByteBuffer)
	 */
	public void serialize(ByteBuffer buffer) {
		AbstractPVArray.serializeString(getFieldName(), buffer);
		AbstractPVArray.writeSize(fields.length, buffer);
		for (int i = 0; i < fields.length; i++)
		{
			buffer.put((byte)fields[i].getType().ordinal());
			fields[i].serialize(buffer);
		}
	}
	/* (non-Javadoc)
	 * @see org.epics.pvData.pv.Serializable#deserialize(java.nio.ByteBuffer)
	 */
	public void deserialize(ByteBuffer buffer) {
		fieldName = AbstractPVArray.deserializeString(buffer);
		final int size = AbstractPVArray.readSize(buffer);
		Field[] fields = null;
		if (size > 0)
		{
			fields = new Field[size];
			for (int i = 0; i < size; i++)
			{
				final Type type = Type.values()[buffer.get()];
				fields[i] = deserializeFromType(type, buffer);
			}
		}
		initializeFields(fields);
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof BaseStructure) {
			BaseStructure b = (BaseStructure)obj;

			final Field[] bfields = b.getFields(); 
			if (bfields.length == fields.length)
			{
		        for (int i = 0; i < fields.length; i++)
		        	if (!fields[i].equals(bfields[i]))
		        		return false;
		        
				if (getFieldName() == null)
					return b.getFieldName() == null;
				else
					return getFieldName().equals(b.getFieldName());
			}
			else
				return false;
		}
		else
			return false;
	}	
	/**
	 * Deserializes <code>Field</code> from buffer.
	 * @param type	type of <code>Field</code> to deserialize.
	 * @param buffer deserialization buffer.
	 * @return deserialized instance of an <code>Field</code>.
	 */
	// TODO where to put this
	public static Field deserializeFromType(Type type, ByteBuffer buffer)
	{
		Field field;
		switch (type)
		{
			case scalar: field = new BaseScalar(null, null); break;
			case scalarArray: field = new BaseArray(null, null); break;
			case structure: field = new BaseStructure(null, null); break;
			default: throw new UnsupportedOperationException("unknown Field type");
		}
		field.deserialize(buffer);
		return field;
	}
}
