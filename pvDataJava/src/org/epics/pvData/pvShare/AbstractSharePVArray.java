/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.pvShare;

import java.nio.ByteBuffer;

import org.epics.pvData.factory.AbstractPVArray;
import org.epics.pvData.pv.PVArray;
import org.epics.pvData.pv.PVField;
import org.epics.pvData.pv.PVListener;
import org.epics.pvData.pv.PVRecord;
import org.epics.pvData.pv.PVStructure;


/**
 * Base class for implementing PVBooleanArray.
 * @author mrk
 *
 */
public abstract class AbstractSharePVArray extends AbstractPVArray implements PVListener
{
    private PVArray pvShare = null;
    private PVRecord thisPVRecord = null;
    private PVRecord sharePVRecord = null;
    /**
     * Constructor.
     * @param parent The parent.
     * @param array The Introspection interface.
     */
    protected AbstractSharePVArray(PVStructure parent,PVArray pvShare)
    {
        super(parent,pvShare.getArray());
        super.setCapacityMutable(pvShare.isCapacityMutable());
        super.setImmutable();
        this.pvShare = pvShare;
        thisPVRecord = parent.getPVRecord();
        sharePVRecord = pvShare.getPVRecord();
    }        
    /* (non-Javadoc)
     * @see org.epics.pvData.factory.AbstractPVField#toString(int)
     */
    public String toString(int indentLevel) {
        return convert.getString(this, indentLevel)
        + super.toString(indentLevel);
    }
    /**
     * Lock the shared record.
     */
    protected void lockShare() {
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
        if(sharePVRecord!=null) sharePVRecord.unlock();
    }
    /**
     * lock this record.
     */
    protected void lockThis() {
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
        if(thisPVRecord!=null) thisPVRecord.unlock();
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.factory.AbstractPVArray#setCapacity(int)
     */
    public void setCapacity(int len) {
        lockShare();
        try {
            pvShare.setCapacity(length);
            super.capacity = pvShare.getCapacity();
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
     * @see org.epics.pvData.pv.Serializable#serialize(java.nio.ByteBuffer, int, int)
     */
    public void serialize(ByteBuffer buffer, int offset, int count) {
        lockShare();
        try {
            pvShare.serialize(buffer, offset, count);
        } finally {
            unlockShare();
        }
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.Serializable#deserialize(java.nio.ByteBuffer)
     */
    public void deserialize(ByteBuffer buffer) {
        lockShare();
        try {
        pvShare.deserialize(buffer);
        } finally {
            unlockShare();
        }
    }
}