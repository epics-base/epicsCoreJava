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
 * Interface for in-line creating of NTMultiChannel.
 *
 * One instance can be used to create multiple instances.
 * An instance of this object must not be used concurrently (an object has a state).
 * @author dgh
 */
public class NTMultiChannelBuilder
{
    /**
     * specify the union for the value field.
     * If this is not called then a variantUnion is the default.
     * @return this instance of  <b>NTMultiChannelBuilder</b>.
     */
    public NTMultiChannelBuilder value(Union value)
    {
        valueType = value;
        return this;
    }

    /**
     * Add descriptor field to the NTMultiChannel.
     * @return this instance of <b>NTMultiChannelBuilder</b>.
     */
    public NTMultiChannelBuilder addDescriptor()
    {
        descriptor = true;
        return this;
    }

    /**
     * Add alarm structure to the NTMultiChannel.
     * @return this instance of <b>NTMultiChannelBuilder</b>.
     */
    public NTMultiChannelBuilder addAlarm()
    {
        alarm = true;
        return this;
    }

    /**
     * Add timeStamp structure to the NTMultiChannel.
     * @return this instance of <b>NTMultiChannelBuilder</b>.
     */
    public NTMultiChannelBuilder addTimeStamp()
    {
        timeStamp = true;
        return this;
    }

    /**
     * Add severity array to the NTMultiChannel.
     * @return this instance of <b>NTMultiChannelBuilder</b>.
     */
    public NTMultiChannelBuilder addSeverity()
    {
        severity = true;
        return this;
    }

    /**
     * Add status array to the NTMultiChannel.
     * @return this instance of <b>NTMultiChannelBuilder</b>.
     */
    public NTMultiChannelBuilder addStatus()
    {
       status = true;
        return this;
    }

    /**
     * Add message array to the NTMultiChannel.
     * @return this instance of <b>NTMultiChannelBuilder</b>.
     */
    public NTMultiChannelBuilder addMessage()
    {
        message = true;
        return this;
    }

    /**
     * Add secondsPastEpoch array to the NTMultiChannel.
     * @return this instance of <b>NTMultiChannelBuilder</b>.
     */
    public NTMultiChannelBuilder addSecondsPastEpoch()
    {
        secondsPastEpoch = true;
        return this;
    }

    /**
     * Add nanoseconds array to the NTMultiChannel.
     * @return this instance of <b>NTMultiChannelBuilder</b>.
     */
    public NTMultiChannelBuilder addNanoseconds()
    {
        nanoseconds = true;
        return this;
    }

    /**
     * Add userTag array to the NTMultiChannel.
     * @return this instance of <b>NTMultiChannelBuilder</b>.
     */
    public NTMultiChannelBuilder addUserTag()
    {
        userTag = true;
        return this;
    }

    /**
     * Create a <b>Structure</b> that represents NTMultiChannel.
     * This resets this instance state and allows new instance to be created.
     * @return a new instance of a <b>Structure</b>.
     */
    public Structure createStructure()
    {
        int nfields = 3;
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
        names[ind] = "isConnected";
        fields[ind++] =  fieldCreate.createScalarArray(ScalarType.pvBoolean);
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
        for (int i = 0; i< extraCount; i++) {
            names[ind] = extraFieldNames.get(i);
            fields[ind++] = extraFields.get(i);
        }

        Structure st = fieldCreate.createStructure(NTMultiChannel.URI,names,fields);
        reset();
        return st;
    }

    /**
     * Create a <b>PVStructure</b> that represents NTMultiChannel.
     * This resets this instance state and allows new {@code instance to be created.}
     * @return a new instance of a <b>PVStructure</b>
     */
    public PVStructure createPVStructure()
    {
        return PVDataFactory.getPVDataCreate().createPVStructure(createStructure());
    }

    /**
     * Create a <b>NTMultiChannel</b> instance.
     * This resets this instance state and allows new {@code instance to be created.}
     * @return a new instance of a <b>NTMultiChannel</b>
     */
    public NTMultiChannel create()
    {
        return new NTMultiChannel(createPVStructure());
    }

    /**
     * Add extra <b>Field</b> to the type.
     * @param name name of the field.
     * @param field a field to add.
     * @return this instance of a <b>NTMultiChannelBuilder</b>
     */
    public NTMultiChannelBuilder add(String name, Field field)
    {
        extraFields.add(field);
        extraFieldNames.add(name);
        return this;
    }

    NTMultiChannelBuilder() {}

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

    // NOTE: this preserves order, however it does not handle duplicates
    private ArrayList<String> extraFieldNames = new ArrayList<String>();
    private ArrayList<Field> extraFields = new ArrayList<Field>();
}


