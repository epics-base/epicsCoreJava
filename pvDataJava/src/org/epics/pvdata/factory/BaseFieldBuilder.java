/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.factory;

import java.util.LinkedHashMap;

import org.epics.pvdata.pv.BoundedString;
import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.FieldBuilder;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.Scalar;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Structure;
import org.epics.pvdata.pv.Union;

/**
 * <code>FieldBuilder</code> implementation.
 * @author mse
 *
 */
public class BaseFieldBuilder implements FieldBuilder {

	private static FieldCreate fieldCreate = FieldFactory.getFieldCreate();

	private String id;
	private final LinkedHashMap<String, Field> members = new LinkedHashMap<String, Field>();
	private final FieldBuilder parentBuilder;
	private final Class<? extends Field> nestedClassToBuild;
	private final String nestedName;
	private final boolean nestedArray;

	/**
	 * Constructor
	 */
	public BaseFieldBuilder()
	{
		parentBuilder = null;
		nestedClassToBuild = null;
		nestedName = null;
		nestedArray = false;
	}

	private BaseFieldBuilder(FieldBuilder parentBuilder,
			String nestedName,
			Class<? extends Field> nestedClassToBuild, boolean nestedArray)
	{
		this.parentBuilder = parentBuilder;
		this.nestedClassToBuild = nestedClassToBuild;
		this.nestedName = nestedName;
		this.nestedArray = nestedArray;
	}

	private void reset()
	{
		id = null;
		members.clear();
	}

	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.FieldBuilder#setId(java.lang.String)
	 */
	public FieldBuilder setId(String id) {
		this.id = id;
		return this;
	}

	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.FieldBuilder#add(java.lang.String, org.epics.pvdata.pv.ScalarType)
	 */
	public FieldBuilder add(String name, ScalarType scalarType) {
		members.put(name, fieldCreate.createScalar(scalarType));
		return this;
	}

	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.FieldBuilder#addBoundedString(java.lang.String, int)
	 */
	public FieldBuilder addBoundedString(String name, int maxLength) {
		members.put(name, fieldCreate.createBoundedString(maxLength));
		return this;
	}

	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.FieldBuilder#add(java.lang.String, org.epics.pvdata.pv.Field)
	 */
	public FieldBuilder add(String name, Field field) {
		members.put(name, field);
		return this;
	}

	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.FieldBuilder#addArray(java.lang.String, org.epics.pvdata.pv.ScalarType)
	 */
	public FieldBuilder addArray(String name, ScalarType scalarType) {
		members.put(name, fieldCreate.createScalarArray(scalarType));
		return this;
	}

	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.FieldBuilder#addFixedArray(java.lang.String, org.epics.pvdata.pv.ScalarType, int)
	 */
	public FieldBuilder addFixedArray(String name, ScalarType scalarType,
			int size) {
		members.put(name, fieldCreate.createFixedScalarArray(scalarType, size));
		return this;
	}

	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.FieldBuilder#addBoundedArray(java.lang.String, org.epics.pvdata.pv.ScalarType, int)
	 */
	public FieldBuilder addBoundedArray(String name, ScalarType scalarType,
			int bound) {
		members.put(name, fieldCreate.createBoundedScalarArray(scalarType, bound));
		return this;
	}

	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.FieldBuilder#addArray(java.lang.String, org.epics.pvdata.pv.Field)
	 */
	public FieldBuilder addArray(String name, Field element) {
		if (element instanceof Structure)
			members.put(name, fieldCreate.createStructureArray((Structure)element));
		else if (element instanceof Union)
			members.put(name, fieldCreate.createUnionArray((Union)element));
		else if (element instanceof Scalar)
		{
			if (element instanceof BoundedString)
				throw new IllegalArgumentException("bounded string arrays are not supported");

			members.put(name, fieldCreate.createScalarArray(((Scalar)element).getScalarType()));
		}
		else
			throw new IllegalArgumentException("unsupported array element type:" + element.getClass());
		return this;
	}

	private <T> T createFieldInternal(Class<T> type)
	{
		int size = members.size();

		// minor optimization
		if (size == 0 && type.equals(Union.class))
			return type.cast(fieldCreate.createVariantUnion());

		String[] fieldNames = new String[size];
		members.keySet().toArray(fieldNames);

		Field[] fields = new Field[size];
		members.values().toArray(fields);

		if (type.equals(Structure.class))
		{
			Structure structure = (id != null) ?
				fieldCreate.createStructure(id, fieldNames, fields) :
				fieldCreate.createStructure(fieldNames, fields);

			// needed to avoid unchecked casts
			return type.cast(structure);
		}
		else if (type.equals(Union.class))
		{
			Union union = (id != null) ?
				fieldCreate.createUnion(id, fieldNames, fields) :
				fieldCreate.createUnion(fieldNames, fields);

			// needed to avoid unchecked casts
			return type.cast(union);
		}
		else
			throw new IllegalArgumentException("unsupported type: " + type);
	}

	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.FieldBuilder#createStructure()
	 */
	public Structure createStructure() {
		if (parentBuilder != null)
			throw new IllegalStateException("createStructure() called in nested FieldBuilder");

		Structure structure = createFieldInternal(Structure.class);
		reset();
		return structure;
	}

	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.FieldBuilder#createUnion()
	 */
	public Union createUnion() {
		if (parentBuilder != null)
			throw new IllegalStateException("createStructure() called in nested FieldBuilder");

		Union union = createFieldInternal(Union.class);
		reset();
		return union;
	}

	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.FieldBuilder#addNestedStructure(java.lang.String)
	 */
	public FieldBuilder addNestedStructure(String name) {
		return new BaseFieldBuilder(this, name, Structure.class, false);
	}

	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.FieldBuilder#addNestedUnion(java.lang.String)
	 */
	public FieldBuilder addNestedUnion(String name) {
		return new BaseFieldBuilder(this, name, Union.class, false);
	}

	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.FieldBuilder#addNestedStructureArray(java.lang.String)
	 */
	public FieldBuilder addNestedStructureArray(String name) {
		return new BaseFieldBuilder(this, name, Structure.class, true);
	}

	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.FieldBuilder#addNestedUnionArray(java.lang.String)
	 */
	public FieldBuilder addNestedUnionArray(String name) {
		return new BaseFieldBuilder(this, name, Union.class, true);
	}

	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.FieldBuilder#endNested()
	 */
	public FieldBuilder endNested() {
		if (parentBuilder == null)
			throw new IllegalStateException("this method can only be called to create nested fields");

		Field nestedField = createFieldInternal(nestedClassToBuild);
		if (nestedArray)
			parentBuilder.addArray(nestedName, nestedField);
		else
			parentBuilder.add(nestedName, nestedField);

		return parentBuilder;
	}

}
