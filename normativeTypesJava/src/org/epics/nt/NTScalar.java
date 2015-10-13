/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.nt;

import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.Scalar;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Structure;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVScalar;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.property.PVTimeStamp;
import org.epics.pvdata.property.PVAlarm;
import org.epics.pvdata.property.PVDisplay;
import org.epics.pvdata.property.PVControl;

/**
 * Wrapper class for NTScalar.
 *
 * @author dgh
 */
public class NTScalar
    implements HasAlarm, HasTimeStamp, HasDisplay, HasControl
{
    public static final String URI = "epics:nt/NTScalar:1.0";

    /**
     * Creates an NTScalar wrapping the specified PVStructure if the latter is compatible.
     * <p>
     * Checks the supplied structure is compatible with NTScalar
     * and if so returns a NTScalar which wraps it.
     * This method will return null if the structure is is not compatible
     * or is null.
     *
     * @param pvStructure the PVStructure to be wrapped
     * @return NTScalar instance on success, null otherwise
     */
    public static NTScalar wrap(PVStructure pvStructure)
    {
        if (!isCompatible(pvStructure))
            return null;
        return wrapUnsafe(pvStructure);
    }

    /**
     * Creates an NTScalar wrapping the specified PVStructure, regardless of the latter's compatibility.
     * <p>
     * No checks are made as to whether the specified PVStructure
     * is compatible with NTScalar or is non-null.
     *
     * @param pvStructure the PVStructure to be wrapped
     * @return NTScalar instance
     */
    public static NTScalar wrapUnsafe(PVStructure pvStructure)
    {
        return new NTScalar(pvStructure);
    }

    /**
     * Checks if the specified structure reports to be a compatible NTScalar.
     * <p>
     * Checks whether the specified structure reports compatibility with this
     * version of NTScalar through type ID, including checking version numbers.
     * The return value does not depend on whether the structure is actually
     * compatible in terms of its introspection type.
     *
     * @param structure the Structure to test
     * @return (false,true) if (is not, is) a compatible NTScalar
     */
    public static boolean is_a(Structure structure)
    {
        return NTUtils.is_a(structure.getID(), URI);
    }

    /**
     * Checks if the specified structure reports to be a compatible NTScalar.
     * <p>
     * Checks whether the specified structure reports compatibility with this
     * version of NTScalar through type ID, including checking version numbers.
     * The return value does not depend on whether the structure is actually
     * compatible in terms of its introspection type.
     *
     * @param pvStructure the PVStructure to test
     * @return (false,true) if (is not, is) a compatible NTScalar
     */
    public static boolean is_a(PVStructure pvStructure)
    {
        return is_a(pvStructure.getStructure());
    }

    /**
     * Checks if the specified structure is compatible with NTScalar.
     * <p>
     * Checks whether the specified structure is compatible with this version
     * of NTScalar through introspection interface.
     *
     * @param structure the Structure to test
     * @return (false,true) if (is not, is) a compatible NTScalar
     */
    public static boolean isCompatible(Structure structure)
    {
        if (structure == null) return false;

        Scalar valueField = structure.getField(Scalar.class, "value");
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
     * Checks if the specified structure is compatible with NTScalar.
     * <p>
     * Checks whether the specified structure is compatible with this version
     * of NTScalar through introspection interface.
     *
     * @param pvStructure the PVStructure to test
     * @return (false,true) if (is not, is) a compatible NTScalar
     */
    public static boolean isCompatible(PVStructure pvStructure)
    {
        if (pvStructure == null) return false;

        return isCompatible(pvStructure.getStructure());
    }

    /**
     * Checks if the specified structure is a valid NTScalar.
     * <p>
     * Checks whether the wrapped structure is valid with respect to this
     * version of NTScalar.
     *
     * @return (false,true) if (is not, is) a valid NTScalar
     */
    public boolean isValid()
    {
        return true;
    }

    /**
     * Create an NTScalar builder instance.
     *
     * @return builder instance
     */
    public static NTScalarBuilder createBuilder()
    {
        return new NTScalarBuilder();
    }

    /**
     * Get the PVStructure.
     *
     * @return PVStructure
     */
    public PVStructure getPVStructure()
    {
        return pvNTScalar;
    }

    /**
     * Get the value field.
     *
     * @return the PVScalar for the value field
     */
    public PVScalar getValue()
    {
        return pvValue;
    }

    /* Get the value field of a specified type (e.g. PVDouble).
     *
     * @param <T> the expected type of the value field
     * @param c class object modeling the class T
     * @return the PVScalar or null if the subfield does not exist, or the field is not of <code>c</code> type
     */
    public <T extends PVScalar> T getValue(Class<T> c)
    {
		if (c.isInstance(pvValue))
			return c.cast(pvValue);
		else
			return null;
    }

    /**
     * Get the descriptor field.
     *
     * @return the PVString or null if no descriptor field
     */
    public PVString getDescriptor()
    {
        return pvNTScalar.getSubField(PVString.class, "descriptor");
    }

    /* (non-Javadoc)
	 * @see org.epics.pvdata.nt.HasAlarm#getAlarm()
	 */
    public PVStructure getAlarm()
    {
       return pvNTScalar.getSubField(PVStructure.class, "alarm");
    }

    /* (non-Javadoc)
	 * @see org.epics.pvdata.nt.HasTimeStamp#getTimeStamp()
	 */
    public PVStructure getTimeStamp()
    {
        return pvNTScalar.getSubField(PVStructure.class, "timeStamp");
    }

    /* (non-Javadoc)
	 * @see org.epics.pvdata.nt.Has#getDisplay()
	 */
    public PVStructure getDisplay()
    {
       return pvNTScalar.getSubField(PVStructure.class, "display");
    }

    /* (non-Javadoc)
	 * @see org.epics.pvdata.nt.HasControl#getControl()
	 */
    public PVStructure getControl()
    {
       return pvNTScalar.getSubField(PVStructure.class, "control");
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
    NTScalar(PVStructure pvStructure)
    {
        pvNTScalar = pvStructure;
        pvValue = pvNTScalar.getSubField(PVScalar.class, "value");
    }

    private PVStructure pvNTScalar;
    private PVScalar pvValue;
}


