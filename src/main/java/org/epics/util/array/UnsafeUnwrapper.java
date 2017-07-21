/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.util.array;

import static org.epics.util.array.CollectionNumbers.byteArrayCopyOf;
import static org.epics.util.array.CollectionNumbers.doubleArrayCopyOf;
import static org.epics.util.array.CollectionNumbers.floatArrayCopyOf;
import static org.epics.util.array.CollectionNumbers.intArrayCopyOf;
import static org.epics.util.array.CollectionNumbers.longArrayCopyOf;
import static org.epics.util.array.CollectionNumbers.shortArrayCopyOf;

/**
 * Utilities to work with number collections.
 *
 * @author carcassi
 */
public class UnsafeUnwrapper {

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
    public static Object wrappedArray(CollectionNumber coll) {
        Object data = wrappedFloatArray(coll);
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
    public static float[] wrappedFloatArray(CollectionNumber coll) {
        if (coll instanceof ArrayFloat) {
            return ((ArrayFloat) coll).wrappedArray();
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
    public static double[] wrappedDoubleArray(CollectionNumber coll) {
        if (coll instanceof ArrayDouble) {
            return ((ArrayDouble) coll).wrappedArray();
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
    public static byte[] wrappedByteArray(CollectionNumber coll) {
        if (coll instanceof ArrayByte) {
            return ((ArrayByte) coll).wrappedArray();
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
    public static short[] wrappedShortArray(CollectionNumber coll) {
        if (coll instanceof ArrayShort) {
            return ((ArrayShort) coll).wrappedArray();
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
    public static int[] wrappedIntArray(CollectionNumber coll) {
        if (coll instanceof ArrayInt) {
            return ((ArrayInt) coll).wrappedArray();
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
    public static long[] wrappedLongArray(CollectionNumber coll) {
        if (coll instanceof ArrayLong) {
            return ((ArrayLong) coll).wrappedArray();
        }

        return null;
    }

    /**
     * Returns either the wrapped array (if exists and matches the type)
     * or a copy - USE WITH CAUTION AS IT MAY EXPOSE THE INTERNAL STATE
     * OF THE COLLECTION.
     *
     * @param coll the collection
     * @return the array
     */
    public static float[] floatArrayWrappedOrCopy(CollectionNumber coll) {
        float[] array = wrappedFloatArray(coll);
        if (array != null) {
            return array;
        }
        return floatArrayCopyOf(coll);
    }

    /**
     * Returns either the wrapped array (if exists and matches the type)
     * or a copy - USE WITH CAUTION AS IT MAY EXPOSE THE INTERNAL STATE
     * OF THE COLLECTION.
     *
     * @param coll the collection
     * @return the array
     */
    public static double[] doubleArrayWrappedOrCopy(CollectionNumber coll) {
        double[] array = wrappedDoubleArray(coll);
        if (array != null) {
            return array;
        }
        return doubleArrayCopyOf(coll);
    }

    /**
     * Returns either the wrapped array (if exists and matches the type)
     * or a copy - USE WITH CAUTION AS IT MAY EXPOSE THE INTERNAL STATE
     * OF THE COLLECTION.
     *
     * @param coll the collection
     * @return the array
     */
    public static byte[] byteArrayWrappedOrCopy(CollectionNumber coll) {
        byte[] array = wrappedByteArray(coll);
        if (array != null) {
            return array;
        }
        return byteArrayCopyOf(coll);
    }

    /**
     * Returns either the wrapped array (if exists and matches the type)
     * or a copy - USE WITH CAUTION AS IT MAY EXPOSE THE INTERNAL STATE
     * OF THE COLLECTION.
     *
     * @param coll the collection
     * @return the array
     */
    public static short[] shortArrayWrappedOrCopy(CollectionNumber coll) {
        short[] array = wrappedShortArray(coll);
        if (array != null) {
            return array;
        }
        return shortArrayCopyOf(coll);
    }

    /**
     * Returns either the wrapped array (if exists and matches the type)
     * or a copy - USE WITH CAUTION AS IT MAY EXPOSE THE INTERNAL STATE
     * OF THE COLLECTION.
     *
     * @param coll the collection
     * @return the array
     */
    public static int[] intArrayWrappedOrCopy(CollectionNumber coll) {
        int[] array = wrappedIntArray(coll);
        if (array != null) {
            return array;
        }
        return intArrayCopyOf(coll);
    }

    /**
     * Returns either the wrapped array (if exists and matches the type)
     * or a copy - USE WITH CAUTION AS IT MAY EXPOSE THE INTERNAL STATE
     * OF THE COLLECTION.
     *
     * @param coll the collection
     * @return the array
     */
    public static long[] longArrayWrappedOrCopy(CollectionNumber coll) {
        long[] array = wrappedLongArray(coll);
        if (array != null) {
            return array;
        }
        return longArrayCopyOf(coll);
    }
    
}
