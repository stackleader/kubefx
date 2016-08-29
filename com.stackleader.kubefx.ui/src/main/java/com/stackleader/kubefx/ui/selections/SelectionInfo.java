/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.stackleader.kubefx.ui.selections;

import com.stackleader.kubefx.kubernetes.api.model.Pod;
import com.stackleader.kubefx.tabs.api.TabProvider;
import java.util.Optional;
import javafx.beans.property.ObjectProperty;

/**
 *
 * @author dcnorris
 */
public interface SelectionInfo {

    ObjectProperty<Optional<Pod>> getSelectedPod();

    ObjectProperty<Optional<TabProvider>> getSelectedTabProvider();

}
