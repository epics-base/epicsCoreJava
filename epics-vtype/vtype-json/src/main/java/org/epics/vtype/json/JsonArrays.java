/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.vtype.json;

import org.epics.util.array.*;
import org.epics.util.number.UnsignedConversions;
import org.joda.time.Instant;

import javax.json.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility classes to convert JSON arrays to and from Lists and ListNumbers.
 *
 * @author carcassi
 */
public class JsonArrays {

    /**
     * Checks whether the array contains only numbers.
     *
     * @param array a JSON array
     * @return true if all elements are JSON numbers
     */
    public static boolean isNumericArray(JsonArray array) {
        for (JsonValue value : array) {
            if (!(value instanceof JsonNumber)) {
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
        for (JsonValue value : array) {
            if (!(value instanceof JsonString)) {
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
            values[i] = VTypeToJsonV1.getDoubleFromJsonString(array.get(i).toString());
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
            if (array.isNull(i)) {
                values[i] = Float.NaN;
            } else {
                values[i] = (float) array.getJsonNumber(i).doubleValue();
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
            values[i] = (long) array.getJsonNumber(i).longValue();
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
            values[i] = (long) array.getJsonNumber(i).longValue();
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
            values[i] = (int) array.getJsonNumber(i).intValue();
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
            values[i] = (int) array.getJsonNumber(i).intValue();
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
            values[i] = (short) array.getJsonNumber(i).intValue();
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
            values[i] = (short) array.getJsonNumber(i).intValue();
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
            values[i] = (byte) array.getJsonNumber(i).intValue();
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
            values[i] = (byte) array.getJsonNumber(i).intValue();
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
            strings.add(array.getString(i));
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
            if (array.isNull(i)) {
                timestamps.add(null);
            } else {
                timestamps.add(Instant.ofEpochMilli(array.getJsonNumber(i).longValue()));
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
    public static JsonArrayBuilder fromListString(List<String> list) {
        JsonArrayBuilder b = JsonVTypeBuilder.factory.createArrayBuilder();
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
    public static JsonArrayBuilder fromListTimestamp(List<Instant> list) {
        JsonArrayBuilder b = JsonVTypeBuilder.factory.createArrayBuilder();
        for (Instant element : list) {
            if (element == null) {
                b.addNull();
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
    public static JsonArrayBuilder fromListNumber(ListNumber list) {
        JsonArrayBuilder b = JsonVTypeBuilder.factory.createArrayBuilder();
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
                    b.add(VTypeJsonMapper.NAN);
                } else if (java.lang.Double.valueOf(value).equals(java.lang.Double.POSITIVE_INFINITY)) {
                    b.add(VTypeJsonMapper.POS_INF);
                } else if (java.lang.Double.valueOf(value).equals(java.lang.Double.NEGATIVE_INFINITY)) {
                    b.add(VTypeJsonMapper.NEG_INF);
                } else if (!Double.isInfinite(value)) {
                    b.add(value);
                }
            }
        }
        return b;
    }

}
