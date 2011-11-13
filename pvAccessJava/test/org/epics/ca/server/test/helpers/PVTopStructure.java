package org.epics.ca.server.test.helpers;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.epics.ca.client.Lockable;
import org.epics.pvData.factory.FieldFactory;
import org.epics.pvData.factory.PVDataFactory;
import org.epics.pvData.misc.BitSet;
import org.epics.pvData.pv.Field;
import org.epics.pvData.pv.FieldCreate;
import org.epics.pvData.pv.MessageType;
import org.epics.pvData.pv.PVDataCreate;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.ScalarType;

public class PVTopStructure implements Lockable
{
	public interface PVTopStructureListener {
		public void topStructureChanged(BitSet changedBitSet);
	}
	
    private static final FieldCreate fieldCreate = FieldFactory.getFieldCreate();
    private static final PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();

    private final Lock lock = new ReentrantLock();
	private final PVStructure pvStructure;
	private final ArrayList<PVTopStructureListener> listeners = new ArrayList<PVTopStructureListener>();
	
	public PVTopStructure(Field valueType)
	{
		if (valueType == null)
		{
			pvStructure = null;
		}
		else
		{
			// TODO use PVStandard when available
			
			PVStructure timeStampStructure;
			{
		        Field[] fields = new Field[3];
		        fields[0] = fieldCreate.createScalar("secondsPastEpoch", ScalarType.pvLong);
		        fields[1] = fieldCreate.createScalar("nanoSeconds", ScalarType.pvInt);
		        fields[2] = fieldCreate.createScalar("userTag", ScalarType.pvInt);
		        timeStampStructure = pvDataCreate.createPVStructure(null, "timeStamp", fields);
			}
		
			PVStructure alarmStructure;
			{
		        Field[] fields = new Field[3];
		        fields[0] = fieldCreate.createScalar("severity", ScalarType.pvInt);
		        fields[1] = fieldCreate.createScalar("status", ScalarType.pvInt);
		        fields[2] = fieldCreate.createScalar("message", ScalarType.pvString);
		        alarmStructure = pvDataCreate.createPVStructure(null, "alarm", fields);
			}
			
	        Field[] fields = new Field[3];
	        fields[0] = fieldCreate.create("value", valueType);
	        fields[1] = timeStampStructure.getField();
	        fields[2] = alarmStructure.getField();
	        
	        pvStructure = pvDataCreate.createPVStructure(null, "", fields);
		}
	}
	
	public PVStructure getPVStructure()
	{
		return pvStructure;
	}
	
	public void process()
	{
		// default is noop
	}
	
	// TODO async
	public PVStructure request(PVStructure pvArgument)
	{
		throw new UnsupportedOperationException("not implemented");
	}
	
	
	public void lock()
	{
		lock.lock();
	}
	
	public void unlock()
	{
		lock.unlock();
	}
	
	public void registerListener(PVTopStructureListener listener)
	{
		synchronized (listeners) {
			listeners.add(listener);
		}
	}
	
   	public void unregisterListener(PVTopStructureListener listener)
	{
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}
   	
   	public void notifyListeners(BitSet changedBitSet)
	{
		synchronized (listeners) {
			for (PVTopStructureListener listener : listeners)
			{
				try {
					listener.topStructureChanged(changedBitSet);
				}
				catch (Throwable th) {
					Writer writer = new StringWriter();
					PrintWriter printWriter = new PrintWriter(writer);
					th.printStackTrace(printWriter);
					pvStructure.message("Unexpected exception caught: " + writer, MessageType.fatalError);
				}
			}
		}
	}
   	
}