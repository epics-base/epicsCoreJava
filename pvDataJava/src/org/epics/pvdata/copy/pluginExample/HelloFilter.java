
/*
 * The License for this software can be found in the file LICENSE that is included with the distribution.
 */
package org.epics.pvdata.copy.pluginExample;

import org.epics.pvdata.copy.PVFilter;
import org.epics.pvdata.factory.ConvertFactory;
import org.epics.pvdata.misc.BitSet;
import org.epics.pvdata.pv.Convert;
import org.epics.pvdata.pv.*;
/**
 * A filter that says hello with the full fieldName.
 * @author mrk
 * @date 2017.02.23
 */
public class  HelloFilter implements PVFilter{
	static final Convert convert = ConvertFactory.getConvert();
	boolean sayHello;
	
	HelloFilter(boolean sayHello,PVField pvField)
	{
		this.sayHello = sayHello;
	}
	
	/* (non-Javadoc)
	 * @see org.epics.pvdata.copy.PVFilter#filter(org.epics.pvdata.pv.PVField, org.epics.pvdata.misc.BitSet)
	 */
	public boolean filter(PVField pvCopy,BitSet bitSet)
	{
		if(sayHello) {
			System.out.println("hello " + pvCopy.getFullName());
		}
		return false;
	}
	/* (non-Javadoc)
	 * @see org.epics.pvdata.copy.PVFilter#getName()
	 */
	public String getName()
	{
		return HelloPlugin.name;
	}
}
