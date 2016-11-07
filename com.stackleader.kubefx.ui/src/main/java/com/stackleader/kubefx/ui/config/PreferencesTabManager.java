package com.stackleader.kubefx.ui.config;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.stackleader.kubefx.config.api.PreferencesTabProvider;
import static com.stackleader.kubefx.ui.utils.FXUtilities.runAndWait;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true, provide = PreferencesTabManager.class)
public class PreferencesTabManager {

    private static final Logger LOG = LoggerFactory.getLogger(PreferencesTabManager.class);
    private Set<PreferencesTabProvider> tabs;
    private TabPane pane;
    private Stage stage;

    public PreferencesTabManager() {
        tabs = new TreeSet<PreferencesTabProvider>(Comparator.comparingInt(x -> x.getTabWeight()));
        initComponents();
    }

    @Reference(optional = true, multiple = true, unbind = "removeTab", dynamic = true)
    public void addTab(PreferencesTabProvider tabProvider) {
        runAndWait(() -> {
            tabs.add(tabProvider);
            pane.getTabs().clear();
            pane.getTabs().addAll(tabs.stream().map(tab -> tab.getPreferencesTab()).collect(Collectors.toList()));
        });
    }

    public void removeTab(PreferencesTabProvider tabProvider) {
        runAndWait(() -> {
            tabs.remove(tabProvider);
            pane.getTabs().remove(tabProvider.getPreferencesTab());
        });
    }

    public TabPane getPreferencesTabPane() {
        return pane;
    }

    private void setAnchorPaneConstraints(TabPane pane) {
        double anchor = 0;
        AnchorPane.setBottomAnchor(pane, anchor);
        AnchorPane.setTopAnchor(pane, anchor);
        AnchorPane.setRightAnchor(pane, anchor);
        AnchorPane.setLeftAnchor(pane, anchor);
    }

    private void initComponents() {
        runAndWait(() -> {
            stage = new Stage();
            pane = new TabPane();
            final Scene scene = new Scene(pane);
//            StyleUtils.applyDarkTheme(pane);
            pane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
            setAnchorPaneConstraints(pane);
            stage.setScene(scene);
            pane.setPrefSize(800, 500);
            stage.sizeToScene();
            stage.centerOnScreen();
            stage.setResizable(true);
            stage.setTitle("Configuration");
        });
    }

    public void showPreferences() {
        Platform.runLater(() -> {
            stage.show();
            stage.setAlwaysOnTop(true);
            stage.setAlwaysOnTop(false);
        });
    }
}
