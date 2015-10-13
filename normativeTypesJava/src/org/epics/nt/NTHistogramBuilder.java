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
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;

import java.util.ArrayList;

/**
 * Interface for in-line creating of NTHistogram.
 *
 * One instance can be used to create multiple instances.
 * An instance of this object must not be used concurrently (an object has a state).
 * @author dgh
 */
public class NTHistogramBuilder
{
    /**
     * Set the type of the value field of an NTHistogram.
     *
     * @param scalarType the scalar type of the value field
     * @return this instance of <b>NTHistogramBuilder</b>
     */
    public NTHistogramBuilder value(ScalarType scalarType)
    {
        valueType = scalarType;
        valueTypeSet = true;
        return this;
    }

    /**
     * Add descriptor field to the NTHistogram.
     *
     * @return this instance of <b>NTHistogramBuilder</b>
     */
    public NTHistogramBuilder addDescriptor()
    {
        descriptor = true;
        return this;
    }

    /**
     * Add alarm structure to the NTHistogram.
     *
     * @return this instance of <b>NTHistogramBuilder</b>
     */
    public NTHistogramBuilder addAlarm()
    {
        alarm = true;
        return this;
    }

    /**
     * Add timeStamp structure to the NTHistogram.
     *
     * @return this instance of <b>NTHistogramBuilder</b>
     */
    public NTHistogramBuilder addTimeStamp()
    {
        timeStamp = true;
        return this;
    }

    /**
     * Create a <b>Structure</b> that represents NTHistogram.
     * This resets this instance state and allows new instance to be created.
     *
     * @return a new instance of a <b>Structure</b>
     */
    public Structure createStructure()
    {
        if (!valueTypeSet)
            throw new RuntimeException("value type not set");

        FieldBuilder builder =
            FieldFactory.getFieldCreate().createFieldBuilder().
               setId(NTHistogram.URI).
               addArray("ranges", ScalarType.pvDouble).
               addArray("value", valueType);

        NTField ntField = NTField.get();

        if (descriptor)
            builder.add("descriptor", ScalarType.pvString);

        if (alarm)
            builder.add("alarm", ntField.createAlarm());

        if (timeStamp)
            builder.add("timeStamp", ntField.createTimeStamp());

        int extraCount = extraFieldNames.size();
        for (int i = 0; i< extraCount; i++)
            builder.add(extraFieldNames.get(i), extraFields.get(i));

        Structure s = builder.createStructure();

        reset();
        return s;
    }

    /**
     * Create a <b>PVStructure</b> that represents NTHistogram.
     * This resets this instance state and allows new instance to be created.
     *
     * @return a new instance of a <b>PVStructure</b>
     */
    public PVStructure createPVStructure()
    {
        return PVDataFactory.getPVDataCreate().createPVStructure(createStructure());
    }

    /**
     * Create a <b>NTHistogram</b> instance.
     * This resets this instance state and allows new instance to be created.
     *
     * @return a new instance of a <b>NTHistogram</b>
     */
    public NTHistogram create()
    {
        return new NTHistogram(createPVStructure());
    }

    /**
     * Add extra <b>Field</b> to the type.
     *
     * @param name the name of the field
     * @param field the field to add
     * @return this instance of <b>NTHistogramBuilder</b>
     */
    public NTHistogramBuilder add(String name, Field field) 
    {
        extraFields.add(field);
        extraFieldNames.add(name);
        return this;
    }

    NTHistogramBuilder()
    {
        reset();
    }

    private void reset()
    {
        valueTypeSet = false;
        descriptor = false;
        alarm = false;
        timeStamp = false;
        extraFieldNames.clear();
        extraFields.clear();
    }
    private boolean valueTypeSet;
    private ScalarType valueType;

    private boolean descriptor;
    private boolean alarm;
    private boolean timeStamp;

    // NOTE: this preserves order, however it does not handle duplicates
    private ArrayList<String> extraFieldNames = new ArrayList<String>();
    private ArrayList<Field> extraFields = new ArrayList<Field>();
}

