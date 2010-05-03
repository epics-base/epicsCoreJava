/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.monitor.test;

import junit.framework.TestCase;

import org.epics.pvData.factory.PVDatabaseFactory;
import org.epics.pvData.factory.PVReplaceFactory;
import org.epics.pvData.misc.BitSet;
import org.epics.pvData.monitor.Monitor;
import org.epics.pvData.monitor.MonitorElement;
import org.epics.pvData.monitor.MonitorFactory;
import org.epics.pvData.monitor.MonitorRequester;
import org.epics.pvData.pv.MessageType;
import org.epics.pvData.pv.PVDatabase;
import org.epics.pvData.pv.PVDouble;
import org.epics.pvData.pv.PVInt;
import org.epics.pvData.pv.PVLong;
import org.epics.pvData.pv.PVRecord;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.Requester;
import org.epics.pvData.pv.Status;
import org.epics.pvData.pv.Structure;
import org.epics.pvData.pvCopy.PVCopyFactory;
import org.epics.pvData.test.RequesterForTesting;
import org.epics.pvData.xml.XMLToPVDatabaseFactory;



/**
 * JUnit test for pvAccess.
 * It also provides examples of how to use the pvAccess interfaces.
 * @author mrk
 *
 */
public class MonitorTest extends TestCase {
    private static PVDatabase master = PVDatabaseFactory.getMaster();
    private static final Requester requester = new RequesterImpl();
    
    private static class RequesterImpl implements Requester {
		@Override
		public String getRequesterName() {
			return "pvCopyTest";
		}
		@Override
		public void message(String message, MessageType messageType) {
		    System.out.printf("message %s messageType %s%n",message,messageType.name());
			
		}
    }
    
    public static void testPVCopy() {
     // get database for testing
        Requester iocRequester = new RequesterForTesting("accessTest");
        XMLToPVDatabaseFactory.convert(master,"${PVDATA}/xml/structures.xml", iocRequester);
        XMLToPVDatabaseFactory.convert(master,"${PVDATA}/src/org/epics/pvData/monitor/test/power.xml", iocRequester);
        PVReplaceFactory.replace(master);
        timeStampTest();
        noQueueTest();
        queueTest();
        deadbandTest();
        deadbandPercentTest();
        periodicTest();
    }
    
    public static void timeStampTest() {
        System.out.printf("%n****timeStamp Test****%n");
        // definitions for request structure to pass to PVCopyFactory
        PVRecord pvRecord = master.findRecord("powerWithoutDeadband");
        PVStructure pvStructure = pvRecord.getPVStructure();
        PVLong pvRecordSeconds = (PVLong)pvStructure.getSubField("timeStamp.secondsPastEpoch");
        PVInt pvRecordNanoSeconds = (PVInt)pvStructure.getSubField("timeStamp.nanoSeconds");
        PVDouble pvRecordPowerValue = (PVDouble)pvStructure.getSubField("power.value");
        String request = "record[queueSize=1]field(alarm,timeStamp[algorithm=onChange,causeMonitor=true],power.value)";
        PVStructure pvRequest = PVCopyFactory.createRequest(request,requester);
//System.out.println("request:" + request);
//System.out.println("pvRequest:" + pvRequest);
        MonitorRequesterImpl monitorRequester = new  MonitorRequesterImpl(pvRecord,pvRequest);
        MonitorElement monitorElement = monitorRequester.poll();
        assertTrue(monitorElement!=null);
        PVStructure pvCopy = monitorElement.getPVStructure();
        PVStructure pvTimeStamp = pvCopy.getStructureField("timeStamp");
        int timeStampOffset = pvTimeStamp.getFieldOffset();
        PVDouble pvValue = pvCopy.getDoubleField("value");
        BitSet change = monitorElement.getChangedBitSet();
        BitSet overrun = monitorElement.getOverrunBitSet();
//System.out.println("pvCopy " + pvCopy);
//System.out.println("change " + change);
//System.out.println("overrun " + overrun);
        assertTrue(change.get(0));
        assertTrue(overrun.isEmpty());
        change.clear(0);
        assertTrue(change.isEmpty());
        monitorRequester.release();
        pvRecord.beginGroupPut();
        pvRecordSeconds.put(5);
        pvRecordNanoSeconds.put(0);
        pvRecord.endGroupPut();
        monitorElement = monitorRequester.poll();
        assertTrue(monitorElement!=null);
        pvCopy = monitorElement.getPVStructure();
        change = monitorElement.getChangedBitSet();
        overrun = monitorElement.getOverrunBitSet();
//System.out.println("change " + change);
//System.out.println("overrun " + overrun);
        assertTrue(change.get(timeStampOffset));
        change.clear(timeStampOffset);
        assertTrue(change.isEmpty());
        assertTrue(overrun.isEmpty());
        assertTrue(pvRecordPowerValue.get()==pvValue.get());
        monitorRequester.release();
        monitorElement = monitorRequester.poll();
        assertTrue(monitorElement==null);
        monitorRequester.destroy();
    }
    
    public static void noQueueTest() {
        System.out.printf("%n****No Queue Test****%n");
        // definitions for request structure to pass to PVCopyFactory
        PVRecord pvRecord = master.findRecord("powerWithoutDeadband");
        PVStructure pvStructure = pvRecord.getPVStructure();
        PVLong pvRecordSeconds = (PVLong)pvStructure.getSubField("timeStamp.secondsPastEpoch");
        PVInt pvRecordNanoSeconds = (PVInt)pvStructure.getSubField("timeStamp.nanoSeconds");
        PVDouble pvRecordPowerValue = (PVDouble)pvStructure.getSubField("power.value");
        String request = "record[queueSize=1]field(alarm,timeStamp,power.value)";
//System.out.println("pvRecord " + pvRecord);
        PVStructure pvRequest = PVCopyFactory.createRequest(request,requester);
//System.out.println("request:" + request);
//System.out.println("pvRequest:" + pvRequest);
        MonitorRequesterImpl monitorRequester = new  MonitorRequesterImpl(pvRecord,pvRequest);
        MonitorElement monitorElement = monitorRequester.poll();
        assertTrue(monitorElement!=null);
        PVStructure pvCopy = monitorElement.getPVStructure();
        PVStructure pvTimeStamp = pvCopy.getStructureField("timeStamp");
        int timeStampOffset = pvTimeStamp.getFieldOffset();
        PVDouble pvValue = pvCopy.getDoubleField("value");
        int valueOffset = pvValue.getFieldOffset();
        BitSet change = monitorElement.getChangedBitSet();
        BitSet overrun = monitorElement.getOverrunBitSet();
//System.out.println("pvCopy " + pvCopy);
//System.out.println("change " + change);
//System.out.println("overrun " + overrun);
        assertTrue(change.get(0));
        assertTrue(overrun.isEmpty());
        change.clear(0);
        assertTrue(change.isEmpty());
        monitorRequester.release();
        pvRecord.beginGroupPut();
        pvRecordSeconds.put(5);
        pvRecordNanoSeconds.put(0);
        pvRecordPowerValue.put(5.0);
        pvRecord.endGroupPut();
        monitorElement = monitorRequester.poll();
        assertTrue(monitorElement!=null);
        pvCopy = monitorElement.getPVStructure();
        change = monitorElement.getChangedBitSet();
        overrun = monitorElement.getOverrunBitSet();
//System.out.println("change " + change);
//System.out.println("overrun " + overrun);
        assertTrue(change.get(timeStampOffset));
        change.clear(timeStampOffset);
        assertTrue(change.get(valueOffset));
        change.clear(valueOffset);
        assertTrue(change.isEmpty());
        assertTrue(overrun.isEmpty());
        assertTrue(pvRecordPowerValue.get()==pvValue.get());
        monitorRequester.release();
        pvRecord.beginGroupPut();
        pvRecordPowerValue.put(6.0);
        pvRecordPowerValue.put(6.5);
        pvRecord.endGroupPut();
        monitorElement = monitorRequester.poll();
        assertTrue(monitorElement!=null);
        pvCopy = monitorElement.getPVStructure();
        change = monitorElement.getChangedBitSet();
        overrun = monitorElement.getOverrunBitSet();
//System.out.println("change " + change);
//System.out.println("overrun " + overrun);
        assertTrue(change.get(valueOffset));
        change.clear(valueOffset);
        assertTrue(change.isEmpty());
        assertTrue(overrun.get(valueOffset));
        assertTrue(pvRecordPowerValue.get()==pvValue.get());
        monitorRequester.release();
        monitorElement = monitorRequester.poll();
        assertTrue(monitorElement==null);
        monitorRequester.destroy();
    }
    
    public static void queueTest() {
        System.out.printf("%n****Queue Test****%n");
        // definitions for request structure to pass to PVCopyFactory
        PVRecord pvRecord = master.findRecord("powerWithoutDeadband");
        PVStructure pvStructure = pvRecord.getPVStructure();
        PVLong pvRecordSeconds = (PVLong)pvStructure.getSubField("timeStamp.secondsPastEpoch");
        PVInt pvRecordNanoSeconds = (PVInt)pvStructure.getSubField("timeStamp.nanoSeconds");
        PVDouble pvRecordPowerValue = (PVDouble)pvStructure.getSubField("power.value");
        String request = "alarm,timeStamp,power.value";
//System.out.println("pvRecord " + pvRecord);
        PVStructure pvRequest = PVCopyFactory.createRequest(request,requester);
//System.out.println("request:" + request);
//System.out.println("pvRequest:" + pvRequest);
        MonitorRequesterImpl monitorRequester = new  MonitorRequesterImpl(pvRecord,pvRequest);
        MonitorElement monitorElement = monitorRequester.poll();
        assertTrue(monitorElement!=null);
        PVStructure pvCopy = monitorElement.getPVStructure();
        BitSet change = monitorElement.getChangedBitSet();
        BitSet overrun = monitorElement.getOverrunBitSet();
        PVStructure pvTimeStamp = pvCopy.getStructureField("timeStamp");
        int timeStampOffset = pvTimeStamp.getFieldOffset();
        PVDouble pvValue = pvCopy.getDoubleField("value");
        int valueOffset = pvValue.getFieldOffset();
//System.out.println("pvCopy " + pvCopy);
//System.out.println("change " + change);
//System.out.println("overrun " + overrun);
        assertTrue(change.get(0));
        assertTrue(overrun.isEmpty());
        change.clear(0);
        assertTrue(change.isEmpty());
        monitorRequester.release();
        pvRecord.beginGroupPut();
        pvRecordSeconds.put(5);
        pvRecordNanoSeconds.put(0);
        pvRecordPowerValue.put(5.0);
        pvRecord.endGroupPut();
        monitorElement = monitorRequester.poll();
        assertTrue(monitorElement!=null);
        pvCopy = monitorElement.getPVStructure();
        change = monitorElement.getChangedBitSet();
        overrun = monitorElement.getOverrunBitSet();
//System.out.println("change " + change);
//System.out.println("overrun " + overrun);
        assertTrue(change.get(timeStampOffset));
        change.clear(timeStampOffset);
        assertTrue(change.get(valueOffset));
        change.clear(valueOffset);
        assertTrue(change.isEmpty());
        assertTrue(overrun.isEmpty());
        assertTrue(pvRecordPowerValue.get()==pvValue.get());
        monitorRequester.release();
        pvRecordPowerValue.put(6.0);
        pvRecordPowerValue.put(7.0);
        pvRecordPowerValue.put(8.0);
        monitorElement = monitorRequester.poll();
        assertTrue(monitorElement!=null);
        pvCopy = monitorElement.getPVStructure();
        change = monitorElement.getChangedBitSet();
        overrun = monitorElement.getOverrunBitSet();
//System.out.println("change " + change);
//System.out.println("overrun " + overrun);
        assertTrue(change.get(valueOffset));
        change.clear(valueOffset);
        assertTrue(change.isEmpty());
        assertTrue(pvValue.get()==6.0);
        monitorRequester.release();
        monitorElement = monitorRequester.poll();
        assertTrue(monitorElement!=null);
        pvCopy = monitorElement.getPVStructure();
        change = monitorElement.getChangedBitSet();
        overrun = monitorElement.getOverrunBitSet();
//System.out.println("change " + change);
//System.out.println("overrun " + overrun);
        assertTrue(change.get(valueOffset));
        change.clear(valueOffset);
        assertTrue(change.isEmpty());
        assertTrue(pvValue.get()==pvRecordPowerValue.get());
        assertTrue(overrun.get(valueOffset));
        overrun.clear(valueOffset);
        assertTrue(overrun.isEmpty());
        monitorRequester.release();
        monitorElement = monitorRequester.poll();
        assertTrue(monitorElement==null);
        monitorRequester.destroy();
    }
    
    public static void deadbandTest() {
        System.out.printf("%n****deadband Test****%n");
        // definitions for request structure to pass to PVCopyFactory
        PVRecord pvRecord = master.findRecord("powerWithDeadband");
        PVStructure pvStructure = pvRecord.getPVStructure();
        PVLong pvRecordSeconds = (PVLong)pvStructure.getSubField("timeStamp.secondsPastEpoch");
        PVInt pvRecordNanoSeconds = (PVInt)pvStructure.getSubField("timeStamp.nanoSeconds");
        PVDouble pvRecordPowerValue = (PVDouble)pvStructure.getSubField("power.value");
        String request = "alarm,timeStamp,power.value";
//System.out.println("pvRecord " + pvRecord);
        PVStructure pvRequest = PVCopyFactory.createRequest(request,requester);
//System.out.println("request:" + request);
//System.out.println("pvRequest:" + pvRequest);
        MonitorRequesterImpl monitorRequester = new  MonitorRequesterImpl(pvRecord,pvRequest);
        MonitorElement monitorElement = monitorRequester.poll();
        assertTrue(monitorElement!=null);
        PVStructure pvCopy = monitorElement.getPVStructure();
        BitSet change = monitorElement.getChangedBitSet();
        BitSet overrun = monitorElement.getOverrunBitSet();
        PVStructure pvTimeStamp = pvCopy.getStructureField("timeStamp");
        int timeStampOffset = pvTimeStamp.getFieldOffset();
        PVDouble pvValue = pvCopy.getDoubleField("value");
        int valueOffset = pvValue.getFieldOffset();
//System.out.println("pvCopy " + pvCopy);
//System.out.println("change " + change);
//System.out.println("overrun " + overrun);
        assertTrue(change.get(0));
        assertTrue(overrun.isEmpty());
        change.clear(0);
        assertTrue(change.isEmpty());
        monitorRequester.release();
        pvRecord.beginGroupPut();
        pvRecordSeconds.put(5);
        pvRecordNanoSeconds.put(0);
        pvRecordPowerValue.put(5.0);
        pvRecord.endGroupPut();
        monitorElement = monitorRequester.poll();
        assertTrue(monitorElement!=null);
        pvCopy = monitorElement.getPVStructure();
        change = monitorElement.getChangedBitSet();
        overrun = monitorElement.getOverrunBitSet();
//System.out.println("change " + change);
//System.out.println("overrun " + overrun);
        assertTrue(change.get(timeStampOffset));
        change.clear(timeStampOffset);
        assertTrue(change.get(valueOffset));
        change.clear(valueOffset);
        assertTrue(change.isEmpty());
        assertTrue(overrun.isEmpty());
        assertTrue(pvRecordPowerValue.get()==pvValue.get());
        monitorRequester.release();
        pvRecordPowerValue.put(5.01);
        monitorElement = monitorRequester.poll();
        assertTrue(monitorElement==null);
        pvRecordPowerValue.put(5.11);
        monitorElement = monitorRequester.poll();
        pvRecordPowerValue.put(6.0);
        assertTrue(monitorElement!=null);
        pvCopy = monitorElement.getPVStructure();
        change = monitorElement.getChangedBitSet();
        overrun = monitorElement.getOverrunBitSet();
//System.out.println("change " + change);
//System.out.println("overrun " + overrun);
        assertTrue(change.get(valueOffset));
        change.clear(valueOffset);
        assertTrue(change.isEmpty());
        assertTrue(pvValue.get()==5.11);
        monitorRequester.release();
        monitorElement = monitorRequester.poll();
        assertTrue(monitorElement!=null);
        pvCopy = monitorElement.getPVStructure();
        change = monitorElement.getChangedBitSet();
        overrun = monitorElement.getOverrunBitSet();
//System.out.println("change " + change);
//System.out.println("overrun " + overrun);
        assertTrue(change.get(valueOffset));
        change.clear(valueOffset);
        assertTrue(change.isEmpty());
        assertTrue(pvValue.get()==pvRecordPowerValue.get());
        assertTrue(overrun.isEmpty());
        monitorRequester.release();
        monitorElement = monitorRequester.poll();
        assertTrue(monitorElement==null);
        monitorRequester.destroy();
    }
    
    public static void deadbandPercentTest() {
        System.out.printf("%n****deadband percent Test****%n");
        // definitions for request structure to pass to PVCopyFactory
        PVRecord pvRecord = master.findRecord("powerWithDeadbandPercent");
        PVStructure pvStructure = pvRecord.getPVStructure();
        PVLong pvRecordSeconds = (PVLong)pvStructure.getSubField("timeStamp.secondsPastEpoch");
        PVInt pvRecordNanoSeconds = (PVInt)pvStructure.getSubField("timeStamp.nanoSeconds");
        PVDouble pvRecordPowerValue = (PVDouble)pvStructure.getSubField("power.value");
        String request = "alarm,timeStamp,power.value";
//System.out.println("pvRecord " + pvRecord);
        PVStructure pvRequest = PVCopyFactory.createRequest(request,requester);
//System.out.println("request:" + request);
//System.out.println("pvRequest:" + pvRequest);
        MonitorRequesterImpl monitorRequester = new  MonitorRequesterImpl(pvRecord,pvRequest);
        MonitorElement monitorElement = monitorRequester.poll();
        assertTrue(monitorElement!=null);
        PVStructure pvCopy = monitorElement.getPVStructure();
        BitSet change = monitorElement.getChangedBitSet();
        BitSet overrun = monitorElement.getOverrunBitSet();
        PVStructure pvTimeStamp = pvCopy.getStructureField("timeStamp");
        int timeStampOffset = pvTimeStamp.getFieldOffset();
        PVDouble pvValue = pvCopy.getDoubleField("value");
        int valueOffset = pvValue.getFieldOffset();
//System.out.println("pvCopy " + pvCopy);
//System.out.println("change " + change);
//System.out.println("overrun " + overrun);
        assertTrue(change.get(0));
        assertTrue(overrun.isEmpty());
        change.clear(0);
        assertTrue(change.isEmpty());
        monitorRequester.release();
        pvRecord.beginGroupPut();
        pvRecordSeconds.put(5);
        pvRecordNanoSeconds.put(0);
        pvRecordPowerValue.put(5.0);
        pvRecord.endGroupPut();
        monitorElement = monitorRequester.poll();
        assertTrue(monitorElement!=null);
        pvCopy = monitorElement.getPVStructure();
        change = monitorElement.getChangedBitSet();
        overrun = monitorElement.getOverrunBitSet();
//System.out.println("change " + change);
//System.out.println("overrun " + overrun);
        assertTrue(change.get(timeStampOffset));
        change.clear(timeStampOffset);
        assertTrue(change.get(valueOffset));
        change.clear(valueOffset);
        assertTrue(change.isEmpty());
        assertTrue(overrun.isEmpty());
        assertTrue(pvRecordPowerValue.get()==pvValue.get());
        monitorRequester.release();
        pvRecordPowerValue.put(5.01);
        monitorElement = monitorRequester.poll();
        assertTrue(monitorElement==null);
        pvRecordPowerValue.put(5.11);
        monitorElement = monitorRequester.poll();
        pvRecordPowerValue.put(6.0);
        assertTrue(monitorElement!=null);
        pvCopy = monitorElement.getPVStructure();
        change = monitorElement.getChangedBitSet();
        overrun = monitorElement.getOverrunBitSet();
//System.out.println("change " + change);
//System.out.println("overrun " + overrun);
        assertTrue(change.get(valueOffset));
        change.clear(valueOffset);
        assertTrue(change.isEmpty());
        assertTrue(pvValue.get()==5.11);
        monitorRequester.release();
        monitorElement = monitorRequester.poll();
        assertTrue(monitorElement!=null);
        pvCopy = monitorElement.getPVStructure();
        change = monitorElement.getChangedBitSet();
        overrun = monitorElement.getOverrunBitSet();
//System.out.println("change " + change);
//System.out.println("overrun " + overrun);
        assertTrue(change.get(valueOffset));
        change.clear(valueOffset);
        assertTrue(change.isEmpty());
        assertTrue(pvValue.get()==pvRecordPowerValue.get());
        assertTrue(overrun.isEmpty());
        monitorRequester.release();
        monitorElement = monitorRequester.poll();
        assertTrue(monitorElement==null);
        monitorRequester.destroy();
    }
    
    public static void periodicTest() {
        System.out.printf("%n****periodic Test****%n");
        // definitions for request structure to pass to PVCopyFactory
        PVRecord pvRecord = master.findRecord("powerWithoutDeadband");
        PVStructure pvStructure = pvRecord.getPVStructure();
        PVLong pvRecordSeconds = (PVLong)pvStructure.getSubField("timeStamp.secondsPastEpoch");
        PVInt pvRecordNanoSeconds = (PVInt)pvStructure.getSubField("timeStamp.nanoSeconds");
        PVDouble pvRecordPowerValue = (PVDouble)pvStructure.getSubField("power.value");
        String request = "record[periodicRate=.2]field(alarm,timeStamp,power.value)";
//System.out.println("pvRecord " + pvRecord);
        PVStructure pvRequest = PVCopyFactory.createRequest(request,requester);
//System.out.println("request:" + request);
//System.out.println("pvRequest:" + pvRequest);
        MonitorRequesterImpl monitorRequester = new  MonitorRequesterImpl(pvRecord,pvRequest);
        MonitorElement monitorElement = monitorRequester.poll();
        assertTrue(monitorElement!=null);
        PVStructure pvCopy = monitorElement.getPVStructure();
        PVStructure pvTimeStamp = pvCopy.getStructureField("timeStamp");
        int timeStampOffset = pvTimeStamp.getFieldOffset();
        PVDouble pvValue = pvCopy.getDoubleField("value");
        int valueOffset = pvValue.getFieldOffset();
        BitSet change = monitorElement.getChangedBitSet();
        BitSet overrun = monitorElement.getOverrunBitSet();
//System.out.println("pvCopy " + pvCopy);
//System.out.println("change " + change);
//System.out.println("overrun " + overrun);
        assertTrue(change.get(0));
        assertTrue(overrun.isEmpty());
        change.clear(0);
        assertTrue(change.isEmpty());
        monitorRequester.release();
        pvRecord.beginGroupPut();
        pvRecordSeconds.put(5);
        pvRecordNanoSeconds.put(0);
        pvRecordPowerValue.put(5.0);
        pvRecord.endGroupPut();
        monitorElement = monitorRequester.poll();
        assertTrue(monitorElement==null);
        try {
        	Thread.sleep(300);
        } catch (InterruptedException e) {}
        monitorElement = monitorRequester.poll();
        assertTrue(monitorElement!=null);
        pvCopy = monitorElement.getPVStructure();
        change = monitorElement.getChangedBitSet();
        overrun = monitorElement.getOverrunBitSet();
//System.out.println("change " + change);
//System.out.println("overrun " + overrun);
        assertTrue(change.get(timeStampOffset));
        change.clear(timeStampOffset);
        assertTrue(change.get(valueOffset));
        change.clear(valueOffset);
        assertTrue(change.isEmpty());
        assertTrue(overrun.isEmpty());
        assertTrue(pvRecordPowerValue.get()==pvValue.get());
        monitorRequester.release();
        pvRecord.beginGroupPut();
        pvRecordPowerValue.put(6.0);
        pvRecordPowerValue.put(6.0);
        pvRecord.endGroupPut();
        monitorElement = monitorRequester.poll();
        assertTrue(monitorElement==null);
        try {
        	Thread.sleep(300);
        } catch (InterruptedException e) {}
        monitorElement = monitorRequester.poll();
        assertTrue(monitorElement!=null);
        pvCopy = monitorElement.getPVStructure();
        change = monitorElement.getChangedBitSet();
        overrun = monitorElement.getOverrunBitSet();
//System.out.println("change " + change);
//System.out.println("overrun " + overrun);
        assertTrue(change.get(valueOffset));
        change.clear(valueOffset);
        assertTrue(change.isEmpty());
        assertTrue(overrun.get(valueOffset));
        assertTrue(pvRecordPowerValue.get()==pvValue.get());
        monitorRequester.release();
        monitorElement = monitorRequester.poll();
        assertTrue(monitorElement==null);
        monitorRequester.destroy();
    }
    
    private static class MonitorRequesterImpl implements MonitorRequester {
    	private Monitor monitor = null;
    	private boolean isDestroyed = false;
    	private MonitorElement monitorElement = null;
    	
    	MonitorRequesterImpl(PVRecord pvRecord,PVStructure pvRequest) {
    		monitor = MonitorFactory.create(pvRecord, this, pvRequest);
    		monitor.start();
    	}
    	
    	void destroy() {
    		isDestroyed = true;
    		monitor.destroy();
    		monitor = null;
    	}
    	
    	MonitorElement poll() {
    		monitorElement =  monitor.poll();
    		return monitorElement;
    	}
    	
    	void release() {
    		if(monitorElement==null) {
    			throw new IllegalStateException("Logic error. Should never get here");
    		}
    		monitor.release(monitorElement);
    		monitorElement = null;
    	}

		@Override
		public void monitorConnect(Status status, Monitor monitor,Structure structure) {
			if(isDestroyed) return;
			if(status.isSuccess()) {
				this.monitor = monitor;
				return;
			}
			System.out.printf("monitorConnect %s %s%n",status.getMessage(),structure.toString());
		}

		@Override
		public void monitorEvent(Monitor monitor) {
			if(isDestroyed) return;
		}

		@Override
		public void unlisten(Monitor monitor) {}

		@Override
		public String getRequesterName() {
			return "monitorTest";
		}

		@Override
		public void message(String message, MessageType messageType) {
			System.out.printf("%s %s%n",messageType.toString(),message);
		}  
    }
}

