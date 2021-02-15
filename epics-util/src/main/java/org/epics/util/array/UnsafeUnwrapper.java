/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.array;

/**
 * A set of utilities that allows, when really needed, to get the primitive
 * array wrapped by the {@code ArrayXxx} classes.
 * <p>
 * These methods should only be used when the data stored in the array classes
 * needs to be passed to other libraries that operate directly on primitive
 * array. In these cases, the only choice is to break encapsulation and
 * expose the internal state. USE WITH CAUTION.
 * <p>
 * There are three type of methods. {@code wrappedXxxArray} returns the array
 * the {@link CollectionNumber} is wrapping or null if no array is being wrapped.
 * {@code readSafeXxxArray} returns an array, possibly a copy, that is always
 * safe to read. {@code writeSafeXxxArray} returns an array, possibly a copy, that is always
 * safe to write to.
 */
public class UnsafeUnwrapper {

    /**
     * A primitive array together with the starting index and the number
     * of elements to be read from the starting index.
     *
     * @param <T> the array type
     */
    public static final class Array<T> {
        public final T array;
        public final int startIndex;
        public final int size;

        Array(T array, int startIndex, int size) {
            this.array = array;
            this.startIndex = startIndex;
            this.size = size;
        }

    }

    /**
     * If available, return the array wrapped by the collection - USE WITH
     * CAUTION AS IT EXPOSES THE INTERNAL STATE OF THE COLLECTION. This
     * is provided in case an external routine for computation
     * requires you to use array, and you want to avoid the copy
     * for performance reason.
     *
     * @param coll the collection
     * @return the array or null
     */
    public static Array<?> wrappedArray(CollectionNumber coll) {
        Array<?> data = wrappedFloatArray(coll);
        if (data != null) {
            return data;
        }
        data = wrappedDoubleArray(coll);
        if (data != null) {
            return data;
        }
        data = wrappedByteArray(coll);
        if (data != null) {
            return data;
        }
        data = wrappedShortArray(coll);
        if (data != null) {
            return data;
        }
        data = wrappedIntArray(coll);
        if (data != null) {
            return data;
        }
        data = wrappedLongArray(coll);
        if (data != null) {
            return data;
        }
        return null;
    }

    /**
     * If available, return the array wrapped by the collection - USE WITH
     * CAUTION AS IT EXPOSES THE INTERNAL STATE OF THE COLLECTION. This
     * is provided in case an external routine for computation
     * requires you to use array, and you want to avoid the copy
     * for performance reason.
     *
     * @param coll the collection
     * @return the array or null
     */
    public static Array<float[]> wrappedFloatArray(CollectionNumber coll) {
        if (coll instanceof ArrayFloat) {
            ArrayFloat wrapper = (ArrayFloat) coll;
            return new Array<float[]>(wrapper.wrappedArray(), wrapper.startIndex(), wrapper.size());
        }

        return null;
    }

    /**
     * If available, return the array wrapped by the collection - USE WITH
     * CAUTION AS IT EXPOSES THE INTERNAL STATE OF THE COLLECTION. This
     * is provided in case an external routine for computation
     * requires you to use array, and you want to avoid the copy
     * for performance reason.
     *
     * @param coll the collection
     * @return the array or null
     */
    public static Array<double[]> wrappedDoubleArray(CollectionNumber coll) {
        if (coll instanceof ArrayDouble) {
            ArrayDouble wrapper = (ArrayDouble) coll;
            return new Array<double[]>(wrapper.wrappedArray(), wrapper.startIndex(), wrapper.size());
        }

        return null;
    }

    /**
     * If available, return the array wrapped by the collection - USE WITH
     * CAUTION AS IT EXPOSES THE INTERNAL STATE OF THE COLLECTION. This
     * is provided in case an external routine for computation
     * requires you to use array, and you want to avoid the copy
     * for performance reason.
     *
     * @param coll the collection
     * @return the array or null
     */
    public static Array<byte[]> wrappedByteArray(CollectionNumber coll) {
        if (coll instanceof ArrayByte) {
            ArrayByte wrapper = (ArrayByte) coll;
            return new Array<byte[]>(wrapper.wrappedArray(), wrapper.startIndex(), wrapper.size());
        }

        return null;
    }

    /**
     * If available, return the array wrapped by the collection - USE WITH
     * CAUTION AS IT EXPOSES THE INTERNAL STATE OF THE COLLECTION. This
     * is provided in case an external routine for computation
     * requires you to use array, and you want to avoid the copy
     * for performance reason.
     *
     * @param coll the collection
     * @return the array or null
     */
    public static Array<short[]> wrappedShortArray(CollectionNumber coll) {
        if (coll instanceof ArrayShort) {
            ArrayShort wrapper = (ArrayShort) coll;
            return new Array<short[]>(wrapper.wrappedArray(), wrapper.startIndex(), wrapper.size());
        }

        return null;
    }

    /**
     * If available, return the array wrapped by the collection - USE WITH
     * CAUTION AS IT EXPOSES THE INTERNAL STATE OF THE COLLECTION. This
     * is provided in case an external routine for computation
     * requires you to use array, and you want to avoid the copy
     * for performance reason.
     *
     * @param coll the collection
     * @return the array or null
     */
    public static Array<int[]> wrappedIntArray(CollectionNumber coll) {
        if (coll instanceof ArrayInteger) {
            ArrayInteger wrapper = (ArrayInteger) coll;
            return new Array<int[]>(wrapper.wrappedArray(), wrapper.startIndex(), wrapper.size());
        }

        return null;
    }

    /**
     * If available, return the array wrapped by the collection - USE WITH
     * CAUTION AS IT EXPOSES THE INTERNAL STATE OF THE COLLECTION. This
     * is provided in case an external routine for computation
     * requires you to use array, and you want to avoid the copy
     * for performance reason.
     *
     * @param coll the collection
     * @return the array or null
     */
    public static Array<long[]> wrappedLongArray(CollectionNumber coll) {
        if (coll instanceof ArrayLong) {
            ArrayLong wrapper = (ArrayLong) coll;
            return new Array<long[]>(wrapper.wrappedArray(), wrapper.startIndex(), wrapper.size());
        }

        return null;
    }

    /**
     * Returns a double array that contains the elements of the collection
     * meant for read-only - USE WITH CAUTION AS IT MAY EXPOSE THE INTERNAL STATE
     * OF THE COLLECTION
     * Returns either the wrapped array (if exists and matches the type)
     * or a copy. It may return the array protected by an unmodifiable view.
     *
     * @param coll the collection
     * @return the array
     */
    public static Array<double[]> readSafeDoubleArray(CollectionNumber coll) {
        Array<double[]> array = wrappedDoubleArray(coll);
        if (array != null) {
            return array;
        }
        return new Array<double[]>(coll.toArray(new double[coll.size()]), 0, coll.size());
    }

    /**
     * Returns a float array that contains the elements of the collection
     * meant for read-only - USE WITH CAUTION AS IT MAY EXPOSE THE INTERNAL STATE
     * OF THE COLLECTION
     * Returns either the wrapped array (if exists and matches the type)
     * or a copy. It may return the array protected by an unmodifiable view.
     *
     * @param coll the collection
     * @return the array
     */
    public static Array<float[]> readSafeFloatArray(CollectionNumber coll) {
        Array<float[]> array = wrappedFloatArray(coll);
        if (array != null) {
            return array;
        }
        return new Array<float[]>(coll.toArray(new float[coll.size()]), 0, coll.size());
    }

    /**
     * Returns a long array that contains the elements of the collection
     * meant for read-only - USE WITH CAUTION AS IT MAY EXPOSE THE INTERNAL STATE
     * OF THE COLLECTION
     * Returns either the wrapped array (if exists and matches the type)
     * or a copy. It may return the array protected by an unmodifiable view.
     *
     * @param coll the collection
     * @return the array
     */
    public static Array<long[]> readSafeLongArray(CollectionNumber coll) {
        Array<long[]> array = wrappedLongArray(coll);
        if (array != null) {
            return array;
        }
        return new Array<long[]>(coll.toArray(new long[coll.size()]), 0, coll.size());
    }

    /**
     * Returns a int array that contains the elements of the collection
     * meant for read-only - USE WITH CAUTION AS IT MAY EXPOSE THE INTERNAL STATE
     * OF THE COLLECTION
     * Returns either the wrapped array (if exists and matches the type)
     * or a copy. It may return the array protected by an unmodifiable view.
     *
     * @param coll the collection
     * @return the array
     */
    public static Array<int[]> readSafeIntArray(CollectionNumber coll) {
        Array<int[]> array = wrappedIntArray(coll);
        if (array != null) {
            return array;
        }
        return new Array<int[]>(coll.toArray(new int[coll.size()]), 0, coll.size());
    }

    /**
     * Returns a short array that contains the elements of the collection
     * meant for read-only - USE WITH CAUTION AS IT MAY EXPOSE THE INTERNAL STATE
     * OF THE COLLECTION
     * Returns either the wrapped array (if exists and matches the type)
     * or a copy. It may return the array protected by an unmodifiable view.
     *
     * @param coll the collection
     * @return the array
     */
    public static Array<short[]> readSafeShortArray(CollectionNumber coll) {
        Array<short[]> array = wrappedShortArray(coll);
        if (array != null) {
            return array;
        }
        return new Array<short[]>(coll.toArray(new short[coll.size()]), 0, coll.size());
    }

    /**
     * Returns a byte array that contains the elements of the collection
     * meant for read-only - USE WITH CAUTION AS IT MAY EXPOSE THE INTERNAL STATE
     * OF THE COLLECTION
     * Returns either the wrapped array (if exists and matches the type)
     * or a copy. It may return the array protected by an unmodifiable view.
     *
     * @param coll the collection
     * @return the array
     */
    public static Array<byte[]> readSafeByteArray(CollectionNumber coll) {
        Array<byte[]> array = wrappedByteArray(coll);
        if (array != null) {
            return array;
        }
        return new Array<byte[]>(coll.toArray(new byte[coll.size()]), 0, coll.size());
    }

    /**
     * Returns a double array that contains the elements of the collection
     * meant - USE WITH CAUTION AS IT MAY EXPOSE THE INTERNAL STATE
     * OF THE COLLECTION
     * Returns either the wrapped array (if exists and matches the type and the
     * collection is modifiable) or a copy.
     *
     * @param coll the collection
     * @return the array
     */
    public static Array<double[]> writeSafeDoubleArray(CollectionNumber coll) {
        Array<double[]> array = wrappedDoubleArray(coll);
        if (array != null && !((ArrayDouble) coll).isReadOnly()) {
            return array;
        }
        return new Array<double[]>(coll.toArray(new double[coll.size()]), 0, coll.size());
    }

    /**
     * Returns a float array that contains the elements of the collection
     * meant - USE WITH CAUTION AS IT MAY EXPOSE THE INTERNAL STATE
     * OF THE COLLECTION
     * Returns either the wrapped array (if exists and matches the type and the
     * collection is modifiable) or a copy.
     *
     * @param coll the collection
     * @return the array
     */
    public static Array<float[]> writeSafeFloatArray(CollectionNumber coll) {
        Array<float[]> array = wrappedFloatArray(coll);
        if (array != null && !((ArrayFloat) coll).isReadOnly()) {
            return array;
        }
        return new Array<float[]>(coll.toArray(new float[coll.size()]), 0, coll.size());
    }

    /**
     * Returns a long array that contains the elements of the collection
     * meant - USE WITH CAUTION AS IT MAY EXPOSE THE INTERNAL STATE
     * OF THE COLLECTION
     * Returns either the wrapped array (if exists and matches the type and the
     * collection is modifiable) or a copy.
     *
     * @param coll the collection
     * @return the array
     */
    public static Array<long[]> writeSafeLongArray(CollectionNumber coll) {
        Array<long[]> array = wrappedLongArray(coll);
        if (array != null && !((ArrayLong) coll).isReadOnly()) {
            return array;
        }
        return new Array<long[]>(coll.toArray(new long[coll.size()]), 0, coll.size());
    }

    /**
     * Returns a int array that contains the elements of the collection
     * meant - USE WITH CAUTION AS IT MAY EXPOSE THE INTERNAL STATE
     * OF THE COLLECTION
     * Returns either the wrapped array (if exists and matches the type and the
     * collection is modifiable) or a copy.
     *
     * @param coll the collection
     * @return the array
     */
    public static Array<int[]> writeSafeIntArray(CollectionNumber coll) {
        Array<int[]> array = wrappedIntArray(coll);
        if (array != null && !((ArrayInteger) coll).isReadOnly()) {
            return array;
        }
        return new Array<int[]>(coll.toArray(new int[coll.size()]), 0, coll.size());
    }

    /**
     * Returns a short array that contains the elements of the collection
     * meant - USE WITH CAUTION AS IT MAY EXPOSE THE INTERNAL STATE
     * OF THE COLLECTION
     * Returns either the wrapped array (if exists and matches the type and the
     * collection is modifiable) or a copy.
     *
     * @param coll the collection
     * @return the array
     */
    public static Array<short[]> writeSafeShortArray(CollectionNumber coll) {
        Array<short[]> array = wrappedShortArray(coll);
        if (array != null && !((ArrayShort) coll).isReadOnly()) {
            return array;
        }
        return new Array<short[]>(coll.toArray(new short[coll.size()]), 0, coll.size());
    }

    /**
     * Returns a byte array that contains the elements of the collection
     * meant - USE WITH CAUTION AS IT MAY EXPOSE THE INTERNAL STATE
     * OF THE COLLECTION
     * Returns either the wrapped array (if exists and matches the type and the
     * collection is modifiable) or a copy.
     *
     * @param coll the collection
     * @return the array
     */
    public static Array<byte[]> writeSafeByteArray(CollectionNumber coll) {
        Array<byte[]> array = wrappedByteArray(coll);
        if (array != null && !((ArrayByte) coll).isReadOnly()) {
            return array;
        }
        return new Array<byte[]>(coll.toArray(new byte[coll.size()]), 0, coll.size());
    }

}
