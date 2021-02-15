package org.epics.pvaccess.server.test.helpers;

import org.epics.pvaccess.PVFactory;
import org.epics.pvdata.misc.BitSet;
import org.epics.pvdata.misc.Timer;
import org.epics.pvdata.misc.Timer.TimerCallback;
import org.epics.pvdata.misc.Timer.TimerNode;
import org.epics.pvdata.misc.TimerFactory;
import org.epics.pvdata.property.PVTimeStamp;
import org.epics.pvdata.property.PVTimeStampFactory;
import org.epics.pvdata.property.TimeStamp;
import org.epics.pvdata.property.TimeStampFactory;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVInt;
import org.epics.pvdata.pv.ScalarType;

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
		super(fieldCreate.createScalar(ScalarType.pvInt));

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
	 * @see org.epics.pvaccess.server.test.TestChannelProviderImpl.PVTopStructure#process()
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

	public void timerStopped() {
	}

	public void cancel()
	{
		if (timerNode != null)
			timerNode.cancel();
	}
}
