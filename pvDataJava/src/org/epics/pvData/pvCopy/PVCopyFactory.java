/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.pvCopy;

import java.util.*;

import org.epics.pvData.pv.*;
import org.epics.pvData.factory.*;
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
     * @return The PVCopy interface.
     */
    public static PVCopy create(PVRecord pvRecord,PVStructure pvRequest) {
        PVCopyImpl impl = new PVCopyImpl(pvRecord);
        impl.init(pvRequest);
        return impl;
    }
    
    private static final FieldCreate fieldCreate = FieldFactory.getFieldCreate();
    private static final PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
    private static final Convert convert = ConvertFactory.getConvert();
    
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
        private Structure structure = null;
        private Node headNode = null;
        private PVStructure cacheInitStructure = null;
        
        private void init(PVStructure pvRequest) {
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
            structure = createStructure(pvRecord,pvRequest);
            cacheInitStructure = createPVStructure();
            StructureNode structureNode = new StructureNode();
            createStructureNodes(structureNode,pvRecord,pvRequest,cacheInitStructure);
            headNode = structureNode;
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
            return pvDataCreate.createPVStructure(null, "", structure);
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
        
        /* (non-Javadoc)
         * @see org.epics.pvData.pvCopy.PVCopy#checkBitSet(java.util.BitSet)
         */
        @Override
        public boolean checkBitSet(BitSet bitSet) {
            if(headNode.isStructure) {
                return checkBitSetStructureNode((StructureNode)headNode,bitSet);
            } else {
                return checkBitSetRecordPVField(((RecordNode)headNode).recordPVField,bitSet,headNode.structureOffset);
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
            if(!atLeastOneBitSet) return false;
            int offset = pvCopy.getFieldOffset();
            int nextOffset = pvCopy.getNextFieldOffset();
            int nextClear = bitSet.nextClearBit(offset+1);
            if(nextClear==-1 || nextClear>=nextOffset) {
                bitSet.set(offset);
                int next = offset;
                while(++next < nextOffset) bitSet.clear(next);
            }
            return true;
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
        
        private boolean checkBitSetStructureNode(StructureNode structureNode,BitSet bitSet) {
            boolean atLeastOneBitSet = false;
            boolean allBitsSet = true;
            for(Node node : structureNode.nodes) {
                if(node.isStructure) {
                    if(checkBitSetStructureNode((StructureNode)node,bitSet)) {
                        atLeastOneBitSet = true;
                    }
                } else {
                    if(checkBitSetRecordPVField(((RecordNode)node).recordPVField,bitSet,node.structureOffset)) {
                        atLeastOneBitSet = true;
                    }
                }
                if(!bitSet.get(node.structureOffset)) {
                    allBitsSet = false;
                }
            }
            if(allBitsSet) {
                int offset = structureNode.structureOffset;
                int nbits = structureNode.nfields;
                bitSet.clear(offset+1, offset+nbits+1);
                bitSet.set(offset);
            } else {
                bitSet.clear(structureNode.structureOffset);
            }
            return atLeastOneBitSet;
        }
        
        private boolean checkBitSetRecordPVField(PVField pvRecordField,BitSet bitSet,int initialOffset) {
            boolean atLeastOneBitSet = false;
            boolean allBitsSet = true;
            int offset = initialOffset;
            int nbits = pvRecordField.getNumberFields();
            while(offset<initialOffset + nbits) {
                PVField recordPVField = getRecordPVField(offset);
                int nbitsNow = recordPVField.getNumberFields();
                if(nbitsNow==1) {
                    if(bitSet.get(offset)) {
                        atLeastOneBitSet = true;
                    } else {
                        allBitsSet = false;
                    }
                    offset++;
                } else {
                    offset++;
                    PVStructure pvRecordStructure = (PVStructure)pvRecordField;
                    PVField[] pvRecordFields = pvRecordStructure.getPVFields();
                    for(PVField pvField: pvRecordFields) {
                        boolean result = checkBitSetRecordPVField(pvField,bitSet,offset);
                        if(result) {
                            atLeastOneBitSet = true;
                            if(!bitSet.get(offset)) {
                                allBitsSet = false;
                            }
                        } else {
                            allBitsSet = false;
                        }
                        offset += pvField.getNumberFields();
                    }
                }
            }
            if(allBitsSet) {
                if(nbits>1) {
                    bitSet.clear(initialOffset+1, initialOffset+nbits + 1);
                }
                bitSet.set(initialOffset);
            }
            return atLeastOneBitSet;
        }
    }
    
    
    private static Structure createStructure(PVStructure pvRecord,PVStructure pvFromRequest) {
        PVField[] pvFromFields = pvFromRequest.getPVFields();
        int length = pvFromFields.length;
        Field[] fields = new Field[length];
        int numberNull = 0;
        for(int i=0; i<length; i++) {
            PVField pvField = pvFromFields[i];
            if(pvField.getField().getType()==Type.structure) {
                fields[i] = createStructure(pvRecord,(PVStructure)pvField);
            } else {
                PVString pvString = (PVString)pvFromFields[i];
                PVField pvRecordField = pvRecord.getSubField(pvString.get());
                if(pvRecordField==null) {
                    fields[i] = null;
                    numberNull++;
                } else {
                    fields[i] = pvRecordField.getField();
                }
            }
        }
        if(numberNull>0) {
            int newLength = length-numberNull;
            Field[] newFields = new Field[newLength];
            int next = 0;
            for(Field field : fields) {
                if(field!=null) {
                    newFields[next++] = field;
                    
                }
            }
            length = newFields.length;
            fields = newFields;
        }
        return fieldCreate.createStructure(pvFromRequest.getField().getFieldName(), fields);
    }
    
    private static void createStructureNodes(StructureNode structureNode,
            PVStructure pvRecord,
            PVStructure pvFromRequest,
            PVStructure pvFromStructure)
    {
        PVField[] pvFromStructureFields = pvFromStructure.getPVFields();
        PVField[] pvFromRequestFields = pvFromRequest.getPVFields();
        int indRequestFields = 0;
        int length = pvFromStructureFields.length;
        structureNode.isStructure = true;
        structureNode.nodes = new Node[length];
        structureNode.structureOffset = pvFromStructure.getFieldOffset();
        structureNode.nfields = pvFromStructure.getNumberFields();
        int nextOffset = structureNode.structureOffset + 1;
        for(int i=0; i<length; i++) {
            PVField pvRequest = pvFromRequestFields[indRequestFields];
            if(pvRequest.getField().getType()==Type.structure) {
                indRequestFields++;
                PVStructure pvFromStruct = (PVStructure)pvFromStructureFields[i];
                StructureNode newNode = new StructureNode();
                createStructureNodes(newNode,pvRecord,(PVStructure)pvRequest,pvFromStruct);
                structureNode.nodes[i] = newNode;
                nextOffset = newNode.structureOffset + 1;
            } else {
                RecordNode recordNode = new RecordNode();
                while(true) {
                    PVString pvString = (PVString)pvFromRequestFields[indRequestFields];
                    PVField pvRecordField = pvRecord.getSubField(pvString.get());
                    indRequestFields++;
                    if(pvRecordField!=null) {
                        recordNode.recordPVField = pvRecordField;
                        structureNode.nodes[i] = recordNode;
                        recordNode.nfields = pvRecordField.getNumberFields();
                        recordNode.structureOffset = nextOffset;
                        nextOffset += recordNode.nfields;
                        break;
                    }
                }
            }
        }
    }
}
