/**
 * 
 */
package org.epics.ca.server.test.helpers;

import org.epics.ca.PVFactory;
import org.epics.pvData.pv.Field;
import org.epics.pvData.pv.FieldCreate;
import org.epics.pvData.pv.PVDataCreate;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.ScalarType;

/**
 * @author msekoranja
 *
 */
public class RPCTopStructure extends PVTopStructure {

    private static final PVDataCreate pvDataCreate = PVFactory.getPVDataCreate();
    private static final FieldCreate fieldCreate = PVFactory.getFieldCreate();

    public RPCTopStructure() {
		super(null);
	}

	/* (non-Javadoc)
	 * @see org.epics.ca.server.test.helpers.PVTopStructure#request(org.epics.pvData.pv.PVStructure)
	 */
	@Override
	public PVStructure request(PVStructure pvArgument) {
		double a = pvArgument.getDoubleField("a").get();
		double b = pvArgument.getDoubleField("b").get();
		
		PVStructure result;
		{
	        Field[] fields = new Field[1];
	        fields[0] = fieldCreate.createScalar(ScalarType.pvDouble);
	        result = pvDataCreate.createPVStructure(null,
	        		fieldCreate.createStructure(new String[] { "c" } , fields)
	        );
	        
		}
		
		result.getDoubleField("c").put(a+b);
		
		return result;
	}

}
