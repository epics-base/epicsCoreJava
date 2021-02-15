/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient.javafx.tools;

import java.net.URL;
import org.joda.time.Duration;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import org.epics.gpclient.GPClient;
import org.epics.gpclient.PV;
import org.epics.gpclient.PVEvent;
import org.epics.gpclient.PVListener;
import org.epics.gpclient.javafx.Executors;
import org.epics.vtype.Alarm;
import org.epics.vtype.AlarmSeverity;
import org.epics.vtype.VType;

public class ProbeController implements Initializable {

    private PV<VType, Object> pv;

//    private ValueFormat format = new SimpleValueFormat(3);

    @FXML
    private TextField channelField;
    @FXML
    private TextField valueField;
    @FXML
    private TextField newValueField;
    @FXML
    private TextField errorField;
    @FXML
    private ValueViewer valueViewer;
    @FXML
    private EventLogViewer eventLogViewer;
    @FXML
    private ExpressionProbe expressionProbe;

    @FXML
    private void onChannelChanged(ActionEvent event) {
        if (pv != null) {
            pv.close();
            newValueField.setText(null);
            valueField.setText(null);
            newValueField.setEditable(false);
            newValueField.setDisable(true);
            changeValue(null, false);
            errorField.setText(null);
            expressionProbe.setExpression(null);
        }

        expressionProbe.setExpression(channelField.getText());

        pv = GPClient.readAndWrite(channelField.getText())
                .addListener(eventLogViewer.eventLog().<VType, Object>createReadListener())
                .addListener(new PVListener<VType, Object>() {
                    public void pvChanged(PVEvent event, PV<VType, Object> pv) {
                        changeValue(pv.getValue(), pv.isConnected());
                        if (event.isType(PVEvent.Type.EXCEPTION)) {
                            errorField.setText(event.getException().getMessage());
                        } else {
                            errorField.setText(null);
                        }
                        if (event.isType(PVEvent.Type.WRITE_CONNECTION)) {
                            if (pv.isWriteConnected()) {
                                newValueField.setDisable(false);
                                newValueField.setEditable(true);
                            } else {
                                newValueField.setEditable(false);
                                newValueField.setDisable(true);
                            }
                        }
                    }
                })
                .connectionTimeout(Duration.standardSeconds(2), "Still connecting...")
                .notifyOn(Executors.javaFXAT())
                .maxRate(Duration.millis(20))
                .start();
    }

    private void changeValue(Object obj, boolean connected) {
        if (obj != null) {
            // TODO format value
            valueField.setText(obj.toString());
        } else {
            valueField.setText("");
        }
        setAlarm(obj, connected);
        valueViewer.setValue(obj, connected);
    }

    private static final Map<AlarmSeverity, Border> BORDER_MAP = createBorderMap();

    private static Map<AlarmSeverity, Border> createBorderMap() {
        Map<AlarmSeverity, Border> map = new EnumMap<AlarmSeverity, Border>(AlarmSeverity.class);
        map.put(AlarmSeverity.NONE, null);
        map.put(AlarmSeverity.MINOR, new Border(new BorderStroke(Color.YELLOW, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(2))));
        map.put(AlarmSeverity.MAJOR, new Border(new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(2))));
        map.put(AlarmSeverity.INVALID, new Border(new BorderStroke(Color.PURPLE, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(2))));
        map.put(AlarmSeverity.UNDEFINED, new Border(new BorderStroke(Color.PURPLE, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(2))));
        return Collections.unmodifiableMap(map);
    }

    private void setAlarm(Object value, boolean connected) {
        Alarm alarm = Alarm.alarmOf(value, connected);
        valueField.setBorder(BORDER_MAP.get(alarm.getSeverity()));
    }

    @FXML
    private void onNewValueChanged(ActionEvent event) {
        pv.write(newValueField.getText());
    }

    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }
}
