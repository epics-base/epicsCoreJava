/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.factory;

import java.util.Arrays;

import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.PVStructureArray;
import org.epics.pvdata.pv.Serializable;
import org.epics.pvdata.pv.Structure;
import org.epics.pvdata.pv.StructureArray;
import org.epics.pvdata.pv.StructureArrayData;


/**
 * Base class for implementing PVStructureArray.
 * @author mrk
 */
public class BasePVStructureArray extends AbstractPVComplexArray implements PVStructureArray
{
	private static final PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
    protected PVStructure[] value;

    protected StructureArray structureArray;

    /**
     * Constructor.
     * @param structureArray The Introspection interface.
     */
    public BasePVStructureArray(StructureArray structureArray)
    {
        super(structureArray);
        this.structureArray = structureArray;
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVStructureArray#getStructureArray()
     */
    public StructureArray getStructureArray() {
		return structureArray;
    }

    @Override
    protected void allocate(int newCapacity) {
    	value = new PVStructure[newCapacity];
    	capacity = newCapacity;
    }

    @Override
    protected Object getValue()
    {
    	return value;
    }

    @Override
    protected void setValue(Object array)
    {
    	value = (PVStructure[])array;
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVStructureArray#get(int, int, org.epics.pvdata.pv.StructureArrayData)
     */
    public int get(int offset, int len, StructureArrayData data) {
    	return internalGet(offset, len, data);
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVStructureArray#put(int, int, org.epics.pvdata.pv.PVStructure[], int)
     */
    public int put(int offset, int len, PVStructure[] from, int fromOffset) {

    	// first check if the PVStructure-s being written are of the right type
    	Structure elementField = structureArray.getStructure();

    	for (int i = 0; i < len; i++) {
    	    PVStructure pvs = from[i];
    	    if (pvs != null && !pvs.getStructure().equals(elementField))
                throw new IllegalStateException("element is not a compatible structure");
        }
    	return internalPut(offset, len, from, fromOffset);
    }


    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVStructureArray#shareData(org.epics.pvdata.pv.PVStructure[])
     */
    public void shareData(PVStructure[] from) {
    	internalShareData(from);
    }

    @Override
    protected boolean valueEquals(Object obj)
    {
		PVStructureArray b = (PVStructureArray)obj;
		StructureArrayData arrayData = new StructureArrayData();
    	// NOTE: this assumes entire array set to arrayData
	    b.get(0, b.getLength(), arrayData);
		return Arrays.equals(arrayData.data, value);
    }

	@Override
	protected Serializable getAt(int index) {
		return value[index];
	}

	@Override
	protected void setAt(int index, Serializable obj) {
		value[index] = (PVStructure)obj;
	}

	@Override
	protected Serializable createNewInstance() {
		return pvDataCreate.createPVStructure(structureArray.getStructure());
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Arrays.hashCode(value);
	}

}
