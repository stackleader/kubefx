package com.stackleader.kubefx.ui.footer;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import javafx.application.Platform;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import org.osgi.framework.BundleContext;

@Component(immediate = true, provide = LogoBanner.class)
public class LogoBanner extends HBox {

    private String logoCssPath;
    private String logoImagePath;

    public LogoBanner() {
        setPrefHeight(50);
        setMaxHeight(50);
    }

    @Activate
    public void activate(BundleContext bc) {
        Platform.runLater(() -> {
            logoCssPath = bc.getBundle().getEntry("logo.css").toExternalForm();
            logoImagePath = bc.getBundle().getEntry("stackLeaderLogo.png").toExternalForm();
            getStylesheets().add(logoCssPath);
            getStyleClass().add("kubefx-banner");
            HBox logoView = new HBox();
            logoView.getStyleClass().add("kubefx-logo");
            logoView.getChildren().addAll(new ImageView(logoImagePath));
            getChildren().addAll(logoView);
        });
    }

}
