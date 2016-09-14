package com.stackleader.kubefx.ui.tabs;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.stackleader.kubefx.kubernetes.api.KubernetesClient;
import com.stackleader.kubefx.kubernetes.api.model.Pod;
import com.stackleader.kubefx.ui.selections.SelectionInfo;
import static com.stackleader.kubefx.ui.utils.FXUtilities.runAndWait;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Level;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import javafx.util.Pair;
import okhttp3.Call;
import okhttp3.Response;
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
    @FXML
    private Button startTail;
    @FXML
    private Button stopTail;
    @FXML
    private Button copyBtn;
    @FXML
    private Button clearBtn;
    @FXML
    private TextArea logsTextArea;
    @FXML
    private AnchorPane infoAnchorPane;
    final TableView<Pair<String, String>> infoTable;
    private Pod selectedPod;
    private Call logRequestCall;
    private final BlockingDeque<String> logContent;
    private EventSource<Void> updateStream;
    public ObservableList<Pair<String, String>> data;

    public PodInfoPane() {
        infoTable = new TableView<>();
        updateStream = new EventSource<>();
        logContent = new LinkedBlockingDeque<>(10_000);

        data = FXCollections.observableArrayList();
        infoTable.setItems(data);
         // table definition
        TableColumn<Pair<String, String>, String> nameColumn = new TableColumn<>("NAME");
        TableColumn<Pair<String, String>, String> valueColumn = new TableColumn<>("VALUE");
        valueColumn.setSortable(false);

        nameColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Pair<String, String>, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Pair<String, String>, String> param) {
                return new ReadOnlyStringWrapper(param.getValue().getKey());
            }
        });
       valueColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Pair<String, String>, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Pair<String, String>, String> param) {
                  return new ReadOnlyStringWrapper(param.getValue().getValue());
            }
        });

        infoTable.getColumns().setAll(nameColumn, valueColumn);
        final URL resource = PodInfoPane.class.getClassLoader().getResource("podInfo.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(resource);
        fxmlLoader.setClassLoader(this.getClass().getClassLoader());
        fxmlLoader.setController(this);
        runAndWait(() -> {
            try {
                StackPane root = fxmlLoader.load();

                AnchorPane.setBottomAnchor(infoTable, 0d);
                AnchorPane.setLeftAnchor(infoTable, 0d);
                AnchorPane.setRightAnchor(infoTable, 0d);
                AnchorPane.setTopAnchor(infoTable, 0d);
                infoAnchorPane.getChildren().add(infoTable);

                AnchorPane.setBottomAnchor(root, 0d);
                AnchorPane.setLeftAnchor(root, 0d);
                AnchorPane.setRightAnchor(root, 0d);
                AnchorPane.setTopAnchor(root, 0d);
                AnchorPane wrapper = new AnchorPane(root);
                getChildren().add(wrapper);
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        });
    }

    @Activate
    public void activate(BundleContext bc) {
        Platform.runLater(() -> {
            infoTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            logsTextArea.setEditable(false);
            logsTextArea.getStylesheets().add(bc.getBundle().getEntry("consoleStyle.css").toExternalForm());
        });
        updateStream.successionEnds(Duration.ofMillis(500)).subscribe(e -> {
            final ArrayList<String> logLines = new ArrayList<String>();
            logContent.drainTo(logLines);
            final String joinedLogLines = String.join("", logLines);
            try {
                File file = new File("test.txt");
                Files.write(joinedLogLines, file, Charsets.UTF_8);
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(PodInfoPane.class.getName()).log(Level.SEVERE, null, ex);
            }
            Platform.runLater(() -> {
                logsTextArea.selectEnd();
                logsTextArea.appendText(joinedLogLines);
            });
        });
        selectionInfo.getSelectedPod().addListener((ObservableValue<? extends Optional<Pod>> observable, Optional<Pod> oldValue, Optional<Pod> newValue) -> {
            newValue.ifPresent((Pod selectedPod) -> {
                Platform.runLater(() -> {
                    logsTextArea.clear();
                    this.selectedPod = selectedPod;
                    data.clear();
                    data.addAll(selectedPod.getAttributes());
                });
            });
        });
        startTail.setOnAction(event -> {
            if (selectedPod != null) {
                logContent.add("Starting Tail");
                updateStream.emit(null);
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

}
