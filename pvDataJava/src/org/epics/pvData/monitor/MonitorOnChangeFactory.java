/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.monitor;


import org.epics.pvData.factory.PVDataFactory;
import org.epics.pvData.misc.BitSet;
import org.epics.pvData.pv.MessageType;
import org.epics.pvData.pv.PVDataCreate;
import org.epics.pvData.pv.PVField;
import org.epics.pvData.pv.PVRecord;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pvCopy.PVCopy;

/**
 * @author mrk
 *
 */
public class MonitorOnChangeFactory {
    private static final String name = "onChange";
    private static final MonitorOnChange monitorOnChange = new MonitorOnChange();
    private static final PVDataCreate pvDataCreate= PVDataFactory.getPVDataCreate();

    public static MonitorCreate getMonitorCreate() {
        return monitorOnChange;
    }
    private static class MonitorOnChange implements MonitorCreate {
        /* (non-Javadoc)
         * @see org.epics.ioc.channelAccess.MonitorCreate#create(org.epics.ca.channelAccess.client.ChannelMonitorRequester, org.epics.pvData.pv.PVStructure, org.epics.pvData.pvCopy.PVCopy, byte, org.epics.pvData.misc.Executor)
         */
        public Monitor create(
                PVRecord pvRecord,
                MonitorRequester monitorRequester,
                PVStructure pvOption,
                PVCopy pvCopy,
                int queueSize)
        {
            PVStructure pvStructure = pvCopy.createPVStructure();
            PVField pvField = pvStructure.getSubField("value");
            if(pvField==null) {
                monitorRequester.message("value field not defined", MessageType.error);
                return null;
            }
            pvField = pvCopy.getRecordPVField(pvField.getFieldOffset());
            return new Monitor(pvRecord,monitorRequester,pvCopy,queueSize,pvField);
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.channelAccess.MonitorCreate#getName()
         */
        @Override
        public String getName() {
            return name;
        }
    }
    
    
    private static class Monitor extends BaseMonitor {
        private Monitor(
                PVRecord pvRecord,
                MonitorRequester monitorRequester,
                PVCopy pvCopy,
                int queueSize,
                PVField valuePVField)
        {
            super(pvRecord,monitorRequester,pvCopy,queueSize);
            this.valuePVField = valuePVField;
            pvPrev = pvDataCreate.createPVField(null, null, valuePVField);
        }
        
        private PVField valuePVField;
        private PVField pvPrev;
        /* (non-Javadoc)
         * @see org.epics.ioc.channelAccess.BaseMonitor#generateMonitor(org.epics.pvData.misc.BitSet)
         */
        @Override
        protected boolean generateMonitor(BitSet changeBitSet) {
            if(valuePVField.equals(pvPrev)) return false;
            convert.copy(valuePVField, pvPrev);   
            return true;
        }
        
    }
}
