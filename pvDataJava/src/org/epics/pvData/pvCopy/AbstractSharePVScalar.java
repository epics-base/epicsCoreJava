/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.pvCopy;

import java.nio.ByteBuffer;

import org.epics.pvData.factory.AbstractPVScalar;
import org.epics.pvData.pv.DeserializableControl;
import org.epics.pvData.pv.PVField;
import org.epics.pvData.pv.PVListener;
import org.epics.pvData.pv.PVRecord;
import org.epics.pvData.pv.PVRecordField;
import org.epics.pvData.pv.PVScalar;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.SerializableControl;

/**
 * @author mrk
 *
 */
public abstract class AbstractSharePVScalar extends AbstractPVScalar implements PVListener {
    private PVScalar pvShare = null;
    /**
     * Constructor.
     * @param parent The parent.
     * @param scalar The ScalarType.
     */
    protected AbstractSharePVScalar(PVStructure parent, PVScalar  pvShare) {
        super(parent,pvShare.getScalar());
        this.pvShare = pvShare;
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
        PVRecordField pvRecordField = pvShare.getPVRecordField();
        if(pvRecordField==null) return;
        PVRecord sharePVRecord = pvRecordField.getPVRecord();
        pvRecordField = super.getPVRecordField();
        PVRecord thisPVRecord = null;
        if(pvRecordField!=null) thisPVRecord = pvRecordField.getPVRecord();
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
        PVRecordField pvRecordField = pvShare.getPVRecordField();
        if(pvRecordField==null) return;
        pvRecordField.getPVRecord().unlock();
    }
    /**
     * lock this record.
     */
    protected void lockThis() {
        PVRecordField pvRecordField = super.getPVRecordField();
        if(pvRecordField==null) return;
        PVRecord thisPVRecord = pvRecordField.getPVRecord();
        pvRecordField = pvShare.getPVRecordField();
        PVRecord sharePVRecord = null;
        if(pvRecordField!=null) sharePVRecord = pvRecordField.getPVRecord();
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
        PVRecordField pvRecordField = super.getPVRecordField();
        if(pvRecordField==null) return;
        pvRecordField.getPVRecord().unlock();
    }

    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVListener#beginGroupPut(org.epics.pvData.pv.PVRecord)
     */
    @Override
    public void beginGroupPut(PVRecord pvRecord) {
        lockThis();
        try {
        PVRecord thisPVRecord = this.getPVRecordField().getPVRecord();
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
        PVRecord thisPVRecord = this.getPVRecordField().getPVRecord();
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
        PVRecord thisPVRecord = this.getPVRecordField().getPVRecord();
        if(thisPVRecord!=null) thisPVRecord.removeEveryListener();
        } finally {
            unlockThis();
        }
    }

    /* (non-Javadoc)
     * @see org.epics.pvData.pv.Serializable#deserialize(java.nio.ByteBuffer, org.epics.pvData.pv.DeserializableControl)
     */
    @Override
    public void deserialize(ByteBuffer buffer, DeserializableControl control) {
        lockShare();
        try {
        pvShare.deserialize(buffer, control);
        } finally {
            unlockShare();
        }
    }

    /* (non-Javadoc)
     * @see org.epics.pvData.pv.Serializable#serialize(java.nio.ByteBuffer, org.epics.pvData.pv.SerializableControl)
     */
    @Override
    public void serialize(ByteBuffer buffer, SerializableControl flusher) {
        lockShare();		// TODO this can block !!!
        try {
            pvShare.serialize(buffer, flusher);
        } finally {
            unlockShare();
        }
    }
}
