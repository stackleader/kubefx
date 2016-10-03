package com.stackleader.kubefx.ui.auth;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.stackleader.kubefx.config.api.PreferencesTabProvider;
import com.stackleader.kubefx.kubernetes.api.KubeConfigUtils;
import com.stackleader.kubefx.preferences.PreferenceUtils;
import com.stackleader.kubefx.ui.selections.SelectionInfo;
import com.stackleader.kubefx.ui.tabs.PodInfoPane;
import static com.stackleader.kubefx.ui.utils.FXUtilities.runAndWait;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Optional;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
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
    private TextField passwordTextField;
    @FXML
    private CheckBox anonCheckBox;
    @FXML
    private Button okBtn;
    @FXML
    private Button cancelBtn;

    @FXML
    private Label testResultLabel;

    private Tab configTab;
    private SelectionInfo selectionInfo;
    private Preferences preferences;

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
            selectPreferredCredential();
            addCredentialSelectionListener();
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
                            save();
                            window.hide();
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
    }

    private void save() {

    }

    @Reference
    public void setSelectionInfo(SelectionInfo selectionInfo) {
        this.selectionInfo = selectionInfo;
    }

    private void addStoredCredentials() {
        try {
            Arrays.stream(preferences.childrenNames()).map(nodeName -> preferences.node(nodeName)).forEach(node -> {
                try {
                    String name = node.get("name", null);
                    String masterUrl = node.get("masterUrl", null);
                    String username = node.get("username", null);
                    String password = node.get("password", null);
                    if (name != null && masterUrl != null && username != null && password != null) {
                        BasicAuthCredential basicAuthCredential = new BasicAuthCredential(name, username, password, masterUrl);
                        credentialList.getItems().add(basicAuthCredential);
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
                        } else {
                            credentialList.getSelectionModel().selectFirst();
                        }
                    }
                } else {
                    //TODO force credential add
                }
            });
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

    private void addCredentialSelectionListener() {
        final ChangeListener<BasicAuthCredential> changeListener = new ChangeListener<BasicAuthCredential>() {
            @Override
            public void changed(ObservableValue<? extends BasicAuthCredential> ov, BasicAuthCredential t, BasicAuthCredential selectedCredential) {
                nameTextField.setText(selectedCredential.getName());
                hostNameTextField.setText(selectedCredential.getMasterUrl());
                usernameTextField.setText(selectedCredential.getUsername());
                passwordTextField.setText(selectedCredential.getPassword());
                selectionInfo.getSelectedCredential().set(Optional.ofNullable(selectedCredential));
            }
        };
        credentialList.getSelectionModel().selectedItemProperty().addListener(changeListener);
        selectionInfo.getSelectedCredential().set(Optional.ofNullable(credentialList.getSelectionModel().getSelectedItem()));

    }

}
