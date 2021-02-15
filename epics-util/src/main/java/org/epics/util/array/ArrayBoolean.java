/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.array;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Wraps a {@code boolean[]} into a {@link ListBoolean}.
 *
 * @author Gabriele Carcassi
 */
public final class ArrayBoolean extends ListBoolean implements Serializable {

    private static final long serialVersionUID = 7493025761455302915L;

    private final boolean[] array;
    private final boolean readOnly;

    /**
     * A new read-only {@code ArrayBoolean} that wraps around the given array.
     *
     * @param array an array
     */
    public ArrayBoolean(boolean... array) {
        this(array, true);
    }

    /**
     * A new {@code ArrayBoolean} that wraps around the given array.
     *
     * @param array an array
     * @param readOnly if false the wrapper allows writes to the array
     */
    public ArrayBoolean(boolean[] array, boolean readOnly) {
        this.array = array;
        this.readOnly = readOnly;
    }

    @Override
    public final int size() {
        return array.length;
    }

    @Override
    public boolean getBoolean(int index) {
        return array[index];
    }

    @Override
    public void setBoolean(int index, boolean value) {
        checkBounds(index, readOnly, false, array.length);
        array[index] = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ArrayBoolean) {
            return Arrays.equals(array, ((ArrayBoolean) obj).array);
        }

        return super.equals(obj);
    }

    boolean[] wrappedArray() {
        return array;
    }

    /**
     * Returns an unmodifiable {@link ArrayBoolean} wrapper for the given {@code boolean} array.
     *
     * @param values a primitive array.
     * @return an immutable wrapper.
     */
    public static ArrayBoolean of(boolean... values) {
        return new ArrayBoolean(values);
    }

}
