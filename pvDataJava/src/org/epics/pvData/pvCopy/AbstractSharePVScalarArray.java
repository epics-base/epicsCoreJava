/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.pvCopy;

import org.epics.pvData.pv.PVArray;
import org.epics.pvData.pv.PVScalarArray;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.ScalarArray;


/**
 * Base class for implementing PVBooleanArray.
 * @author mrk
 *
 */
/**
 * @author mrk
 *
 */
public abstract class AbstractSharePVScalarArray extends AbstractSharePVArray implements PVScalarArray
{
    /**
     * Constructor.
     * @param parent The parent.
     * @param array The Introspection interface.
     */
    protected AbstractSharePVScalarArray(PVStructure parent,PVArray pvShare)
    {
        super(parent,pvShare);
    }
	@Override
	public ScalarArray getScalarArray() {
		// TODO Auto-generated method stub
		return (ScalarArray)super.getField();
	}        
    
}