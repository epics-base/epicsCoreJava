/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.nt;

import org.epics.pvdata.pv.Structure;
import org.epics.pvdata.pv.Union;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVInt;
import org.epics.pvdata.pv.PVLong;
import org.epics.pvdata.pv.PVScalar;
import org.epics.pvdata.pv.PVScalarArray;
import org.epics.pvdata.pv.PVStringArray;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.PVStructureArray;
import org.epics.pvdata.pv.PVUnion;
import org.epics.pvdata.property.PVTimeStamp;
import org.epics.pvdata.property.PVAlarm;
import org.epics.pvdata.property.PVDisplay;

/**
 * Wrapper class for NTNDArray
 *
 * @author dgh
 */
public class NTNDArray
    implements HasTimeStamp, HasAlarm, HasDisplay
{
    public static final String URI = "epics:nt/NTNDArray:1.0";

    /**
     * Creates an NTNDArray wrapping the specified PVStructure if the latter is compatible.
     *
     * Checks the supplied structure is compatible with NTNDArray
     * and if so returns a NTNDArray which wraps it.
     * This method will return null if the structure is is not compatible
     * or is null.
     * @param pvStructure The PVStructure to be wrapped.
     * @return NTNDArray instance on success, null otherwise.
     */
    public static NTNDArray wrap(PVStructure pvStructure)
    {
        if (!isCompatible(pvStructure))
            return null;
        return wrapUnsafe(pvStructure);
    }


    /**
     * Creates an NTNDArray wrapping the specified PVStructure, regardless of the latter's compatibility.
     *
     * No checks are made as to whether the specified PVStructure
     * is compatible with NTNDArray or is non-null.
     * @param pvStructure The PVStructure to be wrapped.
     * @return NTNDArray instance.
     */
    public static NTNDArray wrapUnsafe(PVStructure pvStructure)
    {
        return new NTNDArray(pvStructure);
    }

    /**
     * Checks if the specified structure reports to be a compatible NTNDArray.
     *
     * Checks whether the specified structure reports compatibility with this
     * version of NTNDArray through type ID, including checking version numbers.
     * The return value does not depend on whether the structure is actually
     * compatible in terms of its introspection type
     * @param structure The pvStructure to test.
     * @return (false,true) if (is not, is) a compatible NTNDArray.
     */
    public static boolean is_a(Structure structure)
    {
        return NTUtils.is_a(structure.getID(), URI);
    }

    /**
     * Checks if the specified structure is compatible with NTNDArray.
     *
     * Checks whether the specified structure is compatible with this version
     * of NTNDArray through introspection interface.
     * @param pvStructure The PVStructure to test.
     * @return (false,true) if (is not, is) a compatible NTNDArray.
     */
    public static boolean isCompatible(PVStructure pvStructure)
    {
        NTField ntField = NTField.get();

        if (pvStructure == null) return false;
        PVUnion pvValue = pvStructure.getSubField(PVUnion.class, "value");
        if (pvValue == null) return false;

        PVField pvField = pvStructure.getSubField("descriptor");
        if (pvField != null && pvStructure.getSubField(PVString.class, "descriptor") == null)
            return false;

        if(pvStructure.getSubField(PVLong.class, "compressedSize") == null) return false;
        if(pvStructure.getSubField(PVLong.class, "uncompressedSize") == null) return false;
        PVStructure pvCodec = pvStructure.getSubField(PVStructure.class, "codec");
        if(pvCodec == null) return false;
        if(pvCodec.getSubField(PVString.class, "name")== null) return false;
        if(pvCodec.getSubField(PVUnion.class, "parameters")== null) return false;
        PVStructureArray pvDimension = pvStructure.getSubField(PVStructureArray.class, "dimension");
        if(!pvDimension.getStructureArray().getStructure().getID().equals("dimension_t")) return false;
        if(pvStructure.getSubField(PVInt.class, "uniqueId") == null) return false;
        pvField = pvStructure.getSubField("dataTimeStamp");
        if(pvField != null && !ntField.isTimeStamp(pvField.getField())) return false;
        PVStructureArray pvAttribute = pvStructure.
            getSubField(PVStructureArray.class, "attribute");
        if(pvAttribute == null) return false;
        if(!pvAttribute.getStructureArray().getStructure().
            getID().equals(NTNDArray.NTAttributeURI)) return false;

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

    public static NTNDArrayBuilder createBuilder()
    {
        return new NTNDArrayBuilder();
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

    /**
     * Get the pvStructure.
     * @return PVStructure.
     */
    public PVStructure getPVStructure()
    {
        return pvNTNDArray;
    }

    /**
     * Get the value field.
     * @return The PVField for the values.
     */
    public PVUnion getValue()
    {
        return pvNTNDArray.getSubField(PVUnion.class, "value");
    }

    /**
     * Get the codec field.
     * @return the PVStructure.
     */
    public PVStructure getCodec()
    {
        return pvNTNDArray.getSubField(PVStructure.class, "codec");
    }

    /**
     * Get the compressedDataSize field.
     * @return PVStructure.
     */
    public PVLong getCompressedDataSize()
    {
        return pvNTNDArray.getSubField(PVLong.class, "compressedSize");
    }

    /**
     * Get the uncompressedDataSize field.
     * @return PVStructure.
     */
    public PVLong getUncompressedDataSize()
    {
        return pvNTNDArray.getSubField(PVLong.class, "uncompressedSize");
    }

    /**
     * Get the dimension field.
     * @return the PVStructure.
     */
    public PVStructureArray getDimension()
    {
        return pvNTNDArray.getSubField(PVStructureArray.class, "dimension");
    }

    /**
     * Get the uniqueId field.
     * @return PVInt
     */
    public PVInt getUniqueId()
    {
        return pvNTNDArray.getSubField(PVInt.class, "uniqueId");
    }

    /**
     * Get the data timeStamp field.
     * @return PVStructure.
     */
    public PVStructure getDataTimeStamp()
    {    
        return pvNTNDArray.getSubField(PVStructure.class, "dataTimeStamp");
    }

    /**
     * Get the attribute field.
     * @return the PVStructure.
     */
    public PVStructureArray getAttribute()
    {
        return pvNTNDArray.getSubField(PVStructureArray.class, "attribute");
    }

    /**
     * Get the descriptor field.
     * @return The pvString or null if no function field.
     */
    public PVString getDescriptor()
    {
        return pvNTNDArray.getSubField(PVString.class, "descriptor");
    }

    /* (non-Javadoc)
	 * @see org.epics.pvdata.nt.HasAlarm#getAlarm()
	 */
    public PVStructure getAlarm()
    {
       return pvNTNDArray.getSubField(PVStructure.class, "alarm");
    }

    /* (non-Javadoc)
	 * @see org.epics.pvdata.nt.HasTimeStamp#getTimeStamp()
	 */
    public PVStructure getTimeStamp()
    {
        return pvNTNDArray.getSubField(PVStructure.class, "timeStamp");
    }

    /* (non-Javadoc)
	 * @see org.epics.pvdata.nt.Has#getDisplay()
	 */
    public PVStructure getDisplay()
    {
       return pvNTNDArray.getSubField(PVStructure.class, "display");
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
    NTNDArray(PVStructure pvStructure)
    {
        pvNTNDArray = pvStructure;
    }

    static final String NTAttributeURI = "epics:nt/NTAttribute:1.0";
    private PVStructure pvNTNDArray;
}

