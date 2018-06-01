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
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStringArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.property.PVTimeStamp;
import org.epics.pvdata.property.PVAlarm;

/**
 * Wrapper class for NTContinuum.
 *
 * @author dgh
 */
public class NTContinuum
    implements HasTimeStamp, HasAlarm
{
    public static final String URI = "epics:nt/NTContinuum:1.0";

    /**
     * Creates an NTContinuum wrapping the specified PVStructure if the latter is compatible.
     * <p>
     * Checks the supplied structure is compatible with NTContinuum
     * and if so returns an NTContinuum which wraps it.
     * This method will return null if the structure is is not compatible
     * or is null.
     *
     * @param pvStructure the PVStructure to be wrapped
     * @return NTContinuum instance on success, null otherwise
     */
    public static NTContinuum wrap(PVStructure pvStructure)
    {
        if (!isCompatible(pvStructure))
            return null;
        return wrapUnsafe(pvStructure);
    }

    /**
     * Creates an NTContinuum wrapping the specified PVStructure, regardless of the latter's compatibility.
     * <p>
     * No checks are made as to whether the specified PVStructure
     * is compatible with NTContinuum or is non-null.
     *
     * @param pvStructure the PVStructure to be wrapped
     * @return NTContinuum instance.
     */
    public static NTContinuum wrapUnsafe(PVStructure pvStructure)
    {
        return new NTContinuum(pvStructure);
    }

    /**
     * Returns whether the specified Structure reports to be a compatible NTContinuum.
     * <p>
     * Checks if the specified Structure reports compatibility with this
     * version of NTContinuum through its type ID, including checking version numbers.
     * The return value does not depend on whether the structure is actually
     * compatible in terms of its introspection type
     *
     * @param structure the Structure to test
     * @return (false,true) if (is not, is) a compatible NTContinuum
     */
    public static boolean is_a(Structure structure)
    {
        return NTUtils.is_a(structure.getID(), URI);
    }

    /**
     * Returns whether the specified PVStructure reports to be a compatible NTContinuum.
     * <p>
     * Checks if the specified Structure reports compatibility with this
     * version of NTContinuum through its type ID, including checking version numbers.
     * The return value does not depend on whether the structure is actually
     * compatible in terms of its introspection type
     *
     * @param pvStructure The PVStructure to test
     * @return (false,true) if (is not, is) a compatible NTContinuum
     */
    public static boolean is_a(PVStructure pvStructure)
    {
        return is_a(pvStructure.getStructure());
    }

    /**
     * Returns whether the specified Structure is compatible with NTContinuum.
     * <p>
     * Checks if the specified Structure is compatible with this version
     * of NTContinuum through the introspection interface.
     *
     * @param structure The Structure to test
     * @return (false,true) if (is not, is) a compatible NTContinuum
     */
    public static boolean isCompatible(Structure structure)
    {
        if (structure == null) return false;

        ScalarArray baseField = structure.getField(ScalarArray.class, "base");
        if (baseField == null)
            return false;

        if (baseField.getElementType() != ScalarType.pvDouble)
            return false;

        ScalarArray valueField = structure.getField(ScalarArray.class, "value");
        if (valueField == null)
            return false;

        if (valueField.getElementType() != ScalarType.pvDouble)
            return false;

        ScalarArray unitsField = structure.getField(ScalarArray.class, "units");
        if (unitsField == null)
            return false;

        if (unitsField.getElementType() != ScalarType.pvString)
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
     * Returns whether the specified PVStructure is compatible with NTContinuum.
     * <p>
     * Checks if the specified PVStructure is compatible with this version
     * of NTContinuum through the introspection interface.
     *
     * @param pvStructure the PVStructure to test
     * @return (false,true) if (is not, is) a compatible NTContinuum
     */
    public static boolean isCompatible(PVStructure pvStructure)
    {
        if (pvStructure == null) return false;

        return isCompatible(pvStructure.getStructure());
    }

    /**
     * Returns whether the wrapped structure is valid with respect to this
     * version of NTContinuum.
     * <p>
     * Unlike isCompatible(), isValid() may perform checks on the value
     * data as well as the introspection data.
     *
     * @return (false,true) if wrapped PVStructure (is not, is) a valid NTContinuum
     */
    public boolean isValid()
    {
        return ((getUnits().getLength()-1)*getBase().getLength() ==
            getValue().getLength());
    }

    /**
     * Creates an NTContinuum builder instance.
     *
     * @return builder instance
     */
    public static NTContinuumBuilder createBuilder()
    {
        return new NTContinuumBuilder();
    }

    /**
     * Returns the PVStructure wrapped by this instance.
     *
     * @return the PVStructure wrapped by this instance
     */
    public PVStructure getPVStructure()
    {
        return pvNTContinuum;
    }

    /**
     * Returns the base field.
     *
     * @return the base field
     */
    public PVDoubleArray getBase()
    {
        return pvNTContinuum.getSubField(PVDoubleArray.class, "base");
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
     * Returns the units field.
     *
     * @return the units field
     */
    public PVStringArray getUnits()
    {
        return pvNTContinuum.getSubField(PVStringArray.class, "units");
    }

    /**
     * Returns the descriptor field.
     *
     * @return the descriptor field or null if no such field
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
     *
     * @param pvStructure the PVStructure to be wrapped
     */
    NTContinuum(PVStructure pvStructure)
    {
        pvNTContinuum = pvStructure;
        pvValue = pvNTContinuum.getSubField(PVDoubleArray.class, "value");
    }

    private PVStructure pvNTContinuum;
    private PVDoubleArray pvValue;
}


