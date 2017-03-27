/*
 * The License for this software can be found in the file LICENSE that is included with the distribution.
 */
package org.epics.pvdata.copy.deadbandPlugin;

import org.epics.pvdata.copy.PVCopy;
import org.epics.pvdata.copy.PVFilter;
import org.epics.pvdata.copy.PVPlugin;
import org.epics.pvdata.copy.PVPluginRegistry;
import org.epics.pvdata.pv.PVField;
/**
 * A plugin for a filter that provides a deadband for a numeric scalar field.
 * @author mrk
 * @since date 2017.02.23
 */
public class DeadbandPlugin implements PVPlugin
{
    static String name = "deadband";
    /**
     * Constructor
     */
    public DeadbandPlugin()
    {
        PVPluginRegistry.registerPlugin(name,this);
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.copy.PVPlugin#create(java.lang.String, org.epics.pvdata.copy.PVCopy, org.epics.pvdata.pv.PVField)
     */
    public PVFilter create(String requestValue,PVCopy pvCopy,PVField master)
    {
        return DeadbandFilter.create(requestValue,master);
    }
}
