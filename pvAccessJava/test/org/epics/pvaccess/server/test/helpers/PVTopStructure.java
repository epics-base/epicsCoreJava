package org.epics.pvaccess.server.test.helpers;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.epics.pvaccess.PVFactory;
import org.epics.pvaccess.client.Lockable;
import org.epics.pvdata.factory.StandardFieldFactory;
import org.epics.pvdata.misc.BitSet;
import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.MessageType;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.StandardField;

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
	        
	        pvStructure = pvDataCreate.createPVStructure(fieldCreate.createStructure(new String[] { "value", "timeStamp", "alarm" }, fields)
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
