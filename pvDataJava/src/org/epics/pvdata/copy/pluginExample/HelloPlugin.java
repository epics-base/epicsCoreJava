package org.epics.pvdata.copy.pluginExample;

import org.epics.pvdata.copy.PVCopy;
import org.epics.pvdata.copy.PVFilter;
import org.epics.pvdata.copy.PVPlugin;
import org.epics.pvdata.copy.PVPluginRegistry;
import org.epics.pvdata.pv.PVField;
/**
 * A filter that just says hello with the full field name.
 * @author mrk
 * @since 2017.02.23
 */
public class HelloPlugin implements PVPlugin
{
	static String name = "hello";
	
	/**
	 * Constructor
	 */
	public HelloPlugin()
	{
		PVPluginRegistry.registerPlugin(name,this);
	}
	
	/* (non-Javadoc)
	 * @see org.epics.pvdata.copy.PVPlugin#create(java.lang.String, org.epics.pvdata.copy.PVCopy, org.epics.pvdata.pv.PVField)
	 */
	public PVFilter create(String requestValue,PVCopy pvCopy,PVField master)
	{
		boolean sayHello = false;
		if(requestValue.equals("true")) sayHello = true;
	    return new HelloFilter(sayHello,master);
	}

}
