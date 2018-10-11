/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.property;


/**
 * AlarmStatus definitions.
 * @author mrk
 *
 */
public enum AlarmStatus {
    /**
     * No alarm.
     */
    NONE,
    /**
     * An error conditioned generated by the hardware.
     */
    DEVICE,

    /**
     * An error conditioned raised by the driver (e.g. the device is not responding,
     * cannot write to it, ...)
     */
    DRIVER,

    /**
     * An error generated as part of the record calculation (e.g. alarm limits,
     * state alarm, error in the calculation)
     */
    RECORD,

    /**
     * An error generated by the interaction of multiple records.
     */
    DB,

    /**
     * An error generated by an error in configuration of one or multiple records.
     */
    CONF,

    /**
     * The status for a record that was never processed.
     */
    UNDEFINED,

    /**
     * An error generated by the client (e.g channel not found, disconnected, ...)
     */
    CLIENT;
    
    /**
     * Get the alarm status.
     *
     * @param value the integer value
     * @return The alarm status
     */
    public static AlarmStatus getStatus(int value) {
        switch(value) {
        case 0: return AlarmStatus.NONE;
        case 1: return AlarmStatus.DEVICE;
        case 2: return AlarmStatus.DRIVER;
        case 3: return AlarmStatus.RECORD;
        case 4: return AlarmStatus.DB;
        case 5: return AlarmStatus.CONF;
        case 6: return AlarmStatus.UNDEFINED;
        case 7: return AlarmStatus.CLIENT;
        }
        throw new IllegalArgumentException("AlarmStatus.getStatus("
            + ((Integer)value).toString() + ") is not a valid AlarmStatus");
    }
    
    private static final String[] alarmStatusNames = {
        "NONE","DEVICE","DRIVER","RECORD","DB","CONF","UNDEFINED","CLIENT"
    };

    /**
     * Get the names associated with each status.
     *
     * @return the array of names
     */
    public static String[] getStatusNames() { return alarmStatusNames;}
}