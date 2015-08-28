/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.nt;

import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.Scalar;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Structure;
import org.epics.pvdata.pv.StructureArray;
import org.epics.pvdata.pv.Union;
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
import org.epics.pvdata.pv.StructureArrayData;
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
     * Checks if the specified structure reports to be a compatible NTNDArray.
     *
     * Checks whether the specified structure reports compatibility with this
     * version of NTNDArray through type ID, including checking version numbers.
     * The return value does not depend on whether the structure is actually
     * compatible in terms of its introspection type
     * @param pvStructure The PVStructure to test.
     * @return (false,true) if (is not, is) a compatible NTNDArray.
     */
    public static boolean is_a(PVStructure pvStructure)
    {
        return is_a(pvStructure.getStructure());
    }

    /**
     * Checks if the specified structure is compatible with NTNDArray.
     *
     * Checks whether the specified structure is compatible with this version
     * of NTNDArray through introspection interface.
     * @param structure The Structure to test.
     * @return (false,true) if (is not, is) a compatible NTNDArray.
     */
    public static boolean isCompatible(Structure structure)
    {
        if (structure == null) return false;

        Union valueField = structure.getField(Union.class,"value");
        if (valueField == null)
            return false;

        // TODO: Do this without converting to string
        String valueTypeStr = NTNDArrayBuilder.getValueType().
            toString();

        if (!valueField.toString().equals(valueTypeStr))
            return false;


        Structure codecField = structure.getField(Structure.class, "codec");

        if(!codecField.getID().equals("codec_t")) return false;

        // TODO: Do this without converting to string
        String codecStrucStr = NTNDArrayBuilder.getCodecStructure().
            toString();

        if (!codecField.toString().equals(codecStrucStr))
            return false;


        Scalar compressedSizeField = structure.getField(Scalar.class, "compressedSize");
        if (compressedSizeField == null)
            return false;

        if (compressedSizeField.getScalarType() != ScalarType.pvLong)
            return false;


        Scalar uncompressedSizeField = structure.getField(Scalar.class, "uncompressedSize");
        if (uncompressedSizeField == null)
            return false;

        if (uncompressedSizeField.getScalarType() != ScalarType.pvLong)
            return false;


        StructureArray dimensionField = structure.getField(StructureArray.class, "dimension");

        Structure dimElementStruc = dimensionField.getStructure();

        if(!dimElementStruc.getID().equals("dimension_t"))
            return false;

        // TODO: Do this without converting to string
        String dimensionStrucStr = NTNDArrayBuilder.
             getDimensionStructure().toString();

        if (!dimElementStruc.toString().equals(dimensionStrucStr))
            return false;


        NTField ntField = NTField.get();

        Structure dataTimeStampField = structure.getField(Structure.class,
            "dataTimeStamp");
        if (dataTimeStampField == null || !ntField.isTimeStamp(dataTimeStampField))
            return false;


        Scalar uniqueIdField = structure.getField(Scalar.class, "uniqueId");
        if (uniqueIdField == null)
            return false;

        if (uniqueIdField.getScalarType() != ScalarType.pvInt)
            return false;


        StructureArray attributeField = structure.getField(StructureArray.class, "attribute");

        Structure attributeElementStruc = attributeField.getStructure();

        if (!NTNDArrayAttribute.isCompatible(attributeElementStruc))
            return false;

        Field field = structure.getField("descriptor");
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

        field = structure.getField("display");
        if (field != null && !ntField.isDisplay(field))
            return false;

        return true;
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
        if (pvStructure == null) return false;

        return isCompatible(pvStructure.getStructure());
    }

    /**
     * Checks if the specified structure is a valid NTNDArray.
     *
     * Checks whether the wrapped structure is valid with respect to this
     * version of NTNDArray
     * @return (false,true) if (is not, is) a valid NTNDArray.
     */
    public boolean isValid()
    {
        long valueSize = getValueSize();
        long compressedSize = getCompressedDataSize().get();
        if (valueSize != compressedSize)
            return false;

        long expectedUncompressed = getExpectedUncompressedSize();
        long uncompressedSize = getUncompressedDataSize().get();
        if (uncompressedSize != expectedUncompressed)
            return false;

        String codecName = getCodec().getSubField(PVString.class, "name").get();
        if (codecName.equals("") && valueSize < uncompressedSize)
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

    private long getExpectedUncompressedSize()
    {
        long size = 0;
        PVStructureArray pvDim = getDimension();

        if (pvDim.getLength() != 0)
        {
            size = getValueTypeSize();
            StructureArrayData data = new StructureArrayData();
            pvDim.get(0, pvDim.getLength(),data);
            for (PVStructure dim : data.data)
            {
                size *= dim.getSubField(PVInt.class, "size").get();
            }
        }

        return size;
    }

    private long getValueSize()
    {
        long size = 0;
        PVScalarArray storedValue = getValue().get(PVScalarArray.class);
        if (storedValue != null)
        {
            size = storedValue.getLength()*getValueTypeSize();
        }
        return size;
    }

    private int getValueTypeSize()
    {
        int typeSize = 0;
        PVScalarArray storedValue = getValue().get(PVScalarArray.class);
        if (storedValue != null)
        {
            switch (storedValue.getScalarArray().getElementType())
            {
            case pvBoolean:
            case pvByte:
            case pvUByte:
                typeSize = 1;
                break;

            case pvShort:
            case pvUShort:
                typeSize = 2;
                break;

            case pvInt:
            case pvUInt:
            case pvFloat:
                typeSize = 4;
                break;

            case pvLong:
            case pvULong:
            case pvDouble:
                typeSize = 8;
                break;

            default:
                break;
            }
        }
        return typeSize;
    }

    static final String NTAttributeURI = "epics:nt/NTAttribute:1.0";
    private PVStructure pvNTNDArray;
}

