package org.epics.pvaccess.impl.remote;

import java.nio.ByteBuffer;

import org.epics.pvaccess.PVFactory;
import org.epics.pvdata.factory.ConvertFactory;
import org.epics.pvdata.misc.BitSet;
import org.epics.pvdata.pv.Convert;
import org.epics.pvdata.pv.DeserializableControl;
import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVField;
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
	 * @param control deserialization control.
	 * @return PVStructure instance, can be <code>null</code>.
	 */
	public static final PVStructure deserializeStructureAndCreatePVStructure(ByteBuffer payloadBuffer, DeserializableControl control) {
		return deserializeStructureAndCreatePVStructure(payloadBuffer, control, null);
	}

	/**
	 * Deserialize Structure and create PVStructure instance, if necessary.
	 * @param payloadBuffer data buffer.
	 * @param control deserialization control.
	 * @param existingStructure if deserialized Field matches <code>existingStrcuture</code> Field, then
	 * 			<code>existingStructure</code> instance is returned. <code>null</code> value is allowed.
	 * @return PVStructure instance, can be <code>null</code>.
	 */
	public static final PVStructure deserializeStructureAndCreatePVStructure(ByteBuffer payloadBuffer, DeserializableControl control, PVStructure existingStructure) {
		final Field field = control.cachedDeserialize(payloadBuffer);
		if (field == null)
			return null;
		// reuse existing structure case
		if (existingStructure != null && field.equals(existingStructure.getField()))
			return existingStructure;
		else
			return pvDataCreate.createPVStructure((Structure)field);
	}
	
	/**
	 * Deserialize optional PVStructrue.
	 * @param payloadBuffer data buffer.
	 * @return deserialized PVStructure, can be <code>null</code>.
	 */
	public static final PVStructure deserializeStructureFull(ByteBuffer payloadBuffer, DeserializableControl control) {
		return (PVStructure)deserializeFull(payloadBuffer, control);
	}

	/**
	 * Deserialize optional PVField.
	 * @param payloadBuffer data buffer.
	 * @return deserialized PVField, can be <code>null</code>.
	 */
	public static final PVField deserializeFull(ByteBuffer payloadBuffer, DeserializableControl control) {
	    PVField pvField = null;
	    final Field field = control.cachedDeserialize(payloadBuffer);
	    if (field != null)
	    {
	    	pvField = pvDataCreate.createPVField(field);
	    	pvField.deserialize(payloadBuffer, control);
	    }
	    return pvField;
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
		serializeFull(buffer, control, pvStructure);
	}

	/**
	 * Serialize optional PVField.
	 * @param buffer data buffer.
	 */
	public static final void serializeFull(ByteBuffer buffer, SerializableControl control, PVField pvField) {
	
	    if (pvField == null)
	    	serializeNullField(buffer, control);
	    else {
		    control.cachedSerialize(pvField.getField(), buffer);
		    pvField.serialize(buffer, control);
	    }
	}
	
	static Convert convert = ConvertFactory.getConvert();
	public static void copyUnchecked(PVField from, PVField to)
	{
		// TODO implement
		convert.copy(from, to);
	}
	
	public static void copyStructureUnchecked(PVStructure from, PVStructure to) {
		
		if (from == to)
			return;
			
        PVField[] fromPVFields = from.getPVFields();
        PVField[] toPVFields = to.getPVFields();
        
        for (int i = 0; i < fromPVFields.length; i++)
        {
        	final PVField pvField = fromPVFields[i];
            int numberFields = pvField.getNumberFields();
            
            // serialize field or fields
            if (numberFields == 1)
            	copyUnchecked(pvField, toPVFields[i]);
            else
            	copyStructureUnchecked((PVStructure)pvField, (PVStructure)toPVFields[i]);
        }
	}

	public static void partialCopy(PVStructure from, PVStructure to, BitSet maskBitSet) {
		partialCopy(from, to, maskBitSet, false);
	}
	
	public static void partialCopy(PVStructure from, PVStructure to, BitSet maskBitSet, boolean inverse) {
		
		if (from == to)
			return;
			
        int offset = from.getFieldOffset();
        int numberFields = from.getNumberFields();
        int next = inverse ?
        		maskBitSet.nextClearBit(offset) :
        		maskBitSet.nextSetBit(offset);
        
        // no more changes or no changes in this structure
        if (next<0 || next>=offset+numberFields) return;

        // entire structure
        if(offset==next) {
        	copyStructureUnchecked(from, to);
        	return;
        }
        
        PVField[] fromPVFields = from.getPVFields();
        PVField[] toPVFields = to.getPVFields();
        
        for (int i = 0; i < fromPVFields.length; i++)
        {
        	final PVField pvField = fromPVFields[i];
            offset = pvField.getFieldOffset();
            numberFields = pvField.getNumberFields();
            next = inverse ?
            		maskBitSet.nextClearBit(offset) :
            		maskBitSet.nextSetBit(offset);
            
            // no more changes
            if (next<0) return;
            //  no change in this pvField
            if (next>=offset+numberFields) continue;
            
            // serialize field or fields
            if (numberFields == 1)
            	copyUnchecked(pvField, toPVFields[i]);
            else
            	partialCopy((PVStructure)pvField, (PVStructure)toPVFields[i], maskBitSet, inverse);
        }
	}
	
}
