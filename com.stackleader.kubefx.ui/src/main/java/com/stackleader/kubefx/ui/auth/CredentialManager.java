package com.stackleader.kubefx.ui.auth;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.stackleader.kubefx.config.api.PreferencesTabProvider;
import com.stackleader.kubefx.kubernetes.api.KubeConfigUtils;
import com.stackleader.kubefx.kubernetes.api.model.BasicAuthCredential;
import static com.stackleader.kubefx.kubernetes.api.model.BasicAuthCredential.HEAPSTER_URL_PREF_KEY;
import static com.stackleader.kubefx.kubernetes.api.model.BasicAuthCredential.IS_ACTIVE_PREF_KEY;
import static com.stackleader.kubefx.kubernetes.api.model.BasicAuthCredential.MASTER_URL_PREF_KEY;
import static com.stackleader.kubefx.kubernetes.api.model.BasicAuthCredential.NAME_PREF_KEY;
import static com.stackleader.kubefx.kubernetes.api.model.BasicAuthCredential.PASSWORD_PREF_KEY;
import static com.stackleader.kubefx.kubernetes.api.model.BasicAuthCredential.USERNAME_PREF_KEY;
import com.stackleader.kubefx.preferences.PreferenceUtils;
import com.stackleader.kubefx.selections.api.SelectionInfo;
import com.stackleader.kubefx.ui.actions.RefreshAction;
import com.stackleader.kubefx.ui.tabs.PodDetailsPane;
import static com.stackleader.kubefx.ui.utils.FXUtilities.runAndWait;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
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
    private TextField heapsterUrlTextField;
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
    private BasicAuthCredentialValidator basicAuthValidator;
    private final static FontAwesomeIconView ACTIVE_CREDENTIAL_ICON = new FontAwesomeIconView(FontAwesomeIcon.CHECK);

    public CredentialManager() {
        preferences = PreferenceUtils.getClassPrefsNode(CredentialManager.class);
        final URL resource = PodDetailsPane.class.getClassLoader().getResource("credentialManager.fxml");
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
        ACTIVE_CREDENTIAL_ICON.setFill(Color.FORESTGREEN);
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
                            if (credential.isActive().get()) {
//                                setStyle("-fx-background-color: #87e1fb;");
                                setGraphic(ACTIVE_CREDENTIAL_ICON);
                            }
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
            handleOkBtnAction();
        });
        cancelBtn.setOnAction(evt -> {
            cancelBtnAction();
        });
        BooleanBinding userPassEnabled = BooleanBinding.booleanExpression(anonCheckBox.selectedProperty()).and(Bindings.isNotNull(credentialList.getSelectionModel().selectedItemProperty()));
        usernameTextField.disableProperty().bind(userPassEnabled);
        passwordTextField.disableProperty().bind(userPassEnabled);
        testBtn.setOnAction(evt -> {
            validateCredential();
        });
        addBtn.setOnAction(evt -> {
            handleAddBtnAction();
        });
        removeBtn.setOnAction(evt -> {
            handleRemoveBtnActino();
        });
        final BooleanBinding credentialSelectionNull = Bindings.isNull(credentialList.getSelectionModel().selectedItemProperty());
        removeBtn.disableProperty().bind(credentialSelectionNull);
        testBtn.disableProperty().bind(credentialSelectionNull);
        hostNameTextField.disableProperty().bind(credentialSelectionNull);
        heapsterUrlTextField.disableProperty().bind(credentialSelectionNull);
        nameTextField.disableProperty().bind(credentialSelectionNull);
        hostNameTextField.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (heapsterUrlTextField.getText().isEmpty()) {
                    heapsterUrlTextField.setText(hostNameTextField.getText() + HEAPSTER_DEFAULT_URL_PATH);
                }
            }
        });
    }

    private void handleOkBtnAction() {
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
    }

    private void handleRemoveBtnActino() {
        try {
            BasicAuthCredential selectedItem = getSelectedCredential();
            Preferences node = preferences.node(selectedItem.getName());
            node.removeNode();
            credentialList.getItems().remove(selectedItem);
            credentialList.getSelectionModel().selectFirst();
            refreshSelections();
            save();
        } catch (BackingStoreException ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

    private void handleAddBtnAction() {
        String name = "DEFAULT_" + UUID.randomUUID().toString().subSequence(0, 3);
        Preferences node = preferences.node(name);
        BasicAuthCredential basicAuthCredential = new BasicAuthCredential(name, "", "", "", "", node);
        credentialList.getItems().add(basicAuthCredential);
        credentialList.getSelectionModel().select(basicAuthCredential);
        refreshSelections();
    }

    private void cancelBtnAction() {
        Optional.ofNullable(configTab.getTabPane()).ifPresent(tabPane -> {
            Optional.ofNullable(tabPane.getScene()).ifPresent(scene -> {
                Optional.ofNullable(scene.getWindow()).ifPresent(window -> {
                    Platform.runLater(() -> {
                        window.hide();
                    });
                });
            });
        });
    }

    private boolean validateCredential() {
        BasicAuthCredential selectedCredential = getSelectedCredential();
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
        BasicAuthCredential selectedCredential = getSelectedCredential();
        if (selectedCredential != null) {
            updateSelectedCredential(selectedCredential);
            triggerDelayedRefresh();
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

    private BasicAuthCredential getSelectedCredential() {
        BasicAuthCredential selectedCredential = credentialList.getSelectionModel().getSelectedItem();
        if (selectedCredential == null) {
            Optional<BasicAuthCredential> findFirst = credentialList.getItems().stream().filter(item -> item.isActive().get()).findFirst();
            if (findFirst.isPresent()) {
                credentialList.getSelectionModel().select(findFirst.get());
                return findFirst.get();
            }
        }
        return selectedCredential;
    }

    @Reference
    public void setSelectionInfo(SelectionInfo selectionInfo) {
        this.selectionInfo = selectionInfo;
    }

    private void addStoredCredentials() {
        try {
            Arrays.stream(preferences.childrenNames()).map(nodeName -> preferences.node(nodeName)).forEach(node -> {
                try {
                    String name = node.get(NAME_PREF_KEY, null);
                    String masterUrl = node.get(MASTER_URL_PREF_KEY, null);
                    String heapsterUrl = node.get(HEAPSTER_URL_PREF_KEY, null);
                    String username = node.get(USERNAME_PREF_KEY, null);
                    String password = node.get(PASSWORD_PREF_KEY, null);
                    boolean isActive = node.getBoolean(IS_ACTIVE_PREF_KEY, false);
                    if (name != null && masterUrl != null && username != null && password != null) {
                        BasicAuthCredential basicAuthCredential = new BasicAuthCredential(name, username, password, masterUrl, heapsterUrl, node);
                        basicAuthCredential.isActive().set(isActive);
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
            if (!credentialList.getItems().isEmpty()) {
                selectCredentialFromExistingList();
            } else {
                fallbackToSelectFromKubeCtlConfig();
            }
        } catch (Exception ex) {
            LOG.warn("could not find kubctl config file, skipping");
        }
    }

    private void fallbackToSelectFromKubeCtlConfig() throws IOException {
        File configFile = new File(System.getProperty("user.home") + File.separator + ".kube" + File.separator + "config");
        KubeConfigUtils.parseConfig(configFile).ifPresent(activeConfig -> {
            String name = activeConfig.getName();
            String masterUrl = activeConfig.getMasterUrl();
            String heapsterUrl = activeConfig.getMasterUrl() + HEAPSTER_DEFAULT_URL_PATH;
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
                        Preferences node = preferences.node(name);
                        BasicAuthCredential basicAuthCredential = new BasicAuthCredential(name, username, password, masterUrl, heapsterUrl, node);
                        credentialList.getItems().add(basicAuthCredential);
                        credentialList.getSelectionModel().select(basicAuthCredential);
                        refreshSelections();
                        basicAuthCredential.isActive().set(true);
                    } else {
                        credentialList.getSelectionModel().selectFirst();
                        refreshSelections();
                    }
                }
            } else {
                //TODO force credential add
            }
        });
    }
    private static final String HEAPSTER_DEFAULT_URL_PATH = "/api/v1/proxy/namespaces/kube-system/services/heapster/";

    private void selectCredentialFromExistingList() {
        Optional<BasicAuthCredential> activeCredential = credentialList.getItems().stream().filter(item -> item.isActive().get()).findFirst();
        if (activeCredential.isPresent()) {
            credentialList.getSelectionModel().select(activeCredential.get());
        } else {
            credentialList.getSelectionModel().selectFirst();
            refreshSelections();
        }
    }

    private void refreshSelections() {
        credentialList.getItems().forEach(item -> item.isActive().set(false));
        final BasicAuthCredential selectedItem = credentialList.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            selectedItem.isActive().set(true);
        }
    }

    private void addCredentialSelectionListener() {
        final ChangeListener<BasicAuthCredential> changeListener = new ChangeListener<BasicAuthCredential>() {
            @Override
            public void changed(ObservableValue<? extends BasicAuthCredential> ov, BasicAuthCredential t, BasicAuthCredential selectedCredential) {
                if (selectedCredential != null) {
                    if (!anonCheckBox.isSelected()) {
                        usernameTextField.setText(selectedCredential.getUsername());
                        passwordTextField.setText(selectedCredential.getPassword());
                    }
                    nameTextField.setText(selectedCredential.getName());
                    hostNameTextField.setText(selectedCredential.getMasterUrl());
                    heapsterUrlTextField.setText(selectedCredential.getHeapsterUrl());
                    selectionInfo.getSelectedCredential().set(Optional.ofNullable(selectedCredential));
                    credentialList.getItems().forEach(item -> item.isActive().set(false));
                    selectedCredential.isActive().set(true);
                    triggerDelayedRefresh();
                } else {
                    nameTextField.setText("");
                    hostNameTextField.setText("");
                    heapsterUrlTextField.setText("");
                    usernameTextField.setText("");
                    passwordTextField.setText("");
                    selectionInfo.getSelectedCredential().set(Optional.empty());
                }
            }
        };
        credentialList.getSelectionModel().selectedItemProperty().addListener(changeListener);
        selectionInfo.getSelectedCredential().set(Optional.ofNullable(credentialList.getSelectionModel().getSelectedItem()));

    }

    private void triggerDelayedRefresh() {
        FxTimer.runLater(Duration.ofMillis(750), () -> refreshAction.invokeAction());
    }

    @Reference
    public void setBasicAuthCredentialValidator(BasicAuthCredentialValidator basicAuthValidator) {
        this.basicAuthValidator = basicAuthValidator;
    }

    @Reference
    public void setRefreshAction(RefreshAction refreshAction) {
        this.refreshAction = refreshAction;
    }

    private void updateSelectedCredential(BasicAuthCredential selectedCredential) {
        String name = nameTextField.getText().trim();
        String masterUrl = hostNameTextField.getText().trim();
        String heapsterUrl = heapsterUrlTextField.getText().trim();
        String username = usernameTextField.getText().trim();
        String password = passwordTextField.getText().trim();
        selectedCredential.setMasterUrl(masterUrl);
        selectedCredential.setHeapsterUrl(heapsterUrl);
        selectedCredential.setName(name);
        selectedCredential.setUsername(username);
        selectedCredential.setPassword(password);
    }
}
