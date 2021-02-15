/*
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.ca;

import gov.aps.jca.dbr.Status;
import gov.aps.jca.dbr.*;
import org.epics.pvdata.factory.ConvertFactory;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.factory.StandardFieldFactory;
import org.epics.pvdata.factory.StandardPVFieldFactory;
import org.epics.pvdata.misc.BitSet;
import org.epics.pvdata.property.AlarmSeverity;
import org.epics.pvdata.property.AlarmStatus;
import org.epics.pvdata.property.PVEnumerated;
import org.epics.pvdata.property.PVEnumeratedFactory;
import org.epics.pvdata.pv.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author mrk
 */
public class BaseV3ChannelStructure implements V3ChannelStructure {
    private static final Convert convert = ConvertFactory.getConvert();
    private static final StandardField standardField = StandardFieldFactory.getStandardField();
    private static final StandardPVField standardPVField = StandardPVFieldFactory.getStandardPVField();
    private static final PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();

    private static enum DBRProperty {none, status, time, graphic, control}

    ;
    private static final Map<gov.aps.jca.dbr.Status, AlarmStatus> statusMap = new HashMap<Status, AlarmStatus>();

    private final V3Channel v3Channel;

    // TODO variables below are not synced!!!
    private DBRType nativeDBRType = null;
    private DBRType requestDBRType = null;
    private PVStructure pvStructure = null;
    private BitSet bitSet = null;
    private PVStructure pvAlarm = null;
    private PVString pvAlarmMessage = null;
    private PVInt pvAlarmSeverity = null;
    private PVInt pvAlarmStatus = null;
    private PVStructure pvTimeStamp = null;
    private PVLong pvSeconds = null;
    private PVInt pvNanoseconds = null;
    private PVScalar pvScalarValue = null;
    private PVScalarArray pvArrayValue = null;
    // Following not null if nativeDBRType.isENUM
    private PVEnumerated pvEnumerated = null;
    private PVInt pvEnumeratedIndex = null;

    private final AtomicBoolean firstGetPVStructure = new AtomicBoolean(true);

    static {
        statusMap.put(Status.NO_ALARM, AlarmStatus.NONE);
        statusMap.put(Status.READ_ALARM, AlarmStatus.DRIVER);
        statusMap.put(Status.WRITE_ALARM, AlarmStatus.DRIVER);
        statusMap.put(Status.HIHI_ALARM, AlarmStatus.RECORD);
        statusMap.put(Status.HIGH_ALARM, AlarmStatus.RECORD);
        statusMap.put(Status.LOLO_ALARM, AlarmStatus.RECORD);
        statusMap.put(Status.LOW_ALARM, AlarmStatus.RECORD);
        statusMap.put(Status.STATE_ALARM, AlarmStatus.RECORD);
        statusMap.put(Status.COS_ALARM, AlarmStatus.RECORD);
        statusMap.put(Status.COMM_ALARM, AlarmStatus.DRIVER);
        statusMap.put(Status.TIMEOUT_ALARM, AlarmStatus.DRIVER);
        statusMap.put(Status.HW_LIMIT_ALARM, AlarmStatus.DEVICE);
        statusMap.put(Status.CALC_ALARM, AlarmStatus.RECORD);
        statusMap.put(Status.SCAN_ALARM, AlarmStatus.DB);
        statusMap.put(Status.LINK_ALARM, AlarmStatus.DB);
        statusMap.put(Status.SOFT_ALARM, AlarmStatus.CONF);
        statusMap.put(Status.BAD_SUB_ALARM, AlarmStatus.CONF);
        statusMap.put(Status.UDF_ALARM, AlarmStatus.UNDEFINED);
        statusMap.put(Status.DISABLE_ALARM, AlarmStatus.RECORD);
        statusMap.put(Status.SIMM_ALARM, AlarmStatus.RECORD);
        statusMap.put(Status.READ_ACCESS_ALARM, AlarmStatus.DRIVER);
        statusMap.put(Status.WRITE_ACCESS_ALARM, AlarmStatus.DRIVER);
    }

    /**
     * The Constructor
     *
     * @param v3Channel The v3Channel.
     */
    public BaseV3ChannelStructure(V3Channel v3Channel) {
        this.v3Channel = v3Channel;
    }

    /* (non-Javadoc)
     * @see org.epics.ioc.caV3.V3ChannelStructure#createPVStructure(org.epics.ioc.caV3.V3ChannelStructureRequester, org.epics.pvdata.pv.PVStructure, boolean)
     */
    public PVStructure createPVStructure(PVStructure pvRequest, boolean propertiesAllowed) {
        List<String> propertyList = new ArrayList<String>();
        gov.aps.jca.Channel jcaChannel = v3Channel.getJCAChannel();
        int elementCount = jcaChannel.getElementCount();
        nativeDBRType = jcaChannel.getFieldType();
        PVField[] pvFields = null;
        PVField pvf = pvRequest.getSubField("field");
        if (pvf != null && pvf.getField().getType() == Type.structure) {
            PVStructure pvStruct = pvRequest.getStructureField("field");
            pvFields = pvStruct.getPVFields();
        } else {
            pvFields = pvRequest.getPVFields();
        }
        boolean valueIsIndex = false;
        boolean valueIsChoice = false;
        if (pvFields.length == 0 && propertiesAllowed) {
            propertyList.add("timeStamp");
            propertyList.add("alarm");
            if (nativeDBRType != DBRType.ENUM) {
                propertyList.add("display");
                propertyList.add("control");
                if (nativeDBRType != DBRType.STRING) {
                    propertyList.add("valueAlarm");
                }
            }
        }
        for (PVField pvField : pvFields) {
            if (pvField.getFieldName().equals("alarm")) {
                if (propertiesAllowed) propertyList.add("alarm");
                continue;
            }
            if (pvField.getFieldName().equals("timeStamp")) {
                if (propertiesAllowed) propertyList.add("timeStamp");
                continue;
            }
            if (pvField.getFieldName().equals("display")) {
                if (propertiesAllowed && nativeDBRType != DBRType.ENUM) propertyList.add("display");
                continue;
            }
            if (pvField.getFieldName().equals("control")) {
                if (propertiesAllowed && nativeDBRType != DBRType.ENUM) propertyList.add("control");
                continue;
            }
            if (pvField.getFieldName().equals("valueAlarm")) {
                if (propertiesAllowed && nativeDBRType != DBRType.STRING && nativeDBRType != DBRType.ENUM)
                    propertyList.add("valueAlarm");
                continue;
            }
            if (!pvField.getFieldName().equals("value")) {
                String message = pvField.toString() + " name not suported";
                v3Channel.message(message, MessageType.error);
                continue;
            }
            // must be value
            if (nativeDBRType != DBRType.ENUM) continue;
            PVStructure pvValue = (PVStructure) pvField;
            PVField[] pvValueFields = pvValue.getPVFields();
            if (pvValueFields.length == 0) {
                continue;
            } else {
                if (pvValueFields.length != 1) {
                    valueIsChoice = true;
                    String message = pvField.toString() + " value has unsupported subfields";
                    v3Channel.message(message, MessageType.error);
                } else {
                    String fieldName = pvValueFields[0].getFieldName();
                    if (fieldName.equals("index")) {
                        valueIsIndex = true;
                    } else {
                        valueIsChoice = true;
                    }
                }
            }

        }
        if (nativeDBRType.isENUM()) {
            if (valueIsIndex) {
                nativeDBRType = DBRType.INT;
            } else if (valueIsChoice) {
                nativeDBRType = DBRType.STRING;
            }
        }

        DBRProperty dbrProperty = DBRProperty.none;
        if (propertyList.size() > 0) {
            for (String propertyName : propertyList) {
                if (propertyName.equals("alarm") && (dbrProperty.compareTo(DBRProperty.status) < 0)) {
                    dbrProperty = DBRProperty.status;
                    continue;
                }
                if (propertyName.equals("timeStamp") && (dbrProperty.compareTo(DBRProperty.time) < 0)) {
                    dbrProperty = DBRProperty.time;
                    continue;
                }
                if (propertyName.equals("display") && (dbrProperty.compareTo(DBRProperty.graphic) < 0)) {
                    dbrProperty = DBRProperty.graphic;
                    continue;
                }
                if (propertyName.equals("control") && (dbrProperty.compareTo(DBRProperty.control) < 0)) {
                    dbrProperty = DBRProperty.control;
                    continue;
                }
                if (propertyName.equals("valueAlarm") && (dbrProperty.compareTo(DBRProperty.control) < 0)) {
                    dbrProperty = DBRProperty.control;
                    continue;
                }
            }
        }
        Type type = Type.scalar;
        ScalarType scalarType = null;
        if (nativeDBRType.isBYTE()) {
            scalarType = ScalarType.pvByte;
        } else if (nativeDBRType.isSHORT()) {
            scalarType = ScalarType.pvShort;
        } else if (nativeDBRType.isINT()) {
            scalarType = ScalarType.pvInt;
        } else if (nativeDBRType.isFLOAT()) {
            scalarType = ScalarType.pvFloat;
        } else if (nativeDBRType.isDOUBLE()) {
            scalarType = ScalarType.pvDouble;
        } else if (nativeDBRType.isSTRING()) {
            scalarType = ScalarType.pvString;
        } else if (nativeDBRType.isENUM()) {
            type = Type.structure;
        }

        String properties = propertyList.toString();
        if (elementCount > 1 && type == Type.scalar) type = Type.scalarArray;

        switch (type) {
            case scalar:
                pvStructure = standardPVField.scalar(scalarType, properties);
                break;
            case scalarArray:
                pvStructure = standardPVField.scalarArray(scalarType, properties);
                break;
            case structure:
                Structure field = standardField.enumerated(properties);
                pvStructure = pvDataCreate.createPVStructure(field);
        }
        if (nativeDBRType.isENUM()) {
            PVStructure pvStruct = (PVStructure) pvStructure.getPVFields()[0];
            pvEnumeratedIndex = pvStruct.getIntField("index");
            pvEnumerated = PVEnumeratedFactory.create();
            if (pvEnumeratedIndex == null || !pvEnumerated.attach(pvStruct)) {
                v3Channel.message("field is not an enumerated structure ", MessageType.error);
                return null;
            }
        }
        if (pvStructure.getSubField("alarm") != null) {
            pvAlarm = pvStructure.getStructureField("alarm");
            if (pvAlarm != null) {
                pvAlarmMessage = pvAlarm.getStringField("message");
                pvAlarmSeverity = pvAlarm.getIntField("severity");
                pvAlarmStatus = pvAlarm.getIntField("status");
            }
        }
        if (pvStructure.getSubField("timeStamp") != null) {
            pvTimeStamp = pvStructure.getStructureField("timeStamp");
            if (pvTimeStamp != null) {
                pvSeconds = pvTimeStamp.getLongField("secondsPastEpoch");
                pvNanoseconds = pvTimeStamp.getIntField("nanoseconds");
            }
        }
        bitSet = new BitSet(pvStructure.getNumberFields());
        PVField pvValue = pvStructure.getPVFields()[0];
        if (nativeDBRType.isENUM()) {
            requestDBRType = DBRType.CTRL_ENUM;
            return pvStructure;
        }
        if (elementCount < 2) {
            pvScalarValue = (PVScalar) pvValue;
        } else {
            pvArrayValue = (PVScalarArray) pvValue;
        }
        requestDBRType = null;
        switch (dbrProperty) {
            case none:
                requestDBRType = nativeDBRType;
                break;
            case status:
                if (nativeDBRType == DBRType.BYTE) {
                    requestDBRType = DBRType.STS_BYTE;
                } else if (nativeDBRType == DBRType.SHORT) {
                    requestDBRType = DBRType.STS_SHORT;
                } else if (nativeDBRType == DBRType.INT) {
                    requestDBRType = DBRType.STS_INT;
                } else if (nativeDBRType == DBRType.FLOAT) {
                    requestDBRType = DBRType.STS_FLOAT;
                } else if (nativeDBRType == DBRType.DOUBLE) {
                    requestDBRType = DBRType.STS_DOUBLE;
                } else if (nativeDBRType == DBRType.STRING) {
                    requestDBRType = DBRType.STS_STRING;
                } else if (nativeDBRType == DBRType.ENUM) {
                    requestDBRType = DBRType.STS_ENUM;
                }
                break;
            case time:
                if (nativeDBRType == DBRType.BYTE) {
                    requestDBRType = DBRType.TIME_BYTE;
                } else if (nativeDBRType == DBRType.SHORT) {
                    requestDBRType = DBRType.TIME_SHORT;
                } else if (nativeDBRType == DBRType.INT) {
                    requestDBRType = DBRType.TIME_INT;
                } else if (nativeDBRType == DBRType.FLOAT) {
                    requestDBRType = DBRType.TIME_FLOAT;
                } else if (nativeDBRType == DBRType.DOUBLE) {
                    requestDBRType = DBRType.TIME_DOUBLE;
                } else if (nativeDBRType == DBRType.STRING) {
                    requestDBRType = DBRType.TIME_STRING;
                } else if (nativeDBRType == DBRType.ENUM) {
                    requestDBRType = DBRType.TIME_ENUM;
                }
                break;
            case graphic:
                if (nativeDBRType == DBRType.BYTE) {
                    requestDBRType = DBRType.GR_BYTE;
                } else if (nativeDBRType == DBRType.SHORT) {
                    requestDBRType = DBRType.GR_SHORT;
                } else if (nativeDBRType == DBRType.INT) {
                    requestDBRType = DBRType.GR_INT;
                } else if (nativeDBRType == DBRType.FLOAT) {
                    requestDBRType = DBRType.GR_FLOAT;
                } else if (nativeDBRType == DBRType.DOUBLE) {
                    requestDBRType = DBRType.GR_DOUBLE;
                } else if (nativeDBRType == DBRType.STRING) {
                    requestDBRType = DBRType.GR_STRING;
                } else if (nativeDBRType == DBRType.ENUM) {
                    requestDBRType = DBRType.CTRL_ENUM;
                }
                break;
            case control:
                if (nativeDBRType == DBRType.BYTE) {
                    requestDBRType = DBRType.CTRL_BYTE;
                } else if (nativeDBRType == DBRType.SHORT) {
                    requestDBRType = DBRType.CTRL_SHORT;
                } else if (nativeDBRType == DBRType.INT) {
                    requestDBRType = DBRType.CTRL_INT;
                } else if (nativeDBRType == DBRType.FLOAT) {
                    requestDBRType = DBRType.CTRL_FLOAT;
                } else if (nativeDBRType == DBRType.DOUBLE) {
                    requestDBRType = DBRType.CTRL_DOUBLE;
                } else if (nativeDBRType == DBRType.STRING) {
                    requestDBRType = DBRType.CTRL_STRING;
                } else if (nativeDBRType == DBRType.ENUM) {
                    requestDBRType = DBRType.CTRL_ENUM;
                }
                break;
        }
        return pvStructure;
    }

    /* (non-Javadoc)
     * @see org.epics.ioc.caV3.V3ChannelStructure#getPVStructure()
     */
    public PVStructure getPVStructure() {
        return pvStructure;
    }

    /* (non-Javadoc)
     * @see org.epics.ioc.caV3.V3ChannelStructure#getBitSet()
     */
    public BitSet getBitSet() {
        return bitSet;
    }

    /* (non-Javadoc)
     * @see org.epics.ioc.caV3.V3ChannelRecord#getValueDBRType()
     */
    public DBRType getNativeDBRType() {
        return nativeDBRType;
    }

    /* (non-Javadoc)
     * @see org.epics.ioc.caV3.V3ChannelStructure#getRequestDBRType()
     */
    public DBRType getRequestDBRType() {
        return requestDBRType;
    }

    private void setAlarm(AlarmStatus alarmStatus, AlarmSeverity alarmSeverity, String message) {
        int severity = alarmSeverity.ordinal();
        int status = alarmStatus.ordinal();
        if (pvAlarm != null) {
            String oldMessage = pvAlarmMessage.get();
            if (message != null && oldMessage != null && !message.equals(oldMessage)) {
                pvAlarmMessage.put(message);
            }
            pvAlarmSeverity.put(severity);
            pvAlarmStatus.put(status);
        } else {
            System.err.println(
                    v3Channel.getChannelName()
                            + " v3Ca error " + message
                            + " severity " + alarmSeverity.toString()
                            + " status " + alarmStatus.toString());
        }
    }

    /* (non-Javadoc)
     * @see org.epics.ioc.caV3.V3ChannelStructure#toStructure(gov.aps.jca.dbr.DBR)
     */
    public void toStructure(DBR fromDBR) {
        if (fromDBR == null) {
            setAlarm(AlarmStatus.UNDEFINED, AlarmSeverity.INVALID, "fromDBR is null");
            return;
        }
        Status status = null;
        gov.aps.jca.dbr.TimeStamp timeStamp = null;
        gov.aps.jca.dbr.Severity severity = null;
        gov.aps.jca.Channel jcaChannel = v3Channel.getJCAChannel();
        int elementCount = jcaChannel.getElementCount();

        double displayLow = 0.0;
        double displayHigh = 0.0;
        double controlLow = 0.0;
        double controlHigh = 0.0;

        double lowAlarmLimit = 0.0;
        double lowWarningLimit = 0.0;
        double highWarningLimit = 0.0;
        double highAlarmLimit = 0.0;

        String units = null;
        bitSet.clear();
        DBRType requestDBRType = fromDBR.getType();
        if (nativeDBRType.isENUM()) {
            int index = pvEnumerated.getIndex();
            if (requestDBRType == DBRType.ENUM) {
                DBR_Enum dbr = (DBR_Enum) fromDBR;
                index = dbr.getEnumValue()[0];
            } else if (requestDBRType == DBRType.TIME_ENUM) {
                DBR_TIME_Enum dbr = (DBR_TIME_Enum) fromDBR;
                index = dbr.getEnumValue()[0];
                status = dbr.getStatus();
                severity = dbr.getSeverity();
                timeStamp = dbr.getTimeStamp();
            } else if (requestDBRType == DBRType.STS_ENUM) {
                DBR_STS_Enum dbr = (DBR_STS_Enum) fromDBR;
                index = dbr.getEnumValue()[0];
                status = dbr.getStatus();
                severity = dbr.getSeverity();
            } else if (requestDBRType == DBRType.CTRL_ENUM) {
                DBR_CTRL_Enum dbr = (DBR_CTRL_Enum) fromDBR;
                String[] labels = dbr.getLabels();
                pvEnumerated.setChoices(labels);
                index = dbr.getEnumValue()[0];
                status = dbr.getStatus();
                severity = dbr.getSeverity();
                if (pvTimeStamp != null) {
                    this.requestDBRType = DBRType.TIME_ENUM;
                } else if (pvAlarmSeverity != null) {
                    this.requestDBRType = DBRType.STS_ENUM;
                } else {
                    this.requestDBRType = DBRType.ENUM;
                }
            } else {
                setAlarm(AlarmStatus.UNDEFINED, AlarmSeverity.INVALID,
                        " unsupported DBRType " + requestDBRType.getName());
                return;
            }
            if (index != pvEnumerated.getIndex()) pvEnumerated.setIndex(index);
        } else {
            if (requestDBRType == DBRType.DOUBLE) {
                DBR_Double dbr = (DBR_Double) fromDBR;
                if (elementCount == 1) {
                    convert.fromDouble(pvScalarValue, dbr.getDoubleValue()[0]);
                } else {
                    convert.fromDoubleArray(pvArrayValue, 0, dbr.getCount(), dbr.getDoubleValue(), 0);
                }
            } else if (requestDBRType == DBRType.STS_DOUBLE) {
                DBR_STS_Double dbr = (DBR_STS_Double) fromDBR;
                status = dbr.getStatus();
                severity = dbr.getSeverity();
                if (elementCount == 1) {
                    convert.fromDouble(pvScalarValue, dbr.getDoubleValue()[0]);
                } else {
                    convert.fromDoubleArray(pvArrayValue, 0, dbr.getCount(), dbr.getDoubleValue(), 0);
                }
            } else if (requestDBRType == DBRType.TIME_DOUBLE) {
                DBR_TIME_Double dbr = (DBR_TIME_Double) fromDBR;
                status = dbr.getStatus();
                timeStamp = dbr.getTimeStamp();
                severity = dbr.getSeverity();
                if (elementCount == 1) {
                    convert.fromDouble(pvScalarValue, dbr.getDoubleValue()[0]);
                } else {
                    convert.fromDoubleArray(pvArrayValue, 0, dbr.getCount(), dbr.getDoubleValue(), 0);
                }
            } else if (requestDBRType == DBRType.SHORT) {
                DBR_Short dbr = (DBR_Short) fromDBR;
                if (elementCount == 1) {
                    convert.fromShort(pvScalarValue, dbr.getShortValue()[0]);
                } else {
                    convert.fromShortArray(pvArrayValue, 0, dbr.getCount(), dbr.getShortValue(), 0);
                }
            } else if (requestDBRType == DBRType.STS_SHORT) {
                DBR_STS_Short dbr = (DBR_STS_Short) fromDBR;
                status = dbr.getStatus();
                severity = dbr.getSeverity();
                if (elementCount == 1) {
                    convert.fromShort(pvScalarValue, dbr.getShortValue()[0]);
                } else {
                    convert.fromShortArray(pvArrayValue, 0, dbr.getCount(), dbr.getShortValue(), 0);
                }
            } else if (requestDBRType == DBRType.TIME_SHORT) {
                DBR_TIME_Short dbr = (DBR_TIME_Short) fromDBR;
                status = dbr.getStatus();
                timeStamp = dbr.getTimeStamp();
                severity = dbr.getSeverity();
                if (elementCount == 1) {
                    convert.fromShort(pvScalarValue, dbr.getShortValue()[0]);
                } else {
                    convert.fromShortArray(pvArrayValue, 0, dbr.getCount(), dbr.getShortValue(), 0);
                }
            } else if (requestDBRType == DBRType.INT) {
                DBR_Int dbr = (DBR_Int) fromDBR;
                if (elementCount == 1) {
                    convert.fromInt(pvScalarValue, dbr.getIntValue()[0]);
                } else {
                    convert.fromIntArray(pvArrayValue, 0, dbr.getCount(), dbr.getIntValue(), 0);
                }
            } else if (requestDBRType == DBRType.STS_INT) {
                DBR_STS_Int dbr = (DBR_STS_Int) fromDBR;
                status = dbr.getStatus();
                severity = dbr.getSeverity();
                if (elementCount == 1) {
                    convert.fromInt(pvScalarValue, dbr.getIntValue()[0]);
                } else {
                    convert.fromIntArray(pvArrayValue, 0, dbr.getCount(), dbr.getIntValue(), 0);
                }
            } else if (requestDBRType == DBRType.TIME_INT) {
                DBR_TIME_Int dbr = (DBR_TIME_Int) fromDBR;
                status = dbr.getStatus();
                timeStamp = dbr.getTimeStamp();
                severity = dbr.getSeverity();
                if (elementCount == 1) {
                    convert.fromInt(pvScalarValue, dbr.getIntValue()[0]);
                } else {
                    convert.fromIntArray(pvArrayValue, 0, dbr.getCount(), dbr.getIntValue(), 0);
                }
            } else if (requestDBRType == DBRType.BYTE) {
                DBR_Byte dbr = (DBR_Byte) fromDBR;
                if (elementCount == 1) {
                    convert.fromByte(pvScalarValue, dbr.getByteValue()[0]);
                } else {
                    convert.fromByteArray(pvArrayValue, 0, dbr.getCount(), dbr.getByteValue(), 0);
                }
            } else if (requestDBRType == DBRType.STS_BYTE) {
                DBR_STS_Byte dbr = (DBR_STS_Byte) fromDBR;
                status = dbr.getStatus();
                severity = dbr.getSeverity();
                if (elementCount == 1) {
                    convert.fromByte(pvScalarValue, dbr.getByteValue()[0]);
                } else {
                    convert.fromByteArray(pvArrayValue, 0, dbr.getCount(), dbr.getByteValue(), 0);
                }
            } else if (requestDBRType == DBRType.TIME_BYTE) {
                DBR_TIME_Byte dbr = (DBR_TIME_Byte) fromDBR;
                status = dbr.getStatus();
                timeStamp = dbr.getTimeStamp();
                severity = dbr.getSeverity();
                if (elementCount == 1) {
                    convert.fromByte(pvScalarValue, dbr.getByteValue()[0]);
                } else {
                    convert.fromByteArray(pvArrayValue, 0, dbr.getCount(), dbr.getByteValue(), 0);
                }
            } else if (requestDBRType == DBRType.FLOAT) {
                DBR_Float dbr = (DBR_Float) fromDBR;
                if (elementCount == 1) {
                    convert.fromFloat(pvScalarValue, dbr.getFloatValue()[0]);
                } else {
                    convert.fromFloatArray(pvArrayValue, 0, dbr.getCount(), dbr.getFloatValue(), 0);
                }
            } else if (requestDBRType == DBRType.STS_FLOAT) {
                DBR_STS_Float dbr = (DBR_STS_Float) fromDBR;
                status = dbr.getStatus();
                severity = dbr.getSeverity();
                if (elementCount == 1) {
                    convert.fromFloat(pvScalarValue, dbr.getFloatValue()[0]);
                } else {
                    convert.fromFloatArray(pvArrayValue, 0, dbr.getCount(), dbr.getFloatValue(), 0);
                }
            } else if (requestDBRType == DBRType.TIME_FLOAT) {
                DBR_TIME_Float dbr = (DBR_TIME_Float) fromDBR;
                status = dbr.getStatus();
                timeStamp = dbr.getTimeStamp();
                severity = dbr.getSeverity();
                if (elementCount == 1) {
                    convert.fromFloat(pvScalarValue, dbr.getFloatValue()[0]);
                } else {
                    convert.fromFloatArray(pvArrayValue, 0, dbr.getCount(), dbr.getFloatValue(), 0);
                }
            } else if (requestDBRType == DBRType.STRING) {
                DBR_String dbr = (DBR_String) fromDBR;
                if (elementCount == 1) {
                    convert.fromString(pvScalarValue, dbr.getStringValue()[0]);
                } else {
                    convert.fromStringArray(pvArrayValue, 0, dbr.getCount(), dbr.getStringValue(), 0);
                }
            } else if (requestDBRType == DBRType.STS_STRING) {
                DBR_STS_String dbr = (DBR_STS_String) fromDBR;
                status = dbr.getStatus();
                severity = dbr.getSeverity();
                if (elementCount == 1) {
                    convert.fromString(pvScalarValue, dbr.getStringValue()[0]);
                } else {
                    convert.fromStringArray(pvArrayValue, 0, dbr.getCount(), dbr.getStringValue(), 0);
                }
            } else if (requestDBRType == DBRType.TIME_STRING) {
                DBR_TIME_String dbr = (DBR_TIME_String) fromDBR;
                status = dbr.getStatus();
                timeStamp = dbr.getTimeStamp();
                severity = dbr.getSeverity();
                if (elementCount == 1) {
                    convert.fromString(pvScalarValue, dbr.getStringValue()[0]);
                } else {
                    convert.fromStringArray(pvArrayValue, 0, dbr.getCount(), dbr.getStringValue(), 0);
                }
            } else if (requestDBRType == DBRType.GR_BYTE) {
                DBR_GR_Byte dbr = (DBR_GR_Byte) fromDBR;
                status = dbr.getStatus();
                timeStamp = dbr.getTimeStamp();
                severity = dbr.getSeverity();
                units = dbr.getUnits();
                displayLow = dbr.getLowerDispLimit().doubleValue();
                displayHigh = dbr.getUpperDispLimit().doubleValue();
                if (elementCount == 1) {
                    convert.fromByte(pvScalarValue, dbr.getByteValue()[0]);
                } else {
                    convert.fromByteArray(pvArrayValue, 0, dbr.getCount(), dbr.getByteValue(), 0);
                }
                if (pvTimeStamp != null) {
                    this.requestDBRType = DBRType.TIME_BYTE;
                } else if (pvAlarmSeverity != null) {
                    this.requestDBRType = DBRType.STS_BYTE;
                } else {
                    this.requestDBRType = DBRType.BYTE;
                }
            } else if (requestDBRType == DBRType.CTRL_BYTE) {
                DBR_CTRL_Byte dbr = (DBR_CTRL_Byte) fromDBR;
                status = dbr.getStatus();
                timeStamp = dbr.getTimeStamp();
                severity = dbr.getSeverity();
                units = dbr.getUnits();
                displayLow = dbr.getLowerDispLimit().doubleValue();
                displayHigh = dbr.getUpperDispLimit().doubleValue();
                controlLow = dbr.getLowerCtrlLimit().doubleValue();
                controlHigh = dbr.getUpperCtrlLimit().doubleValue();

                lowAlarmLimit = dbr.getLowerAlarmLimit().doubleValue();
                lowWarningLimit = dbr.getLowerWarningLimit().doubleValue();
                highWarningLimit = dbr.getUpperWarningLimit().doubleValue();
                highAlarmLimit = dbr.getUpperAlarmLimit().doubleValue();

                if (elementCount == 1) {
                    convert.fromByte(pvScalarValue, dbr.getByteValue()[0]);
                } else {
                    convert.fromByteArray(pvArrayValue, 0, dbr.getCount(), dbr.getByteValue(), 0);
                }
                if (pvTimeStamp != null) {
                    this.requestDBRType = DBRType.TIME_BYTE;
                } else if (pvAlarmSeverity != null) {
                    this.requestDBRType = DBRType.STS_BYTE;
                } else {
                    this.requestDBRType = DBRType.BYTE;
                }
            } else if (requestDBRType == DBRType.GR_SHORT) {
                DBR_GR_Short dbr = (DBR_GR_Short) fromDBR;
                status = dbr.getStatus();
                timeStamp = dbr.getTimeStamp();
                severity = dbr.getSeverity();
                units = dbr.getUnits();
                displayLow = dbr.getLowerDispLimit().doubleValue();
                displayHigh = dbr.getUpperDispLimit().doubleValue();
                if (elementCount == 1) {
                    convert.fromShort(pvScalarValue, dbr.getShortValue()[0]);
                } else {
                    convert.fromShortArray(pvArrayValue, 0, dbr.getCount(), dbr.getShortValue(), 0);
                }
                if (pvTimeStamp != null) {
                    this.requestDBRType = DBRType.TIME_SHORT;
                } else if (pvAlarmSeverity != null) {
                    this.requestDBRType = DBRType.STS_SHORT;
                } else {
                    this.requestDBRType = DBRType.SHORT;
                }
            } else if (requestDBRType == DBRType.CTRL_SHORT) {
                DBR_CTRL_Short dbr = (DBR_CTRL_Short) fromDBR;
                status = dbr.getStatus();
                timeStamp = dbr.getTimeStamp();
                severity = dbr.getSeverity();
                units = dbr.getUnits();
                displayLow = dbr.getLowerDispLimit().doubleValue();
                displayHigh = dbr.getUpperDispLimit().doubleValue();
                controlLow = dbr.getLowerCtrlLimit().doubleValue();
                controlHigh = dbr.getUpperCtrlLimit().doubleValue();

                lowAlarmLimit = dbr.getLowerAlarmLimit().doubleValue();
                lowWarningLimit = dbr.getLowerWarningLimit().doubleValue();
                highWarningLimit = dbr.getUpperWarningLimit().doubleValue();
                highAlarmLimit = dbr.getUpperAlarmLimit().doubleValue();

                if (elementCount == 1) {
                    convert.fromShort(pvScalarValue, dbr.getShortValue()[0]);
                } else {
                    convert.fromShortArray(pvArrayValue, 0, dbr.getCount(), dbr.getShortValue(), 0);
                }
                if (pvTimeStamp != null) {
                    this.requestDBRType = DBRType.TIME_SHORT;
                } else if (pvAlarmSeverity != null) {
                    this.requestDBRType = DBRType.STS_SHORT;
                } else {
                    this.requestDBRType = DBRType.SHORT;
                }
            } else if (requestDBRType == DBRType.GR_INT) {
                DBR_GR_Int dbr = (DBR_GR_Int) fromDBR;
                status = dbr.getStatus();
                timeStamp = dbr.getTimeStamp();
                severity = dbr.getSeverity();
                units = dbr.getUnits();
                displayLow = dbr.getLowerDispLimit().doubleValue();
                displayHigh = dbr.getUpperDispLimit().doubleValue();
                if (elementCount == 1) {
                    convert.fromInt(pvScalarValue, dbr.getIntValue()[0]);
                } else {
                    convert.fromIntArray(pvArrayValue, 0, dbr.getCount(), dbr.getIntValue(), 0);
                }
                if (pvTimeStamp != null) {
                    this.requestDBRType = DBRType.TIME_INT;
                } else if (pvAlarmSeverity != null) {
                    this.requestDBRType = DBRType.STS_INT;
                } else {
                    this.requestDBRType = DBRType.INT;
                }
            } else if (requestDBRType == DBRType.CTRL_INT) {
                DBR_CTRL_Int dbr = (DBR_CTRL_Int) fromDBR;
                status = dbr.getStatus();
                timeStamp = dbr.getTimeStamp();
                severity = dbr.getSeverity();
                units = dbr.getUnits();
                displayLow = dbr.getLowerDispLimit().doubleValue();
                displayHigh = dbr.getUpperDispLimit().doubleValue();
                controlLow = dbr.getLowerCtrlLimit().doubleValue();
                controlHigh = dbr.getUpperCtrlLimit().doubleValue();

                lowAlarmLimit = dbr.getLowerAlarmLimit().doubleValue();
                lowWarningLimit = dbr.getLowerWarningLimit().doubleValue();
                highWarningLimit = dbr.getUpperWarningLimit().doubleValue();
                highAlarmLimit = dbr.getUpperAlarmLimit().doubleValue();

                if (elementCount == 1) {
                    convert.fromInt(pvScalarValue, dbr.getIntValue()[0]);
                } else {
                    convert.fromIntArray(pvArrayValue, 0, dbr.getCount(), dbr.getIntValue(), 0);
                }
                if (pvTimeStamp != null) {
                    this.requestDBRType = DBRType.TIME_INT;
                } else if (pvAlarmSeverity != null) {
                    this.requestDBRType = DBRType.STS_INT;
                } else {
                    this.requestDBRType = DBRType.INT;
                }
            } else if (requestDBRType == DBRType.GR_FLOAT) {
                DBR_GR_Float dbr = (DBR_GR_Float) fromDBR;
                status = dbr.getStatus();
                timeStamp = dbr.getTimeStamp();
                severity = dbr.getSeverity();
                units = dbr.getUnits();
                displayLow = dbr.getLowerDispLimit().doubleValue();
                displayHigh = dbr.getUpperDispLimit().doubleValue();
                if (elementCount == 1) {
                    convert.fromFloat(pvScalarValue, dbr.getFloatValue()[0]);
                } else {
                    convert.fromFloatArray(pvArrayValue, 0, dbr.getCount(), dbr.getFloatValue(), 0);
                }
                if (pvTimeStamp != null) {
                    this.requestDBRType = DBRType.TIME_FLOAT;
                } else if (pvAlarmSeverity != null) {
                    this.requestDBRType = DBRType.STS_FLOAT;
                } else {
                    this.requestDBRType = DBRType.FLOAT;
                }
            } else if (requestDBRType == DBRType.CTRL_FLOAT) {
                DBR_CTRL_Float dbr = (DBR_CTRL_Float) fromDBR;
                status = dbr.getStatus();
                timeStamp = dbr.getTimeStamp();
                severity = dbr.getSeverity();
                units = dbr.getUnits();
                displayLow = dbr.getLowerDispLimit().doubleValue();
                displayHigh = dbr.getUpperDispLimit().doubleValue();
                controlLow = dbr.getLowerCtrlLimit().doubleValue();
                controlHigh = dbr.getUpperCtrlLimit().doubleValue();

                lowAlarmLimit = dbr.getLowerAlarmLimit().doubleValue();
                lowWarningLimit = dbr.getLowerWarningLimit().doubleValue();
                highWarningLimit = dbr.getUpperWarningLimit().doubleValue();
                highAlarmLimit = dbr.getUpperAlarmLimit().doubleValue();

                if (elementCount == 1) {
                    convert.fromFloat(pvScalarValue, dbr.getFloatValue()[0]);
                } else {
                    convert.fromFloatArray(pvArrayValue, 0, dbr.getCount(), dbr.getFloatValue(), 0);
                }
                if (pvTimeStamp != null) {
                    this.requestDBRType = DBRType.TIME_FLOAT;
                } else if (pvAlarmSeverity != null) {
                    this.requestDBRType = DBRType.STS_FLOAT;
                } else {
                    this.requestDBRType = DBRType.FLOAT;
                }
            } else if (requestDBRType == DBRType.GR_DOUBLE) {
                DBR_GR_Double dbr = (DBR_GR_Double) fromDBR;
                status = dbr.getStatus();
                timeStamp = dbr.getTimeStamp();
                severity = dbr.getSeverity();
                units = dbr.getUnits();
                displayLow = dbr.getLowerDispLimit().doubleValue();
                displayHigh = dbr.getUpperDispLimit().doubleValue();
                if (elementCount == 1) {
                    convert.fromDouble(pvScalarValue, dbr.getDoubleValue()[0]);
                } else {
                    convert.fromDoubleArray(pvArrayValue, 0, dbr.getCount(), dbr.getDoubleValue(), 0);
                }
                if (pvTimeStamp != null) {
                    this.requestDBRType = DBRType.TIME_DOUBLE;
                } else if (pvAlarmSeverity != null) {
                    this.requestDBRType = DBRType.STS_DOUBLE;
                } else {
                    this.requestDBRType = DBRType.DOUBLE;
                }
            } else if (requestDBRType == DBRType.CTRL_DOUBLE) {
                DBR_CTRL_Double dbr = (DBR_CTRL_Double) fromDBR;
                status = dbr.getStatus();
                timeStamp = dbr.getTimeStamp();
                severity = dbr.getSeverity();
                units = dbr.getUnits();
                displayLow = dbr.getLowerDispLimit().doubleValue();
                displayHigh = dbr.getUpperDispLimit().doubleValue();
                controlLow = dbr.getLowerCtrlLimit().doubleValue();
                controlHigh = dbr.getUpperCtrlLimit().doubleValue();

                lowAlarmLimit = dbr.getLowerAlarmLimit().doubleValue();
                lowWarningLimit = dbr.getLowerWarningLimit().doubleValue();
                highWarningLimit = dbr.getUpperWarningLimit().doubleValue();
                highAlarmLimit = dbr.getUpperAlarmLimit().doubleValue();

                if (elementCount == 1) {
                    convert.fromDouble(pvScalarValue, dbr.getDoubleValue()[0]);
                } else {
                    convert.fromDoubleArray(pvArrayValue, 0, dbr.getCount(), dbr.getDoubleValue(), 0);
                }
                if (pvTimeStamp != null) {
                    this.requestDBRType = DBRType.TIME_DOUBLE;
                } else if (pvAlarmSeverity != null) {
                    this.requestDBRType = DBRType.STS_DOUBLE;
                } else {
                    this.requestDBRType = DBRType.DOUBLE;
                }
            } else if (requestDBRType == DBRType.GR_STRING) {
                DBR_GR_String dbr = (DBR_GR_String) fromDBR;
                status = dbr.getStatus();
                timeStamp = dbr.getTimeStamp();
                severity = dbr.getSeverity();
                if (elementCount == 1) {
                    convert.fromString(pvScalarValue, dbr.getStringValue()[0]);
                } else {
                    convert.fromStringArray(pvArrayValue, 0, dbr.getCount(), dbr.getStringValue(), 0);
                }
                if (pvTimeStamp != null) {
                    this.requestDBRType = DBRType.TIME_STRING;
                } else if (pvAlarmSeverity != null) {
                    this.requestDBRType = DBRType.STS_STRING;
                } else {
                    this.requestDBRType = DBRType.STRING;
                }
            } else if (requestDBRType == DBRType.CTRL_STRING) {
                DBR_CTRL_String dbr = (DBR_CTRL_String) fromDBR;
                status = dbr.getStatus();
                timeStamp = dbr.getTimeStamp();
                severity = dbr.getSeverity();
                if (elementCount == 1) {
                    convert.fromString(pvScalarValue, dbr.getStringValue()[0]);
                } else {
                    convert.fromStringArray(pvArrayValue, 0, dbr.getCount(), dbr.getStringValue(), 0);
                }
                if (pvTimeStamp != null) {
                    this.requestDBRType = DBRType.TIME_STRING;
                } else if (pvAlarmSeverity != null) {
                    this.requestDBRType = DBRType.STS_STRING;
                } else {
                    this.requestDBRType = DBRType.STRING;
                }
            } else {
                setAlarm(AlarmStatus.UNDEFINED, AlarmSeverity.INVALID,
                        " unsupported DBRType " + requestDBRType.getName());
                return;
            }
        }
        PVStructure pvStructure = null;
        if (timeStamp != null && pvTimeStamp != null) {
            long seconds = timeStamp.secPastEpoch();
            seconds += 7305 * 86400;
            pvSeconds.put(seconds);
            pvNanoseconds.put((int) timeStamp.nsec());
        }
        if (severity != null && pvAlarm != null) {
            int index = severity.getValue();
            AlarmSeverity alarmSeverity = AlarmSeverity.getSeverity(index);
            String message = status.getName();
            setAlarm(statusMap.get(status), alarmSeverity, message);
        }
        if (units != null) {
            pvStructure = this.pvStructure.getStructureField("display");
            if (pvStructure != null) {
                PVString pvUnits = pvStructure.getStringField("units");
                if (pvUnits != null) {
                    pvUnits.put(units.toString());
                }
            }
        }
        if (displayLow < displayHigh) {
            pvStructure = this.pvStructure.getStructureField("display");
            if (pvStructure != null) {
                PVDouble pvLow = pvStructure.getDoubleField("limitLow");
                PVDouble pvHigh = pvStructure.getDoubleField("limitHigh");
                if (pvLow != null && pvHigh != null) {
                    pvLow.put(displayLow);
                    pvHigh.put(displayHigh);
                }
            }
        }
        if (controlLow < controlHigh) {
            pvStructure = this.pvStructure.getStructureField("control");
            if (pvStructure != null) {
                PVDouble pvLow = pvStructure.getDoubleField("limitLow");
                PVDouble pvHigh = pvStructure.getDoubleField("limitHigh");
                if (pvLow != null && pvHigh != null) {
                    pvLow.put(displayLow);
                    pvHigh.put(displayHigh);
                }
            }
        }

        if (lowAlarmLimit < highAlarmLimit || lowWarningLimit < highWarningLimit) {
            pvStructure = this.pvStructure.getStructureField("valueAlarm");
            if (pvStructure != null) {
                PVDouble pvLowWarning = pvStructure.getDoubleField("lowWarningLimit");
                PVDouble pvHighWarning = pvStructure.getDoubleField("highWarningLimit");
                if (pvLowWarning != null && pvHighWarning != null) {
                    pvLowWarning.put(lowWarningLimit);
                    pvHighWarning.put(highWarningLimit);
                }
                PVDouble pvLowAlarm = pvStructure.getDoubleField("lowAlarmLimit");
                PVDouble pvHighAlarm = pvStructure.getDoubleField("highAlarmLimit");
                if (pvLowAlarm != null && pvHighAlarm != null) {
                    pvLowAlarm.put(lowAlarmLimit);
                    pvHighAlarm.put(highAlarmLimit);
                }
            }
        }

        if (firstGetPVStructure.getAndSet(false)) {
            bitSet.clear();
            bitSet.set(0);
        } else {
            if (pvAlarmMessage != null) bitSet.set(pvAlarmMessage.getFieldOffset());
            if (pvAlarmSeverity != null) bitSet.set(pvAlarmSeverity.getFieldOffset());
            if (pvTimeStamp != null) bitSet.set(pvTimeStamp.getFieldOffset());
            if (pvScalarValue != null) bitSet.set(pvScalarValue.getFieldOffset());
            if (pvArrayValue != null) bitSet.set(pvArrayValue.getFieldOffset());
            if (pvEnumerated != null) bitSet.set(pvEnumeratedIndex.getFieldOffset());
        }
    }
}
