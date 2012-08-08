/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata.misc;


/**
 * Interface declaring destroy() method.
 * @author mrk
 *
 */
public interface Destroyable  {
    /**
     * Destroy instance.
     */
    void destroy();
}
