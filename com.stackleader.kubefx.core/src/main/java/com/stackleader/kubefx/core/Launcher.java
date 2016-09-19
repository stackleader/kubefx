package com.stackleader.kubefx.core;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class Launcher extends Application {

    private static final Logger LOG = LoggerFactory.getLogger(Launcher.class);
    private StageProviderRegistrationManager stageRegistrationManager;
    private Stage splashStage;

    @Activate
    public void activate() {
        Executors.defaultThreadFactory().newThread(() -> {
            Thread.currentThread().setContextClassLoader(
                    this.getClass().getClassLoader());
            LOG.info("Launching FX Application");
            try {
                launch();
            } catch (IllegalStateException ex) {
                //Can be caused by relaunch when javafx process is already running
                handleRestartEvent();
            }
        }).start();

    }

    private final EventHandler<WindowEvent> onClose = (WindowEvent event) -> {
        if (event.getEventType().equals(WindowEvent.WINDOW_CLOSE_REQUEST)) {
            LOG.info("Received request to shutdown container");
            CompletableFuture.runAsync(() -> {
                try {
                    BundleContext bc = FrameworkUtil.getBundle(Launcher.class).getBundleContext();
                    Bundle bundle = bc.getBundle(0);
                    bundle.stop();
                } catch (Exception e) {
                    System.err.println("Error when shutting down Apache Karaf");
                }
            });
        }
    };

    private void handleRestartEvent() {
        Platform.runLater(() -> {
            Stage newStage = new Stage();
            Stage newSplashStage = new Stage();
            newStage.setOnCloseRequest(onClose);
            stageRegistrationManager.registerStageProvider(newStage,newSplashStage, getHostServices());
        });
    }

  @Override
    public void start(Stage primaryStage) throws Exception {
        final BundleContext bundleContext = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        ServiceReference<StageProviderRegistrationManager> serviceReference = bundleContext.getServiceReference(StageProviderRegistrationManager.class);
        StageProviderRegistrationManager stageRegistrationManager = bundleContext.getService(serviceReference);
        initSplashScreen();
        stageRegistrationManager.registerStageProvider(primaryStage, splashStage, getHostServices());
        primaryStage.setOnCloseRequest(onClose);
    }

    public void initSplashScreen() {
        splashStage = new Stage(StageStyle.TRANSPARENT);
        splashStage.setAlwaysOnTop(true);
        ProgressBar bar = new ProgressBar();
        bar.setPadding(new Insets(0, 0, 10, 20));
        BorderPane p = new BorderPane();
        bar.prefWidthProperty().bind(p.widthProperty().subtract(20));
        BorderPane center = new BorderPane();
        Label label = new Label("Starting KubeFx");
        label.setPadding(new Insets(0, 0, 0, 20));
        label.setTextFill(Color.web("#B2A571"));
        VBox vbox = new VBox(label, bar);
        center.setCenter(vbox);
        p.setBottom(center);
        BundleContext bc = FrameworkUtil.getBundle(Launcher.class).getBundleContext();
        StackPane stack = new StackPane(new ImageView(bc.getBundle().getEntry("splash-darker.png").toExternalForm()), p);
        splashStage.setScene(new Scene(stack, 550, 250));
        splashStage.show();
    }

    @Reference
    public void setStageRegistrationManger(StageProviderRegistrationManager registrationManager) {
        this.stageRegistrationManager = registrationManager;
    }
}
