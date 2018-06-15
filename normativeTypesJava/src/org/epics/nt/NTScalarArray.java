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
import org.epics.pvdata.pv.PVScalarArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.property.PVTimeStamp;
import org.epics.pvdata.property.PVAlarm;
import org.epics.pvdata.property.PVDisplay;
import org.epics.pvdata.property.PVControl;

/**
 * Wrapper class for NTScalarArray.
 *
 * @author dgh
 */
public class NTScalarArray
    implements HasAlarm, HasTimeStamp, HasDisplay, HasControl
{
    public static final String URI = "epics:nt/NTScalarArray:1.0";

    /**
     * Creates an NTScalarArray wrapping the specified PVStructure if the latter is compatible.
     * <p>
     * Checks the supplied PVStructure is compatible with NTScalarArray
     * and if so returns an NTScalarArray which wraps it.
     * This method will return null if the structure is is not compatible
     * or is null.
     * 
     * @param pvStructure the PVStructure to be wrapped
     * @return NTScalarArray instance on success, null otherwise
     */
    public static NTScalarArray wrap(PVStructure pvStructure)
    {
        if (!isCompatible(pvStructure))
            return null;
        return wrapUnsafe(pvStructure);
    }

    /**
     * Creates an NTScalarArray wrapping the specified PVStructure, regardless of the latter's compatibility.
     * <p>
     * No checks are made as to whether the specified PVStructure
     * is compatible with NTScalarArray or is non-null.
     * 
     * @param pvStructure the PVStructure to be wrapped
     * @return NTScalarArray instance
     */
    public static NTScalarArray wrapUnsafe(PVStructure pvStructure)
    {
        return new NTScalarArray(pvStructure);
    }

    /**
     * Returns whether the specified Structure reports to be a compatible NTScalarArray.
     * <p>
     * Checks if the specified Structure reports compatibility with this
     * version of NTScalarArray through its type ID, including checking version numbers.
     * The return value does not depend on whether the structure is actually
     * compatible in terms of its introspection type.
     * 
     * @param structure the Structure to test
     * @return (false,true) if (is not, is) a compatible NTScalarArray
     */
    public static boolean is_a(Structure structure)
    {
        return NTUtils.is_a(structure.getID(), URI);
    }

    /**
     * Returns whether the specified PVStructure reports to be a compatible NTScalarArray.
     * <p>
     * Checks if the specified PVStructure reports compatibility with this
     * version of NTScalarArray through its type ID, including checking version numbers.
     * The return value does not depend on whether the structure is actually
     * compatible in terms of its introspection type.
     * 
     * @param pvStructure the PVStructure to test
     * @return (false,true) if (is not, is) a compatible NTScalarArray
     */
    public static boolean is_a(PVStructure pvStructure)
    {
        return is_a(pvStructure.getStructure());
    }

    /**
     * Returns whether the specified Structure is compatible with NTScalarArray.
     * <p>
     * Checks if the specified Structure is compatible with this version
     * of NTScalarArray through the introspection interface.
     * 
     * @param structure the Structure to test.
     * @return (false,true) if (is not, is) a compatible NTScalarArray
     */
    public static boolean isCompatible(Structure structure)
    {
        if (structure == null) return false;

        ScalarArray valueField = structure.getField(ScalarArray.class, "value");
        if (valueField == null)
            return false;

        Field field = structure.getField("descriptor");
        if (field != null)
        {
            Scalar descriptorField = structure.getField(Scalar.class, "descriptor");
            if (descriptorField == null || descriptorField.getScalarType() != ScalarType.pvString)
                return false;
        }

        NTField ntField = NTField.get();

        field = structure.getField("alarm");
        if (field != null && !ntField.isAlarm(field))
            return false;

        field = structure.getField("timeStamp");
        if (field != null && !ntField.isTimeStamp(field))
            return false;

        field = structure.getField("display");
        if (field != null && !ntField.isDisplay(field))
            return false;

        field = structure.getField("control");
        if (field != null && !ntField.isControl(field))
            return false;

        return true;
    }

    /**
     * Returns whether the specified PVStructure is compatible with NTScalarArray.
     * <p>
     * Checks if the specified PVStructure is compatible with this version
     * of NTScalarArray through the introspection interface.
     * 
     * @param pvStructure the PVStructure to test
     * @return (false,true) if (is not, is) a compatible NTScalarArray
     */
    public static boolean isCompatible(PVStructure pvStructure)
    {
        if (pvStructure == null) return false;

        return isCompatible(pvStructure.getStructure());
    }

    /**
     * Returns whether the wrapped PVStructure is a valid NTScalarArray.
     * <p>
     * Unlike isCompatible(), isValid() may perform checks on the value
     * data as well as the introspection data.
     *
     * @return (false,true) if wrapped PVStructure (is not, is) a valid NTScalarArray.
     */
    public boolean isValid()
    {
        return true;
    }

    /**
     * Creates an NTScalarArray builder instance.
     * 
     * @return builder instance
     */
    public static NTScalarArrayBuilder createBuilder()
    {
        return new NTScalarArrayBuilder();
    }

    /**
     * Returns the PVStructure wrapped by this instance.
     *
     * @return the PVStructure wrapped by this instance
     */
    public PVStructure getPVStructure()
    {
        return pvNTScalarArray;
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
     * Returns the value field of a specified type (e.g. PVDoubleArray).
     * 
     * @param <T> the expected type of the value field
     * @param c class object modeling the class T
     * @return the value field or null the field is not of <code>c</code> type
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
        return pvNTScalarArray.getSubField(PVString.class, "descriptor");
    }

    /* (non-Javadoc)
	 * @see org.epics.pvdata.nt.HasAlarm#getAlarm()
	 */
    public PVStructure getAlarm()
    {
       return pvNTScalarArray.getSubField(PVStructure.class, "alarm");
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.nt.HasTimeStamp#getTimeStamp()
     */
    public PVStructure getTimeStamp()
    {
        return pvNTScalarArray.getSubField(PVStructure.class, "timeStamp");
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.nt.Has#getDisplay()
     */
    public PVStructure getDisplay()
    {
       return pvNTScalarArray.getSubField(PVStructure.class, "display");
    }

    /* (non-Javadoc)
      * @see org.epics.pvdata.nt.HasControl#getControl()
      */
    public PVStructure getControl()
    {
       return pvNTScalarArray.getSubField(PVStructure.class, "control");
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
	 * @see org.epics.pvdata.nt.HasControl#attachControl(org.epics.pvdata.property.PVControl)
	 */
    public boolean attachControl(PVControl pvControl)
    {
        PVStructure ctrl = getControl();
        if (ctrl != null)
            return pvControl.attach(ctrl);
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
    NTScalarArray(PVStructure pvStructure)
    {
        pvNTScalarArray = pvStructure;
        pvValue = pvNTScalarArray.getSubField(PVScalarArray.class, "value");
    }

    private PVStructure pvNTScalarArray;
    private PVScalarArray pvValue;
}

