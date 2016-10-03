/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.stackleader.kubefx.ui.actions;

/**
 *
 * @author dcnorris
 */
interface KubeAction {
    
    String getActionName();

    Runnable getAction();

    default void invokeAction() {
        getAction().run();
    }
}
