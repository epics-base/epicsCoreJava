/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.nt;

import org.epics.pvdata.pv.Structure;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVInt;
import org.epics.pvdata.pv.PVScalar;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStringArray;
import org.epics.pvdata.property.PVTimeStamp;
import org.epics.pvdata.property.PVAlarm;
import org.epics.pvdata.property.PVDisplay;
import org.epics.pvdata.property.PVControl;

/**
 * Wrapper class for NTEnum
 *
 * @author dgh
 */
public class NTEnum
    implements HasAlarm, HasTimeStamp
{
    public static final String URI = "epics:nt/NTEnum:1.0";

    /**
     * Creates an NTEnum wrapping the specified PVStructure if the latter is compatible.
     *
     * Checks the supplied structure is compatible with NTEnum
     * and if so returns a NTEnum which wraps it.
     * This method will return null if the structure is is not compatible
     * or is null.
     * @param pvStructure The PVStructure to be wrapped.
     * @return NTEnum instance on success, null otherwise.
     */
    public static NTEnum wrap(PVStructure pvStructure)
    {
        if (!isCompatible(pvStructure))
            return null;
        return wrapUnsafe(pvStructure);
    }

    /**
     * Creates an NTEnum wrapping the specified PVStructure, regardless of the latter's compatibility.
     *
     * No checks are made as to whether the specified PVStructure
     * is compatible with NTEnum or is non-null.
     * @param pvStructure The PVStructure to be wrapped.
     * @return NTEnum instance.
     */
    public static NTEnum wrapUnsafe(PVStructure pvStructure)
    {
        return new NTEnum(pvStructure);
    }

    /**
     * Checks if the specified structure reports to be a compatible NTEnum.
     *
     * Checks whether the specified structure reports compatibility with this
     * version of NTEnum through type ID, including checking version numbers.
     * The return value does not depend on whether the structure is actually
     * compatible in terms of its introspection type
     * @param structure The pvStructure to test.
     * @return (false,true) if (is not, is) a compatible NTEnum.
     */
    public static boolean is_a(Structure structure)
    {
        return NTUtils.is_a(structure.getID(), URI);
    }

    /**
     * Checks if the specified structure is compatible with NTEnum.
     *
     * Checks whether the specified structure is compatible with this version
     * of NTEnum through introspection interface.
     * @param pvStructure The PVStructure to test.
     * @return (false,true) if (is not, is) a compatible NTEnum.
     */
    public static boolean isCompatible(PVStructure pvStructure)
    {
        if (pvStructure == null) return false;

        PVStructure pvValue = pvStructure.getSubField(PVStructure.class, "value");
        if (pvValue == null) return false;

        NTField ntField = NTField.get();

        if (!ntField.isEnumerated(pvValue.getField())) return false;

        PVField pvField = pvStructure.getSubField("descriptor");
        if (pvField != null && pvStructure.getSubField(PVString.class, "descriptor") == null)
            return false;

        pvField = pvStructure.getSubField("alarm");
        if (pvField != null  && !ntField.isAlarm(pvField.getField()))
            return false;

        pvField = pvStructure.getSubField("timeStamp");
        if (pvField != null && !ntField.isTimeStamp(pvField.getField()))
            return false;

        return true;
    }

    /**
     * Create an NTEnum builder instance.
     * @return builder instance.
     */
    public static NTEnumBuilder createBuilder()
    {
        return new NTEnumBuilder();
    }

    /**
     * Get the pvStructure.
     * @return PVStructure.
     */
    public PVStructure getPVStructure()
    {
        return pvNTEnum;
    }

    /**
     * Get the value field.
     * @return The PVStructure for the values.
     */
    public PVStructure getValue()
    {
        return pvValue;
    }

    /**
     * Get the descriptor field.
     * @return The pvString or null if no function field.
     */
    public PVString getDescriptor()
    {
        return pvNTEnum.getSubField(PVString.class, "descriptor");
    }

    /* (non-Javadoc)
	 * @see org.epics.pvdata.nt.HasAlarm#getAlarm()
	 */
    public PVStructure getAlarm()
    {
       return pvNTEnum.getSubField(PVStructure.class, "alarm");
    }

    /* (non-Javadoc)
	 * @see org.epics.pvdata.nt.HasTimeStamp#getTimeStamp()
	 */
    public PVStructure getTimeStamp()
    {
        return pvNTEnum.getSubField(PVStructure.class, "timeStamp");
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
    NTEnum(PVStructure pvStructure)
    {
        pvNTEnum = pvStructure;
        pvValue = pvNTEnum.getSubField(PVStructure.class, "value");
    }

    private PVStructure pvNTEnum;
    private PVStructure pvValue;
}


