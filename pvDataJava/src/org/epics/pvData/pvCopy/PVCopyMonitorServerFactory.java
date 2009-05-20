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
public class PVCopyMonitorServerFactory {
    public static PVCopyMonitorServer create(PVCopy pvCopy,int queueSize)
    {
        PVCopyMonitorServerImpl pvCopyMonitorServer = new PVCopyMonitorServerImpl(
                pvCopy,queueSize);
        pvCopyMonitorServer.init();
        return pvCopyMonitorServer;
    }
    
   
    private static class PVCopyMonitorServerImpl implements PVCopyMonitorServer {
        
        private PVCopyMonitorServerImpl(PVCopy pvCopy,int queueSize)
        {
            this.pvCopy = pvCopy;
            this.queueSize = queueSize;
        }
        
        private void init() {
            
        }
        private PVCopy pvCopy = null;
        private int queueSize = 0;
        private PVRecord pvRecord = null;
        private PVField[] pvRecordFields = null;
        private PVStructure pvStructure = null;
        
    }
}
