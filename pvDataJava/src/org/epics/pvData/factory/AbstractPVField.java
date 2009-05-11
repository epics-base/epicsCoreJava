/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.factory;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.regex.Pattern;

import org.epics.pvData.pv.Array;
import org.epics.pvData.pv.Convert;
import org.epics.pvData.pv.Field;
import org.epics.pvData.pv.FieldCreate;
import org.epics.pvData.pv.MessageType;
import org.epics.pvData.pv.PVAuxInfo;
import org.epics.pvData.pv.PVField;
import org.epics.pvData.pv.PVListener;
import org.epics.pvData.pv.PVRecord;
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
    private PVAuxInfo pvAuxInfo = null;
    private boolean isMutable = true;
    private String fullFieldName = "";
    private String fullName = "";
    private Field field;
    private PVStructure parent;
    private PVRecord record = null;
    protected LinkedList<PVListener> pvListenerList = new LinkedList<PVListener>();
    
    /**
     * Convenience for derived classes that perform conversions.
     */
    protected static Convert convert = ConvertFactory.getConvert();
    /**
     * Convenience for derived classes that parse arrays.
     */
    protected static Pattern commaSpacePattern = Pattern.compile("[, ]");
    /**
     * Convenience for classes that parse names.
     */
    protected static  Pattern periodPattern = Pattern.compile("[.]");

       
    /**
     * Constructor that must be called by derived classes.
     * @param parent The parent PVStructure.
     * @param field The introspection interface.
     * @throws IllegalArgumentException if field is null;
     */
    protected AbstractPVField(PVStructure parent, Field field) {
        if(field==null) {
            throw new IllegalArgumentException("field is null");
        }
        pvAuxInfo = new BasePVAuxInfo(this);
        this.field = field;
        this.parent = parent;
        if(parent==null) {
            fullFieldName = fullName = field.getFieldName();
            return;
        }
        record = parent.getPVRecord();
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
     * Called by derived class to specify the PVRecord interface.
     * @param record The PVRecord interface.
     */
    protected void setRecord(PVRecord record) {
        this.record = record;
        createFullNameANDFullFieldName();
    }
    /**
     * Called by BasePVStructure.postPut() and recursively by itself.
     * @param pvField The pvField to post.
     */
    protected void postPutNoParent(AbstractPVField pvField) {
        pvField.callListener();
        if(pvField.getField().getType()==Type.structure) {
            PVStructure pvStructure = (PVStructure)pvField;
            PVField[] pvFields = pvStructure.getPVFields();
            for(PVField pvf : pvFields) {
                postPutNoParent((AbstractPVField)pvf);
            }
        }
        
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVField#removeEveryListener()
     */
    protected void removeEveryListener() {
        pvListenerList.clear();
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVField#getPVAuxInfo()
     */
    public PVAuxInfo getPVAuxInfo() {
        return pvAuxInfo;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.Requester#getRequesterName()
     */
    public String getRequesterName() {
        return getFullName();
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.Requester#message(java.lang.String, org.epics.pvData.pv.MessageType)
     */
    public void message(String message, MessageType messageType) {
        if(record==null) {
            System.out.println(
                    messageType.toString() + " " + fullFieldName + " " + message);
        } else {
            record.message(fullName + " " + message, messageType);
        }
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVField#isMutable()
     */
    public boolean isMutable() {
        return(isMutable);
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVField#setMutable(boolean)
     */
    public void setMutable(boolean value) {
        isMutable = value;
        
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVField#getFullFieldName()
     */
    public String getFullFieldName() {
        return fullFieldName;
    } 
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVField#getFullName()
     */
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
    public PVStructure getParent() {
        return parent;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVField#getPVRecord()
     */
    public PVRecord getPVRecord() {
       return record;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVField#replacePVField(org.epics.pvData.pv.PVField)
     */
    public void replacePVField(PVField newPVField) {
        PVStructure parent = getParent();
        if(parent==null) throw new IllegalArgumentException("no parent");
        parent.replacePVField(field.getFieldName(), newPVField);
    }
   
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVField#renameField(java.lang.String)
     */
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
     * @see org.epics.pvData.pv.PVField#addListener(org.epics.pvData.pv.PVListener)
     */
    public boolean addListener(PVListener pvListener) {
        if(!record.isRegisteredListener(pvListener)) return false;
        return pvListenerList.add(pvListener);
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVField#removeListener(org.epics.pvData.pv.PVListener)
     */
    public void removeListener(PVListener recordListener) {
        pvListenerList.remove(recordListener);
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVField#postPut()
     */
    public void postPut() {
        callListener();
        if(parent!=null)parent.postPut(this);
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVField#toString(int)
     */
    public String toString(int indentLevel) {
        return pvAuxInfo.toString(indentLevel);
    }
    
    private void callListener() {
        Iterator<PVListener> iter;
        iter = pvListenerList.iterator();
        while(iter.hasNext()) {
            PVListener pvListener = iter.next();
            pvListener.dataPut(this);
        }
        
    }
    
    private void createFullNameANDFullFieldName() {
        if(record==null) return;
        if(this==record) {
            fullName = record.getRecordName();
            return;
        }
        StringBuilder fieldName = new StringBuilder();
        Field field = getField();
        String name = field.getFieldName();
        if(name!=null && name.length()>0) {
            fieldName.append(name);
        }
        PVField parent = getParent();
        while(parent!=this.record) {
            field = parent.getField();
            name = field.getFieldName();
            if(name!=null && name.length()>0) {
                fieldName.insert(0,parent.getField().getFieldName()+ ".");
            }
            parent = parent.getParent();
        }
        if(fieldName.length()>0 && fieldName.charAt(0)=='.') {
            fullFieldName = fieldName.substring(1); //remove leading "."
        } else {
            fullFieldName = fieldName.toString();
        }
        if(record!=null) {
            fullName = record.getRecordName() + "." + fullFieldName;
        } else {
            fullName = fullFieldName;
        }
    }
}
