/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient.datasource.ca.types;

import gov.aps.jca.dbr.*;
import org.epics.gpclient.ReadCollector;
import org.epics.gpclient.datasource.ca.CAConnectionPayload;
import org.epics.util.array.*;
import org.epics.util.stats.Range;
import org.epics.vtype.*;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author carcassi
 */
public class CAVTypeAdapterSet implements CATypeAdapterSet {

    public Set<CATypeAdapter> getAdapters() {
        return converters;
    }

    // DBR_TIME_Float -> VFloat
    final static CATypeAdapter DBRFloatToVFloat = new CATypeAdapter(VFloat.class, DBR_TIME_Float.TYPE, DBR_CTRL_Double.TYPE, false) {

        @Override
        public Object createValue(DBR rawMessage, DBR rawMetadata, CAConnectionPayload connPayload) {
            DBR_TIME_Float message = (DBR_TIME_Float)rawMessage;
            DBR_CTRL_Double metadata = (DBR_CTRL_Double)rawMetadata;
            Alarm alarm;
            if (!connPayload.isChannelConnected()) {
                alarm = Alarm.disconnected();
            } else {
                alarm = CADataUtils.fromEpics(message.getSeverity());
            }
            Time timestamp;
            if (!connPayload.isChannelConnected()) {
                timestamp = Time.of(connPayload.getEventTime());
            } else {
                timestamp = CADataUtils.timestampOf(message.getTimeStamp());
            }
            NumberFormat format = CADataUtils.getFormat(metadata, connPayload.getCADataSource().isHonorZeroPrecision());
            Display display = Display.of(Range.of(metadata.getLowerDispLimit().doubleValue(), metadata.getUpperDispLimit().doubleValue()),
                                         Range.of(metadata.getLowerAlarmLimit().doubleValue(), metadata.getUpperAlarmLimit().doubleValue()),
                                         Range.of(metadata.getLowerWarningLimit().doubleValue(), metadata.getUpperWarningLimit().doubleValue()),
                                         Range.of(metadata.getLowerCtrlLimit().doubleValue(), metadata.getUpperCtrlLimit().doubleValue()),
                                         metadata.getUnits(),
                                         format);
            return VFloat.of(message.getFloatValue()[0], alarm, timestamp, display);
        }
    };

    // DBR_TIME_Double -> VDouble
    final static CATypeAdapter DBRDoubleToVDouble = new CATypeAdapter(VDouble.class, DBR_TIME_Double.TYPE,
            DBR_CTRL_Double.TYPE, false) {

        @Override
        public Object createValue(DBR rawMessage, DBR rawMetadata, CAConnectionPayload connPayload) {
            DBR_TIME_Double message = (DBR_TIME_Double)rawMessage;
            DBR_CTRL_Double metadata = (DBR_CTRL_Double)rawMetadata;
            Alarm alarm;
            if (!connPayload.isChannelConnected()) {
                alarm = Alarm.disconnected();
            } else {
                alarm = CADataUtils.fromEpics(((DBR_TIME_Double)message).getSeverity());
            }
            Time timestamp;
            if (!connPayload.isChannelConnected()) {
                timestamp = Time.of(connPayload.getEventTime());
            } else {
                timestamp = CADataUtils.timestampOf(((DBR_TIME_Double)message).getTimeStamp());
            }
            NumberFormat format = CADataUtils.getFormat(metadata, connPayload.getCADataSource().isHonorZeroPrecision());
            Display display = Display.of(Range.of(metadata.getLowerDispLimit().doubleValue(), metadata.getUpperDispLimit().doubleValue()),
                                         Range.of(metadata.getLowerAlarmLimit().doubleValue(), metadata.getUpperAlarmLimit().doubleValue()),
                                         Range.of(metadata.getLowerWarningLimit().doubleValue(), metadata.getUpperWarningLimit().doubleValue()),
                                         Range.of(metadata.getLowerCtrlLimit().doubleValue(), metadata.getUpperCtrlLimit().doubleValue()),
                                         metadata.getUnits(),
                                         format);
            return VDouble.of(((DBR_TIME_Double)message).getDoubleValue()[0], alarm, timestamp, display);
        }
    };

    // DBR_TIME_Byte -> VByte
    final static CATypeAdapter DBRByteToVByte = new CATypeAdapter(VByte.class, DBR_TIME_Byte.TYPE, DBR_CTRL_Double.TYPE,
            false) {

        @Override
        public VByte createValue(DBR rawMessage, DBR rawMetadata, CAConnectionPayload connPayload) {
            DBR_TIME_Byte message = (DBR_TIME_Byte)rawMessage;
            DBR_CTRL_Double metadata = (DBR_CTRL_Double)rawMetadata;
            Alarm alarm;
            if (!connPayload.isChannelConnected()) {
                alarm = Alarm.disconnected();
            } else {
                alarm = CADataUtils.fromEpics(((DBR_TIME_Byte)message).getSeverity());
            }
            Time timestamp;
            if (!connPayload.isChannelConnected()) {
                timestamp = Time.of(connPayload.getEventTime());
            } else {
                timestamp = CADataUtils.timestampOf(((DBR_TIME_Byte)message).getTimeStamp());
            }
            NumberFormat format = CADataUtils.getFormat(metadata, connPayload.getCADataSource().isHonorZeroPrecision());
            Display display = Display.of(Range.of(metadata.getLowerDispLimit().doubleValue(), metadata.getUpperDispLimit().doubleValue()),
                                         Range.of(metadata.getLowerAlarmLimit().doubleValue(), metadata.getUpperAlarmLimit().doubleValue()),
                                         Range.of(metadata.getLowerWarningLimit().doubleValue(), metadata.getUpperWarningLimit().doubleValue()),
                                         Range.of(metadata.getLowerCtrlLimit().doubleValue(), metadata.getUpperCtrlLimit().doubleValue()),
                                         metadata.getUnits(),
                                         format);
            return VByte.of(((DBR_TIME_Byte)message).getByteValue()[0], alarm, timestamp, display);
        }
    };

    // DBR_TIME_Short -> VInt
    final static CATypeAdapter DBRShortToVShort = new CATypeAdapter(VShort.class, DBR_TIME_Short.TYPE,
            DBR_CTRL_Double.TYPE, false) {

        @Override
        public VShort createValue(DBR rawMessage, DBR rawMetadata, CAConnectionPayload connPayload) {
            DBR_TIME_Short message = (DBR_TIME_Short)rawMessage;
            DBR_CTRL_Double metadata = (DBR_CTRL_Double)rawMetadata;
            Alarm alarm;
            if (!connPayload.isChannelConnected()) {
                alarm = Alarm.disconnected();
            } else {
                alarm = CADataUtils.fromEpics(message.getSeverity());
            }
            Time timestamp;
            if (!connPayload.isChannelConnected()) {
                timestamp = Time.of(connPayload.getEventTime());
            } else {
                timestamp = CADataUtils.timestampOf(message.getTimeStamp());
            }
            NumberFormat format = CADataUtils.getFormat(metadata, connPayload.getCADataSource().isHonorZeroPrecision());
            Display display = Display.of(Range.of(metadata.getLowerDispLimit().doubleValue(), metadata.getUpperDispLimit().doubleValue()),
                                         Range.of(metadata.getLowerAlarmLimit().doubleValue(), metadata.getUpperAlarmLimit().doubleValue()),
                                         Range.of(metadata.getLowerWarningLimit().doubleValue(), metadata.getUpperWarningLimit().doubleValue()),
                                         Range.of(metadata.getLowerCtrlLimit().doubleValue(), metadata.getUpperCtrlLimit().doubleValue()),
                                         metadata.getUnits(),
                                         format);
            return VShort.of(((DBR_TIME_Short)message).getShortValue()[0], alarm, timestamp, display);
        }
    };

    // DBR_TIME_Int -> VInt
    final static CATypeAdapter DBRIntToVInt = new CATypeAdapter(VInt.class, DBR_TIME_Int.TYPE, DBR_CTRL_Double.TYPE, false) {

        @Override
        public VInt createValue(DBR rawMessage, DBR rawMetadata, CAConnectionPayload connPayload) {
            DBR_TIME_Int message = (DBR_TIME_Int)rawMessage;
            DBR_CTRL_Double metadata = (DBR_CTRL_Double)rawMetadata;
            Alarm alarm;
            if (!connPayload.isChannelConnected()) {
                alarm = Alarm.disconnected();
            } else {
                alarm = CADataUtils.fromEpics(message.getSeverity());
            }
            Time timestamp;
            if (!connPayload.isChannelConnected()) {
                timestamp = Time.of(connPayload.getEventTime());
            } else {
                timestamp = CADataUtils.timestampOf(message.getTimeStamp());
            }
            NumberFormat format = CADataUtils.getFormat(metadata, connPayload.getCADataSource().isHonorZeroPrecision());
            Display display = Display.of(Range.of(metadata.getLowerDispLimit().doubleValue(), metadata.getUpperDispLimit().doubleValue()),
                                         Range.of(metadata.getLowerAlarmLimit().doubleValue(), metadata.getUpperAlarmLimit().doubleValue()),
                                         Range.of(metadata.getLowerWarningLimit().doubleValue(), metadata.getUpperWarningLimit().doubleValue()),
                                         Range.of(metadata.getLowerCtrlLimit().doubleValue(), metadata.getUpperCtrlLimit().doubleValue()),
                                         metadata.getUnits(),
                                         format);
            return VInt.of(((DBR_TIME_Int)message).getIntValue()[0], alarm, timestamp, display);
        }
    };

    // DBR_TIME_String -> VString
    final static CATypeAdapter DBRStringToVString = new CATypeAdapter(VString.class, DBR_TIME_String.TYPE, null, false) {

        @Override
        public VString createValue(DBR rawMessage, DBR metadata, CAConnectionPayload connPayload) {
            DBR_TIME_String message = (DBR_TIME_String)rawMessage;
            Alarm alarm;
            if (!connPayload.isChannelConnected()) {
                alarm = Alarm.disconnected();
            } else {
                alarm = CADataUtils.fromEpics(message.getSeverity());
            }
            Time timestamp;
            if (!connPayload.isChannelConnected()) {
                timestamp = Time.of(connPayload.getEventTime());
            } else {
                timestamp = CADataUtils.timestampOf(message.getTimeStamp());
            }
            return VString.of(String.valueOf(message.getStringValue()[0]), alarm, timestamp);
        }
    };

    // DBR_TIME_String -> VString
    final static CATypeAdapter DBRByteToVString = new CATypeAdapter(VString.class, DBR_TIME_Byte.TYPE, null, null) {

        @Override
        public boolean match(ReadCollector<?,?> cache, CAConnectionPayload connPayload) {
            if (!connPayload.isLongString()) {
                return false;
            }
            return super.match(cache, connPayload);
        }

        @Override
        public VString createValue(DBR value, DBR metadata, CAConnectionPayload connPayload) {
            DBR_TIME_String message = (DBR_TIME_String)value;
            Alarm alarm;
            if (!connPayload.isChannelConnected()) {
                alarm = Alarm.disconnected();
            } else {
                alarm = CADataUtils.fromEpics(message.getSeverity());
            }
            Time timestamp;
            if (!connPayload.isChannelConnected()) {
                timestamp = Time.of(connPayload.getEventTime());
            } else {
                timestamp = CADataUtils.timestampOf(message.getTimeStamp());
            }
            return VString.of(String.valueOf(message.getStringValue()), alarm, timestamp);
        }
    };

    // DBR_TIME_Enum -> VEnum
    final static CATypeAdapter DBREnumToVEnum = new CATypeAdapter(VEnum.class, DBR_TIME_Enum.TYPE, DBR_LABELS_Enum.TYPE, false) {

        @Override
        public VEnum createValue(DBR value, DBR rawMetadata, CAConnectionPayload connPayload) {
            DBR_TIME_Enum message = (DBR_TIME_Enum)value;
            DBR_LABELS_Enum metadata = (DBR_LABELS_Enum)rawMetadata;
            Alarm alarm;
            if (!connPayload.isChannelConnected()) {
                alarm = Alarm.disconnected();
            } else {
                alarm = CADataUtils.fromEpics(message.getSeverity());
            }
            Time timestamp;
            if (!connPayload.isChannelConnected()) {
                timestamp = Time.of(connPayload.getEventTime());
            } else {
                timestamp = CADataUtils.timestampOf(message.getTimeStamp());
            }
            return VEnum.of(message.getEnumValue()[0], EnumDisplay.of(metadata.getLabels()), alarm, timestamp);
        }
    };

    // DBR_TIME_Float -> VFloatArray
    final static CATypeAdapter DBRFloatToVFloatArray = new CATypeAdapter(VFloatArray.class, DBR_TIME_Float.TYPE, DBR_CTRL_Double.TYPE, true) {

        @Override
        public VFloatArray createValue(DBR value, DBR rawMetadata, CAConnectionPayload connPayload) {
            DBR_TIME_Float message = (DBR_TIME_Float) value;
            DBR_CTRL_Double metadata = (DBR_CTRL_Double) rawMetadata;

            Alarm alarm;
            if (!connPayload.isChannelConnected()) {
                alarm = Alarm.disconnected();
            } else {
                alarm = CADataUtils.fromEpics(message.getSeverity());
            }
            Time timestamp;
            if (!connPayload.isChannelConnected()) {
                timestamp = Time.of(connPayload.getEventTime());
            } else {
                timestamp = CADataUtils.timestampOf(message.getTimeStamp());
            }
            NumberFormat format = CADataUtils.getFormat(metadata, connPayload.getCADataSource().isHonorZeroPrecision());
            Display display = Display.of(Range.of(metadata.getLowerDispLimit().doubleValue(), metadata.getUpperDispLimit().doubleValue()),
                                         Range.of(metadata.getLowerAlarmLimit().doubleValue(), metadata.getUpperAlarmLimit().doubleValue()),
                                         Range.of(metadata.getLowerWarningLimit().doubleValue(), metadata.getUpperWarningLimit().doubleValue()),
                                         Range.of(metadata.getLowerCtrlLimit().doubleValue(), metadata.getUpperCtrlLimit().doubleValue()),
                                         metadata.getUnits(),
                                         format);
            //ListInteger sizes = CollectionNumbers.toListInt(message.get);
            ListFloat data = CollectionNumbers.toListFloat(message.getFloatValue());
            return VFloatArray.of(data, alarm, timestamp, display);
        }
    };

    // DBR_TIME_Double -> VDoubleArray
    final static CATypeAdapter DBRDoubleToVDoubleArray = new CATypeAdapter(VDoubleArray.class, DBR_TIME_Double.TYPE, DBR_CTRL_Double.TYPE, true) {

        @Override
        public VDoubleArray createValue(DBR value, DBR rawMetadata, CAConnectionPayload connPayload) {
            DBR_TIME_Double message = (DBR_TIME_Double) value;
            DBR_CTRL_Double metadata = (DBR_CTRL_Double) rawMetadata;

            Alarm alarm;
            if (!connPayload.isChannelConnected()) {
                alarm = Alarm.disconnected();
            } else {
                alarm = CADataUtils.fromEpics(message.getSeverity());
            }
            Time timestamp;
            if (!connPayload.isChannelConnected()) {
                timestamp = Time.of(connPayload.getEventTime());
            } else {
                timestamp = CADataUtils.timestampOf(message.getTimeStamp());
            }
            NumberFormat format = CADataUtils.getFormat(metadata, connPayload.getCADataSource().isHonorZeroPrecision());
            Display display = Display.of(Range.of(metadata.getLowerDispLimit().doubleValue(), metadata.getUpperDispLimit().doubleValue()),
                                         Range.of(metadata.getLowerAlarmLimit().doubleValue(), metadata.getUpperAlarmLimit().doubleValue()),
                                         Range.of(metadata.getLowerWarningLimit().doubleValue(), metadata.getUpperWarningLimit().doubleValue()),
                                         Range.of(metadata.getLowerCtrlLimit().doubleValue(), metadata.getUpperCtrlLimit().doubleValue()),
                                         metadata.getUnits(),
                                         format);
            //ListInteger sizes = CollectionNumbers.toListInt(message.get);
            ListDouble data = CollectionNumbers.toListDouble(message.getDoubleValue());
            return VDoubleArray.of(data, alarm, timestamp, display);
        }
    };

    // DBR_TIME_Byte -> VByteArray
    final static CATypeAdapter DBRByteToVByteArray = new CATypeAdapter(VByteArray.class, DBR_TIME_Byte.TYPE, DBR_CTRL_Double.TYPE, true) {

        @Override
        public boolean match(ReadCollector<?,?> cache, CAConnectionPayload connPayload) {
            if (connPayload.isLongString()) {
                return false;
            }

            return super.match(cache, connPayload);
        }

        @Override
        public VByteArray createValue(DBR value, DBR rawMetadata, CAConnectionPayload connPayload) {
            DBR_TIME_Byte message = (DBR_TIME_Byte) value;
            DBR_CTRL_Double metadata = (DBR_CTRL_Double) rawMetadata;

            Alarm alarm;
            if (!connPayload.isChannelConnected()) {
                alarm = Alarm.disconnected();
            } else {
                alarm = CADataUtils.fromEpics(message.getSeverity());
            }
            Time timestamp;
            if (!connPayload.isChannelConnected()) {
                timestamp = Time.of(connPayload.getEventTime());
            } else {
                timestamp = CADataUtils.timestampOf(message.getTimeStamp());
            }
            NumberFormat format = CADataUtils.getFormat(metadata, connPayload.getCADataSource().isHonorZeroPrecision());
            Display display = Display.of(Range.of(metadata.getLowerDispLimit().doubleValue(), metadata.getUpperDispLimit().doubleValue()),
                                         Range.of(metadata.getLowerAlarmLimit().doubleValue(), metadata.getUpperAlarmLimit().doubleValue()),
                                         Range.of(metadata.getLowerWarningLimit().doubleValue(), metadata.getUpperWarningLimit().doubleValue()),
                                         Range.of(metadata.getLowerCtrlLimit().doubleValue(), metadata.getUpperCtrlLimit().doubleValue()),
                                         metadata.getUnits(),
                                         format);
            //ListInteger sizes = CollectionNumbers.toListInt(message.get);
            ListByte data = CollectionNumbers.toListByte(message.getByteValue());
            return VByteArray.of(data, alarm, timestamp, display);
        }
    };

    // DBR_TIME_Short -> VShortArray
    final static CATypeAdapter DBRShortToVShortArray = new CATypeAdapter(VShortArray.class, DBR_TIME_Short.TYPE, DBR_CTRL_Double.TYPE, true) {

        @Override
        public VShortArray createValue(DBR value, DBR rawMetadata, CAConnectionPayload connPayload) {
            DBR_TIME_Short message = (DBR_TIME_Short) value;
            DBR_CTRL_Double metadata = (DBR_CTRL_Double) rawMetadata;

            Alarm alarm;
            if (!connPayload.isChannelConnected()) {
                alarm = Alarm.disconnected();
            } else {
                alarm = CADataUtils.fromEpics(message.getSeverity());
            }
            Time timestamp;
            if (!connPayload.isChannelConnected()) {
                timestamp = Time.of(connPayload.getEventTime());
            } else {
                timestamp = CADataUtils.timestampOf(message.getTimeStamp());
            }
            NumberFormat format = CADataUtils.getFormat(metadata, connPayload.getCADataSource().isHonorZeroPrecision());
            Display display = Display.of(Range.of(metadata.getLowerDispLimit().doubleValue(), metadata.getUpperDispLimit().doubleValue()),
                                         Range.of(metadata.getLowerAlarmLimit().doubleValue(), metadata.getUpperAlarmLimit().doubleValue()),
                                         Range.of(metadata.getLowerWarningLimit().doubleValue(), metadata.getUpperWarningLimit().doubleValue()),
                                         Range.of(metadata.getLowerCtrlLimit().doubleValue(), metadata.getUpperCtrlLimit().doubleValue()),
                                         metadata.getUnits(),
                                         format);
            //ListInteger sizes = CollectionNumbers.toListInt(message.get);
            ListShort data = CollectionNumbers.toListShort(message.getShortValue());
            return VShortArray.of(data, alarm, timestamp, display);
        }
    };

    // DBR_TIME_Int -> VIntArray
        final static CATypeAdapter DBRIntToVIntArray = new CATypeAdapter(VIntArray.class, DBR_TIME_Int.TYPE, DBR_CTRL_Double.TYPE, true) {

        @Override
        public VIntArray createValue(DBR value, DBR rawMetadata, CAConnectionPayload connPayload) {
            DBR_TIME_Int message = (DBR_TIME_Int) value;
            DBR_CTRL_Double metadata = (DBR_CTRL_Double) rawMetadata;

            Alarm alarm;
            if (!connPayload.isChannelConnected()) {
                alarm = Alarm.disconnected();
            } else {
                alarm = CADataUtils.fromEpics(message.getSeverity());
            }
            Time timestamp;
            if (!connPayload.isChannelConnected()) {
                timestamp = Time.of(connPayload.getEventTime());
            } else {
                timestamp = CADataUtils.timestampOf(message.getTimeStamp());
            }
            NumberFormat format = CADataUtils.getFormat(metadata, connPayload.getCADataSource().isHonorZeroPrecision());
            Display display = Display.of(Range.of(metadata.getLowerDispLimit().doubleValue(), metadata.getUpperDispLimit().doubleValue()),
                                         Range.of(metadata.getLowerAlarmLimit().doubleValue(), metadata.getUpperAlarmLimit().doubleValue()),
                                         Range.of(metadata.getLowerWarningLimit().doubleValue(), metadata.getUpperWarningLimit().doubleValue()),
                                         Range.of(metadata.getLowerCtrlLimit().doubleValue(), metadata.getUpperCtrlLimit().doubleValue()),
                                         metadata.getUnits(),
                                         format);
            //ListInteger sizes = CollectionNumbers.toListInt(message.get);
            ListInteger data = CollectionNumbers.toListInt(message.getIntValue());
            return VIntArray.of(data, alarm, timestamp, display);
        }
    };

    // DBR_TIME_String -> VString
    final static CATypeAdapter DBRStringToVStringArray = new CATypeAdapter(VStringArray.class, DBR_TIME_String.TYPE, null, true) {

        @Override
        public VStringArray createValue(DBR rawMessage, DBR metadata, CAConnectionPayload connPayload) {
            DBR_TIME_String message = (DBR_TIME_String)rawMessage;
            Alarm alarm;
            if (!connPayload.isChannelConnected()) {
                alarm = Alarm.disconnected();
            } else {
                alarm = CADataUtils.fromEpics(message.getSeverity());
            }
            Time timestamp;
            if (!connPayload.isChannelConnected()) {
                timestamp = Time.of(connPayload.getEventTime());
            } else {
                timestamp = CADataUtils.timestampOf(message.getTimeStamp());
            }
            return VStringArray.of(Arrays.asList(message.getStringValue()), alarm, timestamp);
        }
    };

    private static final Set<CATypeAdapter> converters;

    static {
        Set<CATypeAdapter> newFactories = new HashSet<CATypeAdapter>();
        // Add all SCALARs
        newFactories.add(DBRFloatToVFloat);
        newFactories.add(DBRDoubleToVDouble);
        newFactories.add(DBRByteToVByte);
        newFactories.add(DBRShortToVShort);
        newFactories.add(DBRIntToVInt);
        newFactories.add(DBRStringToVString);
        newFactories.add(DBRByteToVString);
        newFactories.add(DBREnumToVEnum);

        // Add all ARRAYs
        newFactories.add(DBRFloatToVFloatArray);
        newFactories.add(DBRDoubleToVDoubleArray);
        newFactories.add(DBRByteToVByteArray);
        newFactories.add(DBRShortToVShortArray);
        newFactories.add(DBRIntToVIntArray);
        newFactories.add(DBRStringToVStringArray);
        converters = Collections.unmodifiableSet(newFactories);
    }

}
