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
import org.epics.pvdata.pv.Union;
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
 * Wrapper class for NTAttribute.
 *
 * @author dgh
 */
public class NTAttribute
    implements HasAlarm, HasTimeStamp
{
    public static final String URI = "epics:nt/NTAttribute:1.0";

    /**
     * Creates an NTAttribute wrapping the specified PVStructure if the latter is compatible.
     * <p>
     * Checks the supplied PVStructure is compatible with NTAttribute
     * and if so returns an NTAttribute which wraps it.
     * This method will return null if the structure is is not compatible
     * or is null.
     *
     * @param pvStructure the PVStructure to be wrapped
     * @return NTAttribute instance on success, null otherwise
     */
    public static NTAttribute wrap(PVStructure pvStructure)
    {
        if (!isCompatible(pvStructure))
            return null;
        return wrapUnsafe(pvStructure);
    }

    /**
     * Creates an NTAttribute wrapping the specified PVStructure, regardless of the latter's compatibility.
     * <p> 
     * No checks are made as to whether the specified PVStructure
     * is compatible with NTAttribute or is non-null.
     *
     * @param pvStructure the PVStructure to be wrapped
     * @return NTAttribute instance
     */
    public static NTAttribute wrapUnsafe(PVStructure pvStructure)
    {
        return new NTAttribute(pvStructure);
    }

    /**
     * Returns whether the specified Structure reports to be a compatible NTAttribute.
     * <p>
     * Checks if the specified Structure reports compatibility with this
     * version of NTAttribute through its type ID, including checking version numbers.
     * The return value does not depend on whether the structure is actually
     * compatible in terms of its introspection type.
     *
     * @param structure the pvStructure to test
     * @return (false,true) if (is not, is) a compatible NTAttribute
     */
    public static boolean is_a(Structure structure)
    {
        return NTUtils.is_a(structure.getID(), URI);
    }

    /**
     * Returns whether the specified PVStructure reports to be a compatible NTAttribute.
     * <p>
     * Checks if the specified PVStructure reports compatibility with this
     * version of NTAttribute through its type ID, including checking version numbers.
     * The return value does not depend on whether the structure is actually
     * compatible in terms of its introspection type.
     *
     * @param pvStructure the PVStructure to test
     * @return (false,true) if (is not, is) a compatible NTAttribute
     */
    public static boolean is_a(PVStructure pvStructure)
    {
        return is_a(pvStructure.getStructure());
    }

    /**
     * Returns whether the specified Structure is compatible with NTAttribute.
     * <p>
     * Checks if the specified Structure is compatible with this version
     * of NTAttribute through the introspection interface.
     *
     * @param structure the Structure to test
     * @return (false,true) if (is not, is) a compatible NTAttribute
     */
    public static boolean isCompatible(Structure structure)
    {
        if (structure == null) return false;

        Scalar nameField = structure.getField(Scalar.class, "name");
        if (nameField == null)
            return false;

        if (nameField.getScalarType() != ScalarType.pvString)
            return false;

        Union valueField = structure.getField(Union.class, "value");
        if (valueField == null)
            return false;

       if (!valueField.isVariant())
            return false;

        Field field = structure.getField("tags");
        if (field != null)
        {
            ScalarArray tagsField = structure.getField(ScalarArray.class, "tags");
            if (tagsField == null || tagsField.getElementType() != ScalarType.pvString)
                return false;
        }

        NTField ntField = NTField.get();

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
     * Returns whether the specified PVStructure is compatible with NTAttribute.
     * <p>
     * Checks if the specified PVStructure is compatible with this version
     * of NTAttribute through the introspection interface.
     *
     * @param pvStructure the PVStructure to test
     * @return (false,true) if (is not, is) a compatible NTAttribute
     */
    public static boolean isCompatible(PVStructure pvStructure)
    {
        if (pvStructure == null) return false;

        return isCompatible(pvStructure.getStructure());
    }

    /**
     * Returns whether the wrapped PVStructure is valid with respect to this
     * version of NTAttribute.
     * <p>
     * Unlike isCompatible(), isValid() may perform checks on the value
     * data as well as the introspection data.
     *
     * @return (false,true) if wrapped PVStructure (is not, is) a valid NTAttribute
     */
    public boolean isValid()
    {
        return true;
    }

    /**
     * Creates an NTAttribute builder instance.
     *
     * @return builder instance.
     */
    public static NTAttributeBuilder createBuilder()
    {
        return new NTAttributeBuilder();
    }

    /**
     * Returns the PVStructure wrapped by this instance.
     *
     * @return the PVStructure wrapped by this instance
     */
    public PVStructure getPVStructure()
    {
        return pvNTAttribute;
    }

    /**
     * Returns the name field.
     *
     * @return the name field
     */
    public PVString getName()
    {
        return pvNTAttribute.getSubField(PVString.class, "name");
    }

    /**
     * Returns the value field.
     *
     * @return the value field
     */
    public PVUnion getValue()
    {
        return pvValue;
    }

    /**
     * Returns the tags field.
     *
     * @return the tags field or null if no such field
     */
    public PVStringArray getTags()
    {
        return pvNTAttribute.getSubField(PVStringArray.class, "tags");
    }

    /**
     * Returns the descriptor field.
     *
     * @return the descriptor field or null if no such field
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
     *
     * @param pvStructure the PVStructure to be wrapped
     */
    NTAttribute(PVStructure pvStructure)
    {
        pvNTAttribute = pvStructure;
        pvValue = pvNTAttribute.getSubField(PVUnion.class, "value");
    }

    protected PVStructure pvNTAttribute;
    private PVUnion pvValue;
}


