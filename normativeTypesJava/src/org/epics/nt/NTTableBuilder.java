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
import org.epics.pvdata.pv.PVStringArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;
import java.util.ArrayList;

/**
 * Interface for in-line creating of NTTable.
 *
 * One instance can be used to create multiple instances.
 * An instance of this object must not be used concurrently (an object has a state).
 * @author dgh
 */
public class NTTableBuilder
{
    /**
     * Adds a column of a given ScalarType to the NTTable.
     *
     * @param name the name of the column
     * @param elementType the type of the scalar array elements of the column
     * @return this instance of NTTableBuilder
     */
    public NTTableBuilder addColumn(String name, ScalarType elementType)
    {
        // TODO: check for duplicate columns

        columnNames.add(name);
        types.add(elementType);

        return this;
    }

    /**
     * Adds columns, each of a given ScalarType, to the NTTable.
     *
     * @param names the names of the columns
     * @param elementTypes the types of the scalar array elements of the columns
     * @return this instance of NTTableBuilder
     */
    public NTTableBuilder addColumns(String[] names, ScalarType[] elementTypes)
    {
        // TODO: check for duplicate columns

        if (names.length != elementTypes.length)
            throw new RuntimeException("Column name and type lengths must match)");

        for (int i = 0; i < names.length; ++i)
            addColumn(names[i], elementTypes[i]);

        return this;
    }

    /**
     * Adds descriptor field to the NTTable.
     *
     * @return this instance of NTTableBuilder
     */
    public NTTableBuilder addDescriptor()
    {
        descriptor = true;
        return this;
    }

    /**
     * Adds alarm field to the NTTable.
     *
     * @return this instance of NTTableBuilder
     */
    public NTTableBuilder addAlarm()
    {
        alarm = true;
        return this;
    }

    /**
     * Adds timeStamp field to the NTTable.
     *
     * @return this instance of NTTableBuilder
     */
    public NTTableBuilder addTimeStamp()
    {
        timeStamp = true;
        return this;
    }

    /**
     * Creates a Structure that represents NTTable.
     * This resets this instance state and allows new instance to be created.
     *
     * @return a new instance of a Structure
     */
    public Structure createStructure()
    {
        FieldBuilder builder =
            FieldFactory.getFieldCreate().createFieldBuilder();

        FieldBuilder nestedBuilder = builder.
               setId(NTTable.URI).
               addArray("labels", ScalarType.pvString).
               addNestedStructure("value");

        int len = columnNames.size();
        for (int i = 0; i < len; i++)
           nestedBuilder.addArray(columnNames.get(i), types.get(i));

        builder = nestedBuilder.endNested();

        NTField ntField = NTField.get();

        if (descriptor)
            builder.add("descriptor", ScalarType.pvString);

        if (alarm)
            builder.add("alarm", ntField.createAlarm());

        if (timeStamp)
            builder.add("timeStamp", ntField.createTimeStamp());

        int extraCount = extraFieldNames.size();
        for (int i = 0; i < extraCount; i++)
            builder.add(extraFieldNames.get(i), extraFields.get(i));

        Structure s = builder.createStructure();

        reset();
        return s;
    }

    /**
     * Creates a PVStructure that represents NTTable.
     * This resets this instance state and allows new instance to be created
     *
     * @return a new instance of a PVStructure
     */
    public PVStructure createPVStructure()
    {
        // put the column names in labels by default
        String[] labelArray = columnNames.toArray(new String[0]);

        PVStructure s = PVDataFactory.getPVDataCreate().createPVStructure(
            createStructure());

        s.getSubField(PVStringArray.class, "labels").put(
            0, labelArray.length, labelArray, 0);

        return s;
    }

    /**
     * Creates an NTTable instance.
     * This resets this instance state and allows new instance to be created
     *
     * @return a new instance of an NTTable
     */
    public NTTable create()
    {
        return new NTTable(createPVStructure());
    }

    /**
     * Adds extra Field to the type.
     *
     * @param name the name of the field
     * @param field the field to add
     * @return this instance of NTTableBuilder
     */
    public NTTableBuilder add(String name, Field field)
    {
        extraFields.add(field);
        extraFieldNames.add(name);
        return this;
    }

    NTTableBuilder()
    {
        reset();
    }

    private void reset()
    {
        columnNames.clear();
        types.clear();
        descriptor = false;
        alarm = false;
        timeStamp = false;
        extraFieldNames.clear();
        extraFields.clear();
    }

    // NOTE: this preserves order, however it does not handle duplicates
    private ArrayList<String> columnNames = new ArrayList<String>();
    private ArrayList<ScalarType> types = new ArrayList<ScalarType>();

    private boolean descriptor;
    private boolean alarm;
    private boolean timeStamp;

    // NOTE: this preserves order, however it does not handle duplicates
    private ArrayList<String> extraFieldNames = new ArrayList<String>();
    private ArrayList<Field> extraFields = new ArrayList<Field>();
}

