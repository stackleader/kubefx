package com.stackleader.kubefx.ui;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.stackleader.kubefx.kubernetes.api.KubernetesClient;
import com.stackleader.kubefx.preferences.PreferenceUtils;
import java.io.IOException;
import java.util.Hashtable;
import java.util.prefs.Preferences;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import org.controlsfx.validation.decoration.StyleClassValidationDecoration;
import org.controlsfx.validation.decoration.ValidationDecoration;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tbee.javafx.scene.layout.MigPane;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true, provide = ConfigurationModal.class)
public class ConfigurationModal extends StackPane {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationModal.class);
    private TextField passwordField;
    private TextField usernameField;
    private TextField masterUrlField;
    private CheckBox remeber;
    private Button loginBtn;
    private Preferences kubeClientConfigPrefNode;
    private BooleanProperty showConfigScreen;
    private ConfigurationAdmin configAdmin;
    private BundleContext bc;

    public ConfigurationModal() {
        showConfigScreen = new SimpleBooleanProperty(true);
        kubeClientConfigPrefNode = PreferenceUtils.getClassPrefsNode(KubernetesClient.class);
        initLayout();

    }

    private void initLayout() {
        MigPane migPane = new MigPane("fill");
        masterUrlField = new TextField();
        usernameField = new TextField();
        passwordField = new TextField();

        Label masterUrlLabel = new Label("Kubernetes Master Url:");
        Label usernameLabel = new Label("Username:");
        Label passwordLabel = new Label("Password:");

        migPane.add(masterUrlLabel, "align right");
        migPane.add(masterUrlField, "wrap");

        migPane.add(usernameLabel, "align right");
        migPane.add(usernameField, "wrap");

        migPane.add(passwordLabel, "align right");
        migPane.add(passwordField, "wrap");

        loginBtn = new Button("Login");
        remeber = new CheckBox("Remember my credentials");
        remeber.setSelected(true);
        remeber.setDisable(true);   
        migPane.add(remeber, "skip, wrap");
        migPane.add(loginBtn, "growx, span 2");
        getChildren().add(migPane);
    }

    @Activate
    public void activate(BundleContext bc) {
        this.bc = bc;
        getStylesheets().add(bc.getBundle().getEntry("validation.css").toExternalForm());
        checkConfiguration();
        initListeners();

    }

    public ReadOnlyBooleanProperty getShowConfigScreen() {
        return showConfigScreen;
    }

    private void checkConfiguration() {
        String masterUrl = kubeClientConfigPrefNode.get("masterUrl", null);
        String username = kubeClientConfigPrefNode.get("username", null);
        String password = kubeClientConfigPrefNode.get("password", null);
        if (masterUrl != null && username != null && password != null) {
            updateKubeConfig(kubeClientConfigPrefNode);
            showConfigScreen.setValue(false);
        }
    }

    private void initListeners() {
        ValidationSupport validationSupport = new ValidationSupport();
        ValidationDecoration cssDecorator = new StyleClassValidationDecoration();
        validationSupport.setValidationDecorator(cssDecorator);
        validationSupport.registerValidator(masterUrlField, Validator.createEmptyValidator("Master Url is required"));
        loginBtn.setOnAction(action -> {
            validationSupport.getValidationResult();
            if (!validationSupport.isInvalid()) {
                String masterUrl = masterUrlField.getText().trim();
                String username = usernameField.getText().trim();
                String password = passwordField.getText().trim();
                if (!masterUrl.isEmpty()) {
                    if (remeber.isSelected()) {
                        kubeClientConfigPrefNode.put("masterUrl", masterUrl);
                        kubeClientConfigPrefNode.put("username", username);
                        kubeClientConfigPrefNode.put("password", password);
                        updateKubeConfig(kubeClientConfigPrefNode);
                    } else {
                        updateKubeConfig(masterUrl, username, password);
                    }
                    showConfigScreen.setValue(false);
                }
            }
        });
    }

    private void updateKubeConfig(String masterUrl, String username, String password) {
        try {
            Configuration configuration = configAdmin.getConfiguration(KubernetesClient.PID);
            Hashtable<String, String> properties = new Hashtable<>();
            if (masterUrl != null && username != null && password != null) {
                properties.put("masterUrl", masterUrl);
                properties.put("username", username);
                properties.put("password", password);
                configuration.update(properties);
            }
        } catch (IOException ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

    private void updateKubeConfig(Preferences node) {
        try {
            Configuration configuration = configAdmin.getConfiguration(KubernetesClient.PID);
            Hashtable<String, String> properties = new Hashtable<>();
            String masterUrl = node.get("masterUrl", null);
            String username = node.get("username", null);
            String password = node.get("password", null);
            if (masterUrl != null && username != null && password != null) {
                properties.put("masterUrl", masterUrl);
                properties.put("username", username);
                properties.put("password", password);
                configuration.update(properties);
            }
        } catch (IOException ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

    @Reference
    public void setConfigAdmin(ConfigurationAdmin configAdmin) {
        this.configAdmin = configAdmin;
    }

}
