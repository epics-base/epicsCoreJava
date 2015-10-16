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
 * Wrapper class for NTTable.
 *
 * @author dgh
 */
public class NTTable
    implements HasTimeStamp, HasAlarm
{
    public static final String URI = "epics:nt/NTTable:1.0";

    /**
     * Creates an NTTable wrapping the specified PVStructure if the latter is compatible.
     * <p>
     * Checks the supplied PVStructure is compatible with NTTable
     * and if so returns an NTTable which wraps it.
     * This method will return null if the structure is is not compatible
     * or is null.
     *
     * @param pvStructure the PVStructure to be wrapped
     * @return NTTable instance on success, null otherwise
     */
    public static NTTable wrap(PVStructure pvStructure)
    {
        if (!isCompatible(pvStructure))
            return null;
        return wrapUnsafe(pvStructure);
    }

    /**
     * Creates an NTTable wrapping the specified PVStructure, regardless of the latter's compatibility.
     * <p>
     * No checks are made as to whether the specified PVStructure
     * is compatible with NTTable or is non-null.
     *
     * @param structure the PVStructure to be wrapped
     * @return NTTable instance
     */
    public static NTTable wrapUnsafe(PVStructure structure)
    {
        return new NTTable(structure);
    }

    /**
     * Returns whether the specified Structure reports to be a compatible NTTable.
     * <p>
     * Checks if the specified Structure reports compatibility with this
     * version of NTTable through its type ID, including checking version numbers.
     * The return value does not depend on whether the structure is actually
     * compatible in terms of its introspection type.
     *
     * @param structure the Structure to test
     * @return (false,true) if (is not, is) a compatible NTTable
     */
    public static boolean is_a(Structure structure)
    {
        return NTUtils.is_a(structure.getID(), URI);
    }


    /**
     * Returns whether the specified PVStructure reports to be a compatible NTTable.
     * <p>
     * Checks if the specified PVStructure reports compatibility with this
     * version of NTTable through its type ID, including checking version numbers.
     * The return value does not depend on whether the structure is actually
     * compatible in terms of its introspection type.
     *
     * @param pvStructure the PVStructure to test
     * @return (false,true) if (is not, is) a compatible NTTable
     */
    public static boolean is_a(PVStructure pvStructure)
    {
        return is_a(pvStructure.getStructure());
    }

    /**
     * Returns whether the specified Structure is compatible with NTTable.
     * <p>
     * Checks if the specified Structure is compatible with this version
     * of NTTable through the introspection interface.
     *
     * @param structure the Structure to test
     * @return (false,true) if (is not, is) a compatible NTTable
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
     * Returns whether the specified PVStructure is compatible with NTTable.
     *
     * Checks if the specified PVStructure is compatible with this version
     * of NTTable through the introspection interface.
     *
     * @param pvStructure the PVStructure to test
     * @return (false,true) if (is not, is) a compatible NTTable
     */
    public static boolean isCompatible(PVStructure pvStructure)
    {
        if (pvStructure == null) return false;

        return isCompatible(pvStructure.getStructure());
    }

    /**
     * Returns whether the wrapped PVStructure is a valid NTTable.
     * <p>
     * Unlike isCompatible(), isValid() may perform checks on the value
     * data as well as the introspection data.
     *
     * @return (false,true) if wrapped PVStructure (is not, is) a valid NTTable
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
     * Creates an NTTable builder instance.
     *
     * @return builder instance.
     */
    public static NTTableBuilder createBuilder()
    {
        return new NTTableBuilder();
    }

    /**
     * Returns the PVStructure wrapped by this instance.
     *
     * @return the PVStructure wrapped by this instance
     */
    public PVStructure getPVStructure()
    {
        return pvNTTable;
    }

    /**
     * Returns the value field.
     *
     * @return the PVScalar for the value field
     */
    public PVStructure getValue()
    {
        return pvValue;
    }

    /**
     * Returns the labels field.
     *
     * @return the labels field
     */
    public PVStringArray getLabels()
    {
       return pvNTTable.getSubField(PVStringArray.class, "labels");
    }

    /**
     * Returns the column names for the table.
     *
     * For each name, calling getColumn should return the column, which should not be null.
     * @return the column names.
     */
    public String[] getColumnNames()
    {
       return pvValue.getStructure().getFieldNames();
    }

    /**
     * Returns the the column with the specified colum name.
     *
     * @param columnName the name of the column.
     * @return the field for the column or null if column does not exist.
     */
    public PVScalarArray getColumn(String columnName)
    {
       return pvValue.getSubField(PVScalarArray.class, columnName);
    }

    /**
     * Returns the the column with the specified colum name of a
     * specified type (e.g. PVDoubleArray).
     *
     * @param <T> the expected type of the column
     * @param c class object modeling the class T
     * @param columnName the name of the column
     * @return the field for the column or null if the column does not exist or the field is not of <code>c</code> type.
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
     * Returns the descriptor field.
     *
     * @return the descriptor field or null if no such field
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
     * Constructor.
     *
     * @param pvStructure the PVStructure to be wrapped
     */
    NTTable(PVStructure pvStructure)
    {
        pvNTTable = pvStructure;
        pvValue = pvNTTable.getSubField(PVStructure.class, "value");
    }

    private PVStructure pvNTTable;
    private PVStructure pvValue;
}

