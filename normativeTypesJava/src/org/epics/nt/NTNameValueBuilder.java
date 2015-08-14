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
import org.epics.pvdata.pv.PVStringArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;

import java.util.ArrayList;

/**
 * Interface for in-line creating of NTNameValue.
 *
 * One instance can be used to create multiple instances.
 * An instance of this object must not be used concurrently (an object has a state).
 * @author dgh
 */
public class NTNameValueBuilder
{
    /**
     * Set a value type of a NTNameValue.
     * @param scalarType the value type.
     * @return this instance of <b>NTNameValueBuilder</b>.</b>.
     */
    public NTNameValueBuilder value(ScalarType scalarType)
    {
        valueType = scalarType;
        valueTypeSet = true;
        return this;
    }

    /**
     * Add descriptor field to the NTNameValue.
     * @return this instance of <b>NTNameValueBuilder</b>.</b>.
     */
    public NTNameValueBuilder addDescriptor()
    {
        descriptor = true;
        return this;
    }

    /**
     * Add alarm structure to the NTNameValue.
     * @return this instance of <b>NTNameValueBuilder</b>.</b>.
     */
    public NTNameValueBuilder addAlarm()
    {
        alarm = true;
        return this;
    }

    /**
     * Add timeStamp structure to the NTNameValue.
     * @return this instance of <b>NTNameValueBuilder</b>.</b>.
     */
    public NTNameValueBuilder addTimeStamp()
    {
        timeStamp = true;
        return this;
    }

    /**
     * Create a <b>Structure</b> that represents NTNameValue.
     * This resets this instance state and allows new instance to be created.
     * @return a new instance of a <b>Structure</b>.
     */
    public Structure createStructure()
    {
        if (!valueTypeSet)
            throw new RuntimeException("value type not set");

        FieldBuilder builder =
            FieldFactory.getFieldCreate().createFieldBuilder().
               setId(NTNameValue.URI).
               addArray("value", valueType).
               addArray("name", ScalarType.pvString);

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
     * Create a <b>PVStructure</b> that represents NTNameValue.
     * This resets this instance state and allows new instance to be created.
     * @return a new instance of a <b>PVStructure</b>.
     */
    public PVStructure createPVStructure()
    {
        return PVDataFactory.getPVDataCreate().createPVStructure(createStructure());
    }

    /**
     * Create a <b>NTNameValue</b> instance.
     * This resets this instance state and allows new instance to be created.
     * @return a new instance of a <b>NTNameValue</b>.
     */
    public NTNameValue create()
    {
        return new NTNameValue(createPVStructure());
    }

    /**
     * Add extra <b>Field</b> to the type.
     * @param name name of the field.
     * @param field a field to add.
     * @return this instance of <b>NTNameValueBuilder</b>.</b>.
     */
    public NTNameValueBuilder add(String name, Field field) 
    {
        extraFields.add(field);
        extraFieldNames.add(name);
        return this;
    }


    NTNameValueBuilder()
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

