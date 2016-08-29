package com.stackleader.kubefx.ui.tabs;

import aQute.bnd.annotation.component.Component;
import com.stackleader.kubefx.tabs.api.TabDockingPosition;
import com.stackleader.kubefx.tabs.api.TabProvider;
import javafx.scene.control.Tab;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true)
public class NodesTab extends Tab implements TabProvider {

    public NodesTab() {
        setText("Nodes");
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

    @Override
    public Pane getInfoPane() {
      return new StackPane();
    }

}
