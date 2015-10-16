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
     * Adds tags field to the NTAttribute.
     *
     * @return this instance of NTAttributeBuilder
     */
    public NTAttributeBuilder addTags()
    {
        tags = true;
        return this;
    }

    /**
     * Adds descriptor field to the NTAttribute.
     *
     * @return this instance of NTAttributeBuilder
     */
    public NTAttributeBuilder addDescriptor()
    {
        descriptor = true;
        return this;
    }

    /**
     * Adds alarm field to the NTAttribute.
     *
     * @return this instance of NTAttributeBuilder
     */
    public NTAttributeBuilder addAlarm()
    {
        alarm = true;
        return this;
    }

    /**
     * Adds timeStamp field to the NTAttribute.
     *
     * @return this instance of NTAttributeBuilder
     */
    public NTAttributeBuilder addTimeStamp()
    {
        timeStamp = true;
        return this;
    }

    /**
     * Creates a Structure that represents NTAttribute.
     * This resets this instance state and allows new instance to be created.
     *
     * @return a new instance of a Structure
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
     * Creates a PVStructure that represents NTAttribute.
     * This resets this instance state and allows new instance to be created.
     *
     * @return a new instance of a PVStructure
     */
    public PVStructure createPVStructure()
    {
        return PVDataFactory.getPVDataCreate().createPVStructure(createStructure());
    }

    /**
     * Creates an NTAttribute instance.
     * This resets this instance state and allows new instance to be created.
     *
     * @return a new instance of an NTAttribute
     */
    public NTAttribute create()
    {
        return new NTAttribute(createPVStructure());
    }

    /**
     * Adds extra Field to the type.
     *
     * @param name name of the field
     * @param field a field to add
     * @return this instance of NTAttributeBuilder
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

