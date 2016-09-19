/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.stackleader.kubefx.core.api;

import javafx.application.HostServices;
import javafx.stage.Stage;

/**
 *
 * @author dcnorris
 */
public interface StageProvider {

    Stage getStage();

    Stage getSplashStage();

    HostServices getHostServices();
}
