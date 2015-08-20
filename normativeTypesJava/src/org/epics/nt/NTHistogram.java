/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.nt;

import org.epics.pvdata.pv.Structure;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVDoubleArray;
import org.epics.pvdata.pv.PVScalarArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.property.PVTimeStamp;
import org.epics.pvdata.property.PVAlarm;

/**
 * Wrapper class for NTHistogram
 *
 * @author dgh
 */
public class NTHistogram
    implements HasTimeStamp, HasAlarm
{
    public static final String URI = "epics:nt/NTHistogram:1.0";

    /**
     * Creates an NTHistogram wrapping the specified PVStructure if the latter is compatible.
     *
     * Checks the supplied structure is compatible with NTHistogram
     * and if so returns a NTHistogram which wraps it.
     * This method will return null if the structure is is not compatible
     * or is null.
     * @param pvStructure The PVStructure to be wrapped.
     * @return NTHistogram instance on success, null otherwise.
     */
    public static NTHistogram wrap(PVStructure pvStructure)
    {
        if (!isCompatible(pvStructure))
            return null;
        return wrapUnsafe(pvStructure);
    }

    /**
     * Creates an NTHistogram wrapping the specified PVStructure, regardless of the latter's compatibility.
     *
     * No checks are made as to whether the specified PVStructure
     * is compatible with NTHistogram or is non-null.
     * @param pvStructure The PVStructure to be wrapped.
     * @return NTHistogram instance.
     */
    public static NTHistogram wrapUnsafe(PVStructure pvStructure)
    {
        return new NTHistogram(pvStructure);
    }

    /**
     * Checks if the specified structure reports to be a compatible NTHistogram.
     *
     * Checks whether the specified structure reports compatibility with this
     * version of NTHistogram through type ID, including checking version numbers.
     * The return value does not depend on whether the structure is actually
     * compatible in terms of its introspection type
     * @param structure The pvStructure to test.
     * @return (false,true) if (is not, is) a compatible NTHistogram.
     */
    public static boolean is_a(Structure structure)
    {
        return NTUtils.is_a(structure.getID(), URI);
    }

    /**
     * Checks if the specified structure reports to be a compatible NTHistogram.
     *
     * Checks whether the specified structure reports compatibility with this
     * version of NTHistogram through type ID, including checking version numbers.
     * The return value does not depend on whether the structure is actually
     * compatible in terms of its introspection type
     * @param structure The pvStructure to test.
     * @return (false,true) if (is not, is) a compatible NTHistogram.
     */
    public static boolean is_a(PVStructure pvStructure)
    {
        return is_a(pvStructure.getStructure());
    }

    /**
     * Checks if the specified structure is compatible with NTHistogram.
     *
     * Checks whether the specified structure is compatible with this version
     * of NTHistogram through introspection interface.
     * @param structure The Structure to test.
     * @return (false,true) if (is not, is) a compatible NTHistogram.
     */
    public static boolean isCompatible(Structure structure)
    {
        // TODO implement through introspection interface
        return isCompatible(org.epics.pvdata.factory.PVDataFactory.
            getPVDataCreate().createPVStructure(structure));
    }

    /**
     * Checks if the specified structure is compatible with NTHistogram.
     *
     * Checks whether the specified structure is compatible with this version
     * of NTHistogram through introspection interface.
     * @param pvStructure The PVStructure to test.
     * @return (false,true) if (is not, is) a compatible NTHistogram.
     */
    public static boolean isCompatible(PVStructure pvStructure)
    {
        if (pvStructure == null) return false;

        PVField pvField = pvStructure.getSubField("ranges");
        if (pvField != null && pvStructure.getSubField(PVDoubleArray.class, "ranges") == null)
            return false;

        PVScalarArray pvValue = pvStructure.getSubField(PVDoubleArray.class, "value");
        if (pvValue == null) return false;
        switch(pvValue.getScalarArray().getElementType())
        {
        case pvShort:
        case pvInt:
        case pvLong:
            break;
        default:
            return false;
        }

        pvField = pvStructure.getSubField("descriptor");
        if (pvField != null && pvStructure.getSubField(PVString.class, "descriptor") == null)
            return false;

        NTField ntField = NTField.get();

        pvField = pvStructure.getSubField("alarm");
        if (pvField != null  && !ntField.isAlarm(pvField.getField()))
            return false;

        pvField = pvStructure.getSubField("timeStamp");
        if (pvField != null && !ntField.isTimeStamp(pvField.getField()))
            return false;

        return true;
    }


    /**
     * Checks if the specified structure is a valid NTHistogram.
     *
     * Checks whether the wrapped structure is valid with respect to this
     * version of NTHistogram
     * @return (false,true) if (is not, is) a valid NTHistogram.
     */
    public boolean isValid()
    {
        return true;
    }

    /**
     * Create a NTHistogram builder instance.
     * @return builder instance.
     */
    public static NTHistogramBuilder createBuilder()
    {
        return new NTHistogramBuilder();
    }

    /**
     * Get the pvStructure.
     * @return PVStructure.
     */
    public PVStructure getPVStructure()
    {
        return pvNTHistogram;
    }

    /**
     * Get the ranges field.
     * @return The PVDoubleArray for the values.
     */
    public PVDoubleArray getRanges()
    {
        return pvNTHistogram.getSubField(PVDoubleArray.class, "ranges");
    }

    /**
     * Get the value field.
     * @return The PVScalarArray for the values.
     */
    public PVScalarArray getValue()
    {
        return pvValue;
    }


    /* Get the value field of a specified type.
     * @param c expected class of a requested field (must be PVShortArray, PVIntArray or PVLongArray).
     * @return The PVScalarArray or null if the subfield does not exist, or the field is not of <code>c</code> type.
     */
    public <T extends PVScalarArray> T getValue(Class<T> c)
    {
        if (c.isInstance(pvValue))
            return c.cast(pvValue);
        else
            return null;
    }


    /**
     * Get the descriptor field.
     * @return The pvString or null if no function field.
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
     * Constructor
     * @param pvStructure The PVStructure to be wrapped.
     */
    NTHistogram(PVStructure pvStructure)
    {
        pvNTHistogram = pvStructure;
        pvValue = pvNTHistogram.getSubField(PVScalarArray.class, "value");
    }

    private PVStructure pvNTHistogram;
    private PVScalarArray pvValue;
}


