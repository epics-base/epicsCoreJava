package org.epics.ca.server.test.helpers;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.epics.ca.PVFactory;
import org.epics.ca.client.Lockable;
import org.epics.pvData.factory.StandardFieldFactory;
import org.epics.pvData.misc.BitSet;
import org.epics.pvData.pv.Field;
import org.epics.pvData.pv.FieldCreate;
import org.epics.pvData.pv.MessageType;
import org.epics.pvData.pv.PVDataCreate;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.StandardField;

public class PVTopStructure implements Lockable
{
	public interface PVTopStructureListener {
		public void topStructureChanged(BitSet changedBitSet);
	}
	
    private static final FieldCreate fieldCreate = PVFactory.getFieldCreate();
    private static final PVDataCreate pvDataCreate = PVFactory.getPVDataCreate();

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
			// TODO access via PVFactory?
			StandardField standardField = StandardFieldFactory.getStandardField();
				
	        Field[] fields = new Field[3];
	        fields[0] = valueType;
	        fields[1] = standardField.timeStamp();
	        fields[2] = standardField.doubleAlarm();
	        
	        pvStructure = pvDataCreate.createPVStructure(null,
	        		fieldCreate.createStructure(new String[] { "value", "timeStamp", "alarm" }, fields)
	        );
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