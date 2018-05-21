/**
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient.sample;

import java.time.Duration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.epics.gpclient.GPClient;
import org.epics.gpclient.PVReader;
import org.epics.vtype.VType;
import static org.epics.gpclient.GPClient.*;
import org.epics.gpclient.PV;

/**
 *
 * @author carcassi
 */
public class BasicExamples {

    public static void simpleRead() {
        PVReader<VType> pv = GPClient.read("sim://noise")
                .addReadListener((event, p) -> {
                    System.out.println(event + " " + p.isConnected() + " " + p.getValue());
                })
                .start();

        pause(2000);

        pv.close();
    }

    public static void simpleReadAndWrite() {
        PV<VType, Object> pv = GPClient.readAndWrite(channel("loc://a(\"init\")"))
                .addListener((event, p) -> {
                    System.out.println(event + " " + p.isConnected() + " " + p.isWriteConnected() + " " + p.getValue());
                })
                .start();

        pause(1000);

        pv.write(null);
        pause(100);
        pv.write("final");

        pause(1000);

        pv.close();
    }

    public static void connectionTimeout() {
        PVReader<VType> pv = GPClient.read("sim://delayedConnectionChannel(2, \"init\")")
                .addReadListener((event, pvReader) -> {
                    System.out.println(event + " " + pvReader.isConnected() + " " + pvReader.getValue());
                })
                .connectionTimeout(Duration.ofSeconds(1))
                .start();

        pause(2500);

        pv.close();
    }

    public static void readAllValues() {
        PVReader<List<VType>> pv = GPClient.read(channel("sim://noise", queueAllValues(VType.class)))
                .addReadListener((event, pvReader) -> {
                    System.out.println(event + " " + pvReader.isConnected() + " " + pvReader.getValue());
                })
                .start();

        pause(2000);

        pv.close();
    }

    public static void readLatestValueBurst() {
        PVReader<VType> pv = GPClient.read("sim://noise(-5,5,0.01)")
                .addReadListener((event, p) -> {
                    System.out.println(event + " " + p.isConnected() + " " + p.getValue());
                })
                .start();

        pause(2000);

        pv.close();
    }

    public static void readAllValuesBurst() {
        PVReader<List<VType>> pv = GPClient.read(channel("sim://noise(-5,5,0.01)", queueAllValues(VType.class)))
                .addReadListener((event, pvReader) -> {
                    System.out.println(event + " " + pvReader.isConnected() + " " + pvReader.getValue());
                })
                .start();

        pause(2000);

        pv.close();
    }

    public static void run(String title, Runnable test) {
        System.out.println(title);
        System.out.println(title.replaceAll(".", "-"));
        test.run();
        System.out.println("");
    }

    public static void pause(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Logger.getLogger(BasicExamples.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) {
        run("Simple read", BasicExamples::simpleRead);
        run("Simple read and write", BasicExamples::simpleReadAndWrite);
        run("Connection timeout", BasicExamples::connectionTimeout);
        run("Read all values", BasicExamples::readAllValues);
        run("Read latest value (burst)", BasicExamples::readLatestValueBurst);
        run("Read all values (burst)", BasicExamples::readAllValuesBurst);
        
        GPClient.defaultInstance().getDefaultDataSource().close();
    }
}
