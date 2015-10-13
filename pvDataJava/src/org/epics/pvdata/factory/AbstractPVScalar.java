/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata.factory;

import org.epics.pvdata.pv.PVScalar;
import org.epics.pvdata.pv.Scalar;

/**
 * Abstract base class for a PVScalar.
 * A factory that implements PVScalar must extend this class.
 * 
 * @author mrk
 *
 */
public abstract class AbstractPVScalar extends AbstractPVField implements PVScalar {
    /**
     * Constructor.
     * @param scalar the ScalarType
     */
    protected AbstractPVScalar(Scalar scalar) {
        super(scalar);
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVScalar#getScalar()
     */
    public Scalar getScalar() {
        return (Scalar)super.getField();
    }
}
