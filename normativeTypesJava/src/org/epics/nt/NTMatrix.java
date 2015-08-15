/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.nt;

import org.epics.pvdata.pv.Structure;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVDoubleArray;
import org.epics.pvdata.pv.PVIntArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.property.PVTimeStamp;
import org.epics.pvdata.property.PVAlarm;
import org.epics.pvdata.property.PVDisplay;
import org.epics.pvdata.property.PVControl;

/**
 * Wrapper class for NTMatrix
 *
 * @author dgh
 */
public class NTMatrix
    implements HasTimeStamp, HasAlarm, HasDisplay
{
    public static final String URI = "epics:nt/NTMatrix:1.0";

    /**
     * Creates an NTMatrix wrapping the specified PVStructure if the latter is compatible.
     *
     * Checks the supplied structure is compatible with NTMatrix
     * and if so returns a NTMatrix which wraps it.
     * This method will return null if the structure is is not compatible
     * or is null.
     * @param structure The structure to be wrapped.
     * @return NTMatrix instance on success, null otherwise.
     */
    public static NTMatrix wrap(PVStructure structure)
    {
        if (!isCompatible(structure))
            return null;
        return wrapUnsafe(structure);
    }

    /**
     * Creates an NTMatrix wrapping the specified PVStructure, regardless of the latter's compatibility.
     *
     * No checks are made as to whether the specified PVStructure
     * is compatible with NTMatrix or is non-null.
     * @param pvStructure The PVStructure to be wrapped.
     * @return NTMatrix instance.
     */
    public static NTMatrix wrapUnsafe(PVStructure pvStructure)
    {
        return new NTMatrix(pvStructure);
    }

    /**
     * Checks if the specified structure reports to be a compatible NTMatrix.
     *
     * Checks whether the specified structure reports compatibility with this
     * version of NTMatrix through type ID, including checking version numbers.
     * The return value does not depend on whether the structure is actually
     * compatible in terms of its introspection type
     * @param structure The pvStructure to test.
     * @return (false,true) if (is not, is) a compatible NTMatrix.
     */
    public static boolean is_a(Structure structure)
    {
        return NTUtils.is_a(structure.getID(), URI);
    }

    /**
     * Checks if the specified structure is compatible with NTMatrix.
     *
     * Checks whether the specified structure is compatible with this version
     * of NTMatrix through introspection interface.
     * @param pvStructure The PVStructure to test.
     * @return (false,true) if (is not, is) a compatible NTMatrix.
     */
    public static boolean isCompatible(PVStructure pvStructure)
    {
        if (pvStructure == null) return false;

        PVDoubleArray pvValue = pvStructure.getSubField(PVDoubleArray.class, "value");
        if (pvValue == null) return false;

        PVIntArray pvDim = pvStructure.getSubField(PVIntArray.class, "dim");
        if (pvDim == null) return false;

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

        return true;
    }

    /**
     * Create a NTMatrix builder instance.
     * @return builder instance.
     */
    public static NTMatrixBuilder createBuilder()
    {
        return new NTMatrixBuilder();
    }

    /**
     * Get the pvStructure.
     * @return PVStructure.
     */
    public PVStructure getPVStructure()
    {
        return pvNTMatrix;
    }


    /**
     * Get the value field.
     * @return The PVDoubleArray for the values.
     */
    public PVDoubleArray getValue()
    {
        return pvValue;
    }

    /**
     * Get the value field.
     * @return The PVIntArray for the values.
     */
    public PVIntArray getDim()
    {
        return pvNTMatrix.getSubField(PVIntArray.class, "dim");
    }


    /**
     * Get the descriptor field.
     * @return The pvString or null if no function field.
     */
    public PVString getDescriptor()
    {
        return pvNTMatrix.getSubField(PVString.class, "descriptor");
    }

    /* (non-Javadoc)
	 * @see org.epics.pvdata.nt.HasAlarm#getAlarm()
	 */
    public PVStructure getAlarm()
    {
       return pvNTMatrix.getSubField(PVStructure.class, "alarm");
    }

    /* (non-Javadoc)
	 * @see org.epics.pvdata.nt.HasTimeStamp#getTimeStamp()
	 */
    public PVStructure getTimeStamp()
    {
        return pvNTMatrix.getSubField(PVStructure.class, "timeStamp");
    }

    /* (non-Javadoc)
	 * @see org.epics.pvdata.nt.Has#getDisplay()
	 */
    public PVStructure getDisplay()
    {
       return pvNTMatrix.getSubField(PVStructure.class, "display");
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
    NTMatrix(PVStructure pvStructure)
    {
        pvNTMatrix = pvStructure;
        pvValue = pvNTMatrix.getSubField(PVDoubleArray.class, "value");
    }

    private PVStructure pvNTMatrix;
    private PVDoubleArray pvValue;
}


