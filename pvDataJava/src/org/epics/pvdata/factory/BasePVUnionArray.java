/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.factory;

import java.util.Arrays;

import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVUnion;
import org.epics.pvdata.pv.PVUnionArray;
import org.epics.pvdata.pv.Serializable;
import org.epics.pvdata.pv.Union;
import org.epics.pvdata.pv.UnionArray;
import org.epics.pvdata.pv.UnionArrayData;


/**
 * Base class for implementing PVUnionArray.
 * @author mse
 *
 */
public class BasePVUnionArray extends AbstractPVComplexArray implements PVUnionArray
{
	private static final PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
    protected PVUnion[] value;

	protected UnionArray unionArray;

    /**
     * Constructor.
     * @param unionArray The Introspection interface.
     */
    public BasePVUnionArray(UnionArray unionArray)
    {
        super(unionArray);
        this.unionArray = unionArray;
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVUnionArray#getUnionArray()
     */
    public UnionArray getUnionArray() {
		return unionArray;
    }

    @Override
    protected void allocate(int newCapacity) {
    	value = new PVUnion[newCapacity];
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
    	value = (PVUnion[])array;
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVUnionArray#get(int, int, org.epics.pvdata.pv.UnionArrayData)
     */
    public int get(int offset, int len, UnionArrayData data) {
    	return internalGet(offset, len, data);
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVUnionArray#put(int, int, org.epics.pvdata.pv.PVUnion[], int)
     */
    public int put(int offset, int len, PVUnion[] from, int fromOffset) {

    	// first check if all the PVUnion-s are of the right type
    	Union elementField = unionArray.getUnion();
    	for (PVUnion pvu : from)
    		if (pvu != null && !pvu.getUnion().equals(elementField))
    			throw new IllegalStateException("element is not a compatible union");

    	return internalPut(offset, len, from, fromOffset);
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVUnionArray#shareData(org.epics.pvdata.pv.PVUnion[])
     */
    public void shareData(PVUnion[] from) {
    	internalShareData(from);
    }

    @Override
    protected boolean valueEquals(Object obj)
    {
		PVUnionArray b = (PVUnionArray)obj;
		UnionArrayData arrayData = new UnionArrayData();
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
		value[index] = (PVUnion)obj;
	}

	@Override
	protected Serializable createNewInstance() {
		return pvDataCreate.createPVUnion(unionArray.getUnion());
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Arrays.hashCode(value);
	}

}
