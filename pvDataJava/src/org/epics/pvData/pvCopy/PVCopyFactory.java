/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.pvCopy;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.regex.Pattern;

import org.epics.pvData.factory.ConvertFactory;
import org.epics.pvData.factory.FieldFactory;
import org.epics.pvData.factory.PVDataFactory;
import org.epics.pvData.pv.Convert;
import org.epics.pvData.pv.Field;
import org.epics.pvData.pv.FieldCreate;
import org.epics.pvData.pv.PVArray;
import org.epics.pvData.pv.PVDataCreate;
import org.epics.pvData.pv.PVField;
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
     * @param pvRecord The PVREcord.
     * @param pvRequest A PVStructure which describes the set of fields of PVRecord that
     * should be mapped. See the packaged overview for details.
     * @param structureName TODO
     * @param shareData TODO
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
         * @see org.epics.pvData.pvCopy.PVCopy#initCopy(org.epics.pvData.pv.PVStructure, java.util.BitSet)
         */
        @Override
        public void initCopy(PVStructure copyPVStructure, BitSet bitSet) {
            pvRecord.lock();
            try {
                if(headNode.isStructure) {
                    updateStructureNode(copyPVStructure,(StructureNode)headNode,bitSet, true);
                } else {
                    updateRecordNode(copyPVStructure,(RecordNode)headNode,bitSet, true);
                }
            } finally {
                pvRecord.unlock();
            }
            bitSet.clear();
            bitSet.set(0);
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pvCopy.PVCopy#updateCopy(org.epics.pvData.pv.PVStructure, java.util.BitSet)
         */
        @Override
        public boolean updateCopy(PVStructure copyPVStructure, BitSet bitSet) {
            pvRecord.lock();
            try {
                if(headNode.isStructure) {
                    return updateStructureNode(copyPVStructure,(StructureNode)headNode,bitSet, true);
                } else {
                    return updateRecordNode(copyPVStructure,(RecordNode)headNode,bitSet, true);
                }
            } finally {
                pvRecord.unlock();
            }
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pvCopy.PVCopy#updateRecord(org.epics.pvData.pv.PVStructure, java.util.BitSet)
         */
        @Override
        public boolean updateRecord(PVStructure copyPVStructure, BitSet bitSet) {
            pvRecord.lock();
            try {
                if(headNode.isStructure) {
                    return updateStructureNode(copyPVStructure,(StructureNode)headNode,bitSet, false);
                } else {
                    return updateRecordNode(copyPVStructure,(RecordNode)headNode,bitSet, false);
                }
            } finally {
                pvRecord.unlock();
            }
        }
        
        private boolean updateStructureNode(PVStructure pvCopy,StructureNode structureNode,BitSet bitSet, boolean toCopy) {
            boolean atLeastOneBitSet = false;
            for(Node node : structureNode.nodes) {
                PVField pvField = pvCopy.getSubField(node.structureOffset);
                if(node.isStructure) {
                    if(updateStructureNode((PVStructure)pvField,(StructureNode)node,bitSet, toCopy)) {
                        atLeastOneBitSet = true;
                    }
                } else {
                    if(updateRecordNode(pvField,(RecordNode)node,bitSet, toCopy)) {
                        atLeastOneBitSet = true;
                    }
                }
            }
            return atLeastOneBitSet;
        }
       
        private boolean updateRecordNode(PVField pvCopy,RecordNode recordNode,BitSet bitSet, boolean toCopy) {
            if(pvCopy.getField().getType()==Type.structure) {
                return updateSubStructure((PVStructure)pvCopy,(PVStructure)(recordNode.recordPVField),bitSet, toCopy);
            } else {
                if(!(pvCopy.equals(recordNode.recordPVField))) {
                    if(toCopy) {
                        convert.copy(recordNode.recordPVField, pvCopy);
                    } else {
                        convert.copy(pvCopy,recordNode.recordPVField);
                    }
                    bitSet.set(pvCopy.getFieldOffset());
                    return true;
                }
                return false;
            }
        }
        private boolean updateSubStructure(PVStructure pvCopy,PVStructure pvRecord,BitSet bitSet, boolean toCopy) {
            PVField[] pvCopyFields = pvCopy.getPVFields();
            PVField[] pvRecordFields = pvRecord.getPVFields();
            int length = pvCopyFields.length;
            boolean atLeastOneBitSet = false;
            for(int i=0; i<length; i++) {
                PVField fromCopy = pvCopyFields[i];
                PVField fromRecord = pvRecordFields[i];
                if(fromCopy.getField().getType()==Type.structure) {
                    if(updateSubStructure((PVStructure)fromCopy,(PVStructure)fromRecord,bitSet, toCopy)) {
                        atLeastOneBitSet = true;
                    }
                } else {
                    if(!fromCopy.equals(fromRecord)) {
                        if(toCopy) {
                            convert.copy(fromRecord, fromCopy);
                        } else {
                            convert.copy(fromCopy, fromRecord);
                        }
                        bitSet.set(fromCopy.getFieldOffset());
                        atLeastOneBitSet = true;
                    }

                }
            }
            return atLeastOneBitSet;
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
                        fieldList.add(pvRecordField.getField());
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
                nextOffset = newNode.structureOffset + 1;
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
