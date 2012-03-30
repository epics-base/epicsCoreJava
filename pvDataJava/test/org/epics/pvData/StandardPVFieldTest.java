/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData;

import junit.framework.TestCase;

import org.epics.pvData.factory.FieldFactory;
import org.epics.pvData.factory.PVDataFactory;
import org.epics.pvData.factory.StandardPVFieldFactory;
import org.epics.pvData.pv.Field;
import org.epics.pvData.pv.FieldCreate;
import org.epics.pvData.pv.PVDataCreate;
import org.epics.pvData.pv.PVField;
import org.epics.pvData.pv.PVString;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.ScalarType;
import org.epics.pvData.pv.Structure;
import org.epics.pvData.pv.*;



/**
 * JUnit test for pvAccess.
 * It also provides examples of how to use the pvAccess interfaces.
 * @author mrk
 *
 */
public class StandardPVFieldTest extends TestCase {
    
    private static final FieldCreate fieldCreate = FieldFactory.getFieldCreate();
    private static final PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
    private static final StandardPVField standardPVField = StandardPVFieldFactory.getStandardPVField();
	
	
    public static void testSimple() {     
        PVStructure pvStructure = standardPVField.scalar(null, "value",ScalarType.pvDouble,"alarm,timeStamp.display,control");
        System.out.println(pvStructure);
    }
    
}

