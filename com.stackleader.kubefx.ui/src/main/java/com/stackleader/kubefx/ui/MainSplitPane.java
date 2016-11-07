package com.stackleader.kubefx.ui;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.stackleader.kubefx.tabs.api.TabProvider;
import com.stackleader.kubefx.selections.api.SelectionInfo;
import com.stackleader.kubefx.ui.tabs.PodDetailsPane;
import com.stackleader.kubefx.ui.tabs.TabPaneManager;
import static com.stackleader.kubefx.ui.utils.FXUtilities.runAndWait;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TabPane;
import javafx.scene.layout.StackPane;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true, provide = MainSplitPane.class)
public class MainSplitPane {

    private static final Logger LOG = LoggerFactory.getLogger(MainSplitPane.class);
    private TabPaneManager tabPaneManager;
    @FXML
    private StackPane leftSide;
    @FXML
    private StackPane rightSide;
    private SelectionInfo selectionInfo;
    private SplitPane root;

    public MainSplitPane() {
        final URL resource = PodDetailsPane.class.getClassLoader().getResource("MainSplitPane.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(resource);
        fxmlLoader.setClassLoader(this.getClass().getClassLoader());
        fxmlLoader.setController(this);
        runAndWait(() -> {
            try {
                root = fxmlLoader.load();
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        });
    }

    @Activate
    public void activate(BundleContext bundleContext) {
        final TabPane leftTabPane = tabPaneManager.getLeftTabPane();
        leftSide.getChildren().add(leftTabPane);
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

    public SplitPane getRoot() {
        return root;
    }

}
