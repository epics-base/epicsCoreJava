/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.vtype.gson;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.epics.util.array.*;
import org.joda.time.Instant;
import org.epics.util.stats.Range;
import org.epics.vtype.*;

import java.text.DecimalFormat;
import java.util.*;

import static org.epics.vtype.gson.GsonArrays.*;

/**
 * Gson adapted VTypeJsonMapper
 *
 * @author <a href="mailto:changj@frib.msu.edu">Genie Jhang</a>
 *
 * Original author:
 * @author carcassi
 */
class VTypeGsonMapper extends JsonElement {

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

    public VTypeGsonMapper(JsonObject json) {
        this.json = json;
    }

    public String getTypeName() {
        JsonObject type = json.getAsJsonObject("type");
        if (type == null) {
            return null;
        }
        return type.get("name").getAsString();
    }

    public Alarm getAlarm() {
        JsonObject alarm = json.getAsJsonObject("alarm");
        if (alarm == null) {
            return null;
        }
        return Alarm.of(AlarmSeverity.valueOf(alarm.get("severity").getAsString()), AlarmStatus.valueOf(alarm.get("status").getAsString()), alarm.get("name").getAsString());
    }

    public Time getTime() {
        VTypeGsonMapper time = getJsonObject("time");
        if (time == null) {
            return null;
        }
        return Time.of(Instant.ofEpochMilli(time.getInt("unixSec")*1000L + time.getInt("nanoSec")/1000000L), time.getInteger("userTag"), true);
    }

    public Display getDisplay() {
        VTypeGsonMapper display = getJsonObject("display");
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
            values[i] = array.get(i).getAsBoolean();
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
            String type = array.get(i).getAsString();
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
                result.add(toListString(array.get(i).getAsJsonArray()));
            } else if (double.class.equals(type)) {
                result.add(toListDouble(array.get(i).getAsJsonArray()));
            } else if (float.class.equals(type)) {
                result.add(toListFloat(array.get(i).getAsJsonArray()));
            } else if (long.class.equals(type)) {
                result.add(toListLong(array.get(i).getAsJsonArray()));
            } else if (int.class.equals(type)) {
                result.add(toListInt(array.get(i).getAsJsonArray()));
            } else if (short.class.equals(type)) {
                result.add(toListShort(array.get(i).getAsJsonArray()));
            } else if (byte.class.equals(type)) {
                result.add(toListByte(array.get(i).getAsJsonArray()));
            } else if (Instant.class.equals(type)) {
                result.add(toListTimestamp(array.get(i).getAsJsonArray()));
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
        return getJsonNumber(string).getAsDouble();
    }

    public JsonArray getJsonArray(String string) {
        return json.get(string).getAsJsonArray();
    }

    public VTypeGsonMapper getJsonObject(String string) {
        return new VTypeGsonMapper(json.get(string).getAsJsonObject());
    }

    public JsonElement getJsonNumber(String string) {
        return json.get(string);
    }

    public JsonElement getJsonString(String string) {
        return json.get(string);
    }

    public String getString(String string) {
        return json.get(string).getAsString();
    }

    public String getString(String string, String string1) {
        return json.get(string).isJsonNull() ? string1 : json.get(string).getAsString();
    }

    public int getInt(String string) {
        return json.get(string).getAsInt();
    }

    public int getInt(String string, int i) {
        return json.get(string).isJsonNull() ? i : json.get(string).getAsInt();
    }

    public boolean getBoolean(String string) {
        return json.get(string).getAsBoolean();
    }

    public boolean getBoolean(String string, boolean bln) {
        return json.get(string).isJsonNull() ? bln : json.get(string).getAsBoolean();
    }

    public boolean isNull(String string) {
        return json.get(string) == null || json.get(string).isJsonNull();
    }

    public int size() {
        return json.size();
    }

    public boolean isEmpty() {
        return json.isJsonNull();
    }

    public boolean containsKey(Object key) {
        return json.get(key.toString()) != null && !json.get(key.toString()).isJsonNull();
    }

    public boolean containsValue(Object value) {
        return json.get(value.toString()) != null && !json.get(value.toString()).isJsonNull();
    }

    public JsonElement get(Object key) {
        return json.get((String)key);
    }

    public JsonElement put(String key, JsonElement value) {
        json.add(key, value);
        return json.get(key);
    }

    public JsonElement remove(Object key) {
        return json.remove((String)key);
    }

    public void putAll(Map<? extends String, ? extends JsonElement> m) {
        for ( Map.Entry<? extends String, ? extends JsonElement> e : m.entrySet()) {
            json.add(e.getKey(), e.getValue());
        }
    }

    public void clear() {
    }

    public Set<String> keySet() {
        return json.keySet();
    }

    public Collection<JsonElement> values() {
        return Collections.emptyList();
    }

    public Set<Map.Entry<String, JsonElement>> entrySet() {
        return json.entrySet();
    }

    @Override
    public JsonElement deepCopy() {
        return null;
    }
}
