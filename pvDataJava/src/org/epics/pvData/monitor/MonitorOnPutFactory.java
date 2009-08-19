/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.monitor;

import org.epics.ca.channelAccess.client.Channel;
import org.epics.ca.channelAccess.client.ChannelMonitor;
import org.epics.ca.channelAccess.client.ChannelMonitorRequester;
import org.epics.ca.channelAccess.server.ChannelServer;
import org.epics.ca.channelAccess.server.MonitorCreate;
import org.epics.ca.channelAccess.server.impl.ChannelServerFactory;
import org.epics.pvData.misc.BitSet;
import org.epics.pvData.misc.Executor;
import org.epics.pvData.pv.PVField;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pvCopy.PVCopy;

/**
 * @author mrk
 *
 */
public class MonitorOnPutFactory{
    private static final String name = "onPut";
    private static final MonitorOnPut monitorOnPut = new MonitorOnPut();
    private static final ChannelServer channelServer = ChannelServerFactory.getChannelServer();

    public static void start() {
        channelServer.registerMonitor(monitorOnPut);
    }
    
    private static class MonitorOnPut implements MonitorCreate {
        /* (non-Javadoc)
         * @see org.epics.ioc.channelAccess.MonitorCreate#create(org.epics.ca.channelAccess.client.Channel, org.epics.ca.channelAccess.client.ChannelMonitorRequester, org.epics.pvData.pv.PVStructure, org.epics.pvData.pvCopy.PVCopy, int, org.epics.pvData.misc.Executor)
         */
        public Monitor create(
                Channel channel,
                ChannelMonitorRequester channelMonitorRequester,
                PVStructure pvOption,
                PVCopy pvCopy,
                int queueSize,
                Executor executor)
        {
            return new Monitor(channel,channelMonitorRequester,pvCopy,queueSize,executor);
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
                Channel channel,
                ChannelMonitorRequester channelMonitorRequester,
                PVCopy pvCopy,
                int queueSize,
                Executor executor)
        {
            super(channel,channelMonitorRequester,pvCopy,queueSize,executor);
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
         * @see org.epics.ioc.channelAccess.BaseMonitor#generateMonitor(org.epics.pvData.misc.BitSet)
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
