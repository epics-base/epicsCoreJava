/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
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
 * Wrapper class for NTMatrix.
 *
 * @author dgh
 */
public class NTMatrix
    implements HasTimeStamp, HasAlarm, HasDisplay
{
    public static final String URI = "epics:nt/NTMatrix:1.0";

    /**
     * Creates an NTMatrix wrapping the specified PVStructure if the latter is compatible.
     * <p>
     * Checks the supplied PVStructure is compatible with NTMatrix
     * and if so returns an NTMatrix which wraps it.
     * This method will return null if the structure is is not compatible
     * or is null.
     *
     * @param pvStructure the PVStructure to be wrapped
     * @return NTMatrix instance on success, null otherwise
     */
    public static NTMatrix wrap(PVStructure pvStructure)
    {
        if (!isCompatible(pvStructure))
            return null;
        return wrapUnsafe(pvStructure);
    }

    /**
     * Creates an NTMatrix wrapping the specified PVStructure, regardless of the latter's compatibility.
     * <p>
     * No checks are made as to whether the specified PVStructure
     * is compatible with NTMatrix or is non-null.
     *
     * @param pvStructure the PVStructure to be wrapped
     * @return NTMatrix instance
     */
    public static NTMatrix wrapUnsafe(PVStructure pvStructure)
    {
        return new NTMatrix(pvStructure);
    }

    /**
     * Returns whether the specified Structure reports to be a compatible NTMatrix.
     * <p>
     * Checks if the specified Structure reports compatibility with this
     * version of NTMatrix through its type ID, including checking version numbers.
     * The return value does not depend on whether the structure is actually
     * compatible in terms of its introspection type.
     *
     * @param structure the structure to test
     * @return (false,true) if (is not, is) a compatible NTMatrix
     */
    public static boolean is_a(Structure structure)
    {
        return NTUtils.is_a(structure.getID(), URI);
    }

    /**
     * Returns whether the specified PVStructure reports to be a compatible NTMatrix.
     * <p>
     * Checks if the specified PVStructure reports compatibility with this
     * version of NTMatrix through its type ID, including checking version numbers.
     * The return value does not depend on whether the structure is actually
     * compatible in terms of its introspection type.
     *
     * @param pvStructure the PVStructure to test
     * @return (false,true) if (is not, is) a compatible NTMatrix
     */
    public static boolean is_a(PVStructure pvStructure)
    {
        return is_a(pvStructure.getStructure());
    }

    /**
     * Returns whether the specified Structure is compatible with NTMatrix.
     * <p>
     * Checks if the specified Structure is compatible with this version
     * of NTMatrix through the introspection interface.
     *
     * @param structure the Structure to test
     * @return (false,true) if (is not, is) a compatible NTMatrix
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
     * Returns whether the specified PVStructure is compatible with NTMatrix.
     * <p>
     * Checks if the specified PVStructure is compatible with this version
     * of NTMatrix through the introspection interface.
     *
     * @param pvStructure the PVStructure to test
     * @return (false,true) if (is not, is) a compatible NTMatrix
     */
    public static boolean isCompatible(PVStructure pvStructure)
    {
        if (pvStructure == null) return false;

        return isCompatible(pvStructure.getStructure());
    }

    /**
     * Checks whether the wrapped PVStructure is valid with respect to this
     * version of NTMatrix.
     * <p>
     * Unlike isCompatible(), isValid() may perform checks on the value
     * data as well as the introspection data.
     *
     * @return (false,true) if wrapped PVStructure (is not, is) a valid NTMatrix
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
     * Creates an NTMatrix builder instance.
     *
     * @return builder instance
     */
    public static NTMatrixBuilder createBuilder()
    {
        return new NTMatrixBuilder();
    }

    /**
     * Returns the PVStructure wrapped by this instance.
     *
     * @return the PVStructure wrapped by this instance
     */
    public PVStructure getPVStructure()
    {
        return pvNTMatrix;
    }

    /**
     * Returns the value field.
     *
     * @return the value field
     */
    public PVDoubleArray getValue()
    {
        return pvValue;
    }

    /**
     * Returns the dim field.
     *
     * @return the dim field or or null if no such field
     */
    public PVIntArray getDim()
    {
        return pvNTMatrix.getSubField(PVIntArray.class, "dim");
    }

    /**
     * Returns the descriptor field.
     *
     * @return the descriptor field or null if no such field
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
     *
     * @param pvStructure the PVStructure to be wrapped
     */
    NTMatrix(PVStructure pvStructure)
    {
        pvNTMatrix = pvStructure;
        pvValue = pvNTMatrix.getSubField(PVDoubleArray.class, "value");
    }

    private PVStructure pvNTMatrix;
    private PVDoubleArray pvValue;
}


