/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.factory;

import org.epics.pvData.pv.Array;
import org.epics.pvData.pv.Convert;
import org.epics.pvData.pv.Field;
import org.epics.pvData.pv.FieldCreate;
import org.epics.pvData.pv.MessageType;
import org.epics.pvData.pv.PVAuxInfo;
import org.epics.pvData.pv.PVField;
import org.epics.pvData.pv.PVRecord;
import org.epics.pvData.pv.PVRecordField;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.Scalar;
import org.epics.pvData.pv.Structure;
import org.epics.pvData.pv.Type;


/**
 * Abstract base class for a PVField.
 * A factory that implements PVField must extend this class.
 * @author mrk
 *
 */
public abstract class AbstractPVField implements PVField{
    private static FieldCreate fieldCreate = FieldFactory.getFieldCreate();
    private int fieldOffset = 0;;
    private int nextFieldOffset = 0;;
    private PVAuxInfo pvAuxInfo = null;
    private boolean isImmutable = false;
    private String fullFieldName = "";
    private String fullName = "";
    private Field field;
    private PVStructure pvParent;
    private PVRecordField pvRecordField = null;
    /**
     * Convenience for derived classes that perform conversions.
     */
    protected static Convert convert = ConvertFactory.getConvert();
    /**
     * Constructor that must be called by derived classes.
     * @param pvParent The pvParent PVStructure.
     * @param field The introspection interface.
     * @throws IllegalArgumentException if field is null;
     */
    protected AbstractPVField(PVStructure parent, Field field) {
        if(field==null) {
            throw new IllegalArgumentException("field is null");
        }
        this.field = field;
        this.pvParent = parent;
        if(parent==null) {
            fullFieldName = fullName = field.getFieldName();
            return;
        }
        createFullNameANDFullFieldName();
    }
    /**
     * Called by derived classes to replace a field.
     * @param field The new field.
     */
    protected void replaceField(Field field) {
        this.field = field;
        createFullNameANDFullFieldName();
    }
    /**
     * Initial call is by implementation of PVRecord..
     * @param pvRecord The PVRecord interface.
     */
    protected void setRecord(PVRecord record) {
        pvRecordField = new BasePVRecordField(this,record);
        // call setRecord for all subfields.
        if(field.getType()==Type.structure) {
            PVField[] pvFields = ((PVStructure)this).getPVFields();
            for(int i=0; i<pvFields.length; i++) {
                AbstractPVField pvField = (AbstractPVField)pvFields[i];
                pvField.setRecord(record);
            }
        }
        createFullNameANDFullFieldName();
    }
    /**
     * Called by BasePVStructure
     * @param fieldOffset The fieldOffset for this field.
     * @param nextFieldOffset The nextFieldOffset for this field.
     */
    protected void setOffsets(int offset,int nextOffset) {
        this.fieldOffset = offset;
        this.nextFieldOffset = nextOffset;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVField#postPut()
     */
    @Override
    public void postPut() {
        if(pvRecordField!=null) pvRecordField.postPut();
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVField#getOffset()
     */
    @Override
    public int getFieldOffset() {
        return fieldOffset;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVField#getNextOffset()
     */
    @Override
    public int getNextFieldOffset() {
        return nextFieldOffset;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVField#getNumberFields()
     */
    @Override
    public int getNumberFields() {
        return (nextFieldOffset - fieldOffset);
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVField#getPVAuxInfo()
     */
    @Override
    public PVAuxInfo getPVAuxInfo() {
        if(pvAuxInfo==null) pvAuxInfo = new BasePVAuxInfo(this);
        return pvAuxInfo;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.Requester#getRequesterName()
     */
    @Override
    public String getRequesterName() {
        return getFullName();
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.Requester#message(java.lang.String, org.epics.pvData.pv.MessageType)
     */
    @Override
    public void message(String message, MessageType messageType) {
        if(pvRecordField==null) {
            System.out.println(messageType.toString() + " " + fullFieldName + " " + message);
        } else {
            pvRecordField.getPVRecord().message(fullName + " " + message, messageType);
        }
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVField#isImmutable()
     */
    @Override
    public boolean isImmutable() {
        return(isImmutable);
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVField#setMutable(boolean)
     */
    @Override
    public void setImmutable() {
        if(!isImmutable && pvRecordField!=null) {
            pvRecordField.getPVRecord().removeEveryListener();
        }
        isImmutable = true;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVField#getFullFieldName()
     */
    @Override
    public String getFullFieldName() {
        return fullFieldName;
    } 
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVField#getFullName()
     */
    @Override
    public String getFullName() {
        return fullName;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVField#getField()
     */
    public Field getField() {
        return field;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVField#getParent()
     */
    @Override
    public PVStructure getParent() {
        return pvParent;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVField#getPVRecordField()
     */
    @Override
    public PVRecordField getPVRecordField() {
        return pvRecordField;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVField#replacePVField(org.epics.pvData.pv.PVField)
     */
    @Override
    public void replacePVField(PVField newPVField) {
        PVStructure parent = getParent();
        if(parent==null) throw new IllegalArgumentException("no pvParent");
        parent.replacePVField(field.getFieldName(), newPVField);
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVField#renameField(java.lang.String)
     */
    @Override
    public void renameField(String newName) {
        switch(field.getType()) {
        case scalar: {
            Scalar scalar = (Scalar)field;
            scalar = fieldCreate.createScalar(newName, scalar.getScalarType());
            this.field = scalar;
            createFullNameANDFullFieldName();
            return;
        }
        case scalarArray: {
            Array array = (Array)field;
            array = fieldCreate.createArray(newName, array.getElementType());
            this.field = array;
            createFullNameANDFullFieldName();
            return;
        }
        case structure: {
            Structure structure = (Structure)field;
            Field[] origFields = structure.getFields();
            structure = fieldCreate.createStructure(newName, origFields);
            this.field = structure;
            createFullNameANDFullFieldName();
            PVStructure pvStructure = (PVStructure)this;
            // must make all subfields call createFullNameANDFullFieldName()
            for(PVField pvField: pvStructure.getPVFields()) {
                pvField.renameField(pvField.getField().getFieldName());
            }
            return;
        }
        }
    }
   
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return toString(0);
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVField#toString(int)
     */
    @Override
    public String toString(int indentLevel) {
        if(pvAuxInfo==null) return "";
        return pvAuxInfo.toString(indentLevel);
    }
    
    private void createFullNameANDFullFieldName() {
        PVRecord pvRecord = null;
        if(pvRecordField!=null) {
        	pvRecord = pvRecordField.getPVRecord();
        	if(this==pvRecord.getPVStructure()) {
        		fullName = pvRecord.getRecordName();
        		fullFieldName = "";
        		return;
        	}
        }
        if(pvParent==null) {
            fullFieldName = fullName = field.getFieldName();
            return;
        }
        StringBuilder fieldName = new StringBuilder();
        Field field = getField();
        String name = field.getFieldName();
        if(name!=null && name.length()>0) {
            fieldName.append(name);
        }
        PVField parent = getParent();
        while(parent!=null && parent!=pvRecord) {
            field = parent.getField();
            name = field.getFieldName();
            if(name!=null && name.length()>0) {
                fieldName.insert(0,parent.getField().getFieldName()+ ".");
            }
            parent = parent.getParent();
        }
        fullFieldName = fieldName.toString();
        if(pvRecord!=null) {
            fullName = pvRecord.getRecordName() + "." + fullFieldName;
        } else {
            fullName = fullFieldName;
        }
    }    
}
