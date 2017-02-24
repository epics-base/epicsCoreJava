
/*
 * The License for this software can be found in the file LICENSE that is included with the distribution.
 */
package org.epics.pvdata.copy.arrayPlugin;

import org.epics.pvdata.copy.PVFilter;
import org.epics.pvdata.factory.ConvertFactory;
import org.epics.pvdata.misc.BitSet;
import org.epics.pvdata.pv.Convert;
import org.epics.pvdata.pv.*;
/**
 * A Plugin for a filter that gets a sub array from a PVScalarArray.
 * @author mrk
 * @date 2017.02.23
 */
public class  ArrayFilter implements PVFilter{
	static final Convert convert = ConvertFactory.getConvert();
	PVScalarArray masterArray;
	int start;
	int increment;
	int end;
	
	ArrayFilter(int start,int increment,int end,PVScalarArray masterArray)
	{
		this.start = start;
		this.increment = increment;
		this.end = end;
		this.masterArray = masterArray;
	}
	
	/* (non-Javadoc)
	 * @see org.epics.pvdata.copy.PVFilter#filter(org.epics.pvdata.pv.PVField, org.epics.pvdata.misc.BitSet)
	 */
	public boolean filter(PVField pvCopy,BitSet bitSet)
	{
		PVScalarArray copyArray = (PVScalarArray)(pvCopy);
		int len = 0;
		int start = this.start;
		int end = this.end;
		int no_elements = masterArray.getLength();
		if(start<0) start = no_elements+start;
		if(start<0) start = 0;
		if(start>no_elements) start = no_elements;
		if (end < 0) end = no_elements + end;
	    if (end < 0) end = 0;
	    if (end >= no_elements) end = no_elements - 1;

	    if (end - start >= 0) len = 1 + (end - start) / increment;
	    if(len<=0) return false;
		int indfrom = start;
		int indto = 0;
		copyArray.setCapacity(len);
		if(increment==1) {
			convert.copyScalarArray(masterArray,indfrom,copyArray,indto,len);
		} else {
			for(int i=0; i<len; ++i) {
				convert.copyScalarArray(masterArray,indfrom,copyArray,indto,1);
				indfrom += increment;
				indto += 1;
			}
		}
		copyArray.setLength(len);
		bitSet.set(pvCopy.getFieldOffset());
		return true;
	}
	/* (non-Javadoc)
	 * @see org.epics.pvdata.copy.PVFilter#getName()
	 */
	public String getName()
	{
		return ArrayPlugin.name;
	}
}
