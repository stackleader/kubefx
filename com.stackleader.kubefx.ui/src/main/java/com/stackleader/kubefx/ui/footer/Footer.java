package com.stackleader.kubefx.ui.footer;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true, provide = Footer.class)
public class Footer extends HBox {

    private static final Logger LOG = LoggerFactory.getLogger(Footer.class);
    private static final double FIXED_HEIGHT = 25.0;
    private MemoryTracker memoryTracker;
    private Pane spacer;

    public Footer() {
        setMaxHeight(FIXED_HEIGHT);
        setPrefHeight(FIXED_HEIGHT);
        memoryTracker = new MemoryTracker();
        spacer = new Pane();
        spacer.setMaxHeight(FIXED_HEIGHT);
        spacer.setPrefHeight(FIXED_HEIGHT);
        HBox.setHgrow(spacer, Priority.ALWAYS);
        getChildren().addAll(memoryTracker, spacer);
    }

    @Deactivate
    public void deactivate() {
        LOG.info("Footer deactivated");
    }
}
