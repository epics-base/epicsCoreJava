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
     * get the single implementation of this class.
     *
     * @return the implementation
     */
    public static PVNTField get() { return pvntField; }

    /**
     * Create an enumerated PVStructure.
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
     * Create a timeStamp PVStructure.
     *
     * @return a timeStamp PVStructure
     */
    public PVStructure createTimeStamp()
    {
        Structure timeStamp = ntfield.createTimeStamp();
        return pvDataCreate.createPVStructure(timeStamp);
    }

    /**
     * Create an alarm PVStructure.
     *
     * @return an alarm PVStructure
     */
    public PVStructure createAlarm()
    {
        Structure alarm = ntfield.createAlarm();
        return pvDataCreate.createPVStructure(alarm);
    }

    /**
     * Create an alarmLimit PVStructure.
     *
     * @return an alarmLimit PVStructure
     */
    public PVStructure createAlarmLimit()
    {
        Structure alarmLimit = ntfield.createAlarmLimit();
        return pvDataCreate.createPVStructure(alarmLimit);
    }

    /**
     * Create a display PVStructure.
     *
     * @return a display PVStructure
     */
    public PVStructure createDisplay()
    {
        Structure display = ntfield.createDisplay();
        return pvDataCreate.createPVStructure(display);
    }

    /**
     * Create a control PVStructure.
     *
     * @return a control PVStructure
     */
    public PVStructure createControl()
    {
        Structure control = ntfield.createControl();
        return pvDataCreate.createPVStructure(control);
    }

    /**
     * Create an enumerated PVStructureArray.
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
     * Create a timeStamp PVStructureArray.
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
     * Create an alarm PVStructureArray.
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

