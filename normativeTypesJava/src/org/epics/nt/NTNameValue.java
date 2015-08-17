/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.nt;

import org.epics.pvdata.pv.Structure;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVScalarArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStringArray;
import org.epics.pvdata.property.PVTimeStamp;
import org.epics.pvdata.property.PVAlarm;
import org.epics.pvdata.property.PVDisplay;
import org.epics.pvdata.property.PVControl;

/**
 * Wrapper class for NTNameValue
 *
 * @author dgh
 */
public class NTNameValue
    implements HasAlarm, HasTimeStamp
{
    public static final String URI = "epics:nt/NTNameValue:1.0";

    /**
     * Creates an NTNameValue wrapping the specified PVStructure if the latter is compatible.
     *
     * Checks the supplied structure is compatible with NTNameValue
     * and if so returns a NTNameValue which wraps it.
     * This method will return null if the structure is is not compatible
     * or is null.
     * @param pvStructure The PVStructure to be wrapped.
     * @return NTNameValue instance on success, null otherwise.
     */
    public static NTNameValue wrap(PVStructure pvStructure)
    {
        if (!isCompatible(pvStructure))
            return null;
        return wrapUnsafe(pvStructure);
    }

    /**
     * Creates an NTNameValue wrapping the specified PVStructure, regardless of the latter's compatibility.
     *
     * No checks are made as to whether the specified PVStructure
     * is compatible with NTNameValue or is non-null.
     * @param pvStructure The PVStructure to be wrapped.
     * @return NTNameValue instance.
     */
    public static NTNameValue wrapUnsafe(PVStructure pvStructure)
    {
        return new NTNameValue(pvStructure);
    }

    /**
     * Checks if the specified structure reports to be a compatible NTNameValue.
     *
     * Checks whether the specified structure reports compatibility with this
     * version of NTNameValue through type ID, including checking version numbers.
     * The return value does not depend on whether the structure is actually
     * compatible in terms of its introspection type
     * @param structure The structure to test.
     * @return (false,true) if (is not, is) a compatible NTNameValue.
     */
    public static boolean is_a(Structure structure)
    {
        return NTUtils.is_a(structure.getID(), URI);
    }

    /**
     * Checks if the specified structure is compatible with NTNameValue.
     *
     * Checks whether the specified structure is compatible with this version
     * of NTNameValue through introspection interface.
     * @param pvStructure The PVStructure to test.
     * @return (false,true) if (is not, is) a compatible NTNameValue.
     */
    public static boolean isCompatible(PVStructure pvStructure)
    {
        if (pvStructure == null) return false;

        PVScalarArray pvValue = pvStructure.getSubField(PVScalarArray.class, "value");
        if (pvValue == null) return false;

        PVStringArray pvName = pvStructure.getSubField(PVStringArray.class, "name");
        if (pvName == null) return false;

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
     * Create a NTNameValue builder instance.
     * @return builder instance.
     */
    public static NTNameValueBuilder createBuilder()
    {
        return new NTNameValueBuilder();
    }

    /**
     * Get the pvStructure.
     * @return PVStructure.
     */
    public PVStructure getPVStructure()
    {
        return pvNTNameValue;
    }

    /**
     * Get the value field.
     * @return The PVField for the values.
     */
    public PVField getValue()
    {
        return pvValue;
    }

    /* Get the value field of a specified type (e.g. PVDoubleArray).
     * @param c expected class of a requested field.
     * @return The PVField or null if the subfield does not exist, or the field is not of <code>c</code> type.
     */
    public <T extends PVField> T getValue(Class<T> c)
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
        return pvNTNameValue.getSubField(PVString.class, "descriptor");
    }

    /* (non-Javadoc)
	 * @see org.epics.pvdata.nt.HasAlarm#getAlarm()
	 */
    public PVStructure getAlarm()
    {
       return pvNTNameValue.getSubField(PVStructure.class, "alarm");
    }

    /* (non-Javadoc)
	 * @see org.epics.pvdata.nt.HasTimeStamp#getTimeStamp()
	 */
    public PVStructure getTimeStamp()
    {
        return pvNTNameValue.getSubField(PVStructure.class, "timeStamp");
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
    NTNameValue(PVStructure pvStructure)
    {
        pvNTNameValue = pvStructure;
        pvValue = pvNTNameValue.getSubField("value");
    }

    private PVStructure pvNTNameValue;
    private PVField pvValue;
}


