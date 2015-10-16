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

/**
 * Interface for in-line creating of NTScalarMultiChannel.
 *
 * One instance can be used to create multiple instances.
 * An instance of this object must not be used concurrently (an object has a state).
 * @author dgh
 */
public class NTScalarMultiChannelBuilder
{
    /**
     * Sets the scalar type of the value field elements
     * If this is not called then a variantUnion is the default.
     *
     * @param scalarType the scalar type of the value field elements
     * @return this instance of  NTScalarMultiChannelBuilder
     */
    public NTScalarMultiChannelBuilder value(ScalarType scalarType)
    {
        valueType = scalarType;
        return this;
    }

    /**
     * Adds descriptor field to the NTScalarMultiChannel.
     *
     * @return this instance of NTScalarMultiChannelBuilder
     */
    public NTScalarMultiChannelBuilder addDescriptor()
    {
        descriptor = true;
        return this;
    }

    /**
     * Adds alarm structure to the NTScalarMultiChannel.
     *
     * @return this instance of NTScalarMultiChannelBuilder
     */
    public NTScalarMultiChannelBuilder addAlarm()
    {
        alarm = true;
        return this;
    }

    /**
     * Adds timeStamp field to the NTScalarMultiChannel.
     *
     * @return this instance of NTScalarMultiChannelBuilder
     */
    public NTScalarMultiChannelBuilder addTimeStamp()
    {
        timeStamp = true;
        return this;
    }

    /**
     * Adds severity array to the NTScalarMultiChannel.
     *
     * @return this instance of NTScalarMultiChannelBuilder
     */
    public NTScalarMultiChannelBuilder addSeverity()
    {
        severity = true;
        return this;
    }

    /**
     * Adds status array to the NTScalarMultiChannel.
     *
     * @return this instance of NTScalarMultiChannelBuilder
     */
    public NTScalarMultiChannelBuilder addStatus()
    {
        status = true;
        return this;
    }

    /**
     * Adds message array to the NTScalarMultiChannel.
     *
     * @return this instance of NTScalarMultiChannelBuilder
     */
    public NTScalarMultiChannelBuilder addMessage()
    {
        message = true;
        return this;
    }

    /**
     * Adds secondsPastEpoch array to the NTScalarMultiChannel.
     *
     * @return this instance of NTScalarMultiChannelBuilder
     */
    public NTScalarMultiChannelBuilder addSecondsPastEpoch()
    {
        secondsPastEpoch = true;
        return this;
    }

    /**
     * Adds nanoseconds array to the NTScalarMultiChannel.
     *
     * @return this instance of NTScalarMultiChannelBuilder
     */
    public NTScalarMultiChannelBuilder addNanoseconds()
    {
        nanoseconds = true;
        return this;
    }

    /**
     * Adds userTag array to the NTScalarMultiChannel.
     *
     * @return this instance of NTScalarMultiChannelBuilder
     */
    public NTScalarMultiChannelBuilder addUserTag()
    {
        userTag = true;
        return this;
    }

    /**
     * Adds isConnected array to the NTScalarMultiChannel.
     *
     * @return this instance of NTScalarMultiChannelBuilder
     */
    public NTScalarMultiChannelBuilder addIsConnected()
    {
        isConnected = true;
        return this;
    }

    /**
     * Create a Structure that represents NTScalarMultiChannel.
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
        fields[ind++] = fieldCreate.createScalarArray(valueType);
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

        Structure st = fieldCreate.createStructure(NTScalarMultiChannel.URI,names,fields);
        reset();
        return st;
    }

    /**
     * Creates a PVStructure that represents NTScalarMultiChannel.
     * This resets this instance state and allows new instance to be created.
     *
     * @return a new instance of a PVStructure
     */
    public PVStructure createPVStructure()
    {
        return PVDataFactory.getPVDataCreate().createPVStructure(createStructure());
    }

    /**
     * Creates an NTScalarMultiChannel instance.
     * This resets this instance state and allows new instance to be created.
     *
     * @return a new instance of an NTScalarMultiChannel
     */
    public NTScalarMultiChannel create()
    {
        return new NTScalarMultiChannel(createPVStructure());
    }

    /**
     * Adds extra Field to the type.
     *
     * @param name the name of the field
     * @param field the field to add
     * @return this instance of an NTScalarMultiChannelBuilder
     */
    public NTScalarMultiChannelBuilder add(String name, Field field)
    {
        extraFields.add(field);
        extraFieldNames.add(name);
        return this;
    }

    NTScalarMultiChannelBuilder()
    {
        reset();
    }

    private void reset()
    {
        valueType = ScalarType.pvDouble;
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

    private ScalarType valueType = ScalarType.pvDouble;
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


