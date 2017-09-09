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
     * Takes a primitive array and wraps it into the appropriate mutable
     * array wrapper.
     * 
     * @param values a primitive array (e.g. int[])
     * @return a mutable wrapper
     * @exception IllegalArgumentException  if the given object is not
     *               a primitive array.
     */
    public static ListNumber toList(Object values) {
        if (values instanceof double[]) {
            return toListDouble((double[]) values);
        } else if (values instanceof float[]) {
            return toListFloat((float[]) values);
        } else if (values instanceof long[]) {
            return toListLong((long[]) values);
        } else if (values instanceof int[]) {
            return toListInt((int[]) values);
        } else if (values instanceof short[]) {
            return toListShort((short[]) values);
        } else if (values instanceof byte[]) {
            return toListByte((byte[]) values);
        } else {
            throw new IllegalArgumentException(values + " is not a an array of primitive numbers");
        }
    }
    
    
    // Design tradeoff:
    // Ideally, it would have been better to have all the methods named
    // the same (i.e. toList) and let the compiler pick the correct one. Unfortunately,
    // varargs, primitives, casting and overriding do not play together as one
    // would expect. First, the generic method must have signature toList(Object).
    // This means that any vararg calls with one argument (i.e. toList(1) ) goes
    // to the generic method which expects an actual array. One would have to
    // implement the methods with a signle primitive (i.e. toList(int) ) to have
    // the correct behavior. Moreover, the vararg method is chosen depending
    // on the wider primitive in the list, which may make it confusing to use.
    // Last, byte and short can't really use varargs as one would have to cast
    // every single element of the list. This remains a somewhat imperfect solution.
    
    /**
     * Takes a double array and wraps it into an ArrayDouble.
     * 
     * @param values a primitive array
     * @return a mutable wrapper
     * @exception IllegalArgumentException  if the given object is not
     *               a primitive array.
     */
    public static ArrayDouble toListDouble(double... values) {
        return new ArrayDouble(values, 0, values.length, false);
    }
    
    /**
     * Takes a float array and wraps it into an ArrayFloat.
     * 
     * @param values a primitive array
     * @return a mutable wrapper
     * @exception IllegalArgumentException  if the given object is not
     *               a primitive array.
     */
    public static ArrayFloat toListFloat(float... values) {
        return new ArrayFloat(values, 0, values.length, false);
    }
    
    /**
     * Takes a long array and wraps it into an ArrayLong.
     * 
     * @param values a primitive array
     * @return a mutable wrapper
     * @exception IllegalArgumentException  if the given object is not
     *               a primitive array.
     */
    public static ArrayLong toListLong(long... values) {
        return new ArrayLong(values, 0, values.length, false);
    }
    
    /**
     * Takes an int array and wraps it into an ArrayInt.
     * 
     * @param values a primitive array
     * @return a mutable wrapper
     * @exception IllegalArgumentException  if the given object is not
     *               a primitive array.
     */
    public static ArrayInt toListInt(int... values) {
        return new ArrayInt(values, 0, values.length, false);
    }
    
    /**
     * Takes a short array and wraps it into an ArrayShort.
     * 
     * @param values a primitive array
     * @return a mutable wrapper
     * @exception IllegalArgumentException  if the given object is not
     *               a primitive array.
     */
    public static ArrayShort toListShort(short... values) {
        return new ArrayShort(values, 0, values.length, false);
    }
    
    /**
     * Takes a byte array and wraps it into an ArrayByte.
     * 
     * @param values a primitive array
     * @return a mutable wrapper
     * @exception IllegalArgumentException  if the given object is not
     *               a primitive array.
     */
    public static ArrayByte toListByte(byte... values) {
        return new ArrayByte(values, 0, values.length, false);
    }
    
    public static ListNumber unmodifiableList(ListNumber list) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
    /**
     * Returns an unmodifiable view of the specified list.
     * 
     * @param list the list for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified list.
     */
    public static ArrayDouble unmodifiableList(ArrayDouble list) {
        return new ArrayDouble(list.wrappedArray(), list.startIndex(), list.size(), true);
    }
    
    /**
     * Returns an unmodifiable view of the specified list.
     * 
     * @param list the list for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified list.
     */
    public static ArrayFloat unmodifiableList(ArrayFloat list) {
        return new ArrayFloat(list.wrappedArray(), list.startIndex(), list.size(), true);
    }
    
    /**
     * Returns an unmodifiable view of the specified list.
     * 
     * @param list the list for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified list.
     */
    public static ArrayLong unmodifiableList(ArrayLong list) {
        return new ArrayLong(list.wrappedArray(), list.startIndex(), list.size(), true);
    }
    
    /**
     * Returns an unmodifiable view of the specified list.
     * 
     * @param list the list for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified list.
     */
    public static ArrayInt unmodifiableList(ArrayInt list) {
        return new ArrayInt(list.wrappedArray(), list.startIndex(), list.size(), true);
    }
    
    /**
     * Returns an unmodifiable view of the specified list.
     * 
     * @param list the list for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified list.
     */
    public static ArrayShort unmodifiableList(ArrayShort list) {
        return new ArrayShort(list.wrappedArray(), list.startIndex(), list.size(), true);
    }
    
    /**
     * Returns an unmodifiable view of the specified list.
     * 
     * @param list the list for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified list.
     */
    public static ArrayByte unmodifiableList(ArrayByte list) {
        return new ArrayByte(list.wrappedArray(), list.startIndex(), list.size(), true);
    }
    
    /**
     * Returns an unmodifiable {@link ArrayDouble} wrapper for the given {@code double} array.
     * 
     * @param values a primitive array.
     * @return an immutable wrapper.
     */
    public static ArrayDouble unmodifiableListDouble(double... values) {
        return new ArrayDouble(values, 0, values.length, true);
    }
    
    /**
     * Returns an unmodifiable {@link ArrayFloat} wrapper for the given {@code float} array.
     * 
     * @param values a primitive array.
     * @return an immutable wrapper.
     */
    public static ArrayFloat unmodifiableListFloat(float... values) {
        return new ArrayFloat(values, 0, values.length, true);
    }
    
    /**
     * Returns an unmodifiable {@link ArrayLong} wrapper for the given {@code long} array.
     * 
     * @param values a primitive array.
     * @return an immutable wrapper.
     */
    public static ArrayLong unmodifiableListLong(long... values) {
        return new ArrayLong(values, 0, values.length, true);
    }
    
    /**
     * Returns an unmodifiable {@link ArrayInt} wrapper for the given {@code int} array.
     * 
     * @param values a primitive array.
     * @return an immutable wrapper.
     */
    public static ArrayInt unmodifiableListInt(int... values) {
        return new ArrayInt(values, 0, values.length, true);
    }
    
    /**
     * Returns an unmodifiable {@link ArrayShort} wrapper for the given {@code short} array.
     * 
     * @param values a primitive array.
     * @return an immutable wrapper.
     */
    public static ArrayShort unmodifiableListShort(short... values) {
        return new ArrayShort(values, 0, values.length, true);
    }
    
    /**
     * Returns an unmodifiable {@link ArrayByte} wrapper for the given {@code byte} array.
     * 
     * @param values a primitive array.
     * @return an immutable wrapper.
     */
    public static ArrayByte unmodifiableListByte(byte... values) {
        return new ArrayByte(values, 0, values.length, true);
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
