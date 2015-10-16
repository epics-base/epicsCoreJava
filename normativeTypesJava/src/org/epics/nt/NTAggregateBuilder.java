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
 * <p>
 * One instance can be used to create multiple instances.
 * An instance of this object must not be used concurrently (an object has a state).
 * @author dgh
 */
public class NTAggregateBuilder
{
    /**
     * Adds dispersion field to the NTAggregate.
     *
     * @return this instance of NTAggregateBuilder
     */
    public NTAggregateBuilder addDispersion()
    {
        dispersion = true;
        return this;
    }

    /**
     * Adds first field to the NTAggregate.
     *
     * @return this instance of NTAggregateBuilder
     */
    public NTAggregateBuilder addFirst()
    {
        first = true;
        return this;
    }

    /**
     * Adds firstTimeStamp field to the NTAggregate.
     *
     * @return this instance of NTAggregateBuilder
     */
    public NTAggregateBuilder addFirstTimeStamp()
    {
        firstTimeStamp = true;
        return this;
    }

    /**
     * Adds last field to the NTAggregate.
     *
     * @return this instance of NTAggregateBuilder
     */
    public NTAggregateBuilder addLast()
    {
        last = true;
        return this;
    }

    /**
     * Adds lastTimeStamp field to the NTAggregate.
     *
     * @return this instance of NTAggregateBuilder
     */
    public NTAggregateBuilder addLastTimeStamp()
    {
        lastTimeStamp = true;
        return this;
    }

    /**
     * Adds max field to the NTAggregate.
     *
     * @return this instance of NTAggregateBuilder
     */
    public NTAggregateBuilder addMax()
    {
        max = true;
        return this;
    }

    /**
     * Adds min field to the NTAggregate.
     *
     * @return this instance of NTAggregateBuilder
     */
    public NTAggregateBuilder addMin()
    {
        min = true;
        return this;
    }

    /**
     * Adds descriptor field to the NTAggregate.
     *
     * @return this instance of NTAggregateBuilder
     */
    public NTAggregateBuilder addDescriptor()
    {
        descriptor = true;
        return this;
    }

    /**
     * Adds alarm field to the NTAggregate.
     *
     * @return this instance of NTAggregateBuilder
     */
    public NTAggregateBuilder addAlarm()
    {
        alarm = true;
        return this;
    }

    /**
     * Adds timeStamp field to the NTAggregate.
     *
     * @return this instance of NTAggregateBuilder
     */
    public NTAggregateBuilder addTimeStamp()
    {
        timeStamp = true;
        return this;
    }

    /**
     * Creates a Structure that represents NTAggregate.
     * This resets this instance state and allows a new instance to be created.
     *
     * @return a new instance of a Structure
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
     * Creates a PVStructure that represents NTAggregate.
     * This resets this instance state and allows a new instance to be created.
     *
     * @return a new instance of a PVStructure
     */
    public PVStructure createPVStructure()
    {
        return PVDataFactory.getPVDataCreate().createPVStructure(createStructure());
    }

    /**
     * Creates an NTAggregate instance.
     * This resets this instance state and allows a new instance to be created.
     *
     * @return a new instance of an NTAggregate
     */
    public NTAggregate create()
    {
        return new NTAggregate(createPVStructure());
    }

    /**
     * Adds extra Field to the type.
     *
     * @param name the name of the field
     * @param field the field to add
     * @return this instance of NTAggregateBuilder
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

