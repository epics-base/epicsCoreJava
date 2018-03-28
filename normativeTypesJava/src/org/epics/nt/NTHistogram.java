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
import org.epics.pvdata.pv.PVScalarArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.property.PVTimeStamp;
import org.epics.pvdata.property.PVAlarm;

/**
 * Wrapper class for NTHistogram.
 *
 * @author dgh
 */
public class NTHistogram
    implements HasTimeStamp, HasAlarm
{
    public static final String URI = "epics:nt/NTHistogram:1.0";

    /**
     * Creates an NTHistogram wrapping the specified PVStructure if the latter is compatible.
     * <p>
     * Checks the supplied PVStructure is compatible with NTHistogram
     * and if so returns an NTHistogram which wraps it.
     * This method will return null if the structure is is not compatible
     * or is null.
     *
     * @param pvStructure the PVStructure to be wrapped
     * @return NTHistogram instance on success, null otherwise
     */
    public static NTHistogram wrap(PVStructure pvStructure)
    {
        if (!isCompatible(pvStructure))
            return null;
        return wrapUnsafe(pvStructure);
    }

    /**
     * Creates an NTHistogram wrapping the specified PVStructure, regardless of the latter's compatibility.
     * <p>
     * No checks are made as to whether the specified PVStructure
     * is compatible with NTHistogram or is non-null.
     *
     * @param pvStructure the PVStructure to be wrapped.
     * @return NTHistogram instance.
     */
    public static NTHistogram wrapUnsafe(PVStructure pvStructure)
    {
        return new NTHistogram(pvStructure);
    }

    /**
     * Returns whether the specified Structure reports to be a compatible NTHistogram.
     * <p>
     * Checks if the specified Structure reports compatibility with this
     * version of NTHistogram through its type ID, including checking version numbers.
     * The return value does not depend on whether the structure is actually
     * compatible in terms of its introspection type.
     *
     * @param structure the Structure to test.
     * @return (false,true) if (is not, is) a compatible NTHistogram
     */
    public static boolean is_a(Structure structure)
    {
        return NTUtils.is_a(structure.getID(), URI);
    }

    /**
     * Returns whether the specified PVStructure reports to be a compatible NTHistogram.
     * <p>
     * Checks if the specified PVStructure reports compatibility with this
     * version of NTHistogram through its type ID, including checking version numbers.
     * The return value does not depend on whether the structure is actually
     * compatible in terms of its introspection type.
     *
     * @param pvStructure the PVStructure to test
     * @return (false,true) if (is not, is) a compatible NTHistogram
     */
    public static boolean is_a(PVStructure pvStructure)
    {
        return is_a(pvStructure.getStructure());
    }

    /**
     * Returns whether the specified Structure is compatible with NTHistogram.
     * <p>
     * Checks if the specified Structure is compatible with this version
     * of NTHistogram through the introspection interface.
     *
     * @param structure the Structure to test
     * @return (false,true) if (is not, is) a compatible NTHistogram
     */
    public static boolean isCompatible(Structure structure)
    {
        if (structure == null) return false;

        ScalarArray rangesField = structure.getField(ScalarArray.class, "ranges");
        if (rangesField == null)
            return false;

        if (rangesField.getElementType() != ScalarType.pvDouble)
            return false;

        ScalarArray valueField = structure.getField(ScalarArray.class, "value");
        if (valueField == null)
            return false;

        ScalarType scalarType = valueField.getElementType();
        if (scalarType != ScalarType.pvShort &&
            scalarType != ScalarType.pvInt &&
            scalarType != ScalarType.pvLong)
            return false;

        NTField ntField = NTField.get();

        Field field = structure.getField("descriptor");
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

        return true;
    }

    /**
     * Returns whether the specified PVStructure is compatible with NTHistogram.
     * <p>
     * Checks if the specified PVStructure is compatible with this version
     * of NTHistogram through the introspection interface.
     *
     * @param pvStructure the PVStructure to test
     * @return (false,true) if (is not, is) a compatible NTHistogram
     */
    public static boolean isCompatible(PVStructure pvStructure)
    {
        if (pvStructure == null) return false;

        return isCompatible(pvStructure.getStructure());
    }

    /**
     * Returns whether the wrapped structure is valid with respect to this
     * version of NTHistogram.
     * <p>
     * Unlike isCompatible(), isValid() may perform checks on the value
     * data as well as the introspection data.
     *
     * @return (false,true) if wrapped PVStructure (is not, is) a valid NTHistogram
     */
    public boolean isValid()
    {
        return (getValue().getLength()+1 == getRanges().getLength());
    }

    /**
     * Creates an NTHistogram builder instance.
     * @return builder instance.
     */
    public static NTHistogramBuilder createBuilder()
    {
        return new NTHistogramBuilder();
    }

    /**
     * Returns the PVStructure wrapped by this instance.
     *
     * @return the PVStructure wrapped by this instance
     */
    public PVStructure getPVStructure()
    {
        return pvNTHistogram;
    }

    /**
     * Returns the ranges field.
     *
     * @return the ranges field
     */
    public PVDoubleArray getRanges()
    {
        return pvNTHistogram.getSubField(PVDoubleArray.class, "ranges");
    }

    /**
     * Returns the value field.
     *
     * @return the value field
     */
    public PVScalarArray getValue()
    {
        return pvValue;
    }

    /** 
     * Returns the value of a specified type.
     *
     * @param <T> the expected type of the value field
     * @param c class object modeling the class T (must be PVShortArray, PVIntArray or PVLongArray)
     * @return the PVScalarArray or null if the subfield does not exist, or the field is not of <code>c</code> type
     */
    public <T extends PVScalarArray> T getValue(Class<T> c)
    {
        if (c.isInstance(pvValue))
            return c.cast(pvValue);
        else
            return null;
    }

    /**
     * Returns the descriptor field.
     *
     * @return the descriptor field or null if no such field
     */
    public PVString getDescriptor()
    {
        return pvNTHistogram.getSubField(PVString.class, "descriptor");
    }

    /* (non-Javadoc)
	 * @see org.epics.pvdata.nt.HasAlarm#getAlarm()
	 */
    public PVStructure getAlarm()
    {
       return pvNTHistogram.getSubField(PVStructure.class, "alarm");
    }

    /* (non-Javadoc)
	 * @see org.epics.pvdata.nt.HasTimeStamp#getTimeStamp()
	 */
    public PVStructure getTimeStamp()
    {
        return pvNTHistogram.getSubField(PVStructure.class, "timeStamp");
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
     * @see java.lang.Object#toString()
     */ 
    public String toString()
    {
        return getPVStructure().toString();
    }

    /**
     * Constructor.
     *
     * @param pvStructure the PVStructure to be wrapped
     */
    NTHistogram(PVStructure pvStructure)
    {
        pvNTHistogram = pvStructure;
        pvValue = pvNTHistogram.getSubField(PVScalarArray.class, "value");
    }

    private PVStructure pvNTHistogram;
    private PVScalarArray pvValue;
}


