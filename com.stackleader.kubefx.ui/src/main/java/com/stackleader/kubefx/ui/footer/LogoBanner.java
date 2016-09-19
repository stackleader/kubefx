package com.stackleader.kubefx.ui.footer;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import org.osgi.framework.BundleContext;

@Component(immediate = true, provide = LogoBanner.class)
public class LogoBanner extends HBox {

    private static final double FIXED_HEIGHT = 50;
    private String logoCssPath;
    private String logoImagePath;
    private Button refreshBtn;
    private Pane spacer;

    public LogoBanner() {
        setPrefHeight(50);
        setMaxHeight(50);
        final FontAwesomeIconView fontAwesomeIconView = new FontAwesomeIconView(FontAwesomeIcon.REFRESH);
        fontAwesomeIconView.setFill(Color.WHITE);
        refreshBtn = new Button("", fontAwesomeIconView);
        refreshBtn.getStyleClass().add("darkButton");
        spacer = new Pane();
        spacer.setMaxHeight(FIXED_HEIGHT);
        spacer.setPrefHeight(FIXED_HEIGHT);
        HBox.setHgrow(spacer, Priority.ALWAYS);

    }

    @Activate
    public void activate(BundleContext bc) {
        Platform.runLater(() -> {
            logoCssPath = bc.getBundle().getEntry("logo.css").toExternalForm();
            logoImagePath = bc.getBundle().getEntry("kubeFxLogo.png").toExternalForm();
            getStylesheets().add(logoCssPath);
            getStyleClass().add("kubefx-banner");
            HBox logoView = new HBox();
            logoView.setStyle("-fx-alignment: center-right;");
            logoView.getChildren().addAll(new ImageView(logoImagePath));
            getChildren().addAll(refreshBtn, spacer, logoView);
        });
    }

}
