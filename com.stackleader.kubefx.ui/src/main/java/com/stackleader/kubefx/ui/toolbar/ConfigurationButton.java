package com.stackleader.kubefx.ui.toolbar;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.stackleader.kubefx.ui.config.PreferencesTabManager;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true)
public class ConfigurationButton implements ToolbarButtonProvider {

    private final Button gearBtn;
    private PreferencesTabManager preferencesTabManager;

    public ConfigurationButton() {
        final FontAwesomeIconView fontAwesomeIconView = new FontAwesomeIconView(FontAwesomeIcon.GEAR);
        fontAwesomeIconView.setFill(Color.WHITE);
        gearBtn = new Button("", fontAwesomeIconView);
    }

    @Activate
    public void activate() {
        gearBtn.setOnAction(action -> {
            preferencesTabManager.showPreferences();
        });
    }

    @Override
    public Button getButton() {
        return gearBtn;
    }

    @Override
    public int getWeight() {
        return 1;
    }

    @Reference
    public void setPreferencesTabManager(PreferencesTabManager preferencesTabManager) {
        this.preferencesTabManager = preferencesTabManager;
    }
}
