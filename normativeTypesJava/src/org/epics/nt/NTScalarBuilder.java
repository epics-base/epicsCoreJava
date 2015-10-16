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
     * Sets the value type of an NTScalar.
     *
     * @param scalarType the ScalarType of the value field
     * @return this instance of NTScalarBuilder.
     */
    public NTScalarBuilder value(ScalarType scalarType)
    {
        valueType = scalarType;
        valueTypeSet = true;
        return this;
    }

    /**
     * Adds descriptor field to the NTScalar.
     * @return this instance of NTScalarBuilder.
     */
    public NTScalarBuilder addDescriptor()
    {
        descriptor = true;
        return this;
    }

    /**
     * Adds alarm structure to the NTScalar.
     * @return this instance of NTScalarBuilder.
     */
    public NTScalarBuilder addAlarm()
    {
        alarm = true;
        return this;
    }

    /**
     * Adds timeStamp field to the NTScalar.
     * @return this instance of NTScalarBuilder.
     */
    public NTScalarBuilder addTimeStamp()
    {
        timeStamp = true;
        return this;
    }

    /**
     * Adds display field to the NTScalar.
     * @return this instance of NTScalarBuilder.
     */
    public NTScalarBuilder addDisplay()
    {
        display = true;
        return this;
    }

    /**
     * Adds control field to the NTScalar.
     * @return this instance of NTScalarBuilder.
     */
    public NTScalarBuilder addControl()
    {
        control = true;
        return this;
    }

    /**
     * Creates a Structure that represents NTScalar.
     * This resets this instance state and allows new instance to be created.
     * @return a new instance of a Structure.
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
     * Creates a PVStructure that represents NTScalar.
     * This resets this instance state and allows new instance to be created.
     * @return a new instance of a PVStructure.
     */
    public PVStructure createPVStructure()
    {
        return PVDataFactory.getPVDataCreate().createPVStructure(createStructure());
    }

    /**
     * Creates an NTScalar instance.
     * This resets this instance state and allows new instance to be created.
     * @return a new instance of an NTScalar.
     */
    public NTScalar create()
    {
        return new NTScalar(createPVStructure());
    }

    /**
     * Adds extra Field to the type.
     * @param name name of the field.
     * @param field a field to add.
     * @return this instance of NTScalarBuilder.
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

