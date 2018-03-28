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
     * Adds dim field to the NTMatrix.
     *
     * @return this instance of NTMatrixBuilder
     */
    public NTMatrixBuilder addDim()
    {
        dim = true;
        return this;
    }

    /**
     * Adds descriptor field to the NTMatrix.
     *
     * @return this instance of NTMatrixBuilder
     */
    public NTMatrixBuilder addDescriptor()
    {
        descriptor = true;
        return this;
    }

    /**
     * Adds alarm field to the NTMatrix.
     *
     * @return this instance of NTMatrixBuilder
     */
    public NTMatrixBuilder addAlarm()
    {
        alarm = true;
        return this;
    }

    /**
     * Adds timeStamp field to the NTMatrix.
     *
     * @return this instance of NTMatrixBuilder
     */
    public NTMatrixBuilder addTimeStamp()
    {
        timeStamp = true;
        return this;
    }

    /**
     * Adds display field to the NTMatrix.
     *
     * @return this instance of NTMatrixBuilder
     */
    public NTMatrixBuilder addDisplay()
    {
        display = true;
        return this;
    }

    /**
     * Creates a Structure that represents NTMatrix.
     * This resets this instance state and allows new instance to be created.
     *
     * @return a new instance of a Structure
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
     * Creates a PVStructure that represents NTMatrix.
     * This resets this instance state and allows new instance to be created.
     *
     * @return a new instance of a PVStructure
     */
    public PVStructure createPVStructure()
    {
        return PVDataFactory.getPVDataCreate().createPVStructure(createStructure());
    }

    /**
     * Creates a NTMatrix instance.
     * This resets this instance state and allows new instance to be created.
     *
     * @return a new instance of a NTMatrix
     */
    public NTMatrix create()
    {
        return new NTMatrix(createPVStructure());
    }

    /**
     * Adds extra Field to the type.
     *
     * @param name name of the field
     * @param field a field to add
     * @return this instance of NTMatrixBuilder
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

