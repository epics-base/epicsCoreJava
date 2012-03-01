package org.epics.ca.server.test.helpers;

import org.epics.ca.PVFactory;
import org.epics.pvData.misc.BitSet;
import org.epics.pvData.misc.Timer;
import org.epics.pvData.misc.Timer.TimerCallback;
import org.epics.pvData.misc.Timer.TimerNode;
import org.epics.pvData.misc.TimerFactory;
import org.epics.pvData.property.PVTimeStamp;
import org.epics.pvData.property.PVTimeStampFactory;
import org.epics.pvData.property.TimeStamp;
import org.epics.pvData.property.TimeStampFactory;
import org.epics.pvData.pv.FieldCreate;
import org.epics.pvData.pv.PVField;
import org.epics.pvData.pv.PVInt;
import org.epics.pvData.pv.ScalarType;

public class CounterTopStructure extends PVTopStructure implements TimerCallback
{
    private static final FieldCreate fieldCreate = PVFactory.getFieldCreate();

    private final PVInt valueField;
	private final int timeStampFieldOffset;
	private final PVTimeStamp timeStampField;
	private final TimerNode timerNode;
	
	private final TimeStamp timeStamp = TimeStampFactory.create();

	private final BitSet changedBitSet;
	
	public CounterTopStructure(double scanPeriodHz, Timer timer) {
		super(fieldCreate.createScalar("value", ScalarType.pvInt));

		changedBitSet = new BitSet(getPVStructure().getNumberFields());
		
		valueField = getPVStructure().getIntField("value");
		
		timeStampField = PVTimeStampFactory.create();
		PVField ts = getPVStructure().getStructureField("timeStamp");
		timeStampField.attach(ts);
		timeStampFieldOffset = ts.getFieldOffset();
		if (scanPeriodHz > 0.0)
		{
			timerNode = TimerFactory.createNode(this);
			timer.schedulePeriodic(timerNode, 0.0, scanPeriodHz);
		}
		else
			timerNode = null;
		
	}

	/* (non-Javadoc)
	 * @see org.epics.ca.server.test.TestChannelProviderImpl.PVTopStructure#process()
	 */
	@Override
	public void process() {
		changedBitSet.clear();
		
		valueField.put((valueField.get() + 1) % 11);
		changedBitSet.set(valueField.getFieldOffset());
		
		timeStamp.getCurrentTime();
		timeStampField.set(timeStamp);
		changedBitSet.set(timeStampFieldOffset);
		notifyListeners(changedBitSet);
	}

	@Override
	public void callback() {
		// TODO this causes deadlock !!! with topStructure
	//	lock();
		try
		{
			process();
		} 
		finally
		{
	//		unlock();
		}
	}

	@Override
	public void timerStopped() {
	}

	public void cancel()
	{
		if (timerNode != null)
			timerNode.cancel();
	}
}