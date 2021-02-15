/*
 *
 */
package org.epics.pvaccess.server.test.helpers;

import org.epics.pvaccess.PVFactory;
import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarType;

/**
 * @author msekoranja
 *
 */
public class RPCTopStructure extends PVTopStructure {

    private static final PVDataCreate pvDataCreate = PVFactory.getPVDataCreate();
    private static final FieldCreate fieldCreate = PVFactory.getFieldCreate();

    public RPCTopStructure() {
		super((Field)null);
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.server.test.helpers.PVTopStructure#request(org.epics.pvdata.pv.PVStructure)
	 */
	@Override
	public PVStructure request(PVStructure pvArgument) {
		double a = pvArgument.getDoubleField("a").get();
		double b = pvArgument.getDoubleField("b").get();

		PVStructure result;
		{
	        Field[] fields = new Field[1];
	        fields[0] = fieldCreate.createScalar(ScalarType.pvDouble);
	        result = pvDataCreate.createPVStructure(fieldCreate.createStructure(new String[] { "c" } , fields)
	        );

		}

		result.getDoubleField("c").put(a+b);

		return result;
	}

}
