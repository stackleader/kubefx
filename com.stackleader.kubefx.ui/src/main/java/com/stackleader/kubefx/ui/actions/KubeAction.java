/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.stackleader.kubefx.ui.actions;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.input.KeyCombination;

/**
 *
 * @author dcnorris
 */
interface KubeAction {

    ObjectProperty<KeyCombination> acceleratorProperty();

    BooleanProperty disabledProperty();

    KeyCombination getAccelerator();

    Node getGraphic();

    ObjectProperty<Node> graphicProperty();

    boolean isDisabled();

    void setAccelerator(KeyCombination value);

    void setDisabled(boolean value);

    void setGraphic(Node value);

    StringProperty textProperty();

    String getText();

    void setText(String value);

    Runnable getAction();

    default void invokeAction() {
        getAction().run();
    }
}
