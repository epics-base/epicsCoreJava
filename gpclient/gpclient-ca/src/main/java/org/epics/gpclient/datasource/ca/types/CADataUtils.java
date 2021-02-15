/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient.datasource.ca.types;

import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.PRECISION;
import gov.aps.jca.dbr.Severity;
import gov.aps.jca.dbr.TimeStamp;
import org.epics.util.text.NumberFormats;
import org.epics.vtype.Alarm;
import org.epics.vtype.AlarmSeverity;
import org.epics.vtype.AlarmStatus;
import org.epics.vtype.Time;
import org.joda.time.Instant;

import java.text.NumberFormat;

public class CADataUtils {

    /**
     * Converts an alarm severity from JCA to {@link Alarm}.
     *
     * @param severity the JCA severity
     * @return the VData severity
     */
    public static Alarm fromEpics(Severity severity) {

        if (Severity.NO_ALARM.isEqualTo(severity)) {
            return Alarm.none();
        } else if (Severity.MINOR_ALARM.isEqualTo(severity)) {
            return Alarm.of(AlarmSeverity.MINOR, AlarmStatus.RECORD, "");
        } else if (Severity.MAJOR_ALARM.isEqualTo(severity)) {
            return Alarm.of(AlarmSeverity.MAJOR, AlarmStatus.RECORD, "");
        } else if (Severity.INVALID_ALARM.isEqualTo(severity)) {
            return Alarm.of(AlarmSeverity.INVALID, AlarmStatus.RECORD, "");
        } else {
            return Alarm.of(AlarmSeverity.UNDEFINED, AlarmStatus.UNDEFINED, "");
        }
    }

    /**
     * Constant to convert epics seconds to UNIX seconds. It counts the number
     * of seconds for 20 years, 5 of which leap years. It does _not_ count the
     * number of leap seconds (which should have been 15).
     */
    static long TS_EPOCH_SEC_PAST_1970 = 631152000; //7305*86400;

    /**
     * Converts a JCA timestamp to an epics.util timestamp.
     *
     * @param timeStamp the epics timestamp
     * @return a new epics.util timestamp
     */
    public static Time timestampOf(TimeStamp timeStamp) {
        if (timeStamp == null)
            return null;
        return Time.of(Instant.ofEpochSecond(timeStamp.secPastEpoch() + TS_EPOCH_SEC_PAST_1970)
                .plus(timeStamp.nsec() * 1000));
    }

    public static NumberFormat getFormat(DBR metadata, boolean honorZeroPrecision) {
        int precision = -1;
        if (metadata instanceof PRECISION) {
            precision = ((PRECISION) metadata).getPrecision();
        }

        // If precision is 0 or less, we assume full precision
        if (precision < 0) {
            return NumberFormats.toStringFormat();
        } else if (precision == 0) {
            if (honorZeroPrecision) {
                return NumberFormats.precisionFormat(0);
            } else {
                return NumberFormats.toStringFormat();
            }
        } else {
            return NumberFormats.precisionFormat(precision);
        }
    }

}
