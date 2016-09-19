package com.stackleader.kubefx.ui.tabs;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.stackleader.kubefx.kubernetes.api.KubernetesClient;
import com.stackleader.kubefx.kubernetes.api.model.Service;
import com.stackleader.kubefx.tabs.api.TabDockingPosition;
import com.stackleader.kubefx.tabs.api.TabProvider;
import com.stackleader.kubefx.ui.selections.SelectionInfo;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
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
public class ServicesTab extends Tab implements TabProvider {

    private KubernetesClient client;
    private ObservableList<Service> nodes;
    private StackPane tabContent;
    private SelectionInfo selectionInfo;
    private ServiceStatusTable<Service> servicesTable;

    public ServicesTab() {
        setText("Services");
        nodes = FXCollections.observableArrayList();
        servicesTable = new ServiceStatusTable<>(nodes);
        servicesTable.setItems(nodes);
        tabContent = new StackPane(servicesTable);
        setContent(tabContent);
    }

    @Activate
    public void activate() {
        List<Service> clientPods = client.getServices();
        clientPods.forEach(pod -> {
            Platform.runLater(() -> {
                nodes.add(pod);
            });
        });
        initializeSelectionListeners();
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
          return 2;
    }

    @Reference
    public void setClient(KubernetesClient client) {
        this.client = client;
    }

    @Reference
    public void setSelectionInfo(SelectionInfo selectionInfo) {
        this.selectionInfo = selectionInfo;
    }

    private void initializeSelectionListeners() {
        servicesTable.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends Service> observable, Service oldValue, Service newValue) -> {
            selectionInfo.getSelectedService().setValue(Optional.ofNullable(newValue));
        });
    }

    @Override
    public Pane getInfoPane() {
        return new Pane();
    }

}
