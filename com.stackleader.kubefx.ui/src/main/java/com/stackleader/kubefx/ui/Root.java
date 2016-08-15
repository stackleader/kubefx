package com.stackleader.kubefx.ui;

import aQute.bnd.annotation.component.Component;
import java.awt.DisplayMode;
import java.awt.GraphicsEnvironment;
import javafx.scene.layout.VBox;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true, provide = Root.class)
public class Root extends VBox {

    public Root() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        final DisplayMode displayMode = ge.getDefaultScreenDevice().getDisplayMode();
        double width = displayMode.getWidth() * .8;
        double height = displayMode.getHeight() * .8;
        setPrefSize(width, height);
    }

}
