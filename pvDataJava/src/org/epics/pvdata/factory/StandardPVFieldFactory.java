/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */

package org.epics.pvdata.factory;

import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVScalarArray;
import org.epics.pvdata.pv.PVStringArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.StandardField;
import org.epics.pvdata.pv.StandardPVField;
import org.epics.pvdata.pv.Structure;

/**
 * Generate data object for standard fields.
 * @author mrk
 *
 */
public final class  StandardPVFieldFactory {
	/**
	 * Get the single instance of PVStandardField.
     * @return The interface for PVStandardField.
	 */
	public static synchronized StandardPVField getStandardPVField()
	{
		if(impl==null) {
			impl = new StandardPVFieldImpl();
		}
		return impl;
	}
	private static StandardPVFieldImpl impl = null;
	private static PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
	private static StandardField standardField = StandardFieldFactory.getStandardField();
	private static final class StandardPVFieldImpl implements StandardPVField
	{
		StandardPVFieldImpl(){}

		/* (non-Javadoc)
		 * @see org.epics.pvdata.pv.StandardPVField#scalar(org.epics.pvdata.pv.ScalarType, java.lang.String)
		 */
		@Override
		public PVStructure scalar(ScalarType scalarType,String properties)
		{
		    Structure field = standardField.scalar(scalarType,properties);
		    PVStructure pvStructure = pvDataCreate.createPVStructure(field);
		    return pvStructure;
		}
		/* (non-Javadoc)
		 * @see org.epics.pvdata.pv.StandardPVField#scalarArray(org.epics.pvdata.pv.ScalarType, java.lang.String)
		 */
		@Override
		public PVStructure scalarArray(ScalarType elementType, String properties)
		{
		    Structure field = standardField.scalarArray(elementType,properties);
            PVStructure pvStructure = pvDataCreate.createPVStructure(field);
            return pvStructure;
		}
		/* (non-Javadoc)
		 * @see org.epics.pvdata.pv.StandardPVField#structureArray(org.epics.pvdata.pv.Structure, java.lang.String)
		 */
		@Override
		public PVStructure structureArray(Structure structure,String properties)
		{
		    Structure field = standardField.structureArray(structure,properties);
            PVStructure pvStructure = pvDataCreate.createPVStructure(field);
            return pvStructure;
		}
		/* (non-Javadoc)
		 * @see org.epics.pvdata.pv.StandardPVField#enumerated(java.lang.String[])
		 */
		@Override
		public PVStructure enumerated(String[] choices)
		{
		    Structure field = standardField.enumerated();
		    PVStructure pvStructure = pvDataCreate.createPVStructure(field);
		    PVScalarArray pvScalarArray = pvStructure.getScalarArrayField("choices",ScalarType.pvString);
		    if(pvScalarArray==null) {
		        throw new IllegalStateException("logic error");
		    }
		    PVStringArray pvChoices = (PVStringArray)pvScalarArray;
		    pvChoices.put(0,choices.length,choices,0);
		    pvChoices.setImmutable();
		    return pvStructure;
		}
		/* (non-Javadoc)
		 * @see org.epics.pvdata.pv.StandardPVField#enumerated(java.lang.String[], java.lang.String)
		 */
		@Override
		public PVStructure enumerated(String[] choices,String properties)
		{
		    Structure field = standardField.enumerated(properties);
		    PVStructure pvStructure =  pvDataCreate.createPVStructure(field);
		    PVScalarArray pvScalarArray = pvStructure.getScalarArrayField("value.choices",ScalarType.pvString);
		    if(pvScalarArray==null) {
		        throw new IllegalStateException("logic error");
		    }
		    PVStringArray pvChoices = (PVStringArray)pvScalarArray;
		    pvChoices.put(0,choices.length,choices,0);
		    pvChoices.setImmutable();
		    return pvStructure;
		}
	}
}


