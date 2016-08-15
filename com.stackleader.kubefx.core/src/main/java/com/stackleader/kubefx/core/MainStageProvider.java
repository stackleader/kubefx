package com.stackleader.kubefx.core;

import com.stackleader.kubefx.core.api.StageProvider;
import javafx.application.HostServices;
import javafx.stage.Stage;

public class MainStageProvider implements StageProvider {

    Stage stage;
    HostServices hostServices;

    public MainStageProvider(Stage stage, HostServices hostServices) {
        this.stage = stage;
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
}
