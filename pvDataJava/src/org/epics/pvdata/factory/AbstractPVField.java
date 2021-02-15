/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.factory;


import org.epics.pvdata.pv.*;


/**
 * Abstract base class for a PVField.
 * A factory that implements PVField must extend this class.
 *
 * @author mrk
 */
public abstract class AbstractPVField implements PVField {
    private int fieldOffset = 0;
    private int nextFieldOffset = 0;
    private boolean isImmutable = false;
    private String fieldName = null;
    private Field field;
    private PVStructure pvParent = null;
    private PostHandler postHandler = null;

    /**
     * Convenience method for derived classes that perform conversions.
     */
    protected static final Convert convert = ConvertFactory.getConvert();

    /**
     * Constructor that must be called by derived classes.
     *
     * @param field The introspection interface.
     * @throws IllegalArgumentException if field is null;
     */
    protected AbstractPVField(Field field) {
        if (field == null) {
            throw new IllegalArgumentException("field is null");
        }
        this.field = field;
    }

    protected void setData(Field field, PVStructure parent, String fieldName) {
        this.field = field;
        pvParent = parent;
        this.fieldName = fieldName;
    }

    protected void changeField(Field field) {
        this.field = field;
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVField#getFieldName()
     */
    public String getFieldName() {
        if (fieldName != null) return fieldName;
        if (pvParent == null) {
            fieldName = "";
            return fieldName;
        }
        throw new IllegalStateException("logic error");
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVField#getFullName()
     */
    public String getFullName() {
        StringBuilder ret = new StringBuilder(fieldName);
        for (PVField fld = getParent(); fld != null; fld = fld.getParent()) {
            if (fld.getFieldName().length() == 0)
                break;
            ret.insert(0, fld.getFieldName() + '.');
        }
        return ret.toString();
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVField#postPut()
     */
    public void postPut() {
        if (postHandler != null) postHandler.postPut();
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVField#registerPostHandler(org.epics.pvdata.pv.PostHandler)
     */
    public void setPostHandler(PostHandler postHandler) {
        if (this.postHandler != null) {
            if (postHandler == this.postHandler) return;
            throw new IllegalStateException("A postHandler is already registered");
        }
        this.postHandler = postHandler;
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVField#getOffset()
     */
    public int getFieldOffset() {
        if (nextFieldOffset == 0) computeOffset();
        return fieldOffset;
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVField#getNextOffset()
     */
    public int getNextFieldOffset() {
        if (nextFieldOffset == 0) computeOffset();
        return nextFieldOffset;
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVField#getNumberFields()
     */
    public int getNumberFields() {
        if (nextFieldOffset == 0) computeOffset();
        return (nextFieldOffset - fieldOffset);
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVField#isImmutable()
     */
    public boolean isImmutable() {
        return (isImmutable);
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVField#setMutable(boolean)
     */
    public void setImmutable() {
        isImmutable = true;
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVField#getField()
     */
    public Field getField() {
        return field;
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVField#getParent()
     */
    public PVStructure getParent() {
        return pvParent;
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVField#toString(java.lang.StringBuilder)
     */
    public void toString(StringBuilder buf) {
        toString(buf, 0);
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVField#toString(java.lang.StringBuilder, int)
     */
    public void toString(StringBuilder buf, int indentLevel) {
        convert.getString(buf, this, indentLevel);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        toString(builder);
        return builder.toString();
    }

    private void computeOffset() {
        PVStructure pvTop = this.pvParent;
        if (pvTop == null) {
            if (getField().getType() != Type.structure) {
                fieldOffset = 0;
                nextFieldOffset = 1;
                return;
            }
            pvTop = (PVStructure) this;
        } else {
            while (pvTop.getParent() != null) pvTop = pvTop.getParent();
        }
        int offset = 0;
        int nextOffset = 1;
        PVField[] pvFields = pvTop.getPVFields();
        for (PVField value : pvFields) {
            offset = nextOffset;
            PVField pvField = (AbstractPVField) value;
            Field field = pvField.getField();
            switch (field.getType()) {
                case scalar:
                case scalarArray:
                case structureArray:
                case union:
                case unionArray: {
                    AbstractPVField pv = (AbstractPVField) pvField;
                    nextOffset++;
                    pv.fieldOffset = offset;
                    pv.nextFieldOffset = nextOffset;
                    break;
                }
                case structure: {
                    AbstractPVField pv = (AbstractPVField) pvField;
                    pv.computeOffset(offset);
                    nextOffset = pv.getNextFieldOffset();
                }
            }
        }
        AbstractPVField top = (AbstractPVField) pvTop;
        top.fieldOffset = 0;
        top.nextFieldOffset = nextOffset;
    }

    private void computeOffset(int offset) {
        int beginOffset = offset;
        int nextOffset = offset + 1;
        PVStructure pvStructure = (PVStructure) this;
        PVField[] pvFields = pvStructure.getPVFields();
        for (PVField value : pvFields) {
            offset = nextOffset;
            PVField pvField = (AbstractPVField) value;
            Field field = pvField.getField();
            switch (field.getType()) {
                case scalar:
                case scalarArray:
                case structureArray:
                case union:
                case unionArray: {
                    AbstractPVField pv = (AbstractPVField) pvField;
                    nextOffset++;
                    pv.fieldOffset = offset;
                    pv.nextFieldOffset = nextOffset;
                    break;
                }
                case structure: {
                    AbstractPVField pv = (AbstractPVField) pvField;
                    pv.computeOffset(offset);
                    nextOffset = pv.getNextFieldOffset();
                }
            }
        }
        this.fieldOffset = beginOffset;
        this.nextFieldOffset = nextOffset;
    }
}
