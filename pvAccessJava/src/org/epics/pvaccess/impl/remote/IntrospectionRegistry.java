/*
 * Copyright (c) 2008 by Cosylab
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

package org.epics.pvaccess.impl.remote;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.epics.pvaccess.PVFactory;
import org.epics.pvaccess.util.BooleanHolder;
import org.epics.pvdata.pv.DeserializableControl;
import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.SerializableControl;
import org.epics.pvdata.pv.Type;



/**
 * PVData Structure registry.
 * Registry is used to cache introspection interfaces to minimize network traffic.
 * This class is not thread safe (optimized to be used only by one thread).
 * @author msekoranja
 */
public final class IntrospectionRegistry {

	protected Map<Short, Field> registry =
			new HashMap<Short, Field>();
	protected short pointer;

	public IntrospectionRegistry()
	{
		reset();
	}

	/**
	 * Reset registry, i.e. must be done when transport is changed (server restarted).
	 */
	public void reset()
	{
		pointer = 1;
		registry.clear();
	}
	/**
	 * Get introspection interface for given ID.
	 * @param id the ID.
	 * @return <code>Field</code> instance for given ID.
	 */
	public Field getIntrospectionInterface(short id)
	{
		return registry.get(id);
	}

	/**
	 * Register introspection interface with given ID.
	 * @param id ID to use to register.
	 * @param field field to register.
	 */
	public void registerIntrospectionInterface(short id, Field field)
	{
		registry.put(id, field);
	}

	/**
	 * Private helper variable (optimization).
	 */
	private Short shortHolder = Short.valueOf((short)0);

	/**
	 * Register introspection interface and get it's ID. Always OUTGOING.
	 * If it is already registered only preassigned ID is returned.
	 * @param field field to register.
	 * @param existing indication whether the field was already registered.
	 * @return id of given <code>Field</code>
	 */
	public short registerIntrospectionInterface(Field field, BooleanHolder existing)
	{
		Field potentiallyExistingField = registry.get(shortHolder);
		if(potentiallyExistingField != null && potentiallyExistingField.equals(field)) {
			existing.value = true;
			return shortHolder;
		}
		else
		{
			existing.value = false;
			final short key = pointer++;
			registry.put(key, field);
			return key;
		}
	}

	/**
	 * Null type.
	 */
	public static final byte NULL_TYPE_CODE = (byte)-1;

	/**
	 * Serialization contains only an ID (that was assigned by one of the previous <code>FULL_WITH_ID</code> descriptions).
	 */
	public static final byte ONLY_ID_TYPE_CODE = (byte)-2;

	/**
	 * Serialization contains an ID (that can be used later, if cached) and full interface description.
	 */
	public static final byte FULL_WITH_ID_TYPE_CODE = (byte)-3;


	public final void serialize(Field field, ByteBuffer buffer, SerializableControl control) {
		if (field == null) {
			SerializationHelper.serializeNullField(buffer, control);
		}
		else
		{
			// do not cache scalars, scalarArrays
			// ... and (array of) variant unions - not worth the complex condition,
			// unless bool Field.cache() would exist
			if (field.getType() != Type.scalar &&
				field.getType() != Type.scalarArray)
			{
				BooleanHolder existing = new BooleanHolder();
				final short key = registerIntrospectionInterface(field, existing);
				if (existing.value) {
					control.ensureBuffer(3);
					buffer.put(ONLY_ID_TYPE_CODE);
					buffer.putShort(key);
					return;
				}
				else {
					control.ensureBuffer(3);
					buffer.put(FULL_WITH_ID_TYPE_CODE);	// could also be a mask
					buffer.putShort(key);
				}
			}

			field.serialize(buffer, control);
		}
	}

	static final FieldCreate fieldCreate = PVFactory.getFieldCreate();

	public final Field deserialize(ByteBuffer buffer, DeserializableControl control) {

		control.ensureData(1);
		int pos = buffer.position();
		final byte typeCode = buffer.get();

		if (typeCode == NULL_TYPE_CODE)
		{
			return null;
		}
		else if (typeCode == ONLY_ID_TYPE_CODE)
		{
			control.ensureData(Short.SIZE/Byte.SIZE);
			return getIntrospectionInterface(buffer.getShort());
		}
		// could also be a mask
		else if (typeCode == FULL_WITH_ID_TYPE_CODE)
		{
			control.ensureData(Short.SIZE/Byte.SIZE);
			final short key = buffer.getShort();
			final Field field = fieldCreate.deserialize(buffer, control);
			registerIntrospectionInterface(key, field);
			return field;
		}
		else
		{
			// return typeCode back
			buffer.position(pos);
			return fieldCreate.deserialize(buffer, control);
		}
	}

}
