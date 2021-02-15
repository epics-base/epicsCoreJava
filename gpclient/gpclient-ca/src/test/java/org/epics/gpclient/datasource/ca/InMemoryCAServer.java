package org.epics.gpclient.datasource.ca;

import com.cosylab.epics.caj.cas.util.DefaultServerImpl;
import com.cosylab.epics.caj.cas.util.MemoryProcessVariable;
import gov.aps.jca.CAException;
import gov.aps.jca.JCALibrary;
import gov.aps.jca.cas.ServerContext;
import gov.aps.jca.dbr.DBR_Double;
import gov.aps.jca.dbr.DBR_Float;
import gov.aps.jca.dbr.DBR_Int;
import gov.aps.jca.dbr.DBR_String;
import org.epics.util.compat.legacy.lang.Random;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Create an inmemory channel access server
 *
 * @author Kunal Shroff
 *
 */
public class InMemoryCAServer {

    /**
     * JCA server context.
     */
    private static ServerContext context = null;
    private static AtomicBoolean initialized = new AtomicBoolean(false);

    public static boolean initializeServerInstance() {
        if (!initialized.get()) {
            initialize();
        }
        return initialized.get();
    }

    public static void closeServerInstance() {
        if (initialized.get()) {
            try {
                context.destroy();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (CAException e) {
                e.printStackTrace();
            }
        }
    }

    private static final Random generator = new Random();

    static synchronized void initialize() {

        // Get the JCALibrary instance.
        JCALibrary jca = JCALibrary.getInstance();

        // Create server implementation
        DefaultServerImpl server = new DefaultServerImpl();

        // Create a context with default configuration values.
        try {
            context = jca.createServerContext(JCALibrary.CHANNEL_ACCESS_SERVER_JAVA, server);
        } catch (CAException e) {
            e.printStackTrace();
        }

        // Display basic information about the context.
        System.out.println(context.getVersion().getVersionString());
        context.printInfo();
        System.out.println();

        // register process variables
        registerProcessVariables(server);
        initialized.set(true);
    }

    static void registerProcessVariables(DefaultServerImpl server) {
        for (int i = 0; i < 10000; i++) {
            createDoubleProcessVariable("test_double_" + i, generator.nextDouble(-10, 10), server);
            createIntProcessVariable("test_int_" + i, generator.nextInt(-10, 10), server);
            createFloatProcessVariable("test_long_" + i, generator.nextFloat(), server);
            createStringProcessVariable("test_String_" + i, String.valueOf(i), server);
        }
    }

    static void createDoubleProcessVariable(String name, double value, DefaultServerImpl server) {
        // PV supporting all GR/CTRL info
        MemoryProcessVariable mpv = new MemoryProcessVariable(name, null, DBR_Double.TYPE, new double[] { value });

        mpv.setUpperDispLimit(new Double(10));
        mpv.setLowerDispLimit(new Double(-10));

        mpv.setUpperAlarmLimit(new Double(9));
        mpv.setLowerAlarmLimit(new Double(-9));

        mpv.setUpperCtrlLimit(new Double(8));
        mpv.setLowerCtrlLimit(new Double(-8));

        mpv.setUpperWarningLimit(new Double(7));
        mpv.setLowerWarningLimit(new Double(-7));

        mpv.setUnits("units");
        mpv.setPrecision((short) 3);

        server.registerProcessVaribale(mpv);
    }

    static void createIntProcessVariable(String name, int value, DefaultServerImpl server) {
        // PV supporting all GR/CTRL info
        MemoryProcessVariable mpv = new MemoryProcessVariable(name, null, DBR_Int.TYPE, new int[] { value });

        mpv.setUpperDispLimit(new Double(10));
        mpv.setLowerDispLimit(new Double(-10));

        mpv.setUpperAlarmLimit(new Double(9));
        mpv.setLowerAlarmLimit(new Double(-9));

        mpv.setUpperCtrlLimit(new Double(8));
        mpv.setLowerCtrlLimit(new Double(-8));

        mpv.setUpperWarningLimit(new Double(7));
        mpv.setLowerWarningLimit(new Double(-7));

        mpv.setUnits("units");
        mpv.setPrecision((short) 3);

        server.registerProcessVaribale(mpv);
    }

    static void createFloatProcessVariable(String name, float value, DefaultServerImpl server) {
        // PV supporting all GR/CTRL info
        MemoryProcessVariable mpv = new MemoryProcessVariable(name, null, DBR_Float.TYPE, new float[] { value });

        mpv.setUpperDispLimit(new Double(10));
        mpv.setLowerDispLimit(new Double(-10));

        mpv.setUpperAlarmLimit(new Double(9));
        mpv.setLowerAlarmLimit(new Double(-9));

        mpv.setUpperCtrlLimit(new Double(8));
        mpv.setLowerCtrlLimit(new Double(-8));

        mpv.setUpperWarningLimit(new Double(7));
        mpv.setLowerWarningLimit(new Double(-7));

        mpv.setUnits("units");
        mpv.setPrecision((short) 3);

        server.registerProcessVaribale(mpv);
    }

    static void createStringProcessVariable(String name, String value, DefaultServerImpl server) {
        // PV supporting all GR/CTRL info
        MemoryProcessVariable mpv = new MemoryProcessVariable(name, null, DBR_String.TYPE, new String[] { value });
        server.registerProcessVaribale(mpv);
    }

    public static void main(String[] args) throws IllegalStateException, CAException {
        InMemoryCAServer.initializeServerInstance();
        try {
            context.run(0);
            Thread.sleep(30000);
            context.printInfo(System.out);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (CAException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            context.destroy();
        }
    }

}
