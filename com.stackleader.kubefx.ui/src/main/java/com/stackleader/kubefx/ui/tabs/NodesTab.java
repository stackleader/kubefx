package com.stackleader.kubefx.ui.tabs;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.stackleader.kubefx.kubernetes.api.KubernetesClient;
import com.stackleader.kubefx.kubernetes.api.model.Node;
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
import com.stackleader.kubefx.ui.actions.RefreshActionListener;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true)
public class NodesTab extends Tab implements TabProvider, RefreshActionListener {

    private KubernetesClient client;
    private ObservableList<Node> nodes;
    private StackPane tabContent;
    private SelectionInfo selectionInfo;
    private NodeInfoPane podInfoPane;
    private NodeStatusTable<Node> nodesTable;

    public NodesTab() {
        setText("Nodes");
        nodes = FXCollections.observableArrayList();
        nodesTable = new NodeStatusTable<>(nodes);
        nodesTable.setItems(nodes);
        tabContent = new StackPane(nodesTable);
        setContent(tabContent);
    }

    @Activate
    public void activate() {
        refresh();
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
        return 1;
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
    public void setPodInfoPane(NodeInfoPane nodeInfoPane) {
        this.podInfoPane = nodeInfoPane;
    }

    private void initializeSelectionListeners() {
        nodesTable.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends Node> observable, Node oldValue, Node newValue) -> {
            selectionInfo.getSelectedNode().setValue(Optional.ofNullable(newValue));
        });
    }

    @Override
    public Pane getInfoPane() {
        return podInfoPane;
    }

    @Override
    public void refresh() {
        runAndWait(() -> {
            nodes.clear();
            client.getNodes().forEach(pod -> {
                nodes.add(pod);
            });
        });
    }

}
