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
 * Base class for implementing an union.
 * It is also a complete implementation.
 *
 * @author mse
 */
public class BaseUnion extends BaseField implements Union {
    private static Convert convert = ConvertFactory.getConvert();
    private final String id;
    private final Field[] fields;
    private String[] fieldNames;

    /**
     * Default unrestricted union (aka any type) ID.
     */
    private static final String[] ANY_FIELD_NAMES = new String[0];
    private static final Field[] ANY_FIELDS = new Field[0];

    /**
     * Constructor for a variant union (aka any type).
     */
    public BaseUnion() {
        this(ANY_ID, ANY_FIELD_NAMES, ANY_FIELDS);
    }

    /**
     * Constructor for an union field.
     *
     * @param fieldNames The field names for the subfields
     * @param fields     The array of nodes definitions for the nodes of the structure.
     */
    public BaseUnion(String[] fieldNames, Field[] fields) {
        this(fieldNames.length > 0 ? DEFAULT_ID : ANY_ID, fieldNames, fields);
    }

    /**
     * Constructor for an union field.
     *
     * @param id         The identification string for the union.
     * @param fieldNames The field names for the subfields
     * @param fields     The union fields (members).
     * @throws IllegalArgumentException if id is null or empty.
     */
    public BaseUnion(String id, String[] fieldNames, Field[] fields) {
        super(Type.union);

        if (id == null)
            throw new IllegalArgumentException("id == null");

        if (id.trim().length() == 0)
            throw new IllegalArgumentException("id is empty");

        if (fieldNames.length != fields.length)
            throw new IllegalArgumentException("fieldNames has different length than fields");

        if (fields.length == 0 && id != ANY_ID)
            throw new IllegalArgumentException("no fields but id is different than " + ANY_ID);

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

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.Union#getField(java.lang.String)
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
     * @see org.epics.pvdata.pv.Union#getFieldIndex(java.lang.String)
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
     * @see org.epics.pvdata.pv.Union#getField(int)
     */
    public Field getField(int fieldIndex) {
        return fields[fieldIndex];
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.Union#getField(java.lang.Class, java.lang.String)
     */
    public <T extends Field> T getField(Class<T> c, String fieldName) {
        Field pv = getField(fieldName);
        if (c.isInstance(pv))
            return c.cast(pv);
        else
            return null;
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.Union#getField(java.lang.Class, int)
     */
    public <T extends Field> T getField(Class<T> c, int fieldOffset) {
        Field pv = getField(fieldOffset);
        if (c.isInstance(pv))
            return c.cast(pv);
        else
            return null;
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.Union#getFieldNames()
     */
    public String[] getFieldNames() {
        return fieldNames;
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.Union#getFieldName(int)
     */
    public String getFieldName(int fieldIndex) {
        return fieldNames[fieldIndex];
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.Union#getFields()
     */
    public Field[] getFields() {
        return fields;
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.Union#isVariant()
     */
    public boolean isVariant() {
        return fields.length == 0;
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
        int length = fields.length;
        if (length == 0)    // variant support
            return;
        convert.newLine(buf, indentLevel);
        for (int i = 0; i < length; i++) {
            Field field = fields[i];
            buf.append(field.getID() + " " + fieldNames[i]);
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
                    field.toString(buf, indentLevel + 1);
                    break;
                case union:
                    BaseUnion union = (BaseUnion) field;
                    union.toStringCommon(buf, indentLevel + 1);
                    break;
                case unionArray:
                    convert.newLine(buf, indentLevel + 1);
                    field.toString(buf, indentLevel + 1);
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
        final int PRIME = 37;
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
        final BaseUnion other = (BaseUnion) obj;
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

    @Override
     */
    public void serialize(ByteBuffer buffer, SerializableControl control) {
        control.ensureBuffer(1);
        if (fields.length == 0) {
            // unrestricted/variant union
            buffer.put((byte) 0x82);
        } else {
            buffer.put((byte) 0x81);
            serializeUnionField(this, buffer, control);
        }
    }

    private static final String EMPTY_ID = "";

    static void serializeUnionField(final Union union, ByteBuffer buffer,
                                    SerializableControl control) {

        // to optimize default (non-empty) IDs optimization (yes, by ref. string comparison)
        // empty IDs are not allowed
        final String id = union.getID();
        final String idToSerialize = (id == DEFAULT_ID) ? EMPTY_ID : id;
        SerializeHelper.serializeString(idToSerialize, buffer, control);

        final Field[] fields = union.getFields();
        final String[] fieldNames = union.getFieldNames();
        SerializeHelper.writeSize(fields.length, buffer, control);
        for (int i = 0; i < fields.length; i++) {
            SerializeHelper.serializeString(fieldNames[i], buffer, control);
            control.cachedSerialize(fields[i], buffer);
        }
    }

    static final Union deserializeUnionField(ByteBuffer buffer, DeserializableControl control) {
        final String id = SerializeHelper.deserializeString(buffer, control);
        final int size = SerializeHelper.readSize(buffer, control);
        final Field[] fields = new Field[size];
        final String[] fieldNames = new String[size];
        for (int i = 0; i < size; i++) {
            fieldNames[i] = SerializeHelper.deserializeString(buffer, control);
            fields[i] = control.cachedDeserialize(buffer);
        }

        if (id == null || id.trim().length() == 0)
            return new BaseUnion(fieldNames, fields);
        else
            return new BaseUnion(id, fieldNames, fields);
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.Serializable#deserialize(java.nio.ByteBuffer, org.epics.pvdata.pv.DeserializableControl)
     */
    public void deserialize(ByteBuffer buffer, DeserializableControl control) {
        // must be done via FieldCreate
        throw new RuntimeException("not valid operation, use FieldCreate.deserialize instead");
    }

}
