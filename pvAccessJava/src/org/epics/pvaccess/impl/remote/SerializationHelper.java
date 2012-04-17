package org.epics.pvaccess.impl.remote;

import java.nio.ByteBuffer;

import org.epics.pvaccess.PVFactory;
import org.epics.pvdata.pv.DeserializableControl;
import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.SerializableControl;
import org.epics.pvdata.pv.Structure;

public class SerializationHelper {

	/**
	 * PVField factory.
	 */
	private static final PVDataCreate pvDataCreate = PVFactory.getPVDataCreate();

	/**
	 * Deserialize PVRequest.
	 * @param payloadBuffer data buffer.
	 * @return deserialized PVRequest, can be <code>null</code>.
	 */
	public static final PVStructure deserializePVRequest(ByteBuffer payloadBuffer, DeserializableControl control) {
		// for now ordinary structure, later can be changed
		return deserializeStructureFull(payloadBuffer, control);
	}

	/**
	 * Deserialize Structure and create PVStructure instance.
	 * @param payloadBuffer data buffer.
	 * @return PVStructure instance, can be <code>null</code>.
	 */
	public static final PVStructure deserializeStructureAndCreatePVStructure(ByteBuffer payloadBuffer, DeserializableControl control) {
		final Field field = control.cachedDeserialize(payloadBuffer);
		if (field == null)
			return null;
		return pvDataCreate.createPVStructure(null, (Structure)field);
	}

	/**
	 * Deserialize optional PVStructrue.
	 * @param payloadBuffer data buffer.
	 * @return deserialized PVStructure, can be <code>null</code>.
	 */
	public static final PVStructure deserializeStructureFull(ByteBuffer payloadBuffer, DeserializableControl control) {
	    PVStructure pvStructure = null;
	    final Field structureField = control.cachedDeserialize(payloadBuffer);
	    if (structureField != null)
	    {
	    	pvStructure = pvDataCreate.createPVStructure(null, (Structure)structureField);
	    	pvStructure.deserialize(payloadBuffer, control);
	    }
	    return pvStructure;
	}

	public final static void serializeNullField(ByteBuffer buffer, SerializableControl control)
	{
		control.ensureBuffer(1);
		buffer.put(IntrospectionRegistry.NULL_TYPE_CODE);
	}

	/**
	 * Serialize PVRequest.
	 * @param buffer data buffer.
	 */
	public static final void serializePVRequest(ByteBuffer buffer, SerializableControl control, PVStructure pvRequest) {
		// for now ordinary structure, later can be changed
		serializeStructureFull(buffer, control, pvRequest);
	}

	/**
	 * Serialize optional PVStructrue.
	 * @param buffer data buffer.
	 */
	public static final void serializeStructureFull(ByteBuffer buffer, SerializableControl control, PVStructure pvStructure) {
	
	    if (pvStructure == null)
	    	serializeNullField(buffer, control);
	    else {
		    control.cachedSerialize(pvStructure.getField(), buffer);
		    pvStructure.serialize(buffer, control);
	    }
	}

}
