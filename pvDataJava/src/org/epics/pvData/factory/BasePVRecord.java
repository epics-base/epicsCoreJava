/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.factory;

import java.util.concurrent.locks.ReentrantLock;

import org.epics.pvData.misc.LinkedList;
import org.epics.pvData.misc.LinkedListCreate;
import org.epics.pvData.misc.LinkedListNode;
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
    private static LinkedListCreate<Requester> requesterListCreate = new LinkedListCreate<Requester>();
    private static LinkedListCreate<PVListener> pvListenerListCreate = new LinkedListCreate<PVListener>();
    private String recordName;
    private LinkedList<Requester> requesterList = requesterListCreate.create();
    private LinkedList<PVListener> pvListenerList = pvListenerListCreate.create();
    private ReentrantLock lock = new ReentrantLock();
    private static volatile int numberRecords = 0;
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
        if(message!=null && message.charAt(0)!='.') message = " " + message;
        if(requesterList.isEmpty()) {
            System.out.println(messageType.toString() + " " + message);
            return;
        }
        // no need to synchronize because record must be locked when this is called.
        LinkedListNode<Requester> node = requesterList.getHead();
        while(node!=null) {
            Requester requester = node.getObject();
            requester.message(message, messageType);
            node = requesterList.getNext(node);
        }
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVRecord#addRequester(org.epics.pvData.pv.Requester)
     */
    public void addRequester(Requester requester) {
        LinkedListNode<Requester> node = requesterListCreate.createNode(requester);
        // no need to synchronize because record must be locked when this is called.
        requesterList.addTail(node);
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVRecord#removeRequester(org.epics.pvData.pv.Requester)
     */
    public void removeRequester(Requester requester) {
        // no need to synchronize because record must be locked when this is called.
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
        // no need to synchronize because record must be locked when this is called.
        LinkedListNode<PVListener> node = pvListenerList.getHead();
        while(node!=null) {
            PVListener pvListener = node.getObject();
            pvListener.beginGroupPut(this);
            node = pvListenerList.getNext(node);
        }
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVRecord#endGroupPut()
     */
    public void endGroupPut() {
     // no need to synchronize because record must be locked when this is called.
        LinkedListNode<PVListener> node = pvListenerList.getHead();
        while(node!=null) {
            PVListener pvListener = node.getObject();
            pvListener.endGroupPut(this);
            node = pvListenerList.getNext(node);
        }
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVRecord#registerListener(org.epics.pvData.pv.PVListener)
     */
    public void registerListener(PVListener recordListener) {
        LinkedListNode<PVListener> node = pvListenerListCreate.createNode(recordListener);
        pvListenerList.addTail(node);
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
        LinkedListNode<PVListener> node = null;
        while((node = pvListenerList.removeHead())!=null) {
            PVListener pvListener = node.getObject();
            pvListener.unlisten(this);
        }
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
