/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.vtype.json;

import org.epics.util.array.*;
import org.epics.util.number.UByte;
import org.epics.util.number.UInteger;
import org.epics.util.number.ULong;
import org.epics.util.number.UShort;
import org.epics.vtype.*;
import org.joda.time.Instant;
import org.junit.Test;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonWriter;
import javax.json.stream.JsonGenerator;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

/**
 * @author carcassi
 */
public class VTypeToJsonTest {

    /**
     * Serializes the given value and compares the output with the given JSON file.
     *
     * @param value                the value to serialize
     * @param expectedJsonFileName the filename to compare
     */
    public static void testSerialization(VType value, String expectedJsonFileName) {
        JsonObject json = VTypeToJson.toJson(value);

        boolean success = false;
        try {
            JsonObject reference = loadJson(expectedJsonFileName + ".json");
            assertThat(json, equalTo(reference));
            success = true;
        } finally {
            File failedJsonFile = new File("src/test/resources/org/epics/vtype/json/" + expectedJsonFileName + ".failed.json");
            if (!success) {
                saveErrorJson(json, failedJsonFile);
            } else {
                if (failedJsonFile.exists()) {
                    failedJsonFile.delete();
                }
            }
        }
    }

    /**
     * Deserializes the JSON file and compares the value with the given VType object.
     *
     * @param jsonFileName the JSON file to deserialize
     * @param expected     the object to compare to
     */
    public static void testDeserialization(String jsonFileName, VType expected) {
        VType actual = VTypeToJson.toVType(loadJson(jsonFileName + ".json"));
        assertThat(actual, equalTo(expected));
    }

    public static JsonObject loadJson(String jsonFile) {
        JsonReader reader = null;
        try {
            reader = Json.createReader(VTypeToJsonTest.class.getResourceAsStream(jsonFile));
            return reader.readObject();
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    public static void saveErrorJson(JsonObject json, File jsonFile) {
        StringWriter sw = new StringWriter();
        JsonWriter jsonWriter = null;
        try {
            jsonWriter = Json.createWriterFactory(Collections.singletonMap(JsonGenerator.PRETTY_PRINTING, true)).createWriter(sw);
            jsonWriter.writeObject(json);
        } finally {
            if ( jsonWriter != null ) {
                jsonWriter.close();
            }
        }
        FileWriter fw = null;
        try {
            fw = new FileWriter(jsonFile);
            fw.append(sw.toString().substring(1));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } finally {
            if ( fw != null ) {
                try {
                    fw.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Test
    public void vDouble1() {
        VDouble vDouble1 = VDouble.of(3.14, Alarm.of(AlarmSeverity.MINOR, AlarmStatus.DB, "LOW"), Time.of(Instant.ofEpochSecond(0)), Display.none());
        testSerialization(vDouble1, "VDouble1");
        testDeserialization("VDouble1", vDouble1);
        testDeserialization("VDouble1a", vDouble1);
    }

    @Test
    public void vFloat1() {
        VFloat vFloat1 = VFloat.of((float) 3.125, Alarm.of(AlarmSeverity.MINOR, AlarmStatus.DB, "HIGH"), Time.of(Instant.ofEpochSecond(0)), Display.none());
        testSerialization(vFloat1, "VFloat1");
        testDeserialization("VFloat1", vFloat1);
        testDeserialization("VFloat1a", vFloat1);
    }

    @Test
    public void vULong1() {
        VULong vULong1 = VULong.of(new ULong(-1), Alarm.of(AlarmSeverity.MINOR, AlarmStatus.DB, "HIGH"), Time.of(Instant.ofEpochSecond(0)), Display.none());
        testSerialization(vULong1, "VULong1");
        testDeserialization("VULong1", vULong1);
    }

    @Test
    public void vLong1() {
        VLong vLong1 = VLong.of(313L, Alarm.of(AlarmSeverity.MINOR, AlarmStatus.DB, "HIGH"), Time.of(Instant.ofEpochSecond(0)), Display.none());
        testSerialization(vLong1, "VLong1");
        testDeserialization("VLong1", vLong1);
        testDeserialization("VLong1a", vLong1);
    }

    @Test
    public void vUInt1() {
        VUInt vUInt1 = VUInt.of(new UInteger(-1), Alarm.none(), Time.of(Instant.ofEpochSecond(0)), Display.none());
        testSerialization(vUInt1, "VUInt1");
        testDeserialization("VUInt1", vUInt1);
    }

    @Test
    public void vInt1() {
        VInt vInt1 = VInt.of(314, Alarm.none(), Time.of(Instant.ofEpochSecond(0)), Display.none());
        testSerialization(vInt1, "VInt1");
        testDeserialization("VInt1", vInt1);
        testDeserialization("VInt1a", vInt1);
    }

    @Test
    public void vUShort1() {
        VUShort vUShort1 = VUShort.of(new UShort((short) -1), Alarm.of(AlarmSeverity.MINOR, AlarmStatus.DB, "HIGH"), Time.of(Instant.ofEpochSecond(0)), Display.none());
        testSerialization(vUShort1, "VUShort1");
        testDeserialization("VUShort1", vUShort1);
    }

    @Test
    public void vShort1() {
        VShort vShort1 = VShort.of((short) 314, Alarm.none(), Time.of(Instant.ofEpochSecond(0)), Display.none());
        testSerialization(vShort1, "VShort1");
        testDeserialization("VShort1", vShort1);
        testDeserialization("VShort1a", vShort1);
    }

    @Test
    public void vUByte1() {
        VUByte vUByte1 = VUByte.of(new UByte((byte) -1), Alarm.of(AlarmSeverity.MINOR, AlarmStatus.DB, "HIGH"), Time.of(Instant.ofEpochSecond(0)), Display.none());
        testSerialization(vUByte1, "VUByte1");
        testDeserialization("VUByte1", vUByte1);
    }

    @Test
    public void vByte1() {
        VByte vByte1 = VByte.of((byte) 31, Alarm.none(), Time.of(Instant.ofEpochSecond(0)), Display.none());
        testSerialization(vByte1, "VByte1");
        testDeserialization("VByte1", vByte1);
        testDeserialization("VByte1a", vByte1);
    }

    @Test
    public void vString1() {
        VString vString1 = VString.of("Flower", Alarm.none(), Time.of(Instant.ofEpochSecond(0)));
        testSerialization(vString1, "VString1");
        testDeserialization("VString1", vString1);
        testDeserialization("VString1a", vString1);
    }

    @Test
    public void vEnum1() {
        VEnum vEnum1 = VEnum.of(1, EnumDisplay.of(Arrays.asList("One", "Two", "Three")), Alarm.none(), Time.of(Instant.ofEpochSecond(0)));
        testSerialization(vEnum1, "VEnum1");
        testDeserialization("VEnum1", vEnum1);
        testDeserialization("VEnum1a", vEnum1);
    }

    @Test
    public void vDoubleArray1() {
        VDoubleArray vDoubleArray1 = VDoubleArray.of(ArrayDouble.of(0, 1, 2), Alarm.none(), Time.of(Instant.ofEpochSecond(0)), Display.none());
        testSerialization(vDoubleArray1, "VDoubleArray1");
        testDeserialization("VDoubleArray1", vDoubleArray1);
        testDeserialization("VDoubleArray1a", vDoubleArray1);
    }

    @Test
    public void vFloatArray1() {
        VFloatArray vFloatArray1 = VFloatArray.of(ArrayFloat.of(0, 1, 2), Alarm.none(), Time.of(Instant.ofEpochSecond(0)), Display.none());
        testSerialization(vFloatArray1, "VFloatArray1");
        testDeserialization("VFloatArray1", vFloatArray1);
        testDeserialization("VFloatArray1a", vFloatArray1);
    }

    @Test
    public void vULongArray1() {
        VULongArray vULongArray1 = VULongArray.of(ArrayULong.of(-1, -2, -3), Alarm.none(), Time.of(Instant.ofEpochSecond(0)), Display.none());
        testSerialization(vULongArray1, "VULongArray1");
        testDeserialization("VULongArray1", vULongArray1);
    }

    @Test
    public void vLongArray1() {
        VLongArray vLongArray1 = VLongArray.of(ArrayLong.of(0, 1, 2), Alarm.none(), Time.of(Instant.ofEpochSecond(0)), Display.none());
        testSerialization(vLongArray1, "VLongArray1");
        testDeserialization("VLongArray1", vLongArray1);
    }

    @Test
    public void vUIntArray1() {
        VUIntArray vUIntArray1 = VUIntArray.of(ArrayUInteger.of(-1, -2, -3), Alarm.none(), Time.of(Instant.ofEpochSecond(0)), Display.none());
        testSerialization(vUIntArray1, "VUIntArray1");
        testDeserialization("VUIntArray1", vUIntArray1);
    }

    @Test
    public void vIntArray1() {
        VIntArray vIntArray1 = VIntArray.of(ArrayInteger.of(0, 1, 2), Alarm.none(), Time.of(Instant.ofEpochSecond(0)), Display.none());
        testSerialization(vIntArray1, "VIntArray1");
        testDeserialization("VIntArray1", vIntArray1);
    }

    @Test
    public void vUShortArray1() {
        VUShortArray vUShortArray1 = VUShortArray.of(ArrayUShort.of(new short[]{-1, -2, -3}), Alarm.none(), Time.of(Instant.ofEpochSecond(0)), Display.none());
        testSerialization(vUShortArray1, "VUShortArray1");
        testDeserialization("VUShortArray1", vUShortArray1);
    }

    @Test
    public void vShortArray1() {
        VShortArray vShortArray1 = VShortArray.of(ArrayShort.of(new short[]{0, 1, 2}), Alarm.none(), Time.of(Instant.ofEpochSecond(0)), Display.none());
        testSerialization(vShortArray1, "VShortArray1");
        testDeserialization("VShortArray1", vShortArray1);
    }

    @Test
    public void vUByteArray1() {
        VUByteArray vUByteArray1 = VUByteArray.of(ArrayUByte.of(new byte[]{-1, -2, -3}), Alarm.none(), Time.of(Instant.ofEpochSecond(0)), Display.none());
        testSerialization(vUByteArray1, "VUByteArray1");
        testDeserialization("VUByteArray1", vUByteArray1);
    }

    @Test
    public void vByteArray1() {
        VByteArray vByteArray1 = VByteArray.of(ArrayByte.of(new byte[]{0, 1, 2}), Alarm.none(), Time.of(Instant.ofEpochSecond(0)), Display.none());
        testSerialization(vByteArray1, "VByteArray1");
        testDeserialization("VByteArray1", vByteArray1);
        testDeserialization("VByteArray1a", vByteArray1);
    }

    @Test
    public void vStringArray1() {
        List<String> data = new ArrayList<String>();
        data.add("a");
        data.add("b");
        data.add("c");
        VStringArray vStringArray1 = VStringArray.of(data, Alarm.none(), Time.of(Instant.ofEpochSecond(0)));
        testSerialization(vStringArray1, "VStringArray1");
        testDeserialization("VStringArray1", vStringArray1);
    }

    /**
     * Tests serialization and de-serialization of a Double.NaN. See {@link JsonVTypeBuilder#add} and
     * {@link VTypeToJsonV1#toVNumber}
     */
    @Test
    public void testDoubleNaN() {
        VDouble vDouble = VDouble.of(Double.NaN, Alarm.none(), Time.of(Instant.EPOCH), Display.none());
        JsonObject jsonObject = VTypeToJson.toJson(vDouble);
        assertEquals("NaN", jsonObject.getString("value"));
        vDouble = (VDouble) VTypeToJson.toVType(jsonObject);
        assertTrue(Double.isNaN(vDouble.getValue()));
    }

    @Test
    public void testDoublePositiveInfinity() {
        VDouble vDouble = VDouble.of(Double.POSITIVE_INFINITY, Alarm.none(), Time.of(Instant.EPOCH), Display.none());
        JsonObject jsonObject = VTypeToJson.toJson(vDouble);
        assertEquals(Double.toString(Double.POSITIVE_INFINITY), jsonObject.getString("value"));
        vDouble = (VDouble) VTypeToJson.toVType(jsonObject);
        assertTrue(vDouble.getValue().equals(Double.POSITIVE_INFINITY));
    }

    @Test
    public void testDoubleNegativeInfinity() {
        VDouble vDouble = VDouble.of(Double.NEGATIVE_INFINITY, Alarm.none(), Time.of(Instant.EPOCH), Display.none());
        JsonObject jsonObject = VTypeToJson.toJson(vDouble);
        assertEquals(VTypeJsonMapper.NEG_INF, jsonObject.getString("value"));
        vDouble = (VDouble) VTypeToJson.toVType(jsonObject);
        assertTrue(vDouble.getValue().equals(Double.NEGATIVE_INFINITY));
    }

    @Test
    public void testDoubleNaNInArray() {
        VDoubleArray vDoubleArray1 = VDoubleArray.of(ArrayDouble.of(0, Double.NaN, 2), Alarm.none(), Time.of(Instant.ofEpochSecond(0)), Display.none());
        JsonObject jsonObject = VTypeToJson.toJson(vDoubleArray1);
        List valueObject = (List) jsonObject.get("value");
        assertEquals(VTypeJsonMapper.NAN_QUOTED, valueObject.get(1).toString());
        vDoubleArray1 = (VDoubleArray) VTypeToJson.toVType(jsonObject);
        assertTrue(Double.isNaN(vDoubleArray1.getData().getDouble(1)));
    }

    @Test
    public void testDoublePositiveInfinityInArray() {
        VDoubleArray vDoubleArray1 = VDoubleArray.of(ArrayDouble.of(0, Double.POSITIVE_INFINITY, 2), Alarm.none(), Time.of(Instant.ofEpochSecond(0)), Display.none());
        JsonObject jsonObject = VTypeToJson.toJson(vDoubleArray1);
        List valueObject = (List) jsonObject.get("value");
        assertEquals(VTypeJsonMapper.POS_INF_QUOTED, valueObject.get(1).toString());
        vDoubleArray1 = (VDoubleArray) VTypeToJson.toVType(jsonObject);
        assertTrue(Double.isInfinite(vDoubleArray1.getData().getDouble(1)));
    }

    @Test
    public void testDoubleNegativeInfinityInArray() {
        VDoubleArray vDoubleArray1 = VDoubleArray.of(ArrayDouble.of(0, Double.NEGATIVE_INFINITY, 2), Alarm.none(), Time.of(Instant.ofEpochSecond(0)), Display.none());
        JsonObject jsonObject = VTypeToJson.toJson(vDoubleArray1);
        List valueObject = (List) jsonObject.get("value");
        assertEquals(VTypeJsonMapper.NEG_INF_QUOTED, valueObject.get(1).toString());
        vDoubleArray1 = (VDoubleArray) VTypeToJson.toVType(jsonObject);
        assertTrue(Double.isInfinite(vDoubleArray1.getData().getDouble(1)));
    }

    @Test(expected = NumberFormatException.class)
    public void testGetDoubleFromStringNonParsableValue() {
        VTypeToJsonV1.getDoubleFromJsonString("invalid");
    }
}
