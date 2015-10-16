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
     * Checks the supplied PVStructure is compatible with NTAttribute
     * extended as required by NTNDArray and if so returns an
     * NTNDArrayAttribute which wraps it.
     * This method will return null if the structure is is not compatible
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
     * is compatible with NTAttribute extended as required by NTNDArray
     * or is non-null.
     * 
     * @param pvStructure the PVStructure to be wrapped
     * @return NTAttribute instance
     */
    public static NTNDArrayAttribute wrapUnsafe(PVStructure pvStructure)
    {
        return new NTNDArrayAttribute(pvStructure);
    }

    /**
     * Returns whether the specified Structure is compatible with NTAttribute 
     * extended as required by NTNDArray.
     * <p>
     * Checks if the specified Structure is compatible with this version
     * of NTAttribute extended as required by this version of NTNDArray
     * through the introspection interface.
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
     * Returns whether the specified PVStructure is compatible with NTAttribute
     *  extended as required by NTNDArray..
     * <p>
     * Checks if the specified PVStructure is compatible with this version
     * of NTAttribute extended as required by this version of NTNDArray
     * through the introspection interface.

     * @param pvStructure the PVStructure to test
     * @return (false,true) if (is not, is) a compatible NTAttribute
     */
    public static boolean isCompatible(PVStructure pvStructure)
    {
        if (pvStructure == null) return false;

        return isCompatible(pvStructure.getStructure());
    }

    /**
     * Creates an NTNDArrayAttribute builder instance.
     * 
     * @return builder instance
     */
    public static NTNDArrayAttributeBuilder createBuilder()
    {
        return new NTNDArrayAttributeBuilder();
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
     * Returns the sourceType field.
     *
     * @return the sourceType field
     */
    public PVInt getSourceType()
    {
        return pvNTAttribute.getSubField(PVInt.class, "sourceType");
    }

    /**
     * Returns the source field.
     *
     * @return the source field
     */
    public PVString getSource()
    {
        return pvNTAttribute.getSubField(PVString.class, "source");
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


