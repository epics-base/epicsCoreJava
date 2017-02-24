/*
 * The License for this software can be found in the file LICENSE that is included with the distribution.
 */
package org.epics.pvdata.copy;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author mrk
 * @date 2017.02.23
 * 
 * A registry for filter plugins for PVCopy.
 *
 */
public class PVPluginRegistry {
	private static final Map<String,PVPlugin> pluginMap = new TreeMap<String,PVPlugin>();
	
	public static void registerPlugin(String name,PVPlugin pvPlugin)
	{
		synchronized(pluginMap) {
			PVPlugin pv = pluginMap.get(name);
			if(pv!=null) {
				return;
			}
			pluginMap.put(name,pvPlugin);
        }
	}
	
	public static PVPlugin find(String name)
	{
		synchronized(pluginMap) {
			return pluginMap.get(name);
			
        }
	}
}
