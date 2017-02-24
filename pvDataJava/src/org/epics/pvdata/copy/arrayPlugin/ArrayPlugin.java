package org.epics.pvdata.copy.arrayPlugin;

import org.epics.pvdata.copy.PVCopy;
import org.epics.pvdata.copy.PVFilter;
import org.epics.pvdata.copy.PVPlugin;
import org.epics.pvdata.copy.PVPluginRegistry;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVScalarArray;
import org.epics.pvdata.pv.Type;
/**
 * A filter that gets a sub array from a PVScalarArray.
 * @author mrk
 * @date 2017.02.23
 */
public class ArrayPlugin implements PVPlugin
{
	static String name = "array";
	
	/**
	 * Constructor
	 */
	public ArrayPlugin()
	{
		PVPluginRegistry.registerPlugin(name,this);
	}
	
	/* (non-Javadoc)
	 * @see org.epics.pvdata.copy.PVPlugin#create(java.lang.String, org.epics.pvdata.copy.PVCopy, org.epics.pvdata.pv.PVField)
	 */
	public PVFilter create(String requestValue,PVCopy pvCopy,PVField master)
	{
		Type type = master.getField().getType();
		if(type!=Type.scalarArray) {
			return null;
		}
		int start =0;
		int increment =1;
		int end = -1;
		String[] values = requestValue.split(":");
		int num = values.length;
		if(num==1) {
			String value = values[0];
			try{
				int result = Integer.parseInt(value);
				start = result;
		    }catch(NumberFormatException e){}
		}
		if(num==2){
			String value = values[0];
			try{
				int result = Integer.parseInt(value);
				start = result;
		    }catch(NumberFormatException e){}
			value = values[1];
			try{
				int result = Integer.parseInt(value);
				end = result;
		    }catch(NumberFormatException e){}
		}
		if(num==3){
			String value = values[0];
			try{
				int result = Integer.parseInt(value);
				start = result;
		    }catch(NumberFormatException e){}
			value = values[1];
			try{
				int result = Integer.parseInt(value);
				if(result>0) increment = result;
		    }catch(NumberFormatException e){}
			value = values[2];
			try{
				int result = Integer.parseInt(value);
				end = result;
		    }catch(NumberFormatException e){}
		}
		return new ArrayFilter(start,increment,end,(PVScalarArray)(master));
	}

}
