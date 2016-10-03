/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.stackleader.kubefx.config.api;

import javafx.scene.control.Tab;

public interface PreferencesTabProvider {

    Tab getPreferencesTab();

    /**
     * @return the weight of tab.
     * Tabs will be sorted LEFT < RIGHT
     */
    int getTabWeight();
}
