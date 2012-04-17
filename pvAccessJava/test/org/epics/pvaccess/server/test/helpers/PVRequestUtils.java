package org.epics.pvaccess.server.test.helpers;

import org.epics.pvdata.pv.PVBoolean;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.Scalar;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Type;

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
