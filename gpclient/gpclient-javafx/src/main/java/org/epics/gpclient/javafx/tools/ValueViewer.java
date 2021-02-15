/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient.javafx.tools;

import static javafx.collections.FXCollections.emptyObservableList;
import static javafx.collections.FXCollections.unmodifiableObservableList;

import java.io.IOException;

import org.epics.vtype.Alarm;
import org.epics.vtype.Display;
import org.epics.vtype.Time;
import org.epics.vtype.VEnum;
import org.epics.vtype.VType;

import javafx.collections.FXCollections;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.PropertyValueFactory;

public final class ValueViewer extends ScrollPane {

    @FXML
    private TitledPane commonMetadata;
    @FXML
    private TextField typeField;
    @FXML
    private TextField alarmField;
    @FXML
    private TextField timeField;
    @FXML
    private TitledPane numberMetadata;
    @FXML
    private TextField displayRangeField;
    @FXML
    private TextField alarmRangeField;
    @FXML
    private TextField warningRangeField;
    @FXML
    private TextField controlRangeField;
    @FXML
    private TextField unitField;
    @FXML
    private TitledPane enumMetadata;
    @FXML
    private TitledPane tableMetadata;
    @FXML
    private TableView<VTableColumn> columnsTable;
    @FXML
    private TableColumn<VTableColumn, String> columnNameColumn;
    @FXML
    private TableColumn<VTableColumn, String> columnTypeColumn;
    @FXML
    private TableColumn<VTableColumn, Number> columnSizeColumn;
    @FXML
    private ListView<String> labelsField;
    @FXML
    private Button inspectTableButton;

    public ValueViewer() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ValueViewer.fxml"));

        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        columnNameColumn.setCellValueFactory(new PropertyValueFactory<VTableColumn, String>("name"));
        columnTypeColumn.setCellValueFactory(new PropertyValueFactory<VTableColumn, String>("type"));
        columnSizeColumn.setCellValueFactory(new PropertyValueFactory<VTableColumn, Number>("size"));

        setValue(null, false);
    }

    private Object value;

    public void setValue(Object value, boolean connection) {
        commonMetadata(value, connection);
        numberDisplay(Display.displayOf(value));
        enumMetadata(value);
        tableMetadata(value);
        this.value = value;
    }

    private void commonMetadata(Object value, boolean connection) {
        if (value == null) {
            typeField.setText(null);
            alarmField.setText(null);
            timeField.setText(null);
        } else {
            Class<?> clazz = VType.typeOf(value);
            if (clazz == null) {
                typeField.setText(null);
            } else {
                typeField.setText(clazz.getSimpleName());
            }
            alarmField.setText(Alarm.alarmOf(value, connection).toString());
            timeField.setText(Time.timeOf(value).toString());
        }
    }

    private void numberDisplay(Display display) {
        if (display == null || display.equals(Display.none())) {
            numberMetadata.setVisible(false);
            numberMetadata.setManaged(false);
        } else {
            numberMetadata.setVisible(true);
            numberMetadata.setManaged(true);
            displayRangeField.setText(display.getDisplayRange().toString());
            alarmRangeField.setText(display.getAlarmRange().toString());
            warningRangeField.setText(display.getWarningRange().toString());
            controlRangeField.setText(display.getControlRange().toString());
            unitField.setText(display.getUnit());
        }
    }

    private void enumMetadata(Object value) {
        if (value instanceof VEnum) {
            enumMetadata.setVisible(true);
            enumMetadata.setManaged(true);
            labelsField.setItems(FXCollections.observableList(((VEnum) value).getDisplay().getChoices()));
        } else {
            enumMetadata.setVisible(false);
            enumMetadata.setManaged(false);
        }
    }

    public static class VTableColumn {
        private final Object vTable;
        private final int columnIndex;

        public VTableColumn(Object vTable, int columnIndex) {
            this.vTable = vTable;
            this.columnIndex = columnIndex;
        }

        public String getName() {
//            return vTable.getColumnName(columnIndex);
            return "None";
        }

        public String getType() {
//            return vTable.getColumnType(columnIndex).getSimpleName();
            return "None";
        }

        public int getSize() {
//            Object data = vTable.getColumnData(columnIndex);
//            if (data instanceof ListNumber) {
//                return ((ListNumber) data).size();
//            } else if (data instanceof List) {
//                return ((List) data).size();
//            } else {
//                return 0;
//            }
            return 0;
        }


    }
//
    private void tableMetadata(Object value) {
//        if (value instanceof org.diirt.vtype.VTable) {
//            tableMetadata.setVisible(true);
//            tableMetadata.setManaged(true);
//            VTable vTable = (VTable) value;
//            List<VTableColumn> columns = new ArrayList<VTableColumn>();
//            for (int n = 0; n < vTable.getColumnCount(); n++) {
//                columns.add(new VTableColumn(vTable, n));
//            }
//            columnsTable.setItems(FXCollections.observableList(columns));
//        } else {
            tableMetadata.setVisible(false);
            tableMetadata.setManaged(false);
            columnsTable.setItems(FXCollections.unmodifiableObservableList(FXCollections.<VTableColumn>emptyObservableList()));
//        }
    }

    @FXML
    private void onInspectTable(ActionEvent event) {
//        VTableInspector.instpectValue((VTable) value);
    }

}
