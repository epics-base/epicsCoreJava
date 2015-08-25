/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.nt;

import org.epics.pvdata.pv.Structure;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVDouble;
import org.epics.pvdata.pv.PVLong;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.property.PVTimeStamp;
import org.epics.pvdata.property.PVAlarm;

/**
 * Wrapper class for NTAggregate
 *
 * @author dgh
 */
public class NTAggregate
    implements HasAlarm, HasTimeStamp
{
    public static final String URI = "epics:nt/NTAggregate:1.0";

    /**
     * Creates an NTAggregate wrapping the specified PVStructure if the latter is compatible.
     *
     * Checks the supplied structure is compatible with NTAggregate
     * and if so returns a NTAggregate which wraps it.
     * This method will return null if the structure is is not compatible
     * or is null.
     * @param pvStructure The PVStructure to be wrapped.
     * @return NTAggregate instance on success, null otherwise.
     */
    public static NTAggregate wrap(PVStructure pvStructure)
    {
        if (!isCompatible(pvStructure))
            return null;
        return wrapUnsafe(pvStructure);
    }

    /**
     * Creates an NTAggregate wrapping the specified PVStructure, regardless of the latter's compatibility.
     *
     * No checks are made as to whether the specified PVStructure
     * is compatible with NTAggregate or is non-null.
     * @param pvStructure The PVStructure to be wrapped.
     * @return NTAggregate instance.
     */
    public static NTAggregate wrapUnsafe(PVStructure pvStructure)
    {
        return new NTAggregate(pvStructure);
    }

    /**
     * Checks if the specified structure reports to be a compatible NTAggregate.
     *
     * Checks whether the specified structure reports compatibility with this
     * version of NTAggregate through type ID, including checking version numbers.
     * The return value does not depend on whether the structure is actually
     * compatible in terms of its introspection type
     * @param structure The Structure to test.
     * @return (false,true) if (is not, is) a compatible NTAggregate.
     */
    public static boolean is_a(Structure structure)
    {
        return NTUtils.is_a(structure.getID(), URI);
    }

    /**
     * Checks if the specified structure reports to be a compatible NTAggregate.
     *
     * Checks whether the specified structure reports compatibility with this
     * version of NTAggregate through type ID, including checking version numbers.
     * The return value does not depend on whether the structure is actually
     * compatible in terms of its introspection type
     * @param pvStructure The PVStructure to test.
     * @return (false,true) if (is not, is) a compatible NTAggregate.
     */
    public static boolean is_a(PVStructure pvStructure)
    {
        return is_a(pvStructure.getStructure());
    }

    /**
     * Checks if the specified structure is compatible with NTAggregate.
     *
     * Checks whether the specified structure is compatible with this version
     * of NTAggregate through introspection interface.
     * @param structure The Structure to test.
     * @return (false,true) if (is not, is) a compatible NTAggregate.
     */
    public static boolean isCompatible(Structure structure)
    {
        // TODO implement through introspection interface
        return isCompatible(org.epics.pvdata.factory.PVDataFactory.
            getPVDataCreate().createPVStructure(structure));
    }

    /**
     * Checks if the specified structure is compatible with NTAggregate.
     *
     * Checks whether the specified structure is compatible with this version
     * of NTAggregate through introspection interface.
     * @param pvStructure The PVStructure to test.
     * @return (false,true) if (is not, is) a compatible NTAggregate.
     */
    public static boolean isCompatible(PVStructure pvStructure)
    {
        if (pvStructure == null) return false;

        PVDouble pvValue = pvStructure.getSubField(PVDouble.class, "value");
        if (pvValue == null) return false;

        PVLong pvLong = pvStructure.getSubField(PVLong.class, "N");
        if (pvLong == null) return false;

        if (pvStructure.getSubField("dispersion") != null &&
            pvStructure.getSubField(PVDouble.class, "dispersion") == null)
 return false;

        NTField ntField = NTField.get();

        if (pvStructure.getSubField("first") != null &&
            pvStructure.getSubField(PVDouble.class, "first") == null)
 return false;

        PVField pvField = pvStructure.getSubField("firstTimeStamp");
        if (pvField != null && !ntField.isTimeStamp(pvField.getField()))
 return false;

        if (pvStructure.getSubField("last") != null &&
            pvStructure.getSubField(PVDouble.class, "last") == null)
 return false;

        pvField = pvStructure.getSubField("lastTimeStamp");
        if (pvField != null && !ntField.isTimeStamp(pvField.getField()))
 return false;

        if (pvStructure.getSubField("max") != null &&
            pvStructure.getSubField(PVDouble.class, "max") == null)
 return false;

        pvField = pvStructure.getSubField("descriptor");
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
     * Checks if the specified structure is a valid NTAggregate.
     *
     * Checks whether the wrapped structure is valid with respect to this
     * version of NTAggregate
     * @return (false,true) if (is not, is) a valid NTAggregate.
     */
    public boolean isValid()
    {
        return true;
    }

    /**
     * Create an NTAggregate builder instance.
     * @return builder instance.
     */
    public static NTAggregateBuilder createBuilder()
    {
        return new NTAggregateBuilder();
    }

    /**
     * Get the pvStructure.
     * @return PVStructure.
     */
    public PVStructure getPVStructure()
    {
        return pvNTAggregate;
    }

    /**
     * Get the value field.
     * @return The PVDouble for the aggregate.
     */
    public PVDouble getValue()
    {
        return pvValue;
    }

    /**
     * Get the N field.
     * @return The PVLong for the aggregate.
     */
    public PVLong getN()
    {
        return pvNTAggregate.getSubField(PVLong.class, "N");
    }

    /**
     * Get the dispersion field.
     * @return The PVDouble for the aggregate.
     */
    public PVDouble getDispersion()
    {
        return pvNTAggregate.getSubField(PVDouble.class, "dispersion");
    }

    /**
     * Get the first field.
     * @return The last value for the aggregate.
     */
    public PVDouble getFirst()
    {
        return pvNTAggregate.getSubField(PVDouble.class, "first");
    }

    /**
     * Get the firstTimeStamp field.
     * @return The timeStamp for the first value of the aggregate.
     */
    public PVStructure getFirstTimeStamp()
    {
        return pvNTAggregate.getSubField(PVStructure.class, "firstTimeStamp");
    }

    /**
     * Get the last field.
     * @return The last value for the aggregate.
     */
    public PVDouble getLast()
    {
        return pvNTAggregate.getSubField(PVDouble.class, "last");
    }

    /**
     * Get the lastTimeStamp field.
     * @return The timeStamp for the last value of the aggregate.
     */
    public PVStructure getLastTimeStamp()
    {
        return pvNTAggregate.getSubField(PVStructure.class, "lastTimeStamp");
    }

    /**
     * Get the max field.
     * @return The max for the aggregate.
     */
    public PVDouble getMax()
    {
        return pvNTAggregate.getSubField(PVDouble.class, "max");
    }

    /**
     * Get the min field.
     * @return The max for the aggregate.
     */
    public PVDouble getMin()
    {
        return pvNTAggregate.getSubField(PVDouble.class, "min");
    }

    /**
     * Get the descriptor field.
     * @return The pvString or null if no function field.
     */
    public PVString getDescriptor()
    {
        return pvNTAggregate.getSubField(PVString.class, "descriptor");
    }

    /* (non-Javadoc)
	 * @see org.epics.pvdata.nt.HasAlarm#getAlarm()
	 */
    public PVStructure getAlarm()
    {
       return pvNTAggregate.getSubField(PVStructure.class, "alarm");
    }

    /* (non-Javadoc)
	 * @see org.epics.pvdata.nt.HasTimeStamp#getTimeStamp()
	 */
    public PVStructure getTimeStamp()
    {
        return pvNTAggregate.getSubField(PVStructure.class, "timeStamp");
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
    NTAggregate(PVStructure pvStructure)
    {
        pvNTAggregate = pvStructure;
        pvValue = pvNTAggregate.getSubField(PVDouble.class, "value");
    }

    private PVStructure pvNTAggregate;
    private PVDouble pvValue;
}


