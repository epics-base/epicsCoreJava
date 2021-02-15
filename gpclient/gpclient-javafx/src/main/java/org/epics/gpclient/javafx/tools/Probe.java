/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient.javafx.tools;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;


public class Probe extends VBox {

    public Probe() {
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("Probe.fxml"));

        fxmlLoader.setRoot(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static void main(String[] args) {
        JavaFXLaunchUtil.launch("Probe", Probe.class, args);
    }

}
