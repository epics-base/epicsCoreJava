
/*
 * The License for this software can be found in the file LICENSE that is included with the distribution.
 */
package org.epics.pvdata.copy.deadbandPlugin;

import org.epics.pvdata.copy.PVFilter;
import org.epics.pvdata.factory.ConvertFactory;
import org.epics.pvdata.misc.BitSet;
import org.epics.pvdata.pv.Convert;
import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVScalar;
import org.epics.pvdata.pv.Scalar;
import org.epics.pvdata.pv.Type;
/**
 * A Plugin for a filter that gets a sub array from a PVScalarArray.
 * @author mrk
 * @since 2017.02.23
 */
public class  DeadbandFilter implements PVFilter{
	static final Convert convert = ConvertFactory.getConvert();
	PVScalar master;
	boolean absolute;
	boolean firstTime = true;
	double deadband;
	double lastReportedValue = 0.0;
	
	
	public static DeadbandFilter create(String requestValue,PVField master)
	{
		Field field = master.getField();
		Type type = field.getType();
		if(type!=Type.scalar) {
			return null;
		}
		Scalar scalar = (Scalar)field;
		if(!scalar.getScalarType().isNumeric()) return null;
		boolean absolute = false;
		if(requestValue.startsWith("abs",0)) {
			absolute = true;
		} else if(!requestValue.startsWith("rel",0)) {
			return null;
		}
		int ind = requestValue.indexOf(':');
		if(ind<=0) return null;
		String svalue = requestValue.substring(ind+1);
		double deadband = Double.parseDouble(svalue);
		return new DeadbandFilter(absolute,deadband,(PVScalar)(master));
	}
	
	private DeadbandFilter(boolean absolute,double deadband,PVScalar master)
	{
		this.master = master;
		this.absolute = absolute;
		this.deadband = deadband;
	}
	
	/* (non-Javadoc)
	 * @see org.epics.pvdata.copy.PVFilter#filter(org.epics.pvdata.pv.PVField, org.epics.pvdata.misc.BitSet, boolean)
	 */
	public boolean filter(PVField pvCopy,BitSet bitSet,boolean toCopy)
	{
		if(!toCopy) return false;
		double value = convert.toDouble(master);
		double diff = value - lastReportedValue;
		if(diff<0.0) diff = - diff;
        boolean report = true;
		if(firstTime) {
			firstTime = false;
		} else if(absolute) {
			if(diff<deadband) report = false;
		} else {
			double last = lastReportedValue;
			if(last<0.0) last = -last;
			if(last>1e-20) {
				double percent = (diff/last)*100.0;
				if(percent<deadband) report = false;
			}
		}
		PVScalar copy = (PVScalar)(pvCopy);
		convert.fromDouble(copy,value);
        if(report) {
        	lastReportedValue = value;
        	bitSet.set(pvCopy.getFieldOffset());
        } else {
        	bitSet.clear(pvCopy.getFieldOffset());
        }
		return true;
	}
	/* (non-Javadoc)
	 * @see org.epics.pvdata.copy.PVFilter#getName()
	 */
	public String getName()
	{
		return DeadbandPlugin.name;
	}
}
