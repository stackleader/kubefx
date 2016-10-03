package com.stackleader.kubefx.ui.toolbar;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.stackleader.kubefx.ui.actions.RefreshAction;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true)
public class RefreshButton implements ToolbarButtonProvider {

    private final Button refreshBtn;
    private RefreshAction refreshAction;

    public RefreshButton() {
        final FontAwesomeIconView fontAwesomeIconView = new FontAwesomeIconView(FontAwesomeIcon.REFRESH);
        fontAwesomeIconView.setFill(Color.WHITE);
        refreshBtn = new Button("", fontAwesomeIconView);
    }

    @Activate
    public void activate() {
        refreshBtn.setOnAction(action -> {
            refreshAction.invokeAction();
        });
    }

    @Override
    public Button getButton() {
        return refreshBtn;
    }

    @Override
    public int getWeight() {
        return 0;
    }

    @Reference
    public void setRefreshAction(RefreshAction refreshAction) {
        this.refreshAction = refreshAction;
    }
}
