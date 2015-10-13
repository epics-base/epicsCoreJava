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
 * Interface for in-line creating of NTAttribute.
 * 
 * One instance can be used to create multiple instances.
 * An instance of this object must not be used concurrently (an object has a state).
 * @author dgh
 */
public class NTAttributeBuilder
{
    /**
     * Add tags field to the NTAttribute.
     *
     * @return this instance of <b>NTAttributeBuilder</b>
     */
    public NTAttributeBuilder addTags()
    {
        tags = true;
        return this;
    }

    /**
     * Add descriptor field to the NTAttribute.
     *
     * @return this instance of <b>NTAttributeBuilder</b>
     */
    public NTAttributeBuilder addDescriptor()
    {
        descriptor = true;
        return this;
    }

    /**
     * Add alarm structure to the NTAttribute.
     *
     * @return this instance of <b>NTAttributeBuilder</b>
     */
    public NTAttributeBuilder addAlarm()
    {
        alarm = true;
        return this;
    }

    /**
     * Add timeStamp structure to the NTAttribute.
     *
     * @return this instance of <b>NTAttributeBuilder</b>
     */
    public NTAttributeBuilder addTimeStamp()
    {
        timeStamp = true;
        return this;
    }

    /**
     * Create a <b>Structure</b> that represents NTAttribute.
     * This resets this instance state and allows new instance to be created.
     *
     * @return a new instance of a <b>Structure</b>
     */
    public Structure createStructure()
    {
        NTField ntField = NTField.get();
        FieldCreate fieldCreate = FieldFactory.getFieldCreate();

        FieldBuilder builder = fieldCreate.createFieldBuilder().
               setId(NTAttribute.URI).
               add("name", ScalarType.pvString).
               add("value", fieldCreate.createVariantUnion());

        if (tags)
            builder.addArray("tags", ScalarType.pvString);

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
     * Create a <b>PVStructure</b> that represents NTAttribute.
     * This resets this instance state and allows new instance to be created.
     *
     * @return a new instance of a <b>PVStructure</b>
     */
    public PVStructure createPVStructure()
    {
        return PVDataFactory.getPVDataCreate().createPVStructure(createStructure());
    }

    /**
     * Create an <b>NTAttribute</b> instance.
     * This resets this instance state and allows new instance to be created.
     *
     * @return a new instance of an <b>NTAttribute</b>
     */
    public NTAttribute create()
    {
        return new NTAttribute(createPVStructure());
    }

    /**
     * Add extra <b>Field</b> to the type.
     *
     * @param name name of the field
     * @param field a field to add
     * @return this instance of <b>NTAttributeBuilder</b>
     */
    public NTAttributeBuilder add(String name, Field field) 
    {
        extraFields.add(field);
        extraFieldNames.add(name);
        return this;
    }

    NTAttributeBuilder()
    {
        reset();
    }

    void reset()
    {
        tags = false;
        descriptor = false;
        alarm = false;
        timeStamp = false;
        extraFieldNames.clear();
        extraFields.clear();
    }

    boolean tags;
    boolean descriptor;
    boolean alarm;
    boolean timeStamp;

    // NOTE: this preserves order, however it does not handle duplicates
    ArrayList<String> extraFieldNames = new ArrayList<String>();
    ArrayList<Field> extraFields = new ArrayList<Field>();
}

