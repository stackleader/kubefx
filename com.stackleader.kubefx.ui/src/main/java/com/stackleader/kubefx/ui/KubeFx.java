package com.stackleader.kubefx.ui;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;
import com.stackleader.kubefx.core.api.StageProvider;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
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
            });
        });
        delay.play();

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

}
