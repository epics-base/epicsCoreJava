/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.nt;

import org.epics.pvdata.property.PVAlarm;
import org.epics.pvdata.property.PVDisplay;
import org.epics.pvdata.property.PVTimeStamp;
import org.epics.pvdata.pv.*;

/**
 * Wrapper class for NTNDArray.
 *
 * @author dgh
 */
public class NTNDArray
        implements HasTimeStamp, HasAlarm, HasDisplay {
    public static final String URI = "epics:nt/NTNDArray:1.0";

    /**
     * Creates an NTNDArray wrapping the specified PVStructure if the latter is compatible.
     * <p>
     * Checks the supplied PVStructure is compatible with NTNDArray
     * and if so returns an NTNDArray which wraps it.
     * This method will return null if the structure is is not compatible
     * or is null.
     *
     * @param pvStructure the PVStructure to be wrapped
     * @return NTNDArray instance on success, null otherwise
     */
    public static NTNDArray wrap(PVStructure pvStructure) {
        if (!isCompatible(pvStructure))
            return null;
        return wrapUnsafe(pvStructure);
    }


    /**
     * Creates an NTNDArray wrapping the specified PVStructure, regardless of the latter's compatibility.
     * <p>
     * No checks are made as to whether the specified PVStructure
     * is compatible with NTNDArray or is non-null.
     *
     * @param pvStructure the PVStructure to be wrapped
     * @return NTNDArray instance
     */
    public static NTNDArray wrapUnsafe(PVStructure pvStructure) {
        return new NTNDArray(pvStructure);
    }

    /**
     * Returns whether the specified Structure reports to be a compatible NTNDArray.
     * <p>
     * Checks if the specified Structure reports compatibility with this
     * version of NTNDArray through its type ID, including checking version numbers.
     * The return value does not depend on whether the structure is actually
     * compatible in terms of its introspection type.
     *
     * @param structure the pvStructure to test
     * @return (false, true) if (is not, is) a compatible NTNDArray
     */
    public static boolean is_a(Structure structure) {
        return NTUtils.is_a(structure.getID(), URI);
    }

    /**
     * Returns whether the specified PVStructure reports to be a compatible NTNDArray.
     * <p>
     * Checks if the specified PVStructure reports compatibility with this
     * version of NTNDArray through its type ID, including checking version numbers.
     * The return value does not depend on whether the structure is actually
     * compatible in terms of its introspection type.
     *
     * @param pvStructure the PVStructure to test
     * @return (false, true) if (is not, is) a compatible NTNDArray
     */
    public static boolean is_a(PVStructure pvStructure) {
        return is_a(pvStructure.getStructure());
    }

    /**
     * Returns whether the specified Structure is compatible with NTNDArray.
     * <p>
     * Checks if the specified Structure is compatible with this version
     * of NTNDArray through the introspection interface.
     *
     * @param structure the Structure to test
     * @return (false, true) if (is not, is) a compatible NTNDArray
     */
    public static boolean isCompatible(Structure structure) {
        if (structure == null) return false;

        Union valueField = structure.getField(Union.class, "value");
        if (valueField == null)
            return false;

        if (!NTValueType.isCompatible(valueField))
            return false;


        Structure codecField = structure.getField(Structure.class, "codec");

        if (codecField == null)
            return false;

        if (!NTCodec.isCompatible(codecField))
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
        if (dimensionField == null)
            return false;

        Structure dimElementStruc = dimensionField.getStructure();

        if (!NTDimension.isCompatible(dimElementStruc))
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

        if (attributeField == null)
            return false;

        Structure attributeElementStruc = attributeField.getStructure();

        if (!NTNDArrayAttribute.isCompatible(attributeElementStruc))
            return false;


        Field field = structure.getField("descriptor");
        if (field != null) {
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
     * Returns whether the specified PVStructure is compatible with NTNDArray.
     * <p>
     * Checks if the specified PVStructure is compatible with this version
     * of NTNDArray through the introspection interface.
     *
     * @param pvStructure the PVStructure to test
     * @return (false, true) if (is not, is) a compatible NTNDArray
     */
    public static boolean isCompatible(PVStructure pvStructure) {
        if (pvStructure == null) return false;

        return isCompatible(pvStructure.getStructure());
    }

    /**
     * Returns whether the wrapped PVStructure is valid with respect to this
     * version of NTNDArray.
     * <p>
     * Unlike isCompatible(), isValid() may perform checks on the value
     * data as well as the introspection data.
     *
     * @return (false, true) if wrapped PVStructure (is not, is) a valid NTNDArray
     */
    public boolean isValid() {
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

    public static NTNDArrayBuilder createBuilder() {
        return new NTNDArrayBuilder();
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.nt.HasDisplay#attachDisplay(org.epics.pvdata.property.PVDisplay)
     */
    public boolean attachDisplay(PVDisplay pvDisplay) {
        PVStructure dp = getDisplay();
        if (dp != null)
            return pvDisplay.attach(dp);
        else
            return false;
    }

    /**
     * Returns the PVStructure wrapped by this instance.
     *
     * @return the PVStructure wrapped by this instance
     */
    public PVStructure getPVStructure() {
        return pvNTNDArray;
    }

    /**
     * Returns the value field.
     *
     * @return the value field
     */
    public PVUnion getValue() {
        return pvNTNDArray.getSubField(PVUnion.class, "value");
    }

    /**
     * Returns the codec field.
     *
     * @return the codec field
     */
    public PVStructure getCodec() {
        return pvNTNDArray.getSubField(PVStructure.class, "codec");
    }

    /**
     * Returns the compressedDataSize field.
     *
     * @return the compressedDataSize field
     */
    public PVLong getCompressedDataSize() {
        return pvNTNDArray.getSubField(PVLong.class, "compressedSize");
    }

    /**
     * Returns the uncompressedDataSize field.
     *
     * @return the uncompressedDataSize field
     */
    public PVLong getUncompressedDataSize() {
        return pvNTNDArray.getSubField(PVLong.class, "uncompressedSize");
    }

    /**
     * Returns the dimension field.
     *
     * @return dimension field
     */
    public PVStructureArray getDimension() {
        return pvNTNDArray.getSubField(PVStructureArray.class, "dimension");
    }

    /**
     * Returns the uniqueId field.
     *
     * @return the uniqueId field
     */
    public PVInt getUniqueId() {
        return pvNTNDArray.getSubField(PVInt.class, "uniqueId");
    }

    /**
     * Returns the dataTimeStamp field.
     *
     * @return dataTimeStamp field
     */
    public PVStructure getDataTimeStamp() {
        return pvNTNDArray.getSubField(PVStructure.class, "dataTimeStamp");
    }

    /**
     * Returns the attribute field.
     *
     * @return the attribute field
     */
    public PVStructureArray getAttribute() {
        return pvNTNDArray.getSubField(PVStructureArray.class, "attribute");
    }

    /**
     * Returns the descriptor field.
     *
     * @return the descriptor field or null if no such field
     */
    public PVString getDescriptor() {
        return pvNTNDArray.getSubField(PVString.class, "descriptor");
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.nt.HasAlarm#getAlarm()
     */
    public PVStructure getAlarm() {
        return pvNTNDArray.getSubField(PVStructure.class, "alarm");
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.nt.HasTimeStamp#getTimeStamp()
     */
    public PVStructure getTimeStamp() {
        return pvNTNDArray.getSubField(PVStructure.class, "timeStamp");
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.nt.Has#getDisplay()
     */
    public PVStructure getDisplay() {
        return pvNTNDArray.getSubField(PVStructure.class, "display");
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.nt.HasTimeStamp#attachTimeStamp(org.epics.pvdata.property.PVTimeStamp)
     */
    public boolean attachTimeStamp(PVTimeStamp pvTimeStamp) {
        PVStructure ts = getTimeStamp();
        if (ts != null)
            return pvTimeStamp.attach(ts);
        else
            return false;
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.nt.HasAlarm#attachAlarm(org.epics.pvdata.property.PVAlarm)
     */
    public boolean attachAlarm(PVAlarm pvAlarm) {
        PVStructure al = getAlarm();
        if (al != null)
            return pvAlarm.attach(al);
        else
            return false;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return getPVStructure().toString();
    }

    /**
     * Constructor.
     *
     * @param pvStructure the PVStructure to be wrapped
     */
    NTNDArray(PVStructure pvStructure) {
        pvNTNDArray = pvStructure;
    }

    private long getExpectedUncompressedSize() {
        long size = 0;
        PVStructureArray pvDim = getDimension();

        if (pvDim.getLength() != 0) {
            size = getValueTypeSize();
            StructureArrayData data = new StructureArrayData();
            pvDim.get(0, pvDim.getLength(), data);
            for (PVStructure dim : data.data) {
                size *= dim.getSubField(PVInt.class, "size").get();
            }
        }

        return size;
    }

    private long getValueSize() {
        long size = 0;
        PVScalarArray storedValue = getValue().get(PVScalarArray.class);
        if (storedValue != null) {
            size = (long) storedValue.getLength() * getValueTypeSize();
        }
        return size;
    }

    private int getValueTypeSize() {
        int typeSize = 0;
        PVScalarArray storedValue = getValue().get(PVScalarArray.class);
        if (storedValue != null) {
            switch (storedValue.getScalarArray().getElementType()) {
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


class NTValueType {
    public static boolean isCompatible(Union u) {
        if (u == null) return false;

        if (u.getID() != Union.DEFAULT_ID) return false;
        if (u.isVariant()) return false;

        for (ScalarType scalarType : ScalarType.values()) {
            if (scalarType != ScalarType.pvString) {
                String name = scalarType.toString() + "Value";
                ScalarArray scalarField = u.getField(ScalarArray.class, name);
                if (scalarField == null ||
                        scalarField.getElementType() != scalarType)
                    return false;
            }
        }

        return true;
    }
};

class NTCodec {
    public static boolean isCompatible(Structure structure) {
        if (structure == null) return false;

        if (structure.getID() != "codec_t") return false;

        Scalar scalarField = structure.getField(Scalar.class, "name");
        if (scalarField == null || scalarField.getScalarType() != ScalarType.pvString)
            return false;

        Union paramField = structure.getField(Union.class, "parameters");
        if (paramField == null || !paramField.isVariant())
            return false;

        return true;
    }
};


class NTDimension {
    public static boolean isCompatible(Structure structure) {
        if (structure == null) return false;

        if (structure.getID() != "dimension_t") return false;

        Scalar scalarField = structure.getField(Scalar.class, "size");
        if (scalarField == null || scalarField.getScalarType() != ScalarType.pvInt)
            return false;

        scalarField = structure.getField(Scalar.class, "offset");
        if (scalarField == null || scalarField.getScalarType() != ScalarType.pvInt)
            return false;

        scalarField = structure.getField(Scalar.class, "fullSize");
        if (scalarField == null || scalarField.getScalarType() != ScalarType.pvInt)
            return false;

        scalarField = structure.getField(Scalar.class, "binning");
        if (scalarField == null || scalarField.getScalarType() != ScalarType.pvInt)
            return false;

        scalarField = structure.getField(Scalar.class, "reverse");
        if (scalarField == null || scalarField.getScalarType() != ScalarType.pvBoolean)
            return false;

        return true;
    }
};


