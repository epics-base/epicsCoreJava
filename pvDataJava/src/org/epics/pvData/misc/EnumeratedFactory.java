/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.misc;


import java.util.Arrays;

import org.epics.pvData.factory.BasePVInt;
import org.epics.pvData.factory.*;
import org.epics.pvData.pv.Field;
import org.epics.pvData.pv.MessageType;
import org.epics.pvData.pv.PVField;
import org.epics.pvData.pv.PVInt;
import org.epics.pvData.pv.PVStringArray;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.Scalar;
import org.epics.pvData.pv.ScalarArray;
import org.epics.pvData.pv.ScalarType;
import org.epics.pvData.pv.Structure;
import org.epics.pvData.pv.*;

/**
 * Factory for an enumerated structure.
 * @author mrk
 *
 */
public class EnumeratedFactory {
    /** Factory that creates Enumerated
     * @param pvField The field. It must be an enumerated structure.
     */
    public static Enumerated getEnumerated(PVField pvField) {
        if(pvField.getField().getType()!=Type.structure) {
            pvField.message("field is not a structure", MessageType.error);
            return null;
        }
        PVStructure pvStructure = (PVStructure)pvField;
        Structure structure = pvStructure.getStructure();
        Field[] fields = structure.getFields();
        if(fields.length!=2) {
            pvStructure.message("structure does not have exactly two fields", MessageType.error);
            return null;
        }
        Field field = fields[0];
        if(!field.getFieldName().equals("index") || field.getType()!=Type.scalar || ((Scalar)field).getScalarType()!=ScalarType.pvInt) {
            pvStructure.message("structure does not have field index of type int", MessageType.error);
            return null;
        }
        field = fields[1];
        if(!field.getFieldName().equals("choices") || field.getType()!=Type.scalarArray || ((ScalarArray)field).getElementType()!=ScalarType.pvString) {
            pvStructure.message("structure does not have field choices of type array", MessageType.error);
            return null;
        }
        ScalarArray array = (ScalarArray)fields[1];
        if(array.getElementType()!=ScalarType.pvString) {
            pvStructure.message("elementType for choices is not string", MessageType.error);
            return null;
        }
        PVField[] pvFields = pvStructure.getPVFields();
        return new EnumeratedImpl(pvStructure,(PVInt)pvFields[0],(PVStringArray)pvFields[1]);
    }
   
   
    private static class EnumeratedImpl implements Enumerated{
    	private final PVStructure pvStructure;
    	private final PVInt pvIndex;
    	private final PVStringArray pvChoices;
    	private StringArrayData stringArrayData = new StringArrayData();

    	private EnumeratedImpl(PVStructure pvStructure,PVInt pvIndex,PVStringArray pvChoices) {
    		this.pvStructure = pvStructure;
    		this.pvIndex = pvIndex;
    		this.pvChoices = pvChoices;
    	}       
    	/* (non-Javadoc)
    	 * @see org.epics.pvData.misc.Enumerated#getPV()
    	 */
    	@Override
    	public PVStructure getPV() {
    		return pvStructure;
    	}
    	/* (non-Javadoc)
    	 * @see org.epics.pvData.misc.Enumerated#getChoice()
    	 */
    	@Override
    	public String getChoice() {
    		pvChoices.get(0, pvChoices.getLength(), stringArrayData);
    		return stringArrayData.data[pvIndex.get()];
    	}
    	/* (non-Javadoc)
    	 * @see org.epics.pvData.misc.Enumerated#getChoices()
    	 */
    	@Override
    	public PVStringArray getChoices() {
    		return pvChoices;
    	}
    	/* (non-Javadoc)
    	 * @see org.epics.pvData.misc.Enumerated#getIndex()
    	 */
    	@Override
    	public PVInt getIndex() {
    		return pvIndex;
    	}

    	/* (non-Javadoc)
    	 * @see java.lang.Object#hashCode()
    	 */
    	@Override
    	public int hashCode() {
    		final int prime = 31;
    		int result = super.hashCode();
    		pvChoices.get(0, pvChoices.getLength(), stringArrayData);
    		result = prime * result + Arrays.hashCode(stringArrayData.data);
    		result = prime * result + pvIndex.get();
    		return result;
    	}
    	/* (non-Javadoc)
    	 * @see java.lang.Object#equals(java.lang.Object)
    	 */
    	@Override
    	public boolean equals(Object obj) {
    		if (this == obj)
    			return true;
    		if (!super.equals(obj))
    			return false;
    		if (getClass() != obj.getClass())
    			return false;
    		EnumeratedImpl other = (EnumeratedImpl) obj;
    		pvChoices.get(0, pvChoices.getLength(), stringArrayData);
    		String[] choices = stringArrayData.data;
    		other.pvChoices.get(0, pvChoices.getLength(), stringArrayData);
    		String[] otherChoices = stringArrayData.data;
    		if (!Arrays.equals(choices, otherChoices))
    			return false;
    		if (pvIndex.get() != other.pvIndex.get())
    			return false;
    		return true;
    	}
    }
}