/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */

package org.epics.nt;

import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.FieldBuilder;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Structure;
import org.epics.pvdata.pv.Union;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;

import java.util.ArrayList;

import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVInt;
import org.epics.pvdata.pv.PVLong;
import org.epics.pvdata.pv.PVScalarArray;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.PVStructureArray;
import org.epics.pvdata.pv.PVUnion;
import org.epics.pvdata.property.PVTimeStamp;
import org.epics.pvdata.property.PVAlarm;
import org.epics.pvdata.property.PVDisplay;
/**
 * Interface for in-line creating of NTNDArray.
 *
 * One instance can be used to create multiple instances.
 * An instance of this object must not be used concurrently (an object has a state).
 * @author dgh
 */
public class NTNDArrayBuilder
{
    /**
     * Add descriptor field to the NTNDArray.
     * 
     * @return this instance of <b>NTNDArrayBuilder</b>
     */
    public NTNDArrayBuilder addDescriptor()
    {
        descriptor = true;
        return this;
    }

    /**
     * Add alarm structure to the NTNDArray.
     * 
     * @return this instance of <b>NTNDArrayBuilder</b>
     */
    public NTNDArrayBuilder addAlarm()
    {
        alarm = true;
        return this;
    }

    /**
     * Add timeStamp structure to the NTNDArray.
     * 
     * @return this instance of <b>NTNDArrayBuilder</b>
     */
    public NTNDArrayBuilder addTimeStamp()
    {
        timeStamp = true;
        return this;
    }

    /**
     * Add display structure to the NTNDArray.
     * 
     * @return this instance of <b>NTNDArrayBuilder</b>
     */
    public NTNDArrayBuilder addDisplay()
    {
        display = true;
        return this;
    }

    /**
     * Create a <b>Structure</b> that represents NTNDArray.
     * This resets this instance state and allows new instance to be created
     * 
     * @return a new instance of a <b>Structure</b>
     */
    public Structure createStructure()
    {
        int index = 0;

        FieldCreate fieldCreate = FieldFactory.getFieldCreate();

        FieldBuilder builder =
            FieldFactory.getFieldCreate().createFieldBuilder();

        NTField ntField = NTField.get();

        builder.setId(NTNDArray.URI).
            add("value", getValueType()).
            add("codec", getCodecStructure()).
            add("compressedSize", ScalarType.pvLong).
            add("uncompressedSize", ScalarType.pvLong).
            addArray("dimension", getDimensionStructure()).
            add("uniqueId", ScalarType.pvInt).
            add("dataTimeStamp", ntField.createTimeStamp()).
            addArray("attribute", getAttributeStructure());

        if (descriptor)
            builder.add("descriptor", ScalarType.pvString);

        if (alarm)
            builder.add("alarm", ntField.createAlarm());

        if (timeStamp)
            builder.add("timeStamp", ntField.createTimeStamp());

        if (display)
            builder.add("display", ntField.createTimeStamp());

        int extraCount = extraFieldNames.size();
        for (int i = 0; i < extraCount; i++)
            builder.add(extraFieldNames.get(i), extraFields.get(i));

        reset();

        return builder.createStructure();
}


    /**
     * Create a <b>PVStructure</b> that represents NTNDArray.
     * This resets this instance state and allows new instance to be created.
     * @return a new instance of a <b>PVStructure</b>.
     */
    public PVStructure createPVStructure()
    {
        return PVDataFactory.getPVDataCreate().createPVStructure(createStructure());
    }

    /**
     * Create a <b>NTNDArray</b> instance.
     * This resets this instance state and allows new instance to be created.
     * 
     * @return a new instance of a <b>NTNDArray</b>
     */
    public NTNDArray create()
    {
        return new NTNDArray(createPVStructure());
    }
    /**
     * Add extra <b>Field</b> to the type.
     * 
     * @param name name of the field
     * @param field a field to add
     * @return this instance of <b>NTNDArrayBuilder</b>
     */
    public NTNDArrayBuilder add(String name, Field field) 
    {
        extraFields.add(field);
        extraFieldNames.add(name);
        return this;
    }

    NTNDArrayBuilder() {}

    private void reset()
    {
        descriptor = false;
        alarm = false;
        timeStamp = false;
        display = false;
        extraFieldNames.clear();
        extraFields.clear();
    }

    static Union getValueType()
    {
        if (valueType == null)
        {
            for (ScalarType st : ScalarType.values())
            {
                if (st != ScalarType.pvString)
                    builder.addArray(st.toString() + "Value", st);
            }
            valueType = builder.createUnion();                
        }
        return valueType;
    }

    static Structure getCodecStructure()
    {
        if (codecStruc == null)
        {
            codecStruc = builder.setId("codec_t").
                add("name", ScalarType.pvString).
                add("parameters", fieldCreate.createVariantUnion()).
                createStructure();
        }
        return codecStruc;
    }

    static Structure getDimensionStructure()
    {
        if (dimensionStruc == null)
        {
            dimensionStruc = builder.setId("dimension_t").
                add("size", ScalarType.pvInt).
                add("offset",  ScalarType.pvInt).
                add("fullSize",  ScalarType.pvInt).
                add("binning",  ScalarType.pvInt).
                add("reverse",  ScalarType.pvBoolean).
                createStructure();
        }
        return dimensionStruc;
    }

    static Structure getAttributeStructure()
    {
        if (attributeStruc == null)
        {
            attributeStruc = builder.setId(NTNDArray.NTAttributeURI).
		        add("name", ScalarType.pvString).
			    add("value", fieldCreate.createVariantUnion()).
			    add("descriptor", ScalarType.pvString).
                add("sourceType", ScalarType.pvInt).
                add("source", ScalarType.pvString).
               createStructure();
        }
        return attributeStruc;
    }

    private static FieldCreate fieldCreate = FieldFactory.getFieldCreate();

    private static FieldBuilder builder = fieldCreate.createFieldBuilder();

    private static Union valueType;
    private static Structure codecStruc;
    private static Structure dimensionStruc;
    private static Structure attributeStruc;

    private boolean descriptor;
    private boolean timeStamp;
    private boolean alarm;
    private boolean display;

    // NOTE: this preserves order, however it does not handle duplicates
    private ArrayList<String> extraFieldNames = new ArrayList<String>();
    private ArrayList<Field> extraFields = new ArrayList<Field>();

}


