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
public class PVCopyFactory {
    public static PVCopy create(PVRecord pvRecord,PVStructure request) {
        PVCopyImpl pvCopy = new PVCopyImpl(pvRecord,request);
        pvCopy.init();
        return pvCopy;
    }
    
    private static class PVCopyImpl implements PVCopy{
        
        private PVCopyImpl(PVRecord pvRecord,PVStructure request) {
            this.pvRecord = pvRecord;
            this.request = request;
        }
        
        private void init() {
            
        }

        private PVRecord pvRecord;
        private PVStructure request;
        /* (non-Javadoc)
         * @see org.epics.pvData.pvCopy.PVCopy#getCopyStructure()
         */
        @Override
        public Structure getCopyStructure() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.epics.pvData.pvCopy.PVCopy#getPVRecord()
         */
        @Override
        public PVRecord getPVRecord() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.epics.pvData.pvCopy.PVCopy#getRecordPVFields()
         */
        @Override
        public PVField[] getRecordPVFields() {
            // TODO Auto-generated method stub
            return null;
        }
    }
}
