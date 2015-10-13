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
import org.epics.pvdata.pv.Type;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVDouble;
import org.epics.pvdata.pv.PVLong;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.property.PVTimeStamp;
import org.epics.pvdata.property.PVAlarm;

/**
 * Wrapper class for NTAggregate.
 *
 * @author dgh
 */
public class NTAggregate
    implements HasAlarm, HasTimeStamp
{
    public static final String URI = "epics:nt/NTAggregate:1.0";

    /**
     * Creates an NTAggregate wrapping the specified PVStructure if the latter is compatible.
     * <p>
     * Checks the supplied structure is compatible with NTAggregate
     * and if so returns a NTAggregate which wraps it.
     * This method will return null if the structure is is not compatible
     * or is null.
     * @param pvStructure the PVStructure to be wrapped
     * @return the NTAggregate instance on success, null otherwise
     */
    public static NTAggregate wrap(PVStructure pvStructure)
    {
        if (!isCompatible(pvStructure))
            return null;
        return wrapUnsafe(pvStructure);
    }

    /**
     * Creates an NTAggregate wrapping the specified PVStructure, regardless of the latter's compatibility.
     * <p>
     * No checks are made as to whether the specified PVStructure
     * is compatible with NTAggregate or is non-null.
     *
     * @param pvStructure the PVStructure to be wrapped
     * @return the NTAggregate instance
     */
    public static NTAggregate wrapUnsafe(PVStructure pvStructure)
    {
        return new NTAggregate(pvStructure);
    }

    /**
     * Checks if the specified structure reports to be a compatible NTAggregate.
     * <p>
     * Checks whether the specified structure reports compatibility with this
     * version of NTAggregate through type ID, including checking version numbers.
     * The return value does not depend on whether the structure is actually
     * compatible in terms of its introspection type.
     *
     * @param structure the Structure to test
     * @return (false,true) if (is not, is) a compatible NTAggregate
     */
    public static boolean is_a(Structure structure)
    {
        return NTUtils.is_a(structure.getID(), URI);
    }

    /**
     * Checks if the specified structure reports to be a compatible NTAggregate.
     * <p>
     * Checks whether the specified structure reports compatibility with this
     * version of NTAggregate through type ID, including checking version numbers.
     * The return value does not depend on whether the structure is actually
     * compatible in terms of its introspection type.
     *
     * @param pvStructure the PVStructure to test.
     * @return (false,true) if (is not, is) a compatible NTAggregate.
     */
    public static boolean is_a(PVStructure pvStructure)
    {
        return is_a(pvStructure.getStructure());
    }

    /**
     * Checks if the specified structure is compatible with NTAggregate.
     * <p>
     * Checks whether the specified structure is compatible with this version
     * of NTAggregate through introspection interface.
     *
     * @param structure the Structure to test
     * @return (false,true) if (is not, is) a compatible NTAggregate
     */
    public static boolean isCompatible(Structure structure)
    {
        if (structure == null) return false;

        Scalar valueField = structure.getField(Scalar.class, "value");
        if (valueField == null)
            return false;

       if (valueField.getScalarType() != ScalarType.pvDouble)
            return false;

        Scalar nField = structure.getField(Scalar.class, "N");
        if (nField == null)
            return false;

        if (nField.getScalarType() != ScalarType.pvLong)
            return false;

        Field field = structure.getField("first");
        if (field != null)
        {
            Scalar firstField = structure.getField(Scalar.class, "first");
            if (firstField == null || firstField.getScalarType() != ScalarType.pvDouble)
                return false;
        }

        NTField ntField = NTField.get();

        field = structure.getField("firstTimeStamp");
        if (field != null && !ntField.isTimeStamp(field))
            return false;

        field = structure.getField("last");
        if (field != null)
        {
            Scalar lastField = structure.getField(Scalar.class, "last");
            if (lastField == null || lastField.getScalarType() != ScalarType.pvDouble)
                return false;
        }

        field = structure.getField("lastTimeStamp");
        if (field != null && !ntField.isTimeStamp(field))
            return false;

        field = structure.getField("max");
        if (field != null)
        {
            Scalar maxField = structure.getField(Scalar.class, "max");
            if (maxField == null || maxField.getScalarType() != ScalarType.pvString)
                return false;
        }

        field = structure.getField("min");
        if (field != null)
        {
            Scalar minField = structure.getField(Scalar.class, "min");
            if (minField == null || minField.getScalarType() != ScalarType.pvString)
                return false;
        }

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

        return true;
    }

    /**
     * Checks if the specified structure is compatible with NTAggregate.
     * <p>
     * Checks whether the specified structure is compatible with this version
     * of NTAggregate through introspection interface.
     *
     * @param pvStructure the PVStructure to test
     * @return (false,true) if (is not, is) a compatible NTAggregate
     */
    public static boolean isCompatible(PVStructure pvStructure)
    {
        if (pvStructure == null) return false;

        return isCompatible(pvStructure.getStructure());
    }

    /**
     * Checks if the specified structure is a valid NTAggregate.
     * <p>
     * Checks whether the wrapped structure is valid with respect to this
     * version of NTAggregate.
     *
     * @return (false,true) if (is not, is) a valid NTAggregate
     */
    public boolean isValid()
    {
        return true;
    }

    /**
     * Create an NTAggregate builder instance.
     *
     * @return builder instance.
     */
    public static NTAggregateBuilder createBuilder()
    {
        return new NTAggregateBuilder();
    }

    /**
     * Get the pvStructure.
     *
     * @return PVStructure
     */
    public PVStructure getPVStructure()
    {
        return pvNTAggregate;
    }

    /**
     * Get the value field.
     *
     * @return the PVDouble for the aggregate
     */
    public PVDouble getValue()
    {
        return pvValue;
    }

    /**
     * Get the N field.
     *
     * @return the PVLong for the aggregate
     */
    public PVLong getN()
    {
        return pvNTAggregate.getSubField(PVLong.class, "N");
    }

    /**
     * Get the dispersion field.
     *
     * @return the PVDouble for the aggregate
     */
    public PVDouble getDispersion()
    {
        return pvNTAggregate.getSubField(PVDouble.class, "dispersion");
    }

    /**
     * Get the first field.
     *
     * @return the last value for the aggregate
     */
    public PVDouble getFirst()
    {
        return pvNTAggregate.getSubField(PVDouble.class, "first");
    }

    /**
     * Get the firstTimeStamp field.
     *
     * @return the timeStamp for the first value of the aggregate
     */
    public PVStructure getFirstTimeStamp()
    {
        return pvNTAggregate.getSubField(PVStructure.class, "firstTimeStamp");
    }

    /**
     * Get the last field.
     *
     * @return the last value for the aggregate
     */
    public PVDouble getLast()
    {
        return pvNTAggregate.getSubField(PVDouble.class, "last");
    }

    /**
     * Get the lastTimeStamp field.
     *
     * @return the timeStamp for the last value of the aggregate
     */
    public PVStructure getLastTimeStamp()
    {
        return pvNTAggregate.getSubField(PVStructure.class, "lastTimeStamp");
    }

    /**
     * Get the max field.
     *
     * @return the max for the aggregate
     */
    public PVDouble getMax()
    {
        return pvNTAggregate.getSubField(PVDouble.class, "max");
    }

    /**
     * Get the min field.
     *
     * @return the max for the aggregate
     */
    public PVDouble getMin()
    {
        return pvNTAggregate.getSubField(PVDouble.class, "min");
    }

    /**
     * Get the descriptor field.
     *
     * @return the PVString or null if no descriptor field
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
     *
     * @param pvStructure the PVStructure to be wrapped
     */
    NTAggregate(PVStructure pvStructure)
    {
        pvNTAggregate = pvStructure;
        pvValue = pvNTAggregate.getSubField(PVDouble.class, "value");
    }

    private PVStructure pvNTAggregate;
    private PVDouble pvValue;
}


