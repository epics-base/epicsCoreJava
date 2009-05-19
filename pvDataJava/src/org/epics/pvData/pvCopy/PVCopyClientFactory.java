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
public class PVCopyClientFactory {
    public static PVCopyClient create(PVStructure pvStructure) {
        return new PVCopyClientImpl(pvStructure);
    }
    
   
    private static class PVCopyClientImpl implements PVCopyClient {
        
        private PVCopyClientImpl(PVStructure pvStructure) {
            this.pvStructure = pvStructure;
        }
        private PVStructure pvStructure = null;
        
    }
}
