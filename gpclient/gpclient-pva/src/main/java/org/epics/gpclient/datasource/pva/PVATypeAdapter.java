/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient.datasource.pva;

import org.epics.gpclient.ReadCollector;
import org.epics.gpclient.datasource.DataSourceTypeAdapter;
import org.epics.pvdata.pv.*;

import java.util.Arrays;

/**
 * Type adapter for PVA data source. Will match a channel based on the value
 * type provided and the array flag. Will match the cache based on the type class.
 *
 * @author msekoranja
 */
abstract class PVATypeAdapter implements DataSourceTypeAdapter<PVAConnectionPayload, PVStructure> {

	// e.g. VDouble.class
    private final Class<?> typeClass;

    // PVStructure requirements
    private final String[] ntIds;
    private final Field[] valueFieldTypes;

    /**
     * Creates a new type adapter.
     *
     * @param typeClass the java type this adapter will create
     * @param ntIds array of IDs this adapter is able convert, <code>null</code> allowed
     */
    public PVATypeAdapter(Class<?> typeClass, String[] ntIds) {
    	this(typeClass, ntIds, (Field[])null);
    }

    /**
     * Creates a new type adapter.
     *
     * @param typeClass the java type this adapter will create
     * @param ntIds array of IDs this adapter is able convert, <code>null</code> allowed
     * @param fieldType <code>Field</code> instance this adapter is able convert
     */
    public PVATypeAdapter(Class<?> typeClass, String[] ntIds, Field fieldType) {
    	this(typeClass, ntIds, new Field[] { fieldType });
    }

    /**
     * Creates a new type adapter.
     *
     * @param typeClass the java type this adapter will create
     * @param ntIds array of IDs this adapter is able convert, <code>null</code> allowed
     * @param fieldTypes <code>Field</code> instances this adapter is able convert, <code>null</code> allowed
     */
    public PVATypeAdapter(Class<?> typeClass, String[] ntIds, Field[] fieldTypes) {
        this.typeClass = typeClass;
        this.ntIds = ntIds;
        this.valueFieldTypes = fieldTypes;
    }

    public boolean match(Structure structure) {
        // If one of the IDs does not match, no match
        if (ntIds != null)
        {
        	boolean match = false;
        	String ntId = structure.getID();
        	// TODO "structure" ID ??
        	for (String id : ntIds)
        		if (ntId.startsWith(id))	// ignore minor version
        		{
        			match = true;
        			break;
        		}

        	if (!match)
        		return false;
        }

        // If the type of the channel does not match, no match
        if (valueFieldTypes != null)
        {
        	boolean match = false;
        	// we assume Structure here
        	Field channelValueType = structure.getField("value");
        	if (channelValueType != null)
    		{
            	for (Field vf : valueFieldTypes)
            		if (channelValueType.equals(vf))
            		{
            			match = true;
            			break;
            		}

            	if (!match)
            		return false;
    		}
        }

        // Everything matches
        return true;
    }

    public boolean match(ReadCollector<?, ?> cache, PVAConnectionPayload connection) {

    	// If the generated type can't be put in the cache, no match
        if (!cache.getType().isAssignableFrom(typeClass))
            return false;

        // If the channel type is not available, no match
        if (connection.channelType == null)
            return false;

        // If one of the IDs does not match, no match
        if (ntIds != null)
        {
        	boolean match = false;
        	String ntId = connection.channelType.getID();
        	// TODO "structure" ID ??
        	for (String id : ntIds)
        		if (ntId.startsWith(id))	// ignore minor version
        		{
        			match = true;
        			break;
        		}

        	if (!match)
        		return false;
        }

        // If the type of the channel does not match, no match
        if (valueFieldTypes != null)
        {
        	boolean match = false;
        	// we assume Structure here
        	Field channelType = connection.channelType;
        	Field channelValueType = (channelType.getType() == Type.structure) ?
        			((Structure)channelType).getField("value") : channelType;
        	if (channelValueType != null)
    		{
            	for (Field vf : valueFieldTypes)
            		if (channelValueType.equals(vf))
            		{
            			match = true;
            			break;
            		}

            	if (!match)
            		return false;
    		}
        }

        // Everything matches
        return true;
    }

    public Object getSubscriptionParameter(ReadCollector<?, ?> cache, PVAConnectionPayload connection) {
        throw new UnsupportedOperationException("Not implemented: PVAChannelHandler is multiplexed, will not use this method");
    }

    @SuppressWarnings("unchecked")
    public void updateCache(@SuppressWarnings("rawtypes") ReadCollector cache, PVAConnectionPayload connection, PVStructure message) {

    	PVField valueField = null;
    	String extractFieldName = connection.extractFieldName;
    	if (extractFieldName != null)
    	{
    		if (connection.channelType.getType() == Type.structure)
    			message = message.getStructureField(extractFieldName);
    		else
    			// this avoids problem when scalars/scalar arrays needs to be passed as PVStructure message
    			valueField = message.getSubField(extractFieldName);

    	}

        Object value = createValue(message, valueField, !connection.connected);
        cache.updateValue(value);
    }

    /**
     * Given the value create the new value.
     *
     * @param message the value taken from the monitor
     * @param valueField the value field data, optional
     * @param disconnected true if the value should report the channel is currently disconnected
     * @return the new value
     */
    public abstract Object createValue(PVStructure message, PVField valueField, boolean disconnected);

	@Override
	public String toString() {
		return "PVATypeAdapter [typeClass=" + typeClass + ", ntIds="
				+ Arrays.toString(ntIds) + ", valueFieldTypes="
				+ Arrays.toString(valueFieldTypes) + "]";
	}

}
