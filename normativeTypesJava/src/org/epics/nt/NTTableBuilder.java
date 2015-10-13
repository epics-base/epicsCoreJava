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
     * Add a column of a given <b>ScalarType</b> to the NTTable.
     *
     * @param name the name of the column
     * @param elementType the type of the scalar array elements of the column
     * @return this instance of <b>NTTableBuilder</b>
     */
    public NTTableBuilder addColumn(String name, ScalarType elementType)
    {
        // TODO: check for duplicate columns

        columnNames.add(name);
        types.add(elementType);

        return this;
    }

    /**
     * Add columns, each of a given <b>ScalarType</b>, to the NTTable.
     * 
     * @param names the names of the columns
     * @param elementTypes the types of the scalar array elements of the columns
     * @return this instance of <b>NTTableBuilder</b>
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
     * Add descriptor field to the NTTable.
     * 
     * @return this instance of <b>NTTableBuilder</b>
     */
    public NTTableBuilder addDescriptor()
    {
        descriptor = true;
        return this;
    }

    /**
     * Add alarm structure to the NTTable.
     * 
     * @return this instance of <b>NTTableBuilder</b>
     */
    public NTTableBuilder addAlarm()
    {
        alarm = true;
        return this;
    }

    /**
     * Add timeStamp structure to the NTTable.
     * 
     * @return this instance of <b>NTTableBuilder</b>
     */
    public NTTableBuilder addTimeStamp()
    {
        timeStamp = true;
        return this;
    }

    /**
     * Create a <b>Structure</b> that represents NTTable.
     * This resets this instance state and allows new instance to be created.
     * 
     * @return a new instance of a <b>Structure</b>
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
     * Create a <b>PVStructure</b> that represents NTTable.
     * This resets this instance state and allows new instance to be created
     *
     * @return a new instance of a <b>PVStructure</b>
     */
    public PVStructure createPVStructure()
    {
        // put the column names in labels by default
        String[] labelArray = columnNames.toArray(new String[columnNames.size()]);

        PVStructure s = PVDataFactory.getPVDataCreate().createPVStructure(
            createStructure());

        s.getSubField(PVStringArray.class, "labels").put(
            0, labelArray.length, labelArray, 0);

        return s;
    }

    /**
     * Create a <b>NTTable</b> instance.
     * This resets this instance state and allows new instance to be created
     *
     * @return a new instance of an <b>NTTable</b>
     */
    public NTTable create()
    {
        return new NTTable(createPVStructure());
    }

    /**
     * Add extra <b>Field</b> to the type.
     *
     * @param name the name of the field
     * @param field the field to add
     * @return this instance of <b>NTTableBuilder</b>
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

