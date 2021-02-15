/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.vtype.json;

import org.epics.util.array.*;
import org.joda.time.Instant;
import org.epics.util.stats.Range;
import org.epics.vtype.*;

import javax.json.*;
import java.text.DecimalFormat;
import java.util.*;

import static org.epics.vtype.json.JsonArrays.*;

/**
 * @author carcassi
 */
class VTypeJsonMapper implements JsonObject {

    private final JsonObject json;

    public static final String NAN = Double.toString(Double.NaN);
    /**
     * Quoted version of {@link Double#NaN}. Needed for comparison when de-serializing,
     * as serialization of {@link Double#NaN} creates a quotes string.
     */
    public static final String NAN_QUOTED = "\"" + NAN + "\"";
    public static final String POS_INF = Double.toString(Double.POSITIVE_INFINITY);
    /**
     * Quoted version of {@link Double#POSITIVE_INFINITY}. Needed for comparison when de-serializing,
     * as serialization of {@link Double#POSITIVE_INFINITY} creates a quotes string.
     */
    public static final String POS_INF_QUOTED = "\"" + POS_INF + "\"";
    public static final String NEG_INF = Double.toString(Double.NEGATIVE_INFINITY);
    /**
     * Quoted version of {@link Double#NEGATIVE_INFINITY}. Needed for comparison when de-serializing,
     * as serialization of {@link Double#NEGATIVE_INFINITY} creates a quotes string.
     */
    public static final String NEG_INF_QUOTED = "\"" + NEG_INF + "\"";

    public VTypeJsonMapper(JsonObject json) {
        this.json = json;
    }

    public String getTypeName() {
        JsonObject type = json.getJsonObject("type");
        if (type == null) {
            return null;
        }
        return type.getString("name");
    }

    public Alarm getAlarm() {
        JsonObject alarm = json.getJsonObject("alarm");
        if (alarm == null) {
            return null;
        }
        return Alarm.of(AlarmSeverity.valueOf(alarm.getString("severity")), AlarmStatus.valueOf(alarm.getString("status")), alarm.getString("name"));
    }

    public Time getTime() {
        VTypeJsonMapper time = getJsonObject("time");
        if (time == null) {
            return null;
        }
        return Time.of(Instant.ofEpochSecond(time.getInt("unixSec")).plus(time.getInt("nanoSec")/1000000L), time.getInteger("userTag"), true);
    }

    public Display getDisplay() {
        VTypeJsonMapper display = getJsonObject("display");
        if (display == null) {
            return null;
        }
        return Display.of(Range.of(display.getNotNullDouble("lowDisplay"), display.getNotNullDouble("highDisplay")),
                Range.of(display.getNotNullDouble("lowAlarm"), display.getNotNullDouble("highAlarm")),
                Range.of(display.getNotNullDouble("lowWarning"), display.getNotNullDouble("highWarning")),
                Range.of(display.getNotNullDouble("lowControl"), display.getNotNullDouble("highControl")),
                display.getString("units"), new DecimalFormat());
    }

    public ListDouble getListDouble(String string) {
        JsonArray array = getJsonArray(string);
        return toListDouble(array);
    }


    public ListFloat getListFloat(String string) {
        JsonArray array = getJsonArray(string);
        return toListFloat(array);
    }

    public ListULong getListULong(String string) {
        JsonArray array = getJsonArray(string);
        return toListULong(array);
    }

    public ListLong getListLong(String string) {
        JsonArray array = getJsonArray(string);
        return toListLong(array);
    }


    public ListUInteger getListUInteger(String string) {
        JsonArray array = getJsonArray(string);
        return toListUInteger(array);
    }

    public ListInteger getListInt(String string) {
        JsonArray array = getJsonArray(string);
        return toListInt(array);
    }


    public ListUShort getListUShort(String string) {
        JsonArray array = getJsonArray(string);
        return toListUShort(array);
    }

    public ListShort getListShort(String string) {
        JsonArray array = getJsonArray(string);
        return toListShort(array);
    }


    public ListUByte getListUByte(String string) {
        JsonArray array = getJsonArray(string);
        return toListUByte(array);
    }

    public ListByte getListByte(String string) {
        JsonArray array = getJsonArray(string);
        return toListByte(array);
    }


    public ListBoolean getListBoolean(String string) {
        JsonArray array = getJsonArray(string);
        boolean[] values = new boolean[array.size()];
        for (int i = 0; i < values.length; i++) {
            values[i] = array.getBoolean(i);
        }
        return new ArrayBoolean(values);
    }

    public List<String> getListString(String string) {
        JsonArray array = getJsonArray(string);
        return toListString(array);
    }


    public List<Class<?>> getColumnTypes(String string) {
        JsonArray array = getJsonArray(string);
        List<Class<?>> types = new ArrayList<Class<?>>();
        for (int i = 0; i < array.size(); i++) {
            String type = array.getString(i);
            if ("String".equals(type)) {
                types.add(String.class);
            } else if ("double".equals(type)) {
                types.add(double.class);
            } else if ("float".equals(type)) {
                types.add(float.class);
            } else if ("long".equals(type)) {
                types.add(long.class);
            } else if ("int".equals(type)) {
                types.add(int.class);
            } else if ("short".equals(type)) {
                types.add(short.class);
            } else if ("byte".equals(type)) {
                types.add(byte.class);
            } else if ("Timestamp".equals(type)) {
                types.add(Instant.class);
            } else {
                throw new IllegalArgumentException("Column type " + type + " not supported");
            }
        }
        return types;
    }

    public List<Object> getColumnValues(String string, List<Class<?>> types) {
        JsonArray array = getJsonArray(string);
        List<Object> result = new ArrayList<Object>();
        for (int i = 0; i < types.size(); i++) {
            Class<?> type = types.get(i);
            if (String.class.equals(type)) {
                result.add(toListString(array.getJsonArray(i)));
            } else if (double.class.equals(type)) {
                result.add(toListDouble(array.getJsonArray(i)));
            } else if (float.class.equals(type)) {
                result.add(toListFloat(array.getJsonArray(i)));
            } else if (long.class.equals(type)) {
                result.add(toListLong(array.getJsonArray(i)));
            } else if (int.class.equals(type)) {
                result.add(toListInt(array.getJsonArray(i)));
            } else if (short.class.equals(type)) {
                result.add(toListShort(array.getJsonArray(i)));
            } else if (byte.class.equals(type)) {
                result.add(toListByte(array.getJsonArray(i)));
            } else if (Instant.class.equals(type)) {
                result.add(toListTimestamp(array.getJsonArray(i)));
            } else {
                throw new IllegalArgumentException("Column type " + type + " not supported");
            }
        }
        return result;
    }

    public Integer getInteger(String string) {
        if (isNull(string)) {
            return null;
        }
        return getInt(string);
    }

    public Double getNotNullDouble(String string) {
        if (isNull(string)) {
            return Double.NaN;
        }
        return getJsonNumber(string).doubleValue();
    }

    public JsonArray getJsonArray(String string) {
        return json.getJsonArray(string);
    }

    public VTypeJsonMapper getJsonObject(String string) {
        return new VTypeJsonMapper(json.getJsonObject(string));
    }

    public JsonNumber getJsonNumber(String string) {
        return json.getJsonNumber(string);
    }

    public JsonString getJsonString(String string) {
        return json.getJsonString(string);
    }

    public String getString(String string) {
        return json.getString(string);
    }

    public String getString(String string, String string1) {
        return json.getString(string, string1);
    }

    public int getInt(String string) {
        return json.getInt(string);
    }

    public int getInt(String string, int i) {
        return json.getInt(string, i);
    }

    public boolean getBoolean(String string) {
        return json.getBoolean(string);
    }

    public boolean getBoolean(String string, boolean bln) {
        return json.getBoolean(string, bln);
    }

    public boolean isNull(String string) {
        return !json.containsKey(string) || json.isNull(string);
    }

    public ValueType getValueType() {
        return json.getValueType();
    }

    public int size() {
        return json.size();
    }

    public boolean isEmpty() {
        return json.isEmpty();
    }

    public boolean containsKey(Object key) {
        return json.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return json.containsValue(value);
    }

    public JsonValue get(Object key) {
        return json.get(key);
    }

    public JsonValue put(String key, JsonValue value) {
        return json.put(key, value);
    }

    public JsonValue remove(Object key) {
        return json.remove(key);
    }

    public void putAll(Map<? extends String, ? extends JsonValue> m) {
        json.putAll(m);
    }

    public void clear() {
        json.clear();
    }

    public Set<String> keySet() {
        return json.keySet();
    }

    public Collection<JsonValue> values() {
        return json.values();
    }

    public Set<Entry<String, JsonValue>> entrySet() {
        return json.entrySet();
    }

}
