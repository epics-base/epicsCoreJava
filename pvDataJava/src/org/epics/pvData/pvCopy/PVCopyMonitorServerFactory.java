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
    public static PVCopyMonitorServer create(PVRecord pvRecord,
            PVField[] pvFields,
            boolean shareArrayData,int queueSize)
    {
        return new PVCopyMonitorServerImpl(
                pvRecord,pvFields,
                shareArrayData,queueSize);
    }
    
   
    private static class PVCopyMonitorServerImpl implements PVCopyMonitorServer {
        
        private PVCopyMonitorServerImpl(
                PVRecord pvRecord,PVField[] pvFields,
                boolean shareArrayData,int queueSize)
        {
            this.pvRecord = pvRecord;
            this.pvRecordFields = pvFields;
            this.shareArrayData = shareArrayData;
            this.queueSize = queueSize;
        }
        private PVRecord pvRecord = null;
        private PVField[] pvRecordFields = null;
        private PVStructure pvStructure = null;
        private boolean shareArrayData = false;
        private int queueSize = 0;
    }
}
