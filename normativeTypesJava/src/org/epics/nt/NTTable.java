/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.nt;

import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.Scalar;
import org.epics.pvdata.pv.ScalarArray;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Structure;
import org.epics.pvdata.pv.Type;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVScalar;
import org.epics.pvdata.pv.PVScalarArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStringArray;
import org.epics.pvdata.property.PVTimeStamp;
import org.epics.pvdata.property.PVAlarm;

/**
 * Wrapper class for NTTable
 *
 * @author dgh
 */
public class NTTable
    implements HasTimeStamp, HasAlarm
{
    public static final String URI = "epics:nt/NTTable:1.0";

    /**
     * Creates an NTTable wrapping the specified PVStructure if the latter is compatible.
     *
     * Checks the supplied structure is compatible with NTTable
     * and if so returns a NTTable which wraps it.
     * This method will return null if the structure is is not compatible
     * or is null.
     * @param pvStructure The PVStructure to be wrapped.
     * @return NTTable instance on success, null otherwise.
     */
    public static NTTable wrap(PVStructure pvStructure)
    {
        if (!isCompatible(pvStructure))
            return null;
        return wrapUnsafe(pvStructure);
    }

    /**
     * Creates an NTTable wrapping the specified PVStructure, regardless of the latter's compatibility.
     *
     * No checks are made as to whether the specified PVStructure
     * is compatible with NTTable or is non-null.
     * @param structure The PVStructure to be wrapped.
     * @return NTTable instance.
     */
    public static NTTable wrapUnsafe(PVStructure structure)
    {
        return new NTTable(structure);
    }

    /**
     * Checks if the specified structure reports to be a compatible NTTable.
     *
     * Checks whether the specified structure reports compatibility with this
     * version of NTTable through type ID, including checking version numbers.
     * The return value does not depend on whether the structure is actually
     * compatible in terms of its introspection type
     * @param structure The Structure to test.
     * @return (false,true) if (is not, is) a compatible NTTable.
     */
    public static boolean is_a(Structure structure)
    {
        return NTUtils.is_a(structure.getID(), URI);
    }


    /**
     * Checks if the specified structure reports to be a compatible NTTable.
     *
     * Checks whether the specified structure reports compatibility with this
     * version of NTTable through type ID, including checking version numbers.
     * The return value does not depend on whether the structure is actually
     * compatible in terms of its introspection type
     * @param pvStructure The PVStructure to test.
     * @return (false,true) if (is not, is) a compatible NTTable.
     */
    public static boolean is_a(PVStructure pvStructure)
    {
        return is_a(pvStructure.getStructure());
    }

    /**
     * Checks if the specified structure is compatible with NTTable.
     *
     * Checks whether the specified structure is compatible with this version
     * of NTTable through introspection interface.
     * @param structure The Structure to test.
     * @return (false,true) if (is not, is) a compatible NTTable.
     */
    public static boolean isCompatible(Structure structure)
    {
        if (structure == null) return false;

        Structure valueField = structure.getField(Structure.class, "value");
        if (valueField == null)
            return false;

        for (Field field : valueField.getFields())
        {
            if (field.getType() != Type.scalarArray) return false;
        }

        ScalarArray labelsField = structure.getField(ScalarArray.class, "labels");
        if (labelsField == null)
            return false;

        if (labelsField.getElementType() != ScalarType.pvString)
            return false;

        Field field = structure.getField("descriptor");
        if (field != null)
        {
            Scalar descriptorField = structure.getField(Scalar.class, "descriptor");
            if (descriptorField == null || descriptorField.getScalarType() != ScalarType.pvString)
                return false;
        }

        NTField ntField = NTField.get();

        field = structure.getField("alarm");
        if (field != null && !ntField.isAlarm(field))
            return false;

        field = structure.getField("timeStamp");
        if (field != null && !ntField.isTimeStamp(field))
            return false;

        return true;
    }

    /**
     * Checks if the specified structure is compatible with NTTable.
     *
     * Checks whether the specified structure is compatible with this version
     * of NTTable through introspection interface.
     * @param pvStructure The PVStructure to test.
     * @return (false,true) if (is not, is) a compatible NTTable.
     */
    public static boolean isCompatible(PVStructure pvStructure)
    {
        if (pvStructure == null) return false;

        return isCompatible(pvStructure.getStructure());
    }

    /**
     * Checks if the specified structure is a valid NTTable.
     *
     * Checks whether the wrapped structure is valid with respect to this
     * version of NTTable
     * @return (false,true) if (is not, is) a valid NTTable.
     */
    public boolean isValid()
    {
        PVField[] columns = pvValue.getPVFields();
        
        if (getLabels().getLength() != columns.length) return false;
        boolean first = true;
        int length = 0;
        for (PVField column : columns)
        {
            try
            {
                int colLength = ((PVScalarArray)column).getLength();
                if (first)
                {
                    length = colLength;
                    first = false;
                }
                else if (length != colLength)
                    return false;
            }
            catch (ClassCastException e)
            {
                return false;
            }
        }

        return true;
    }

    /**
     * Create a NTTable builder instance.
     * @return builder instance.
     */
    public static NTTableBuilder createBuilder()
    {
        return new NTTableBuilder();
    }

    /**
     * Get the pvStructure.
     * @return PVStructure.
     */
    public PVStructure getPVStructure()
    {
        return pvNTTable;
    }

    /**
     * Get the value field.
     * @return The PVStructure for the values.
     */
    public PVStructure getValue()
    {
        return pvValue;
    }

    /**
     * Get the labels field.
     * @return The pvStringArray for the labels.
     */
    public PVStringArray getLabels()
    {
       return pvNTTable.getSubField(PVStringArray.class, "labels");
    }

    /**
     * Get the column names for the table.
     * For each name, calling getColumn should return the column, which should not be null.
     * @return The column names.
     */
    public String[] getColumnNames()
    {
       return pvValue.getStructure().getFieldNames();
    }

    /**
     * Get the PVScalaray field representing the column for a column name.
     * @param columnName The name of the column.
     * @return The PVScalarArray for the field.
     */
    public PVScalarArray getColumn(String columnName)
    {
       return pvValue.getSubField(PVScalarArray.class, columnName);
    }

    /* Get a column of a specified type (e.g. PVDoubleArray).
     * @param c expected class of a requested field.
     * @param columnName The name of the column.
     * @return The PVField or null if the subfield does not exist, or the field is not of <code>c</code> type.
     */
    public <T extends PVScalarArray> T getColumn(Class<T> c, String columnName)
    {
        PVField pvColumn = getColumn(columnName);
        if (c.isInstance(pvColumn))
            return c.cast(pvColumn);
        else
            return null;
    }

    /**
     * Get the descriptor field.
     * @return The pvString or null if no function field.
     */
    public PVString getDescriptor()
    {
        return pvNTTable.getSubField(PVString.class, "descriptor");
    }

    /* (non-Javadoc)
	 * @see org.epics.pvdata.nt.HasAlarm#getAlarm()
	 */
    public PVStructure getAlarm()
    {
       return pvNTTable.getSubField(PVStructure.class, "alarm");
    }

    /* (non-Javadoc)
	 * @see org.epics.pvdata.nt.HasTimeStamp#getTimeStamp()
	 */
    public PVStructure getTimeStamp()
    {
        return pvNTTable.getSubField(PVStructure.class, "timeStamp");
    }


    /* (non-Javadoc)
	 * @see org.epics.pvdata.nt.HasAlarm#attachAlarm(org.epics.pvdata.property.PVAlarm)
	 */
    public boolean attachAlarm(PVAlarm pvAlarm)
    {
        PVStructure al = getAlarm();
        if (al != null)
            return pvAlarm.attach(al);
        else
            return false;
    }

    /* (non-Javadoc)
	 * @see org.epics.pvdata.nt.HasTimeStamp#attachTimeStamp(org.epics.pvdata.property.PVTimeStamp)
	 */
    public boolean attachTimeStamp(PVTimeStamp pvTimeStamp)
    {
        PVStructure ts = getTimeStamp();
        if (ts != null)
            return pvTimeStamp.attach(ts);
        else
            return false;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */ 
    public String toString()
    {
        return getPVStructure().toString();
    }

    /**
     * Constructor
     * @param pvStructure The PVStructure to be wrapped.
     */
    NTTable(PVStructure pvStructure)
    {
        pvNTTable = pvStructure;
        pvValue = pvNTTable.getSubField(PVStructure.class, "value");
    }

    private PVStructure pvNTTable;
    private PVStructure pvValue;
}

