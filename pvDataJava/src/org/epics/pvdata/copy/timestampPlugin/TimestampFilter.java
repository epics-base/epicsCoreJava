
/*
 * The License for this software can be found in the file LICENSE that is included with the distribution.
 */
package org.epics.pvdata.copy.timestampPlugin;

import org.epics.pvdata.copy.PVFilter;
import org.epics.pvdata.misc.BitSet;
import org.epics.pvdata.property.PVTimeStamp;
import org.epics.pvdata.property.PVTimeStampFactory;
import org.epics.pvdata.property.TimeStamp;
import org.epics.pvdata.property.TimeStampFactory;
import org.epics.pvdata.pv.PVField;
/**
 * A filter that sets a timeStamp to the current time.
 * @author mrk
 * @date 2017.02.23
 */
public class  TimestampFilter implements PVFilter{
	private PVTimeStamp pvTimeStamp = PVTimeStampFactory.create();
    private TimeStamp timeStamp = TimeStampFactory.create();
	boolean current;
	
	TimestampFilter(boolean current,PVField pvField)
	{
		this.current = current;
	}
	/**
	 * Update copy from master.
	 * @return (true,false) if copy (was, was not changed).
	 */
	public boolean filter(PVField pvCopy,BitSet bitSet)
	{
		if(current) {		
	        if(!pvTimeStamp.attach(pvCopy)) return false;
	        timeStamp.getCurrentTime();
            pvTimeStamp.set(timeStamp);
            bitSet.set(pvCopy.getFieldOffset());
            return true;
		}
		return false;
	}
	/* (non-Javadoc)
	 * @see org.epics.pvdata.copy.PVFilter#getName()
	 */
	public String getName()
	{
		return TimestampPlugin.name;
	}
}
