/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.factory;


import org.epics.pvData.pv.Convert;
import org.epics.pvData.pv.Field;
import org.epics.pvData.pv.FieldCreate;
import org.epics.pvData.pv.MessageType;
import org.epics.pvData.pv.PVAuxInfo;
import org.epics.pvData.pv.PVField;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.PostHandler;
import org.epics.pvData.pv.Requester;
import org.epics.pvData.pv.Structure;
import org.epics.pvData.pv.Type;


/**
 * Abstract base class for a PVField.
 * A factory that implements PVField must extend this class.
 * @author mrk
 *
 */
public abstract class AbstractPVField implements PVField{
  

    private final static FieldCreate fieldCreate = FieldFactory.getFieldCreate();
    private int fieldOffset = 0;
    private int nextFieldOffset = 0;
    private PVAuxInfo pvAuxInfo = null;
    private boolean isImmutable = false;
    private String fieldName = null;
    private Field field;
    private PVStructure pvParent;
    private Requester requester = null;
	private PostHandler postHandler = null;
    /**
     * Convenience for derived classes that perform conversions.
     */
    protected static final Convert convert = ConvertFactory.getConvert();
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
    }
    /**
     * Called by derived classes to replace a Structure.
     */
    protected void replaceStructure(String[] newFieldNames,PVStructure pvStructure) {
        PVField[] pvFields = pvStructure.getPVFields();
        int length = pvFields.length;
        Field[] newFields = new Field[length];
        for(int i=0; i<length; i++) {
            newFields[i] = pvFields[i].getField();
        }
        Structure newStructure = fieldCreate.createStructure(newFieldNames,newFields);
        field = newStructure;
        if(pvParent!=null) {
            ((AbstractPVField)pvParent).replaceStructure(pvParent.getStructure().getFieldNames(),pvParent);
        }
    }
    protected void setParent(PVStructure parent)
    {
        pvParent = parent;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVField#getFieldName()
     */
    @Override
    public String getFieldName() {
        if(fieldName!=null) return fieldName;
        if(pvParent==null) {
            fieldName = "";
            return fieldName;
        }
        PVField[] pvFields = pvParent.getPVFields();
        String[] fieldNames = pvParent.getStructure().getFieldNames();
        for(int i=0; i<pvFields.length; i++) {
            if(pvFields[i]==this) {
                fieldName = fieldNames[i];
                return fieldName;
            }
        }
        throw new IllegalStateException("logic error");
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.Requester#getRequesterName()
     */
    @Override
	public String getRequesterName() {
		if(requester!=null) {
			return requester.getRequesterName();
		} else {
			return "none";
		}
	}
    
    
    private void messagePvt(String message,MessageType messageType)
    {
        if(pvParent!=null) {
            Structure structure = pvParent.getStructure();
            String[] fieldNames = structure.getFieldNames();
            Field[] fields = structure.getFields();
            for(int i=0; i < fields.length; i++) {
                if(fields[i]==this.field) {
                    message = fieldNames[i] + " " + message;
                    AbstractPVField xxx = (AbstractPVField)pvParent;
                    xxx.messagePvt(message, messageType);
                    return;
                }
            }    		
            throw new IllegalStateException("Logic error in pvDataJava");

        }
        System.out.println(messageType.toString() + " "  + message);
    }
	/* (non-Javadoc)
	 * @see org.epics.pvData.pv.Requester#message(java.lang.String, org.epics.pvData.pv.MessageType)
	 */
	@Override
	public void message(String message, MessageType messageType) {
	    messagePvt(message,messageType);
		
	}
	/* (non-Javadoc)
	 * @see org.epics.pvData.pv.PVField#registerRequester(org.epics.pvData.pv.Requester)
	 */
	@Override
	public void setRequester(Requester requester) {
	    if(pvParent!=null) {
	        throw new IllegalStateException("PVField::setRequester only legal for top level structure");
	    }
		if(this.requester!=null) {
			if(requester==this.requester) return;
			throw new IllegalStateException("A requester is already registered");
		}
		this.requester = requester;
	}
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVField#postPut()
     */
    @Override
    public void postPut() {
        if(postHandler!=null) postHandler.postPut();
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVField#registerPostHandler(org.epics.pvData.pv.PostHandler)
     */
    @Override
	public void setPostHandler(PostHandler postHandler) {
		if(this.postHandler!=null) {
			if(postHandler==this.postHandler) return;
			throw new IllegalStateException("A postHandler is already registered");
		}
		this.postHandler = postHandler;
	}
	/* (non-Javadoc)
     * @see org.epics.pvData.pv.PVField#getOffset()
     */
    @Override
    public int getFieldOffset() {
    	if(nextFieldOffset==0) computeOffset();
        return fieldOffset;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVField#getNextOffset()
     */
    @Override
    public int getNextFieldOffset() {
    	if(nextFieldOffset==0) computeOffset();
        return nextFieldOffset;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVField#getNumberFields()
     */
    @Override
    public int getNumberFields() {
    	if(nextFieldOffset==0) computeOffset();
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
        isImmutable = true;
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
     * @see org.epics.pvData.pv.PVField#replacePVField(org.epics.pvData.pv.PVField)
     */
    @Override
    public void replacePVField(PVField newPVField) {
        PVStructure parent = getParent();
        if(parent==null) throw new IllegalStateException("no pvParent");
        Field[] fields = parent.getStructure().getFields();
        String[] fieldNames = parent.getStructure().getFieldNames();
        PVField[] pvFields = parent.getPVFields();
        for(int i=0; i < fields.length; i++) {
            if(fields[i]==field) {
                pvFields[i] = newPVField;
                ((AbstractPVField)parent).replaceStructure(fieldNames,parent);
                return;
            }
        }
        throw new IllegalStateException("Did not find field in parent");    
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVField#renameField(java.lang.String)
     */
    @Override
    public void renameField(String newName) {
        PVStructure parent = getParent();
        if(parent==null) throw new IllegalStateException("no pvParent");
        Structure structure = parent.getStructure();
        String[] fieldNames = structure.getFieldNames();
        Field[] fields = structure.getFields();
        for(int i=0; i < fields.length; i++) {
            if(fields[i]==field) {
                fieldNames[i] = newName;
                return;
            }
        }
        throw new IllegalStateException("Did not find field in parent");
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVField#toString(java.lang.StringBuilder)
     */
    @Override
    public void toString(StringBuilder buf) {
        toString(buf,0);
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVField#toString(java.lang.StringBuilder, int)
     */
    @Override
    public void toString(StringBuilder buf,int indentLevel) {
        convert.getString(buf,this,indentLevel);
        if(pvAuxInfo==null) return;
        pvAuxInfo.toString(buf,indentLevel);
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
        if(pvTop==null) {
            if(getField().getType()!=Type.structure) {
                fieldOffset = 0;
                nextFieldOffset = 1;
                return;
            }
            pvTop = (PVStructure)this;
        } else {
    		while(pvTop.getParent()!=null) pvTop = pvTop.getParent();
    	}
        int offset = 0;
        int nextOffset = 1;
        PVField[] pvFields = pvTop.getPVFields();
        for(int i=0; i < pvFields.length; i++) {
            offset = nextOffset;
            PVField pvField = (AbstractPVField)pvFields[i];
            Field field = pvField.getField();
            switch(field.getType()) {
            case scalar:
            case scalarArray:
            case structureArray:{
                AbstractPVField pv = (AbstractPVField)pvField;
                nextOffset++;
                pv.fieldOffset = offset;
                pv.nextFieldOffset = nextOffset;
                break;
            }
            case structure: {
                AbstractPVField pv = (AbstractPVField)pvField;
                pv.computeOffset(offset);
                nextOffset = pv.getNextFieldOffset();
            }
            }
        }
        AbstractPVField top = (AbstractPVField)pvTop;
        top.fieldOffset = 0;
        top.nextFieldOffset = nextOffset;
    }
    
    private void computeOffset(int offset) {
        int beginOffset = offset;
        int nextOffset = offset + 1;
        PVStructure pvStructure = (PVStructure)this;
        PVField[] pvFields = pvStructure.getPVFields();
        for(int i=0; i < pvFields.length; i++) {
            offset = nextOffset;
            PVField pvField = (AbstractPVField)pvFields[i];
            Field field = pvField.getField();
            switch(field.getType()) {
            case scalar:
            case scalarArray: {
                AbstractPVField pv = (AbstractPVField)pvField;
                nextOffset++;
                pv.fieldOffset = offset;
                pv.nextFieldOffset = nextOffset;
                break;
            }
            case structure: {
            	AbstractPVField pv = (AbstractPVField)pvField;
                pv.computeOffset(offset);
                nextOffset = pv.getNextFieldOffset();
            }
            }
        }
        this.fieldOffset = beginOffset;
        this.nextFieldOffset = nextOffset;
    }
}
