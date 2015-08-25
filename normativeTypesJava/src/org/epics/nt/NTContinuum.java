/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.nt;

import org.epics.pvdata.pv.Structure;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVDoubleArray;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStringArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.property.PVTimeStamp;
import org.epics.pvdata.property.PVAlarm;

/**
 * Wrapper class for NTContinuum
 *
 * @author dgh
 */
public class NTContinuum
    implements HasTimeStamp, HasAlarm
{
    public static final String URI = "epics:nt/NTContinuum:1.0";

    /**
     * Creates an NTContinuum wrapping the specified PVStructure if the latter is compatible.
     *
     * Checks the supplied structure is compatible with NTContinuum
     * and if so returns a NTContinuum which wraps it.
     * This method will return null if the structure is is not compatible
     * or is null.
     * @param pvStructure The PVStructure to be wrapped.
     * @return NTContinuum instance on success, null otherwise.
     */
    public static NTContinuum wrap(PVStructure pvStructure)
    {
        if (!isCompatible(pvStructure))
            return null;
        return wrapUnsafe(pvStructure);
    }

    /**
     * Creates an NTContinuum wrapping the specified PVStructure, regardless of the latter's compatibility.
     *
     * No checks are made as to whether the specified PVStructure
     * is compatible with NTContinuum or is non-null.
     * @param pvStructure The PVStructure to be wrapped.
     * @return NTContinuum instance.
     */
    public static NTContinuum wrapUnsafe(PVStructure pvStructure)
    {
        return new NTContinuum(pvStructure);
    }

    /**
     * Checks if the specified structure reports to be a compatible NTContinuum.
     *
     * Checks whether the specified structure reports compatibility with this
     * version of NTContinuum through type ID, including checking version numbers.
     * The return value does not depend on whether the structure is actually
     * compatible in terms of its introspection type
     * @param structure The Structure to test.
     * @return (false,true) if (is not, is) a compatible NTContinuum.
     */
    public static boolean is_a(Structure structure)
    {
        return NTUtils.is_a(structure.getID(), URI);
    }

    /**
     * Checks if the specified structure reports to be a compatible NTContinuum.
     *
     * Checks whether the specified structure reports compatibility with this
     * version of NTContinuum through type ID, including checking version numbers.
     * The return value does not depend on whether the structure is actually
     * compatible in terms of its introspection type
     * @param pvStructure The PVStructure to test.
     * @return (false,true) if (is not, is) a compatible NTContinuum.
     */
    public static boolean is_a(PVStructure pvStructure)
    {
        return is_a(pvStructure.getStructure());
    }

    /**
     * Checks if the specified structure is compatible with NTScalar.
     *
     * Checks whether the specified structure is compatible with this version
     * of NTScalar through introspection interface.
     * @param structure The Structure to test.
     * @return (false,true) if (is not, is) a compatible NTScalar.
     */
    public static boolean isCompatible(Structure structure)
    {
        // TODO implement through introspection interface
        return isCompatible(org.epics.pvdata.factory.PVDataFactory.
            getPVDataCreate().createPVStructure(structure));
    }

    /**
     * Checks if the specified structure is compatible with NTContinuum.
     *
     * Checks whether the specified structure is compatible with this version
     * of NTContinuum through introspection interface.
     * @param pvStructure The PVStructure to test.
     * @return (false,true) if (is not, is) a compatible NTContinuum.
     */
    public static boolean isCompatible(PVStructure pvStructure)
    {
        if (pvStructure == null) return false;

        PVDoubleArray pvBase = pvStructure.getSubField(PVDoubleArray.class, "base");
        if (pvBase == null) return false;

        PVDoubleArray pvValue = pvStructure.getSubField(PVDoubleArray.class, "value");
        if (pvValue == null) return false;

        PVStringArray pvUnits = pvStructure.getSubField(PVStringArray.class, "units");
        if (pvUnits == null) return false;

        PVField pvField = pvStructure.getSubField("descriptor");
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
     * Checks if the specified structure is a valid NTScalar.
     *
     * Checks whether the wrapped structure is valid with respect to this
     * version of NTScalar
     * @return (false,true) if (is not, is) a valid NTScalar.
     */
    public boolean isValid()
    {
        return true;
    }

    /**
     * Create a NTContinuum builder instance.
     * @return builder instance.
     */
    public static NTContinuumBuilder createBuilder()
    {
        return new NTContinuumBuilder();
    }

    /**
     * Get the pvStructure.
     * @return PVStructure.
     */
    public PVStructure getPVStructure()
    {
        return pvNTContinuum;
    }

    /**
     * Get the base field.
     * @return The PVDoubleArray for the values.
     */
    public PVDoubleArray getBase()
    {
        return pvNTContinuum.getSubField(PVDoubleArray.class, "base");
    }

    /**
     * Get the value field.
     * @return The PVScalarArray for the values.
     */
    public PVDoubleArray getValue()
    {
        return pvValue;
    }

    /**
     * Get the units field.
     * @return The pvString or null if no function field.
     */
    public PVStringArray getUnits()
    {
        return pvNTContinuum.getSubField(PVStringArray.class, "units");
    }

    /**
     * Get the descriptor field.
     * @return The pvString or null if no function field.
     */
    public PVString getDescriptor()
    {
        return pvNTContinuum.getSubField(PVString.class, "descriptor");
    }

    /* (non-Javadoc)
	 * @see org.epics.pvdata.nt.HasAlarm#getAlarm()
	 */
    public PVStructure getAlarm()
    {
       return pvNTContinuum.getSubField(PVStructure.class, "alarm");
    }

    /* (non-Javadoc)
	 * @see org.epics.pvdata.nt.HasTimeStamp#getTimeStamp()
	 */
    public PVStructure getTimeStamp()
    {
        return pvNTContinuum.getSubField(PVStructure.class, "timeStamp");
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
    NTContinuum(PVStructure pvStructure)
    {
        pvNTContinuum = pvStructure;
        pvValue = pvNTContinuum.getSubField(PVDoubleArray.class, "value");
    }

    private PVStructure pvNTContinuum;
    private PVDoubleArray pvValue;
}


