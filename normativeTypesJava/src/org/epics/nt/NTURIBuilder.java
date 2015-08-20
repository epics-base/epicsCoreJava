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
 * Interface for in-line creating of NTURI.
 *
 * One instance can be used to create multiple instances.
 * An instance of this object must not be used concurrently (an object has a state).
 * @author dgh
 */
public class NTURIBuilder
{
    /**
     * Add authority field to the NTURI.
     * @return this instance of <b>NTURIBuilder</b>.
     */
    public NTURIBuilder addAuthority()
    {
        authority = true;
        return this;
    }

    /**
     * Create a <b>Structure</b> that represents NTURI.
     * This resets this instance state and allows new instance to be created.
     * @return a new instance of a <b>Structure</b>.
     */
    public Structure createStructure()
    {
        FieldBuilder builder =
            FieldFactory.getFieldCreate().createFieldBuilder().
               setId(NTURI.URI).
               add("scheme", ScalarType.pvString);

        if (authority)
            builder.add("authority", ScalarType.pvString);

        builder.add("path", ScalarType.pvString);

        if (query)
        {
            FieldBuilder nestedBuilder = builder.addNestedStructure("query");

            int extraCount = queryFieldNames.size();
            for (int i = 0; i< extraCount; i++)
                nestedBuilder.add(queryFieldNames.get(i), queryTypes.get(i));

            builder = nestedBuilder.endNested();
        }

        int extraCount = extraFieldNames.size();
        for (int i = 0; i< extraCount; i++)
            builder.add(extraFieldNames.get(i), extraFields.get(i));

        Structure s = builder.createStructure();

        reset();
        return s;
    }

    /**
     * Create a <b>PVStructure</b> that represents NTURI.
     * This resets this instance state and allows new instance to be created.
     * @return a new instance of a <b>PVStructure</b>.
     */
    public PVStructure createPVStructure()
    {
        return PVDataFactory.getPVDataCreate().createPVStructure(createStructure());
    }

    /**
     * Create an <b>NTURI</b> instance.
     * This resets this instance state and allows new instance to be created.
     * @return a new instance of an <b>NTURI</b>.
     */
    public NTURI create()
    {
        return new NTURI(createPVStructure());
    }

    /**
     * Add extra <b>Scalar</b> of ScalarType pvString
     * to the query field of the type.
     * @param name name of the field.
     * @return this instance of <b>NTURIBuilder</b>.
     */
    public NTURIBuilder addQueryString(String name) 
    {
        query = true;
        queryFieldNames.add(name);
        queryTypes.add(ScalarType.pvString);
        return this;
    }

    /**
     * Add extra <b>Scalar</b> of ScalarType pvDouble
     * to the query field of the type.
     * @param name name of the field.
     * @return this instance of <b>NTURIBuilder</b>.
     */
    public NTURIBuilder addQueryDouble(String name) 
    {
        query = true;
        queryFieldNames.add(name);
        queryTypes.add(ScalarType.pvDouble);
        return this;
    }

    /**
     * Add extra <b>Scalar</b> of ScalarType pvDouble
     * to the query field of the type.
     * @param name name of the field.
     * @return this instance of <b>NTURIBuilder</b>.
     */
    public NTURIBuilder addQueryInt(String name) 
    {
        query = true;
        queryFieldNames.add(name);
        queryTypes.add(ScalarType.pvInt);
        return this;
    }

    /**
     * Add extra <b>Field</b> to the type.
     * @param name name of the field.
     * @param field a field to add.
     * @return this instance of <b>NTURIBuilder</b>.
     */
    public NTURIBuilder add(String name, Field field) 
    {
        extraFields.add(field);
        extraFieldNames.add(name);
        return this;
    }


    NTURIBuilder()
    {
        reset();
    }

    private void reset()
    {
        authority = false;
        query = false;
        queryFieldNames.clear();
        queryTypes.clear();
        extraFieldNames.clear();
        extraFields.clear();
    }

    private boolean authority;
    private boolean query;

    // NOTE: these preserve order, however they do not handle duplicates
    private ArrayList<String> queryFieldNames = new ArrayList<String>();
    private ArrayList<ScalarType> queryTypes = new ArrayList<ScalarType>();

    private ArrayList<String> extraFieldNames = new ArrayList<String>();
    private ArrayList<Field> extraFields = new ArrayList<Field>();
}

