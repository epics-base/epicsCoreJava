/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient.javafx.tools;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import org.epics.gpclient.PVEvent;

public final class EventViewer extends ScrollPane {

    @FXML
    private TitledPane eventReadConnection;
    @FXML
    private CheckBox readConnectedField;
    @FXML
    private TitledPane eventReadValue;
    @FXML
    private TextField valueField;
    @FXML
    private TitledPane eventReadError;
    @FXML
    private TextField readExceptionMessageField;
    @FXML
    private TextArea readExceptionField;
    @FXML
    private TitledPane eventWriteConnection;
    @FXML
    private CheckBox writeConnectedField;
    @FXML
    private TitledPane eventWriteError;
    @FXML
    private TitledPane eventWriteSucceeded;
    @FXML
    private TitledPane eventWriteFailed;
    @FXML
    private TextField writeFailedMessageField;
    @FXML
    private TextArea writeFailedField;
    @FXML
    private TextField writeExceptionMessageField;
    @FXML
    private TextArea writeExceptionField;

    public EventViewer() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("EventViewer.fxml"));

        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        setEvent(null);
    }

    public void setEvent(Event event) {
        Event readEvent = null;
        updateReadConnection(event);
        updateReadValue(event);
        updateReadError(event);
        updateWriteConnection(event);
        updateWriteSucceeded(event);
        updateWriteFailed(event);
        updateWriteError(event);
    }

    private void updateReadConnection(Event readEvent) {
        if (readEvent != null && readEvent.getEvent().isType(PVEvent.Type.READ_CONNECTION)) {
            eventReadConnection.setVisible(true);
            eventReadConnection.setManaged(true);
            readConnectedField.setSelected(readEvent.isConnected());
        } else {
            eventReadConnection.setVisible(false);
            eventReadConnection.setManaged(false);
            readConnectedField.setSelected(false);
        }
    }

//    private ValueFormat format = new SimpleValueFormat(3);

    private void updateReadValue(Event readEvent) {
        if (readEvent != null && readEvent.getEvent().isType(PVEvent.Type.VALUE)) {
            eventReadValue.setVisible(true);
            eventReadValue.setManaged(true);
            // Value should be formatted
            //valueField.setText(format.format(readEvent.getValue()));
            valueField.setText(readEvent.getValue().toString());
        } else {
            eventReadValue.setVisible(false);
            eventReadValue.setManaged(false);
            valueField.setText(null);
        }
    }

    private void updateReadError(Event readEvent) {
        if (readEvent != null && readEvent.getEvent().isType(PVEvent.Type.EXCEPTION)) {
            eventReadError.setVisible(true);
            eventReadError.setManaged(true);
            readExceptionMessageField.setText(readEvent.getEvent().getException().getMessage());
            StringWriter sw = new StringWriter();
            readEvent.getEvent().getException().printStackTrace(new PrintWriter(sw));
            readExceptionField.setText(sw.toString());
        } else {
            eventReadError.setVisible(false);
            eventReadError.setManaged(false);
            readExceptionMessageField.setText(null);
            readExceptionField.setText(null);
        }
    }

    private void updateWriteConnection(Event writeEvent) {
        if (writeEvent != null && writeEvent.getEvent().isType(PVEvent.Type.WRITE_CONNECTION)) {
            eventWriteConnection.setVisible(true);
            eventWriteConnection.setManaged(true);
            writeConnectedField.setSelected(writeEvent.isConnected());
        } else {
            eventWriteConnection.setVisible(false);
            eventWriteConnection.setManaged(false);
            writeConnectedField.setSelected(false);
        }
    }

    private void updateWriteSucceeded(Event writeEvent) {
        if (writeEvent != null && writeEvent.getEvent().isType(PVEvent.Type.WRITE_SUCCEEDED)) {
            eventWriteSucceeded.setVisible(true);
            eventWriteSucceeded.setManaged(true);
        } else {
            eventWriteSucceeded.setVisible(false);
            eventWriteSucceeded.setManaged(false);
        }
    }

    private void updateWriteFailed(Event writeEvent) {
        if (writeEvent != null && writeEvent.getEvent().isType(PVEvent.Type.WRITE_FAILED)) {
            eventWriteFailed.setVisible(true);
            eventWriteFailed.setManaged(true);
            writeFailedMessageField.setText(writeEvent.getEvent().getWriteError().getMessage());
            StringWriter sw = new StringWriter();
            writeEvent.getEvent().getWriteError().printStackTrace(new PrintWriter(sw));
            writeFailedField.setText(sw.toString());
        } else {
            eventWriteFailed.setVisible(false);
            eventWriteFailed.setManaged(false);
            writeFailedMessageField.setText(null);
            writeFailedField.setText(null);
        }
    }

    private void updateWriteError(Event writeEvent) {
//        if (writeEvent != null && writeEvent.getEvent().isExceptionChanged()) {
//            eventWriteError.setVisible(true);
//            eventWriteError.setManaged(true);
//            writeExceptionMessageField.setText(writeEvent.getLastException().getMessage());
//            StringWriter sw = new StringWriter();
//            writeEvent.getLastException().printStackTrace(new PrintWriter(sw));
//            writeExceptionField.setText(sw.toString());
//        } else {
            eventWriteError.setVisible(false);
            eventWriteError.setManaged(false);
            writeExceptionMessageField.setText(null);
            writeExceptionField.setText(null);
//        }
    }

}
