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
 * @author mrk
 *
 */
public class PVCopyFactory {
    
    public static PVCopy create(PVRecord pvRecord,PVStructure request) {
        BaseNode head = createHead(pvRecord,request);
        Structure structure = createStructure(head);
        return new PVCopyImpl(pvRecord,head,structure);
    }
    
    private static final FieldCreate fieldCreate = FieldFactory.getFieldCreate();
    private static final PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
    
    private static class PVCopyImpl implements PVCopy{
        
        private PVCopyImpl(PVRecord pvRecord,BaseNode head,Structure structure) {
            this.pvRecord = pvRecord;
            this.head = head;
            this.structure = structure;
        }
        
        private PVRecord pvRecord;
        private BaseNode head = null;
        private Structure structure = null;
        
        
        /* (non-Javadoc)
         * @see org.epics.pvData.pvCopy.PVCopy#createPVStructure()
         */
        public PVStructure createPVStructure() {
            return pvDataCreate.createPVStructure(null, "", structure.getFields());
        }

        /* (non-Javadoc)
         * @see org.epics.pvData.pvCopy.PVCopy#getPVRecord()
         */
        public PVRecord getPVRecord() {
            return pvRecord;
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pvCopy.PVCopy#getStructure()
         */
        public Structure getStructure() {
            return structure;
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pvCopy.PVCopy#getOffset(org.epics.pvData.pv.PVField)
         */
        public int getOffset(PVField recordPVField) {
            BaseNode node = getOffset(recordPVField,head);
            return ((node==null) ? -1 : node.offset);
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pvCopy.PVCopy#getOffset(org.epics.pvData.pv.PVStructure, org.epics.pvData.pv.PVField)
         */
        public int getOffset(PVStructure recordPVStructure,PVField recordPVField) {
            BaseNode baseNode = getOffset(recordPVStructure,head);
            if(baseNode==null) return -1;
            if(baseNode.isStructure) return -1;
            return getOffset(recordPVStructure,recordPVField,baseNode.offset);
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pvCopy.PVCopy#getPVField(int)
         */
        public PVField getPVField(int offset) {
            return getPVField(offset,head);
        }
        
        
        private PVField getPVField(int offset,BaseNode head) {
            if(!head.isStructure) {
                RecordNode recordNode = (RecordNode)head;
                PVField pvField = recordNode.pvField;
                int baseOffset = recordNode.offset;
                if(baseOffset==offset) return pvField;
                if(offset<baseOffset) return null;
                int numberFields = recordNode.numberFields;
                if(offset>(baseOffset+numberFields)) return null;
                if(pvField.getField().getType()!=Type.structure) return null;
                return getPVField(pvField,offset-baseOffset);
            } else {
                StructureNode structureNode = (StructureNode)head;
                BaseNode[] baseNodes = structureNode.nodes;
                int length = baseNodes.length;
                for(int i=0; i<length ; i++) {
                    BaseNode baseNode = baseNodes[i];
                    int baseOffset = baseNode.offset;
                    if(offset<baseOffset) continue;
                    int numberFields = baseNode.numberFields;
                    if(offset<(baseOffset+numberFields)) {
                        return getPVField(offset,baseNode);
                    }
                }
            }
            return null;
        }
        
       
        
        private PVField getPVField(PVField startField,int offset) {
            if(startField.getField().getType()!=Type.structure) {
                return startField;
            }
            PVStructure pvStructure = (PVStructure)startField;
            PVField[] pvFields = pvStructure.getPVFields();
            int number = 0;
            for(PVField pvField: pvFields) {
                if(number>offset) return null;
                if(number==offset) return pvField;
                if(pvField.getField().getType()!=Type.structure) {
                    number++;
                    continue;
                }
                PVStructure next = (PVStructure)pvField;
                int total = addNumFields(next.getStructure(),1);
                if(offset<(total+number)) {
                    return getPVField(next,offset-number);
                }
                number = number + total;
            }
            return null;
        }
        
        private BaseNode getOffset(PVField recordPVField,BaseNode head) {
            if(!head.isStructure) {
                RecordNode recordNode = (RecordNode)head;
                if(recordNode.pvField==recordPVField) return recordNode;
                return null;
            }
            StructureNode structureNode = (StructureNode)head;
            BaseNode[] nodes = structureNode.nodes;
            for(BaseNode node : nodes) {
                if(!node.isStructure) {
                    RecordNode recordNode = (RecordNode)node;
                    if(recordNode.pvField==recordPVField) return node;
                } else {
                    BaseNode ret = getOffset(recordPVField,node);
                    if(ret!=null) return ret;
                }
            }
            return null;
        }
        
        private int getOffset(PVStructure recordPVStructure,PVField recordPVField,int offset) {
            PVField[] pvFields = recordPVStructure.getPVFields();
            for(PVField pvField : pvFields) {
                offset++;
                if(pvField==recordPVField) return offset;
                if(pvField.getField().getType()==Type.structure) {
                    offset = getOffset((PVStructure)pvField,recordPVField,offset);
                }
            }
            return offset;
        }
    }
    
    private static class BaseNode {
        boolean isStructure;
        int offset;
        int numberFields;
        String fieldName;
    }
    
    private static class StructureNode extends BaseNode {
        BaseNode[] nodes;
    }
    
    private static class RecordNode extends BaseNode {
        PVField pvField;
    }
    
    private static int addNumFields(Structure structure,int num) {
        num++;
        Field[] fields = structure.getFields();
        for(Field field : fields) {
            if(field.getType()==Type.structure) {
                num = addNumFields((Structure)field,num);
            } else {
                num++;
            }
        }
        return num;
    }
    
    private static BaseNode  createHead(PVRecord pvRecord,PVStructure request) {
        PVField[] pvFields = request.getPVFields();
        int length = pvFields.length;
        if(length==0) {
            RecordNode node = new RecordNode();
            node.isStructure = false;
            node.offset = 0;
            node.fieldName = "";
            node.numberFields = addNumFields(pvRecord.getStructure(),0);
            node.pvField = pvRecord.getPVStructure();
            return node;
        }
        StructureNode head = new StructureNode();
        head.isStructure = true;
        head.offset = 0;
        head.fieldName = "";
        head.numberFields = 0;
        initStructureNode(pvRecord,request,(StructureNode)head);
        return head;
    }
    
    private static void initStructureNode(PVRecord pvRecord,PVStructure pvStructure,StructureNode head) {
        PVField[] pvFields = pvStructure.getPVFields();
        int length = pvFields.length;
        int nfields = 0;
        for(int i=0; i<length; i++) {
            PVField pvField = pvFields[i];
            if(pvField.getField().getType()==Type.scalar) {
                PVString pvString = (PVString)pvField;
                PVField pvRecordField = pvRecord.getSubField(pvString.get());
                if(pvRecordField==null) continue;
                nfields++;
            } else {
                nfields++;
            }
        }
        head.numberFields += 1; // include the structure itself
        head.nodes = new BaseNode[nfields];
        if(nfields==0) return;
        int indfield = 0;
        for(int i=0; i<length; i++) {
            PVField pvField = pvFields[i];
            if(pvField.getField().getType()==Type.scalar) {
                PVString pvString = (PVString)pvField;
                PVField pvRecordField = pvRecord.getSubField(pvString.get());
                if(pvRecordField==null) continue;
                RecordNode node = new RecordNode();
                node.isStructure = false;
                node.offset = head.offset + head.numberFields;
                node.pvField = pvRecordField;
                node.fieldName = pvField.getField().getFieldName();
                if(pvRecordField.getField().getType()!=Type.structure) {
                    node.numberFields = 1;
                    head.numberFields +=1;
                } else {
                    Structure structure = (Structure)pvRecordField.getField();
                    node.numberFields = addNumFields(structure,0);
                    head.numberFields += node.numberFields;
                }
                head.nodes[indfield++] = node;
                continue;
            }
            StructureNode newHead = new StructureNode();
            newHead.isStructure = true;
            newHead.offset = head.offset + head.numberFields;
            newHead.fieldName = pvField.getField().getFieldName();
            initStructureNode(pvRecord,(PVStructure)pvField,newHead);
            head.nodes[indfield++] = newHead;
            head.numberFields += newHead.numberFields;
        }
        return;
    }
    
    private static Structure createStructure(BaseNode node) {
        if(!node.isStructure) {
            RecordNode recordNode = (RecordNode)node;
            return (Structure)recordNode.pvField.getField();
        }
        StructureNode head = (StructureNode)node;
        Field[] fields = new Field[head.nodes.length];
        
        initStructure(head,fields);
        Structure structure = fieldCreate.createStructure("", fields);
        return structure;
    }
    
    private static void initStructure(StructureNode head,Field[] fields) {
        BaseNode[] nodes = head.nodes;
        int length = fields.length;
        for(int i=0; i<length; i++) {
            BaseNode node = nodes[i];
            if(!node.isStructure) {
                RecordNode recordNode = (RecordNode)node;
                Field field = recordNode.pvField.getField();
                Field newField = null;
                switch(field.getType()) {
                case scalar:
                    newField = fieldCreate.createScalar(node.fieldName, ((Scalar)field).getScalarType());
                    break;
                case scalarArray:
                    newField = fieldCreate.createArray(node.fieldName, ((Array)field).getElementType());
                    break;
                case structure:
                    newField = fieldCreate.createStructure(node.fieldName,((Structure)field).getFields());
                }
                fields[i] = newField;
            } else {
                StructureNode newHead = (StructureNode)node;
                Field[] newFields = new Field[newHead.nodes.length];
                initStructure(newHead,newFields);
                fields[i] = fieldCreate.createStructure(newHead.fieldName, newFields);
                
            }
        }
    }
}
