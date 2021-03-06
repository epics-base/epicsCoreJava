/*
 * Copyright (c) 2009 by Cosylab
 *
 * The full license specifying the redistribution, modification, usage and other
 * rights and obligations is included with the distribution of this project in
 * the file "LICENSE-CAJ". If the license is not included visit Cosylab web site,
 * <http://www.cosylab.com>.
 *
 * THIS SOFTWARE IS PROVIDED AS-IS WITHOUT WARRANTY OF ANY KIND, NOT EVEN THE
 * IMPLIED WARRANTY OF MERCHANTABILITY. THE AUTHOR OF THIS SOFTWARE, ASSUMES
 * _NO_ RESPONSIBILITY FOR ANY CONSEQUENCE RESULTING FROM THE USE, MODIFICATION,
 * OR REDISTRIBUTION OF THIS SOFTWARE.
 */

package org.epics.pvaccess.util;

/**
 * Implementaton of circular FIFO buffer.
 * If buffer is full, the oldest element is replaced with the new one.
 * Instance is not a bit synchronized.
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @param <T> buffer element type.
 */
public final class CircularBuffer<T> {

	/**
	 * Array (circular buffer) of elements.
	 */
	private final T[] elements;
	
	/**
	 * Take (read) pointer.
	 */
	private int takePointer = 0;
	
	/**
	 * Put (write) pointer.
	 */
	private int putPointer = 0;       

	/**
	 * Number of elements in the buffer.
	 */
	private int count = 0;
	
	/**
	 * Create a BoundedBuffer with the given capacity.
	 * @param capacity buffer capacity.
	 * @exception IllegalArgumentException if capacity less or equal to zero
	 **/
	@SuppressWarnings("unchecked")
	public CircularBuffer(int capacity) throws IllegalArgumentException {
		if (capacity <= 0)
			throw new IllegalArgumentException();
		elements = (T[])new Object[capacity];
	}

	/**
	 * Get number of elements in the buffer.
	 * @return number of elements in the buffer.
	 */
	public int size() { return count; }

	/**
	 * Get buffer capacity.
	 * @return buffer capacity.
	 */
	public int capacity() { return elements.length; }

	/**
	 * Insert a new element in to the buffer. If buffer full, oldest element will be overriden.
	 * @param x element to insert.
	 * @return overriden element, if any.
	 */
	public final T insert(T x) {
		if (count < elements.length)
			++count;
		else
			if (++takePointer >= elements.length) takePointer = 0;
		
		final T old = elements[putPointer];
		elements[putPointer] = x;
		if (++putPointer >= elements.length) putPointer = 0;
		return old;
	}

	/**
	 * Extract the oldest element from the buffer.
	 * @return the oldest element from the buffer.
	 */
	public final T extract() {
		if (count == 0)
			return null;
		count--;
		final T old = elements[takePointer];
		elements[takePointer] = null;
		if (++takePointer >= elements.length) takePointer = 0;
		return old;
	}
}
