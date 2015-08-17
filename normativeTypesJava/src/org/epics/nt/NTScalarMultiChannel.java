/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.nt;

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
 * Wrapper class for NTScalarMultiChannel
 *
 * @author dgh
 */
public class NTScalarMultiChannel
    implements HasTimeStamp, HasAlarm
{
    public static final String URI = "epics:nt/NTScalarMultiChannel:1.0";

    /**
     * Creates an NTScalarMultiChannel wrapping the specified PVStructure if the latter is compatible.
     *
     * Checks the supplied structure is compatible with NTScalarMultiChannel
     * and if so returns a NTScalarMultiChannel which wraps it.
     * This method will return null if the structure is is not compatible
     * or is null.
     * @param pvStructure The PVStructure to be wrapped.
     * @return NTScalarMultiChannel instance on success, null otherwise.
     */
    public static NTScalarMultiChannel wrap(PVStructure pvStructure)
    {
        if (!isCompatible(pvStructure))
            return null;
        return wrapUnsafe(pvStructure);
    }

    /**
     * Creates an NTScalarMultiChannel wrapping the specified PVStructure, regardless of the latter's compatibility.
     *
     * No checks are made as to whether the specified PVStructure
     * is compatible with NTScalarMultiChannel or is non-null.
     * @param pvStructure The PVStructure to be wrapped.
     * @return NTScalarMultiChannel instance.
     */
    public static NTScalarMultiChannel wrapUnsafe(PVStructure pvStructure)
    {
        return new NTScalarMultiChannel(pvStructure);
    }

    /**
     * Checks if the specified structure reports to be a compatible NTScalarMultiChannel.
     *
     * Checks whether the specified structure reports compatibility with this
     * version of NTScalarMultiChannel through type ID, including checking version numbers.
     * The return value does not depend on whether the structure is actually
     * compatible in terms of its introspection type
     * @param structure The structure to test.
     * @return (false,true) if (is not, is) a compatible NTScalarMultiChannel.
     */
    public static boolean is_a(Structure structure)
    {
        return NTUtils.is_a(structure.getID(), URI);
    }

    /**
     * Checks if the specified structure is compatible with NTScalarMultiChannel.
     *
     * Checks whether the specified structure is compatible with this version
     * of NTScalarMultiChannel through introspection interface.
     * @param pvStructure The PVStructure to test.
     * @return (false,true) if (is not, is) a compatible NTScalarMultiChannel.
     */
    public static boolean isCompatible(PVStructure pvStructure)
    {
        if (pvStructure == null)
            return false;

        PVScalarArray pvValue = pvStructure.getSubField(PVScalarArray.class, "value");
        if (pvValue== null)
            return false;

        PVField pvField = pvStructure.getSubField("descriptor");
        if (pvField != null && pvStructure.getSubField(PVString.class, "descriptor")== null)
            return false;

        NTField ntField = NTField.get();

        pvField = pvStructure.getSubField("alarm");
        if (pvField != null && !ntField.isAlarm(pvField.getField()))
            return false;

        pvField = pvStructure.getSubField("timeStamp");
        if (pvField != null && !ntField.isTimeStamp(pvField.getField()))
            return false;

        pvField = pvStructure.getSubField("severity");
        if (pvField != null && pvStructure.getSubField(PVIntArray.class, "severity")== null)
            return false;

        pvField = pvStructure.getSubField("status");
        if (pvField != null && pvStructure.getSubField(PVIntArray.class, "status") == null)
            return false;

        pvField = pvStructure.getSubField("message");
        if (pvField != null && pvStructure.getSubField(PVStringArray.class, "message")== null)
            return false;

        pvField = pvStructure.getSubField("secondsPastEpoch");
        if (pvField != null && pvStructure.getSubField(PVLongArray.class, "secondsPastEpoch")== null)
            return false;

        pvField = pvStructure.getSubField("nanoseconds");
        if (pvField != null && pvStructure.getSubField(PVIntArray.class, "nanoseconds")== null)
            return false;

        pvField = pvStructure.getSubField("userTag");
        if (pvField != null && pvStructure.getSubField(PVIntArray.class, "userTag")== null)
            return false;

        return true;
    }

    /**
     * Create a NTScalarMultiChannelBuilder instance
     * @return builder instance.
     */
    public static NTScalarMultiChannelBuilder createBuilder()
    {
        return new NTScalarMultiChannelBuilder();
    }

    /**
     * Get the pvStructure.
     * @return PVStructure.
     */
    public PVStructure getPVStructure()
    {
        return pvNTScalarMultiChannel;
    }

    /**
     * Get the display field.
     * @return PVStructure which may be null.
     */
    public PVScalarArray getValue()
    {
        return pvValue;
    }

    /* Get the value field of a specified type (e.g. PVDoubleArray).
     * @param c expected class of a requested field.
     * @return The PVField or null if the subfield does not exist, or the field is not of <code>c</code> type.
     */
    public <T extends PVScalarArray> T getValue(Class<T> c)
    {
		if (c.isInstance(pvValue))
			return c.cast(pvValue);
		else
			return null;
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
     * @return PVBooleanArray
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

