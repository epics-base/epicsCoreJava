/*
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.ca;


import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBRType;

import org.epics.pvdata.misc.BitSet;
import org.epics.pvdata.pv.PVStructure;


/**
 * Interface to the data for the channel.
 * @author mrk
 *
 */
public interface V3ChannelStructure {
    /**
     * Get the native DBRType for the value field.
     * @return The DBRType.
     */
    DBRType getNativeDBRType();
    /**
     * Create the PVStructure.
     * @param pvRequest The pvRequest.
     * @param propertiesAllowed Are properties are allowed, i.e. alarm, timeStamp, control, display?
     * @return pvStructure or null if failure.
     */
    PVStructure createPVStructure(PVStructure pvRequest,boolean propertiesAllowed);
    /**
     * Get the request DBRType.
     * @return The DBRType.
     */
    DBRType getRequestDBRType();
    /**
     * Get the PVStructure interface.
     * @return The interface.
     */
    PVStructure getPVStructure();
    /**
     * Get the bitSet for changes.
     * @return The bitSet.
     */
    BitSet getBitSet();
    /**
     * Update the PVStructure with data from a DBR.
     * @param fromDBR The DBR that holds the new data.
     */
    void toStructure(DBR fromDBR);
}
