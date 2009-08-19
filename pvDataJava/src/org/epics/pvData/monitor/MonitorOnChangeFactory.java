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
import org.epics.pvData.factory.PVDataFactory;
import org.epics.pvData.misc.BitSet;
import org.epics.pvData.misc.Executor;
import org.epics.pvData.pv.MessageType;
import org.epics.pvData.pv.PVDataCreate;
import org.epics.pvData.pv.PVField;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pvCopy.PVCopy;

/**
 * @author mrk
 *
 */
public class MonitorOnChangeFactory {
    private static final String name = "onChange";
    private static final MonitorOnChange monitorOnChange = new MonitorOnChange();
    private static final ChannelServer channelServer = ChannelServerFactory.getChannelServer();
    private static final PVDataCreate pvDataCreate= PVDataFactory.getPVDataCreate();

    public static void start() {
        channelServer.registerMonitor(monitorOnChange);
    }

    private static class MonitorOnChange implements MonitorCreate {
        /* (non-Javadoc)
         * @see org.epics.ioc.channelAccess.MonitorCreate#create(org.epics.ca.channelAccess.client.ChannelMonitorRequester, org.epics.pvData.pv.PVStructure, org.epics.pvData.pvCopy.PVCopy, byte, org.epics.pvData.misc.Executor)
         */
        public Monitor create(
                Channel channel,
                ChannelMonitorRequester channelMonitorRequester,
                PVStructure pvOption,
                PVCopy pvCopy,
                int queueSize,
                Executor executor)
        {
            PVStructure pvStructure = pvCopy.createPVStructure();
            PVField pvField = pvStructure.getSubField("value");
            if(pvField==null) {
                channelMonitorRequester.message("value field not defined", MessageType.error);
                return null;
            }
            pvField = pvCopy.getRecordPVField(pvField.getFieldOffset());
            return new Monitor(channel,channelMonitorRequester,pvCopy,queueSize,executor,pvField);
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
                Executor executor,
                PVField valuePVField)
        {
            super(channel,channelMonitorRequester,pvCopy,queueSize,executor);
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
