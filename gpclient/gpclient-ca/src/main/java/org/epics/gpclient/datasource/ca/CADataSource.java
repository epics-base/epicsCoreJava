/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient.datasource.ca;

import com.cosylab.epics.caj.CAJContext;
import gov.aps.jca.CAException;
import gov.aps.jca.Context;
import gov.aps.jca.JCALibrary;
import gov.aps.jca.Monitor;
import org.epics.gpclient.datasource.ChannelHandler;
import org.epics.gpclient.datasource.DataSource;
import org.epics.gpclient.datasource.ca.types.CATypeSupport;
import org.epics.gpclient.datasource.ca.types.CAVTypeAdapterSet;

import java.util.logging.Level;
import java.util.logging.Logger;

public class CADataSource extends DataSource {

    static final Logger log = Logger.getLogger(CADataSource.class.getName());

    // pure java channel access context
    private Context context;

    // JCA context properties
    private int monitorMask = Monitor.VALUE | Monitor.ALARM;
    private boolean dbePropertySupported = false;
    private boolean varArraySupported = true;
    private boolean rtypValueOnly = false;
    private boolean honorZeroPrecision = true;

    private final CATypeSupport caTypeSupport = new CATypeSupport(new CAVTypeAdapterSet());

    public CADataSource() {
        super();
        // Some properties are not pre-initialized to the default,
        // so if they were not set, we should initialize them.

        // Default JCA context is pure Java
        try {
            JCALibrary jca = JCALibrary.getInstance();
            context = jca.createContext(JCALibrary.CHANNEL_ACCESS_JAVA);
        } catch (CAException ex) {
            log.log(Level.SEVERE, "JCA context creation failed", ex);
            throw new RuntimeException("JCA context creation failed", ex);
        }

        try {
            if (context instanceof CAJContext) {
                ((CAJContext) context).setDoNotShareChannels(true);
            }
        } catch (Throwable t) {
            log.log(Level.WARNING,
                    "Couldn't change CAJContext to doNotShareChannels: this may cause some rare notification problems.",
                    t);
        }

        // Default support for var array needs to be detected
        try {
            Class cajClazz = Class.forName("com.cosylab.epics.caj.CAJContext");
            if (cajClazz.isInstance(context)) {
                varArraySupported = !(context.getVersion().getMajorVersion() <= 1
                        && context.getVersion().getMinorVersion() <= 1
                        && context.getVersion().getMaintenanceVersion() <= 9);
            }
        } catch (ClassNotFoundException ex) {
            // Can't be CAJ, fall back to JCA
        }

    }

    public Context getContext() {
        return context;
    }

    public CATypeSupport getCaTypeSupport() {
        return caTypeSupport;
    }

    public int getMonitorMask() {
        return monitorMask;
    }

    public boolean isDbePropertySupported() {
        return dbePropertySupported;
    }

    public boolean isVarArraySupported() {
        return varArraySupported;
    }

    public boolean isRtypValueOnly() {
        return rtypValueOnly;
    }

    public boolean isHonorZeroPrecision() {
        return honorZeroPrecision;
    }

    @Override
    protected ChannelHandler createChannel(String channelName) {
        log.log(Level.INFO, "CREATE channel " + channelName);
        return new CAChannelHandler(channelName, this);
    }

    @Override
    public void close() {
        context.dispose();
    }

}
