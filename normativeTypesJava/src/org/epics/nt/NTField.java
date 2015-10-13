/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.nt;

import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.Scalar;
import org.epics.pvdata.pv.ScalarArray;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.StandardField;
import org.epics.pvdata.pv.StructureArray;
import org.epics.pvdata.pv.Structure;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVScalar;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.StandardFieldFactory;
import org.epics.pvdata.property.PVTimeStamp;
import org.epics.pvdata.property.PVAlarm;
import org.epics.pvdata.property.PVDisplay;
import org.epics.pvdata.property.PVControl;


/**
 * Convenience Class for introspection fields of a Normative Type.
 *
 * @author dgh
 * 
 */
public class NTField {

    /**
     * Get the single implementation of this class.
     *
     * @return the implementation
     */
    public static NTField get() { return ntstructureField; }

    /**
     * Is field an enumerated structure?
     *
     * @param field the field to test
     * @return (false,true) if field (is not,is) an enumerated structure
     */
    public boolean isEnumerated(Field field)
    {
        try {
        Structure structure = (Structure)field;
        if (structure == null)
            return false;

        if (!structure.getID().equals("enum_t"))
            return false;

        Field[] fields = structure.getFields();
        String[] names = structure.getFieldNames();
        int n = fields.length;
        if (n<2) return false;

        if (!checkScalar(fields[0], names[0], "index", ScalarType.pvInt))
            return false;

        if (!checkScalarArray(fields[1], names[1], "choices", ScalarType.pvString))
            return false;
        }
        catch (ClassCastException e) {
            return false;
        }
        return true;
    }

    /**
     * Is field a timeStamp structure?
     *
     * @param field the field to test
     * @return (false,true) if field (is not,is) a timeStamp structure
     */
    public boolean isTimeStamp(Field field)
    {
        try {
        Structure structure = (Structure)field;
        if (structure == null)
            return false;

        if (!structure.getID().equals("time_t"))
            return false;

        Field[] fields = structure.getFields();
        String[] names = structure.getFieldNames();
        int n = fields.length;
        if (n < 3) return false;

        if (!checkScalar(fields[0], names[0], "secondsPastEpoch", ScalarType.pvLong))
            return false;

        if (!checkScalar(fields[1], names[1], "nanoseconds", ScalarType.pvInt))
            return false;

        if (!checkScalar(fields[2], names[2], "userTag", ScalarType.pvInt))
            return false;
        }
        catch (ClassCastException e) {
            return false;
        }
        return true;
    }

    /**
     * Is field an alarm structure?
     *
     * @param field the field to test
     * @return (false,true) if field (is not,is) an alarm structure
     */
    public boolean isAlarm(Field field)
    {
        try {
        Structure structure = (Structure)field;
        if (structure == null)
            return false;

        if (!structure.getID().equals("alarm_t"))
            return false;

        Field[] fields = structure.getFields();
        String[] names = structure.getFieldNames();
        int n = fields.length;

        if (n < 3) return false;

        if (!checkScalar(fields[0], names[0], "severity", ScalarType.pvInt))
            return false;

        if (!checkScalar(fields[1], names[1], "status", ScalarType.pvInt))
            return false;

        if (!checkScalar(fields[2], names[2], "message", ScalarType.pvString))
            return false;
        }
        catch (ClassCastException e) {
            return false;
        }
        return true;
    }

    /**
     * Is field a display structure?
     *
     * @param field the field to test
     * @return (false,true) if field (is not,is) a display structure
     */
    public boolean isDisplay(Field field)
    {
        try {
        Structure structure = (Structure)field;
        if (structure == null)
            return false;

        if (!structure.getID().equals("display_t"))
            return false;

        Field[] fields = structure.getFields();
        String[] names = structure.getFieldNames();
        int n = fields.length;
        if (n < 5) return false;

        if (!checkScalar(fields[0], names[0], "limitLow", ScalarType.pvDouble))
            return false;

        if (!checkScalar(fields[1], names[1], "limitHigh", ScalarType.pvDouble))
            return false;

        if (!checkScalar(fields[2], names[2], "description", ScalarType.pvString))
            return false;

        if (!checkScalar(fields[3], names[3], "format", ScalarType.pvString))
            return false;

        if (!checkScalar(fields[4], names[4], "units", ScalarType.pvString))
            return false;
        }
        catch (ClassCastException e) {
            return false;
        }
        return true;
    }

    /**
     * Is field an alarmLimit structure.
     *
     * @param field the field to test
     * @return (false,true) if field (is not,is) an alarmLimit structure
     */
    public boolean isAlarmLimit(Field field)
    {
        try {
        Structure structure = (Structure)field;
        if (structure == null)
            return false;

        if (!structure.getID().equals("alarmLimit_t"))
            return false;

        Field[] fields = structure.getFields();
        String[] names = structure.getFieldNames();
        int n = fields.length;
        if (n < 10) return false;

        if (!checkScalar(fields[0], names[0], "active", ScalarType.pvBoolean))
            return false;

        if (!checkScalar(fields[1], names[1], "lowAlarmLimit", ScalarType.pvDouble))
            return false;

        if (!checkScalar(fields[2], names[2], "lowWarningLimit", ScalarType.pvDouble))
            return false;

        if (!checkScalar(fields[3], names[3], "highWarningLimit", ScalarType.pvDouble))
            return false;

        if (!checkScalar(fields[4], names[4], "highAlarmLimit", ScalarType.pvDouble))
            return false;

        if (!checkScalar(fields[5], names[5], "lowAlarmSeverity", ScalarType.pvInt))
            return false;

        if (!checkScalar(fields[6], names[6], "lowWarningSeverity", ScalarType.pvInt))
            return false;

        if (!checkScalar(fields[7], names[7], "highWarningSeverity", ScalarType.pvInt))
            return false;

        if (!checkScalar(fields[8], names[8], "highAlarmSeverity", ScalarType.pvInt))
            return false;

        if (!checkScalar(fields[9], names[9], "hysteresis", ScalarType.pvDouble))
            return false;
        }
        catch (ClassCastException e) {
            return false;
        }
        return true;
    }

    /**
     * Is field a control structure.
     *
     * @param field the field to test
     * @return (false,true) if field (is not,is) a control structure
     */
    public boolean isControl(Field field)
    {
        try {
        Structure structure = (Structure)field;
        if (structure == null)
            return false;

        if (!structure.getID().equals("control_t"))
            return false;

        Field[] fields = structure.getFields();
        String[] names = structure.getFieldNames();
        int n = fields.length;
        if (n < 3) return false;

        if (!checkScalar(fields[0], names[0], "limitLow", ScalarType.pvDouble))
            return false;

        if (!checkScalar(fields[1], names[1], "limitHigh", ScalarType.pvDouble))
            return false;

        if (!checkScalar(fields[2], names[2], "minStep", ScalarType.pvDouble))
            return false;
        }
        catch (ClassCastException e) {
            return false;
        }
        return true;
    }

    /**
     * Create an enumerated structure.
     *
     * @return an enumerated structure
     */
    public Structure createEnumerated()
    {
        return standardField.enumerated();
    }

    /**
     * Create a timeStamp structure.
     *
     * @return a timeStamp structure
     */
    public Structure createTimeStamp()
    {
        return standardField.timeStamp();
    }
    /**
     * Create an alarm structure.
     *
     * @return an alarm structure
     */
    public Structure createAlarm()
    {
        return standardField.alarm();
    }

    /**
     * Create an alarmLimit structure.
     *
     * @return an alarmLimit structure
     */
    public Structure createAlarmLimit()
    {
        return fieldCreate.createFieldBuilder().
           setId("alarmLimit_t").
           add("active", ScalarType.pvBoolean).
           add("lowAlarmLimit", ScalarType.pvDouble). 
           add("lowWarningLimit", ScalarType.pvDouble). 
           add("highWarningLimit", ScalarType.pvDouble). 
           add("highAlarmLimit", ScalarType.pvDouble). 
           add("lowAlarmSeverity", ScalarType.pvInt). 
           add("lowWarningSeverity", ScalarType.pvInt). 
           add("highWarningSeverity", ScalarType.pvInt).
           add("highAlarmSeverity", ScalarType.pvInt).
           add("hysteresis", ScalarType.pvDouble).
           createStructure();
    }

    /**
     * Create a display structure.
     *
     * @return a display structure
     */

    public Structure createDisplay()
    {
        return standardField.display();
    }

    /**
     * Create a control structure.
     *
     * @return a control structure
     */
    public Structure createControl()
    {    
        return standardField.control();
    }

    /**
     * Create an array of enumerated structures.
     *
     * @return an array of enumerated structures
     */
    public StructureArray createEnumeratedArray()
    {
        return fieldCreate.createStructureArray(createEnumerated());
    }

    /**
     * Create an array of timeStamp structures.
     *
     * @return an array of timeStamp structures
     */
    public StructureArray createTimeStampArray()
    {
        Structure st = createTimeStamp();
        return fieldCreate.createStructureArray(st);
    }

    /**
     * Create an array of alarm structures.
     *
     * @return an array of alarm structures
     */
    public StructureArray createAlarmArray()
    {
        Structure st = createAlarm();
        return fieldCreate.createStructureArray(st);
    }

    private boolean checkScalar(Field field, String fieldName, String name, ScalarType scalarType)
    {
        if (!fieldName.equals(name))
            return false;

        Scalar s = (Scalar)field;
        if (s == null || s.getScalarType() != scalarType)
            return false;

        return true;
    }

    private boolean checkScalarArray(Field field, String fieldName, String name, ScalarType scalarType)
    {
        if (!fieldName.equals(name))
            return false;

        ScalarArray s = (ScalarArray)field;
        if (s == null || s.getElementType() != scalarType)
            return false;

        return true;
    }

    private NTField() {}
    private FieldCreate fieldCreate = FieldFactory.getFieldCreate();
    private StandardField standardField = StandardFieldFactory.getStandardField();
    static private NTField ntstructureField = new NTField();
};



