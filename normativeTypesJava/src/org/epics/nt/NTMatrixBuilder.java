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
 * Interface for in-line creating of NTMatrix.
 *
 * One instance can be used to create multiple instances.
 * An instance of this object must not be used concurrently (an object has a state).
 * @author dgh
 */
public class NTMatrixBuilder
{
    /**
     * Add dim field to the NTMatrix.
     * @return this instance of <b>NTMatrixBuilder</b>.
     */
    public NTMatrixBuilder addDim()
    {
        dim = true;
        return this;
    }

    /**
     * Add descriptor field to the NTMatrix.
     * @return this instance of <b>NTMatrixBuilder</b>.
     */
    public NTMatrixBuilder addDescriptor()
    {
        descriptor = true;
        return this;
    }

    /**
     * Add alarm structure to the NTMatrix.
     * @return this instance of <b>NTMatrixBuilder</b>.
     */
    public NTMatrixBuilder addAlarm()
    {
        alarm = true;
        return this;
    }

    /**
     * Add timeStamp structure to the NTMatrix.
     * @return this instance of <b>NTMatrixBuilder</b>.
     */
    public NTMatrixBuilder addTimeStamp()
    {
        timeStamp = true;
        return this;
    }

    /**
     * Add display structure to the NTMatrix.
     * @return this instance of <b>NTMatrixBuilder</b>.
     */
    public NTMatrixBuilder addDisplay()
    {
        display = true;
        return this;
    }

    /**
     * Create a <b>Structure</b> that represents NTMatrix.
     * This resets this instance state and allows new instance to be created.
     * @return a new instance of a <b>Structure</b>.
     */
    public Structure createStructure()
    {
        FieldBuilder builder =
            FieldFactory.getFieldCreate().createFieldBuilder().
               setId(NTMatrix.URI).
               addArray("value", ScalarType.pvDouble);

        NTField ntField = NTField.get();

        if (dim)
           builder.addArray("dim", ScalarType.pvInt);

        if (descriptor)
            builder.add("descriptor", ScalarType.pvString);

        if (alarm)
            builder.add("alarm", ntField.createAlarm());

        if (timeStamp)
            builder.add("timeStamp", ntField.createTimeStamp());

        if (display)
            builder.add("display", ntField.createDisplay());

        int extraCount = extraFieldNames.size();
        for (int i = 0; i< extraCount; i++)
            builder.add(extraFieldNames.get(i), extraFields.get(i));

        Structure s = builder.createStructure();

        reset();
        return s;
    }

    /**
     * Create a <b>PVStructure</b> that represents NTMatrix.
     * This resets this instance state and allows new instance to be created.
     * @return a new instance of a <b>PVStructure</b>.
     */
    public PVStructure createPVStructure()
    {
        return PVDataFactory.getPVDataCreate().createPVStructure(createStructure());
    }

    /**
     * Create a <b>NTMatrix</b> instance.
     * This resets this instance state and allows new instance to be created.
     * @return a new instance of a <b>NTMatrix</b>.
     */
    public NTMatrix create()
    {
        return new NTMatrix(createPVStructure());
    }

    /**
     * Add extra <b>Field</b> to the type.
     * @param name name of the field.
     * @param field a field to add.
     * @return this instance of <b>NTMatrixBuilder</b>.
     */
    public NTMatrixBuilder add(String name, Field field) 
    {
        extraFields.add(field);
        extraFieldNames.add(name);
        return this;
    }

    NTMatrixBuilder()
    {
        reset();
    }

    private void reset()
    {
        dim = false;
        descriptor = false;
        alarm = false;
        timeStamp = false;
        display = false;
        extraFieldNames.clear();
        extraFields.clear();
    }

    private boolean dim;
    private boolean descriptor;
    private boolean alarm;
    private boolean timeStamp;
    private boolean display;

    // NOTE: this preserves order, however it does not handle duplicates
    private ArrayList<String> extraFieldNames = new ArrayList<String>();
    private ArrayList<Field> extraFields = new ArrayList<Field>();
}

