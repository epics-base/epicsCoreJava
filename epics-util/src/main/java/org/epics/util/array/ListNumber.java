/**
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.array;

/**
 * An ordered collection of numeric (primitive) elements. This class allows to implement
 * a single binding for a list of primitive values of six different
 * binding. If the original type is required, instanceof can be used to
 * differentiate between {@link ListDouble}, {@link ListFloat}, {@link ListLong},
 * {@link ListInteger}, {@link ListShort} and {@link ListByte}.
 *
 */
public interface ListNumber extends CollectionNumber {

    /**
     * Returns the element at the specified position in this list casted to a double.
     *
     * @param index position of the element to return
     * @return the element at the specified position in this list
     * @throws IndexOutOfBoundsException if the index is out of range
     *         (<tt>index &lt; 0 || index &gt;= size()</tt>)
     */
    double getDouble(int index);

    /**
     * Returns the element at the specified position in this list casted to a float.
     *
     * @param index position of the element to return
     * @return the element at the specified position in this list
     * @throws IndexOutOfBoundsException if the index is out of range
     *         (<tt>index &lt; 0 || index &gt;= size()</tt>)
     */
    float getFloat(int index);

    /**
     * Returns the element at the specified position in this list casted to a long.
     *
     * @param index position of the element to return
     * @return the element at the specified position in this list
     * @throws IndexOutOfBoundsException if the index is out of range
     *         (<tt>index &lt; 0 || index &gt;= size()</tt>)
     */
    long getLong(int index);

    /**
     * Returns the element at the specified position in this list casted to an int.
     *
     * @param index position of the element to return
     * @return the element at the specified position in this list
     * @throws IndexOutOfBoundsException if the index is out of range
     *         (<tt>index &lt; 0 || index &gt;= size()</tt>)
     */
    int getInt(int index);

    /**
     * Returns the element at the specified position in this list casted to a short.
     *
     * @param index position of the element to return
     * @return the element at the specified position in this list
     * @throws IndexOutOfBoundsException if the index is out of range
     *         (<tt>index &lt; 0 || index &gt;= size()</tt>)
     */
    short getShort(int index);

    /**
     * Returns the element at the specified position in this list casted to a byte.
     *
     * @param index position of the element to return
     * @return the element at the specified position in this list
     * @throws IndexOutOfBoundsException if the index is out of range
     *         (<tt>index &lt; 0 || index &gt;= size()</tt>)
     */
    byte getByte(int index);

    /**
     * Changes the element at the specified position, casting to the internal
     * representation.
     *
     * @param index position of the element to change
     * @param value the new value
     * @throws IndexOutOfBoundsException if the index is out of range
     *         (<tt>index &lt; 0 || index &gt;= size()</tt>)
     */
    void setDouble(int index, double value);

    /**
     * Changes the element at the specified position, casting to the internal
     * representation.
     *
     * @param index position of the element to change
     * @param value the new value
     * @throws IndexOutOfBoundsException if the index is out of range
     *         (<tt>index &lt; 0 || index &gt;= size()</tt>)
     */
    void setFloat(int index, float value);

    /**
     * Changes the element at the specified position, casting to the internal
     * representation.
     *
     * @param index position of the element to change
     * @param value the new value
     * @throws IndexOutOfBoundsException if the index is out of range
     *         (<tt>index &lt; 0 || index &gt;= size()</tt>)
     */
    void setLong(int index, long value);

    /**
     * Changes the element at the specified position, casting to the internal
     * representation.
     *
     * @param index position of the element to change
     * @param value the new value
     * @throws IndexOutOfBoundsException if the index is out of range
     *         (<tt>index &lt; 0 || index &gt;= size()</tt>)
     */
    void setInt(int index, int value);

    /**
     * Changes the element at the specified position, casting to the internal
     * representation.
     *
     * @param index position of the element to change
     * @param value the new value
     * @throws IndexOutOfBoundsException if the index is out of range
     *         (<tt>index &lt; 0 || index &gt;= size()</tt>)
     */
    void setShort(int index, short value);

    /**
     * Changes the element at the specified position, casting to the internal
     * representation.
     *
     * @param index position of the element to change
     * @param value the new value
     * @throws IndexOutOfBoundsException if the index is out of range
     *         (<tt>index &lt; 0 || index &gt;= size()</tt>)
     */
    void setByte(int index, byte value);

    /**
     * Changes the elements starting at the specified position, taking them
     * based on the internal representation.
     * 
     * @param index position of the first element to change
     * @param list the new values
     * @throws IndexOutOfBoundsException if the index is out of range
     *         (<tt>index &lt; 0 || index &gt;= size()</tt>) or if this
     *         list is too short to hold the data.
     */
    void setAll(int index, ListNumber list);
    
    /**
     * Returns a view of the portion of this list between the specified
     * <tt>fromIndex</tt>, inclusive, and <tt>toIndex</tt>, exclusive.
     * 
     * @param fromIndex low endpoint (inclusive) of the subList
     * @param toIndex high endpoint (exclusive) of the subList
     * @return a view of the specified range within this list
     * @throws IndexOutOfBoundsException for an illegal endpoint index value
     *         (<tt>fromIndex &lt; 0 || toIndex &gt; size ||
     *         fromIndex &gt; toIndex</tt>)
     */
    ListNumber subList(int fromIndex, int toIndex);

}
