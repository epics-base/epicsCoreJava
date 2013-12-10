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
import java.util.HashMap;
import java.util.Map;

import org.epics.pvaccess.impl.remote.IntrospectionRegistry;
import org.epics.pvaccess.impl.remote.SerializationHelper;
import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.SerializableControl;
import org.epics.pvdata.pv.Type;


/**
 * Outgoing (sending) introspection registry.
 * Registry is used to cache introspection interfaces to minimize network traffic.
 * This class is not thread safe (optimized to be used only by one thread).
 * 
 * Always serializes with id and full introspection data (used by one-to-many transports).
 *
 * @author msekoranja
 */
public final class OutgoingMulticastIntrospectionRegistry {

	// TODO avoid short unboxing
	protected final Map<Field, Short> registry = new HashMap<Field, Short>();
	protected short pointer;
	
	
	public OutgoingMulticastIntrospectionRegistry()
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
	 * Register introspection interface and get it's ID.
	 * If it is already registered only preassigned ID is returned.
	 * @param field
	 * @return id of given <code>Field</code>
	 */
	public short registerIntrospectionInterface(Field field)
	{
		Short existing = registry.get(field);
		if (existing != null)
			return existing;
		else
		{
			final short key = pointer++;
			registry.put(field, key);
			return key;
		}
	}
	
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
				short key = registerIntrospectionInterface(field);
				control.ensureBuffer(3);
				buffer.put(IntrospectionRegistry.FULL_WITH_ID_TYPE_CODE);
				buffer.putShort(key);
			}
			
			field.serialize(buffer, control);
		}
	}

}
