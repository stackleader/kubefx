package com.stackleader.kubefx.ui;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;
import com.stackleader.kubefx.core.api.StageProvider;
import java.awt.SplashScreen;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true)
public class KubeFx {

    private static final Logger LOG = LoggerFactory.getLogger(KubeFx.class);
    private Stage stage;
    private Root root;

    @Activate
    public void activate() {
        closeSplashScreen();
        initializeFxRuntime();
        Platform.runLater(() -> {
            Scene scene = new Scene(root);
            stage.setTitle("KubeFx");
            stage.setScene(scene);
            stage.show();
        });
    }

    private void closeSplashScreen() throws IllegalStateException {
        SplashScreen splashScreen = SplashScreen.getSplashScreen();
        if (splashScreen != null) {
            splashScreen.close();
        }
    }

    private void initializeFxRuntime() {
        new JFXPanel(); // runtime initializer, do not remove
        Platform.setImplicitExit(false);
    }

    @Deactivate
    public void deactivate() {
        Platform.runLater(() -> {
            try {
                stage.hide();
                root.getChildren().clear();
            } catch (Exception ex) {
                //do nothing
            }
        });
    }

    @Reference
    public void setStageProvider(StageProvider stageProvider) {
        this.stage = stageProvider.getStage();
    }

    @Reference
    public void setRoot(Root root) {
        this.root = root;
    }

}
