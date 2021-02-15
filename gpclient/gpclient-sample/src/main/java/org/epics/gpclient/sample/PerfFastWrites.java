/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient.sample;

import java.util.ArrayList;
import java.util.List;
import org.epics.util.compat.legacy.lang.Random;
import java.util.concurrent.atomic.AtomicInteger;
import org.epics.gpclient.GPClient;
import org.epics.gpclient.PV;
import org.epics.gpclient.PVEvent;
import org.epics.gpclient.PVListener;
import org.epics.vtype.VType;

/**
 * This example can be used to test how man write operations can be performed,
 * what is the memory usage profile and that all writes are actually
 * performed.
 *
 * As of 5/1/2018, on Windows 10, Xeon E3-1505M v6, Java 1.8.0_515,
 * 100,000 pvs total. 330,000 writes/sec. Memory usage oscillates between
 * 350MB and 950 MB. This means roughly 3.5KB per pv and 1.2KB per write.
 */
public class PerfFastWrites {
    public static void main(String[] args) throws Exception {
        int nPvs = 100000;
        List<PV> pvs = new ArrayList<PV>();
        Random rand = new Random();

        // Counters
        final AtomicInteger count = new AtomicInteger();
        final AtomicInteger valueCount = new AtomicInteger();
        final AtomicInteger writeCount = new AtomicInteger();

        // Create all the pvs
        for (int i = 0; i < nPvs; i++) {
            pvs.add(GPClient.readAndWrite("loc://test"+i)
                    .addListener(new PVListener<VType, Object>() {
                        public void pvChanged(PVEvent event, PV<VType, Object> pv) {
                            if (event.isType(PVEvent.Type.READ_CONNECTION)) {
                                count.incrementAndGet();
                            }
                            if (event.isType(PVEvent.Type.VALUE)) {
                                valueCount.incrementAndGet();
                            }
                            if (event.isType(PVEvent.Type.WRITE_SUCCEEDED)) {
                                writeCount.incrementAndGet();
                            }
                            if (event.isType(PVEvent.Type.WRITE_FAILED)) {
                                event.getWriteError().printStackTrace();
                            }
                        }
                    }).start());
        }

        // Parameters for writes
        int k = 0;
        int nPeriods = 200;
        int nWritesPerPeriod = (int) (nPvs * 0.7);
        int pauseMs = 20;
        int writtenCount = 0;
        long start = System.currentTimeMillis();

        // Submit writes
        for (int i = 0; i < nPeriods; i++) {
            System.out.println("C" + count + " - " + " R" + valueCount + " W" + writeCount);

            // Start writing only when all pvs are created
            if (count.get() == nPvs) {
                for (int j = 0; j < nWritesPerPeriod; j++) {
                    if (pvs.get(k).isWriteConnected()) {
                        pvs.get(k).write(rand.nextInt(10));
                        writtenCount++;
                    }
                    k++;
                    if (k == pvs.size()) {
                        k = 0;
                    }
                }
            }
            Thread.sleep(pauseMs);
        }
        long end = System.currentTimeMillis();
        long elapsed = (end - start) / 1000;

        System.out.println("Submitted " + writtenCount + " at a rate of " + writtenCount / elapsed + "writes/sec");

        // Wait for all writes to conclude
        Thread.sleep(2000);
        System.out.println("C" + count + " - " + " R" + valueCount + " W" + writeCount);

        // Close all pvs
        for (PV pv : pvs) {
            pv.close();
        }
        System.out.println("C" + count + " - " + " R" + valueCount + " W" + writeCount);
        System.exit(0);
    }
}
