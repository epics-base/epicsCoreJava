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
import org.epics.pvdata.pv.Structure;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVBooleanArray;
import org.epics.pvdata.pv.PVIntArray;
import org.epics.pvdata.pv.PVLongArray;
import org.epics.pvdata.pv.PVScalarArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStringArray;
import org.epics.pvdata.pv.PVUnionArray;
import org.epics.pvdata.property.PVTimeStamp;
import org.epics.pvdata.property.PVAlarm;

/**
 * Wrapper class for NTScalarMultiChannel.
 *
 * @author dgh
 */
public class NTScalarMultiChannel
    implements HasTimeStamp, HasAlarm
{
    public static final String URI = "epics:nt/NTScalarMultiChannel:1.0";

    /**
     * Creates an NTScalarMultiChannel wrapping the specified PVStructure if the latter is compatible.
     * <p>
     * Checks the supplied PVStructure is compatible with NTScalarMultiChannel
     * and if so returns an NTScalarMultiChannel which wraps it.
     * This method will return null if the structure is is not compatible
     * or is null.
     *
     * @param pvStructure the PVStructure to be wrapped
     * @return NTScalarMultiChannel instance on success, null otherwise
     */
    public static NTScalarMultiChannel wrap(PVStructure pvStructure)
    {
        if (!isCompatible(pvStructure))
            return null;
        return wrapUnsafe(pvStructure);
    }

    /**
     * Creates an NTScalarMultiChannel wrapping the specified PVStructure, regardless of the latter's compatibility.
     * <p>
     * No checks are made as to whether the specified PVStructure
     * is compatible with NTScalarMultiChannel or is non-null.
     *
     * @param pvStructure the PVStructure to be wrapped
     * @return NTScalarMultiChannel instance
     */
    public static NTScalarMultiChannel wrapUnsafe(PVStructure pvStructure)
    {
        return new NTScalarMultiChannel(pvStructure);
    }

    /**
     * Returns whether the specified Structure reports to be a compatible NTScalarMultiChannel.
     * <p>
     * Checks if the specified Structure reports compatibility with this
     * version of NTScalarMultiChannel through its type ID, including checking version numbers.
     * The return value does not depend on whether the structure is actually
     * compatible in terms of its introspection type.
     *
     * @param structure the Structure to test
     * @return (false,true) if (is not, is) a compatible NTScalarMultiChannel
     */
    public static boolean is_a(Structure structure)
    {
        return NTUtils.is_a(structure.getID(), URI);
    }

    /**
     * Returns whether the specified PVStructure reports to be a compatible NTScalarMultiChannel.
     * <p>
     * Checks if the specified PVStructure reports compatibility with this
     * version of NTScalarMultiChannel through its type ID, including checking version numbers.
     * The return value does not depend on whether the structure is actually
     * compatible in terms of its introspection type.
     *
     * @param pvStructure the PVStructure to test
     * @return (false,true) if (is not, is) a compatible NTScalarMultiChannel
     */
    public static boolean is_a(PVStructure pvStructure)
    {
        return is_a(pvStructure.getStructure());
    }

    /**
     * Returns whether the specified Structure is compatible with NTScalarMultiChannel.
     * <p>
     * Checks if the specified Structure is compatible with this version
     * of NTScalarMultiChannel through the introspection interface.
     *
     * @param structure the Structure to test
     * @return (false,true) if (is not, is) a compatible NTScalarMultiChannel
     */
    public static boolean isCompatible(Structure structure)
    {
        if (structure == null) return false;

        ScalarArray valueField = structure.getField(ScalarArray.class,
            "value");

        if (valueField == null)
            return false;

        ScalarArray channelNameField = structure.getField(ScalarArray.class,
            "channelName");

        if (channelNameField == null)
            return false;

        if (channelNameField.getElementType() != ScalarType.pvString)
            return false;

        NTField ntField = NTField.get();

        Field field = structure.getField("severity");
        if (field != null)
        {
            ScalarArray severityField = structure.getField(ScalarArray.class, "severity");
            if (severityField == null || severityField.getElementType() != ScalarType.pvInt)
                return false;
        }

        field = structure.getField("status");
        if (field != null)
        {
            ScalarArray statusField = structure.getField(ScalarArray.class, "status");
            if (statusField == null || statusField.getElementType() != ScalarType.pvInt)
                return false;
        }

        field = structure.getField("message");
        if (field != null)
        {
            ScalarArray messageField = structure.getField(ScalarArray.class, "message");
            if (messageField == null || messageField.getElementType() != ScalarType.pvString)
                return false;
        }

        field = structure.getField("secondsPastEpoch");
        if (field != null)
        {
            ScalarArray secondsPastEpochField = structure.getField(ScalarArray.class, "secondsPastEpoch");
            if (secondsPastEpochField == null || secondsPastEpochField.getElementType() != ScalarType.pvLong)
                return false;
        }

        field = structure.getField("nanoseconds");
        if (field != null)
        {
            ScalarArray nanosecondsField = structure.getField(ScalarArray.class, "nanoseconds");
            if (nanosecondsField == null || nanosecondsField.getElementType() != ScalarType.pvInt)
                return false;
        }

        field = structure.getField("userTag");
        if (field != null)
        {
            ScalarArray userTagField = structure.getField(ScalarArray.class, "userTag");
            if (userTagField == null || userTagField.getElementType() != ScalarType.pvInt)
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
     * Returns whether the specified PVStructure is compatible with NTScalarMultiChannel.
     * <p>
     * Checks if the specified PVStructure is compatible with this version
     * of NTScalarMultiChannel through the introspection interface.
     *
     * @param pvStructure the PVStructure to test
     * @return (false,true) if (is not, is) a compatible NTScalarMultiChannel
     */
    public static boolean isCompatible(PVStructure pvStructure)
    {
        if (pvStructure == null) return false;

        return isCompatible(pvStructure.getStructure());
    }

    /**
     * Returns whether the wrapped PVStructure is a valid NTScalarMultiChannel.
     * <p>
     * Unlike isCompatible(), isValid() may perform checks on the value
     * data as well as the introspection data.
     *
     * @return (false,true) if wrapped PVStructure (is not, is) a valid NTScalarMultiChannel
     */
    public boolean isValid()
    {
        int valueLength = getValue().getLength();
        if (getChannelName().getLength() != valueLength) return false;

        PVScalarArray[] arrayFields = {
            getSeverity(), getStatus(), getMessage(), 
            getSecondsPastEpoch(), getNanoseconds(), getUserTag()
        };

        for (PVScalarArray arrayField : arrayFields)
        {
            if (arrayField != null && arrayField.getLength() != valueLength)
                return false;
        }
        return true; 
    }

    /**
     * Creates an NTScalarMultiChannelBuilder instance.
     *
     * @return builder instance
     */
    public static NTScalarMultiChannelBuilder createBuilder()
    {
        return new NTScalarMultiChannelBuilder();
    }

    /**
     * Returns the PVStructure wrapped by this instance.
     *
     * @return the PVStructure wrapped by this instance
     */
    public PVStructure getPVStructure()
    {
        return pvNTScalarMultiChannel;
    }

    /**
     * Returns the field with the value of each channel.
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
     * @return the value field or null if the field is not of <code>c</code> type
     */
    public <T extends PVScalarArray> T getValue(Class<T> c)
    {
		if (c.isInstance(pvValue))
			return c.cast(pvValue);
		else
			return null;
    }

    /**
     * Returns the field with the value of each channel.
     * 
     * @return PVStringArray
     */
    public PVStringArray getChannelName()
    {
       return pvChannelName;
    }

    /**
     * Returns the field with the connection state of each channel.
     *
     * @return the isConnected field or null if no such field
     */
    public PVIntArray getSeverity()
    {
        return pvSeverity;
    }

    /**
     * Returns the field with the status of each channel.
     *
     * @return the status field or null if no such field
     */
    public PVIntArray getStatus()
    {
        return pvStatus;
    }

    /**
     * Returns the field with the message of each channel.
     *
     * @return message field or null if no such field
     */
    public PVStringArray getMessage()
    {
        return pvMessage;
    }

    /**
     * Returns the field with the secondsPastEpoch of each channel.
     *
     * @return the secondsPastEpoch  field or null if no such field
     */
    public PVLongArray getSecondsPastEpoch()
    {
        return pvSecondsPastEpoch;
    }

    /**
     * Returns the field with the nanoseconds of each channel.
     *
     * @return nanoseconds field or null if no such field
     */
    public PVIntArray getNanoseconds()
    {
        return pvNanoseconds;
    }

    /**
     * Returns the field with the userTag of each channel.
     *
     * @return the userTag field or null if no such field
     */
    public PVIntArray getUserTag()
    {
        return pvUserTag;
    }

    /**
     * Returns the field with the connection state of each channel.
     *
     * @return the isConnected field or null if no such field
     */
    public PVBooleanArray getIsConnected()
    {
        return pvIsConnected;
    }

    /**
     * Returns the descriptor field.
     *
     * @return the descriptor field or null if no such field
     */
    public PVString getDescriptor()
    {
        return pvDescriptor;
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.nt.HasAlarm#getAlarm()
     */
    public PVStructure getAlarm()
    {
        return pvAlarm;
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.nt.HasTimeStamp#getTimeStamp()
     */
    public PVStructure getTimeStamp()
    {
        return pvTimeStamp;
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
     * @param pvStructure the PVStructure to be wrapped
     */
    NTScalarMultiChannel(PVStructure pvStructure)
    {
        pvNTScalarMultiChannel = pvStructure;
        pvValue = pvStructure.getSubField(PVScalarArray.class, "value");
        pvChannelName = pvStructure.getSubField(PVStringArray.class, "channelName");
        pvAlarm = pvStructure.getSubField(PVStructure.class, "alarm");
        pvTimeStamp = pvStructure.getSubField(PVStructure.class, "timeStamp");
        pvChannelName = pvStructure.getSubField(PVStringArray.class, "channelName");
        pvDescriptor = pvStructure.getSubField(PVString.class, "descriptor");
        pvSeverity = pvStructure.getSubField(PVIntArray.class, "severity");
        pvStatus = pvStructure.getSubField(PVIntArray.class, "status");
        pvMessage = pvStructure.getSubField(PVStringArray.class, "message");
        pvSecondsPastEpoch = pvStructure.getSubField(PVLongArray.class, "secondsPastEpoch");
        pvNanoseconds = pvStructure.getSubField(PVIntArray.class, "nanoseconds");
        pvUserTag = pvStructure.getSubField(PVIntArray.class, "userTag");
        pvIsConnected = pvStructure.getSubField(PVBooleanArray.class, "isConnected");
    }

    private PVStructure pvNTScalarMultiChannel;
    private PVScalarArray pvValue;
    private PVStringArray pvChannelName;
    private PVBooleanArray pvIsConnected;
    private PVIntArray pvSeverity;
    private PVIntArray pvStatus;
    private PVStringArray pvMessage;
    private PVLongArray pvSecondsPastEpoch;
    private PVIntArray pvNanoseconds;
    private PVIntArray pvUserTag;
    private PVString pvDescriptor;
    private PVStructure pvAlarm;
    private PVStructure pvTimeStamp;
}

