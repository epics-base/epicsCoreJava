/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.nt;

import junit.framework.TestCase;

import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.FieldBuilder;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVDouble;
import org.epics.pvdata.pv.PVInt;
import org.epics.pvdata.pv.PVLong;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.property.*;

/**
 * Base class for testing NT wrapper classes.
 * @author dgh
 *
 */
public class NTTestBase extends TestCase
{
    static FieldCreate fieldCreate = FieldFactory.getFieldCreate();
    static PVDataCreate dataCreate = PVDataFactory.getPVDataCreate();
    static NTField ntField = NTField.get();

    // test attaching timeStamps

    public static void testAttachTimeStamp(HasTimeStamp type, boolean expected)
    {
        PVTimeStamp pvTimeStamp = PVTimeStampFactory.create();
        boolean attached = type.attachTimeStamp(pvTimeStamp);
        assertEquals(expected, attached);

        if (expected && attached)
        {
            TimeStamp timeStamp = TimeStampFactory.create();
            long secondsPastEpoch = 1439251159;
            int nanoseconds = 851129075;
            int userTag = 42;
            timeStamp.put(secondsPastEpoch,nanoseconds);
            timeStamp.setUserTag(userTag);
            pvTimeStamp.set(timeStamp);

            long secondsPastEpoch2 = type.getTimeStamp().
                getSubField(PVLong.class, "secondsPastEpoch").get();
            int nanoseconds2 = type.getTimeStamp().
                getSubField(PVInt.class, "nanoseconds").get();
            int userTag2 = type.getTimeStamp().
                getSubField(PVInt.class, "userTag").get();

            assertEquals(secondsPastEpoch,secondsPastEpoch2);
            assertEquals(nanoseconds,nanoseconds2);
            assertEquals(userTag,userTag2);
        }
    }

    // test attaching alarms

    public static void testAttachAlarm(HasAlarm type, boolean expected)
    {
        PVAlarm pvAlarm = PVAlarmFactory.create();
        boolean attached = type.attachAlarm(pvAlarm);
        assertEquals(expected, attached);

        if (expected && attached)
        {
            Alarm alarm = new Alarm();
            int severity  = 1;
            int status   = 3;
            String message = "STATE_ALARM";

            alarm.setSeverity(AlarmSeverity.getSeverity(severity));
            alarm.setStatus(AlarmStatus.getStatus(status));
            alarm.setMessage(message);

            pvAlarm.set(alarm);

            int severity2  = type.getAlarm().
                getSubField(PVInt.class, "severity").get();
            int status2  = type.getAlarm().
                getSubField(PVInt.class, "status").get();
            String message2 = type.getAlarm().
                getSubField(PVString.class, "message").get();

            assertEquals(severity,severity2);
            assertEquals(status,status2);
            assertEquals(message,message2);
        }
    }

    // test attaching displays

    public static void testAttachDisplay(HasDisplay type, boolean expected)
    {
        PVDisplay pvDisplay = PVDisplayFactory.create();
        boolean attached = type.attachDisplay(pvDisplay);
        assertEquals(expected, attached);

        if (expected && attached)
        {
            Display display = new Display();

            pvDisplay.set(display);

            double low2 = type.getDisplay().
                getSubField(PVDouble.class, "limitLow").get();
            double high2 = type.getDisplay().
                getSubField(PVDouble.class, "limitHigh").get();
            String format2 = type.getDisplay().
                getSubField(PVString.class, "format").get();
            String units2 = type.getDisplay().
                getSubField(PVString.class, "units").get();
            String description2 = type.getDisplay().
                getSubField(PVString.class, "description").get();
        }
    }


    // test attaching controls

    public static void testAttachControl(HasControl type, boolean expected)
    {
        PVControl pvControl = PVControlFactory.create();
        boolean attached = type.attachControl(pvControl);
        assertEquals(expected, attached);

        if (expected && attached)
        {
            Control control = new Control();

            double low = -1.0;
            double high = 1.0;

            control.setLow(low);
            control.setHigh(high);

            pvControl.set(control);

            double low2 = type.getControl().
            getSubField(PVDouble.class, "limitLow").get();
            double high2 = type.getControl().
                getSubField(PVDouble.class, "limitHigh").get();

            assertEquals(low,low2);
            assertEquals(high,high2);
        }
    }



    protected static boolean find(String s, String[] sa)
    {
        if (sa == null) return false;

        for (String s2: sa)
        {
            if (s2.equals(s)) return true;
        }

        return false;
    }
}

