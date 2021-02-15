/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient.sample;

import org.epics.gpclient.*;
import org.joda.time.Duration;
import java.util.List;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.epics.vtype.VType;
import static org.epics.gpclient.GPClient.*;

/**
 *
 * @author carcassi
 */
public class BasicExamples {

    public static void simpleReadOnce() {
        Future<VType> value = GPClient.readOnce("sim://noise");

        try {
            System.out.println("Value " + value.get());
        } catch (Exception ex) {
            Logger.getLogger(BasicExamples.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void simpleRead() {
        PVReader<VType> pv = GPClient.read("sim://noise")
                .addReadListener(new PVReaderListener<VType>() {
                    public void pvChanged(PVEvent event, PVReader<VType> pvReader) {
                        System.out.println(event + " " + pvReader.isConnected() + " " + pvReader.getValue());
                    }
                })
                .start();

        pause(2000);

        pv.close();
    }

    public static void simpleReadAndWrite() {
        PV<VType, Object> pv = readAndWrite(channel("loc://a(\"init\")"))
                .addListener(new PVListener<VType, Object>() {
                    public void pvChanged(PVEvent event, PV<VType, Object> pv) {
                        System.out.println(event + " " + pv.isConnected() + " " + pv.isWriteConnected() + " " + pv.getValue());
                    }
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
                .addReadListener(new PVReaderListener<VType>() {
                    public void pvChanged(PVEvent event, PVReader<VType> pvReader) {
                        System.out.println(event + " " + pvReader.isConnected() + " " + pvReader.getValue());
                    }
                })
                .connectionTimeout(Duration.standardSeconds(1))
                .start();

        pause(2500);

        pv.close();
    }

    public static void readAllValues() {
        PVReader<List<VType>> pv = GPClient.read(channel("sim://noise", queueAllValues(VType.class)))
                .addReadListener(new PVReaderListener<List<VType>>() {
                    public void pvChanged(PVEvent event, PVReader<List<VType>> pvReader) {
                        System.out.println(event + " " + pvReader.isConnected() + " " + pvReader.getValue());
                    }
                })
                .start();

        pause(2000);

        pv.close();
    }

    public static void readLatestValueBurst() {
        PVReader<VType> pv = GPClient.read("sim://noise(-5,5,0.01)")
                .addReadListener(new PVReaderListener<VType>() {
                    public void pvChanged(PVEvent event, PVReader<VType> pvReader) {
                        System.out.println(event + " " + pvReader.isConnected() + " " + pvReader.getValue());
                    }
                })
                .start();

        pause(2000);

        pv.close();
    }

    public static void readAllValuesBurst() {
        PVReader<List<VType>> pv = GPClient.read(channel("sim://noise(-5,5,0.01)", queueAllValues(VType.class)))
                .addReadListener(new PVReaderListener<List<VType>>() {
                    public void pvChanged(PVEvent event, PVReader<List<VType>> pvReader) {
                        System.out.println(event + " " + pvReader.isConnected() + " " + pvReader.getValue());
                    }
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
        run("Simple read once", new Runnable() {
            public void run() {
                simpleReadOnce();
            }
        });
        run("Simple read", new Runnable() {
            public void run() {
                simpleRead();
            }
        });
        run("Simple read and write", new Runnable() {
            public void run() {
                simpleReadAndWrite();
            }
        });
        run("Connection timeout", new Runnable() {
            public void run() {
                connectionTimeout();
            }
        });
        run("Read all values", new Runnable() {
            public void run() {
                readAllValues();
            }
        });
        run("Read latest value (burst)", new Runnable() {
            public void run() {
                readLatestValueBurst();
            }
        });
        run("Read all values (burst)", new Runnable() {
            public void run() {
                readAllValuesBurst();
            }
        });

        GPClient.defaultInstance().getDefaultDataSource().close();
    }
}
