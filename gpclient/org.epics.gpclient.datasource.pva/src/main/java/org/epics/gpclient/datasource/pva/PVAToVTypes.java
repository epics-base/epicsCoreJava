/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.epics.gpclient.datasource.pva;

import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.epics.pvdata.factory.ConvertFactory;
import org.epics.pvdata.pv.Convert;
import org.epics.pvdata.pv.DoubleArrayData;
import org.epics.pvdata.pv.PVDoubleArray;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVInt;
import org.epics.pvdata.pv.PVLong;
import org.epics.pvdata.pv.PVScalar;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStructure;
import org.epics.util.array.ArrayDouble;
import org.epics.util.stats.Range;
import org.epics.util.text.NumberFormats;
import org.epics.vtype.Alarm;
import org.epics.vtype.AlarmSeverity;
import org.epics.vtype.AlarmStatus;
import org.epics.vtype.Display;
import org.epics.vtype.Time;
import org.epics.vtype.VByte;
import org.epics.vtype.VDouble;
import org.epics.vtype.VDoubleArray;
import org.epics.vtype.VFloat;
import org.epics.vtype.VInt;
import org.epics.vtype.VLong;
import org.epics.vtype.VShort;
import org.epics.vtype.VUByte;
import org.epics.vtype.VUInt;
import org.epics.vtype.VULong;
import org.epics.vtype.VUShort;

/**
 *
 * @author carcassi
 */
public class PVAToVTypes {
    

    public static Time timeOf(PVStructure pvField) {
        // timeStamp_t
        PVStructure timeStampStructure = (pvField != null) ? pvField.getStructureField("timeStamp") : null;
        if (timeStampStructure != null) {
            Instant timestamp;
            boolean timeValid;
            Integer timeUserTag;

            PVLong secsField = timeStampStructure.getLongField("secondsPastEpoch");
            PVInt nanosField = timeStampStructure.getIntField("nanoseconds");

            if (secsField != null && nanosField != null) {
                timestamp = Instant.ofEpochSecond(secsField.get(), nanosField.get());
                timeValid = true;
            } else {
                timestamp = Instant.ofEpochSecond(0);
                timeValid = false;
            }

            PVInt userTagField = timeStampStructure.getIntField("userTag");
            if (userTagField != null) {
                timeUserTag = userTagField.get();
            } else {
                timeUserTag = null;
            }

            return Time.of(timestamp, timeUserTag, timeValid);
        } else {
            return Time.now();
        }
    }
    
    // Conversion from pva AlarmSeverity to vtypes AlarmSeverity
    private static final List<AlarmSeverity> FROM_PVA_SEVERITY = Arrays.asList(AlarmSeverity.NONE,
            AlarmSeverity.MINOR,
            AlarmSeverity.MAJOR,
            AlarmSeverity.INVALID,
            AlarmSeverity.UNDEFINED);

    // Conversion from pva AlarmStatus to vtypes AlarmStatus
    private static final List<AlarmStatus> FROM_PVA_STATUS = Arrays.asList(AlarmStatus.NONE,
            AlarmStatus.DEVICE,
            AlarmStatus.DRIVER,
            AlarmStatus.RECORD,
            AlarmStatus.DB,
            AlarmStatus.CONF,
            AlarmStatus.UNDEFINED,
            AlarmStatus.CLIENT);

    public static Alarm alarmOf(PVStructure pvField, boolean disconnected) {
        if (disconnected) {
            return Alarm.disconnected();
        }
        
        PVStructure alarmStructure = (pvField != null) ? pvField.getStructureField("alarm") : null;
        if (alarmStructure != null) {
            AlarmSeverity alarmSeverity;
            AlarmStatus alarmStatus;
            String name;
            
            PVInt severityField = alarmStructure.getIntField("severity");
            if (severityField == null) {
                alarmSeverity = AlarmSeverity.UNDEFINED;
            } else {
                alarmSeverity = FROM_PVA_SEVERITY.get(severityField.get());
            }

            PVInt statusField = alarmStructure.getIntField("status");
            if (statusField == null) {
                alarmStatus = AlarmStatus.UNDEFINED;
            } else {
                alarmStatus = FROM_PVA_STATUS.get(statusField.get());
            }

            PVString messageField = alarmStructure.getStringField("message");
            if (messageField == null) {
                name = "";
            } else {
                name = messageField.get();
            }
            
            return Alarm.of(alarmSeverity, alarmStatus, name);

        } else {
            return Alarm.none();
        }
    }
    
    public static Display displayOf(PVStructure pvField) {
        if (pvField == null) {
            return Display.none();
        }
        
        Range controlRange;
        Range displayRange;
        Range alarmRange;
        Range warningRange;
        NumberFormat format;
        String units;
        
        // display_t
        PVStructure displayStructure = pvField.getStructureField("display");
        displayRange = rangeOf(displayStructure, "limitLow", "limitHigh");
        if (displayStructure != null) {
            PVString formatField = displayStructure.getStringField("format");
            if (formatField == null) {
                format = Display.defaultNumberFormat();
            } else {
                format = NumberFormats.printfFormat(formatField.get());
            }

            PVString unitsField = displayStructure.getStringField("units");
            if (unitsField == null || unitsField.get() == null) {
                units = Display.defaultUnits();
            } else {
                units = unitsField.get();
            }
        } else {
            format = Display.defaultNumberFormat();
            units = Display.defaultUnits();
        }

        // control_t
        controlRange = rangeOf(pvField.getStructureField("control"), "limitLow", "limitHigh");

        // valueAlarm_t
        PVStructure valueAlarmStructure = pvField.getStructureField("valueAlarm");
        warningRange = rangeOf(valueAlarmStructure, "lowWarningLimit", "highWarningLimit");
        alarmRange = rangeOf(valueAlarmStructure, "lowAlarmLimit", "highAlarmLimit");
        
        return Display.of(displayRange, alarmRange, warningRange, controlRange, units, format);
    }
    
    private static final Convert convert = ConvertFactory.getConvert();
    
    private static double doubleValueOf(PVStructure structure, String fieldName, Double defaultValue) {
        PVField field = structure.getSubField(fieldName);
        if (field instanceof PVScalar) {
            return convert.toDouble((PVScalar) field);
        } else {
            return defaultValue;
        }
    }
    
    private static Range rangeOf(PVStructure pvStructure, String lowValueName, String highValueName) {
        if (pvStructure != null) {
            return Range.of(doubleValueOf(pvStructure, lowValueName, Double.NaN),
                    doubleValueOf(pvStructure, highValueName, Double.NaN));
        } else {
            return Range.undefined();
        }
    }

    public static VDouble vDoubleOf(PVStructure pvField, boolean disconnected) {
        return vDoubleOf(pvField.getSubField("value"), pvField, disconnected);
    }

    public static VDouble vDoubleOf(PVField pvField, PVStructure pvMetadata, boolean disconnected) {
        if (pvField instanceof PVScalar) {
            return VDouble.of(convert.toDouble((PVScalar)pvField), alarmOf(pvMetadata, disconnected), timeOf(pvMetadata), displayOf(pvMetadata));
        } else {
            return null;
        }
    }

    public static VFloat vFloatOf(PVStructure pvField, boolean disconnected) {
        return vFloatOf(pvField.getSubField("value"), pvField, disconnected);
    }

    public static VFloat vFloatOf(PVField pvField, PVStructure pvMetadata, boolean disconnected) {
        if (pvField instanceof PVScalar) {
            return VFloat.of(convert.toFloat((PVScalar)pvField), alarmOf(pvMetadata, disconnected), timeOf(pvMetadata), displayOf(pvMetadata));
        } else {
            return null;
        }
    }

    public static VULong vULongOf(PVStructure pvField, boolean disconnected) {
        return vULongOf(pvField.getSubField("value"), pvField, disconnected);
    }

    public static VULong vULongOf(PVField pvField, PVStructure pvMetadata, boolean disconnected) {
        if (pvField instanceof PVScalar) {
            return VULong.of(convert.toLong((PVScalar)pvField), alarmOf(pvMetadata, disconnected), timeOf(pvMetadata), displayOf(pvMetadata));
        } else {
            return null;
        }
    }

    public static VLong vLongOf(PVStructure pvField, boolean disconnected) {
        return vLongOf(pvField.getSubField("value"), pvField, disconnected);
    }

    public static VLong vLongOf(PVField pvField, PVStructure pvMetadata, boolean disconnected) {
        if (pvField instanceof PVScalar) {
            return VLong.of(convert.toLong((PVScalar)pvField), alarmOf(pvMetadata, disconnected), timeOf(pvMetadata), displayOf(pvMetadata));
        } else {
            return null;
        }
    }

    public static VUInt vUIntOf(PVStructure pvField, boolean disconnected) {
        return vUIntOf(pvField.getSubField("value"), pvField, disconnected);
    }

    public static VUInt vUIntOf(PVField pvField, PVStructure pvMetadata, boolean disconnected) {
        if (pvField instanceof PVScalar) {
            return VUInt.of(convert.toInt((PVScalar)pvField), alarmOf(pvMetadata, disconnected), timeOf(pvMetadata), displayOf(pvMetadata));
        } else {
            return null;
        }
    }

    public static VInt vIntOf(PVStructure pvField, boolean disconnected) {
        return vIntOf(pvField.getSubField("value"), pvField, disconnected);
    }

    public static VInt vIntOf(PVField pvField, PVStructure pvMetadata, boolean disconnected) {
        if (pvField instanceof PVScalar) {
            return VInt.of(convert.toInt((PVScalar)pvField), alarmOf(pvMetadata, disconnected), timeOf(pvMetadata), displayOf(pvMetadata));
        } else {
            return null;
        }
    }

    public static VUShort vUShortOf(PVStructure pvField, boolean disconnected) {
        return vUShortOf(pvField.getSubField("value"), pvField, disconnected);
    }

    public static VUShort vUShortOf(PVField pvField, PVStructure pvMetadata, boolean disconnected) {
        if (pvField instanceof PVScalar) {
            return VUShort.of(convert.toShort((PVScalar)pvField), alarmOf(pvMetadata, disconnected), timeOf(pvMetadata), displayOf(pvMetadata));
        } else {
            return null;
        }
    }

    public static VShort vShortOf(PVStructure pvField, boolean disconnected) {
        return vShortOf(pvField.getSubField("value"), pvField, disconnected);
    }

    public static VShort vShortOf(PVField pvField, PVStructure pvMetadata, boolean disconnected) {
        if (pvField instanceof PVScalar) {
            return VShort.of(convert.toShort((PVScalar)pvField), alarmOf(pvMetadata, disconnected), timeOf(pvMetadata), displayOf(pvMetadata));
        } else {
            return null;
        }
    }

    public static VUByte vUByteOf(PVStructure pvField, boolean disconnected) {
        return vUByteOf(pvField.getSubField("value"), pvField, disconnected);
    }

    public static VUByte vUByteOf(PVField pvField, PVStructure pvMetadata, boolean disconnected) {
        if (pvField instanceof PVScalar) {
            return VUByte.of(convert.toByte((PVScalar)pvField), alarmOf(pvMetadata, disconnected), timeOf(pvMetadata), displayOf(pvMetadata));
        } else {
            return null;
        }
    }

    public static VByte vByteOf(PVStructure pvField, boolean disconnected) {
        return vByteOf(pvField.getSubField("value"), pvField, disconnected);
    }

    public static VByte vByteOf(PVField pvField, PVStructure pvMetadata, boolean disconnected) {
        if (pvField instanceof PVScalar) {
            return VByte.of(convert.toByte((PVScalar)pvField), alarmOf(pvMetadata, disconnected), timeOf(pvMetadata), displayOf(pvMetadata));
        } else {
            return null;
        }
    }

    public static VDoubleArray vDoubleArrayOf(PVStructure pvField, boolean disconnected) {
        return vDoubleArrayOf(pvField.getSubField("value"), pvField, disconnected);
    }

    public static VDoubleArray vDoubleArrayOf(PVField pvField, PVStructure pvMetadata, boolean disconnected) {
        if (pvField instanceof PVDoubleArray) {
            PVDoubleArray valueField = (PVDoubleArray) pvField;
            DoubleArrayData data = new DoubleArrayData();
            valueField.get(0, valueField.getLength(), data);
            return VDoubleArray.of(ArrayDouble.of(data.data), alarmOf(pvMetadata, disconnected), timeOf(pvMetadata), displayOf(pvMetadata));
        } else {
            return null;
        }
    }

}
