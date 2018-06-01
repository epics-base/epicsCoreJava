/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.nt;

import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.Scalar;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Structure;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVScalar;
import org.epics.pvdata.pv.PVScalarArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStringArray;
import org.epics.pvdata.property.PVTimeStamp;
import org.epics.pvdata.property.PVAlarm;

/**
 * Wrapper class for NTURI.
 *
 * @author dgh
 */
public class NTURI
{
    public static final String URI = "epics:nt/NTURI:1.0";

    /**
     * Creates an NTURI wrapping the specified PVStructure if the latter is compatible.
     * <p>
     * Checks the supplied PVStructure is compatible with NTURI
     * and if so returns an NTURI which wraps it.
     * This method will return null if the structure is is not compatible
     * or is null.
     *
     * @param pvStructure the PVStructure to be wrapped
     * @return NTURI instance on success, null otherwise
     */
    public static NTURI wrap(PVStructure pvStructure)
    {
        if (!isCompatible(pvStructure))
            return null;
        return wrapUnsafe(pvStructure);
    }

    /**
     * Creates an NTURI wrapping the specified PVStructure, regardless of the latter's compatibility.
     * <p>
     * No checks are made as to whether the specified PVStructure
     * is compatible with NTURI or is non-null.
     *
     * @param structure the PVStructure to be wrapped
     * @return NTURI instance
     */
    public static NTURI wrapUnsafe(PVStructure structure)
    {
        return new NTURI(structure);
    }

    /**
     * Returns whether the specified Structure reports to be a compatible NTURI.
     * <p>
     * Checks if the specified Structure reports compatibility with this
     * version of NTURI through its type ID, including checking version numbers.
     * The return value does not depend on whether the structure is actually
     * compatible in terms of its introspection type.
     *
     * @param structure the Structure to test.
     * @return (false,true) if (is not, is) a compatible NTURI.
     */
    public static boolean is_a(Structure structure)
    {
        return NTUtils.is_a(structure.getID(), URI);
    }

    /**
     * Returns whether the specified PVStructure reports to be a compatible NTURI.
     * <p>
     * Checks if the specified PVStructure reports compatibility with this
     * version of NTURI through its type ID, including checking version numbers.
     * The return value does not depend on whether the structure is actually
     * compatible in terms of its introspection type.
     *
     * @param pvStructure the PVStructure to test.
     * @return (false,true) if (is not, is) a compatible NTURI.
     */
    public static boolean is_a(PVStructure pvStructure)
    {
        return is_a(pvStructure.getStructure());
    }

    /**
     * Returns whether the specified Structure is compatible with NTURI.
     * <p>
     * Checks if the specified Structure is compatible with this version
     * of NTURI through the introspection interface.
     *
     * @param structure the Structure to test
     * @return (false,true) if (is not, is) a compatible NTURI
     */
    public static boolean isCompatible(Structure structure)
    {
        if (structure == null) return false;

        Scalar schemeField = structure.getField(Scalar.class, "scheme");
        if (schemeField == null)
            return false;

        if (schemeField.getScalarType() != ScalarType.pvString)
            return false;


        Scalar pathField = structure.getField(Scalar.class, "path");
        if (pathField == null)
            return false;

        if (pathField.getScalarType() != ScalarType.pvString)
            return false;

        Field field = structure.getField("authority");
        if (field != null)
        {
            Scalar authorityField = structure.getField(Scalar.class, "authority");
            if (authorityField == null || authorityField.getScalarType() != ScalarType.pvString)
                return false;
        }

        field = structure.getField("query");
        if (field != null)
        {
            Structure queryField = structure.getField(Structure.class, "query");
            if (queryField == null)
                return false;

            try {
                // check fields are scalars and int/double/string
                Field[] queryFields = queryField.getFields();
                for (Field f : queryFields)
                {
                    ScalarType t = ((Scalar)f).getScalarType();
                    switch (t)
                    {
                    case pvString:
                    case pvDouble:
                    case pvInt:
                        break;
                    default:
                        return false;
                    }
                }
            }
            catch (ClassCastException e) { return false; }            
        }

        return true;
    }

    /**
     * Returns whether the specified PVStructure is compatible with NTURI.
     * <p>
     * Checks if the specified PVStructure is compatible with this version
     * of NTURI through the introspection interface.
     *
     * @param pvStructure the PVStructure to test
     * @return (false,true) if (is not, is) a compatible NTURI
     */
    public static boolean isCompatible(PVStructure pvStructure)
    {
        if (pvStructure == null) return false;

        return isCompatible(pvStructure.getStructure());
    }

    /**
     * Returns whether the wrapped PVStructure is a valid NTURI.
     * <p>
     * Unlike isCompatible(), isValid() may perform checks on the value
     * data as well as the introspection data.
     *
     * @return (false,true) if wrapped PVStructure (is not, is) a valid NTURI.
     */
    public boolean isValid()
    {
        return true;
    }

    /**
     * Creates an NTURI builder instance.
     *
     * @return builder instance.
     */
    public static NTURIBuilder createBuilder()
    {
        return new NTURIBuilder();
    }

    /**
     * Returns the PVStructure wrapped by this instance.
     *
     * @return the PVStructure wrapped by this instance
     */
    public PVStructure getPVStructure()
    {
        return pvNTURI;
    }

    /**
     * Returns the scheme field.
     *
     * @return the scheme field
     */
    public PVString getScheme()
    {
        return pvNTURI.getSubField(PVString.class, "scheme");
    }

    /**
     * Returns the authority field.
     *
     * @return the authority field or null if no such field
     */
    public PVString getAuthority()
    {
       return pvNTURI.getSubField(PVString.class, "authority");
    }

    /**
     * Returns the path field.
     *
     * @return the path field
     */
    public PVString getPath()
    {
       return pvNTURI.getSubField(PVString.class, "path");
    }

    /**
     * Returns the query field.
     *
     * @return the query field or null if no such field
     */
    public PVStructure getQuery()
    {
       return pvQuery;
    }

    /**
     * Returns the names of the query fields for the URI.
     * For each name, calling getQueryField should return
     * the query field, which should not be null.
     *
     * @return the query field names
     */
    public String[] getQueryNames()
    {
       if (pvQuery == null) return new String[0];

       return pvQuery.getStructure().getFieldNames();
    }

    /**
     * Returns the query subfield with the specified name.
     *
     * @param name the name of the requested field
     * @return the query subfield or null if the subfield does not exist
     */
    public PVScalar getQueryField(String name)
    {
        PVField pvField = pvQuery.getSubField(name);
		if (PVScalar.class.isInstance(pvField))
			return (PVScalar)(pvField);
		else
			return null;
    }

    /**
     * Returns the query subfield with the specified name and
     * of a specified type (e.g. PVString).
     *
     * @param <T> the expected type of the query field
     * @param c class object modeling the class T (must be PVString, PVDouble or PVInt)
     * @param name the name of the query field
     * @return the query subfield or null if the subfield does not exist
     *          or the field is not of <code>c</code> type
     */
    public <T extends PVScalar> T getQueryField(Class<T> c, String name)
    {
        PVField pvField = pvQuery.getSubField(name);
		if (c.isInstance(pvField))
			return c.cast(pvField);
		else
			return null;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */ 
    public String toString()
    {
        return getPVStructure().toString();
    }

    /**
     * Constructor.
     *
     * @param pvStructure the PVStructure to be wrapped
     */
    NTURI(PVStructure pvStructure)
    {
        pvNTURI = pvStructure;
        pvQuery = pvNTURI.getSubField(PVStructure.class, "query");
    }

    private PVStructure pvNTURI;
    private PVStructure pvQuery;
}

