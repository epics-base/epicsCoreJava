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
 * Wrapper class for NTNameValue.
 *
 * @author dgh
 */
public class NTNameValue
    implements HasAlarm, HasTimeStamp
{
    public static final String URI = "epics:nt/NTNameValue:1.0";

    /**
     * Creates an NTNameValue wrapping the specified PVStructure if the latter is compatible.
     * <p>
     * Checks the supplied structure is compatible with NTNameValue
     * and if so returns a NTNameValue which wraps it.
     * This method will return null if the structure is is not compatible
     * or is null.
     *
     * @param pvStructure the PVStructure to be wrapped.
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
     * <p>
     * No checks are made as to whether the specified PVStructure
     * is compatible with NTNameValue or is non-null.
     *
     * @param pvStructure the PVStructure to be wrapped.
     * @return NTNameValue instance.
     */
    public static NTNameValue wrapUnsafe(PVStructure pvStructure)
    {
        return new NTNameValue(pvStructure);
    }

    /**
     * Checks if the specified structure reports to be a compatible NTNameValue.
     * <p>
     * Checks whether the specified structure reports compatibility with this
     * version of NTNameValue through type ID, including checking version numbers.
     * The return value does not depend on whether the structure is actually
     * compatible in terms of its introspection type.
     *
     * @param structure the Structure to test.
     * @return (false,true) if (is not, is) a compatible NTNameValue.
     */
    public static boolean is_a(Structure structure)
    {
        return NTUtils.is_a(structure.getID(), URI);
    }

    /**
     * Checks if the specified structure reports to be a compatible NTNameValue.
     * <p>
     * Checks whether the specified structure reports compatibility with this
     * version of NTNameValue through type ID, including checking version numbers.
     * The return value does not depend on whether the structure is actually
     * compatible in terms of its introspection type
     * @param pvStructure The PVStructure to test.
     * @return (false,true) if (is not, is) a compatible NTNameValue.
     */
    public static boolean is_a(PVStructure pvStructure)
    {
        return is_a(pvStructure.getStructure());
    }

    /**
     * Checks if the specified structure is compatible with NTNameValue.
     * <p>
     * Checks whether the specified structure is compatible with this version
     * of NTNameValue through introspection interface.
     * @param structure the Structure to test.
     * @return (false,true) if (is not, is) a compatible NTNameValue.
     */
    public static boolean isCompatible(Structure structure)
    {
        if (structure == null) return false;

        ScalarArray valueField = structure.getField(ScalarArray.class, "value");
        if (valueField == null)
            return false;

        ScalarArray channelNameField = structure.getField(ScalarArray.class,
            "name");

        if (channelNameField == null)
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

        return true;
    }

    /**
     * Checks if the specified structure is compatible with NTNameValue.
     * <p>
     * Checks whether the specified structure is compatible with this version
     * of NTNameValue through introspection interface.
     *
     * @param pvStructure the PVStructure to test
     * @return (false,true) if (is not, is) a compatible NTNameValue
     */
    public static boolean isCompatible(PVStructure pvStructure)
    {
        if (pvStructure == null) return false;

        return isCompatible(pvStructure.getStructure());
    }

    /**
     * Checks if the specified structure is a valid NTNameValue.
     * <p>
     * Checks whether the wrapped structure is valid with respect to this
     * version of NTNameValue.
     *
     * @return (false,true) if (is not, is) a valid NTNameValue
     */
    public boolean isValid()
    {
        return (getValue().getLength() == getName().getLength());
    }

    /**
     * Create a NTNameValue builder instance.
     *
     * @return builder instance
     */
    public static NTNameValueBuilder createBuilder()
    {
        return new NTNameValueBuilder();
    }

    /**
     * Get the PVStructure.
     *
     * @return PVStructure
     */
    public PVStructure getPVStructure()
    {
        return pvNTNameValue;
    }

    /**
     * Get the value field.
     *
     * @return the PVScalarArray for the value field
     */
    public PVScalarArray getValue()
    {
        return pvValue;
    }

    /* Get the value field of a specified type (e.g. PVDoubleArray).
     *
     * @param <T> the expected type of the value field
     * @param c class object modeling the class T
     * @return the PVScalarArray or null if the subfield does not exist, or the field is not of <code>c</code> type
     */
    public <T extends PVScalarArray> T getValue(Class<T> c)
    {
		if (c.isInstance(pvValue))
			return c.cast(pvValue);
		else
			return null;
    }

    /**
     * Get the name array field.
     * 
     * @return the PVStringArray for the name field
     */
    PVStringArray getName()
    {
        return pvNTNameValue.getSubField(PVStringArray.class, "name");
    }

    /**
     * Get the descriptor field.
     * 
     * @return the PVString or null if no descriptor field
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
     * Constructor.
     * 
     * @param pvStructure the PVStructure to be wrapped
     */
    NTNameValue(PVStructure pvStructure)
    {
        pvNTNameValue = pvStructure;
        pvValue = pvNTNameValue.getSubField(PVScalarArray.class, "value");
    }

    private PVStructure pvNTNameValue;
    private PVScalarArray pvValue;
}


