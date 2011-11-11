package org.epics.ca.server.test.helpers;

import org.epics.pvData.pv.PVBoolean;
import org.epics.pvData.pv.PVField;
import org.epics.pvData.pv.PVString;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.Scalar;
import org.epics.pvData.pv.ScalarType;
import org.epics.pvData.pv.Type;

public class PVRequestUtils {

    public static boolean getProcess(PVStructure pvRequest) {
    	PVField pvField = pvRequest.getSubField("record.process");
    	if(pvField==null || pvField.getField().getType()!=Type.scalar) return false;
    	Scalar scalar = (Scalar)pvField.getField();
    	if(scalar.getScalarType()==ScalarType.pvString) {
    		PVString pvString = (PVString)pvField;
    		return (pvString.get().equalsIgnoreCase("true")) ? true : false;
    	} else if(scalar.getScalarType()==ScalarType.pvBoolean) {
    		PVBoolean pvBoolean = (PVBoolean)pvField;
    		return pvBoolean.get();
    	}
    	return false;
    }

}
