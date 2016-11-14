package com.stackleader.kubefx.ui.tabs;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.stackleader.kubefx.heapster.api.HeapsterClient;
import com.stackleader.kubefx.kubernetes.api.KubernetesClient;
import com.stackleader.kubefx.kubernetes.api.model.Pod;
import com.stackleader.kubefx.selections.api.SelectionInfo;
import static com.stackleader.kubefx.ui.utils.FXUtilities.runAndWait;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.GaugeBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingDeque;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import javafx.util.Pair;
import javafx.util.StringConverter;
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
@Component(immediate = true, provide = PodDetailsPane.class)
public class PodDetailsPane extends StackPane {

    private static final Logger LOG = LoggerFactory.getLogger(PodDetailsPane.class);
    private KubernetesClient client;
    private SelectionInfo selectionInfo;
    @FXML
    private ToggleButton tailToggleBtn;
    @FXML
    private Button copyBtn;
    @FXML
    private Button clearBtn;
    @FXML
    private TextArea logsTextArea;
    @FXML
    private AnchorPane attributeTablePane;
    @FXML
    private StackPane cpuPane;
    @FXML
    private StackPane ramPane;
    @FXML
    private StackPane ioPane;
    final TableView<Pair<String, String>> infoTable;
    private Pod selectedPod;
    private Call logRequestCall;
    private final BlockingDeque<String> logContent;
    private EventSource<Void> updateStream;
    public ObservableList<Pair<String, String>> data;
    private HeapsterClient heapsterClient;
    private BundleContext bc;

    public PodDetailsPane() {
        getStyleClass().add("pod-detail-pane");
        infoTable = new TableView<>();
        final StackPane placeHolder = new StackPane();
        placeHolder.getStyleClass().add("base");
        infoTable.setPlaceholder(placeHolder);
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
        final URL resource = PodDetailsPane.class.getClassLoader().getResource("podInfo.fxml");
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
                attributeTablePane.getChildren().add(infoTable);

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
        this.bc = bc;
        Platform.runLater(() -> {
            infoTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            logsTextArea.setEditable(false);
            logsTextArea.setWrapText(true);
        });
        updateStream.successionEnds(Duration.ofMillis(500)).subscribe(e -> {
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
                refreshPaneContent(selectedPod);
            });
        });
        tailToggleBtn.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean isSelected) -> {
            Platform.runLater(() -> {
                if (isSelected) {
                    tailToggleBtn.getStyleClass().add("armed");
                    if (selectedPod != null) {
                        tailToggleBtn.setText("Tailing Logs...");
                        tailToggleBtn.arm();
                        logContent.add("Starting Tail\n");
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
                } else {
                    tailToggleBtn.getStyleClass().remove("armed");
                    tailToggleBtn.setText("Tail Logs");
                    if (logRequestCall != null) {
                        logRequestCall.cancel();
                    }

                }
            });
        });
        clearBtn.setOnAction(evt -> {
            Platform.runLater(() -> {
                logsTextArea.clear();
            });
        });
        copyBtn.setOnAction(action -> {
            final Clipboard clipboard = Clipboard.getSystemClipboard();
            final ClipboardContent content = new ClipboardContent();
            content.putString(logsTextArea.getText());
            clipboard.setContent(content);
        });

        initializeMetrics();
    }

    private void refreshPaneContent(Pod selectedPod) {
        Platform.runLater(() -> {
            logsTextArea.clear();
            this.selectedPod = selectedPod;
            data.clear();
            tailToggleBtn.setSelected(false);
            data.addAll(selectedPod.getAttributes());
        });
        updateCpuUsageRateData(selectedPod);
        updateMemoryUsageData(selectedPod);
        updateIoUsageData(selectedPod);
    }

    @Reference
    public void setSelectionInfo(SelectionInfo selectionInfo) {
        this.selectionInfo = selectionInfo;
    }

    @Reference
    public void setClient(KubernetesClient client) {
        this.client = client;
    }

    @Reference
    public void setHeapsterClient(HeapsterClient heapsterClient) {
        this.heapsterClient = heapsterClient;
    }

    private void initializeMetrics() {
        initializeCpuUsageChart();
        initializeMemoryGuage();
        initializeIoChart();
    }

    private void initializeCpuUsageChart() {
        final CategoryAxis timeAxis = new CategoryAxis();
        timeAxis.setAutoRanging(true);
        final NumberAxis yCpuUsageAxis = new NumberAxis(0, 100, 10);
        AreaChart<String, Number> ac = new AreaChart<>(timeAxis, yCpuUsageAxis);
        ac.getStylesheets().add(bc.getBundle().getEntry("chartStyle.css").toExternalForm());
        cpuChartData = new XYChart.Series();
        cpuChartData.setName("Usage Rate Last 15 Minutes");
        cpuPane.getChildren().add(ac);
        ac.getData().add(cpuChartData);
    }

    private void initializeMemoryGuage() {
        final Color barColor = Color.web("#E24D42");
        memoryGuage = GaugeBuilder.create()
                .skinType(Gauge.SkinType.LEVEL)
                .backgroundPaint(Color.TRANSPARENT)
                .valueColor(Color.web("#DDDDDD"))
                .barColor(barColor)
                .build();
        ramPane.heightProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            memoryGuage.setPadding(new Insets(ramPane.getHeight() * .1, 0, 0, 0));
        });
        memoryGuage.setPadding(new Insets(ramPane.getHeight() * .1, 0, 0, 0));
        memoryGuageLabel = new Label();
        final FontAwesomeIconView circle = new FontAwesomeIconView(FontAwesomeIcon.CIRCLE);
        circle.setFill(barColor);
        memoryGuageLabel.setGraphic(circle);
        BorderPane borderPane = new BorderPane();
        borderPane.setBottom(new BorderPane(memoryGuageLabel));
        ramPane.getChildren().addAll(memoryGuage, borderPane);
    }
    private Label memoryGuageLabel;
    private Gauge memoryGuage;
    private XYChart.Series<String, Number> cpuChartData;
    private XYChart.Series<String, Number> ioOutChartData;
    private XYChart.Series<String, Number> ioInChartData;

    private void updateCpuUsageRateData(Pod selectedPod) {
        CompletableFuture.<Void>supplyAsync(() -> {
            heapsterClient.getPodCpuUsage(selectedPod.getNamespace(), selectedPod.getName()).ifPresent(podCpuRate -> {
                Platform.runLater(() -> {
                    cpuChartData.getData().clear();
                    List<HeapsterClient.Metric> metrics = podCpuRate.metrics;
                    DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("HH:mm");
                    metrics.stream().forEach(m -> {
                        cpuChartData.getData().add(new XYChart.Data(m.timestamp.format(dateFormat), m.value));
                    });
                });
            });
            return null;
        }).whenComplete((u, t) -> {
            if (t != null) {
                Throwable ex = (Throwable) t;
                LOG.error(ex.getMessage(), ex);
            }
        });
    }

    private void updateMemoryUsageData(Pod selectedPod) {
        CompletableFuture.<Void>supplyAsync(() -> {
            Optional<HeapsterClient.PodMemoryUsage> memoryUsage = heapsterClient.getPodMemoryUsage(selectedPod.getNamespace(), selectedPod.getName());
            memoryUsage.ifPresent(podMemoryUsage -> {
                Optional<HeapsterClient.PodMemoryLimit> memoryLimits = heapsterClient.getPodMemoryLimit(selectedPod.getNamespace(), selectedPod.getName());
                memoryLimits.ifPresent(podMemoryLimit -> {
                    podMemoryUsage.metrics.stream()
                            .filter(limit -> limit.timestamp.equals(podMemoryUsage.latestTimestamp))
                            .findFirst()
                            .ifPresent(latestMemoryUsage -> {
                                podMemoryLimit.metrics.stream()
                                        .filter(limit -> limit.timestamp.equals(podMemoryLimit.latestTimestamp))
                                        .findFirst()
                                        .ifPresent(latestMemoryLimit -> {
                                            Platform.runLater(() -> {
                                                List<HeapsterClient.MemoryMetric> metrics = podMemoryUsage.metrics;
                                                double used = (double) latestMemoryUsage.value.getValue() / (double) latestMemoryLimit.value.getValue();
                                                if (Double.isInfinite(used)) {
                                                    memoryGuage.setValue(0);
                                                    memoryGuageLabel.setText("0 / " + latestMemoryLimit.value.getValueString());
                                                } else {
                                                    final double value = used * 100;
                                                    memoryGuage.setValue(value);
                                                    memoryGuageLabel.setText(latestMemoryUsage.value.getValueString() + " / " + latestMemoryLimit.value.getValueString());
                                                }
                                            });
                                        });
                            });
                });
            });
            return null;
        }).whenComplete((u, t) -> {
            if (t != null) {
                Throwable ex = (Throwable) t;
                LOG.error(ex.getMessage(), ex);
            }
        });;

    }

    private void initializeIoChart() {
        final CategoryAxis labelAxis = new CategoryAxis();
        labelAxis.setAutoRanging(true);
        final NumberAxis yCpuUsageAxis = new NumberAxis();
        yCpuUsageAxis.setTickLabelFormatter(new StringConverter<Number>() {
            @Override
            public String toString(Number object) {
                DecimalFormat dec = new DecimalFormat("0.00");
                double m = ((object.longValue() / 1024.0) / 1024.0);
                return dec.format(m).concat(" MB");
            }

            @Override
            public Number fromString(String string) {
                Long l = Long.parseLong(string.substring(0, string.indexOf(" MB")));
                return (l * 1024) * 1024;
            }
        });
        yCpuUsageAxis.setAutoRanging(true);
        BarChart<String, Number> barc = new BarChart<>(labelAxis, yCpuUsageAxis);
        barc.getStylesheets().add(bc.getBundle().getEntry("chartStyle.css").toExternalForm());
        ioOutChartData = new XYChart.Series();
        ioInChartData = new XYChart.Series();
        ioOutChartData.setName("Out");
        ioInChartData.setName("In");
        ioPane.getChildren().add(barc);
        barc.getData().add(ioOutChartData);
        barc.getData().add(ioInChartData);
    }

    private void updateIoUsageData(Pod selectedPod) {
        CompletableFuture.<Void>supplyAsync(() -> {
            heapsterClient.getPodNetworkOut(selectedPod.getNamespace(), selectedPod.getName()).ifPresent(podIoOut -> {
                Platform.runLater(() -> {
                    ioOutChartData.getData().clear();
                    List<HeapsterClient.MemoryMetric> metrics = podIoOut.metrics;
                    DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("HH:mm");
                    metrics.stream().filter(m -> m.timestamp.equals(podIoOut.latestTimestamp)).forEach(m -> {
                        ioOutChartData.getData().add(new XYChart.Data("", m.value.getValue()));
                    });
                });
            });
            return null;
        }).whenComplete((u, t) -> {
            if (t != null) {
                Throwable ex = (Throwable) t;
                LOG.error(ex.getMessage(), ex);
            }
        });
        CompletableFuture.<Void>supplyAsync(() -> {
            heapsterClient.getPodNetworkIn(selectedPod.getNamespace(), selectedPod.getName()).ifPresent(podIoIn -> {
                Platform.runLater(() -> {
                    ioInChartData.getData().clear();
                    List<HeapsterClient.MemoryMetric> metrics = podIoIn.metrics;
                    metrics.stream().filter(m -> m.timestamp.equals(podIoIn.latestTimestamp)).forEach(m -> {
                        ioInChartData.getData().add(new XYChart.Data("", m.value.getValue()));
                    });
                });
            });
            return null;
        }).whenComplete((u, t) -> {
            if (t != null) {
                Throwable ex = (Throwable) t;
                LOG.error(ex.getMessage(), ex);
            }
        });
    }

}
