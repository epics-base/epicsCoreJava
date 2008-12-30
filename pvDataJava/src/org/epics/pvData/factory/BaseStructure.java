/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.factory;

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
}
