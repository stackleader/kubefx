package com.stackleader.kubefx.ui.auth;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.stackleader.kubefx.config.api.PreferencesTabProvider;
import com.stackleader.kubefx.kubernetes.api.KubeConfigUtils;
import com.stackleader.kubefx.kubernetes.api.KubernetesClient;
import com.stackleader.kubefx.preferences.PreferenceUtils;
import com.stackleader.kubefx.ui.actions.RefreshAction;
import com.stackleader.kubefx.ui.selections.SelectionInfo;
import com.stackleader.kubefx.ui.tabs.PodInfoPane;
import static com.stackleader.kubefx.ui.utils.FXUtilities.runAndWait;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Optional;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.reactfx.util.FxTimer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true, provide = {CredentialManager.class, PreferencesTabProvider.class})
public class CredentialManager implements PreferencesTabProvider {

    private static final Logger LOG = LoggerFactory.getLogger(CredentialManager.class);
    @FXML
    private ListView<BasicAuthCredential> credentialList;
    @FXML
    private Button addBtn;
    @FXML
    private Button removeBtn;
    @FXML
    private TextField nameTextField;
    @FXML
    private TextField hostNameTextField;
    @FXML
    private TextField usernameTextField;
    @FXML
    private PasswordField passwordTextField;
    @FXML
    private CheckBox anonCheckBox;
    @FXML
    private Button okBtn;
    @FXML
    private Button cancelBtn;
    @FXML
    private Button testBtn;

    @FXML
    private Label testResultLabel;

    private Tab configTab;
    private SelectionInfo selectionInfo;
    private Preferences preferences;
    private RefreshAction refreshAction;
    private KubernetesClient client;
    private ConfigurationAdmin configAdmin;
    private BasicAuthCredentialValidator basicAuthValidator;

    public CredentialManager() {
        preferences = PreferenceUtils.getClassPrefsNode(CredentialManager.class);
        final URL resource = PodInfoPane.class.getClassLoader().getResource("credentialManager.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(resource);
        fxmlLoader.setClassLoader(this.getClass().getClassLoader());
        fxmlLoader.setController(this);
        runAndWait(() -> {
            try {
                VBox root = fxmlLoader.load();
                configTab = new Tab("Credential Manager", root);
                initializeComponents();
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        });
    }

    @Activate
    public void activate() {
        runAndWait(() -> {
            addStoredCredentials();
            addCredentialSelectionListener();
            selectPreferredCredential();
        });
    }

    @Override
    public Tab getPreferencesTab() {
        return configTab;
    }

    @Override
    public int getTabWeight() {
        return 0;
    }

    private void initializeComponents() {
        credentialList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        credentialList.setCellFactory(new Callback<ListView<BasicAuthCredential>, ListCell<BasicAuthCredential>>() {
            @Override
            public ListCell<BasicAuthCredential> call(ListView<BasicAuthCredential> p) {
                ListCell<BasicAuthCredential> listCell = new ListCell<BasicAuthCredential>() {
                    @Override
                    protected void updateItem(BasicAuthCredential credential, boolean bln) {
                        super.updateItem(credential, bln);
                        if (credential != null) {
                            setText(credential.getName());
                        } else {
                            setGraphic(null);
                            setText(null);
                        }
                    }
                };
                return listCell;
            }
        });
        okBtn.setOnAction(evt -> {
            Optional.ofNullable(configTab.getTabPane()).ifPresent(tabPane -> {
                Optional.ofNullable(tabPane.getScene()).ifPresent(scene -> {
                    Optional.ofNullable(scene.getWindow()).ifPresent(window -> {
                        Platform.runLater(() -> {
                            if (save()) {
                                window.hide();
                            }
                        });
                    });
                });
            });
        });
        cancelBtn.setOnAction(evt -> {
            Optional.ofNullable(configTab.getTabPane()).ifPresent(tabPane -> {
                Optional.ofNullable(tabPane.getScene()).ifPresent(scene -> {
                    Optional.ofNullable(scene.getWindow()).ifPresent(window -> {
                        Platform.runLater(() -> {
                            window.hide();
                        });
                    });
                });
            });
        });
        BooleanBinding userPassEnabled = BooleanBinding.booleanExpression(anonCheckBox.selectedProperty()).and(Bindings.isNotNull(credentialList.getSelectionModel().selectedItemProperty()));
        usernameTextField.disableProperty().bind(userPassEnabled);
        passwordTextField.disableProperty().bind(userPassEnabled);
        testBtn.setOnAction(evt -> {
            validateCredential();
        });
    }

    private boolean validateCredential() {
        BasicAuthCredential selectedCredential = credentialList.getSelectionModel().getSelectedItem();
        if (selectedCredential != null) {
            updateSelectedCredential(selectedCredential);
            if (basicAuthValidator.credentialsValid(selectedCredential)) {
                Platform.runLater(() -> {
                    testResultLabel.setText("Success");
                    SUCCESS_ICON.setFill(Color.GREEN);
                    testResultLabel.setGraphic(SUCCESS_ICON);
                });
                return true;
            } else {
                credentialsFailed();
            }
        }
        return false;
    }

    private void credentialsFailed() {
        Platform.runLater(() -> {
            testResultLabel.setText("Could not validate credentials");
            ERROR_ICON.setFill(Color.RED);
            testResultLabel.setGraphic(ERROR_ICON);
        });
    }
    private static final FontAwesomeIconView ERROR_ICON = new FontAwesomeIconView(FontAwesomeIcon.INFO_CIRCLE);
    private static final FontAwesomeIconView SUCCESS_ICON = new FontAwesomeIconView(FontAwesomeIcon.CHECK_CIRCLE);

    private boolean save() {
        BasicAuthCredential selectedCredential = credentialList.getSelectionModel().getSelectedItem();
        if (selectedCredential != null) {
            updateSelectedCredential(selectedCredential);
            updateKubeConfig(selectedCredential);
            runAndWait(() -> {
                credentialList.getItems().clear();
                addStoredCredentials();
                if (credentialList.getItems().contains(selectedCredential)) {
                    credentialList.getSelectionModel().select(selectedCredential);
                }
            });
            return validateCredential();
        }
        return false;
    }

    @Reference
    public void setSelectionInfo(SelectionInfo selectionInfo) {
        this.selectionInfo = selectionInfo;
    }

    private void addStoredCredentials() {
        try {
            Arrays.stream(preferences.childrenNames()).map(nodeName -> preferences.node(nodeName)).forEach(node -> {
                try {
                    String name = node.name();
                    String masterUrl = node.get("masterUrl", null);
                    String username = node.get("username", null);
                    String password = node.get("password", null);
                    if (name != null && masterUrl != null && username != null && password != null) {
                        BasicAuthCredential basicAuthCredential = new BasicAuthCredential(name, username, password, masterUrl);
                        if (!credentialList.getItems().contains(basicAuthCredential)) {
                            credentialList.getItems().add(basicAuthCredential);
                        }
                    }
                } catch (Exception ex) {
                    LOG.error(ex.getMessage(), ex);
                }
            });
        } catch (BackingStoreException ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

    private void selectPreferredCredential() {
        try {
            File configFile = new File(System.getProperty("user.home") + File.separator + ".kube" + File.separator + "config");
            KubeConfigUtils.parseConfig(configFile).ifPresent(activeConfig -> {
                String name = activeConfig.getName();
                String masterUrl = activeConfig.getMasterUrl();
                String username = activeConfig.getUsername();
                String password = activeConfig.getPassword();
                if (name != null && masterUrl != null && username != null && password != null) {
                    Optional<BasicAuthCredential> match = credentialList.getItems().stream()
                            .filter(cred -> masterUrl.contains(cred.getMasterUrl()))
                            .filter(cred -> cred.getUsername().equals(username))
                            .filter(cred -> cred.getPassword().equals(password))
                            .findFirst();
                    if (match.isPresent()) {
                        credentialList.getSelectionModel().select(match.get());
                    } else {
                        if (credentialList.getItems().isEmpty()) {
                            BasicAuthCredential basicAuthCredential = new BasicAuthCredential(name, username, password, masterUrl);
                            credentialList.getItems().add(basicAuthCredential);
                            Preferences node = preferences.node(name);
                            node.put("masterUrl", masterUrl);
                            node.put("username", username);
                            node.put("password", password);
                            credentialList.getSelectionModel().selectFirst();
                        } else {
                            credentialList.getSelectionModel().selectFirst();
                        }
                    }
                } else {
                    //TODO force credential add
                }
            });
        } catch (Exception ex) {
            LOG.warn("could not find kubctl config file, skipping");
            if (!credentialList.getItems().isEmpty()) {
                credentialList.getSelectionModel().selectFirst();
            }
        }
    }

    private void addCredentialSelectionListener() {
        final ChangeListener<BasicAuthCredential> changeListener = new ChangeListener<BasicAuthCredential>() {
            @Override
            public void changed(ObservableValue<? extends BasicAuthCredential> ov, BasicAuthCredential t, BasicAuthCredential selectedCredential) {
                if (selectedCredential != null) {
                    nameTextField.setDisable(false);
                    hostNameTextField.setDisable(false);
                    if (!anonCheckBox.isSelected()) {
                        usernameTextField.setText(selectedCredential.getUsername());
                        passwordTextField.setText(selectedCredential.getPassword());
                    }
                    nameTextField.setText(selectedCredential.getName());
                    hostNameTextField.setText(selectedCredential.getMasterUrl());
                    selectionInfo.getSelectedCredential().set(Optional.ofNullable(selectedCredential));
                    updateKubeConfig(selectedCredential);
                } else {
                    nameTextField.setText("");
                    hostNameTextField.setText("");
                    usernameTextField.setText("");
                    passwordTextField.setText("");

                    nameTextField.setDisable(true);
                    hostNameTextField.setDisable(true);

                    selectionInfo.getSelectedCredential().set(Optional.empty());
                }
            }
        };
        credentialList.getSelectionModel().selectedItemProperty().addListener(changeListener);
        selectionInfo.getSelectedCredential().set(Optional.ofNullable(credentialList.getSelectionModel().getSelectedItem()));

    }

    private void updateKubeConfig(BasicAuthCredential authCredential) {
        try {
            Configuration configuration = configAdmin.getConfiguration(KubernetesClient.PID);
            Hashtable<String, String> properties = new Hashtable<>();
            if (authCredential.getMasterUrl() != null) {
                properties.put("masterUrl", authCredential.getMasterUrl());
                if (authCredential.getUsername() != null && authCredential.getPassword() != null) {
                    properties.put("username", authCredential.getUsername());
                    properties.put("password", authCredential.getPassword());
                } else {
                    properties.put("username", "");
                    properties.put("password", "");
                }
                configuration.update(properties);
                FxTimer.runLater(Duration.ofMillis(750), () -> refreshAction.invokeAction());

            }
        } catch (IOException ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

    @Reference
    public void setBasicAuthCredentialValidator(BasicAuthCredentialValidator basicAuthValidator) {
        this.basicAuthValidator = basicAuthValidator;
    }

    @Reference
    public void setRefreshAction(RefreshAction refreshAction) {
        this.refreshAction = refreshAction;
    }

    @Reference
    public void setClient(KubernetesClient client) {
        this.client = client;
    }

    @Reference
    public void setConfigAdmin(ConfigurationAdmin configAdmin) {
        this.configAdmin = configAdmin;
    }

    private void updateSelectedCredential(BasicAuthCredential selectedCredential) {
        String previousName = selectedCredential.getName();
        String name = nameTextField.getText().trim();
        String masterUrl = hostNameTextField.getText().trim();
        String username = usernameTextField.getText().trim();
        String password = passwordTextField.getText().trim();
        selectedCredential.setMasterUrl(masterUrl);
        selectedCredential.setName(name);
        selectedCredential.setUsername(username);
        selectedCredential.setPassword(password);
        Preferences node = preferences.node(previousName);
        node.put("masterUrl", masterUrl);
        node.put("username", username);
        node.put("password", password);
    }
}
