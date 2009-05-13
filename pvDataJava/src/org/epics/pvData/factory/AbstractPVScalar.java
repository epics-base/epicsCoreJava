/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.factory;

import org.epics.pvData.pv.PVScalar;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.Scalar;

/**
 * @author mrk
 *
 */
public abstract class AbstractPVScalar extends AbstractPVField implements PVScalar {
    /**
     * Constructor.
     * @param parent The parent.
     * @param scalar The ScalarType.
     */
    protected AbstractPVScalar(PVStructure parent, Scalar scalar) {
        super(parent,scalar);
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVScalar#getScalar()
     */
    public Scalar getScalar() {
        return (Scalar)super.getField();
    }
}
