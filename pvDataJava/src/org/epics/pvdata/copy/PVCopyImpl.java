/*
 * The License for this software can be
 * found in the file LICENSE that is included with the distribution
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
class PVCopyImpl {
	static PVCopy create(PVStructure pvMaster,PVStructure pvRequest,String structureName) {
		if(structureName!=null && structureName.length()>0) {
			if(pvRequest.getPVFields().length>0) {
				pvRequest = pvRequest.getStructureField(structureName);
				if(pvRequest==null) return null;
			}
		}
		ThePVCopyImpl impl = new ThePVCopyImpl(pvMaster);
		PVStructure pvStruct = pvRequest;
		if(pvRequest.getSubField("field")!=null) {
			pvStruct = pvRequest.getStructureField("field");
		}
		boolean result = impl.init(pvStruct);
		if(!result) return null;
		impl.traverseMasterInitPlugin();
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
        PVFilter[] pvFilters = null;
    }

    
    static class StructureNode extends Node {
        Node[] nodes;
    }    
    
    private static final class ThePVCopyImpl implements PVCopy{
        private final PVStructure pvMaster;
        private Structure structure = null;
        private Node headNode = null;
        private PVStructure cacheInitStructure = null;
        
		ThePVCopyImpl(PVStructure pvMaster) {
            this.pvMaster = pvMaster;
        }
        
        /* (non-Javadoc)
         * @see org.epics.pvdata.copy.PVCopy#getPVMaster()
         */
        @Override
        public PVStructure getPVMaster() {
            return pvMaster;
        }
        public void traverseMaster(PVCopyTraverseMasterCallback callback)
        {
            traverseMaster(headNode,callback);     
        }
        void traverseMaster(Node innode, PVCopyTraverseMasterCallback callback)
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
        /* (non-Javadoc)
         * @see org.epics.pvdata.pvCopy.PVCopy#getStructure()
         */
        @Override
        public Structure getStructure() {
            return structure;
        }
        /* (non-Javadoc)
         * @see org.epics.pvdata.pvCopy.PVCopy#createPVStructure()
         */
        @Override
        public PVStructure createPVStructure() {
            if(cacheInitStructure!=null) {
                PVStructure save = cacheInitStructure;
                cacheInitStructure = null;
                return save;
            }
            PVStructure pvStructure =  pvDataCreate.createPVStructure(structure);
            return pvStructure;
        }
        @Override
        public PVStructure getOptions(int fieldOffset)
        {
            if(fieldOffset==0) return headNode.options;
            Node node = headNode;
            while(true) {
                if(node.structureOffset==fieldOffset) return node.options;
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
        @Override
        public String dump() {
              StringBuilder builder = new StringBuilder();
              dump(builder,headNode,0);
              return builder.toString();
              
        }
        static private void dump(StringBuilder builder,Node node,int indentLevel) {
            convert.newLine(builder, indentLevel);
            String kind;
            if(node.isStructure) {
                kind = "structureNode";
            } else {
                kind = "node";
            }
            builder.append(kind);
            builder.append((" isStructure " + (node.isStructure ? "true" : "false")));
            builder.append(" structureOffset " + node.structureOffset);
            builder.append(" nfields " + node.nfields);
            PVStructure options = node.options;
            if(options!=null) {
                convert.newLine(builder, indentLevel+1);
                options.toString(builder, indentLevel+1);
                convert.newLine(builder, indentLevel);
            }
            String name = node.masterPVField.getFullName();
            convert.newLine(builder, indentLevel+1);
            builder.append(" masterField " + name);
            convert.newLine(builder, indentLevel+1);
            PVFilter[] pvFilters = node.pvFilters;
            if(pvFilters!=null) {
            	builder.append(" filters:");
            	for(int i=0; i< pvFilters.length; ++i) builder.append(" " +pvFilters[i].getName());
            	convert.newLine(builder, indentLevel+1);
            }
            if(!node.isStructure) return;
            StructureNode structureNode = (StructureNode)node;
            Node[] nodes =structureNode.nodes;
            for(int i=0 ; i<nodes.length; i++){
                if(nodes[i]==null) {
                    convert.newLine(builder, indentLevel+1);
                    builder.append("node[" + i + "] is null");
                    continue;
                }
                dump(builder,nodes[i],indentLevel+1);
            }
        }
        @Override
        public int getCopyOffset(PVField masterPVField) {
            if(!headNode.isStructure) {
                Node masterNode = headNode;
                if(masterNode.masterPVField.equals(masterPVField)) return headNode.structureOffset;
                PVStructure parent = masterPVField.getParent();
                int offsetParent = parent.getFieldOffset();
                int off = masterPVField.getFieldOffset();
                int offdiff = off -offsetParent;
                if(offdiff<masterNode.nfields) return headNode.structureOffset + offdiff;
                return -1;
            }
            Node masterNode = getCopyOffset((StructureNode)headNode,masterPVField);
            if(masterNode!=null) {
                int offset = masterPVField.getFieldOffset() - masterNode.masterPVField.getFieldOffset();
                return masterNode.structureOffset + offset;
            }
            return -1;
        }
        
        public int getCopyOffset(PVStructure masterPVStructure,PVField masterPVField) {
            Node masterNode = null;
            if(!headNode.isStructure) {
                masterNode = headNode;
                if(masterNode.masterPVField!=masterPVStructure) {
                    return headNode.structureOffset + 1;
                }
            } else {
                masterNode = getCopyOffset((StructureNode)headNode,masterPVStructure);
            }
            if(masterNode==null) return -1;
            int diff = masterPVField.getFieldOffset() - masterPVStructure.getFieldOffset();
            return masterNode.structureOffset + diff;
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
        /* (non-Javadoc)
         * @see org.epics.pvdata.pvCopy.PVCopy#getPVField(int)
         */
        @Override
        public PVField getMasterPVField(int structureOffset)
        {
            Node masterNode = null;
            if(!headNode.isStructure) {
                masterNode = headNode;
            } else {
                masterNode = getMasterNode((StructureNode)headNode,structureOffset);
            }
            if(masterNode==null) {
                System.err.printf("PVCopy::PVField getRecordPVField(int structureOffset) illegal structureOffset %d %s%n",structureOffset,dump());
            	throw new IllegalArgumentException("structureOffset not valid");
            }
            int diff = structureOffset - masterNode.structureOffset;
            PVField pvMasterField = masterNode.masterPVField;
            if(diff==0) return pvMasterField;
            PVStructure pvStructure = (PVStructure)pvMasterField;
            return pvStructure.getSubField(pvMasterField.getFieldOffset() + diff);
        }
        /* (non-Javadoc)
         * @see org.epics.pvdata.pvCopy.PVCopy#initCopy(org.epics.pvdata.pv.PVStructure, org.epics.pvdata.misc.BitSet)
         */
        @Override
        public void initCopy(PVStructure copyPVStructure, BitSet bitSet) {
            bitSet.clear();
            bitSet.set(0);
            updateCopyFromBitSet(copyPVStructure,bitSet);
        }
        /* (non-Javadoc)
         * @see org.epics.pvioc.pvCopy.PVCopy#updateCopySetBitSet(org.epics.pvdata.pv.PVStructure, org.epics.pvdata.misc.BitSet)
         */
        @Override
        public void updateCopySetBitSet(PVStructure copyPVStructure,BitSet bitSet)
        {
        	updateCopySetBitSet(copyPVStructure,headNode,bitSet);
        }
        /* (non-Javadoc)
         * @see org.epics.pvdata.pvCopy.PVCopy#updateCopyFromBitSet(org.epics.pvdata.pv.PVStructure, org.epics.pvdata.misc.BitSet)
         */
        @Override
        public void updateCopyFromBitSet(PVStructure copyPVStructure,BitSet bitSet) {
            boolean doAll = bitSet.get(0);
            if(headNode.isStructure) {
                updateStructureNodeFromBitSet(copyPVStructure,(StructureNode)headNode,bitSet, true,doAll);
            } else {
                Node masterNode = headNode;
                updateSubFieldFromBitSet(copyPVStructure,masterNode.masterPVField,bitSet, true,doAll);
            }
        }
        /* (non-Javadoc)
         * @see org.epics.pvdata.pvCopy.PVCopy#updateRecord(org.epics.pvdata.pv.PVStructure, org.epics.pvdata.misc.BitSet)
         */
        @Override
        public void updateMaster(PVStructure copyPVStructure, BitSet bitSet) {
            boolean doAll = bitSet.get(0);

            if(headNode.isStructure) {
                updateStructureNodeFromBitSet(copyPVStructure,(StructureNode)headNode,bitSet, false,doAll);
            } else {
                Node masterNode = headNode;
                updateSubFieldFromBitSet(copyPVStructure,masterNode.masterPVField,bitSet,false,doAll);
            }
        }
        
        private void initPlugin(Node node,PVStructure pvOptions,PVField pvMasterField)
        {
        	PVField[] pvFields = pvOptions.getPVFields();
        	int num = pvFields.length;
        	PVFilter[] pvFilters = new PVFilter[num];
        	int numfilter = 0;;
        	for(int i=0; i<num; ++i) {
        		PVString pvOption = (PVString)(pvFields[i]);
        		String name = pvOption.getFieldName();
        		String value = pvOption.get();
            	PVPlugin pvPlugin = PVPluginRegistry.find(name);
            	if(pvPlugin==null) continue;
            	pvFilters[numfilter] = pvPlugin.create(value,this,pvMasterField);
            	if(pvFilters[numfilter]!=null) ++numfilter;
        	}
        	if(numfilter==0) return;
        	node.pvFilters = new PVFilter[numfilter];
        	for(int i=0; i<numfilter; ++i) node.pvFilters[i] = pvFilters[i];
        	
        }
         
        private void traverseMasterInitPlugin()
        {
        	traverseMasterInitPlugin(headNode);     
        }
        private void traverseMasterInitPlugin(Node node)
        {
        	PVField pvField = node.masterPVField;
            PVStructure pvOptions = node.options;
            if(pvOptions!=null) initPlugin(node,pvOptions,pvField);	
            if(!node.isStructure) return;
            StructureNode structNode = (StructureNode)node;
            Node[] nodes = structNode.nodes;
            for(int i=0; i< nodes.length; ++i) {
                node = nodes[i];
                traverseMasterInitPlugin(node);
            }
        }
        
        private boolean  init(PVStructure pvRequest) {
            PVStructure pvMasterStructure = pvMaster;
            int len = pvRequest.getPVFields().length;
            boolean entireRecord = false;
            if(len==0) entireRecord = true;
            PVStructure pvOptions = null;
            if(len==1 && pvRequest.getSubField("_options")!=null) {
                pvOptions = pvRequest.getStructureField("_options");
                entireRecord = true;
            }
            if(entireRecord) {
                // asking for entire record is special case.
                structure = pvMasterStructure.getStructure();
                Node masterNode = new Node();
                headNode = masterNode;
                masterNode.options = pvOptions;
                masterNode.isStructure = false;
                masterNode.structureOffset = 0;
                masterNode.masterPVField = pvMasterStructure;
                masterNode.nfields = pvMasterStructure.getNumberFields();
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
                PVField pvMasterField = null;
                PVField[] pvMasterFields = pvMasterStructure.getPVFields();
                for(int j=0; i<pvMasterFields.length; j++ ) {
                    if(pvMasterFields[j].getFieldName().equals(fieldName)) {
                        pvMasterField = pvMasterFields[j];
                        break;
                    }
                }
                int numberRequest = requestPVStructure.getPVFields().length;
                if(pvSubFieldOptions!=null) numberRequest--;
                if(numberRequest>0) {
                    nodeList.add(createStructureNodes(
                            (PVStructure)pvMasterField,requestPVStructure,(PVStructure)copyPVField));
                    continue;
                }
                Node masterNode = new Node();
                masterNode.options = pvSubFieldOptions;
                masterNode.isStructure = false;
                masterNode.masterPVField = pvMasterField;
                masterNode.nfields = copyPVField.getNumberFields();
                masterNode.structureOffset = copyPVField.getFieldOffset();
                nodeList.add(masterNode);
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

//        private void updateStructureNodeSetBitSet(PVStructure pvCopy,StructureNode structureNode,BitSet bitSet) {
//        	if(structureNode.pvFilters!=null) {
//        		boolean result = false;
//        		for(int i=0; i< structureNode.pvFilters.length; ++i) {
//        			PVFilter pvFilter = structureNode.pvFilters[i];
//        			if(pvFilter.filter(pvCopy,bitSet)) result = true;
//        		}
//        		if(!result) {
//        			convert.copy(structureNode.masterPVField, pvCopy);
//        			bitSet.set(pvCopy.getFieldOffset());
//        		}
//        		return;
//        	}
//            for(int i=0; i<structureNode.nodes.length; i++) {
//                Node node = structureNode.nodes[i];
//                PVField pvField = pvCopy.getSubField(node.structureOffset);
//                if(node.isStructure) {
//                    updateStructureNodeSetBitSet((PVStructure)pvField,(StructureNode)node,bitSet); 
//                } else {
//                    updateSubFieldSetBitSet(pvField,node,bitSet);
//                }
//            }
//        }
        
        private void updateCopySetBitSet(PVField pvCopy,Node node,BitSet bitSet) {
        	boolean result = false;
        	if(node.pvFilters!=null) {
        		for(int i=0; i< node.pvFilters.length; ++i) {
        			PVFilter pvFilter = node.pvFilters[i];
        			if(pvFilter.filter(pvCopy,bitSet)) result = true;
        		}
        	}
        	PVField pvMaster = node.masterPVField;
        	Field field = pvCopy.getField();
        	Type type = field.getType();
        	if(!node.isStructure) {
        		if(result) return;
        		boolean isEqual = pvCopy.equals(pvMaster);
        		if(isEqual) {
        			if(type==Type.structureArray) {
        				// always act as though a change occurred. Note that array elements are shared.
        				bitSet.set(pvCopy.getFieldOffset());
        			}
        		}
        		if(isEqual) return;
        			convert.copy(pvMaster, pvCopy);
        			bitSet.set(pvCopy.getFieldOffset());
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
        
        private void updateStructureNodeFromBitSet(PVStructure pvCopy,StructureNode structureNode,BitSet bitSet,boolean toCopy,boolean doAll) {
            int offset = structureNode.structureOffset;
            if(!doAll) {
                int nextSet = bitSet.nextSetBit(offset);
                if(nextSet==-1) return;
            }
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
                    updateSubFieldFromBitSet(pvField,node.masterPVField,bitSet,toCopy,doAll);
                }
            }
        }
        
       
        private void updateSubFieldFromBitSet(PVField pvCopy,PVField pvMasterField,BitSet bitSet,boolean toCopy,boolean doAll) {
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
                if(pvMasterField.getField().getType()!=Type.structure) {
                    throw new IllegalStateException("Logic error");
                }
                PVStructure pvMasterStructure = (PVStructure)pvMasterField;
                PVField[] pvMasterFields = pvMasterStructure.getPVFields();
                for(int i=0; i<pvCopyFields.length; i++) {
                    updateSubFieldFromBitSet(pvCopyFields[i],pvMasterFields[i],bitSet,toCopy,doAll);
                }
            } else {
                if(toCopy) {
                    convert.copy(pvMasterField, pvCopy);
                } else {
                    convert.copy(pvCopy, pvMasterField);
                }
            }
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
        
    }
}
