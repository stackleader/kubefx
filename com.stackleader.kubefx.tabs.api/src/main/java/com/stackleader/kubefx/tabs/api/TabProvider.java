/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.stackleader.kubefx.tabs.api;

import javafx.scene.control.Tab;
import javafx.scene.layout.Pane;

/**
 *
 * @author dcnorris
 */
public interface TabProvider {

    Tab getTab();
    
    Pane getInfoPane();

    TabDockingPosition getTabDockingPosition();

    /**
     * Tabs will be sorted by integer weights Left to
     * Right for the bottom pane,and top to bottom on
     * the right pane.
     **/
    int getTabWeight();
}
