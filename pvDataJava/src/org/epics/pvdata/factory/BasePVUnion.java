/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata.factory;

import java.nio.ByteBuffer;

import org.epics.pvdata.misc.SerializeHelper;
import org.epics.pvdata.pv.DeserializableControl;
import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVUnion;
import org.epics.pvdata.pv.SerializableControl;
import org.epics.pvdata.pv.Union;

/**
 * Base class for a PVUnion.
 * @author mse
 */
public class BasePVUnion extends AbstractPVField implements PVUnion
{
    private static final PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
    private static final FieldCreate fieldCreate = FieldFactory.getFieldCreate();

    private final Union union;
    // TODO if not initialized?!!!
	private int selector = UNDEFINED_INDEX;
	private PVField value = null;
	private final boolean variant;
    /**
     * Constructor.
     * @param union the reflection interface for the PVUnion data.
     */
    public BasePVUnion(Union union) {
        super(union);
        this.union = union;
        variant = (union.getFields().length == 0);
    }

	@Override
	public Union getUnion() {
		return union;
	}

	@Override
	public PVField get() {
		return value;
	}

	@Override
	public int getSelectedIndex() {
		return selector;
	}

	@Override
	public String getSelectedFieldName() {
		return union.getFieldName(selector);
	}

	@Override
	public PVField select(int index) {
		Field field = union.getField(index);
		
		this.selector = index;
		
		// different introspection interface -> create new instance of PVField
		if (value == null || value.getField().equals(field))
			value = pvDataCreate.createPVField(field);

		return value;
	}

	@Override
	public PVField select(String fieldName) {
		return select(union.getFieldIndex(fieldName));
	}

	@Override
	public void put(int index, PVField value) {
		// TODO variant 
		if (!variant && !value.getField().equals(union.getField(index)))
			throw new IllegalArgumentException("selected field and its introspection data do not match");
		this.selector = index;
		this.value = value;
	}

	@Override
	public void put(String fieldName, PVField value) {
		put(union.getFieldIndex(fieldName), value);
	}

	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.Serializable#serialize(java.nio.ByteBuffer, org.epics.pvdata.pv.SerializableControl)
	 */
	public void serialize(ByteBuffer buffer, SerializableControl flusher) {
		if (variant)
		{
			flusher.cachedSerialize(value.getField(), buffer);
		}
		else
		{
			SerializeHelper.writeSize(selector, buffer, flusher);
		}
		value.serialize(buffer, flusher);
	}
	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.Serializable#deserialize(java.nio.ByteBuffer, org.epics.pvdata.pv.DeserializableControl)
	 */
	public void deserialize(ByteBuffer buffer, DeserializableControl control) {
		Field field;
		if (variant)
		{
			field = fieldCreate.deserialize(buffer, control);
		}
		else
		{
			// TODO if not initialized?!!!
			selector = SerializeHelper.readSize(buffer, control);
			field = union.getField(selector);
		}
		
		// different introspection interface -> create new instance of PVField
		if (value == null || value.getField().equals(field))
			value = pvDataCreate.createPVField(field);
		
		value.deserialize(buffer, control);
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		// TODO anything else?
		if (obj instanceof PVUnion) {
			PVUnion b = (PVUnion)obj;
			if (selector == b.getSelectedIndex())
			{
				if (value.equals(b.get()))
					return true;
				else
					return false;
			}
			else
				return false;
		}
		else
			return false;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int PRIME = 31;
		return selector + PRIME * value.hashCode();
	}
}
