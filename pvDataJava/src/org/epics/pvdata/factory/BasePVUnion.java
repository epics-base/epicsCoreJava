/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.factory;

import java.nio.ByteBuffer;

import org.epics.pvdata.misc.SerializeHelper;
import org.epics.pvdata.pv.DeserializableControl;
import org.epics.pvdata.pv.Field;
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

    private final Union union;
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
        variant = union.isVariant();
    }

	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.PVUnion#getUnion()
	 */
	public Union getUnion() {
		return union;
	}

	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.PVUnion#get()
	 */
	public PVField get() {
		return value;
	}

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVUnion#get(java.lang.Class)
     */
	public <T extends PVField> T get(Class<T> c)
	{
		if (c.isInstance(value))
			return c.cast(value);
		else
			return null;
	}

	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.PVUnion#getSelectedIndex()
	 */
	public int getSelectedIndex() {
		return selector;
	}

	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.PVUnion#getSelectedFieldName()
	 */
	public String getSelectedFieldName() {
		// no name for undefined and for variant unions
		if (selector == UNDEFINED_INDEX)
			return null;
		else
			return union.getFieldName(selector);
	}

	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.PVUnion#select(int)
	 */
	public PVField select(int index) {
		// no change
		if (selector == index)
			return value;

		if (index == UNDEFINED_INDEX)
		{
			selector = UNDEFINED_INDEX;
			value = null;
			return null;
		}
		else if (variant)
			throw new IllegalArgumentException("index out of bounds");
		else if (index < 0 || index > union.getFields().length)
			throw new IllegalArgumentException("index out of bounds");

		Field field = union.getField(index);
		selector = index;
		value = pvDataCreate.createPVField(field);

		return value;
	}

	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.PVUnion#select(java.lang.String)
	 */
	public PVField select(String fieldName) {
		int index = variant ? -1 : union.getFieldIndex(fieldName);
		if (index == -1)
			throw new IllegalArgumentException("no such fieldName");

		return select(index);
	}


	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.PVUnion#select(java.lang.Class, int)
	 */
	public <T extends PVField> T select(Class<T> c, int index) {
		PVField pv = select(index);
		if (c.isInstance(pv))
			return c.cast(pv);
		else
			return null;
	}

	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.PVUnion#select(java.lang.Class, java.lang.String)
	 */
	public <T extends PVField> T select(Class<T> c, String fieldName) {
		PVField pv = select(fieldName);
		if (c.isInstance(pv))
			return c.cast(pv);
		else
			return null;
	}

	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.PVUnion#set(org.epics.pvdata.pv.PVField)
	 */
	public void set(PVField value) {
		set(selector, value);
	}

	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.PVUnion#set(int, org.epics.pvdata.pv.PVField)
	 */
	public void set(int index, PVField value) {
		if (variant && index != UNDEFINED_INDEX)
			throw new IllegalArgumentException("index out of bounds");
		else if (!variant)
		{
			if (index == UNDEFINED_INDEX)
			{
				// for undefined index we accept only null values
				if (value != null)
					throw new IllegalArgumentException("non-null value for index == UNDEFINED_INDEX");
			}
			else if (index < 0 || index > union.getFields().length)
				throw new IllegalArgumentException("index out of bounds");

			if ( value == null ) {
				throw new IllegalArgumentException("PVField is null");
			}

			// value type must match
			if (!value.getField().equals(union.getField(index)))
				throw new IllegalArgumentException("selected field and its introspection data do not match");
		}

		this.selector = index;
		this.value = value;
		super.postPut();
	}

	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.PVUnion#set(java.lang.String, org.epics.pvdata.pv.PVField)
	 */
	public void set(String fieldName, PVField value) {
		int index = variant ? -1 : union.getFieldIndex(fieldName);
		if (index == -1)
			throw new IllegalArgumentException("no such fieldName");

		set(index, value);
	}

	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.Serializable#serialize(java.nio.ByteBuffer, org.epics.pvdata.pv.SerializableControl)
	 */
	public void serialize(ByteBuffer buffer, SerializableControl flusher) {
		if (variant)
		{
			// write introspection data
			if (value == null)
			{
				flusher.ensureBuffer(1);
				buffer.put((byte)-1);
			}
			else
			{
				flusher.cachedSerialize(value.getField(), buffer);
				value.serialize(buffer, flusher);
			}
		}
		else
		{
			// write selector value
			SerializeHelper.writeSize(selector, buffer, flusher);
			// write value, no value for UNDEFINED_INDEX
			if (selector != UNDEFINED_INDEX)
				value.serialize(buffer, flusher);

		}
	}
	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.Serializable#deserialize(java.nio.ByteBuffer, org.epics.pvdata.pv.DeserializableControl)
	 */
	public void deserialize(ByteBuffer buffer, DeserializableControl control) {
		if (variant)
		{
			Field field = control.cachedDeserialize(buffer);
			if (field != null)
			{
				// try to reuse existing field instance
				if (value == null || !field.equals(value.getField()))
					value = pvDataCreate.createPVField(field);
				value.deserialize(buffer, control);
			}
			else
				value = null;
		}
		else
		{
			final int previousSelector = selector;
			selector = SerializeHelper.readSize(buffer, control);
			if (selector != UNDEFINED_INDEX)
			{
				if (selector != previousSelector)
				{
					Field field = union.getField(selector);
					// try to reuse existing field instance
					if (value == null || !field.equals(value.getField()))
						value = pvDataCreate.createPVField(field);
				}
				value.deserialize(buffer, control);
			}
			else
				value = null;
		}
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PVUnion) {
			PVUnion b = (PVUnion)obj;
			if (union.equals(b.getUnion()))
			{
				if (union.isVariant())
				{
					if (value == null)
						return b.get() == null;
					else
						return value.equals(b.get());
				}
				else
				{
					if (selector == b.getSelectedIndex())
					{
						if (selector == UNDEFINED_INDEX || value.equals(b.get()))
							return true;
						else
							return false;
					}
					else
						return false;
				}
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
