/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.factory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.epics.pvData.pv.MessageType;
import org.epics.pvData.pv.PVField;
import org.epics.pvData.pv.PVListener;
import org.epics.pvData.pv.PVRecord;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.Requester;
import org.epics.pvData.pv.Structure;
import org.epics.pvData.pv.Type;



/**
 * Base class for a record instance.
 * @author mrk
 *
 */
public class BasePVRecord extends BasePVStructure implements PVRecord {
    private String recordName;
    private List<Requester> requesterList = new ArrayList<Requester>();
    private LinkedList<PVListener> pvListenerList = new LinkedList<PVListener>();
    private ReentrantLock lock = new ReentrantLock();
    private static int numberRecords = 0;
    private int id = numberRecords++;
    /**
     * Constructor.
     * @param recordName The name of the record.
     * @param structure The introspection interface for the record.
     */
    public BasePVRecord(String recordName,Structure structure)
    {
        super(null,structure);
        this.recordName = recordName;
        super.setRecord(this);
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVRecord#getPVStructure()
     */
    public PVStructure getPVStructure() {
        return (PVStructure)this;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVRecord#getRecordName()
     */
    public String getRecordName() {
        return recordName;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.factory.AbstractPVField#message(java.lang.String, org.epics.pvData.pv.MessageType)
     */
    public void message(String message, MessageType messageType) {
        if(message!=null && message.charAt(0)!='.') message = "." + message;
        message = recordName + message;
        for (Requester requester : requesterList) {
            requester.message(message, messageType);
        }
        if(requesterList.size()==0) {
            System.out.println(messageType.toString() + " " + message);
        }
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVRecord#addRequester(org.epics.pvData.pv.Requester)
     */
    public void addRequester(Requester requester) {
        requesterList.add(requester);
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVRecord#removeRequester(org.epics.pvData.pv.Requester)
     */
    public void removeRequester(Requester requester) {
        requesterList.remove(requester);
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVRecord#lock()
     */
    public void lock() {
        lock.lock();
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVRecord#lockOtherRecord(org.epics.pvData.pv.PVRecord)
     */
    public void lockOtherRecord(PVRecord otherRecord) {
        BasePVRecord impl = (BasePVRecord)otherRecord;
        int otherId = impl.id;
        if(id<=otherId) {
            otherRecord.lock();
            return;
        }
        int count = lock.getHoldCount();
        for(int i=0; i<count; i++) lock.unlock();
        otherRecord.lock();
        for(int i=0; i<count; i++) lock.lock();
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVRecord#unlock()
     */
    public void unlock() {
        lock.unlock();
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVRecord#beginGroupPut()
     */
    public void beginGroupPut() {
        Iterator<PVListener> iter;
        iter = pvListenerList.iterator();
        while(iter.hasNext()) {
            PVListener pvListener = iter.next();
            pvListener.beginGroupPut(this);
        }
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVRecord#endGroupPut()
     */
    public void endGroupPut() {
        Iterator<PVListener> iter;
        iter = pvListenerList.iterator();
        while(iter.hasNext()) {
            PVListener pvListener = iter.next();
            pvListener.endGroupPut(this);
        }
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVRecord#registerListener(org.epics.pvData.pv.PVListener)
     */
    public void registerListener(PVListener recordListener) {
        pvListenerList.add(recordListener);
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVRecord#unregisterListener(org.epics.pvData.pv.PVListener)
     */
    public void unregisterListener(PVListener recordListener) {
        pvListenerList.remove(recordListener);
        removeListener(this,recordListener);
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVRecord#isRegisteredListener(org.epics.pvData.pv.PVListener)
     */
    public boolean isRegisteredListener(PVListener pvListener) {
        if(pvListenerList.contains(pvListener)) return true;
        return false;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.factory.AbstractPVField#removeEveryListener()
     */
    public void removeEveryListener() {
        for(PVListener pvListener : pvListenerList) pvListener.unlisten(this);
        pvListenerList.clear();
        PVStructure pvField = this;
        removeAll((BasePVStructure)pvField);
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.factory.BasePVStructure#toString()
     */
    public String toString() { return toString(0);}
    /* (non-Javadoc)
     * @see org.epics.pvData.factory.BasePVStructure#toString(int)
     */
    public String toString(int indentLevel) {
        return super.toString("record " + recordName,indentLevel);
    }
    
    private void removeAll(BasePVStructure pvStructure) {
        pvStructure.removeEveryListener();
        PVField[] pvFields = pvStructure.getPVFields();
        for(PVField pvField : pvFields) {
            ((AbstractPVField)pvField).removeEveryListener();
            if(pvField.getField().getType()==Type.structure) removeAll((BasePVStructure)pvField);
        }
    }
    
    private void removeListener(PVStructure pvStructure,PVListener pvListener) {
        pvStructure.removeListener(pvListener);
        PVField[] pvFields = pvStructure.getPVFields();
        for(PVField pvField : pvFields) {
            pvField.removeListener(pvListener);
            if(pvField.getField().getType()==Type.structure) removeListener((PVStructure)pvField,pvListener);
        }
    }
}
