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
public class PVCopyServerFactory {
    public static PVCopyServer create(PVCopy pvCopy) {
        PVCopyServerImpl copyServer =  new PVCopyServerImpl(pvCopy);
        copyServer.init();
        return copyServer;
    }
    
   
    private static class PVCopyServerImpl implements PVCopyServer {
        
        private PVCopyServerImpl(PVCopy pvCopy) {
            this.pvCopy = pvCopy;
        }
        
        private void init() {
            
        }
        private PVCopy pvCopy = null;
    }
}
