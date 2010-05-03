/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.monitor;


import org.epics.pvData.factory.ConvertFactory;
import org.epics.pvData.factory.PVDataFactory;
import org.epics.pvData.pv.Convert;
import org.epics.pvData.pv.MessageType;
import org.epics.pvData.pv.PVBoolean;
import org.epics.pvData.pv.PVDataCreate;
import org.epics.pvData.pv.PVField;
import org.epics.pvData.pv.PVRecord;
import org.epics.pvData.pv.PVString;
import org.epics.pvData.pv.PVStructure;

/**
 * Factory that implements alarm onChange
 * @author mrk
 *
 */
public class AlgorithmOnChangeFactory {
    private static final String name = "onChange";
    private static final OnChange onChange = new OnChange();
    protected static final PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
    protected static final Convert convert = ConvertFactory.getConvert();

    /**
     * Register the create factory.
     */
    public static void register() {
    	MonitorFactory.registerMonitorAlgorithmCreater(onChange);
    }
    
    private static class OnChange implements MonitorAlgorithmCreate {
        /* (non-Javadoc)
         * @see org.epics.ioc.channelAccess.MonitorCreate#getName()
         */
        @Override
        public String getAlgorithmName() {
            return name;
        }
		/* (non-Javadoc)
		 * @see org.epics.pvData.monitor.MonitorAlgorithmCreate#create(org.epics.pvData.pv.PVRecord, org.epics.pvData.monitor.MonitorRequester, org.epics.pvData.pv.PVField, org.epics.pvData.pv.PVStructure)
		 */
		@Override
		public MonitorAlgorithm create(PVRecord pvRecord,
				MonitorRequester monitorRequester, PVField fromPVRecord,
				PVStructure pvOptions)
		{
			if(pvOptions==null) {
				monitorRequester.message("no monitor options", MessageType.error);
				return null;
			}
			boolean causeMonitor = true;
			PVField pvField = pvOptions.getSubField("causeMonitor");
			if(pvField!=null) {
				if(pvField instanceof PVString) {
					PVString pvString = (PVString)pvField;
					if(pvString.get().equals("false")) causeMonitor = false;
				} else if(pvField instanceof PVBoolean) {
					PVBoolean pvBoolean = (PVBoolean)pvField;
					causeMonitor = pvBoolean.get();
				}
			}
			return new MonitorAlgorithmImpl(fromPVRecord,causeMonitor);
		}
    }
    
    
    private static class MonitorAlgorithmImpl implements MonitorAlgorithm {
        private MonitorAlgorithmImpl(PVField pvFromRecord,boolean causeMonitor)
        {
            this.pvFromRecord = pvFromRecord;
            this.causeMonitor = causeMonitor;
            if(causeMonitor) {
            	pvCopy = pvDataCreate.createPVField(null, "", pvFromRecord);
            } else {
            	pvCopy = null;
            }
        }
        
        private final PVField pvFromRecord;
        private final boolean causeMonitor;
        private final PVField pvCopy;
		/* (non-Javadoc)
		 * @see org.epics.pvData.monitor.MonitorAlgorithm#causeMonitor()
		 */
		@Override
		public boolean causeMonitor() {
			if(!causeMonitor) return false;
			if(pvFromRecord.equals(pvCopy))return false;
			convert.copy(pvFromRecord, pvCopy);
			return true;
		}
		/* (non-Javadoc)
		 * @see org.epics.pvData.monitor.MonitorAlgorithm#getAlgorithmName()
		 */
		@Override
		public String getAlgorithmName() {
			return name;
		}
		/* (non-Javadoc)
		 * @see org.epics.pvData.monitor.MonitorAlgorithm#monitorIssued()
		 */
		@Override
		public void monitorIssued() {}
        
    }
}
