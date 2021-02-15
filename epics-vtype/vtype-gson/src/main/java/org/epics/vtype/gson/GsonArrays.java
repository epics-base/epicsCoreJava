/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.vtype.gson;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import org.epics.util.array.*;
import org.epics.util.number.UnsignedConversions;
import org.joda.time.Instant;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility classes to convert JSON arrays of Gson objects to and from Lists and ListNumbers. (Modified for Gson)
 *
 * @author <a href="mailto:changj@frib.msu.edu">Genie Jhang</a>
 * <p>
 * Original description:
 * Utility classes to convert JSON arrays to and from Lists and ListNumbers.
 * @author carcassi
 */
public class GsonArrays {

    /**
     * Checks whether the array contains only numbers.
     *
     * @param array a JSON array
     * @return true if all elements are JSON numbers
     */
    public static boolean isNumericArray(JsonArray array) {
        for (JsonElement value : array) {
            if (!value.getAsJsonPrimitive().isNumber()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks whether the array contains only strings.
     *
     * @param array a JSON array
     * @return true if all elements are JSON strings
     */
    public static boolean isStringArray(JsonArray array) {
        for (JsonElement value : array) {
            if (!value.getAsJsonPrimitive().isString()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Converts the given numeric JSON array to a ListDouble.
     *
     * @param array an array of numbers
     * @return a new ListDouble
     */
    public static ListDouble toListDouble(JsonArray array) {
        double[] values = new double[array.size()];
        for (int i = 0; i < values.length; i++) {
            values[i] = VTypeToGson.getDoubleFromJsonString(array.get(i).toString());
        }
        return ArrayDouble.of(values);
    }

    /**
     * Converts the given numeric JSON array to a ListFloat.
     *
     * @param array an array of numbers
     * @return a new ListFloat
     */
    public static ListFloat toListFloat(JsonArray array) {
        float[] values = new float[array.size()];
        for (int i = 0; i < values.length; i++) {
            if (array.get(i) == null) {
                values[i] = Float.NaN;
            } else {
                values[i] = (float) array.get(i).getAsDouble();
            }
        }
        return ArrayFloat.of(values);
    }

    /**
     * Converts the given numeric JSON array to a {@code ListULong}.
     *
     * @param array an array of numbers
     * @return a new {@code ListULong}
     */
    public static ListULong toListULong(JsonArray array) {
        long[] values = new long[array.size()];
        for (int i = 0; i < values.length; i++) {
            values[i] = (long) array.get(i).getAsLong();
        }
        return ArrayULong.of(values);
    }

    /**
     * Converts the given numeric JSON array to a ListLong.
     *
     * @param array an array of numbers
     * @return a new ListLong
     */
    public static ListLong toListLong(JsonArray array) {
        long[] values = new long[array.size()];
        for (int i = 0; i < values.length; i++) {
            values[i] = (long) array.get(i).getAsLong();
        }
        return ArrayLong.of(values);
    }

    /**
     * Converts the given numeric JSON array to a {@code ListUInteger}.
     *
     * @param array an array of numbers
     * @return a new {@code ListUInteger}
     */
    public static ListUInteger toListUInteger(JsonArray array) {
        int[] values = new int[array.size()];
        for (int i = 0; i < values.length; i++) {
            values[i] = (int) array.get(i).getAsInt();
        }
        return ArrayUInteger.of(values);
    }

    /**
     * Converts the given numeric JSON array to a ListInteger.
     *
     * @param array an array of numbers
     * @return a new ListInteger
     */
    public static ListInteger toListInt(JsonArray array) {
        int[] values = new int[array.size()];
        for (int i = 0; i < values.length; i++) {
            values[i] = (int) array.get(i).getAsInt();
        }
        return ArrayInteger.of(values);
    }

    /**
     * Converts the given numeric JSON array to a {@code ListUShort}.
     *
     * @param array an array of numbers
     * @return a new {@code ListUShort}
     */
    public static ListUShort toListUShort(JsonArray array) {
        short[] values = new short[array.size()];
        for (int i = 0; i < values.length; i++) {
            values[i] = (short) array.get(i).getAsInt();
        }
        return ArrayUShort.of(values);
    }

    /**
     * Converts the given numeric JSON array to a ListShort.
     *
     * @param array an array of numbers
     * @return a new ListShort
     */
    public static ListShort toListShort(JsonArray array) {
        short[] values = new short[array.size()];
        for (int i = 0; i < values.length; i++) {
            values[i] = (short) array.get(i).getAsInt();
        }
        return ArrayShort.of(values);
    }

    /**
     * Converts the given numeric JSON array to a {@code ListUByte}.
     *
     * @param array an array of numbers
     * @return a new {@code ListUByte}
     */
    public static ListUByte toListUByte(JsonArray array) {
        byte[] values = new byte[array.size()];
        for (int i = 0; i < values.length; i++) {
            values[i] = (byte) array.get(i).getAsInt();
        }
        return ArrayUByte.of(values);
    }

    /**
     * Converts the given numeric JSON array to a ListByte.
     *
     * @param array an array of numbers
     * @return a new ListByte
     */
    public static ListByte toListByte(JsonArray array) {
        byte[] values = new byte[array.size()];
        for (int i = 0; i < values.length; i++) {
            values[i] = (byte) array.get(i).getAsInt();
        }
        return ArrayByte.of(values);
    }

    /**
     * Converts the given string JSON array to a List of Strings.
     *
     * @param array an array of strings
     * @return a new List of Strings
     */
    public static List<String> toListString(JsonArray array) {
        List<String> strings = new ArrayList<String>();
        for (int i = 0; i < array.size(); i++) {
            strings.add(array.get(i).getAsString());
        }
        return strings;
    }


    /**
     * Converts the given JSON array to a List of Instants.
     *
     * @param array an array
     * @return a new List of Timestamps
     */
    public static List<Instant> toListTimestamp(JsonArray array) {
        List<Instant> timestamps = new ArrayList<Instant>();
        for (int i = 0; i < array.size(); i++) {
            if (array.get(i).isJsonNull()) {
                timestamps.add(null);
            } else {
                timestamps.add(Instant.ofEpochMilli(array.get(i).getAsLong()));
            }
        }
        return timestamps;
    }

    /**
     * Converts the given List of String to a string JSON array.
     *
     * @param list a List of Strings
     * @return an array of strings
     */
    public static JsonArray fromListString(List<String> list) {
        JsonArray b = new JsonArray();
        for (String element : list) {
            // TODO: Not clear how to handle nulls. Converting them to empty strings.
            if (element == null) {
                element = "";
            }
            b.add(element);
        }
        return b;
    }

    /**
     * Converts the given List of Timestamp to a JSON array.
     *
     * @param list a List of Timestamps
     * @return an array
     */
    public static JsonArray fromListTimestamp(List<Instant> list) {
        JsonArray b = new JsonArray();
        for (Instant element : list) {
            if (element == null) {
                b.add(JsonNull.INSTANCE);
            } else {
                b.add(element.getMillis());
            }
        }
        return b;
    }

    /**
     * Converts the given ListNumber to a number JSON array.
     *
     * @param list a list of numbers
     * @return an array of numbers
     */
    public static JsonArray fromListNumber(ListNumber list) {
        JsonArray b = new JsonArray();
        if (list instanceof ListInteger || list instanceof ListUShort || list instanceof ListShort || list instanceof ListUByte || list instanceof ListByte) {
            for (int i = 0; i < list.size(); i++) {
                b.add(list.getInt(i));
            }
        } else if (list instanceof ListLong || list instanceof ListUInteger) {
            for (int i = 0; i < list.size(); i++) {
                b.add(list.getLong(i));
            }
        } else if (list instanceof ListULong) {
            for (int i = 0; i < list.size(); i++) {
                b.add(UnsignedConversions.toBigInteger(list.getLong(i)));
            }
        } else {
            for (int i = 0; i < list.size(); i++) {
                double value = list.getDouble(i);
                if (java.lang.Double.isNaN(value)) {
                    b.add(VTypeGsonMapper.NAN);
                } else if (java.lang.Double.valueOf(value).equals(java.lang.Double.POSITIVE_INFINITY)) {
                    b.add(VTypeGsonMapper.POS_INF);
                } else if (java.lang.Double.valueOf(value).equals(java.lang.Double.NEGATIVE_INFINITY)) {
                    b.add(VTypeGsonMapper.NEG_INF);
                } else if (!Double.isInfinite(value)) {
                    b.add(value);
                }
            }
        }
        return b;
    }

}
