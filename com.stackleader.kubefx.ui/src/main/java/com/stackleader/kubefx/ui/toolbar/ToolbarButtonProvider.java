package com.stackleader.kubefx.ui.toolbar;

import javafx.scene.control.Button;

public interface ToolbarButtonProvider {

    Button getButton();

    /**
     * @return the weight of button.
     * sorted LEFT < RIGHT
     */
    int getWeight();
}
