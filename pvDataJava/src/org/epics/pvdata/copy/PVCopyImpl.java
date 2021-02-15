/*
 * The License for this software can be found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.copy;

import java.util.ArrayList;

import org.epics.pvdata.factory.ConvertFactory;
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.misc.BitSet;
import org.epics.pvdata.pv.Convert;
import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.Structure;
import org.epics.pvdata.pv.Type;
/**
 * @author mrk
 *
 */
class PVCopyImpl implements PVCopy{
    static PVCopy create(
        PVStructure pvMaster,
        PVStructure pvRequest,
        String structureName)
    {
        if(structureName!=null && structureName.length()>0) {
            if(pvRequest.getPVFields().length>0) {
                pvRequest = pvRequest.getStructureField(structureName);
                if(pvRequest==null) return null;
            }
        }
        PVStructure pvStruct = pvRequest;
        if(pvRequest.getSubField("field")!=null) {
            pvStruct = pvRequest.getStructureField("field");
        }
        PVCopyImpl impl = new PVCopyImpl(pvMaster);
        boolean result = impl.init(pvStruct);
        if(!result) return null;
        //System.out.println("\npvCopy" + impl.dump());
        return impl;
    }

    private static final FieldCreate fieldCreate = FieldFactory.getFieldCreate();
    private static final PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
    private static final Convert convert = ConvertFactory.getConvert();

    static class Node {
        PVField masterPVField;
        boolean isStructure = false;
        int structureOffset = 0; // In the copy
        int nfields = 0;
        PVStructure options = null;
    }


    static class StructureNode extends Node {
        Node[] nodes;
    }

    private final PVStructure pvMaster;
    private Structure structure = null;
    private Node headNode = null;
    private PVStructure cacheInitStructure = null;

    private PVCopyImpl(PVStructure pvMaster) {
        this.pvMaster = pvMaster;
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.copy.PVCopy#getPVMaster()
     */
    public PVStructure getPVMaster() {
        return pvMaster;
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.copy.PVCopy#traverseMaster(org.epics.pvdata.copy.PVCopyTraverseMasterCallback)
     */
    public void traverseMaster(PVCopyTraverseMasterCallback callback)
    {
        traverseMaster(headNode,callback);
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.pvCopy.PVCopy#getStructure()
     */
    public Structure getStructure() {
        return structure;
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.pvCopy.PVCopy#createPVStructure()
     */
    public PVStructure createPVStructure() {
        if(cacheInitStructure!=null) {
            PVStructure save = cacheInitStructure;
            cacheInitStructure = null;
            return save;
        }
        PVStructure pvStructure =  pvDataCreate.createPVStructure(structure);
        return pvStructure;
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.copy.PVCopy#getCopyOffset(org.epics.pvdata.pv.PVField)
     */
    public int getCopyOffset(PVField masterPVField) {
        if(!headNode.isStructure) {
            Node node = headNode;
            if(node.masterPVField.equals(masterPVField)) return headNode.structureOffset;
            PVStructure parent = masterPVField.getParent();
            int offsetParent = parent.getFieldOffset();
            int off = masterPVField.getFieldOffset();
            int offdiff = off -offsetParent;
            if(offdiff<node.nfields) return headNode.structureOffset + offdiff;
            return -1;
        }
        Node node = getCopyOffset((StructureNode)headNode,masterPVField);
        if(node!=null) {
            int offset = masterPVField.getFieldOffset() - node.masterPVField.getFieldOffset();
            return node.structureOffset + offset;
        }
        return -1;
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.copy.PVCopy#getCopyOffset(org.epics.pvdata.pv.PVStructure, org.epics.pvdata.pv.PVField)
     */
    public int getCopyOffset(PVStructure masterPVStructure,PVField masterPVField) {
        Node node = null;
        if(!headNode.isStructure) {
            node = headNode;
            if(node.masterPVField!=masterPVStructure) {
                return -1;
            }
        } else {
            node = getCopyOffset((StructureNode)headNode,masterPVStructure);
        }
        if(node==null) return -1;
        int diff = masterPVField.getFieldOffset() - masterPVStructure.getFieldOffset();
        return node.structureOffset + diff;
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.pvCopy.PVCopy#getPVField(int)
     */
    public PVField getMasterPVField(int structureOffset)
    {
        Node node = null;
        if(!headNode.isStructure) {
            node = headNode;
        } else {
            node = getMasterNode((StructureNode)headNode,structureOffset);
        }
        if(node==null) {
            System.err.printf("PVCopy::PVField getRecordPVField(int structureOffset) illegal structureOffset %d %s%n",structureOffset,dump());
            throw new IllegalArgumentException("structureOffset not valid");
        }
        int diff = structureOffset - node.structureOffset;
        PVField pvMasterField = node.masterPVField;
        if(diff==0) return pvMasterField;
        PVStructure pvStructure = (PVStructure)pvMasterField;
        return pvStructure.getSubField(pvMasterField.getFieldOffset() + diff);
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.copy.PVCopy#initCopy(org.epics.pvdata.pv.PVStructure, org.epics.pvdata.misc.BitSet)
     */
    public void initCopy(PVStructure copyPVStructure, BitSet bitSet) {
        bitSet.set(0,copyPVStructure.getNumberFields(),true);
        updateCopyFromBitSet(copyPVStructure,headNode,bitSet);
    }
    /* (non-Javadoc)
     * @see org.epics.pvioc.pvCopy.PVCopy#updateCopySetBitSet(org.epics.pvdata.pv.PVStructure, org.epics.pvdata.misc.BitSet)
     */
    public void updateCopySetBitSet(PVStructure copyPVStructure,BitSet bitSet)
    {
        updateCopySetBitSet(copyPVStructure,headNode,bitSet);
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.pvCopy.PVCopy#updateCopyFromBitSet(org.epics.pvdata.pv.PVStructure, org.epics.pvdata.misc.BitSet)
     */
    public void updateCopyFromBitSet(PVStructure copyPVStructure,BitSet bitSet) {
        if(bitSet.get(0)) bitSet.set(0,copyPVStructure.getNumberFields(),true);
        updateCopyFromBitSet(copyPVStructure,headNode,bitSet);
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.pvCopy.PVCopy#updateRecord(org.epics.pvdata.pv.PVStructure, org.epics.pvdata.misc.BitSet)
     */
    public void updateMaster(PVStructure copyPVStructure, BitSet bitSet) {
        if(bitSet.get(0)) bitSet.set(0,copyPVStructure.getNumberFields(),true);
        updateMaster(copyPVStructure,headNode,bitSet);
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.copy.PVCopy#getOptions(int)
     */
    public PVStructure getOptions(int fieldOffset)
    {
        if(fieldOffset==0) return headNode.options;
        Node node = headNode;
        while(true) {
            if(node.structureOffset==fieldOffset) return node.options;
            if(!node.isStructure) return null;
            StructureNode structNode = (StructureNode)node;
            Node[] nodes = structNode.nodes;
            boolean okToContinue = false;
            for(int i=0; i< nodes.length; i++) {
                node = nodes[i];
                int soff = node.structureOffset;
                if(fieldOffset>=soff && fieldOffset<soff+node.nfields) {
                    if(fieldOffset==soff) return node.options;
                    if(!node.isStructure) {
                        return null;
                    }
                    okToContinue = true;
                    break;
                }
            }
            if(okToContinue) continue;
            throw new IllegalArgumentException("fieldOffset not valid");
        }
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.copy.PVCopy#dump()
     */
    public String dump() {
        StringBuilder builder = new StringBuilder();
        dump(builder,headNode,0);
        return builder.toString();

    }


    private void traverseMaster(Node innode, PVCopyTraverseMasterCallback callback)
    {
        Node node = innode;
        if(!node.isStructure) {
            callback.nextMasterPVField(node.masterPVField);
            return;
        }
        StructureNode structNode = (StructureNode)node;
        Node[] nodes = structNode.nodes;
        for(int i=0; i< nodes.length; ++i) {
            node = nodes[i];
            traverseMaster(node,callback);
        }
    }

    private void updateCopySetBitSet(PVField pvCopy,PVField pvMaster,BitSet bitSet) {
        if(pvCopy.getField().getType()!=Type.structure) {
            if(pvCopy.equals(pvMaster)) return;
            convert.copy(pvMaster, pvCopy);
            bitSet.set(pvCopy.getFieldOffset());
            return;
        }
        PVStructure pvCopyStructure = (PVStructure)pvCopy;
        PVField[] pvCopyFields = pvCopyStructure.getPVFields();
        int length = pvCopyFields.length;
        for(int i=0; i<length; i++) {
            pvMaster = getMasterPVField(pvCopyFields[i].getFieldOffset());
            updateCopySetBitSet(pvCopyFields[i],pvMaster,bitSet);
        }
    }

    private void updateCopySetBitSet(PVField pvCopy,Node node,BitSet bitSet) {
        boolean result = false;
        if(!node.isStructure) {
            if(result) return;
            updateCopySetBitSet(pvCopy,node.masterPVField,bitSet);
            return;
        }
        StructureNode structureNode = (StructureNode)(node);
        PVStructure pvCopyStructure = (PVStructure)pvCopy;
        PVField[] pvCopyFields = pvCopyStructure.getPVFields();
        int length = pvCopyFields.length;
        for(int i=0; i<length; i++) {
            updateCopySetBitSet(pvCopyFields[i],structureNode.nodes[i],bitSet);
        }
    }

    private void updateCopyFromBitSet(PVField pvCopy,Node node,BitSet bitSet) {
        boolean result = false;
        if(!node.isStructure) {
            if(result) return;
            PVField pvMaster = node.masterPVField;
            convert.copy(pvMaster, pvCopy);
            return;
        }
        StructureNode structureNode = (StructureNode)(node);
        int offset = structureNode.structureOffset;
        int nextSet = bitSet.nextSetBit(offset);
        if(nextSet==-1) return;
        if(offset>=pvCopy.getNextFieldOffset()) return;
        PVStructure pvCopyStructure = (PVStructure)pvCopy;
        PVField[] pvCopyFields = pvCopyStructure.getPVFields();
        int length = pvCopyFields.length;
        for(int i=0; i<length; i++) {
            updateCopyFromBitSet(pvCopyFields[i],structureNode.nodes[i],bitSet);
        }
    }

    private void updateMaster(PVField pvCopy,Node node,BitSet bitSet) {
        boolean result = false;
        if(!node.isStructure) {
            if(result) return;
            PVField pvMaster = node.masterPVField;
            convert.copy(pvCopy, pvMaster);
            return;
        }
        StructureNode structureNode = (StructureNode)(node);
        int offset = structureNode.structureOffset;
        int nextSet = bitSet.nextSetBit(offset);
        if(nextSet==-1) return;
        if(offset>=pvCopy.getNextFieldOffset()) return;
        PVStructure pvCopyStructure = (PVStructure)pvCopy;
        PVField[] pvCopyFields = pvCopyStructure.getPVFields();
        int length = pvCopyFields.length;
        for(int i=0; i<length; i++) {
            updateMaster(pvCopyFields[i],structureNode.nodes[i],bitSet);
        }
    }

    private boolean  init(PVStructure pvRequest) {
        PVStructure pvMasterStructure = pvMaster;
        int len = pvRequest.getPVFields().length;
        boolean entireMaster = false;
        if(len==0) entireMaster = true;
        PVStructure pvOptions = null;
        if(len==1 && pvRequest.getSubField("_options")!=null) {
            pvOptions = pvRequest.getStructureField("_options");
        }
        if(entireMaster) {
            // asking for entire record is special case.
            structure = pvMasterStructure.getStructure();
            Node node = new Node();
            headNode = node;
            node.options = pvOptions;
            node.isStructure = false;
            node.structureOffset = 0;
            node.masterPVField = pvMasterStructure;
            node.nfields = pvMasterStructure.getNumberFields();
            return true;
        }
        structure = createStructure(pvMasterStructure,pvRequest);
        if(structure==null) return false;
        cacheInitStructure = createPVStructure();
        headNode = createStructureNodes(pvMaster,pvRequest,cacheInitStructure);
        return true;
    }

    private static Structure createStructure(PVStructure pvMaster,PVStructure pvFromRequest) {
        if(pvFromRequest.getStructure().getFieldNames().length==0) {
            return pvMaster.getStructure();
        }
        PVField[] pvFromRequestFields = pvFromRequest.getPVFields();
        String[] fromRequestFieldNames = pvFromRequest.getStructure().getFieldNames();
        int length = pvFromRequestFields.length;
        if(length==0) return null;
        ArrayList<Field> fieldList = new ArrayList<Field>(length);
        ArrayList<String> fieldNameList = new ArrayList<String>(length);
        for(int i=0; i<length; i++) {
            String fieldName = fromRequestFieldNames[i];
            PVField pvMasterField = pvMaster.getSubField(fieldName);
            if(pvMasterField==null) continue;
            Field field = pvMasterField.getField();
            if(field.getType()==Type.structure) {
                PVStructure pvRequestStructure = (PVStructure)pvFromRequestFields[i];
                if(pvRequestStructure.getNumberFields()>0) {
                    String[] names = pvRequestStructure.getStructure().getFieldNames();
                    int num = names.length;
                    if(num>0 && names[0].equals("_options")) num--;
                    if(num>0 ) {
                        if(pvMasterField.getField().getType()!=Type.structure) continue;
                        fieldNameList.add(fieldName);
                        fieldList.add(createStructure((PVStructure)pvMasterField,pvRequestStructure));
                        continue;
                    }
                }

            }
            fieldNameList.add(fieldName);
            fieldList.add(field);
        }
        int numsubfields = fieldList.size();
        if(numsubfields==0) return null;
        Field[] fields = new Field[fieldList.size()];
        String[] fieldNames = new String[fieldNameList.size()];
        fields = fieldList.toArray(fields);
        fieldNames = fieldNameList.toArray(fieldNames);
        return fieldCreate.createStructure(fieldNames, fields);
    }

    private static Node createStructureNodes(
            PVStructure pvMasterStructure,
            PVStructure pvFromRequest,
            PVStructure pvFromCopy)
    {
        PVField[] copyPVFields = pvFromCopy.getPVFields();
        PVStructure pvOptions = null;
        PVField pvField = pvFromRequest.getSubField("_options");
        if(pvField!=null) pvOptions = (PVStructure)pvField;
        int number = copyPVFields.length;
        ArrayList<Node> nodeList = new ArrayList<Node>(number);
        for(int i=0; i<number; i++) {
            PVField copyPVField = copyPVFields[i];
            String fieldName = copyPVField.getFieldName();

            PVStructure requestPVStructure = (PVStructure)pvFromRequest.getSubField(fieldName);
            PVStructure pvSubFieldOptions = null;
            pvField = requestPVStructure.getSubField("_options");
            if(pvField!=null) pvSubFieldOptions = (PVStructure)pvField;
            PVField pvMasterField = pvMasterStructure.getSubField(fieldName);
            if(pvMasterField==null) {
                throw new NullPointerException("did not find field in master");
            }
            int numberRequest = requestPVStructure.getPVFields().length;
            if(pvSubFieldOptions!=null) numberRequest--;
            if(numberRequest>0) {
                nodeList.add(createStructureNodes(
                        (PVStructure)pvMasterField,requestPVStructure,(PVStructure)copyPVField));
                continue;
            }
            Node node = new Node();
            node.options = pvSubFieldOptions;
            node.isStructure = false;
            node.masterPVField = pvMasterField;
            node.nfields = copyPVField.getNumberFields();
            node.structureOffset = copyPVField.getFieldOffset();
            nodeList.add(node);
        }
        StructureNode structureNode = new StructureNode();
        Node[] nodes = new Node[number];
        nodeList.toArray(nodes);
        structureNode.masterPVField = pvMasterStructure;
        structureNode.isStructure = true;
        structureNode.nodes = nodes;
        structureNode.structureOffset = pvFromCopy.getFieldOffset();
        structureNode.nfields = pvFromCopy.getNumberFields();
        structureNode.options = pvOptions;
        return structureNode;

    }

    private Node getCopyOffset(StructureNode structureNode,PVField masterPVField)
    {
        int offset = masterPVField.getFieldOffset();
        Node[] nodes = structureNode.nodes;
        for(int i=0; i< nodes.length; i++) {
            Node node = nodes[i];
            if(!node.isStructure) {
                int off = node.masterPVField.getFieldOffset();
                int nextOffset = node.masterPVField.getNextFieldOffset();
                if(offset>= off && offset<nextOffset) return node;
            } else {
                StructureNode subNode = (StructureNode)node;
                node = getCopyOffset(subNode,masterPVField);
                if(node!=null) return node;
            }
        }
        return null;
    }

    private Node getMasterNode(StructureNode structureNode,int structureOffset) {
        for(Node node : structureNode.nodes) {
            if(structureOffset>=(node.structureOffset + node.nfields)) continue;
            if(!node.isStructure) return node;
            StructureNode subNode = (StructureNode)node;
            return  getMasterNode(subNode,structureOffset);
        }
        return null;
    }

    private void dump(StringBuilder builder,Node node,int indentLevel) {
        convert.newLine(builder, indentLevel);
        String kind;
        if(node.isStructure) {
            kind = "structureNode";
        } else {
            kind = "node";
        }
        builder.append(kind);
        builder.append(" isStructure ").append(node.isStructure ? "true" : "false");
        builder.append(" structureOffset ").append(node.structureOffset);
        builder.append(" nfields ").append(node.nfields);
        PVStructure options = node.options;
        if(options!=null) {
            convert.newLine(builder, indentLevel+1);
            builder.append(options.getFieldName());
            PVField[] pvFields = options.getPVFields();
            for(int i=0; i<pvFields.length; ++i) {
                PVString pvString = (PVString)pvFields[i];
                convert.newLine(builder, indentLevel+2);
                builder.append(pvString.getFieldName()).append(" ").append(pvString.get());
            }
        }
        String name = node.masterPVField.getFullName();
        convert.newLine(builder, indentLevel+1);
        builder.append("masterField name ").append(name);
        if(!node.isStructure) return;
        StructureNode structureNode = (StructureNode)node;
        Node[] nodes =structureNode.nodes;
        for(int i=0 ; i<nodes.length; i++){
            if(nodes[i]==null) {
                convert.newLine(builder, indentLevel+1);
                builder.append("node[").append(i).append("] is null");
                continue;
            }
            dump(builder,nodes[i],indentLevel+1);
        }
    }
}
