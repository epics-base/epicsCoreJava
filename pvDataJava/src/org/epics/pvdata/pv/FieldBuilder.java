/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata.pv;

/**
 * Interface for in-line creating of introspection interfaces.
 * One instance can be used to create multiple <code>Field</code> instances.
 * An instance of this object must not be used concurrently (an object has a state).
 * @author mse
 */
public interface FieldBuilder
{
	/**
	 * Set ID of an object to be created.
	 * @param id id to be set.
     * @return this instance of a <code>FieldBuilder</code>.
	 */
	public FieldBuilder setId(String id);

    /**
     * Add a <code>Scalar</code>.
     * @param name name of the array.
     * @param scalarType type of a scalar to add.
     * @return this instance of a <code>FieldBuilder</code>.
     */
    public FieldBuilder add(String name, ScalarType scalarType);

    /**
     * Add a bounded string.
     * @param name name of the bounded string.
     * @param maxLength a maximum string length.
     * @return this instance of a <code>FieldBuilder</code>.
     */
    public FieldBuilder addBoundedString(String name, int maxLength);

    /**
     * Add a <code>Field</code> (e.g. <code>Structure</code>, <code>Union</code>).
     * @param name name of the array.
     * @param field a field to add.
     * @return this instance of a <code>FieldBuilder</code>.
     */
    public FieldBuilder add(String name, Field field);

    /**
     * Add variable size array of <code>Scalar</code> elements.
     * @param name name of the array.
     * @param scalarType type of a scalar element.
     * @return this instance of a <code>FieldBuilder</code>.
     */
    public FieldBuilder addArray(String name, ScalarType scalarType);
    
    /**
     * Add fixed-size array of <code>Scalar</code> elements.
     * @param name name of the array.
     * @param scalarType type of a scalar element.
     * @param size Array fixed size.
     * @return this instance of a <code>FieldBuilder</code>.
     */
    public FieldBuilder addFixedArray(String name, ScalarType scalarType, int size);

    /**
     * Add bounded-size array of <code>Scalar</code> elements.
     * @param name name of the array.
     * @param scalarType type of a scalar element.
     * @param bound Array maximum capacity (size).
     * @return this instance of a <code>FieldBuilder</code>.
     */
    public FieldBuilder addBoundedArray(String name, ScalarType scalarType, int bound);

    /**
     * Add array of <code>Field</code> elements.
     * @param name name of the array.
     * @param field The type of each array element.
     * @return this instance of a <code>FieldBuilder</code>.
     */
    public FieldBuilder addArray(String name, Field field);

    /**
     * Create a <code>Structure</code>.
     * This resets this instance state and allows new <code>Field</code> instance to be created.
     * @return a new instance of a <code>Structure</code>.
     */
    public Structure createStructure();
    
    /**
     * Create an <code>Union</code>.
     * This resets this instance state and allows new <code>Field</code> instance to be created.
     * @return a new instance of an <code>Union</code>.
     */
    public Union createUnion();

    /**
     * Add new nested <code>Structure</code>.
     * <code>createNested()</code> method must be called
     * to complete creation of the nested <code>Structure</code>.
     * @param name nested structure name.
     * @return a new instance of a <code>FieldBuilder</code> is returned.
     * @see #endNested()
     */
    FieldBuilder addNestedStructure(String name); 
    
    /**
     * Add new nested <code>Union</code>.
     * <code>createNested()</code> method must be called
     * to complete creation of the nested <code>Union</code>.
     * @param name nested union name.
     * @return a new instance of a <code>FieldBuilder</code> is returned.
     * @see #endNested()
     */
    FieldBuilder addNestedUnion(String name);
    
    /**
     * Add new nested <code>Structure[]</code>.
     * <code>createNested()</code> method must be called
     * to complete creation of the nested <code>Structure</code>.
     * @param name nested structure name.
     * @return a new instance of a <code>FieldBuilder</code> is returned.
     * @see #endNested()
     */
    FieldBuilder addNestedStructureArray(String name); 
    
    /**
     * Add new nested <code>Union[]</code>.
     * <code>createNested()</code> method must be called
     * to complete creation of the nested <code>Union</code>.
     * @param name nested union name.
     * @return a new instance of a <code>FieldBuilder</code> is returned.
     * @see #endNested()
     */
    FieldBuilder addNestedUnionArray(String name);

    /**
     * Complete the creation of a nested object.
     * @see #addNestedStructure(String)
     * @see #addNestedUnion(String)
     * @return a previous (parent) <code>FieldBuilder</code>.
     */
    FieldBuilder endNested();
}
