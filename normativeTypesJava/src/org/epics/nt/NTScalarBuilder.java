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
 * Interface for in-line creating of NTScalar.
 *
 * One instance can be used to create multiple instances.
 * An instance of this object must not be used concurrently (an object has a state).
 * @author dgh
 */
public class NTScalarBuilder
{
    /**
     * Set a value type of an NTScalar.
     * @param scalarType the value type.
     * @return this instance of <b>NTScalarBuilder</b>.
     */
    public NTScalarBuilder value(ScalarType scalarType)
    {
        valueType = scalarType;
        valueTypeSet = true;
        return this;
    }

    /**
     * Add descriptor field to the NTScalar.
     * @return this instance of <b>NTScalarBuilder</b>.
     */
    public NTScalarBuilder addDescriptor()
    {
        descriptor = true;
        return this;
    }

    /**
     * Add alarm structure to the NTScalar.
     * @return this instance of <b>NTScalarBuilder</b>.
     */
    public NTScalarBuilder addAlarm()
    {
        alarm = true;
        return this;
    }

    /**
     * Add timeStamp structure to the NTScalar.
     * @return this instance of <b>NTScalarBuilder</b>.
     */
    public NTScalarBuilder addTimeStamp()
    {
        timeStamp = true;
        return this;
    }

    /**
     * Add display structure to the NTScalar.
     * @return this instance of <b>NTScalarBuilder</b>.
     */
    public NTScalarBuilder addDisplay()
    {
        display = true;
        return this;
    }

    /**
     * Add control structure to the NTScalar.
     * @return this instance of <b>NTScalarBuilder</b>.
     */
    public NTScalarBuilder addControl()
    {
        control = true;
        return this;
    }

    /**
     * Create a <b>Structure</b> that represents NTScalar.
     * This resets this instance state and allows new instance to be created.
     * @return a new instance of a <b>Structure</b>.
     */
    public Structure createStructure()
    {
        if (!valueTypeSet)
            throw new RuntimeException("value type not set");

        FieldBuilder builder =
            FieldFactory.getFieldCreate().createFieldBuilder().
               setId(NTScalar.URI).
               add("value", valueType);

        NTField ntField = NTField.get();

        if (descriptor)
            builder.add("descriptor", ScalarType.pvString);

        if (alarm)
            builder.add("alarm", ntField.createAlarm());

        if (timeStamp)
            builder.add("timeStamp", ntField.createTimeStamp());

        if (display)
            builder.add("display", ntField.createDisplay());

        if (control)
            builder.add("control", ntField.createControl());

        int extraCount = extraFieldNames.size();
        for (int i = 0; i< extraCount; i++)
            builder.add(extraFieldNames.get(i), extraFields.get(i));

        Structure s = builder.createStructure();

        reset();
        return s;
    }

    /**
     * Create a <b>PVStructure</b> that represents NTScalar.
     * This resets this instance state and allows new instance to be created.
     * @return a new instance of a <b>PVStructure</b>.
     */
    public PVStructure createPVStructure()
    {
        return PVDataFactory.getPVDataCreate().createPVStructure(createStructure());
    }

    /**
     * Create an <b>NTScalar</b> instance.
     * This resets this instance state and allows new instance to be created.
     * @return a new instance of an <b>NTScalar</b>.
     */
    public NTScalar create()
    {
        return new NTScalar(createPVStructure());
    }

    /**
     * Add extra <b>Field</b> to the type.
     * @param name name of the field.
     * @param field a field to add.
     * @return this instance of <b>NTScalarBuilder</b>.
     */
    public NTScalarBuilder add(String name, Field field) 
    {
        extraFields.add(field);
        extraFieldNames.add(name);
        return this;
    }


    NTScalarBuilder()
    {
        reset();
    }

    private void reset()
    {
        valueTypeSet = false;
        descriptor = false;
        alarm = false;
        timeStamp = false;
        display = false;
        control = false;
        extraFieldNames.clear();
        extraFields.clear();
    }

    private boolean valueTypeSet;
    private ScalarType valueType;

    private boolean descriptor;
    private boolean alarm;
    private boolean timeStamp;
    private boolean display;
    private boolean control;

    // NOTE: this preserves order, however it does not handle duplicates
    private ArrayList<String> extraFieldNames = new ArrayList<String>();
    private ArrayList<Field> extraFields = new ArrayList<Field>();
}

