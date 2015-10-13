/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
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
     * Checks the supplied structure is compatible with NTURI
     * and if so returns a NTURI which wraps it.
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
     * Checks if the specified structure reports to be a compatible NTURI.
     * <p>
     * Checks whether the specified structure reports compatibility with this
     * version of NTURI through type ID, including checking version numbers.
     * The return value does not depend on whether the structure is actually
     * compatible in terms of its introspection type.
     *
     * @param structure The Structure to test.
     * @return (false,true) if (is not, is) a compatible NTURI.
     */
    public static boolean is_a(Structure structure)
    {
        return NTUtils.is_a(structure.getID(), URI);
    }

    /**
     * Checks if the specified structure reports to be a compatible NTURI.
     * <p>
     * Checks whether the specified structure reports compatibility with this
     * version of NTURI through type ID, including checking version numbers.
     * The return value does not depend on whether the structure is actually
     * compatible in terms of its introspection type
     * @param pvStructure the PVStructure to test.
     * @return (false,true) if (is not, is) a compatible NTURI.
     */
    public static boolean is_a(PVStructure pvStructure)
    {
        return is_a(pvStructure.getStructure());
    }

    /**
     * Checks if the specified structure is compatible with NTURI.
     * <p>
     * Checks whether the specified structure is compatible with this version
     * of NTURI through introspection interface.
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
     * Checks if the specified structure is compatible with NTURI.
     * <p>
     * Checks whether the specified structure is compatible with this version
     * of NTURI through introspection interface.
     *
     * @param pvStructure the PVStructure to test.
     * @return (false,true) if (is not, is) a compatible NTURI.
     */
    public static boolean isCompatible(PVStructure pvStructure)
    {
        if (pvStructure == null) return false;

        return isCompatible(pvStructure.getStructure());
    }

    /**
     * Checks if the specified structure is a valid NTURI.
     *
     * Checks whether the wrapped structure is valid with respect to this
     * version of NTURI
     *
     * @return (false,true) if (is not, is) a valid NTURI.
     */
    public boolean isValid()
    {
        return true;
    }

    /**
     * Create a NTURI builder instance.
     *
     * @return builder instance.
     */
    public static NTURIBuilder createBuilder()
    {
        return new NTURIBuilder();
    }

    /**
     * Get the PVStructure.
     *
     * @return PVStructure
     */
    public PVStructure getPVStructure()
    {
        return pvNTURI;
    }

    /**
     * Get the scheme field.
     *
     * @return The PVString for the scheme
     */
    public PVString getScheme()
    {
        return pvNTURI.getSubField(PVString.class, "scheme");
    }

    /**
     * Get the authority field.
     *
     * @return the PVString for the authority field
     */
    public PVString getAuthority()
    {
       return pvNTURI.getSubField(PVString.class, "authority");
    }

    /**
     * Get the path field.
     *
     * @return the PVString for the path field
     */
    public PVString getPath()
    {
       return pvNTURI.getSubField(PVString.class, "path");
    }

    /**
     * Get the query field.
     *
     * @return The PVStructure for the query field
     */
    public PVStructure getQuery()
    {
       return pvQuery;
    }

    /**
     * Get the names of the query fields for the URI.
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
     * Get the query subfield with the specified name.
     *
     * @param name the name of the requested field
     * @return the PVScalar for the query field
     */
    public PVScalar getQueryField(String name)
    {
        PVField pvField = pvQuery.getSubField(name);
		if (PVScalar.class.isInstance(pvField))
			return (PVScalar)(pvField);
		else
			return null;
    }


    /* Get the query subfield with the specified name and
     * of a specified type (e.g. PVString).
     *
     * @param <T> the expected type of the query field
     * @param c class object modeling the class T (must be PVString, PVDouble or PVInt)
     * @param name the name of the query field
     * @return the field or null if the subfield does not exist, or the field is not of <code>c</code> type
     * @return the PVScalar for the query field
     */
    public <T extends PVScalar> T getValue(Class<T> c, String name)
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

