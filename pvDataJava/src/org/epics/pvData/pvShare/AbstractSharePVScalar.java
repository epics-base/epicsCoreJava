/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.pvShare;

import java.nio.ByteBuffer;

import org.epics.pvData.factory.AbstractPVScalar;
import org.epics.pvData.pv.PVField;
import org.epics.pvData.pv.PVListener;
import org.epics.pvData.pv.PVRecord;
import org.epics.pvData.pv.PVScalar;
import org.epics.pvData.pv.PVStructure;

/**
 * @author mrk
 *
 */
public abstract class AbstractSharePVScalar extends AbstractPVScalar implements PVListener {
    private PVScalar pvShare = null;
    private PVRecord thisPVRecord = null;
    private PVRecord sharePVRecord = null;
    /**
     * Constructor.
     * @param parent The parent.
     * @param scalar The ScalarType.
     */
    protected AbstractSharePVScalar(PVStructure parent, PVScalar  pvShare) {
        super(parent,pvShare.getScalar());
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
     * @see org.epics.pvData.pv.Serializable#deserialize(java.nio.ByteBuffer)
     */
    @Override
    public void deserialize(ByteBuffer buffer) {
        lockShare();
        try {
        pvShare.deserialize(buffer);
        } finally {
            unlockShare();
        }
    }

    /* (non-Javadoc)
     * @see org.epics.pvData.pv.Serializable#serialize(java.nio.ByteBuffer)
     */
    @Override
    public void serialize(ByteBuffer buffer) {
        lockShare();
        try {
            pvShare.serialize(buffer);
        } finally {
            unlockShare();
        }
    }
}
