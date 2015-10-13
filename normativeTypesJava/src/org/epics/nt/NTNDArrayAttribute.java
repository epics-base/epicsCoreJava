/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.nt;

import org.epics.pvdata.pv.Scalar;
import org.epics.pvdata.pv.ScalarType;
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
 * Wrapper class for NTAttribute extended as required by NTNDArray.
 *
 * @author dgh
 */
public class NTNDArrayAttribute extends NTAttribute
   implements HasAlarm, HasTimeStamp
{

    /**
     * Creates an NTNDArrayAttribute wrapping the specified PVStructure if the latter is compatible.
     * <p>
     * Checks the supplied structure is compatible with NTAttribute as extended
     * by NTNDArray and if so returns a NTAttribute which wraps it.
     * This method will return null if the structure is not compatible
     * or is null.
     * 
     * @param pvStructure the PVStructure to be wrapped
     * @return NTAttribute instance on success, null otherwise
     */
    public static NTNDArrayAttribute wrap(PVStructure pvStructure)
    {
        if (!isCompatible(pvStructure))
            return null;
        return wrapUnsafe(pvStructure);
    }

    /**
     * Creates an NTNDArrayAttribute wrapping the specified PVStructure, regardless of the latter's compatibility.
     * <p>
     * No checks are made as to whether the specified PVStructure
     * is compatible with NTAttribute or is non-null.
     * 
     * @param pvStructure the PVStructure to be wrapped
     * @return NTAttribute instance
     */
    public static NTNDArrayAttribute wrapUnsafe(PVStructure pvStructure)
    {
        return new NTNDArrayAttribute(pvStructure);
    }

    /**
     * Checks if the specified structure is compatible with NTAttribute.
     * <p>
     * Checks whether the specified structure is compatible with this version
     * of NTAttribute through introspection interface.
     * 
     * @param structure the Structure to test
     * @return (false,true) if (is not, is) a compatible NTAttribute
     */
    public static boolean isCompatible(Structure structure)
    {
        if (!NTAttribute.isCompatible(structure)) return false;

        Scalar descriptorField = structure.getField(Scalar.class, "descriptor");
        if (descriptorField == null)
            return false;

        if (descriptorField.getScalarType() != ScalarType.pvString)
            return false;

        Scalar sourceField = structure.getField(Scalar.class, "source");
        if (sourceField == null)
            return false;

        if (sourceField.getScalarType() != ScalarType.pvString)
            return false;

        Scalar sourceTypeField = structure.getField(Scalar.class, "sourceType");
        if (sourceTypeField == null)
            return false;

        if (sourceTypeField.getScalarType() != ScalarType.pvInt)
            return false;

        return true;
    }

    /**
     * Checks if the specified structure is compatible with NTAttribute.
     * <p>
     * Checks whether the specified structure is compatible with this version
     * of NTAttribute through introspection interface.

     * @param pvStructure the PVStructure to test
     * @return (false,true) if (is not, is) a compatible NTAttribute
     */
    public static boolean isCompatible(PVStructure pvStructure)
    {
        if (pvStructure == null) return false;

        return isCompatible(pvStructure.getStructure());
    }

    /**
     * Create an NTNDArrayAttribute builder instance.
     * 
     * @return builder instance.
     */
    public static NTNDArrayAttributeBuilder createBuilder()
    {
        return new NTNDArrayAttributeBuilder();
    }

    /**
     * Get the pvStructure.
     * 
     * @return PVStructure
     */
    public PVStructure getPVStructure()
    {
        return pvNTAttribute;
    }

    /**
     * Get the source field.
     * 
     * @return the PVString for the source field
     */
    public PVString getSource()
    {
        return pvNTAttribute.getSubField(PVString.class, "source");
    }

    /**
     * Get the sourceType field.
     * 
     * @return the PVInt for the sourceType field
     */
    public PVInt getSourceType()
    {
        return pvNTAttribute.getSubField(PVInt.class, "sourceType");
    }

    /**
     * Get the descriptor field.
     *
     * @return the PVString for the descriptor field
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

    /**
     * Constructor.
     * 
     * @param pvStructure the PVStructure to be wrapped
     */
    NTNDArrayAttribute(PVStructure pvStructure)
    {
        super(pvStructure);
    }

}


