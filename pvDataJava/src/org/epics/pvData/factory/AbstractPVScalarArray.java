/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.factory;

import org.epics.pvData.pv.PVScalarArray;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.ScalarArray;

/**
 * Abstract base class for any PVArray field.
 * Any code that implements a PVArray field for an IOC database should extend this class.
 * @author mrk
 *
 */
public abstract class AbstractPVScalarArray extends AbstractPVArray implements PVScalarArray{
	/**Constructor that derived classes must call.
	 * @param parent The parent
	 * @param array The reflection interface.
	 */
	protected AbstractPVScalarArray(PVStructure parent,ScalarArray array) {
        super(parent,array);
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVArray#getArray()
     */
    @Override
    public ScalarArray getScalarArray() {
        return (ScalarArray)getField();
    }
    
	
}
