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
     * Copies the content of the collection to an array.
     *
     * @param coll the collection
     * @return the array
     */
    public static float[] floatArrayCopyOf(CollectionNumber coll) {
        float[] data = new float[coll.size()];
        IteratorNumber iter = coll.iterator();
        int index = 0;
        while (iter.hasNext()) {
            data[index] = iter.nextFloat();
            index++;
        }
        return data;
    }

    /**
     * Copies the content of the collection to an array.
     *
     * @param coll the collection
     * @return the array
     */
    public static double[] doubleArrayCopyOf(CollectionNumber coll) {
        double[] data = new double[coll.size()];
        IteratorNumber iter = coll.iterator();
        int index = 0;
        while (iter.hasNext()) {
            data[index] = iter.nextDouble();
            index++;
        }
        return data;
    }

    /**
     * Copies the content of the collection to an array.
     *
     * @param coll the collection
     * @return the array
     */
    public static byte[] byteArrayCopyOf(CollectionNumber coll) {
        byte[] data = new byte[coll.size()];
        IteratorNumber iter = coll.iterator();
        int index = 0;
        while (iter.hasNext()) {
            data[index] = iter.nextByte();
            index++;
        }
        return data;
    }

    /**
     * Copies the content of the collection to an array.
     *
     * @param coll the collection
     * @return the array
     */
    public static short[] shortArrayCopyOf(CollectionNumber coll) {
        short[] data = new short[coll.size()];
        IteratorNumber iter = coll.iterator();
        int index = 0;
        while (iter.hasNext()) {
            data[index] = iter.nextShort();
            index++;
        }
        return data;
    }

    /**
     * Copies the content of the collection to an array.
     *
     * @param coll the collection
     * @return the array
     */
    public static int[] intArrayCopyOf(CollectionNumber coll) {
        int[] data = new int[coll.size()];
        IteratorNumber iter = coll.iterator();
        int index = 0;
        while (iter.hasNext()) {
            data[index] = iter.nextInt();
            index++;
        }
        return data;
    }

    /**
     * Copies the content of the collection to an array.
     *
     * @param coll the collection
     * @return the array
     */
    public static long[] longArrayCopyOf(CollectionNumber coll) {
        long[] data = new long[coll.size()];
        IteratorNumber iter = coll.iterator();
        int index = 0;
        while (iter.hasNext()) {
            data[index] = iter.nextLong();
            index++;
        }
        return data;
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
