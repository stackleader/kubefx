package com.stackleader.kubefx.ui.tabs;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;
import com.stackleader.kubefx.tabs.api.TabProvider;
import com.stackleader.kubefx.selections.api.SelectionInfo;
import static com.stackleader.kubefx.ui.utils.FXUtilities.runAndWait;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.Side;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true, provide = TabPaneManager.class)
public class TabPaneManager {

    private static final Logger LOG = LoggerFactory.getLogger(TabPaneManager.class);
    private final TabPane rightTabPane;
    private final TabPane leftTabPane;
    private final TabPane bottomTabPane;
    private Set<TabProvider> leftTabs;
    private Set<TabProvider> rightTabs;
    private Set<TabProvider> bottomTabs;
    private Map<TabPane, Set<TabProvider>> tabPositions;
    private SelectionInfo selectionInfo;

    public TabPaneManager() {
        leftTabPane = new TabPane();
        rightTabPane = new TabPane();
        bottomTabPane = new TabPane();
        setAnchorPaneConstraints(rightTabPane);
        setAnchorPaneConstraints(bottomTabPane);
        rightTabPane.setSide(Side.RIGHT);
        final Comparator<TabProvider> tabProviderComparator = Comparator.comparingInt(tp -> tp.getTabWeight());
        leftTabs = new TreeSet<>(tabProviderComparator);
        rightTabs = new TreeSet<>(tabProviderComparator);
        bottomTabs = new TreeSet<>(tabProviderComparator);
        tabPositions = new HashMap<>();
        tabPositions.put(leftTabPane, leftTabs);
        tabPositions.put(rightTabPane, rightTabs);
        tabPositions.put(bottomTabPane, bottomTabs);
    }

    @Activate
    public void activate() {
        leftTabPane.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends Tab> observable, Tab oldValue, Tab newValue) -> {
            final Optional<TabProvider> selectedTab = leftTabs.stream().filter(tabProvider -> tabProvider.getTab().equals(newValue)).findFirst();
            selectionInfo.getSelectedTabProvider().setValue(selectedTab);
        });
        leftTabPane.getSelectionModel().selectFirst();
    }

    @Reference(optional = true, multiple = true, unbind = "removeTab", dynamic = true)
    public void addTab(TabProvider tabProvider) {
        tabProvider.getTab().setClosable(false);
        switch (tabProvider.getTabDockingPosition()) {
//            case BOTTOM:
//                addTab(bottomTabPane, tabProvider);
//                break;
//            case RIGHT:
//                addTab(rightTabPane, tabProvider);
//                break;
            case LEFT:
                addTab(leftTabPane, tabProvider);
                break;
        }
    }

    private synchronized void addTab(TabPane tabPane, TabProvider tabProvider) {
        runAndWait(() -> {
            Tab selectedTab = tabPane.getSelectionModel().selectedItemProperty().get();
            final ObservableList<Tab> currentTabs = tabPane.getTabs();
            Set<TabProvider> sortedTabs = tabPositions.get(tabPane);
            sortedTabs.add(tabProvider);
            currentTabs.clear();
            sortedTabs.forEach(tp -> currentTabs.add(tp.getTab()));
            tabPane.getSelectionModel().select(selectedTab);
        });
    }

    private synchronized void removeTab(TabPane tabPane, TabProvider tabProvider) {
        runAndWait(() -> {
            final ObservableList<Tab> currentTabs = tabPane.getTabs();
            Set<TabProvider> sortedTabs = tabPositions.get(tabPane);
            sortedTabs.remove(tabProvider);
            currentTabs.clear();
            sortedTabs.forEach(tp -> currentTabs.add(tp.getTab()));
        });
    }

    public void removeTab(TabProvider tabProvider) {
        switch (tabProvider.getTabDockingPosition()) {
//            case BOTTOM:
//                removeTab(bottomTabPane, tabProvider);
//                break;
//            case RIGHT:
//                removeTab(rightTabPane, tabProvider);
//                break;
            case LEFT:
                removeTab(leftTabPane, tabProvider);
                break;
        }
    }

    public TabPane getRightTabPane() {
        return rightTabPane;
    }

    public TabPane getLeftTabPane() {
        return leftTabPane;
    }

    public TabPane getBottomTabPane() {
        return bottomTabPane;
    }

    @Reference
    public void setSelectionInfo(SelectionInfo selectionInfo) {
        this.selectionInfo = selectionInfo;
    }

    private void setAnchorPaneConstraints(TabPane pane) {
        double anchor = 0;
        AnchorPane.setBottomAnchor(pane, anchor);
        AnchorPane.setTopAnchor(pane, anchor);
        AnchorPane.setRightAnchor(pane, anchor);
        AnchorPane.setLeftAnchor(pane, anchor);
    }

    @Deactivate
    public void deactivate() {
        LOG.info("TabPaneManager deactivated");
    }
}
