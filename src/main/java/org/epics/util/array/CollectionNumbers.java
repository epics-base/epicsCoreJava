/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.util.array;

/**
 * Utilities to work with number collections.
 *
 * @author carcassi
 */
public class CollectionNumbers {

    private CollectionNumbers() {
        // prevent instances
    }
    
    /**
     * This is the implementation of the default CollectionNumber.toArray(array).
     * See that method for the specification.
     */
    static <T> T defaultToArray(CollectionNumber coll, T array) {
        if (array instanceof double[]) {
            double[] doubleArray;
            if (((double[]) array).length < coll.size()) {
                doubleArray = new double[coll.size()];
            } else {
                doubleArray = (double[]) array;
            }
            arrayCopy(coll, doubleArray, 0);
            return (T) doubleArray;
        } else if (array instanceof float[]) {
            float[] floatArray;
            if (((float[]) array).length < coll.size()) {
                floatArray = new float[coll.size()];
            } else {
                floatArray = (float[]) array;
            }
            arrayCopy(coll, floatArray, 0);
            return (T) floatArray;
        } else if (array instanceof long[]) {
            long[] longArray;
            if (((long[]) array).length < coll.size()) {
                longArray = new long[coll.size()];
            } else {
                longArray = (long[]) array;
            }
            arrayCopy(coll, longArray, 0);
            return (T) longArray;
        } else if (array instanceof int[]) {
            int[] intArray;
            if (((int[]) array).length < coll.size()) {
                intArray = new int[coll.size()];
            } else {
                intArray = (int[]) array;
            }
            arrayCopy(coll, intArray, 0);
            return (T) intArray;
        } else if (array instanceof short[]) {
            short[] shortArray;
            if (((short[]) array).length < coll.size()) {
                shortArray = new short[coll.size()];
            } else {
                shortArray = (short[]) array;
            }
            arrayCopy(coll, shortArray, 0);
            return (T) shortArray;
        } else if (array instanceof byte[]) {
            byte[] byteArray;
            if (((byte[]) array).length < coll.size()) {
                byteArray = new byte[coll.size()];
            } else {
                byteArray = (byte[]) array;
            }
            arrayCopy(coll, byteArray, 0);
            return (T) byteArray;
        } else if (array == null) {
            throw new NullPointerException();
        }

        throw new ArrayStoreException("Argument must be an array of primitive numbers");
    }

    /**
     * Copies the content of the collection to an array at the desired position.
     *
     * @param src the source number collection.
     * @param dest the destination array.
     * @param destPos starting position in the destination array.
     * @exception  IndexOutOfBoundsException  if copying would cause
     *               access of data outside array bounds.
     * @exception  NullPointerException if either <code>src</code> or
     *               <code>dest</code> is <code>null</code>.
     */
    public static void arrayCopy(CollectionNumber src, double[] dest, int destPos) {
        // Check boundaries
        if (destPos + src.size() > dest.length) {
            throw new IndexOutOfBoundsException("Length of target array too small");
        }
        
        IteratorNumber iter = src.iterator();
        int index = destPos;
        while (iter.hasNext()) {
            dest[index] = iter.nextDouble();
            index++;
        }
    }

    /**
     * Copies the content of the collection to an array at the desired position.
     *
     * @param src the source number collection.
     * @param dest the destination array.
     * @param destPos starting position in the destination array.
     * @exception  IndexOutOfBoundsException  if copying would cause
     *               access of data outside array bounds.
     * @exception  NullPointerException if either <code>src</code> or
     *               <code>dest</code> is <code>null</code>.
     */
    public static void arrayCopy(CollectionNumber src, float[] dest, int destPos) {
        // Check boundaries
        if (destPos + src.size() > dest.length) {
            throw new IndexOutOfBoundsException("Length of target array too small");
        }
        
        IteratorNumber iter = src.iterator();
        int index = destPos;
        while (iter.hasNext()) {
            dest[index] = iter.nextFloat();
            index++;
        }
    }

    /**
     * Copies the content of the collection to an array at the desired position.
     *
     * @param src the source number collection.
     * @param dest the destination array.
     * @param destPos starting position in the destination array.
     * @exception  IndexOutOfBoundsException  if copying would cause
     *               access of data outside array bounds.
     * @exception  NullPointerException if either <code>src</code> or
     *               <code>dest</code> is <code>null</code>.
     */
    public static void arrayCopy(CollectionNumber src, long[] dest, int destPos) {
        // Check boundaries
        if (destPos + src.size() > dest.length) {
            throw new IndexOutOfBoundsException("Length of target array too small");
        }
        
        IteratorNumber iter = src.iterator();
        int index = destPos;
        while (iter.hasNext()) {
            dest[index] = iter.nextLong();
            index++;
        }
    }

    /**
     * Copies the content of the collection to an array at the desired position.
     *
     * @param src the source number collection.
     * @param dest the destination array.
     * @param destPos starting position in the destination array.
     * @exception  IndexOutOfBoundsException  if copying would cause
     *               access of data outside array bounds.
     * @exception  NullPointerException if either <code>src</code> or
     *               <code>dest</code> is <code>null</code>.
     */
    public static void arrayCopy(CollectionNumber src, int[] dest, int destPos) {
        // Check boundaries
        if (destPos + src.size() > dest.length) {
            throw new IndexOutOfBoundsException("Length of target array too small");
        }
        
        IteratorNumber iter = src.iterator();
        int index = destPos;
        while (iter.hasNext()) {
            dest[index] = iter.nextInt();
            index++;
        }
    }

    /**
     * Copies the content of the collection to an array at the desired position.
     *
     * @param src the source number collection.
     * @param dest the destination array.
     * @param destPos starting position in the destination array.
     * @exception  IndexOutOfBoundsException  if copying would cause
     *               access of data outside array bounds.
     * @exception  NullPointerException if either <code>src</code> or
     *               <code>dest</code> is <code>null</code>.
     */
    public static void arrayCopy(CollectionNumber src, short[] dest, int destPos) {
        // Check boundaries
        if (destPos + src.size() > dest.length) {
            throw new IndexOutOfBoundsException("Length of target array too small");
        }
        
        IteratorNumber iter = src.iterator();
        int index = destPos;
        while (iter.hasNext()) {
            dest[index] = iter.nextShort();
            index++;
        }
    }

    /**
     * Copies the content of the collection to an array at the desired position.
     *
     * @param src the source number collection.
     * @param dest the destination array.
     * @param destPos starting position in the destination array.
     * @exception  IndexOutOfBoundsException  if copying would cause
     *               access of data outside array bounds.
     * @exception  NullPointerException if either <code>src</code> or
     *               <code>dest</code> is <code>null</code>.
     */
    public static void arrayCopy(CollectionNumber src, byte[] dest, int destPos) {
        // Check boundaries
        if (destPos + src.size() > dest.length) {
            throw new IndexOutOfBoundsException("Length of target array too small");
        }
        
        IteratorNumber iter = src.iterator();
        int index = destPos;
        while (iter.hasNext()) {
            dest[index] = iter.nextByte();
            index++;
        }
    }
}
