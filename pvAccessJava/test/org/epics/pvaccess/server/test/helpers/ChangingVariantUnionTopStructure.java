package org.epics.pvaccess.server.test.helpers;

import org.epics.util.compat.legacy.lang.Random;

import org.epics.pvaccess.PVFactory;
import org.epics.pvdata.factory.StandardFieldFactory;
import org.epics.pvdata.factory.StandardPVFieldFactory;
import org.epics.pvdata.misc.BitSet;
import org.epics.pvdata.misc.Timer;
import org.epics.pvdata.misc.Timer.TimerCallback;
import org.epics.pvdata.misc.Timer.TimerNode;
import org.epics.pvdata.misc.TimerFactory;
import org.epics.pvdata.property.PVTimeStamp;
import org.epics.pvdata.property.PVTimeStampFactory;
import org.epics.pvdata.property.TimeStamp;
import org.epics.pvdata.property.TimeStampFactory;
import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.PVByteArray;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVDouble;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVUnion;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Structure;

public class ChangingVariantUnionTopStructure extends PVTopStructure implements TimerCallback
{
    private static final FieldCreate fieldCreate = PVFactory.getFieldCreate();
    private static final PVDataCreate pvDataCreate = PVFactory.getPVDataCreate();

    private final PVUnion valueField;
	private final int timeStampFieldOffset;
	private final PVTimeStamp timeStampField;
	private final TimerNode timerNode;
	private int counter = 0;
	private final Random random = new Random(System.currentTimeMillis());

	private final TimeStamp timeStamp = TimeStampFactory.create();

	private final BitSet changedBitSet;

	private static final Structure structure =
		fieldCreate.createStructure(new String[] { "value", "timeStamp" },
									new Field[] {
										fieldCreate.createVariantUnion(),
										StandardFieldFactory.getStandardField().timeStamp()
									}
		);

	public ChangingVariantUnionTopStructure(double scanPeriodHz, Timer timer) {
		super(structure);

		changedBitSet = new BitSet(getPVStructure().getNumberFields());

		valueField = getPVStructure().getUnionField("value");

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

		PVField value = null;
		switch (counter++ % 4)
		{
		case 0: value = null; break;
		case 1: value = pvDataCreate.createPVScalar(ScalarType.pvDouble); ((PVDouble)value).put(random.nextDouble()); break;
		case 2: value = pvDataCreate.createPVScalarArray(ScalarType.pvByte); ((PVByteArray)value).setLength(random.nextInt(10)); break;
		case 3: value = StandardPVFieldFactory.getStandardPVField().enumerated(new String[] { "on", "off" }); break;
		}
		valueField.set(value);
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
