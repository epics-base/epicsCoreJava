/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */

package org.epics.pvData.factory;

import org.epics.pvData.pv.FieldCreate;
import org.epics.pvData.pv.PVDataCreate;
import org.epics.pvData.pv.PVScalar;
import org.epics.pvData.pv.PVScalarArray;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.PVStructureArray;
import org.epics.pvData.pv.ScalarType;
import org.epics.pvData.pv.StandardPVField;
import org.epics.pvData.pv.Structure;

/**
 * @author mrk
 *
 */
public final class  StandardPVFieldFactory {
	public static synchronized StandardPVField getStandardPVField()
	{
		if(impl==null) {
			impl = new StandardPVFieldImpl();
		}
		return impl;
	}
	private static StandardPVFieldImpl impl = null;
	private static PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
	private static FieldCreate fieldCreate = FieldFactory.getFieldCreate();
	private static final class StandardPVFieldImpl implements StandardPVField
	{

		StandardPVFieldImpl(){}
		@Override
		public PVScalar scalar(PVStructure parent,String fieldName,ScalarType type)
		{
            return null;
		}
		@Override
		public PVStructure scalar(PVStructure parent,String fieldName,ScalarType type,String properties)
		{
			return null;
		}
		@Override
		public PVScalarArray scalarArray(PVStructure parent,String fieldName,ScalarType elementType)
		{
			return null;
		}
		@Override
		public PVStructure scalarArray(PVStructure parent,String fieldName,ScalarType elementType, String properties)
		{
			return null;
		}
		@Override
		public PVStructureArray structureArray(PVStructure parent,String fieldName,Structure structure)
		{
			return null;
		}
		@Override
		public PVStructure structureArray(PVStructure parent,String fieldName,Structure structure,String properties)
		{
			return null;
		}
		@Override
		public PVStructure enumerated(PVStructure parent,String fieldName,String[] choices)
		{
			return null;
		}
		@Override
		public PVStructure enumerated(PVStructure parent,String fieldName,String[] choices,String properties)
		{
			return null;
		}
		@Override
		public PVScalar scalarValue(PVStructure parent,ScalarType type)
		{
			return null;
		}
		@Override
		public PVStructure scalarValue(PVStructure parent,ScalarType type,String properties)
		{
			return null;
		}
		@Override
		public PVScalarArray scalarArrayValue(PVStructure parent,ScalarType elementType)
		{
			return null;
		}
		@Override
		public PVStructure scalarArrayValue(PVStructure parent,ScalarType elementType, String properties)
		{
			return null;
		}
		@Override
		public PVStructureArray structureArrayValue(PVStructure parent,Structure structure)
		{
			return null;
		}
		@Override
		public PVStructure structureArrayValue(PVStructure parent,Structure structure,String properties)
		{
			return null;
		}
		@Override
		public PVStructure enumeratedValue(PVStructure parent,String[] choices)
		{
			return null;
		}
		@Override
		public PVStructure enumeratedValue(PVStructure parent,String[] choices,String properties)
		{
			return null;
		}
		@Override
		public PVStructure alarm(PVStructure parent)
		{
			return null;
		}
		@Override
		public PVStructure timeStamp(PVStructure parent)
		{
			return null;
		}
		@Override
		public PVStructure display(PVStructure parent)
		{
			return null;
		}
		@Override
		public PVStructure control(PVStructure parent)
		{
			return null;
		}
		@Override
		public PVStructure booleanAlarm(PVStructure parent)
		{
			return null;
		}
		@Override
		public PVStructure byteAlarm(PVStructure parent)
		{
			return null;
		}
		@Override
		public PVStructure shortAlarm(PVStructure parent)
		{
			return null;
		}
		@Override
		public PVStructure intAlarm(PVStructure parent)
		{
			return null;
		}
		@Override
		public PVStructure longAlarm(PVStructure parent)
		{
			return null;
		}
		@Override
		public PVStructure floatAlarm(PVStructure parent)
		{
			return null;
		}
		@Override
		public PVStructure doubleAlarm(PVStructure parent)
		{
			return null;
		}
		@Override
		public PVStructure enumeratedAlarm(PVStructure parent)
		{
			return null;
		}
		@Override
		public PVStructure powerSupply(PVStructure parent)
		{
			return null;
		}
	}
}


