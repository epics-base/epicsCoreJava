/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.epics.gpclient.sample;

import org.epics.gpclient.GPClient;
import org.epics.gpclient.PVEvent;
import org.epics.gpclient.PVReader;
import org.epics.vtype.VType;
import static org.epics.gpclient.GPClient.*;
import org.epics.gpclient.PV;

/**
 *
 * @author carcassi
 */
public class BasicExamples {

    public static void b1_readLatestValue() throws Exception {
        PVReader<VType> pv = GPClient.read("pva://TST:I")
                .addListener((PVEvent event, PVReader<VType> pvReader) -> {
                    System.out.println(event + " " + pvReader.isConnected() + " " + pvReader.getValue());
                })
                .start();

        Thread.sleep(2000);

        GPClient.defaultInstance().getDefaultDataSource().close();
    }

    public static void b1_readAndWriteLoc() throws Exception {
        PV<VType, Object> pv = GPClient.readAndWrite(channel("loc://a(\"init\")"))
                .addListener((PVEvent event, PV<VType, Object> pvReader) -> {
                    System.out.println(event + " " + pvReader.isConnected() + " " + pvReader.getValue());
                    if (event.isType(PVEvent.Type.EXCEPTION)) {
                        event.getException().printStackTrace();
                    }
                })
                .start();

        Thread.sleep(1000);
        
        pv.write("New Value");

        GPClient.defaultInstance().getDefaultDataSource().close();
    }

    public static void main(String[] args) throws Exception {
        b1_readAndWriteLoc();
    }
}
