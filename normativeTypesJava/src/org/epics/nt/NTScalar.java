/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.nt;

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
 * Wrapper class for NTScalar
 *
 * @author dgh
 */
public class NTScalar
    implements HasAlarm, HasTimeStamp, HasDisplay, HasControl
{
    public static final String URI = "epics:nt/NTScalar:1.0";

    /**
     * Creates an NTScalar wrapping the specified PVStructure if the latter is compatible.
     *
     * Checks the supplied structure is compatible with NTScalar
     * and if so returns a NTScalar which wraps it.
     * This method will return null if the structure is is not compatible
     * or is null.
     * @param pvStructure The PVStructure to be wrapped.
     * @return NTScalar instance on success, null otherwise.
     */
    public static NTScalar wrap(PVStructure pvStructure)
    {
        if (!isCompatible(pvStructure))
            return null;
        return wrapUnsafe(pvStructure);
    }

    /**
     * Creates an NTScalar wrapping the specified PVStructure, regardless of the latter's compatibility.
     *
     * No checks are made as to whether the specified PVStructure
     * is compatible with NTScalar or is non-null.
     * @param pvStructure The PVStructure to be wrapped.
     * @return NTScalar instance.
     */
    public static NTScalar wrapUnsafe(PVStructure pvStructure)
    {
        return new NTScalar(pvStructure);
    }

    /**
     * Checks if the specified structure reports to be a compatible NTScalar.
     *
     * Checks whether the specified structure reports compatibility with this
     * version of NTScalar through type ID, including checking version numbers.
     * The return value does not depend on whether the structure is actually
     * compatible in terms of its introspection type
     * @param structure The pvStructure to test.
     * @return (false,true) if (is not, is) a compatible NTScalar.
     */
    public static boolean is_a(Structure structure)
    {
        return NTUtils.is_a(structure.getID(), URI);
    }

    /**
     * Checks if the specified structure is compatible with NTScalar.
     *
     * Checks whether the specified structure is compatible with this version
     * of NTScalar through introspection interface.
     * @param pvStructure The PVStructure to test.
     * @return (false,true) if (is not, is) a compatible NTScalar.
     */
    public static boolean isCompatible(PVStructure pvStructure)
    {
        if (pvStructure == null) return false;

        PVScalar pvValue = pvStructure.getSubField(PVScalar.class, "value");
        if (pvValue == null) return false;

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

        pvField = pvStructure.getSubField("display");
        if(pvField != null && !ntField.isDisplay(pvField.getField()))
            return false;

        pvField = pvStructure.getSubField("control");
        if (pvField != null && !ntField.isControl(pvField.getField()))
            return false;

        return true;
    }

    /**
     * Create an NTScalar builder instance.
     * @return builder instance.
     */
    public static NTScalarBuilder createBuilder()
    {
        return new NTScalarBuilder();
    }

    /**
     * Get the pvStructure.
     * @return PVStructure.
     */
    public PVStructure getPVStructure()
    {
        return pvNTScalar;
    }

    /**
     * Get the value field.
     * @return The PVScalar for the values.
     */
    public PVScalar getValue()
    {
        return pvValue;
    }

    /* Get the value field of a specified type (e.g. PVDouble).
     * @param c expected class of a requested field.
     * @return The PVScalar or null if the subfield does not exist, or the field is not of <code>c</code> type.
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
     * @return The pvString or null if no function field.
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
     * Constructor
     * @param pvStructure The PVStructure to be wrapped.
     */
    NTScalar(PVStructure pvStructure)
    {
        pvNTScalar = pvStructure;
        pvValue = pvNTScalar.getSubField(PVScalar.class, "value");
    }

    private PVStructure pvNTScalar;
    private PVScalar pvValue;
}


