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
import org.epics.pvdata.pv.PVUnion;
import org.epics.pvdata.property.PVTimeStamp;
import org.epics.pvdata.property.PVAlarm;
import org.epics.pvdata.property.PVDisplay;
import org.epics.pvdata.property.PVControl;

/**
 * Wrapper class for NTAttribute
 *
 * @author dgh
 */
public class NTAttribute
    implements HasAlarm, HasTimeStamp
{
    public static final String URI = "epics:nt/NTAttribute:1.0";

    /**
     * Creates an NTAttribute wrapping the specified PVStructure if the latter is compatible.
     *
     * Checks the supplied structure is compatible with NTAttribute
     * and if so returns a NTAttribute which wraps it.
     * This method will return null if the structure is is not compatible
     * or is null.
     * @param pvStructure The PVStructure to be wrapped.
     * @return NTAttribute instance on success, null otherwise.
     */
    public static NTAttribute wrap(PVStructure pvStructure)
    {
        if (!isCompatible(pvStructure))
            return null;
        return wrapUnsafe(pvStructure);
    }

    /**
     * Creates an NTAttribute wrapping the specified PVStructure, regardless of the latter's compatibility.
     *
     * No checks are made as to whether the specified PVStructure
     * is compatible with NTAttribute or is non-null.
     * @param pvStructure The PVStructure to be wrapped.
     * @return NTAttribute instance.
     */
    public static NTAttribute wrapUnsafe(PVStructure pvStructure)
    {
        return new NTAttribute(pvStructure);
    }

    /**
     * Checks if the specified structure reports to be a compatible NTAttribute.
     *
     * Checks whether the specified structure reports compatibility with this
     * version of NTAttribute through type ID, including checking version numbers.
     * The return value does not depend on whether the structure is actually
     * compatible in terms of its introspection type
     * @param structure The pvStructure to test.
     * @return (false,true) if (is not, is) a compatible NTAttribute.
     */
    public static boolean is_a(Structure structure)
    {
        return NTUtils.is_a(structure.getID(), URI);
    }

    /**
     * Checks if the specified structure is compatible with NTAttribute.
     *
     * Checks whether the specified structure is compatible with this version
     * of NTAttribute through introspection interface.
     * @param pvStructure The PVStructure to test.
     * @return (false,true) if (is not, is) a compatible NTAttribute.
     */
    public static boolean isCompatible(PVStructure pvStructure)
    {
        if (pvStructure == null) return false;

        PVString pvName = pvStructure.getSubField(PVString.class, "name");
        if (pvName == null) return false;

        PVUnion pvValue = pvStructure.getSubField(PVUnion.class, "value");
        if (pvValue == null) return false;

        if (!pvValue.getUnion().isVariant()) return false;

        PVField pvField = pvStructure.getSubField("tags");
        if (pvField != null && pvStructure.getSubField(PVStringArray.class, "tags") == null) return false;

        NTField ntField = NTField.get();

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
     * Create an NTAttribute builder instance.
     * @return builder instance.
     */
    public static NTAttributeBuilder createBuilder()
    {
        return new NTAttributeBuilder();
    }

    /**
     * Get the pvStructure.
     * @return PVStructure.
     */
    public PVStructure getPVStructure()
    {
        return pvNTAttribute;
    }

    /**
     * Get the name field.
     * @return The PVString for the name.
     */
    public PVString getName()
    {
        return pvNTAttribute.getSubField(PVString.class, "name");
    }

    /**
     * Get the value field.
     * @return The PVUnion for the value.
     */
    public PVUnion getValue()
    {
        return pvValue;
    }

    /**
     * Get the tags field.
     * @return The PVStringArray for the tags, or null if not present.
     */
    public PVStringArray getTags()
    {
        return pvNTAttribute.getSubField(PVStringArray.class, "tags");
    }

    /**
     * Get the descriptor field.
     * @return The PVString or null if no function field.
     */
    public PVString getDescriptor()
    {
        return pvNTAttribute.getSubField(PVString.class, "descriptor");
    }

    /* (non-Javadoc)
	 * @see org.epics.pvdata.nt.HasAlarm#getAlarm()
	 */
    public PVStructure getAlarm()
    {
       return pvNTAttribute.getSubField(PVStructure.class, "alarm");
    }

    /* (non-Javadoc)
	 * @see org.epics.pvdata.nt.HasTimeStamp#getTimeStamp()
	 */
    public PVStructure getTimeStamp()
    {
        return pvNTAttribute.getSubField(PVStructure.class, "timeStamp");
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
    NTAttribute(PVStructure pvStructure)
    {
        pvNTAttribute = pvStructure;
        pvValue = pvNTAttribute.getSubField(PVUnion.class, "value");
    }

    protected PVStructure pvNTAttribute;
    private PVUnion pvValue;
}


