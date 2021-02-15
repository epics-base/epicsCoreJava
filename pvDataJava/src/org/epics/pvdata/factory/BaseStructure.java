/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.factory;

import org.epics.pvdata.misc.SerializeHelper;
import org.epics.pvdata.pv.*;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Base interface for a Structure.
 * It is also a complete implementation.
 *
 * @author mrk
 */
public class BaseStructure extends BaseField implements Structure {
    private static Convert convert = ConvertFactory.getConvert();
    private final String id;
    private Field[] fields;
    private String[] fieldNames;

    /**
     * Constructor for a structure field.
     *
     * @param fieldNames The field names for the subfields
     * @param fields     The array of nodes definitions for the nodes of the structure.
     */
    public BaseStructure(String[] fieldNames, Field[] fields) {
        this(DEFAULT_ID, fieldNames, fields);
    }

    /**
     * Constructor for a structure field.
     *
     * @param id         The identification string for the structure.
     * @param fieldNames The field names for the subfields
     * @param fields     The array of nodes definitions for the nodes of the structure.
     * @throws IllegalArgumentException if id is null or empty.
     */
    public BaseStructure(String id, String[] fieldNames, Field[] fields) {
        super(Type.structure);

        if (id == null)
            throw new IllegalArgumentException("id == null");

        if (id.trim().length() == 0)
            throw new IllegalArgumentException("id is empty");

        if (fieldNames.length != fields.length)
            throw new IllegalArgumentException("fieldNames has different length than fields");

        this.id = id;
        this.fields = fields;
        this.fieldNames = fieldNames;
        for (int i = 0; i < fields.length; i++) {
            String fieldName = fieldNames[i];
            if (fieldName == null) {
                throw new IllegalArgumentException(
                        "fieldName " + i
                                + " is null");
            }
            if (fieldName.length() < 1) {
                throw new IllegalArgumentException(
                        "fieldName " + i
                                + " has length 0");
            }
            for (int j = i + 1; j < fields.length; j++) {
                if (fieldName.equals(fieldNames[j])) {
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
    public String getID() {
        return id;
    }

    /**
     * Called by FieldFactory
     *
     * @param fields     new fields
     * @param fieldNames new names
     */
    void clone(Field[] fields, String[] fieldNames) {
        this.fields = fields;
        this.fieldNames = fieldNames;
        int n = fieldNames.length;
        for (int i = 0; i < n; i++) {
            if (fields[i].getType() == Type.structure) {
                BaseStructure sub = (BaseStructure) fields[i];
                String[] subNames = sub.getFieldNames();
                Field[] subFields = sub.getFields();
                int m = subNames.length;
                String[] newNames = new String[m];
                Field[] newFields = new Field[m];
                for (int j = 0; j < m; j++) {
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
    public Field getField(String name) {
        for (int i = 0; i < fields.length; i++) {
            if (name.equals(fieldNames[i])) {
                return fields[i];
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.Structure#getFieldIndex(java.lang.String)
     */
    public int getFieldIndex(String name) {
        for (int i = 0; i < fields.length; i++) {
            if (name.equals(fieldNames[i])) {
                return i;
            }
        }
        return -1;
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.Structure#getField(int)
     */
    public Field getField(int fieldIndex) {
        return fields[fieldIndex];
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.Structure#getField(java.lang.Class, java.lang.String)
     */
    public <T extends Field> T getField(Class<T> c, String fieldName) {
        Field pv = getField(fieldName);
        if (c.isInstance(pv))
            return c.cast(pv);
        else
            return null;
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.Structure#getField(java.lang.Class, int)
     */
    public <T extends Field> T getField(Class<T> c, int fieldOffset) {
        Field pv = getField(fieldOffset);
        if (c.isInstance(pv))
            return c.cast(pv);
        else
            return null;
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.Structure#getFieldNames()
     */
    public String[] getFieldNames() {
        return fieldNames;
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.Structure#getFieldName(int)
     */
    public String getFieldName(int fieldIndex) {
        return fieldNames[fieldIndex];
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.Structure#getFields()
     */
    public Field[] getFields() {
        return fields;
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.factory.BaseField#toString(java.lang.StringBuilder, int)
     */
    @Override
    public void toString(StringBuilder buf, int indentLevel) {
        buf.append(getID());
        toStringCommon(buf, indentLevel + 1);
    }

    void toStringCommon(StringBuilder buf, int indentLevel) {
        convert.newLine(buf, indentLevel);
        int length = fields.length;
        for (int i = 0; i < length; i++) {
            Field field = fields[i];
            buf.append(field.getID()).append(" ").append(fieldNames[i]);
            Type type = field.getType();
            switch (type) {
                case scalar:
                case scalarArray:
                    break;
                case structure:
                    BaseStructure struct = (BaseStructure) field;
                    struct.toStringCommon(buf, indentLevel + 1);
                    break;
                case structureArray:
                    convert.newLine(buf, indentLevel + 1);
                    Structure structureField = ((StructureArray) field).getStructure();
                    structureField.toString(buf, indentLevel + 1);
                    break;
                case union:
                    BaseUnion union = (BaseUnion) field;
                    union.toStringCommon(buf, indentLevel + 1);
                    break;
                case unionArray:
                    convert.newLine(buf, indentLevel + 1);
                    Union unionField = ((UnionArray) field).getUnion();
                    unionField.toString(buf, indentLevel + 1);
                    break;
            }
            if (i < length - 1) convert.newLine(buf, indentLevel);
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
        return Arrays.equals(fields, other.fields);
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.Serializable#serialize(java.nio.ByteBuffer, org.epics.pvdata.pv.SerializableControl)
     */
    public void serialize(ByteBuffer buffer, SerializableControl control) {
        control.ensureBuffer(1);
        buffer.put((byte) 0x80);
        serializeStructureField(this, buffer, control);
    }

    private static final String EMPTY_ID = "";

    /**
     * @noinspection StringEquality
     */
    static void serializeStructureField(final Structure structure, ByteBuffer buffer,
                                        SerializableControl control) {

        // to optimize default (non-empty) IDs optimization (yes, by ref. string comparison)
        // empty IDs are not allowed
        final String id = structure.getID();
        final String idToSerialize = (id == DEFAULT_ID) ? EMPTY_ID : id;
        SerializeHelper.serializeString(idToSerialize, buffer, control);

        final Field[] fields = structure.getFields();
        final String[] fieldNames = structure.getFieldNames();
        SerializeHelper.writeSize(fields.length, buffer, control);
        for (int i = 0; i < fields.length; i++) {
            SerializeHelper.serializeString(fieldNames[i], buffer, control);
            control.cachedSerialize(fields[i], buffer);
        }
    }

    static Structure deserializeStructureField(ByteBuffer buffer, DeserializableControl control) {
        final String id = SerializeHelper.deserializeString(buffer, control);
        final int size = SerializeHelper.readSize(buffer, control);
        final Field[] fields = new Field[size];
        final String[] fieldNames = new String[size];
        for (int i = 0; i < size; i++) {
            fieldNames[i] = SerializeHelper.deserializeString(buffer, control);
            fields[i] = control.cachedDeserialize(buffer);
        }

        if (id == null || id.trim().length() == 0)
            return new BaseStructure(fieldNames, fields);
        else
            return new BaseStructure(id, fieldNames, fields);
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.Serializable#deserialize(java.nio.ByteBuffer, org.epics.pvdata.pv.DeserializableControl)
     */
    public void deserialize(ByteBuffer buffer, DeserializableControl control) {
        // must be done via FieldCreate
        throw new RuntimeException("not valid operation, use FieldCreate.deserialize instead");
    }

}
