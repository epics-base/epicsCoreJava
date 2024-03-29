/**
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.vtype.gson;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;
import org.epics.util.array.*;
import org.epics.util.number.UByte;
import org.epics.util.number.UInteger;
import org.epics.util.number.ULong;
import org.epics.util.number.UShort;
import org.epics.util.stats.Range;
import org.epics.vtype.*;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 * Testing Gson de/serializer.
 *
 * @author <a href='mailto:changj@frib.msu.edu'>Genie Jhang</a>
 *
 * Original author:
 * @author carcassi
 */
public class VTypeToGsonTest {

    /**
     * Serializes the given value and compares the output with the given JSON file.
     *
     * @param value the value to serialize
     * @param expectedJsonFileName the filename to compare
     */
    public static void testSerialization(VType value, String expectedJsonFileName) {
        JsonElement json = JsonParser.parseString(CustomGson.getGson().toJson(value, VType.class));

        boolean success = false;
        try {
            JsonElement reference = loadJson(expectedJsonFileName + ".json");
            assertThat(json, equalTo(reference));
            success = true;
        } finally{
            File failedJsonFile = new File("src/test/resources/org/epics/vtype/gson/" + expectedJsonFileName + ".failed.json");
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
     * @param expected the object to compare to
     */
    public static void testDeserialization(String jsonFileName, VType expected) {
        VType actual = CustomGson.getGson().fromJson(loadJson(jsonFileName + ".json"), VType.class);
        assertThat(actual, equalTo(expected));
    }

    public static JsonElement loadJson(String jsonFile) {
        InputStream stream = VTypeToGsonTest.class.getResourceAsStream(jsonFile);
        InputStreamReader inputStreamReader = new InputStreamReader(stream);
        return JsonParser.parseReader(inputStreamReader);
    }

    public static void saveErrorJson(JsonElement json, File jsonFile) {
        try (FileWriter fw = new FileWriter(jsonFile)) {
            fw.append(CustomGson.getGson().toJson(json));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Test
    public void vDouble1() {
        VDouble vDouble1 = VDouble.of(3.14, Alarm.of(AlarmSeverity.MINOR, AlarmStatus.DB, "LOW"), Time.of(Instant.ofEpochSecond(0, 0)), Display.none());
        testSerialization(vDouble1, "VDouble1");
        testDeserialization("VDouble1", vDouble1);
        testDeserialization("VDouble1a", vDouble1);
    }

    @Test
    public void vDouble1b() {
        VDouble vDouble1 = VDouble.of(3.14, Alarm.of(AlarmSeverity.MINOR,
                AlarmStatus.DB, "LOW"),
                Time.of(Instant.ofEpochSecond(0, 0)),
                Display.of(Range.of(-100.0, 100.0), Range.of(-100.0, 100.0), Range.of(-10.0, 10.0), Range.of(Double.NaN, Double.NaN), "degC", new DecimalFormat(), null));
        testSerialization(vDouble1, "VDouble1b");
        testDeserialization("VDouble1b", vDouble1);
    }

    @Test
    public void vDouble1c() {
        VDouble vDouble1 = VDouble.of(3.14, Alarm.of(AlarmSeverity.MINOR,
                        AlarmStatus.DB, "LOW"),
                Time.of(Instant.ofEpochSecond(0, 0)),
                Display.of(Range.of(-100.0, 100.0), Range.of(-100.0, 100.0), Range.of(-10.0, 10.0), Range.of(Double.NaN, Double.NaN), "degC", new DecimalFormat(), "fooBar"));
        testSerialization(vDouble1, "VDouble1c");
        testDeserialization("VDouble1c", vDouble1);
    }


    @Test
    public void vDouble2() {
        VDouble vDouble1 = VDouble.of(3.14, Alarm.of(AlarmSeverity.MINOR, AlarmStatus.DB, "LOW"), Time.of(Instant.ofEpochSecond(0, 0)), Display.none());
        testSerialization(vDouble1, "VDouble1");
        testDeserialization("VDouble1", vDouble1);
        testDeserialization("VDouble1a", vDouble1);
    }

    @Test
    public void vFloat1() {
        VFloat vFloat1 = VFloat.of((float) 3.125, Alarm.of(AlarmSeverity.MINOR, AlarmStatus.DB, "HIGH"), Time.of(Instant.ofEpochSecond(0, 0)), Display.none());
        testSerialization(vFloat1, "VFloat1");
        testDeserialization("VFloat1", vFloat1);
        testDeserialization("VFloat1a", vFloat1);
    }

    @Test
    public void vULong1() {
        VULong vULong1 = VULong.of(new ULong(-1), Alarm.of(AlarmSeverity.MINOR, AlarmStatus.DB, "HIGH"), Time.of(Instant.ofEpochSecond(0, 0)), Display.none());
        testSerialization(vULong1, "VULong1");
        testDeserialization("VULong1", vULong1);
    }

    @Test
    public void vLong1() {
        VLong vLong1 = VLong.of(313L, Alarm.of(AlarmSeverity.MINOR, AlarmStatus.DB, "HIGH"), Time.of(Instant.ofEpochSecond(0, 0)), Display.none());
        testSerialization(vLong1, "VLong1");
        testDeserialization("VLong1", vLong1);
        testDeserialization("VLong1a", vLong1);
    }

    @Test
    public void vUInt1() {
        VUInt vUInt1 = VUInt.of(new UInteger(-1), Alarm.none(), Time.of(Instant.ofEpochSecond(0, 0)), Display.none());
        testSerialization(vUInt1, "VUInt1");
        testDeserialization("VUInt1", vUInt1);
    }

    @Test
    public void vInt1() {
        VInt vInt1 = VInt.of(314, Alarm.none(), Time.of(Instant.ofEpochSecond(0, 0)), Display.none());
        testSerialization(vInt1, "VInt1");
        testDeserialization("VInt1", vInt1);
        testDeserialization("VInt1a", vInt1);
    }

    @Test
    public void vUShort1() {
        VUShort vUShort1 = VUShort.of(new UShort((short) -1), Alarm.of(AlarmSeverity.MINOR, AlarmStatus.DB, "HIGH"), Time.of(Instant.ofEpochSecond(0, 0)), Display.none());
        testSerialization(vUShort1, "VUShort1");
        testDeserialization("VUShort1", vUShort1);
    }

    @Test
    public void vShort1() {
        VShort vShort1 = VShort.of((short) 314, Alarm.none(), Time.of(Instant.ofEpochSecond(0, 0)), Display.none());
        testSerialization(vShort1, "VShort1");
        testDeserialization("VShort1", vShort1);
        testDeserialization("VShort1a", vShort1);
    }

    @Test
    public void vUByte1() {
        VUByte vUByte1 = VUByte.of(new UByte((byte) -1), Alarm.of(AlarmSeverity.MINOR, AlarmStatus.DB, "HIGH"), Time.of(Instant.ofEpochSecond(0, 0)), Display.none());
        testSerialization(vUByte1, "VUByte1");
        testDeserialization("VUByte1", vUByte1);
    }

    @Test
    public void vByte1() {
        VByte vByte1 = VByte.of((byte) 31, Alarm.none(), Time.of(Instant.ofEpochSecond(0, 0)), Display.none());
        testSerialization(vByte1, "VByte1");
        testDeserialization("VByte1", vByte1);
        testDeserialization("VByte1a", vByte1);
    }

    @Test
    public void vString1() {
        VString vString1 = VString.of("Flower", Alarm.none(), Time.of(Instant.ofEpochSecond(0, 0)));
        testSerialization(vString1, "VString1");
        testDeserialization("VString1", vString1);
        testDeserialization("VString1a", vString1);
    }

    @Test
    public void vEnum1() {
        VEnum vEnum1 = VEnum.of(1, EnumDisplay.of(Arrays.asList("One", "Two", "Three")), Alarm.none(), Time.of(Instant.ofEpochSecond(0, 0)));
        testSerialization(vEnum1, "VEnum1");
        testDeserialization("VEnum1", vEnum1);
        testDeserialization("VEnum1a", vEnum1);
    }

    @Test
    public void vDoubleArray1() {
        VDoubleArray vDoubleArray1 = VDoubleArray.of(ArrayDouble.of(0, 1, 2), Alarm.none(), Time.of(Instant.ofEpochSecond(0, 0)), Display.none());
        testSerialization(vDoubleArray1, "VDoubleArray1");
        testDeserialization("VDoubleArray1", vDoubleArray1);
        testDeserialization("VDoubleArray1a", vDoubleArray1);
    }

    @Test
    public void vFloatArray1() {
        VFloatArray vFloatArray1 = VFloatArray.of(ArrayFloat.of(0, 1, 2), Alarm.none(), Time.of(Instant.ofEpochSecond(0, 0)), Display.none());
        testSerialization(vFloatArray1, "VFloatArray1");
        testDeserialization("VFloatArray1", vFloatArray1);
        testDeserialization("VFloatArray1a", vFloatArray1);
    }

    @Test
    public void vULongArray1() {
        VULongArray vULongArray1 = VULongArray.of(ArrayULong.of(-1, -2, -3), Alarm.none(), Time.of(Instant.ofEpochSecond(0, 0)), Display.none());
        testSerialization(vULongArray1, "VULongArray1");
        testDeserialization("VULongArray1", vULongArray1);
    }

    @Test
    public void vLongArray1() {
        VLongArray vLongArray1 = VLongArray.of(ArrayLong.of(0, 1, 2), Alarm.none(), Time.of(Instant.ofEpochSecond(0, 0)), Display.none());
        testSerialization(vLongArray1, "VLongArray1");
        testDeserialization("VLongArray1", vLongArray1);
    }

    @Test
    public void vUIntArray1() {
        VUIntArray vUIntArray1 = VUIntArray.of(ArrayUInteger.of(-1, -2, -3), Alarm.none(), Time.of(Instant.ofEpochSecond(0, 0)), Display.none());
        testSerialization(vUIntArray1, "VUIntArray1");
        testDeserialization("VUIntArray1", vUIntArray1);
    }

    @Test
    public void vIntArray1() {
        VIntArray vIntArray1 = VIntArray.of(ArrayInteger.of(0, 1, 2), Alarm.none(), Time.of(Instant.ofEpochSecond(0, 0)), Display.none());
        testSerialization(vIntArray1, "VIntArray1");
        testDeserialization("VIntArray1", vIntArray1);
    }

    @Test
    public void vUShortArray1() {
        VUShortArray vUShortArray1 = VUShortArray.of(ArrayUShort.of(new short[] {-1, -2, -3}), Alarm.none(), Time.of(Instant.ofEpochSecond(0, 0)), Display.none());
        testSerialization(vUShortArray1, "VUShortArray1");
        testDeserialization("VUShortArray1", vUShortArray1);
    }

    @Test
    public void vShortArray1() {
        VShortArray vShortArray1 = VShortArray.of(ArrayShort.of(new short[] {0, 1, 2}), Alarm.none(), Time.of(Instant.ofEpochSecond(0, 0)), Display.none());
        testSerialization(vShortArray1, "VShortArray1");
        testDeserialization("VShortArray1", vShortArray1);
    }

    @Test
    public void vUByteArray1() {
        VUByteArray vUByteArray1 = VUByteArray.of(ArrayUByte.of(new byte[] {-1, -2, -3}), Alarm.none(), Time.of(Instant.ofEpochSecond(0, 0)), Display.none());
        testSerialization(vUByteArray1, "VUByteArray1");
        testDeserialization("VUByteArray1", vUByteArray1);
    }

    @Test
    public void vByteArray1() {
        VByteArray vByteArray1 = VByteArray.of(ArrayByte.of(new byte[] {0, 1, 2}), Alarm.none(), Time.of(Instant.ofEpochSecond(0, 0)), Display.none());
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
        VStringArray vStringArray1 = VStringArray.of(data, Alarm.none(), Time.of(Instant.ofEpochSecond(0, 0)));
        testSerialization(vStringArray1, "VStringArray1");
        testDeserialization("VStringArray1", vStringArray1);
    }

    /**
     * Tests serialization and de-serialization of a Double.NaN. See {@link GsonVTypeBuilder#add} and
     * {@link VTypeToGson#toVNumber}
     */
    @Test
    public void testDoubleNaN(){
        VDouble vDouble = VDouble.of(Double.NaN, Alarm.none(), Time.of(Instant.EPOCH), Display.none());
        JsonElement jsonElement = VTypeToGson.toJson(vDouble);
        assertEquals("NaN", jsonElement.getAsJsonObject().get("value").getAsString());
        vDouble = (VDouble)VTypeToGson.toVType(jsonElement);
        assertTrue(Double.isNaN(vDouble.getValue()));
    }

    @Test
    public void testDoublePositiveInfinity(){
        VDouble vDouble = VDouble.of(Double.POSITIVE_INFINITY, Alarm.none(), Time.of(Instant.EPOCH), Display.none());
        JsonElement jsonElement = VTypeToGson.toJson(vDouble);
        assertEquals(Double.toString(Double.POSITIVE_INFINITY), jsonElement.getAsJsonObject().get("value").getAsString());
        vDouble = (VDouble)VTypeToGson.toVType(jsonElement);
        assertTrue(vDouble.getValue().equals(Double.POSITIVE_INFINITY));
    }

    @Test
    public void testDoubleNegativeInfinity(){
        VDouble vDouble = VDouble.of(Double.NEGATIVE_INFINITY, Alarm.none(), Time.of(Instant.EPOCH), Display.none());
        JsonElement jsonElement = VTypeToGson.toJson(vDouble);
        assertEquals(VTypeGsonMapper.NEG_INF, jsonElement.getAsJsonObject().get("value").getAsString());
        vDouble = (VDouble)VTypeToGson.toVType(jsonElement);
        assertTrue(vDouble.getValue().equals(Double.NEGATIVE_INFINITY));
    }

    @Test
    public void testDoubleNaNInArray(){
        VDoubleArray vDoubleArray1 = VDoubleArray.of(ArrayDouble.of(0, Double.NaN, 2), Alarm.none(), Time.of(Instant.ofEpochSecond(0, 0)), Display.none());
        JsonElement jsonElement = VTypeToGson.toJson(vDoubleArray1);
        List valueObject = CustomGson.getGson().fromJson(jsonElement.getAsJsonObject().get("value"), new TypeToken<List<JsonPrimitive>>() {}.getType());
        assertEquals(VTypeGsonMapper.NAN_QUOTED, valueObject.get(1).toString());
        vDoubleArray1 = (VDoubleArray)VTypeToGson.toVType(jsonElement);
        assertTrue(Double.isNaN(vDoubleArray1.getData().getDouble(1)));
    }

    @Test
    public void testDoublePositiveInfinityInArray(){
        VDoubleArray vDoubleArray1 = VDoubleArray.of(ArrayDouble.of(0, Double.POSITIVE_INFINITY, 2), Alarm.none(), Time.of(Instant.ofEpochSecond(0, 0)), Display.none());
        JsonElement jsonElement = VTypeToGson.toJson(vDoubleArray1);
        List valueObject = CustomGson.getGson().fromJson(jsonElement.getAsJsonObject().get("value"), new TypeToken<List<JsonPrimitive>>() {}.getType());
        assertEquals(VTypeGsonMapper.POS_INF_QUOTED, valueObject.get(1).toString());
        vDoubleArray1 = (VDoubleArray)VTypeToGson.toVType(jsonElement);
        assertTrue(Double.isInfinite(vDoubleArray1.getData().getDouble(1)));
    }

    @Test
    public void testDoubleNegativeInfinityInArray(){
        VDoubleArray vDoubleArray1 = VDoubleArray.of(ArrayDouble.of(0, Double.NEGATIVE_INFINITY, 2), Alarm.none(), Time.of(Instant.ofEpochSecond(0, 0)), Display.none());
        JsonElement jsonElement = VTypeToGson.toJson(vDoubleArray1);
        List valueObject = CustomGson.getGson().fromJson(jsonElement.getAsJsonObject().get("value"), new TypeToken<List<JsonPrimitive>>() {}.getType());
        assertEquals(VTypeGsonMapper.NEG_INF_QUOTED, valueObject.get(1).toString());
        vDoubleArray1 = (VDoubleArray)VTypeToGson.toVType(jsonElement);
        assertTrue(Double.isInfinite(vDoubleArray1.getData().getDouble(1)));
    }

    @Test(expected = NumberFormatException.class)
    public void testGetDoubleFromStringNonParsableValue(){
        VTypeToGson.getDoubleFromJsonString("invalid");
    }

    @Test
    public void testVTable(){
        List<Class<?>> types = Arrays.asList(
                Boolean.TYPE,
                Byte.TYPE,
                Byte.TYPE,
                Short.TYPE,
                Short.TYPE,
                Integer.TYPE,
                Integer.TYPE,
                Long.TYPE,
                Long.TYPE,
                Double.TYPE,
                Float.TYPE,
                String.class,
                Long.TYPE);
        List<String> names = Arrays.asList("bool", "byte", "ubyte", "short", "ushort", "int", "uint", "long", "ulong", "double", "float", "string", "empty");
        boolean[] bools = {true, false, true};
        ArrayBoolean boolValues = ArrayBoolean.of(bools);

        byte[] bytes = {(byte)-1, (byte)2, (byte)3};
        ArrayByte byteValues = ArrayByte.of((byte)-1, (byte)2, (byte)3);
        byte[] ubytes = {(byte)1, (byte)2, (byte)3};
        ArrayUByte ubyteValues = ArrayUByte.of(ubytes);

        short[] shorts = {(short)-1, (short)2, (short)3};
        ArrayShort shortValues = ArrayShort.of(shorts);
        short[] ushorts = {(short)1, (short)2, (short)3};
        ArrayUShort ushortValues = ArrayUShort.of(ushorts);

        int[] ints = {-1, 2, 3};
        ArrayInteger intValues = ArrayInteger.of(ints);
        int[] uints = {1, 2, 3};
        ArrayUInteger uintValues = ArrayUInteger.of(uints);

        long[] longs = {-1L, 2L, 3L, 4L};
        ArrayLong longValues = ArrayLong.of(longs);
        long[] ulongs = {1L, 2L, 3L, 4L};
        ArrayULong ulongValues = ArrayULong.of(ulongs);

        double[] doubles = {1.0, 2.0, 3.0};
        ArrayDouble doubleValues = ArrayDouble.of(doubles);

        float[] floats = {1.1f};
        ArrayFloat floatValues = ArrayFloat.of(floats);

        String[] strings = {"a","b"};
        List<String> stringValues = Arrays.asList(strings);

        long[] emptyLongs = new long[0];
        ArrayLong emptyLongValues = ArrayLong.of(emptyLongs);

        VTable vTable = VTable.of(types, names, Arrays.asList(
                boolValues,
                byteValues,
                ubyteValues,
                shortValues,
                ushortValues,
                intValues,
                uintValues,
                longValues,
                ulongValues,
                doubleValues,
                floatValues,
                stringValues,
                emptyLongValues));

        // This should not fail
        JsonObject jsonObject = VTypeToGson.toJson(vTable);

        VTable deserialized = (VTable) VTypeToGson.toVType(jsonObject);
        assertEquals(vTable.getColumnCount(), deserialized.getColumnCount());
        for(int i = 0; i < deserialized.getColumnCount(); i++){
            assertEquals(vTable.getColumnName(i), deserialized.getColumnName(i));
            assertEquals(vTable.getColumnType(i), deserialized.getColumnType(i));
            assertEquals(vTable.getColumnData(i), deserialized.getColumnData(i));
        }

        // Compare data array lengths and elements
        ArrayBoolean deserializedBoolValues = (ArrayBoolean) deserialized.getColumnData(0);
        boolean[] deserializedBools = new boolean[deserializedBoolValues.size()];
        for(int i = 0; i < deserializedBoolValues.size(); i++){
            deserializedBools[i] = deserializedBoolValues.getBoolean(i);
        }
        assertArrayEquals(bools, deserializedBools);

        ArrayInteger deserializedIntValues = (ArrayInteger) deserialized.getColumnData(5);
        int[] deserializedInts = new int[deserializedIntValues.size()];
        deserializedIntValues.toArray(deserializedInts);
        assertArrayEquals(ints, deserializedInts);

        ArrayUInteger deserializedUIntValues = (ArrayUInteger) deserialized.getColumnData(6);
        int[] deserializedUInts = new int[deserializedUIntValues.size()];
        deserializedUIntValues.toArray(deserializedUInts);
        assertArrayEquals(uints, deserializedUInts);

        ArrayLong deserializedLongValues = (ArrayLong)deserialized.getColumnData(7);
        long[] deserializedLongs = new long[deserializedLongValues.size()];
        deserializedLongValues.toArray(deserializedLongs);
        assertArrayEquals(longs, deserializedLongs);

        ArrayULong deserializedULongValues = (ArrayULong)deserialized.getColumnData(8);
        long[] deserializedULongs = new long[deserializedULongValues.size()];
        deserializedULongValues.toArray(deserializedULongs);
        assertArrayEquals(ulongs, deserializedULongs);

        ArrayShort deserializedShortValues = (ArrayShort)deserialized.getColumnData(3);
        short[] deserializedShorts = new short[deserializedShortValues.size()];
        deserializedShortValues.toArray(deserializedShorts);
        assertArrayEquals(shorts, deserializedShorts);

        ArrayUShort deserializedUShortValues = (ArrayUShort)deserialized.getColumnData(4);
        short[] deserializedUShorts = new short[deserializedUShortValues.size()];
        deserializedUShortValues.toArray(deserializedUShorts);
        assertArrayEquals(ushorts, deserializedUShorts);

        ArrayByte deserializedByteValues = (ArrayByte)deserialized.getColumnData(1);
        byte[] deserializedBytes = new byte[deserializedByteValues.size()];
        deserializedByteValues.toArray(deserializedBytes);
        assertArrayEquals(bytes, deserializedBytes);

        ArrayUByte deserializedUByteValues = (ArrayUByte)deserialized.getColumnData(2);
        byte[] deserializedUBytes = new byte[deserializedUByteValues.size()];
        deserializedUByteValues.toArray(deserializedUBytes);
        assertArrayEquals(ubytes, deserializedUBytes);

        ArrayDouble deserializedDoubleValues = (ArrayDouble) deserialized.getColumnData(9);
        double[] deserializedDoubles = new double[deserializedDoubleValues.size()];
        deserializedDoubleValues.toArray(deserializedDoubles);
        assertArrayEquals(doubles, deserializedDoubles, 0);

        ArrayFloat deserializedFloatValues = (ArrayFloat) deserialized.getColumnData(10);
        float[] deserializedFloats = new float[deserializedFloatValues.size()];
        deserializedFloatValues.toArray(deserializedFloats);
        assertArrayEquals(floats, deserializedFloats, 0);

        List<String> deserializedStringValues = (List)deserialized.getColumnData(11);
        String[] deserializedStrings = new String[deserializedStringValues.size()];
        deserializedStringValues.toArray(deserializedStrings);
        assertArrayEquals(strings, deserializedStrings);

        ArrayLong deserializedEmptyLongValues = (ArrayLong)deserialized.getColumnData(12);
        long[] deserializedEmptyLongs = new long[deserializedEmptyLongValues.size()];
        deserializedEmptyLongValues.toArray(deserializedEmptyLongs);
        assertArrayEquals(emptyLongs, deserializedEmptyLongs);
    }
}
