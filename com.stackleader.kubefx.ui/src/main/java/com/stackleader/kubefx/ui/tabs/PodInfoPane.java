package com.stackleader.kubefx.ui.tabs;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.stackleader.kubefx.kubernetes.api.KubernetesClient;
import com.stackleader.kubefx.kubernetes.api.model.Pod;
import com.stackleader.kubefx.ui.selections.SelectionInfo;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingDeque;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import okhttp3.Call;
import okhttp3.Response;
import org.controlsfx.control.spreadsheet.GridBase;
import org.controlsfx.control.spreadsheet.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.SpreadsheetCellType;
import org.controlsfx.control.spreadsheet.SpreadsheetView;
import org.osgi.framework.BundleContext;
import org.reactfx.EventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true, provide = PodInfoPane.class)
public class PodInfoPane extends StackPane {

    private static final Logger LOG = LoggerFactory.getLogger(PodInfoPane.class);
    private KubernetesClient client;
    private SelectionInfo selectionInfo;
    private SpreadsheetView spreadsheetView;
    private Label label;
    private Button startTail;
    private Button stopTail;
    private TextArea logsTextArea;
    private Pod selectedPod;
    private Call logRequestCall;
    private final BlockingDeque<String> logContent;
    private EventSource<Void> updateStream;

    public PodInfoPane() {
        updateStream = new EventSource<>();
        logContent = new LinkedBlockingDeque<>(10_000);
        label = new Label();
        startTail = new Button("Tail");
        stopTail = new Button("Stop");
        HBox hBox = new HBox(label, startTail, stopTail);
        hBox.setSpacing(5);
        logsTextArea = new TextArea();
        logsTextArea.setEditable(false);
//            initSpreadSheet();
        VBox vBox = new VBox(hBox);
        SplitPane splitPane = new SplitPane(vBox, logsTextArea);
        splitPane.setOrientation(Orientation.VERTICAL);
        splitPane.setDividerPositions(.50);
        getChildren().add(splitPane);
    }

    private void initSpreadSheet() {
        spreadsheetView = new SpreadsheetView(new GridBase(5, 5));
        spreadsheetView.setShowRowHeader(true);
        spreadsheetView.setShowColumnHeader(true);
        spreadsheetView.setRowHeaderWidth(140);
        spreadsheetView.editableProperty().set(false);
        spreadsheetView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        spreadsheetView.setFixingColumnsAllowed(false);
        spreadsheetView.setFixingRowsAllowed(false);
        spreadsheetView.getContextMenu().getItems().clear();
    }

    @Activate
    public void activate(BundleContext bc) {
        logsTextArea.getStylesheets().add(bc.getBundle().getEntry("consoleStyle.css").toExternalForm());
        updateStream.successionEnds(Duration.ofSeconds(1)).subscribe(e -> {
            final ArrayList<String> logLines = new ArrayList<String>();
            logContent.drainTo(logLines);
            final String joinedLogLines = String.join("", logLines);
            Platform.runLater(() -> {
                logsTextArea.selectEnd();
                logsTextArea.appendText(joinedLogLines);
            });
        });
        selectionInfo.getSelectedPod().addListener((ObservableValue<? extends Optional<Pod>> observable, Optional<Pod> oldValue, Optional<Pod> newValue) -> {
            newValue.ifPresent((Pod selectedPod) -> {
                logsTextArea.clear();
                label.setText(selectedPod.getName().get());
                this.selectedPod = selectedPod;
//                updateSpreadSheet();
            });
        });
        startTail.setOnAction(event -> {
            if (selectedPod != null) {
                logRequestCall = client.tailLogs(selectedPod);
                CompletableFuture.runAsync(() -> {
                    try (Response response = logRequestCall.execute();
                            InputStream is = response.body().byteStream()) {
                        byte[] bytes = new byte[1000];
                        while (!logRequestCall.isCanceled() && is.read(bytes) > -1) {
                            if (logContent.remainingCapacity() > 0) {
                                logContent.add(new String(bytes));
                            }
                            updateStream.emit(null);
                        }
                    } catch (IOException ex) {
                        if (!logRequestCall.isCanceled()) {
                            LOG.error(ex.getMessage(), ex);
                        }
                    }
                });
            }
        });
        stopTail.setOnAction(event -> {
            if (logRequestCall != null) {
                logRequestCall.cancel();
            }
        });
    }

    @Reference
    public void setSelectionInfo(SelectionInfo selectionInfo) {
        this.selectionInfo = selectionInfo;
    }

    @Reference
    public void setClient(KubernetesClient client) {
        this.client = client;
    }

    private void updateSpreadSheet() {
        final List<String> rowHeaders = new ArrayList<>();
        rowHeaders.add("name");
        rowHeaders.add("started");
        rowHeaders.add("ip");
        int rowCount = rowHeaders.size();
        int columnCount = 1;
        GridBase grid = new GridBase(rowCount, columnCount);
        ObservableList<ObservableList<SpreadsheetCell>> rows = FXCollections.observableArrayList();
        for (int row = 0; row < grid.getRowCount(); ++row) {
            final ObservableList<SpreadsheetCell> list = FXCollections.observableArrayList();
            for (int column = 0; column < grid.getColumnCount(); ++column) {
                final String cellValue = selectedPod.getName().get();
                final SpreadsheetCell gridCell = SpreadsheetCellType.STRING.createCell(row, column, 1, 1, cellValue);
                gridCell.setWrapText(true);
                //cellValue=StringUtils.abbreviate(cellValue,20); //TODO consider if this would be useful
                list.add(gridCell);
            }
            rows.add(list);
        }
        grid.getColumnHeaders().clear();
        for (int column = 0; column < grid.getColumnCount(); ++column) {
            grid.getColumnHeaders().add("...");
        }
        grid.getRowHeaders().clear();
        grid.getRowHeaders().addAll(rowHeaders);
        grid.setRows(rows);
        spreadsheetView.setGrid(grid);
        spreadsheetView.getColumns().forEach(column -> column.fitColumn());
    }
}
