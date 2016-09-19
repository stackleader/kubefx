package com.stackleader.kubefx.core;

import com.stackleader.kubefx.core.api.StageProvider;
import javafx.application.HostServices;
import javafx.stage.Stage;

public class MainStageProvider implements StageProvider {

    Stage stage;
    Stage splashStage;
    HostServices hostServices;

    public MainStageProvider(Stage stage, Stage splashStage, HostServices hostServices) {
        this.stage = stage;
        this.splashStage = splashStage;
        this.hostServices = hostServices;
    }

    @Override
    public Stage getStage() {
        return stage;
    }

    @Override
    public HostServices getHostServices() {
        return hostServices;
    }

    @Override
    public Stage getSplashStage() {
        return splashStage;
    }
}
