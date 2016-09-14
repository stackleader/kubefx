package com.stackleader.kubefx.ui;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.stackleader.kubefx.ui.footer.Footer;
import com.stackleader.kubefx.ui.footer.LogoBanner;
import java.awt.DisplayMode;
import java.awt.GraphicsEnvironment;
import javafx.application.Platform;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true, provide = Root.class)
public class Root extends VBox {

    private MainSplitPane mainSplitPane;
    private StackPane stackPane;
    private Footer footer;
    private LogoBanner logoBanner;

    public Root() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        final DisplayMode displayMode = ge.getDefaultScreenDevice().getDisplayMode();
        double width = displayMode.getWidth() * .8;
        double height = displayMode.getHeight() * .8;
        setPrefSize(width, height);
        stackPane = new StackPane();
        VBox.setVgrow(stackPane, Priority.ALWAYS);
    }

    @Activate
    public void activate() {
        Platform.runLater(() -> {
            stackPane.getChildren().add(mainSplitPane);
//            getChildren().add(menuBarManager.getMenuBar());
//            getChildren().add(toolbarProvider.getTopToolbar());
            getChildren().add(logoBanner);
            getChildren().add(stackPane);
            getChildren().add(footer);
        });
    }

    @Reference(unbind = "removeMainSplitPane")
    public void setMainSplitPane(MainSplitPane mainSplitPane) {
        this.mainSplitPane = mainSplitPane;
    }

    public void removeMainSplitPane(MainSplitPane mainSplitPane) {
        Platform.runLater(() -> {
            getChildren().clear();
        });
    }

    @Reference(unbind = "removeFooter")
    public void setFooter(Footer footer) {
        this.footer = footer;
    }

    @Reference(unbind = "removeLogoBanner")
    public void setLogoBanner(LogoBanner logoBanner) {
        this.logoBanner = logoBanner;
    }

    public void removeLogoBanner(LogoBanner logoBanner) {
        Platform.runLater(() -> {
            getChildren().remove(logoBanner);
        });
    }

    public void removeFooter(Footer footer) {
        Platform.runLater(() -> {
            getChildren().remove(footer);
        });
    }
}
