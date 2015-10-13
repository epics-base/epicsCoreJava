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
 * Interface for in-line creating of NTAggregate.
 *
 * One instance can be used to create multiple instances.
 * An instance of this object must not be used concurrently (an object has a state).
 * @author dgh
 */
public class NTAggregateBuilder
{
    /**
     * Add dispersion field to the NTAggregate.
     *
     * @return this instance of <b>NTAggregateBuilder</b>
     */
    public NTAggregateBuilder addDispersion()
    {
        dispersion = true;
        return this;
    }

    /**
     * Add first field to the NTAggregate.
     *
     * @return this instance of <b>NTAggregateBuilder</b>
     */
    public NTAggregateBuilder addFirst()
    {
        first = true;
        return this;
    }

    /**
     * Add firstTimeStamp field to the NTAggregate.
     *
     * @return this instance of <b>NTAggregateBuilder</b>
     */
    public NTAggregateBuilder addFirstTimeStamp()
    {
        firstTimeStamp = true;
        return this;
    }

    /**
     * Add last field to the NTAggregate.
     *
     * @return this instance of <b>NTAggregateBuilder</b>
     */
    public NTAggregateBuilder addLast()
    {
        last = true;
        return this;
    }

    /**
     * Add lastTimeStamp field to the NTAggregate.
     *
     * @return this instance of <b>NTAggregateBuilder</b>
     */
    public NTAggregateBuilder addLastTimeStamp()
    {
        lastTimeStamp = true;
        return this;
    }

    /**
     * Add max field to the NTAggregate.
     *
     * @return this instance of <b>NTAggregateBuilder</b>
     */
    public NTAggregateBuilder addMax()
    {
        max = true;
        return this;
    }

    /**
     * Add min field to the NTAggregate.
     *
     * @return this instance of <b>NTAggregateBuilder</b>
     */
    public NTAggregateBuilder addMin()
    {
        min = true;
        return this;
    }

    /**
     * Add descriptor field to the NTAggregate.
     *
     * @return this instance of <b>NTAggregateBuilder</b>
     */
    public NTAggregateBuilder addDescriptor()
    {
        descriptor = true;
        return this;
    }

    /**
     * Add alarm structure to the NTAggregate.
     *
     * @return this instance of <b>NTAggregateBuilder</b>
     */
    public NTAggregateBuilder addAlarm()
    {
        alarm = true;
        return this;
    }

    /**
     * Add timeStamp structure to the NTAggregate.
     *
     * @return this instance of <b>NTAggregateBuilder</b>
     */
    public NTAggregateBuilder addTimeStamp()
    {
        timeStamp = true;
        return this;
    }

    /**
     * Create a <b>Structure</b> that represents NTAggregate.
     * This resets this instance state and allows new instance to be created.
     *
     * @return a new instance of a <b>Structure</b>
     */
    public Structure createStructure()
    {
        FieldBuilder builder =
            FieldFactory.getFieldCreate().createFieldBuilder().
               setId(NTAggregate.URI).
               add("value", ScalarType.pvDouble).
               add("N", ScalarType.pvLong);

        NTField ntField = NTField.get();

        if (dispersion)
            builder.add("dispersion", ScalarType.pvDouble);

        if (first)
            builder.add("first", ScalarType.pvDouble);

        if (firstTimeStamp)
            builder.add("firstTimeStamp", ntField.createTimeStamp());

        if (last)
            builder.add("last", ScalarType.pvDouble);

        if (lastTimeStamp) 
            builder.add("lastTimeStamp",ntField.createTimeStamp()); 

        if (max)
            builder.add("max",  ScalarType.pvDouble);

        if (min)
            builder.add("min",  ScalarType.pvDouble);

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
     * Create a <b>PVStructure</b> that represents NTAggregate.
     * This resets this instance state and allows new instance to be created.
     *
     * @return a new instance of a <b>PVStructure</b>
     */
    public PVStructure createPVStructure()
    {
        return PVDataFactory.getPVDataCreate().createPVStructure(createStructure());
    }

    /**
     * Create an <b>NTAggregate</b> instance.
     * This resets this instance state and allows new instance to be created.
     *
     * @return a new instance of an <b>NTAggregate</b>
     */
    public NTAggregate create()
    {
        return new NTAggregate(createPVStructure());
    }

    /**
     * Add extra <b>Field</b> to the type.
     *
     * @param name the name of the field
     * @param field the field to add
     * @return this instance of <b>NTAggregateBuilder</b>
     */
    public NTAggregateBuilder add(String name, Field field) 
    {
        extraFields.add(field);
        extraFieldNames.add(name);
        return this;
    }

    NTAggregateBuilder()
    {
        reset();
    }

    private void reset()
    {
        dispersion = false;
        first = false;
        firstTimeStamp = false;
        last = false;
        lastTimeStamp = false;
        max = false;
        min = false;

        descriptor = false;
        alarm = false;
        timeStamp = false;

        extraFieldNames.clear();
        extraFields.clear();
    }

    private boolean dispersion;
    private boolean first;
    private boolean firstTimeStamp;
    private boolean last;
    private boolean lastTimeStamp;
    private boolean max;
    private boolean min;

    private boolean descriptor;
    private boolean alarm;
    private boolean timeStamp;

    // NOTE: this preserves order, however it does not handle duplicates
    private ArrayList<String> extraFieldNames = new ArrayList<String>();
    private ArrayList<Field> extraFields = new ArrayList<Field>();
}

