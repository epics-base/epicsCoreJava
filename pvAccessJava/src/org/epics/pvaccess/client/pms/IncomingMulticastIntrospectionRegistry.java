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

package org.epics.pvaccess.client.pms;

import java.nio.ByteBuffer;

import org.epics.pvaccess.PVFactory;
import org.epics.pvaccess.impl.remote.IntrospectionRegistry;
import org.epics.pvaccess.util.ShortHashMap;
import org.epics.pvdata.pv.DeserializableControl;
import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.FieldCreate;


/**
 * Incoming (receive) introspection registry.
 * Registry is used to cache introspection interfaces to minimize network traffic.
 * This class is not thread safe (optimized to be used only by one thread).
 * 
 * Always deserializes with id and full introspection data (used by one-to-many transports).
 *
 * @author msekoranja
 */
public final class IncomingMulticastIntrospectionRegistry {

	static class FieldEntry
	{
		private final Field field;
		private final int serializationSize;
		
		public FieldEntry(Field field, int serializationSize) {
			this.field = field;
			this.serializationSize = serializationSize;
		}

		public FieldEntry(Field field) {
			this.field = field;
			this.serializationSize = -1;
		}

		public Field getField() {
			return field;
		}
		public int getSerializationSize() {
			return serializationSize;
		}
		
	}
	
	// TODO generics
	protected final ShortHashMap registry = new ShortHashMap();
	
	public IncomingMulticastIntrospectionRegistry()
	{
		reset();
	}
	
	/**
	 * Reset registry, i.e. must be done when transport is changed (server restarted).
	 */
	public void reset()
	{
		registry.clear();
	}
	
	/**
	 * Get introspection interface for given ID.
	 * @param id
	 * @return <code>Field</code> instance for given ID.
	 */
	public FieldEntry getIntrospectionInterface(short id)
	{
		return (FieldEntry)registry.get(id);
	}

	/**
	 * Register introspection interface with given ID. 
	 * @param id
	 * @param fieldEntry
	 */
	public void registerIntrospectionInterface(short id, FieldEntry fieldEntry)
	{
		registry.put(id, fieldEntry);
	}

	static final FieldCreate fieldCreate = PVFactory.getFieldCreate();
	
	public final Field deserialize(ByteBuffer buffer, DeserializableControl control) {

		control.ensureData(1);
		int pos = buffer.position();
		final byte typeCode = buffer.get();
		
		if (typeCode == IntrospectionRegistry.NULL_TYPE_CODE)
		{
			return null;
		}
		// could also be a mask
		else if (typeCode == IntrospectionRegistry.FULL_WITH_ID_TYPE_CODE)
		{
			control.ensureData(Short.SIZE/Byte.SIZE);
			final short key = buffer.getShort();
			// first check if we already have the field registered
			FieldEntry fieldEntry = getIntrospectionInterface(key);
			if (fieldEntry != null)
			{
				// skip serialization data
				int toSkip = fieldEntry.getSerializationSize();
				control.ensureData(toSkip);
				buffer.position(buffer.position() + toSkip);
				return fieldEntry.getField();
			}
				
			// .. if not deserialize and store
			int startPos = buffer.position();
			Field field = fieldCreate.deserialize(buffer, control);
			int serializationSize = (buffer.position() - startPos);

			registerIntrospectionInterface(key, new FieldEntry(field, serializationSize));

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
