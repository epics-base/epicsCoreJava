/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.vtype;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.epics.util.array.CollectionNumbers;
import org.epics.util.array.ListNumber;

/**
 * Tag interface to mark all the members of the value classes.
 *
 * @author carcassi
 */
public abstract class VType {

    private static final Collection<Class<?>> TYPES = Arrays.<Class<?>>asList(
            VDouble.class,
            VFloat.class,
            VULong.class,
            VLong.class,
            VUInt.class,
            VInt.class,
            VUShort.class,
            VShort.class,
            VUByte.class,
            VByte.class,
            VEnum.class,
            VBoolean.class,
            VString.class,
            VDoubleArray.class,
            VFloatArray.class,
            VULongArray.class,
            VLongArray.class,
            VUIntArray.class,
            VIntArray.class,
            VUShortArray.class,
            VShortArray.class,
            VUByteArray.class,
            VByteArray.class,
            VStringArray.class,
            VBooleanArray.class,
            VEnumArray.class,
            VImage.class,
            VTable.class);

    /**
     * Returns the type of the object by returning the class object of one
     * of the VXxx interfaces. The getClass() methods returns the
     * concrete implementation type, which is of little use. If no
     * super-interface is found, Object.class is used.
     *
     * @param obj an object implementing a standard type
     * @return the type is implementing
     */
    public static Class<?> typeOf(Object obj) {
        if (obj == null)
            return null;

        for (Class<?> type : TYPES) {
            if (type.isInstance(obj)) {
                return type;
            }
        }

        return Object.class;
    }

    /**
     * As {@link #toVType(java.lang.Object)} but throws an exception
     * if conversion not possible.
     *
     * @param javaObject the value to wrap
     * @return the new VType value
     */
    public static VType toVTypeChecked(Object javaObject) {
        VType value = toVType(javaObject);
        if (value == null) {
            throw new IllegalArgumentException("Value " + value + " cannot be converted to VType.");
        }
        return value;
    }

    /**
     * As {@link #toVType(java.lang.Object, org.epics.vtype.Alarm, org.epics.vtype.Time, org.epics.vtype.Display)} but throws an exception
     * if conversion not possible.
     *
     * @param javaObject the value to wrap
     * @param alarm the alarm
     * @param time the time
     * @param display the display
     * @return the new VType value
     */
    public static VType toVTypeChecked(Object javaObject, Alarm alarm, Time time, Display display) {
        VType value = toVType(javaObject, alarm, time, display);
        if (value == null) {
            throw new IllegalArgumentException("Value " + value + " cannot be converted to VType.");
        }
        return value;
    }

    /**
     * Converts a standard java type to VTypes. Returns null if no conversion
     * is possible. Calls {@link #toVType(java.lang.Object, org.epics.vtype.Alarm, org.epics.vtype.Time, org.epics.vtype.Display) }
     * with no alarm, time now and no display.
     *
     * @param javaObject the value to wrap
     * @return the new VType value
     */
    public static VType toVType(Object javaObject) {
        return toVType(javaObject, Alarm.none(), Time.now(), Display.none());
    }

    /**
     * Converts a standard java type to VTypes. Returns null if no conversion
     * is possible. Calls {@link #toVType(java.lang.Object, org.epics.vtype.Alarm, org.epics.vtype.Time, org.epics.vtype.Display) }
     * with the given alarm, time now and no display.
     *
     * @param javaObject the value to wrap
     * @param alarm the alarm
     * @return the new VType value
     */
    public static VType toVType(Object javaObject, Alarm alarm) {
        return toVType(javaObject, alarm, Time.now(), Display.none());
    }

    /**
     * Converts a standard java type to VTypes. Returns null if no conversion
     * is possible.
     * <p>
     * Types are converted as follow:
     * <ul>
     *   <li>Boolean -&gt; VBoolean</li>
     *   <li>Number -&gt; corresponding VNumber</li>
     *   <li>String -&gt; VString</li>
     *   <li>number array -&gt; corresponding VNumberArray</li>
     *   <li>ListNumber -&gt; corresponding VNumberArray</li>
     *   <li>List -&gt; if all elements are String, VStringArray</li>
     * </ul>
     *
     * @param javaObject the value to wrap
     * @param alarm the alarm
     * @param time the time
     * @param display the display
     * @return the new VType value
     */
    public static VType toVType(Object javaObject, Alarm alarm, Time time, Display display) {
        if (javaObject instanceof Number) {
            return VNumber.of((Number) javaObject, alarm, time, display);
        } else if (javaObject instanceof String) {
            return VString.of((String) javaObject, alarm, time);
        } else if (javaObject instanceof Boolean) {
            return VBoolean.of((Boolean) javaObject, alarm, time);
        } else if (javaObject instanceof byte[]
                || javaObject instanceof short[]
                || javaObject instanceof int[]
                || javaObject instanceof long[]
                || javaObject instanceof float[]
                || javaObject instanceof double[]) {
            return VNumberArray.of(CollectionNumbers.toList(javaObject), alarm, time, display);
        } else if (javaObject instanceof ListNumber) {
            return VNumberArray.of((ListNumber) javaObject, alarm, time, display);
        } else if (javaObject instanceof String[]) {
            return null;//newVStringArray(Arrays.asList((String[]) javaObject), alarm, time);
        } else if (javaObject instanceof List) {
            boolean matches = true;
            List list = (List) javaObject;
            for (Object object : list) {
                if (!(object instanceof String)) {
                    matches = false;
                    break;
                }
            }
            if (matches) {
                @SuppressWarnings("unchecked")
                List<String> newList = (List<String>) list;
                return VStringArray.of(list, alarm, time);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    static void argumentNotNull(String argName, Object value) {
        if (value == null) {
            throw new NullPointerException(argName + " can't be null");
        }
    }
}
