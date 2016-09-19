package com.stackleader.kubefx.ui.footer;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 *
 * @author dcnorris
 */
public class MemoryTracker extends StackPane {

    private static final int FIXED_WIDTH = 250;
    private static final double FIXED_HEIGHT = 20.0;
    private ProgressBar memoryProgressBar;
    private Label memoryLabel;
    private FontAwesomeIconView trashIcon;

    public MemoryTracker() {
        getStyleClass().add("base");
        memoryLabel = new Label();
        memoryProgressBar = new ProgressBar();
        trashIcon = new FontAwesomeIconView(FontAwesomeIcon.TRASH);
        initLayout();
        getChildren().addAll(memoryProgressBar, memoryLabel, trashIcon);
        startTrackingMemory();
        trashIcon.setOnMouseClicked(click -> System.gc());
    }

    private void initLayout() {
        setMaxHeight(FIXED_HEIGHT);
        setPrefHeight(FIXED_HEIGHT);
        setMaxWidth(FIXED_WIDTH);
        setPrefWidth(FIXED_WIDTH);
        //maxHeight="20.0" prefHeight="20.0" prefWidth="250.0" progress="0.27"
        memoryProgressBar.setMaxHeight(FIXED_HEIGHT);
        memoryProgressBar.setPrefHeight(FIXED_HEIGHT);
        memoryProgressBar.setMaxWidth(FIXED_WIDTH);
        memoryProgressBar.setPrefWidth(FIXED_WIDTH);
        StackPane.setAlignment(trashIcon, Pos.CENTER_RIGHT);
        StackPane.setMargin(trashIcon, new Insets(0, 7, 0, 0));
        setPadding(new Insets(5, 0, 0, 5));
    }

    private void startTrackingMemory() {
        Timeline memoryInfoTimeline = new Timeline(new KeyFrame(Duration.seconds(2), (event) -> {
            final int freeMemory = (int) (Runtime.getRuntime().freeMemory() / (1024 * 1024));
            final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / (1024 * 1024));
            final int totalMemory = (int) (Runtime.getRuntime().totalMemory() / (1024 * 1024));
            final double usedPercentage = (double) (totalMemory - freeMemory) / (double) maxMemory;
            final String memoryLabelText = (totalMemory - freeMemory) + "M of " + maxMemory + 'M';
            if (Platform.isFxApplicationThread()) {
                memoryProgressBar.setProgress(usedPercentage);
                memoryLabel.setText(memoryLabelText);
            } else {
                Platform.runLater(() -> {
                    memoryProgressBar.setProgress(usedPercentage);
                    memoryLabel.setText(memoryLabelText);
                });
            }
        }));
        memoryInfoTimeline.setCycleCount(Timeline.INDEFINITE);
        memoryInfoTimeline.play();
    }

}
