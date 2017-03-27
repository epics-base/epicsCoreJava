/*
 * The License for this software can be found in the file LICENSE that is included with the distribution.
 */
package org.epics.pvdata.copy;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author mrk
 * @since 2017.02.23
 * 
 * A registry for filter plugins for PVCopy.
 *
 */
public class PVPluginRegistry {
    private static final Map<String,PVPlugin> pluginMap = new TreeMap<String,PVPlugin>();
    /**
     * Register a plugin.
     * @param name The name that appears in [name=value] of a field request option.
     * @param pvPlugin The implementation for the plugin.
     */
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
    /**
     * Find a plugin.
     * @param name The name that appears in [name=value] of a field request option.
     * @return The plugin implementation or null if no pluging by that name has been registered.
     */
    public static PVPlugin find(String name)
    {
        synchronized(pluginMap) {
            return pluginMap.get(name);

        }
    }
}
