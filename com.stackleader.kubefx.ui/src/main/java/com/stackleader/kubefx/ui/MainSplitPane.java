package com.stackleader.kubefx.ui;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.stackleader.kubefx.tabs.api.TabProvider;
import com.stackleader.kubefx.ui.selections.SelectionInfo;
import com.stackleader.kubefx.ui.tabs.TabPaneManager;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true, provide = MainSplitPane.class)
public class MainSplitPane extends SplitPane {

    private static final Logger LOG = LoggerFactory.getLogger(MainSplitPane.class);
    private TabPaneManager tabPaneManager;
    private StackPane leftSide;
    private StackPane rightSide;
    private SelectionInfo selectionInfo;

    public MainSplitPane() {
        leftSide = new StackPane();
        rightSide = new StackPane();
        getItems().add(leftSide);
        getItems().add(rightSide);
        setDividerPositions(0.35);
    }

    @Activate
    public void activate() {
        leftSide.getChildren().add(tabPaneManager.getLeftTabPane());
//        rightSide.getChildren().add(tabPaneManager.getRightTabPane());
        addRightSidePaneListener();
    }

    @Reference(unbind = "removeTabPaneManager")
    public void setTabPaneManager(TabPaneManager tabPaneManager) {
        this.tabPaneManager = tabPaneManager;
    }

    @Reference
    public void setSelectionInfo(SelectionInfo selectionInfo) {
        this.selectionInfo = selectionInfo;
    }

    public void removeTabPaneManager(TabPaneManager tabPaneManager) {
        LOG.info("removeTabPaneManager called");
        Platform.runLater(() -> {
            leftSide.getChildren().remove(tabPaneManager.getLeftTabPane());
            rightSide.getChildren().remove(tabPaneManager.getRightTabPane());
        });
    }

    private void addRightSidePaneListener() {
        selectionInfo.getSelectedTabProvider().addListener((ObservableValue<? extends Optional<TabProvider>> observable, Optional<TabProvider> oldValue, Optional<TabProvider> newValue) -> {
            newValue.ifPresent((TabProvider selectedTabProvider) -> {
                Platform.runLater(() -> {
                    rightSide.getChildren().clear();
                    rightSide.getChildren().add(selectedTabProvider.getInfoPane());
                });
            });
        });
        selectionInfo.getSelectedTabProvider().get().ifPresent(selectedTabProvider -> {
            Platform.runLater(() -> {
                rightSide.getChildren().clear();
                rightSide.getChildren().add(selectedTabProvider.getInfoPane());
            });
        });
    }

}
