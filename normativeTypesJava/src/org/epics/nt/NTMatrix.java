/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.nt;

import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.Scalar;
import org.epics.pvdata.pv.ScalarArray;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Structure;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVDoubleArray;
import org.epics.pvdata.pv.PVIntArray;
import org.epics.pvdata.pv.IntArrayData;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.property.PVTimeStamp;
import org.epics.pvdata.property.PVAlarm;
import org.epics.pvdata.property.PVDisplay;
import org.epics.pvdata.property.PVControl;

/**
 * Wrapper class for NTMatrix
 *
 * @author dgh
 */
public class NTMatrix
    implements HasTimeStamp, HasAlarm, HasDisplay
{
    public static final String URI = "epics:nt/NTMatrix:1.0";

    /**
     * Creates an NTMatrix wrapping the specified PVStructure if the latter is compatible.
     *
     * Checks the supplied structure is compatible with NTMatrix
     * and if so returns a NTMatrix which wraps it.
     * This method will return null if the structure is is not compatible
     * or is null.
     * @param pvStructure The PVStructure to be wrapped.
     * @return NTMatrix instance on success, null otherwise.
     */
    public static NTMatrix wrap(PVStructure pvStructure)
    {
        if (!isCompatible(pvStructure))
            return null;
        return wrapUnsafe(pvStructure);
    }

    /**
     * Creates an NTMatrix wrapping the specified PVStructure, regardless of the latter's compatibility.
     *
     * No checks are made as to whether the specified PVStructure
     * is compatible with NTMatrix or is non-null.
     * @param pvStructure The PVStructure to be wrapped.
     * @return NTMatrix instance.
     */
    public static NTMatrix wrapUnsafe(PVStructure pvStructure)
    {
        return new NTMatrix(pvStructure);
    }

    /**
     * Checks if the specified structure reports to be a compatible NTMatrix.
     *
     * Checks whether the specified structure reports compatibility with this
     * version of NTMatrix through type ID, including checking version numbers.
     * The return value does not depend on whether the structure is actually
     * compatible in terms of its introspection type
     * @param structure The structure to test.
     * @return (false,true) if (is not, is) a compatible NTMatrix.
     */
    public static boolean is_a(Structure structure)
    {
        return NTUtils.is_a(structure.getID(), URI);
    }

    /**
     * Checks if the specified structure reports to be a compatible NTMatrix.
     *
     * Checks whether the specified structure reports compatibility with this
     * version of NTMatrix through type ID, including checking version numbers.
     * The return value does not depend on whether the structure is actually
     * compatible in terms of its introspection type
     * @param pvStructure The PVStructure to test.
     * @return (false,true) if (is not, is) a compatible NTMatrix.
     */
    public static boolean is_a(PVStructure pvStructure)
    {
        return is_a(pvStructure.getStructure());
    }

    /**
     * Checks if the specified structure is compatible with NTMatrix.
     *
     * Checks whether the specified structure is compatible with this version
     * of NTMatrix through introspection interface.
     * @param structure The Structure to test.
     * @return (false,true) if (is not, is) a compatible NTMatrix.
     */
    public static boolean isCompatible(Structure structure)
    {
        if (structure == null) return false;

        ScalarArray valueField = structure.getField(ScalarArray.class, "value");
        if (valueField == null)
            return false;

        ScalarType scalarType = valueField.getElementType();
        if (scalarType != ScalarType.pvDouble)
            return false;

        Field field = structure.getField("dim");
        if (field != null)
        {
            ScalarArray dimField = structure.getField(ScalarArray.class, "dim");
            if (dimField == null || dimField.getElementType() != ScalarType.pvInt)
                return false;
        }

        NTField ntField = NTField.get();

        field = structure.getField("descriptor");
        if (field != null)
        {
            Scalar descriptorField = structure.getField(Scalar.class, "descriptor");
            if (descriptorField == null || descriptorField.getScalarType() != ScalarType.pvString)
                return false;
        }

        field = structure.getField("alarm");
        if (field != null && !ntField.isAlarm(field))
            return false;

        field = structure.getField("timeStamp");
        if (field != null && !ntField.isTimeStamp(field))
            return false;

        field = structure.getField("display");
        if (field != null && !ntField.isDisplay(field))
            return false;

        return true;
    }

    /**
     * Checks if the specified structure is compatible with NTMatrix.
     *
     * Checks whether the specified structure is compatible with this version
     * of NTMatrix through introspection interface.
     * @param pvStructure The PVStructure to test.
     * @return (false,true) if (is not, is) a compatible NTMatrix.
     */
    public static boolean isCompatible(PVStructure pvStructure)
    {
        return isCompatible(pvStructure.getStructure());
    }

    /**
     * Checks if the specified structure is a valid NTMatrix.
     *
     * Checks whether the wrapped structure is valid with respect to this
     * version of NTMatrix
     * @return (false,true) if (is not, is) a valid NTMatrix.
     */
    public boolean isValid()
    {
        int valueLength = getValue().getLength();
        if (valueLength == 0)
            return false;

        PVIntArray pvDim = getDim();
        if (pvDim != null)
        {
            int length = pvDim.getLength();
            if (length != 1 && length !=2)
                return false;

            IntArrayData data = new IntArrayData();
            pvDim.get(0,length,data);
            int expectedLength = 1;
            for (int d : data.data)
            {
                expectedLength *=d;
            }
            if (expectedLength != valueLength)
                return false; 
        }
        return true;
    }

    /**
     * Create a NTMatrix builder instance.
     * @return builder instance.
     */
    public static NTMatrixBuilder createBuilder()
    {
        return new NTMatrixBuilder();
    }

    /**
     * Get the pvStructure.
     * @return PVStructure.
     */
    public PVStructure getPVStructure()
    {
        return pvNTMatrix;
    }

    /**
     * Get the value field.
     * @return The PVDoubleArray for the values.
     */
    public PVDoubleArray getValue()
    {
        return pvValue;
    }

    /**
     * Get the dim field.
     * @return The PVIntArray for the values.
     */
    public PVIntArray getDim()
    {
        return pvNTMatrix.getSubField(PVIntArray.class, "dim");
    }

    /**
     * Get the descriptor field.
     * @return The pvString or null if no function field.
     */
    public PVString getDescriptor()
    {
        return pvNTMatrix.getSubField(PVString.class, "descriptor");
    }

    /* (non-Javadoc)
	 * @see org.epics.pvdata.nt.HasAlarm#getAlarm()
	 */
    public PVStructure getAlarm()
    {
       return pvNTMatrix.getSubField(PVStructure.class, "alarm");
    }

    /* (non-Javadoc)
	 * @see org.epics.pvdata.nt.HasTimeStamp#getTimeStamp()
	 */
    public PVStructure getTimeStamp()
    {
        return pvNTMatrix.getSubField(PVStructure.class, "timeStamp");
    }

    /* (non-Javadoc)
	 * @see org.epics.pvdata.nt.Has#getDisplay()
	 */
    public PVStructure getDisplay()
    {
       return pvNTMatrix.getSubField(PVStructure.class, "display");
    }


    /* (non-Javadoc)
	 * @see org.epics.pvdata.nt.HasAlarm#attachAlarm(org.epics.pvdata.property.PVAlarm)
	 */
    public boolean attachAlarm(PVAlarm pvAlarm)
    {
        PVStructure al = getAlarm();
        if (al != null)
            return pvAlarm.attach(al);
        else
            return false;
    }

    /* (non-Javadoc)
	 * @see org.epics.pvdata.nt.HasTimeStamp#attachTimeStamp(org.epics.pvdata.property.PVTimeStamp)
	 */
    public boolean attachTimeStamp(PVTimeStamp pvTimeStamp)
    {
        PVStructure ts = getTimeStamp();
        if (ts != null)
            return pvTimeStamp.attach(ts);
        else
            return false;
    }

    /* (non-Javadoc)
	 * @see org.epics.pvdata.nt.HasDisplay#attachDisplay(org.epics.pvdata.property.PVDisplay)
	 */
    public boolean attachDisplay(PVDisplay pvDisplay)
    {
        PVStructure dp = getDisplay();
        if (dp != null)
            return pvDisplay.attach(dp);
        else
            return false;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */ 
    public String toString()
    {
        return getPVStructure().toString();
    }

    /**
     * Constructor
     * @param pvStructure The PVStructure to be wrapped.
     */
    NTMatrix(PVStructure pvStructure)
    {
        pvNTMatrix = pvStructure;
        pvValue = pvNTMatrix.getSubField(PVDoubleArray.class, "value");
    }

    private PVStructure pvNTMatrix;
    private PVDoubleArray pvValue;
}


