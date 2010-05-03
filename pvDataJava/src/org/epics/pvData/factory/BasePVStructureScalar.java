/**
 * 
 */
package org.epics.pvData.factory;

import java.nio.ByteBuffer;

import org.epics.pvData.pv.DeserializableControl;
import org.epics.pvData.pv.PVDataCreate;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.PVStructureScalar;
import org.epics.pvData.pv.SerializableControl;
import org.epics.pvData.pv.StructureScalar;

/**
 * Base class for BasePVStructureScalar.
 * It provides a complete implementation but can be extended.
 * @author mrk
 *
 */
public class BasePVStructureScalar extends AbstractPVScalar implements PVStructureScalar
{
	private static final PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
	protected StructureScalar structureScalar;
	protected PVStructure pvStructure;

	public BasePVStructureScalar(PVStructure parent,StructureScalar structureScalar) {
		super(parent,structureScalar);
		this.structureScalar = structureScalar;
		this.pvStructure = pvDataCreate.createPVStructure(null, structureScalar.getStructure());
	}        
	/* (non-Javadoc)
	 * @see org.epics.pvData.pv.PVStructureScalar#getPVStructure()
	 */
	@Override
	public PVStructure getPVStructure() {
		return pvStructure;
	}
	/* (non-Javadoc)
	 * @see org.epics.pvData.pv.PVStructureScalar#getStructureScalar()
	 */
	@Override
	public StructureScalar getStructureScalar() {
		return structureScalar;
	}
	/* (non-Javadoc)
	 * @see org.epics.pvData.pv.PVStructureScalar#put()
	 */
	@Override
	public void put() {
		super.postPut();
	}
	/* (non-Javadoc)
	 * @see org.epics.pvData.factory.AbstractPVField#toString(int)
	 */
	@Override
	public String toString(int indentLevel) {
		return convert.getString(this, indentLevel)
		+ super.toString(indentLevel);
	}
	/* (non-Javadoc)
	 * @see org.epics.pvData.pv.Serializable#serialize(java.nio.ByteBuffer, org.epics.pvData.pv.SerializableControl)
	 */
	@Override
	public void serialize(ByteBuffer buffer, SerializableControl flusher) {
		pvStructure.serialize(buffer, flusher);
	}
	/* (non-Javadoc)
	 * @see org.epics.pvData.pv.Serializable#deserialize(java.nio.ByteBuffer)
	 */
	@Override
	public void deserialize(ByteBuffer buffer, DeserializableControl control) {
		pvStructure.deserialize(buffer, control);
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		// TODO anything else?
		if (obj instanceof PVStructureScalar) {
			PVStructure other = ((PVStructureScalar)obj).getPVStructure();
			if(pvStructure.equals(other)) return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return pvStructure.hashCode();
	}
}
