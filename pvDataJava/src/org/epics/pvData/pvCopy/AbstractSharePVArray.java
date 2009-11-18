/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.pvCopy;

import java.nio.ByteBuffer;

import org.epics.pvData.factory.AbstractPVField;
import org.epics.pvData.pv.Array;
import org.epics.pvData.pv.DeserializableControl;
import org.epics.pvData.pv.PVArray;
import org.epics.pvData.pv.PVField;
import org.epics.pvData.pv.PVListener;
import org.epics.pvData.pv.PVRecord;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.SerializableControl;


/**
 * Base class for implementing PVBooleanArray.
 * @author mrk
 *
 */
public abstract class AbstractSharePVArray extends AbstractPVField implements PVArray, PVListener
{
    private PVArray pvShare = null;
    /**
     * Constructor.
     * @param parent The parent.
     * @param array The Introspection interface.
     */
    protected AbstractSharePVArray(PVStructure parent,PVArray pvShare)
    {
        super(parent,pvShare.getArray());
        this.pvShare = pvShare;
    }        
    
    /**
     * Lock the shared record.
     */
    protected void lockShare() {
        PVRecord sharePVRecord = pvShare.getPVRecord();
        PVRecord thisPVRecord = super.getPVRecord();
        if(sharePVRecord==null) return;
        if(thisPVRecord==null) {
            sharePVRecord.lock();
        } else {
            thisPVRecord.lockOtherRecord(sharePVRecord);
        }
    }
    /**
     * Unlock the shared record
     */
    protected void unlockShare() {
        PVRecord sharePVRecord = pvShare.getPVRecord();
        if(sharePVRecord!=null) sharePVRecord.unlock();
    }
    /**
     * lock this record.
     */
    protected void lockThis() {
        PVRecord sharePVRecord = pvShare.getPVRecord();
        PVRecord thisPVRecord = super.getPVRecord();
        if(thisPVRecord==null) return;
        if(sharePVRecord==null) {
            thisPVRecord.lock();
        } else {
            sharePVRecord.lockOtherRecord(thisPVRecord);
        }
    }
    /**
     * unlock this record.
     */
    protected void unlockThis() {
        PVRecord thisPVRecord = super.getPVRecord();
        if(thisPVRecord!=null) thisPVRecord.unlock();
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVArray#getArray()
     */
    @Override
    public Array getArray() {
        return (Array)super.getField();
    }

    /* (non-Javadoc)
     * @see org.epics.pvData.pv.Serializable#serialize(java.nio.ByteBuffer, org.epics.pvData.pv.SerializableControl)
     */
    @Override
    public void serialize(ByteBuffer buffer, SerializableControl flusher) {
        serialize(buffer, flusher, 0, -1);
    }

    /* (non-Javadoc)
     * @see org.epics.pvData.factory.AbstractPVArray#setImmutable()
     */
    @Override
    public void setImmutable() {
        pvShare.setImmutable();
        super.setImmutable();
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.factory.AbstractPVArray#setLength(int)
     */
    @Override
    public void setLength(int len) {
        pvShare.setLength(len);
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.factory.AbstractPVArray#getCapacity()
     */
    @Override
    public int getCapacity() {
        return pvShare.getCapacity();
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.factory.AbstractPVArray#getLength()
     */
    @Override
    public int getLength() {
        return pvShare.getLength();
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.factory.AbstractPVArray#isCapacityMutable()
     */
    @Override
    public boolean isCapacityMutable() {
        return pvShare.isCapacityMutable();
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.factory.AbstractPVArray#setCapacityMutable(boolean)
     */
    @Override
    public void setCapacityMutable(boolean isMutable) {
        pvShare.setCapacityMutable(isMutable);
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.factory.AbstractPVField#toString(int)
     */
    public String toString(int indentLevel) {
        return convert.getString(this, indentLevel)
        + super.toString(indentLevel);
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.factory.AbstractPVArray#setCapacity(int)
     */
    public void setCapacity(int len) {
        lockShare();
        try {
            pvShare.setCapacity(len);
        } finally {
            unlockShare();
        }
    }
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
	    lockShare();
        try {
	    return pvShare.equals(obj);
        } finally {
            unlockShare();
        }
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
	    lockShare();
        try {
            return pvShare.hashCode();
        } finally {
            unlockShare();
        }
	}
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVListener#beginGroupPut(org.epics.pvData.pv.PVRecord)
     */
    @Override
    public void beginGroupPut(PVRecord pvRecord) {
        lockThis();
        try {
        PVRecord thisPVRecord = this.getPVRecord();
        if(thisPVRecord!=null) thisPVRecord.beginGroupPut();
        } finally {
            unlockThis();
        }
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVListener#dataPut(org.epics.pvData.pv.PVField)
     */
    @Override
    public void dataPut(PVField pvField) {
        lockThis();
        try {
        super.postPut();
        } finally {
            unlockThis();
        }
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVListener#dataPut(org.epics.pvData.pv.PVStructure, org.epics.pvData.pv.PVField)
     */
    @Override
    public void dataPut(PVStructure requested, PVField pvField) {}
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVListener#endGroupPut(org.epics.pvData.pv.PVRecord)
     */
    @Override
    public void endGroupPut(PVRecord pvRecord) {
        lockThis();
        try {
        PVRecord thisPVRecord = this.getPVRecord();
        if(thisPVRecord!=null) thisPVRecord.endGroupPut();
        } finally {
            unlockThis();
        }
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVListener#unlisten(org.epics.pvData.pv.PVRecord)
     */
    @Override
    public void unlisten(PVRecord pvRecord) {
        lockThis();
        try {
        PVRecord thisPVRecord = this.getPVRecord();
        if(thisPVRecord!=null) thisPVRecord.removeEveryListener();
        } finally {
            unlockThis();
        }
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.SerializableArray#serialize(java.nio.ByteBuffer, org.epics.pvData.pv.SerializableControl, int, int)
     */
    @Override
    public void serialize(ByteBuffer buffer, SerializableControl flusher, int offset, int count) {
        lockShare();		// TODO this can block !!!
        try {
            pvShare.serialize(buffer, flusher, offset, count);
        } finally {
            unlockShare();
        }
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.Serializable#deserialize(java.nio.ByteBuffer, org.epics.pvData.pv.DeserializableControl)
     */
    @Override
    public void deserialize(ByteBuffer buffer, DeserializableControl control) {
        lockShare();	// TODO this can block !!!
        try {
        pvShare.deserialize(buffer, control);
        } finally {
            unlockShare();
        }
    }
}