/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.pvCopy;

import java.util.ArrayList;
import java.util.regex.Pattern;

import org.epics.pvData.factory.ConvertFactory;
import org.epics.pvData.factory.FieldFactory;
import org.epics.pvData.factory.PVDataFactory;
import org.epics.pvData.misc.BitSet;
import org.epics.pvData.pv.Convert;
import org.epics.pvData.pv.Field;
import org.epics.pvData.pv.FieldCreate;
import org.epics.pvData.pv.PVArray;
import org.epics.pvData.pv.PVDataCreate;
import org.epics.pvData.pv.PVField;
import org.epics.pvData.pv.PVListener;
import org.epics.pvData.pv.PVRecord;
import org.epics.pvData.pv.PVScalar;
import org.epics.pvData.pv.PVString;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.Structure;
import org.epics.pvData.pv.Type;
/**
 * A Factory that creates a PVCopy interface which describes a subset of the fields
 * within a PVRecord. It can be used by Channel Access servers.
 * @author mrk
 *
 */
public class PVCopyFactory {
    
    /**
     * Map a subset of the fields within a PVRecord.
     * @param pvRecord The PVRecord.
     * @param pvRequest A PVStructure which describes the set of fields of PVRecord that
     * should be mapped. See the packaged overview for details.
     * @param structureName The name to give to the Structure
     * that describes the subset of the fields of PVRecord that are actually mapped.
     * @param shareData Should the PVStructure created by calling PVCopy.createPVStructure share the data from
     * PVRecord of should it keep a separate copy.
     * @return The PVCopy interface.
     */
    public static PVCopy create(PVRecord pvRecord,PVStructure pvRequest,String structureName,boolean shareData) {
        PVCopyImpl impl = new PVCopyImpl(pvRecord);
        impl.init(pvRequest,structureName,shareData);
        return impl;
    }
    
    private static final FieldCreate fieldCreate = FieldFactory.getFieldCreate();
    private static final PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
    private static final Convert convert = ConvertFactory.getConvert();
    private static final Pattern commaPattern = Pattern.compile("[,]");
    
    private static class Node {
        boolean isStructure;
        int structureOffset;
        int nfields;
    }
    
    private static class RecordNode extends Node{
        PVField recordPVField;
    }
    
    private static class StructureNode extends Node {
        Node[] nodes;
    }
    
    private static class PVCopyImpl implements PVCopy{
        
        private PVCopyImpl(PVRecord pvRecord) {
            this.pvRecord = pvRecord;
        }
        
        private PVRecord pvRecord;
        private boolean shareData = false;
        private Structure structure = null;
        private Node headNode = null;
        private PVStructure cacheInitStructure = null;
        
        private void init(PVStructure pvRequest,String structureName,boolean shareData) {
            this.shareData = shareData;
            if(pvRequest.getPVFields().length==0) {
                // asking for entire record is special case.
                structure = pvRecord.getStructure();
                RecordNode recordNode = new RecordNode();
                headNode = recordNode;
                recordNode.isStructure = false;
                recordNode.structureOffset = 0;
                recordNode.recordPVField = pvRecord;
                recordNode.nfields = pvRecord.getNumberFields();
                return;
            }
            structure = createStructure(pvRecord,pvRequest,structureName);
            cacheInitStructure = createPVStructure();
            StructureNode structureNode = new StructureNode();
            createStructureNodes(structureNode,pvRecord,pvRequest,cacheInitStructure);
            headNode = structureNode;
            referenceImmutable(cacheInitStructure,headNode);
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pvCopy.PVCopy#getPVRecord()
         */
        @Override
        public PVRecord getPVRecord() {
            return pvRecord;
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pvCopy.PVCopy#getStructure()
         */
        @Override
        public Structure getStructure() {
            return structure;
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pvCopy.PVCopy#createPVStructure()
         */
        @Override
        public PVStructure createPVStructure() {
            if(cacheInitStructure!=null) {
                PVStructure save = cacheInitStructure;
                cacheInitStructure = null;
                return save;
            }
            PVStructure pvStructure =  pvDataCreate.createPVStructure(null, "", structure);
            if(headNode!=null) {
                referenceImmutable(pvStructure,headNode);
            }
            return pvStructure;
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pvCopy.PVCopy#isShared()
         */
        @Override
        public boolean isShared() {
            return shareData;
        }
        
        private void referenceImmutable(PVField pvField,Node node) {
            if(node.isStructure) {
                StructureNode structureNode = (StructureNode)node;
                Node[] nodes = structureNode.nodes;
                PVStructure pvStructure = (PVStructure)pvField;
                for(Node nextNode : nodes) {
                    referenceImmutable(pvStructure.getSubField(nextNode.structureOffset),nextNode);
                }
            } else {
                RecordNode recordNode = (RecordNode)node;
                if(shareData) {
                    makeShared(pvField,recordNode.recordPVField);
                } else {
                    referenceImmutable(pvField,recordNode.recordPVField);
                }
            }
        }
        
        private void referenceImmutable(PVField copyPVField,PVField recordPVField) {
            if(recordPVField.getField().getType()==Type.structure) {
                PVField[] copyPVFields = ((PVStructure)copyPVField).getPVFields();
                PVField[] recordPVFields = ((PVStructure)recordPVField).getPVFields();
                for(int i=0; i<copyPVFields.length; i++) {
                    referenceImmutable(copyPVFields[i],recordPVFields[i]);
                }
                return;
            }
            if(recordPVField.isImmutable()) convert.copy(recordPVField, copyPVField);
        }
        
        private void makeShared(PVField copyPVField,PVField recordPVField) {
            switch(recordPVField.getField().getType()) {
            case structure: {
                PVField[] copyPVFields = ((PVStructure)copyPVField).getPVFields();
                PVField[] recordPVFields = ((PVStructure)recordPVField).getPVFields();
                for(int i=0; i<copyPVFields.length; i++) {
                    makeShared(copyPVFields[i],recordPVFields[i]);
                }
                break;
            }
            case scalar:
                PVShareFactory.replace((PVScalar)copyPVField,(PVScalar)recordPVField);
                break;
            case scalarArray:
                PVShareFactory.replace((PVArray)copyPVField,(PVArray)recordPVField);
            }
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pvCopy.PVCopy#getOffset(org.epics.pvData.pv.PVField)
         */
        @Override
        public int getCopyOffset(PVField recordPVField) {
           if(!headNode.isStructure) {
               RecordNode recordNode = (RecordNode)headNode;
               if(recordNode.recordPVField==recordPVField) return headNode.structureOffset;
               return -1;
           }
           RecordNode recordNode = getCopyOffset((StructureNode)headNode,recordPVField);
           if(recordNode!=null) return recordNode.structureOffset;
           return -1;
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pvCopy.PVCopy#getOffset(org.epics.pvData.pv.PVStructure, org.epics.pvData.pv.PVField)
         */
        @Override
        public int getCopyOffset(PVStructure recordPVStructure,PVField recordPVField) {
            RecordNode recordNode = null;
            if(!headNode.isStructure) {
                recordNode = (RecordNode)headNode;
                if(recordNode.recordPVField!=recordPVStructure) return -1;
            } else {
                recordNode = getCopyOffset((StructureNode)headNode,recordPVStructure);
            }
            if(recordNode==null) return -1;
            int diff = recordPVField.getFieldOffset() - recordPVStructure.getFieldOffset();
            return recordNode.structureOffset + diff;
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pvCopy.PVCopy#getPVField(int)
         */
        @Override
        public PVField getRecordPVField(int structureOffset) {
            RecordNode recordNode = null;
            if(!headNode.isStructure) {
                recordNode = (RecordNode)headNode;
            } else {
                recordNode = getRecordNode((StructureNode)headNode,structureOffset);
            }
            if(recordNode==null) return null;
            int diff = structureOffset - recordNode.structureOffset;
            PVField pvField = recordNode.recordPVField;
            if(diff==0) return pvField;
            PVStructure pvStructure = (PVStructure)pvField;
            return pvStructure.getSubField(pvField.getFieldOffset() + diff);
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pvCopy.PVCopy#initCopy(org.epics.pvData.pv.PVStructure, org.epics.pvData.misc.BitSet)
         */
        @Override
        public void initCopy(PVStructure copyPVStructure, BitSet bitSet, boolean lockRecord) {
            bitSet.clear();
            bitSet.set(0);
            updateCopyFromBitSet(copyPVStructure,bitSet,lockRecord);
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pvCopy.PVCopy#updateCopySetBitSet(org.epics.pvData.pv.PVStructure, org.epics.pvData.misc.BitSet, boolean)
         */
        @Override
        public void updateCopySetBitSet(PVStructure copyPVStructure,BitSet bitSet, boolean lockRecord)
        {
            if(lockRecord) pvRecord.lock();
            try {
                if(headNode.isStructure) {
                    updateStructureNodeSetBitSet(copyPVStructure,(StructureNode)headNode,bitSet);
                } else {
                    RecordNode recordNode = (RecordNode)headNode;
                    updateSubFieldSetBitSet(copyPVStructure,recordNode.recordPVField,bitSet);
                }
            } finally {
                if(lockRecord) pvRecord.unlock();
            }
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pvCopy.PVCopy#updateCopyFromBitSet(org.epics.pvData.pv.PVStructure, org.epics.pvData.misc.BitSet, boolean)
         */
        @Override
        public void updateCopyFromBitSet(PVStructure copyPVStructure,BitSet bitSet, boolean lockRecord) {
            boolean doAll = bitSet.get(0);
            if(lockRecord) pvRecord.lock();
            try {
                if(headNode.isStructure) {
                    updateStructureNodeFromBitSet(copyPVStructure,(StructureNode)headNode,bitSet, true,doAll);
                } else {
                    RecordNode recordNode = (RecordNode)headNode;
                    updateSubFieldFromBitSet(copyPVStructure,recordNode.recordPVField,bitSet, true,doAll);
                }
            } finally {
                if(lockRecord) pvRecord.unlock();
            }
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pvCopy.PVCopy#updateRecord(org.epics.pvData.pv.PVStructure, org.epics.pvData.misc.BitSet, boolean)
         */
        @Override
        public void updateRecord(PVStructure copyPVStructure, BitSet bitSet,boolean lockRecord) {
            boolean doAll = bitSet.get(0);
            if(lockRecord) pvRecord.lock();
            try {
                pvRecord.beginGroupPut();
                if(headNode.isStructure) {
                    updateStructureNodeFromBitSet(copyPVStructure,(StructureNode)headNode,bitSet, false,doAll);
                } else {
                    RecordNode recordNode = (RecordNode)headNode;
                    updateSubFieldFromBitSet(copyPVStructure,recordNode.recordPVField,bitSet, false,doAll);
                }
                pvRecord.endGroupPut();
            } finally {
                if(lockRecord) pvRecord.unlock();
            }
        }
         
        /* (non-Javadoc)
         * @see org.epics.pvData.pvCopy.PVCopy#createPVCopyMonitor(org.epics.pvData.pvCopy.PVCopyMonitorRequester)
         */
        @Override
        public PVCopyMonitor createPVCopyMonitor(PVCopyMonitorRequester pvCopyMonitorRequester) {
            return new CopyMonitor(pvCopyMonitorRequester);
        }
        
        private void updateStructureNodeSetBitSet(PVStructure pvCopy,StructureNode structureNode,BitSet bitSet) {
            for(int i=0; i<structureNode.nodes.length; i++) {
                Node node = structureNode.nodes[i];
                PVField pvField = pvCopy.getSubField(node.structureOffset);
                if(node.isStructure) {
                    updateStructureNodeSetBitSet((PVStructure)pvField,(StructureNode)node,bitSet); 
                } else {
                    RecordNode recordNode = (RecordNode)node;
                    updateSubFieldSetBitSet(pvField,recordNode.recordPVField,bitSet);
                }
            }
        }
        
        private void updateSubFieldSetBitSet(PVField pvCopy,PVField pvRecord,BitSet bitSet) {
            if(pvCopy.getField().getType()!=Type.structure) {
                if(pvCopy.equals(pvRecord)) return;
                convert.copy(pvRecord, pvCopy);
                bitSet.set(pvCopy.getFieldOffset());
                return;
            }
            PVStructure pvCopyStructure = (PVStructure)pvCopy;
            PVStructure pvRecordStructure = (PVStructure)pvRecord;
            PVField[] pvCopyFields = pvCopyStructure.getPVFields();
            PVField[] pvRecordFields = pvRecordStructure.getPVFields();
            int length = pvCopyFields.length;
            for(int i=0; i<length; i++) {
                updateSubFieldSetBitSet(pvCopyFields[i],pvRecordFields[i],bitSet);
            }
        }
        
        private void updateStructureNodeFromBitSet(PVStructure pvCopy,StructureNode structureNode,BitSet bitSet,boolean toCopy,boolean doAll) {
            int offset = structureNode.structureOffset;
            int nextSet = bitSet.nextSetBit(offset);
            if(nextSet==-1) return;
            if(offset>=pvCopy.getNextFieldOffset()) return;
            if(!doAll) doAll = bitSet.get(offset);
            Node[] nodes = structureNode.nodes;
            for(int i=0; i<nodes.length; i++) {
                Node node = nodes[i];
                PVField pvField = pvCopy.getSubField(node.structureOffset);
                if(node.isStructure) {
                    StructureNode subStructureNode = (StructureNode)node;
                    updateStructureNodeFromBitSet((PVStructure)pvField,subStructureNode,bitSet,toCopy,doAll);
                } else {
                    RecordNode recordNode = (RecordNode)node;
                    updateSubFieldFromBitSet(pvField,recordNode.recordPVField,bitSet,toCopy,doAll);
                }
            }
        }
        
       
        private void updateSubFieldFromBitSet(PVField pvCopy,PVField pvRecord,BitSet bitSet,boolean toCopy,boolean doAll) {
            if(!doAll) {
                doAll = bitSet.get(pvCopy.getFieldOffset());
            }
            if(!doAll) {
                int offset = pvCopy.getFieldOffset();
                int nextSet = bitSet.nextSetBit(offset);
                if(nextSet==-1) return;
                if(nextSet>=pvCopy.getNextFieldOffset()) return;
            }
            if(pvCopy.getField().getType()==Type.structure) {
                PVStructure pvCopyStructure = (PVStructure)pvCopy;
                PVField[] pvCopyFields = pvCopyStructure.getPVFields();
                PVStructure pvRecordStructure = (PVStructure)pvRecord;
                PVField[] pvRecordFields = pvRecordStructure.getPVFields();
                for(int i=0; i<pvCopyFields.length; i++) {
                    updateSubFieldFromBitSet(pvCopyFields[i],pvRecordFields[i],bitSet,toCopy,doAll);
                }
            } else {
                if(toCopy) {
                    convert.copy(pvRecord, pvCopy);
                } else {
                    convert.copy(pvCopy, pvRecord);
                }
            }
        }
        

        private RecordNode getCopyOffset(StructureNode structureNode,PVField recordPVField) {
            for(Node node : structureNode.nodes) {
                if(!node.isStructure) {
                    RecordNode recordNode = (RecordNode)node;
                    if(recordNode.recordPVField==recordPVField) return recordNode;
                } else {
                    StructureNode subNode = (StructureNode)node;
                    RecordNode recordNode = getCopyOffset(subNode,recordPVField);
                    if(recordNode!=null) return recordNode;
                }
            }
            return null;
        }
        
        private RecordNode getRecordNode(StructureNode structureNode,int structureOffset) {
            for(Node node : structureNode.nodes) {
                if(structureOffset>=(node.structureOffset + node.nfields)) continue;
                if(!node.isStructure) return (RecordNode)node; 
                    StructureNode subNode = (StructureNode)node;
                    return  getRecordNode(subNode,structureOffset);
            }
            return null;
        }
        
        private class CopyMonitor implements PVCopyMonitor, PVListener {
            private PVCopyMonitorRequester pvCopyMonitorRequester;
            private boolean notifyOnly = true;
            private BitSet changeBitSet = null;
            private BitSet overrunBitSet = null;
            private boolean isGroupPut = false;
            private boolean dataChanged = false;
            
            private CopyMonitor(PVCopyMonitorRequester pvCopyMonitorRequester) {
                this.pvCopyMonitorRequester = pvCopyMonitorRequester;
            }
            /* (non-Javadoc)
             * @see org.epics.pvData.pvCopy.PVCopyMonitor#startMonitoring()
             */
            @Override
            public void startMonitoring() {
                notifyOnly = true;
                pvRecord.registerListener(this);
                addListener(headNode);
            }
            /* (non-Javadoc)
             * @see org.epics.pvData.pvCopy.PVCopyMonitor#startMonitoring(org.epics.pvData.pv.PVStructure, org.epics.pvData.misc.BitSet, org.epics.pvData.misc.BitSet)
             */
            @Override
            public void startMonitoring(BitSet changeBitSet, BitSet overrunBitSet) {
                notifyOnly = false;
                this.changeBitSet = changeBitSet;
                this.overrunBitSet = overrunBitSet;
                isGroupPut = false;
                pvRecord.registerListener(this);
                addListener(headNode);
                pvRecord.lock();
                try {
                    changeBitSet.clear();
                    overrunBitSet.clear();
                    changeBitSet.set(0);
                    pvCopyMonitorRequester.dataChanged();
                } finally {
                    pvRecord.unlock();
                }
            }

            /* (non-Javadoc)
             * @see org.epics.pvData.pvCopy.PVCopyMonitor#stopMonitoring()
             */
            @Override
            public void stopMonitoring() {
                pvRecord.unregisterListener(this);
            }
            /* (non-Javadoc)
             * @see org.epics.pvData.pvCopy.PVCopyMonitor#updateCopy(org.epics.pvData.misc.BitSet, org.epics.pvData.misc.BitSet, boolean)
             */
            @Override
            public void switchBitSets(BitSet newChangeBitSet,BitSet newOverrunBitSet, boolean lockRecord) {
                if(notifyOnly) {
                    throw new IllegalStateException("Can not switchBitSets since notifyOnly is true");
                }
                if(lockRecord) pvRecord.lock();
                try {
                    changeBitSet = newChangeBitSet;
                    overrunBitSet = newOverrunBitSet;
                } finally {
                    if(lockRecord) pvRecord.unlock();
                }
            }
            /* (non-Javadoc)
             * @see org.epics.pvData.pv.PVListener#beginGroupPut(org.epics.pvData.pv.PVRecord)
             */
            @Override
            public void beginGroupPut(PVRecord pvRecord) {
                isGroupPut = true;
                dataChanged = false;
            }
            /* (non-Javadoc)
             * @see org.epics.pvData.pv.PVListener#dataPut(org.epics.pvData.pv.PVField)
             */
            @Override
            public void dataPut(PVField pvField) {
                if(!notifyOnly) {
                    Node node = findNode(headNode,pvField);
                    if(node==null) {
                        throw new IllegalStateException("Logic error");
                    }
                    int offset = node.structureOffset;
                    synchronized(changeBitSet) {
                        boolean wasSet = changeBitSet.get(offset);
                        if(wasSet) overrunBitSet.set(offset);
                        changeBitSet.set(offset);
                    }
                }
                if(!isGroupPut) pvCopyMonitorRequester.dataChanged();
                dataChanged = true;
            }
            /* (non-Javadoc)
             * @see org.epics.pvData.pv.PVListener#dataPut(org.epics.pvData.pv.PVStructure, org.epics.pvData.pv.PVField)
             */
            @Override
            public void dataPut(PVStructure requested, PVField pvField) {
                if(!notifyOnly) {
                    Node node = findNode(headNode,requested);
                    if(node==null || node.isStructure) {
                        throw new IllegalStateException("Logic error");
                    }
                    RecordNode recordNode = (RecordNode)node;
                    int offset = recordNode.structureOffset
                    + (pvField.getFieldOffset() - recordNode.recordPVField.getFieldOffset());
                    synchronized(changeBitSet) {
                        boolean wasSet = changeBitSet.get(offset);
                        if(wasSet) overrunBitSet.set(offset);
                        changeBitSet.set(offset);
                    }
                }
                if(!isGroupPut) pvCopyMonitorRequester.dataChanged();
                dataChanged = true;
            }
            /* (non-Javadoc)
             * @see org.epics.pvData.pv.PVListener#endGroupPut(org.epics.pvData.pv.PVRecord)
             */
            @Override
            public void endGroupPut(PVRecord pvRecord) {
                isGroupPut = false;
                if(dataChanged) {
                    dataChanged = false;
                    pvCopyMonitorRequester.dataChanged();
                }
            }

            /* (non-Javadoc)
             * @see org.epics.pvData.pv.PVListener#unlisten(org.epics.pvData.pv.PVRecord)
             */
            @Override
            public void unlisten(PVRecord pvRecord) {
                pvCopyMonitorRequester.unlisten();
            }
            
            private void addListener(Node node) {
                if(!node.isStructure) {
                    PVField pvRecordField = getRecordPVField(node.structureOffset);
                    pvRecordField.addListener(this);
                    return;
                }
                StructureNode structureNode = (StructureNode)node;
                for(int i=0; i<structureNode.nodes.length; i++) {
                    addListener(structureNode.nodes[i]);
                }
            }
            
            private Node findNode(Node node,PVField pvField) {
                if(!node.isStructure) {
                    RecordNode recordNode = (RecordNode)node;
                    if(recordNode.recordPVField==pvField) return node;
                    return null;
                }
                StructureNode structureNode = (StructureNode)node;
                for(int i=0; i<structureNode.nodes.length; i++) {
                    node = findNode(structureNode.nodes[i],pvField);
                    if(node!=null) return node;
                }
                return null;
            }   
            
        }
    }
    
    private static Structure createStructure(PVStructure pvRecord,PVStructure pvFromRequest,String fieldName) {
        PVField[] pvFromFields = pvFromRequest.getPVFields();
        int length = pvFromFields.length;
        ArrayList<Field> fieldList = new ArrayList<Field>(length);
        for(int i=0; i<length; i++) {
            PVField pvField = pvFromFields[i];
            if(pvField.getField().getType()==Type.structure) {
                fieldList.add(createStructure(pvRecord,(PVStructure)pvField,pvField.getField().getFieldName()));
            } else {
                PVString pvString = (PVString)pvFromFields[i];
                if(pvString.getField().getFieldName().equals("fieldList")) {
                    String[] fieldNames = commaPattern.split(pvString.get());
                    for(int j=0; j<fieldNames.length; j++) {
                        PVField pvRecordField = pvRecord.getSubField(fieldNames[j]);;
                        if(pvRecordField!=null) {
                            fieldList.add(pvRecordField.getField());
                        }
                    }
                } else {
                    PVField pvRecordField = pvRecord.getSubField(pvString.get());
                    if(pvRecordField!=null) {
                        Field field = fieldCreate.create(pvField.getField().getFieldName(),pvRecordField.getField());
                        fieldList.add(field);
                    }
                }
            }
        }
        Field[] fields = new Field[fieldList.size()];
        fields = fieldList.toArray(fields);
        return fieldCreate.createStructure(fieldName, fields);
    }
    
    private static void createStructureNodes(StructureNode structureNode,
            PVStructure pvRecord,
            PVStructure pvFromRequest,
            PVStructure pvFromStructure)
    {
        PVField[] pvFromStructureFields = pvFromStructure.getPVFields();
        PVField[] pvFromRequestFields = pvFromRequest.getPVFields();
        int length = pvFromStructureFields.length;
        structureNode.isStructure = true;
        structureNode.nodes = new Node[length];
        structureNode.structureOffset = pvFromStructure.getFieldOffset();
        structureNode.nfields = pvFromStructure.getNumberFields();
        int nextOffset = structureNode.structureOffset + 1;
        int index = 0;
        for(int indRequestFields= 0; indRequestFields <pvFromRequestFields.length;indRequestFields++) {
            PVField pvRequest = pvFromRequestFields[indRequestFields];
            if(pvRequest.getField().getType()==Type.structure) {
                PVStructure pvFromStruct = (PVStructure)pvFromStructureFields[index];
                StructureNode newNode = new StructureNode();
                createStructureNodes(newNode,pvRecord,(PVStructure)pvRequest,pvFromStruct);
                structureNode.nodes[index++] = newNode;
                nextOffset = pvFromStruct.getNextFieldOffset();
                continue;
            } else {
                PVString pvString = (PVString)pvFromRequestFields[indRequestFields];
                if(pvString.getField().getFieldName().equals("fieldList")) {
                    String[] fieldNames = commaPattern.split(pvString.get());
                    for(int j=0; j<fieldNames.length; j++) {
                        PVField pvRecordField = pvRecord.getSubField(fieldNames[j]);;
                        if(pvRecordField!=null) {
                            RecordNode recordNode = new RecordNode();
                            recordNode.recordPVField = pvRecordField;
                            structureNode.nodes[index++] = recordNode;
                            recordNode.nfields = pvRecordField.getNumberFields();
                            recordNode.structureOffset = nextOffset;
                            nextOffset += recordNode.nfields;
                        }
                    }
                } else {
                    PVField pvRecordField = pvRecord.getSubField(pvString.get());
                    if(pvRecordField!=null) {
                        RecordNode recordNode = new RecordNode();
                        recordNode.recordPVField = pvRecordField;
                        structureNode.nodes[index++] = recordNode;
                        recordNode.nfields = pvRecordField.getNumberFields();
                        recordNode.structureOffset = nextOffset;
                        nextOffset += recordNode.nfields;
                    }
                }
            }
        }
    }
}
