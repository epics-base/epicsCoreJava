/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata.factory;

import java.util.LinkedHashMap;

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
	
	@Override
	public FieldBuilder setId(String id) {
		this.id = id;
		return this;
	}

	@Override
	public FieldBuilder add(String name, ScalarType scalarType) {
		members.put(name, fieldCreate.createScalar(scalarType));
		return this;
	}

	@Override
	public FieldBuilder add(String name, Field field) {
		members.put(name, field);
		return this;
	}

	@Override
	public FieldBuilder addArray(String name, ScalarType scalarType) {
		members.put(name, fieldCreate.createScalarArray(scalarType));
		return this;
	}

	@Override
	public FieldBuilder addArray(String name, Field element) {
		if (element instanceof Structure)
			members.put(name, fieldCreate.createStructureArray((Structure)element));
		else if (element instanceof Union)
			members.put(name, fieldCreate.createUnionArray((Union)element));
		else if (element instanceof Scalar)
			members.put(name, fieldCreate.createScalarArray(((Scalar)element).getScalarType()));
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

	@Override
	public Structure createStructure() {
		if (parentBuilder != null)
			throw new IllegalStateException("createStructure() called in nested FieldBuilder");
		
		Structure structure = createFieldInternal(Structure.class);
		reset();
		return structure;
	}

	@Override
	public Union createUnion() {
		if (parentBuilder != null)
			throw new IllegalStateException("createStructure() called in nested FieldBuilder");

		Union union = createFieldInternal(Union.class);
		reset();
		return union;
	}

	@Override
	public FieldBuilder addNestedStructure(String name) {
		return new BaseFieldBuilder(this, name, Structure.class, false);
	}

	@Override
	public FieldBuilder addNestedUnion(String name) {
		return new BaseFieldBuilder(this, name, Union.class, false);
	}

	@Override
	public FieldBuilder addStructureArray(String name) {
		return new BaseFieldBuilder(this, name, Structure.class, true);
	}

	@Override
	public FieldBuilder addUnionArray(String name) {
		return new BaseFieldBuilder(this, name, Union.class, true);
	}

	@Override
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
