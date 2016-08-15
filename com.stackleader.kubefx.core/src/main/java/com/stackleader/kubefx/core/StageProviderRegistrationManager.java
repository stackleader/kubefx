package com.stackleader.kubefx.core;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import com.stackleader.kubefx.core.api.StageProvider;
import java.io.IOException;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// This class is needed to work around the problem of being unable to initialize fields from a javafx runtime context
@Component(immediate = true, provide = StageProviderRegistrationManager.class)
public class StageProviderRegistrationManager {

    private static final Logger LOG = LoggerFactory.getLogger(StageProviderRegistrationManager.class);
    private ServiceRegistration<StageProvider> registerService;
    private BundleContext bundleContext;
    private Stage stage;

    @Activate
    public void activate(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public void registerStageProvider(Stage stage, HostServices hostServices) {
        this.stage = stage;
        registerService = bundleContext.registerService(StageProvider.class, new MainStageProvider(stage, hostServices), null);
    }

    @Deactivate
    public void deactivate() throws IOException {
        Platform.runLater(() -> {
            stage.hide();

        });
        registerService.unregister();
    }

}
