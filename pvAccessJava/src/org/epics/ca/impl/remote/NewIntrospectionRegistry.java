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

package org.epics.ca.impl.remote;

import java.nio.ByteBuffer;

import org.epics.ca.PVFactory;
import org.epics.ca.util.ShortHashMap;
import org.epics.pvData.pv.DeserializableControl;
import org.epics.pvData.pv.Field;
import org.epics.pvData.pv.FieldCreate;
import org.epics.pvData.pv.PVDataCreate;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.SerializableControl;
import org.epics.pvData.pv.Status;
import org.epics.pvData.pv.StatusCreate;
import org.epics.pvData.pv.Structure;
import org.epics.pvData.pv.Type;
import org.omg.CORBA.BooleanHolder;
import org.omg.CORBA.ShortHolder;


/**
 * PVData Structure registry.
 * Registry is used to cache introspection interfaces to minimize network traffic.
 * @author msekoranja
 */
public final class NewIntrospectionRegistry {

	/**
	 * PVField factory.
	 */
	private static final PVDataCreate pvDataCreate = PVFactory.getPVDataCreate();
	
	/**
	 * Status factory.
	 */
    private static final StatusCreate statusCreate = PVFactory.getStatusCreate();

	protected ShortHashMap registry = new ShortHashMap();
	protected short pointer;
	
	public NewIntrospectionRegistry()
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
	 * @param id
	 * @return <code>Field</code> instance for given ID.
	 */
	public Field getIntrospectionInterface(short id)
	{
		return (Field)registry.get(id);
	}

	/**
	 * Register introspection interface with given ID. 
	 * @param id
	 * @param field
	 */
	public void registerIntrospectionInterface(short id, Field field)
	{
		registry.put(id, field);
	}

	/**
	 * Private helper variable (optimization).
	 */
	private ShortHolder shortHolder = new ShortHolder();
	
	/**
	 * Register introspection interface and get it's ID. Always OUTGOING.
	 * If it is already registered only preassigned ID is returned.
	 * @param field
	 * @return id of given <code>Field</code>
	 */
	public short registerIntrospectionInterface(Field field, BooleanHolder existing)
	{
		if (registry.contains(field, shortHolder))
		{
			existing.value = true;
			return shortHolder.value;
		}
		else
		{
			existing.value = false;
			registry.put(pointer++, field);
			return pointer;
		}
	}
	
	public final void serialize(Field field, ByteBuffer buffer, SerializableControl control) {
		serialize(field, null, buffer, control, this);
	}
	
	public final Field deserialize(ByteBuffer buffer, DeserializableControl control) {
		return deserialize(buffer, control, this);
	}


	public static final void serializeFull(Field field, ByteBuffer buffer, SerializableControl control) {
		serialize(field, null, buffer, control, null);
	}

	public static final Field deserializeFull(ByteBuffer buffer, DeserializableControl control) {
		return deserialize(buffer, control, null);
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
	
	public static final void serialize(Field field, Structure parent,
			ByteBuffer buffer, SerializableControl control, NewIntrospectionRegistry registry) {
		if (field == null) {
			control.ensureBuffer(1);
    		buffer.put(NULL_TYPE_CODE);
		}
		else
		{ 
			// use registry check
			// only top IFs and structures
			if (registry != null && (parent == null || field.getType() == Type.structure || field.getType() == Type.structureArray))
			{
				BooleanHolder existing = new BooleanHolder();
				final short key = registry.registerIntrospectionInterface(field, existing);
				if (existing.value) {
					control.ensureBuffer(1+Short.SIZE/Byte.SIZE);
					buffer.put(ONLY_ID_TYPE_CODE);
					buffer.putShort(key);
					return;
				} 
				else {
					control.ensureBuffer(1+Short.SIZE/Byte.SIZE);
					buffer.put(FULL_WITH_ID_TYPE_CODE);	// could also be a mask
					buffer.putShort(key);
				}
			}

			field.serialize(buffer, control);
		}
	}

	static final FieldCreate fieldCreate = PVFactory.getFieldCreate();
	
	public static final Field deserialize(ByteBuffer buffer, DeserializableControl control, NewIntrospectionRegistry registry) {

		control.ensureData(1);
		int pos = buffer.position();
		final byte typeCode = buffer.get();
		if (typeCode == NULL_TYPE_CODE)
			return null;
		else if (typeCode == ONLY_ID_TYPE_CODE) {
			if (registry == null)
				throw new IllegalStateException("deserialization provided cached ID, but no registry provided");
			control.ensureData(Short.SIZE/Byte.SIZE);
			return registry.getIntrospectionInterface(buffer.getShort());
		}

		// could also be a mask
		if (typeCode == FULL_WITH_ID_TYPE_CODE) {
			if (registry == null)
				throw new IllegalStateException("deserialization provided cached ID, but no registry provided");
			control.ensureData(Short.SIZE/Byte.SIZE);
			final short key = buffer.getShort();
			final Field field = deserialize(buffer, control, registry);
			registry.registerIntrospectionInterface(key, field);
			return field;
		}
		
		// return typeCode back
		buffer.position(pos);
		return fieldCreate.deserialize(buffer, control);
	}

	/**
	 * Serialize optional PVStructrue.
	 * @param buffer data buffer.
	 */
	public final void serializeStructure(ByteBuffer buffer, SerializableControl control, PVStructure pvStructure) {

	    if (pvStructure == null)
		    serialize(null, buffer, control);
	    else {
		    serialize(pvStructure.getField(), buffer, control);
		    pvStructure.serialize(buffer, control);
	    }
	}

	/**
	 * Deserialize optional PVStructrue.
	 * @param payloadBuffer data buffer.
	 * @return deserialized PVStructure, can be <code>null</code>.
	 */
	public final PVStructure deserializeStructure(ByteBuffer payloadBuffer, DeserializableControl control) {
	    PVStructure pvStructure = null;
	    final Field structureField = deserialize(payloadBuffer, control);
	    if (structureField != null)
	    {
	    	pvStructure = pvDataCreate.createPVStructure(null, (Structure)structureField);
	    	pvStructure.deserialize(payloadBuffer, control);
	    }
	    return pvStructure;
	}
	
	/**
	 * Serialize PVRequest.
	 * @param buffer data buffer.
	 */
	public final void serializePVRequest(ByteBuffer buffer, SerializableControl control, PVStructure pvRequest) {
		// for now ordinary structure, later can be changed
		serializeStructure(buffer, control, pvRequest);
	}

	/**
	 * Deserialize PVRequest.
	 * @param payloadBuffer data buffer.
	 * @return deserialized PVRequest, can be <code>null</code>.
	 */
	public final PVStructure deserializePVRequest(ByteBuffer payloadBuffer, DeserializableControl control) {
		// for now ordinary structure, later can be changed
		return deserializeStructure(payloadBuffer, control);
	}
		
	/**
	 * Deserialize Structure and create PVStructure instance.
	 * @param payloadBuffer data buffer.
	 * @return PVStructure instance, can be <code>null</code>.
	 */
	public final PVStructure deserializeStructureAndCreatePVStructure(ByteBuffer payloadBuffer, DeserializableControl control) {
		final Field field = deserialize(payloadBuffer, control);
		if (field == null)
			return null;
		return pvDataCreate.createPVStructure(null, (Structure)field);
	}
	
	/**
	 * Serialize status.
	 * TODO optimize duplicates
	 * @param buffer data buffer.
	 * @param control serializaiton control instance.
	 * @param status status to serialize.
	 */
	public final void serializeStatus(ByteBuffer buffer, SerializableControl control, Status status) {
		status.serialize(buffer, control);
	}

	/**
	 * Serialize status.
	 * TODO optimize duplicates
	 * @param buffer data buffer.
	 */
	public final Status deserializeStatus(ByteBuffer buffer, DeserializableControl control) {
		return statusCreate.deserializeStatus(buffer, control);
	}
}
