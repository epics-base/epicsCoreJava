/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
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

/**
 * Interface for in-line creating of NTMultiChannel.
 *
 * One instance can be used to create multiple instances.
 * An instance of this object must not be used concurrently (an object has a state).
 * @author dgh
 */
public class NTMultiChannelBuilder
{
    /**
     * Specify the union for the value field.
     * If this is not called then a variant union is the default.
     *
     * @param value the introspection object for the union value field
     * @return this instance of  NTMultiChannelBuilder
     */
    public NTMultiChannelBuilder value(Union value)
    {
        valueType = value;
        return this;
    }

    /**
     * Adds descriptor field to the NTMultiChannel.
     *
     * @return this instance of NTMultiChannelBuilder
     */
    public NTMultiChannelBuilder addDescriptor()
    {
        descriptor = true;
        return this;
    }

    /**
     * Adds alarm field to the NTMultiChannel.
     *
     * @return this instance of NTMultiChannelBuilder
     */
    public NTMultiChannelBuilder addAlarm()
    {
        alarm = true;
        return this;
    }

    /**
     * Adds timeStamp field to the NTMultiChannel.
     *
     * @return this instance of NTMultiChannelBuilder
     */
    public NTMultiChannelBuilder addTimeStamp()
    {
        timeStamp = true;
        return this;
    }

    /**
     * Adds severity array to the NTMultiChannel.
     *
     * @return this instance of NTMultiChannelBuilder
     */
    public NTMultiChannelBuilder addSeverity()
    {
        severity = true;
        return this;
    }

    /**
     * Adds status array to the NTMultiChannel.
     *
     * @return this instance of NTMultiChannelBuilder
     */
    public NTMultiChannelBuilder addStatus()
    {
       status = true;
        return this;
    }

    /**
     * Adds message array to the NTMultiChannel.
     *
     * @return this instance of NTMultiChannelBuilder
     */
    public NTMultiChannelBuilder addMessage()
    {
        message = true;
        return this;
    }

    /**
     * Adds secondsPastEpoch array to the NTMultiChannel.
     *
     * @return this instance of NTMultiChannelBuilder
     */
    public NTMultiChannelBuilder addSecondsPastEpoch()
    {
        secondsPastEpoch = true;
        return this;
    }

    /**
     * Adds nanoseconds array to the NTMultiChannel.
     *
     * @return this instance of NTMultiChannelBuilder
     */
    public NTMultiChannelBuilder addNanoseconds()
    {
        nanoseconds = true;
        return this;
    }

    /**
     * Adds userTag array to the NTMultiChannel.
     *
     * @return this instance of NTMultiChannelBuilder
     */
    public NTMultiChannelBuilder addUserTag()
    {
        userTag = true;
        return this;
    }

    /**
     * Adds isConnected array to the NTMultiChannel.
     *
     * @return this instance of NTMultiChannelBuilder
     */
    public NTMultiChannelBuilder addIsConnected()
    {
        isConnected = true;
        return this;
    }

    /**
     * Creates a Structure that represents NTMultiChannel.
     * This resets this instance state and allows new instance to be created.
     *
     * @return a new instance of a Structure
     */
    public Structure createStructure()
    {
        int nfields = 2;
        int extraCount = extraFieldNames.size();
        nfields += extraCount;
        if(descriptor) ++nfields;
        if(alarm) ++nfields;
        if(timeStamp) ++nfields;
        if(severity) ++nfields;
        if(status) ++nfields;
        if(message) ++nfields;
        if(secondsPastEpoch) ++nfields;
        if(nanoseconds) ++nfields;
        if(userTag) ++nfields;
        if(isConnected) ++nfields;
        Field[] fields = new Field[nfields];
        String[] names = new String[nfields];
        int ind = 0;
        names[ind] = "value";
        NTField ntField = NTField.get();
        FieldCreate fieldCreate = FieldFactory.getFieldCreate();
        if(valueType != null) {
            fields[ind++] =  fieldCreate.createUnionArray(valueType);
        } else {
            fields[ind++] =  fieldCreate.createVariantUnionArray();
        }
        names[ind] = "channelName";
        fields[ind++] =  fieldCreate.createScalarArray(ScalarType.pvString);
        if(descriptor) {
            names[ind] = "descriptor";
            fields[ind++] = fieldCreate.createScalar(ScalarType.pvString);
        }
        if(alarm) {
            names[ind] = "alarm";
            fields[ind++] = ntField.createAlarm();
        }
        if(timeStamp) {
            names[ind] = "timeStamp";
            fields[ind++] = ntField.createTimeStamp();
        }
        if(severity) {
            names[ind] = "severity";
            fields[ind++] = fieldCreate.createScalarArray(ScalarType.pvInt);
        }
        if(status) {
            names[ind] = "status";
            fields[ind++] = fieldCreate.createScalarArray(ScalarType.pvInt);
        }
        if(message) {
            names[ind] = "message";
            fields[ind++] = fieldCreate.createScalarArray(ScalarType.pvString);
        }
        if(secondsPastEpoch) {
            names[ind] = "secondsPastEpoch";
            fields[ind++] = fieldCreate.createScalarArray(ScalarType.pvLong);
        }
        if(nanoseconds) {
            names[ind] = "nanoseconds";
            fields[ind++] = fieldCreate.createScalarArray(ScalarType.pvInt);
        }
        if(userTag) {
            names[ind] = "userTag";
            fields[ind++] = fieldCreate.createScalarArray(ScalarType.pvInt);
        }
        if(isConnected) {
            names[ind] = "isConnected";
            fields[ind++] = fieldCreate.createScalarArray(ScalarType.pvBoolean);
        }
        for (int i = 0; i< extraCount; i++) {
            names[ind] = extraFieldNames.get(i);
            fields[ind++] = extraFields.get(i);
        }

        Structure st = fieldCreate.createStructure(NTMultiChannel.URI,names,fields);
        reset();
        return st;
    }

    /**
     * Creates a PVStructure that represents NTMultiChannel.
     * This resets this instance state and allows new instance to be created.
     *
     * @return a new instance of a PVStructure
     */
    public PVStructure createPVStructure()
    {
        return PVDataFactory.getPVDataCreate().createPVStructure(createStructure());
    }

    /**
     * Creates an NTMultiChannel instance.
     * This resets this instance state and allows new instance to be created.
     *
     * @return a new instance of an NTMultiChannel
     */
    public NTMultiChannel create()
    {
        return new NTMultiChannel(createPVStructure());
    }

    /**
     * Adds extra Field to the type.
     *
     * @param name name of the field
     * @param field a field to add
     * @return this instance of an NTMultiChannelBuilder
     */
    public NTMultiChannelBuilder add(String name, Field field)
    {
        extraFields.add(field);
        extraFieldNames.add(name);
        return this;
    }

    NTMultiChannelBuilder()
    {
        reset();
    }

    private void reset()
    {
        valueType = null;
        descriptor = false;
        alarm = false;
        timeStamp = false;
        severity = false;
        status = false;
        message = false;
        secondsPastEpoch = false;
        nanoseconds = false;
        userTag = false;
        isConnected = false;
        extraFieldNames.clear();
        extraFields.clear();
    }

    private Union valueType;
    private boolean descriptor;
    private boolean alarm;
    private boolean timeStamp;
    private boolean severity;
    private boolean status;
    private boolean message;
    private boolean secondsPastEpoch;
    private boolean nanoseconds;
    private boolean userTag;
    private boolean isConnected;

    // NOTE: this preserves order, however it does not handle duplicates
    private ArrayList<String> extraFieldNames = new ArrayList<String>();
    private ArrayList<Field> extraFields = new ArrayList<Field>();
}


