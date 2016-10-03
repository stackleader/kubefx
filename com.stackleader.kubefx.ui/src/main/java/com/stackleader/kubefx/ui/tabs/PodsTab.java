package com.stackleader.kubefx.ui.tabs;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.stackleader.kubefx.kubernetes.api.KubernetesClient;
import com.stackleader.kubefx.kubernetes.api.model.Pod;
import com.stackleader.kubefx.tabs.api.TabDockingPosition;
import com.stackleader.kubefx.tabs.api.TabProvider;
import com.stackleader.kubefx.ui.selections.SelectionInfo;
import static com.stackleader.kubefx.ui.utils.FXUtilities.runAndWait;
import java.util.Optional;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Tab;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true)
public class PodsTab extends Tab implements TabProvider, RefreshActionListener {

    private KubernetesClient client;
    private ObservableList<Pod> pods;
    private StackPane tabContent;
    private SelectionInfo selectionInfo;
    private PodInfoPane podInfoPane;
    private PodStatusTable<Pod> podTable;

    public PodsTab() {
        setText("Pods");
        pods = FXCollections.observableArrayList();
        podTable = new PodStatusTable<>(pods);
        podTable.setItems(pods);
        tabContent = new StackPane(podTable);

        setContent(tabContent);
    }

    @Activate
    public void activate() {
        refresh();
        initializeSelectionListeners();
    }

    @Override
    public void refresh() {
        runAndWait(() -> {
            pods.clear();
            client.getPods().forEach(pod -> {
                pods.add(pod);
            });
        });
    }

    @Override
    public Tab getTab() {
        return this;
    }

    @Override
    public TabDockingPosition getTabDockingPosition() {
        return TabDockingPosition.LEFT;
    }

    @Override
    public int getTabWeight() {
        return 0;
    }

    @Reference
    public void setClient(KubernetesClient client) {
        this.client = client;
    }

    @Reference
    public void setSelectionInfo(SelectionInfo selectionInfo) {
        this.selectionInfo = selectionInfo;
    }

    @Reference
    public void setPodInfoPane(PodInfoPane podInfoPane) {
        this.podInfoPane = podInfoPane;
    }

    private void initializeSelectionListeners() {
        podTable.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends Pod> observable, Pod oldValue, Pod newValue) -> {
            selectionInfo.getSelectedPod().setValue(Optional.ofNullable(newValue));
        });
    }

    @Override
    public Pane getInfoPane() {
        return podInfoPane;
    }

}
