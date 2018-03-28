/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.nt;

import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.Scalar;
import org.epics.pvdata.pv.ScalarArray;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.StandardField;
import org.epics.pvdata.pv.StandardPVField;
import org.epics.pvdata.pv.StructureArray;
import org.epics.pvdata.pv.Structure;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVScalar;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.PVStructureArray;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.StandardFieldFactory;
import org.epics.pvdata.factory.StandardPVFieldFactory;
import org.epics.pvdata.property.PVTimeStamp;
import org.epics.pvdata.property.PVAlarm;
import org.epics.pvdata.property.PVDisplay;
import org.epics.pvdata.property.PVControl;

/**
 * Convenience Class for data fields of a Normative Type.
 *
 * @author dgh
 * 
 */
public class PVNTField
{
    /**
     * Returns the single implementation of this class.
     *
     * @return the implementation
     */
    public static PVNTField get() { return pvntField; }

    /**
     * Creates an enumerated PVStructure.
     *
     * @param choices the array of choices
     * @return an enumerated PVStructure
     */
    public PVStructure createEnumerated(
        String[] choices)
    {
        return standardPVField.enumerated(choices);
    }

    /**
     * Creates a timeStamp PVStructure.
     *
     * @return a timeStamp PVStructure
     */
    public PVStructure createTimeStamp()
    {
        Structure timeStamp = ntfield.createTimeStamp();
        return pvDataCreate.createPVStructure(timeStamp);
    }

    /**
     * Creates an alarm PVStructure.
     *
     * @return an alarm PVStructure
     */
    public PVStructure createAlarm()
    {
        Structure alarm = ntfield.createAlarm();
        return pvDataCreate.createPVStructure(alarm);
    }

    /**
     * Creates an alarmLimit PVStructure.
     *
     * @return an alarmLimit PVStructure
     */
    public PVStructure createAlarmLimit()
    {
        Structure alarmLimit = ntfield.createAlarmLimit();
        return pvDataCreate.createPVStructure(alarmLimit);
    }

    /**
     * Creates a display PVStructure.
     *
     * @return a display PVStructure
     */
    public PVStructure createDisplay()
    {
        Structure display = ntfield.createDisplay();
        return pvDataCreate.createPVStructure(display);
    }

    /**
     * Creates a control PVStructure.
     *
     * @return a control PVStructure
     */
    public PVStructure createControl()
    {
        Structure control = ntfield.createControl();
        return pvDataCreate.createPVStructure(control);
    }

    /**
     * Creates an enumerated PVStructureArray.
     *
     * @return an enumerated PVStructureArray
     */
    public PVStructureArray createEnumeratedArray()
    {
        StructureArray sa =
            ntfield.createEnumeratedArray();
        return pvDataCreate.createPVStructureArray(sa);
    }

    /**
     * Creates a timeStamp PVStructureArray.
     *
     * @return a timeStamp PVStructureArray
     */
    public PVStructureArray createTimeStampArray()
    {
        StructureArray sa =
            ntfield.createTimeStampArray();
        return pvDataCreate.createPVStructureArray(sa);
    }

    /**
     * Creates an alarm PVStructureArray.
     *
     * @return an alarm PVStructureArray
     */
    public PVStructureArray createAlarmArray()
    {
        StructureArray sa =
            ntfield.createAlarmArray();
        return pvDataCreate.createPVStructureArray(sa);
    }

    private PVNTField() {}
    private PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
    private StandardField standardField = StandardFieldFactory.getStandardField();
    private StandardPVField standardPVField = StandardPVFieldFactory.getStandardPVField();
    private NTField ntfield = NTField.get();

    static private PVNTField pvntField = new PVNTField();
}

