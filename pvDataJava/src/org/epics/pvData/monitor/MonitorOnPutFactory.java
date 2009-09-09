/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.monitor;

import org.epics.pvData.misc.BitSet;
import org.epics.pvData.pv.PVField;
import org.epics.pvData.pv.PVRecord;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pvCopy.PVCopy;

/**
 * @author mrk
 *
 */
public class MonitorOnPutFactory{
    private static final String name = "onPut";
    private static final MonitorOnPut monitorOnPut = new MonitorOnPut();

    public static MonitorCreate getMonitorCreate() {
        return monitorOnPut;
    }
    
    private static class MonitorOnPut implements MonitorCreate {
        /* (non-Javadoc)
         * @see org.epics.ioc.channelAccess.MonitorCreate#create(org.epics.ca.channelAccess.client.Channel, org.epics.ca.channelAccess.client.ChannelMonitorRequester, org.epics.pvData.pv.PVStructure, org.epics.pvData.pvCopy.PVCopy, int, org.epics.pvData.misc.Executor)
         */
        public Monitor create(
                PVRecord pvRecord,
                MonitorCreator monitorCreator,
                MonitorRequester monitorRequester,
                PVStructure pvOption,
                PVCopy pvCopy,
                int queueSize)
        {
            return new Monitor(pvRecord,monitorCreator,monitorRequester,pvCopy,queueSize);
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.channelAccess.MonitorCreate#getName()
         */
        @Override
        public String getName() {
            return name;
        }
    }

    private static class Monitor extends AbstractLocalMonitor {
        private Monitor(
                PVRecord pvRecord,
                MonitorCreator monitorCreator,
                MonitorRequester monitorRequester,
                PVCopy pvCopy,
                int queueSize)
        {
            super(pvRecord,monitorCreator,monitorRequester,pvCopy,queueSize);
            PVStructure pvStructure = pvCopy.createPVStructure();
            PVField pvField = pvStructure.getSubField("timeStamp");
            if(pvField!=null) {
                timeStampOffset = pvField.getFieldOffset();
                afterTimeStampOffset = pvField.getNextFieldOffset();
            }
        }
        
        private int timeStampOffset = -1;
        private int afterTimeStampOffset = -1;
        /* (non-Javadoc)
         * @see org.epics.pvData.monitor.AbstractMonitor#generateMonitor(org.epics.pvData.misc.BitSet)
         */
        @Override
        protected boolean generateMonitor(BitSet bitSet) {
            if(timeStampOffset<0) return true;
            int first = bitSet.nextSetBit(0);
            int next = bitSet.nextSetBit(afterTimeStampOffset);
            if(first>=timeStampOffset && next==-1) return false;
            return true;
        }

    }
}
