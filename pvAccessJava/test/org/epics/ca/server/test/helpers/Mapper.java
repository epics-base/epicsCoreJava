package org.epics.ca.server.test.helpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

import org.epics.ca.PVFactory;
import org.epics.pvData.factory.ConvertFactory;
import org.epics.pvData.misc.BitSet;
import org.epics.pvData.pv.Convert;
import org.epics.pvData.pv.Field;
import org.epics.pvData.pv.FieldCreate;
import org.epics.pvData.pv.PVDataCreate;
import org.epics.pvData.pv.PVField;
import org.epics.pvData.pv.PVString;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.Structure;
import org.epics.pvData.pv.Type;

public class Mapper
{
	private final static Convert convert = ConvertFactory.getConvert();
    private static final FieldCreate fieldCreate = PVFactory.getFieldCreate();
    private static final PVDataCreate pvDataCreate = PVFactory.getPVDataCreate();

	
	final PVStructure originStructure;
	final PVStructure copyStructure;
    final int[] toOriginStructure;
    final int[] toCopyStructure;

    public Mapper(PVStructure originStructure, PVStructure pvRequest)
    {
    	this(originStructure, pvRequest, null);
    }
    
    public Mapper(PVStructure originStructure, PVStructure pvRequest, String structureName)
	{
		if(structureName!=null && structureName.length()>0) {
			if(pvRequest.getPVFields().length>0) {
				pvRequest = pvRequest.getStructureField(structureName);
				if(pvRequest==null)
					throw new IllegalArgumentException("pvRequest does not contain " + structureName + " subfield");
			}
		}

		this.originStructure = originStructure;
    	
		ArrayList<Integer> indexMapping = new ArrayList<Integer>(originStructure.getNumberFields());
		
        if(pvRequest.getPVFields().length==0)
        {
        	copyStructure = pvDataCreate.createPVStructure(null, originStructure.getStructure());
			// 1-1 mapping
			int fieldCount = copyStructure.getNumberFields();
			for (int i = 0; i < fieldCount; i++)
				indexMapping.add(i);
        }
        else
        {
			indexMapping.add(-1);	// top

			if(pvRequest.getSubField("field")!=null) {
				pvRequest = pvRequest.getStructureField("field");
			}
			Structure structure = createStructure(originStructure, indexMapping, pvRequest);
			this.copyStructure = pvDataCreate.createPVStructure(null, structure);
        }
    	
    	
    	
        toOriginStructure = new int[copyStructure.getNumberFields()];
        toCopyStructure = new int[originStructure.getNumberFields()];
        Arrays.fill(toCopyStructure, -1);

        int ix = 0;
        for (Integer i : indexMapping)
        {
        	int iv = i.intValue();
        	toOriginStructure[ix] = iv;
        	if (iv != -1)
        		toCopyStructure[iv] = ix;
        	ix++;
        }
	}

    public PVStructure getCopyStructure()
    {
    	return copyStructure;
    }
    
    public int getCopyStructureIndex(int ix)
    {
    	return toCopyStructure[ix];
    }

    public int getOriginStructureIndex(int ix)
    {
    	return toOriginStructure[ix];
    }
    
	public void updateCopyStructure(BitSet copyStructureBitSet)
	{
		boolean doAll = copyStructureBitSet == null || copyStructureBitSet.get(0);
		if (doAll)
		{
			for (int i = 1; i < toOriginStructure.length;)
			{
				final PVField copyField = copyStructure.getSubField(i);
				final PVField originField = originStructure.getSubField(toOriginStructure[i]);
				convert.copy(originField, copyField);
				i = copyField.getNextFieldOffset();
			}
		}
		else
		{
			int i = copyStructureBitSet.nextSetBit(1);
			while (i != -1)
			{
				final PVField copyField = copyStructure.getSubField(i);
				final PVField originField = originStructure.getSubField(toOriginStructure[i]);
				convert.copy(originField, copyField);
				i = copyStructureBitSet.nextSetBit(copyField.getNextFieldOffset());
			}
		}
	}

	public void updateOriginStructure(BitSet copyStructureBitSet)
	{
		boolean doAll = copyStructureBitSet == null || copyStructureBitSet.get(0);
		if (doAll)
		{
			for (int i = 1; i < toOriginStructure.length;)
			{
				final PVField copyField = copyStructure.getSubField(i);
				final PVField originField = originStructure.getSubField(toOriginStructure[i]);
				convert.copy(copyField, originField);
				i = copyField.getNextFieldOffset();
			}
		}
		else
		{
			int i = copyStructureBitSet.nextSetBit(1);
			while (i != -1)
			{
				final PVField copyField = copyStructure.getSubField(i);
				final PVField originField = originStructure.getSubField(toOriginStructure[i]);
				convert.copy(copyField, originField);
				i = copyStructureBitSet.nextSetBit(copyField.getNextFieldOffset());
			}
		}
	}

	public void updateCopyStructureOriginBitSet(BitSet originStructureBitSet, BitSet copyBitSet)
	{
		copyBitSet.clear();
		boolean doAll = originStructureBitSet.get(0);
		if (doAll)
		{
			copyBitSet.set(0);
			for (int i = 1; i < toOriginStructure.length;)
			{
				final PVField copyField = copyStructure.getSubField(i);
				final PVField originField = originStructure.getSubField(toOriginStructure[i]);
				convert.copy(originField, copyField);
				i = copyField.getNextFieldOffset();
			}
		}
		else
		{
			int i = originStructureBitSet.nextSetBit(1);
			while (i != -1)
			{
				int toCopyIndex = toCopyStructure[i];
				if (toCopyIndex != -1)
				{
					copyBitSet.set(toCopyIndex);
					final PVField copyField = copyStructure.getSubField(toCopyIndex);
					final PVField originField = originStructure.getSubField(i);
					convert.copy(originField, copyField);
					i = originStructureBitSet.nextSetBit(originField.getNextFieldOffset());
				}
				else
				{
					final PVField originField = originStructure.getSubField(i);
					i = originStructureBitSet.nextSetBit(originField.getNextFieldOffset());
				}
			}
		}
	}

	private static final Pattern commaPattern = Pattern.compile("[,]");

	private static void addMapping(PVField pvRecordField, ArrayList<Integer> indexMapping) {
		if (pvRecordField.getField().getType() == Type.structure)
		{
			indexMapping.add(pvRecordField.getFieldOffset());
			PVStructure struct = (PVStructure)pvRecordField;
			for (PVField pvField : struct.getPVFields())
				addMapping(pvField, indexMapping);
		}
		else
		{
			indexMapping.add(pvRecordField.getFieldOffset());
		}
	}

    private static Structure createStructure(PVStructure pvRecord, ArrayList<Integer> indexMapping, PVStructure pvFromRequest) {
        PVField[] pvFromFields = pvFromRequest.getPVFields();
        int length = pvFromFields.length;
        ArrayList<Field> fieldList = new ArrayList<Field>(length);
        ArrayList<String> fieldNameList = new ArrayList<String>(length);
        for(int i=0; i<length; i++) {
        	PVField pvField = pvFromFields[i];
        	if(pvField.getField().getType()==Type.structure) {
        		PVStructure pvStruct = (PVStructure)pvField;
        		PVField pvLeaf = pvStruct.getSubField("leaf.source");
        		if(pvLeaf!=null && (pvLeaf instanceof PVString)){
        			PVString pvString = (PVString)pvLeaf;
        			PVField pvRecordField = pvRecord.getSubField(pvString.get());
        			if(pvRecordField!=null) {
        				addMapping(pvRecordField, indexMapping);
        				fieldNameList.add(pvString.get());
        				fieldList.add(pvRecordField.getField());
        			}
        		} else {
    				indexMapping.add(-1);		// fake structure, will not be mapped
    				fieldNameList.add("fake");
        			fieldList.add(createStructure(pvRecord,indexMapping,pvStruct));
        		}
        	} else {
        		PVString pvString = (PVString)pvFromFields[i];
        		String n = ((PVStructure)pvString.getParent()).getStructure().getFieldName(i);
        		if(n.equals("fieldList")) {
        			String[] fieldNames = commaPattern.split(pvString.get());
        			for(int j=0; j<fieldNames.length; j++) {
        				String name = fieldNames[j].trim();
        				PVField pvRecordField = pvRecord.getSubField(name);
        				if(pvRecordField!=null) {
            				addMapping(pvRecordField, indexMapping);
            				fieldNameList.add(name);
        					fieldList.add(pvRecordField.getField());
        				}
        			}
        		} else {
        			PVField pvRecordField = pvRecord.getSubField(pvString.get().trim());
        			if(pvRecordField!=null) {
        				addMapping(pvRecordField, indexMapping);
        				fieldNameList.add(pvString.get());
        				fieldList.add(pvRecordField.getField());
        			}
        		}
        	}
        }
        Field[] fields = new Field[fieldList.size()];
        fields = fieldList.toArray(fields);
        String[] names = new String[fieldNameList.size()];
        names = fieldNameList.toArray(names);
        return fieldCreate.createStructure(names, fields);
    }
    
}