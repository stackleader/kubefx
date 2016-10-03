package com.stackleader.kubefx.ui;

import com.stackleader.kubefx.ui.auth.ConfigurationModal;
import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;
import com.stackleader.kubefx.core.api.StageProvider;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true)
public class KubeFx {

    private static final Logger LOG = LoggerFactory.getLogger(KubeFx.class);
    private Stage stage;
    private Root root;
    private ConfigurationModal configurationModal;
    private StageProvider stageProvider;
    private BundleContext bundleContext;

    @Activate
    public void activate(BundleContext bc) {
        this.bundleContext = bc;
        Platform.setImplicitExit(false);
        Stage splashStage = stageProvider.getSplashStage();
        PauseTransition delay = new PauseTransition(Duration.seconds(1.5));
        delay.setOnFinished(event -> {
            Platform.runLater(() -> {
                splashStage.close();
                ReadOnlyBooleanProperty showConfigScreen = configurationModal.getShowConfigScreen();
                showConfigScreen.addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                    updateScene(newValue);
                });
                updateScene(showConfigScreen.get());
            });
        });
        delay.play();

    }

    private void updateScene(boolean showConfigScreen) {
        if (showConfigScreen) {
            Platform.runLater(() -> {
                Scene scene = new Scene(configurationModal);
                stage.setTitle("Manage Credentials");
                stage.setScene(scene);
                stage.sizeToScene();
                stage.show();
            });
        } else {
            Platform.runLater(() -> {
                Scene scene = new Scene(root);
//                try {
                scene.getStylesheets().add(bundleContext.getBundle().getEntry("main.css").toExternalForm());
                //for scenic view styling since it can't osgi file reference
//                    scene.getStylesheets().add(new File("/home/dcnorris/NetBeansProjects/stackleader/kubefx/com.stackleader.kubefx.ui/src/main/resources/main.css").toURI().toURL().toExternalForm());
//                } catch (MalformedURLException ex) {
//                }
                stage.setTitle("KubeFx");
                stage.setScene(scene);
                stage.show();
            });
        }
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
        this.stageProvider = stageProvider;
        this.stage = stageProvider.getStage();
    }

    @Reference
    public void setRoot(Root root) {
        this.root = root;
    }

    @Reference
    public void setConfigurationModal(ConfigurationModal configurationModal) {
        this.configurationModal = configurationModal;
    }

}
