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
import org.epics.pvdata.pv.UnionArray;
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
 * Wrapper class for NTMultiChannel
 *
 * @author dgh
 */
public class NTMultiChannel
    implements HasAlarm, HasTimeStamp
{
    public static final String URI = "epics:nt/NTMultiChannel:1.0";

    /**
     * Creates an NTMultiChannel wrapping the specified PVStructure if the latter is compatible.
     *
     * Checks the supplied structure is compatible with NTMultiChannel
     * and if so returns a NTMultiChannel which wraps it.
     * This method will return null if the structure is is not compatible
     * or is null.
     * @param pvStructure The PVStructure to be wrapped.
     * @return NTMultiChannel instance on success, null otherwise.
     */
    public static NTMultiChannel wrap(PVStructure pvStructure)
    {
        if (!isCompatible(pvStructure))
            return null;
        return wrapUnsafe(pvStructure);
    }

    /**
     * Creates an NTMultiChannel wrapping the specified PVStructure, regardless of the latter's compatibility.
     *
     * No checks are made as to whether the specified PVStructure
     * is compatible with NTMultiChannel or is non-null.
     * @param pvStructure The PVStructure to be wrapped.
     * @return NTMultiChannel instance.
     */
    public static NTMultiChannel wrapUnsafe(PVStructure pvStructure)
    {
        return new NTMultiChannel(pvStructure);
    }

    /**
     * Checks if the specified structure reports to be a compatible NTMultiChannel.
     *
     * Checks whether the specified structure reports compatibility with this
     * version of NTMultiChannel through type ID, including checking version numbers.
     * The return value does not depend on whether the structure is actually
     * compatible in terms of its introspection type
     * @param structure The Structure to test.
     * @return (false,true) if (is not, is) a compatible NTMultiChannel.
     */
    public static boolean is_a(Structure structure)
    {
        return NTUtils.is_a(structure.getID(), URI);
    }

    /**
     * Checks if the specified structure reports to be a compatible NTMultiChannel.
     *
     * Checks whether the specified structure reports compatibility with this
     * version of NTMultiChannel through type ID, including checking version numbers.
     * The return value does not depend on whether the structure is actually
     * compatible in terms of its introspection type
     * @param pvStructure The PVStructure to test.
     * @return (false,true) if (is not, is) a compatible NTMultiChannel.
     */
    public static boolean is_a(PVStructure pvStructure)
    {
        return is_a(pvStructure.getStructure());
    }

    /**
     * Checks if the specified structure is compatible with NTMultiChannel.
     *
     * Checks whether the specified structure is compatible with this version
     * of NTMultiChannel through introspection interface.
     * @param structure The Structure to test.
     * @return (false,true) if (is not, is) a compatible NTMultiChannel.
     */
    public static boolean isCompatible(Structure structure)
    {
        if (structure == null) return false;

        UnionArray valueField = structure.getField(UnionArray.class,
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
     * Checks if the specified structure is compatible with NTMultiChannel.
     *
     * Checks whether the specified structure is compatible with this version
     * of NTMultiChannel through introspection interface.
     * @param pvStructure The PVStructure to test.
     * @return (false,true) if (is not, is) a compatible NTMultiChannel.
     */
    public static boolean isCompatible(PVStructure pvStructure)
    {
        if (pvStructure == null) return false;

        return isCompatible(pvStructure.getStructure());
    }

    /**
     * Checks if the specified structure is a valid NTMultiChannel.
     *
     * Checks whether the wrapped structure is valid with respect to this
     * version of NTMultiChannel
     * @return (false,true) if (is not, is) a valid NTMultiChannel.
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
     * Create a NTMultiChannelBuilder instance
     * @return builder instance.
     */
    public static NTMultiChannelBuilder createBuilder()
    {
        return new NTMultiChannelBuilder();
    }

    /**
     * Get the pvStructure.
     * @return PVStructure.
     */
    public PVStructure getPVStructure()
    {
        return pvNTMultiChannel;
    }

    /**
     * Get the value of each channel.
     * @return PVUnionArray
     */
    public PVUnionArray getValue()
    {
        return pvValue;
    }

    /**
     * Get the channelName of each channel.
     * @return PVStringArray
     */
    public PVStringArray getChannelName()
    {
       return pvChannelName;
    }

    /**
     * Get the severity of each channel.
     * @return PVIntArray which may be null.
     */
    public PVIntArray getSeverity()
    {
        return pvSeverity;
    }

    /**
     * Get the status of each channel.
     * @return PVIntArray which may be null.
     */
    public PVIntArray getStatus()
    {
        return pvStatus;
    }

    /**
     * Get the message of each channel.
     * @return PVStringArray which may be null.
     */
    public PVStringArray getMessage()
    {
        return pvMessage;
    }

    /**
     * Get the secondsPastEpoch of each channel.
     * @return PVLongArray which may be null.
     */
    public PVLongArray getSecondsPastEpoch()
    {
        return pvSecondsPastEpoch;
    }

    /**
     * Get the nanoseconds of each channel.
     * @return PVIntArray which may be null.
     */
    public PVIntArray getNanoseconds()
    {
        return pvNanoseconds;
    }

    /**
     * Get the userTag of each channel.
     * @return PVIntArray which may be null.
     */
    public PVIntArray getUserTag()
    {
        return pvUserTag;
    }

    /**
     * Get the connection state of each channel.
     * @return PVBooleanArray, which may be null.
     */
    public PVBooleanArray getIsConnected()
    {
        return pvIsConnected;
    }

    /**
     * Get the descriptor.
     * @return PVString which may be null.
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
     * @param pvStructure The PVStructure to be wrapped.
     */
    NTMultiChannel(PVStructure pvStructure)
    {
        pvNTMultiChannel = pvStructure;
        pvValue = pvStructure.getSubField(PVUnionArray.class, "value");
        pvTimeStamp = pvStructure.getSubField(PVStructure.class, "timeStamp");
        pvAlarm = pvStructure.getSubField(PVStructure.class, "alarm");
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

    private PVStructure pvNTMultiChannel;
    private PVUnionArray pvValue;
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

