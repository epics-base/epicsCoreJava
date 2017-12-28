/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.vtype.json;

import org.epics.vtype.json.VTypeToJson;
import static com.oracle.jrockit.jfr.ContentType.Timestamp;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonWriter;
import javax.json.stream.JsonGenerator;
import jdk.nashorn.internal.objects.NativeDebug;
import org.epics.util.array.ArrayBoolean;
import org.epics.util.array.ArrayByte;
import org.epics.util.array.ArrayDouble;
import org.epics.util.array.ArrayFloat;
import org.epics.util.array.ArrayInteger;
import org.epics.util.array.ArrayLong;
import org.epics.util.array.ArrayShort;
import org.epics.vtype.Alarm;
import org.epics.vtype.AlarmSeverity;
import org.epics.vtype.AlarmStatus;
import org.epics.vtype.Display;
import org.epics.vtype.EnumDisplay;
import org.epics.vtype.Time;
import org.epics.vtype.VByte;
import org.epics.vtype.VDouble;
import org.epics.vtype.VDoubleArray;
import org.epics.vtype.VEnum;
import org.epics.vtype.VFloat;
import org.epics.vtype.VInt;
import org.epics.vtype.VIntArray;
import org.epics.vtype.VLong;
import org.epics.vtype.VNumberArray;
import org.epics.vtype.VShort;
import org.epics.vtype.VString;
import org.epics.vtype.VType;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import org.junit.BeforeClass;

/**
 *
 * @author carcassi
 */
public class VTypeToJsonTest {
    
    public static void compareJson(JsonObject json, String jsonName) {
        boolean success = false;
        try {
            JsonObject reference = loadJson(jsonName + ".json");
            assertThat(json, equalTo(reference));
            success = true;
        } finally {
            File failedJsonFile = new File("src/test/resources/org/epics/vtype/json/" + jsonName + ".failed.json");
            if (!success) {
                saveErrorJson(json, failedJsonFile);
            } else {
                if (failedJsonFile.exists()) {
                    failedJsonFile.delete();
                }
            }
        }
    }
    
    public void compareVType(VType expected, String jsonName) {
        VType actual = VTypeToJson.toVType(loadJson(jsonName + ".json"));
        assertThat("Type mismatch", VType.typeOf(actual), equalTo(VType.typeOf(expected)));
//        assertThat("Value mismatch", VTypeValueEquals.valueEquals(actual, expected), equalTo(true));
        assertThat("Alarm mismatch", Alarm.alarmOf(expected), equalTo(Alarm.alarmOf(actual)));
//        if (expected instanceof Time) {
//            assertThat("Time mismatch", VTypeValueEquals.timeEquals((Time) actual, (Time) expected), equalTo(true));
//        }
    }
    
    public static JsonObject loadJson(String jsonFile) {
        System.out.println(jsonFile);
        try (JsonReader reader = Json.createReader(VTypeToJsonTest.class.getResourceAsStream(jsonFile))) {
            return reader.readObject();
        }
    }
    
    public static void saveErrorJson(JsonObject json, File jsonFile) {
        StringWriter sw = new StringWriter();
        try (JsonWriter jsonWriter = Json.createWriterFactory(Collections.singletonMap(JsonGenerator.PRETTY_PRINTING, true)).createWriter(sw)) {
            jsonWriter.writeObject(json);
        }
        try (FileWriter fw = new FileWriter(jsonFile)) {
            fw.append(sw.toString().substring(1));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public VDouble vDouble1 = VDouble.of(3.14, Alarm.of(AlarmSeverity.MINOR, AlarmStatus.DB, "LOW"), Time.of(Instant.ofEpochSecond(0, 0)), Display.none());
    public VFloat vFloat1 = VFloat.of((float) 3.125, Alarm.of(AlarmSeverity.MINOR, AlarmStatus.DB, "HIGH"), Time.of(Instant.ofEpochSecond(0, 0)), Display.none());
    public VLong vLong1 = VLong.of(313L, Alarm.of(AlarmSeverity.MINOR, AlarmStatus.DB, "HIGH"), Time.of(Instant.ofEpochSecond(0, 0)), Display.none());
    public VInt vInt1 = VInt.of(314, Alarm.none(), Time.of(Instant.ofEpochSecond(0, 0)), Display.none());
    public VShort vShort1 = VShort.of((short) 314, Alarm.none(), Time.of(Instant.ofEpochSecond(0, 0)), Display.none());
    public VByte vByte1 = VByte.of((byte) 31, Alarm.none(), Time.of(Instant.ofEpochSecond(0, 0)), Display.none());
    public VString vString1 = VString.of("Flower", Alarm.none(), Time.of(Instant.ofEpochSecond(0, 0)));
    public VEnum vEnum1 = VEnum.of(1, EnumDisplay.of(Arrays.asList("One", "Two", "Three")), Alarm.none(), Time.of(Instant.ofEpochSecond(0, 0)));
    public VDoubleArray vDoubleArray1 = VDoubleArray.of(ArrayDouble.of(0.0, 0.1, 0.2), Alarm.none(), Time.of(Instant.ofEpochSecond(0, 0)), Display.none());
//    public VFloatArray vFloatArray = VFloatArray.create(Arrayofof(new float[] {0, 1, 2}), Alarm.none(), Time.create(Instant.oofSecond(0, 0)), Display.none());
//    public String vFloatArrayJson = "{\"type\":{\"name\":\"VFloatArray\",\"version\":1},"
//                + "\"value\":[0.0,1.0,2.0],"
//                + "\"alarm\":{\"severity\":\"NONE\",\"status\":\"None\"},"
//                + "\"time\":{\"unixSec\":0,\"nanoSec\":0,\"userTag\":null},"
//                + "\"display\":{\"lowAlarm\":null,\"highAlarm\":null,\"lowDisplay\":null,\"highDisplay\":null,\"lowWarning\":null,\"highWarning\":null,\"units\":\"\"}}";
//    public VLongArray vLongArray = VLongArray.create(ArrayLong.of(ofng[] {0, 1, 2}), Alarm.none(), Time.create(Instant.ofEpochSeof, 0)), Display.none());
//    public String vLongArrayJson = "{\"type\":{\"name\":\"VLongArray\",\"version\":1},"
//                + "\"value\":[0,1,2],"
//                + "\"alarm\":{\"severity\":\"NONE\",\"status\":\"None\"},"
//                + "\"time\":{\"unixSec\":0,\"nanoSec\":0,\"userTag\":null},"
//                + "\"display\":{\"lowAlarm\":null,\"highAlarm\":null,\"lowDisplay\":null,\"highDisplay\":null,\"lowWarning\":null,\"highWarning\":null,\"units\":\"\"}}";
//    public VIntArray vIntArray = VIntArray.create(ArrayInteger.of(new int[]of, 2}), Alarm.none(), Time.create(Instant.ofEpochSecond(0, ofisplay.none());
//    public String vIntArrayJson = "{\"type\":{\"name\":\"VIntArray\",\"version\":1},"
//                + "\"value\":[0,1,2],"
//                + "\"alarm\":{\"severity\":\"NONE\",\"status\":\"None\"},"
//                + "\"time\":{\"unixSec\":0,\"nanoSec\":0,\"userTag\":null},"
//                + "\"display\":{\"lowAlarm\":null,\"highAlarm\":null,\"lowDisplay\":null,\"highDisplay\":null,\"lowWarning\":null,\"highWarning\":null,\"units\":\"\"}}";
//    public VShortArray vShortArray = newVShortArray(ArrayShort.of(new short[] {0, 1, 2}), Alarm.none(), Time.create(Instant.ofEpochSecond(0, 0)),ofay.none());
//    public String vShortArrayJson = "{\"type\":{\"name\":\"VShortArray\",\"version\":1},"
//                + "\"value\":[0,1,2],"
//                + "\"alarm\":{\"severity\":\"NONE\",\"status\":\"None\"},"
//                + "\"time\":{\"unixSec\":0,\"nanoSec\":0,\"userTag\":null},"
//                + "\"display\":{\"lowAlarm\":null,\"highAlarm\":null,\"lowDisplay\":null,\"highDisplay\":null,\"lowWarning\":null,\"highWarning\":null,\"units\":\"\"}}";
//    public VNumberArray vByteArray = newVNumberArray(ArrayByte.of(new byte[]{0, 1, 2}), Alarm.none(), Time.create(Instant.ofEpochSecond(0, 0)), Disofone());
//    public String vByteArrayJson = "{\"type\":{\"name\":\"VByteArray\",\"version\":1},"
//            + "\"value\":[0,1,2],"
//            + "\"alarm\":{\"severity\":\"NONE\",\"status\":\"None\"},"
//            + "\"time\":{\"unixSec\":0,\"nanoSec\":0,\"userTag\":null},"
//            + "\"display\":{\"lowAlarm\":null,\"highAlarm\":null,\"lowDisplay\":null,\"highDisplay\":null,\"lowWarning\":null,\"highWarning\":null,\"units\":\"\"}}";
//    public VBooleanArray vBooleanArray = newVBooleanArray(new ArrayBoolean(true, false, true), Alarm.none(), Time.create(Instant.ofEpochSecond(0, 0)));
//    of String vBooleanArrayJson = "{\"type\":{\"name\":\"VBooleanArray\",\"version\":1},"
//            + "\"value\":[true,false,true],"
//            + "\"alarm\":{\"severity\":\"NONE\",\"status\":\"None\"},"
//            + "\"time\":{\"unixSec\":0,\"nanoSec\":0,\"userTag\":null}}";
//    public VStringArray vStringArray = newVStringArray(Arrays.asList("A", "B", "C"), Alarm.none(), Time.create(Instant.ofEpochSecond(0, 0)));
//    publofing vStringArrayJson = "{\"type\":{\"name\":\"VStringArray\",\"version\":1},"
//            + "\"value\":[\"A\",\"B\",\"C\"],"
//            + "\"alarm\":{\"severity\":\"NONE\",\"status\":\"None\"},"
//            + "\"time\":{\"unixSec\":0,\"nanoSec\":0,\"userTag\":null}}";
//    public VEnumArray vEnumArray = newVEnumArray(new ArrayInteger(1,0,1), Arrays.asList("One", "Two", "Three"), Alarm.none(), Time.create(Instant.ofEpochSecond(0, 0)));
//    public SofvEnumArrayJson = "{\"type\":{\"name\":\"VEnumArray\",\"version\":1},"
//            + "\"value\":[1,0,1],\"alarm\":{\"severity\":\"NONE\",\"status\":\"None\"},"
//            + "\"time\":{\"unixSec\":0,\"nanoSec\":0,\"userTag\":null},"
//            + "\"enum\":{\"labels\":[\"One\",\"Two\",\"Three\"]}}";
//    public VTable vTable = newVTable(Arrays.<Class<?>>asList(String.class, int.class, double.class), Arrays.asList("Name", "Index", "Value"), Arrays.asList(Arrays.asList("A", "B", "C"), new ArrayInteger(1,2,3), new ArrayDouble(3.14, 1.25, -0.1)));
//    public String vTableJson = "{\"type\":{\"name\":\"VTable\",\"version\":1},"
//            + "\"columnNames\":[\"Name\",\"Index\",\"Value\"],"
//            + "\"columnTypes\":[\"String\",\"int\",\"double\"],"
//            + "\"columnValues\":[[\"A\",\"B\",\"C\"],[1,2,3],[3.14,1.25,-0.1]]}";
//    public VTable vTable2 = newVTable(Arrays.<Class<?>>asList(String.class, int.class, double.class, Timestamp.class), Arrays.asList("Name", "Index", "Value", "Timestamp"), Arrays.asList(Arrays.asList("A", "B", "C"), new ArrayInteger(1,2,3), new ArrayDouble(3.14, 1.25, -0.1), Arrays.asList(Instant.ofEpochSecond(1234, 0), Instant.ofEpochSecond(2345, 0), Instant.ofEpochSecond(3456, 0))));
//    public String vTable2Json = "{\"type\":{\"name\":\"VTable\",\"version\":1},"
//            + "\"columnNames\":[\"Name\",\"Index\",\"Value\",\"Timestamp\"],"
//            + "\"columnTypes\":[\"String\",\"int\",\"double\",\"Timestamp\"],"
//            + "\"columnValues\":[[\"A\",\"B\",\"C\"],[1,2,3],[3.14,1.25,-0.1],[1234000,2345000,3456000]]}";
//    public VTable vTable3 = newVTable(Arrays.<Class<?>>asList(String.class, int.class, double.class, Timestamp.class), Arrays.asList("Name", "Index", "Value", "Timestamp"), Arrays.asList(Arrays.asList(null, "B", "C"), new ArrayInteger(1,2,3), new ArrayDouble(Double.NaN, 1.25, -0.1), Arrays.asList(Instant.ofEpochSecond(1234, 0), null, Instant.ofEpochSecond(3456, 123000000))));
//    public String vTable3Json = "{\"type\":{\"name\":\"VTable\",\"version\":1},"
//            + "\"columnNames\":[\"Name\",\"Index\",\"Value\",\"Timestamp\"],"
//            + "\"columnTypes\":[\"String\",\"int\",\"double\",\"Timestamp\"],"
//            + "\"columnValues\":[[\"\",\"B\",\"C\"],[1,2,3],[null,1.25,-0.1],[1234000,null,3456123]]}";
    
    @Test
    public void serializeVDouble() {
        compareJson(VTypeToJson.toJson(vDouble1), "VDouble1");
    }
    
    @Test
    public void serializeVFloat() {
        compareJson(VTypeToJson.toJson(vFloat1), "VFloat1");
    }

    @Test
    public void serializeVLong() {
        compareJson(VTypeToJson.toJson(vLong1), "VLong1");
    }

    @Test
    public void serializeVInt() {
        compareJson(VTypeToJson.toJson(vInt1), "VInt1");
    }

    @Test
    public void serializeVShort() {
        compareJson(VTypeToJson.toJson(vShort1), "VShort1");
    }

    @Test
    public void serializeVByte() {
        compareJson(VTypeToJson.toJson(vByte1), "VByte1");
    }

//    @Test
//    public void serializeVBoolean() {
//        compareJson(VTypeToJson.toJson(vBoolean), vBooleanJson);
//    }

    @Test
    public void serializeVString() {
        compareJson(VTypeToJson.toJson(vString1), "VString1");
    }
        
    @Test
    public void serializeVEnum() {
        compareJson(VTypeToJson.toJson(vEnum1), "VEnum1");
    }

    @Test
    public void serializeVDoubleArray() {
        compareJson(VTypeToJson.toJson(vDoubleArray1), "VDoubleArray1");
    }

//    @Test
//    public void serializeVFloatArray() {
//        compareJson(VTypeToJson.toJson(vFloatArray), vFloatArrayJson);
//    }
//
//    @Test
//    public void serializeVLongArray() {
//        compareJson(VTypeToJson.toJson(vLongArray), vLongArrayJson);
//    }
//
//    @Test
//    public void serializeVIntArray() {
//        compareJson(VTypeToJson.toJson(vIntArray), vIntArrayJson);
//    }
//
//    @Test
//    public void serializeVShortArray() {
//        compareJson(VTypeToJson.toJson(vShortArray), vShortArrayJson);
//    }
//
//    @Test
//    public void serializeVByteArray() {
//        compareJson(VTypeToJson.toJson(vByteArray), vByteArrayJson);
//    }
//
//    @Test
//    public void serializeVBooleanArray() {
//        compareJson(VTypeToJson.toJson(vBooleanArray), vBooleanArrayJson);
//    }
//
//    @Test
//    public void serializeVStringArray() {
//        compareJson(VTypeToJson.toJson(vStringArray), vStringArrayJson);
//    }
//
//    @Test
//    public void serializeVEnumArray() {
//        compareJson(VTypeToJson.toJson(vEnumArray), vEnumArrayJson);
//    }
//
//    @Test
//    public void serializeVTable1() {
//        compareJson(VTypeToJson.toJson(vTable), vTableJson);
//    }
//
//    @Test
//    public void serializeVTable2() {
//        compareJson(VTypeToJson.toJson(vTable2), vTable2Json);
//    }
//
//    @Test
//    public void serializeVTable3() {
//        compareJson(VTypeToJson.toJson(vTable3), vTable3Json);
//    }

    @Test
    public void parseVDouble() {
        compareVType(vDouble1, "VDouble1");
    }

    @Test
    public void parseVFloat() {
        compareVType(vFloat1, "VFloat1");
    }

    @Test
    public void parseVLong() {
        compareVType(vLong1, "VLong1");
    }

    @Test
    public void parseVInt() {
        compareVType(vInt1, "VInt1");
    }

    @Test
    public void parseVShort() {
        compareVType(vShort1, "VShort1");
    }

    @Test
    public void parseVByte() {
        compareVType(vByte1, "VByte1");
    }

//    @Test
//    public void parseVBoolean() {
//        compareVType(vBoolean, VTypeToJson.toVType(parseJson(vBooleanJson)));
//    }

    @Test
    public void parseVString() {
        compareVType(vString1, "VString1");
    }

    @Test
    public void parseVEnum() {
        compareVType(vEnum1, "VEnum1");
    }

    @Test
    public void parseVDoubleArray() {
        compareVType(vDoubleArray1, "VDoubleArray1");
    }

//    @Test
//    public void parseVFloatArray() {
//        compareVType(vFloatArray, VTypeToJson.toVType(parseJson(vFloatArrayJson)));
//    }
//
//    @Test
//    public void parseVLongArray() {
//        compareVType(vLongArray, VTypeToJson.toVType(parseJson(vLongArrayJson)));
//    }
//
//    @Test
//    public void parseVIntArray() {
//        compareVType(vIntArray, VTypeToJson.toVType(parseJson(vIntArrayJson)));
//    }
//
//    @Test
//    public void parseVShortArray() {
//        compareVType(vShortArray, VTypeToJson.toVType(parseJson(vShortArrayJson)));
//    }
//
//    @Test
//    public void parseVByteArray() {
//        compareVType(vByteArray, VTypeToJson.toVType(parseJson(vByteArrayJson)));
//    }
//
//    @Test
//    public void parseVBooleanArray() {
//        compareVType(vBooleanArray, VTypeToJson.toVType(parseJson(vBooleanArrayJson)));
//    }
//
//    @Test
//    public void parseVStringArray() {
//        compareVType(vStringArray, VTypeToJson.toVType(parseJson(vStringArrayJson)));
//    }
//
//    @Test
//    public void parseVEnumArray() {
//        compareVType(vEnumArray, VTypeToJson.toVType(parseJson(vEnumArrayJson)));
//    }
//
//    @Test
//    public void parseVTable() {
//        compareVType(vTable, VTypeToJson.toVType(parseJson(vTableJson)));
//    }
//
//    @Test
//    public void parseVTable2() {
//        compareVType(vTable2, VTypeToJson.toVType(parseJson(vTable2Json)));
//    }
    
}
