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
    public static PVCopyServer create(PVRecord pvRecord,
            PVField[] pvFields,boolean shareArrayData) {
        return new PVCopyServerImpl(pvRecord,pvFields,shareArrayData);
    }
    
   
    private static class PVCopyServerImpl implements PVCopyServer {
        
        private PVCopyServerImpl(PVRecord pvRecord,PVField[] pvFields,boolean shareArrayData) {
            this.pvRecord = pvRecord;
            this.pvRecordFields = pvFields;
            this.shareArrayData = shareArrayData;
        }
        private PVRecord pvRecord = null;
        private PVField[] pvRecordFields = null;
        private boolean shareArrayData = false;
    }
}
