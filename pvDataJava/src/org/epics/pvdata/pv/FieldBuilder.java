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
     * Add a <code>Field</code> (e.g. <code>Structure</code>, <code>Union</code>).
     * @param name name of the array.
     * @param field a field to add.
     * @return this instance of a <code>FieldBuilder</code>.
     */
    public FieldBuilder add(String name, Field field);

    /**
     * Add array of <code>Scalar</code> elements.
     * @param name name of the array.
     * @param scalarType type of a scalar element.
     * @return this instance of a <code>FieldBuilder</code>.
     */
    public FieldBuilder addArray(String name, ScalarType scalarType);
    
    /**
     * Add array of <code>Field</code> elements.
     * @param name name of the array.
     * @param field a type of an array element.
     * @return this instance of a <code>FieldBuilder</code>.
     */
    public FieldBuilder addArray(String name, Field element);

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
     * @see #createNested()
     */
    FieldBuilder addStructure(String name); 
    
    /**
     * Add new nested <code>Union</code>.
     * <code>createNested()</code> method must be called
     * to complete creation of the nested <code>Union</code>.
     * @param name nested union name.
     * @return a new instance of a <code>FieldBuilder</code> is returned.
     * @see #createNested()
     */
    FieldBuilder addUnion(String name);
    
    /**
     * Add new nested <code>Structure[]</code>.
     * <code>createNested()</code> method must be called
     * to complete creation of the nested <code>Structure</code>.
     * @param name nested structure name.
     * @return a new instance of a <code>FieldBuilder</code> is returned.
     * @see #createNested()
     */
    FieldBuilder addStructureArray(String name); 
    
    /**
     * Add new nested <code>Union[]</code>.
     * <code>createNested()</code> method must be called
     * to complete creation of the nested <code>Union</code>.
     * @param name nested union name.
     * @return a new instance of a <code>FieldBuilder</code> is returned.
     * @see #createNested()
     */
    FieldBuilder addUnionArray(String name);

    /**
     * Complete the creation of a nested object.
     * @see #addStructure(String)
     * @see #addUnion(String)
     * @return a previous (parent) <code>FieldBuilder</code>.
     */
    FieldBuilder createNested();
}
