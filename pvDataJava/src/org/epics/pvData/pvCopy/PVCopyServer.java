/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.pvCopy;
import org.epics.pvData.pv.*;

/**
 * @author mrk
 *
 */
public interface PVCopyServer {
    void destroy();
    MapToFromRecord getMapToFromRecord();
    void updateCopy();
    void updateSource();
}
