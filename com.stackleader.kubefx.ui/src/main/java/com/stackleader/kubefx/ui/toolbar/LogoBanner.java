package com.stackleader.kubefx.ui.toolbar;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import static com.stackleader.kubefx.ui.utils.FXUtilities.runAndWait;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import javafx.geometry.Pos;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import org.osgi.framework.BundleContext;

@Component(immediate = true, provide = LogoBanner.class)
public class LogoBanner extends HBox {

    private static final double FIXED_HEIGHT = 50;
    private String logoCssPath;
    private String logoImagePath;
    private Pane spacer;
    private HBox toolBarButtonContainer;
    private Set<ToolbarButtonProvider> buttonProviders;

    public LogoBanner() {
        buttonProviders = new TreeSet<ToolbarButtonProvider>(Comparator.comparingInt(x -> x.getWeight()));
        setPrefHeight(50);
        setMaxHeight(50);
        toolBarButtonContainer = new HBox();
        toolBarButtonContainer.setSpacing(5);
        toolBarButtonContainer.setAlignment(Pos.CENTER_LEFT);
        spacer = new Pane();
        spacer.setMaxHeight(FIXED_HEIGHT);
        spacer.setPrefHeight(FIXED_HEIGHT);
        HBox.setHgrow(spacer, Priority.ALWAYS);

    }

    @Activate
    public void activate(BundleContext bc) {
        runAndWait(() -> {
            logoCssPath = bc.getBundle().getEntry("logo.css").toExternalForm();
            logoImagePath = bc.getBundle().getEntry("kubeFxLogo.png").toExternalForm();
            getStylesheets().add(logoCssPath);
            getStyleClass().add("kubefx-banner");
            HBox logoView = new HBox();
            logoView.setStyle("-fx-alignment: center-right;");
            logoView.getChildren().addAll(new ImageView(logoImagePath));
            setAlignment(Pos.CENTER);
            getChildren().addAll(toolBarButtonContainer, spacer, logoView);
        });
    }

    @Reference(optional = true, multiple = true, unbind = "removeToolbarButton", dynamic = true)
    public void addToolbarButton(ToolbarButtonProvider buttonProvider) {
        runAndWait(() -> {
            buttonProviders.add(buttonProvider);
            toolBarButtonContainer.getChildren().clear();
            buttonProviders.stream().forEach(bp -> bp.getButton().getStyleClass().add("darkButton"));
            toolBarButtonContainer.getChildren().addAll(buttonProviders.stream().map(bp -> bp.getButton()).collect(Collectors.toList()));
        });
    }

    public void removeToolbarButton(ToolbarButtonProvider buttonProvider) {
        runAndWait(() -> {
            buttonProviders.remove(buttonProvider);
            toolBarButtonContainer.getChildren().clear();
            toolBarButtonContainer.getChildren().addAll(buttonProviders.stream().map(bp -> bp.getButton()).collect(Collectors.toList()));
        });

    }

}
