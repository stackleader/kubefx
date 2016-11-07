package com.stackleader.kubefx.kubernetes.api.model;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import java.util.Objects;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
public class BasicAuthCredential {

    private static final Logger LOG = LoggerFactory.getLogger(BasicAuthCredential.class);
    public static final String PASSWORD_PREF_KEY = "password";
    public static final String USERNAME_PREF_KEY = "username";
    public static final String NAME_PREF_KEY = "name";
    public static final String MASTER_URL_PREF_KEY = "masterUrl";
    public static final String HEAPSTER_URL_PREF_KEY = "heapsterUrl";
    public static final String IS_ACTIVE_PREF_KEY = "isActive";

    private StringProperty name;
    private StringProperty username;
    private StringProperty password;
    private StringProperty masterUrl;
    private StringProperty heapsterUrl;

    private BooleanProperty isActive;

    public BasicAuthCredential(String name, String username, String password, String masterUrl, String heapsterUrl, Preferences node) {
        this.name = new SimpleStringProperty(name);
        this.masterUrl = new SimpleStringProperty(masterUrl);
        this.heapsterUrl = new SimpleStringProperty(heapsterUrl);
        this.username = new SimpleStringProperty(username);
        this.password = new SimpleStringProperty(password);
        isActive = new SimpleBooleanProperty(false);
        node.put(MASTER_URL_PREF_KEY, Strings.nullToEmpty(this.masterUrl.get()));
        node.put(HEAPSTER_URL_PREF_KEY, Strings.nullToEmpty(this.heapsterUrl.get()));
        node.put(USERNAME_PREF_KEY, Strings.nullToEmpty(this.username.get()));
        node.put(PASSWORD_PREF_KEY, Strings.nullToEmpty(this.password.get()));
        node.putBoolean(IS_ACTIVE_PREF_KEY, isActive.get());
        isActive.addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            node.putBoolean(IS_ACTIVE_PREF_KEY, newValue);
        });
        this.name.addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            try {
                Preferences parent = node.parent();
                node.removeNode();
                Preferences updatedNode = parent.node(getNodeName(newValue));
                updatedNode.putBoolean(IS_ACTIVE_PREF_KEY, isActive.get());
                updatedNode.put(NAME_PREF_KEY, newValue);
                updatedNode.put(MASTER_URL_PREF_KEY, this.masterUrl.get());
                updatedNode.put(HEAPSTER_URL_PREF_KEY, this.heapsterUrl.get());
                updatedNode.put(USERNAME_PREF_KEY, this.username.get());
                updatedNode.put(PASSWORD_PREF_KEY, this.password.get());
            } catch (BackingStoreException ex) {
                LOG.error(ex.getMessage(), ex);
            }
        });
        this.masterUrl.addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            node.put(MASTER_URL_PREF_KEY, newValue);
        });
        this.heapsterUrl.addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            node.put(HEAPSTER_URL_PREF_KEY, newValue);
        });
        this.username.addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            node.put(USERNAME_PREF_KEY, newValue);
        });
        this.password.addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            node.put(PASSWORD_PREF_KEY, newValue);
        });
    }

    public String getName() {
        return name.get();
    }

    public StringProperty name() {
        return name;
    }

    public void setName(String name) {
        this.name.setValue(name);
    }

    public String getUsername() {
        return username.get();
    }

    public StringProperty Username() {
        return username;
    }

    public void setUsername(String username) {
        this.username.set(username);
    }

    public String getPassword() {
        return password.get();
    }

    public StringProperty password() {
        return password;
    }

    public void setPassword(String password) {
        this.password.set(password);
    }

    public String getMasterUrl() {
        return masterUrl.get();
    }

    public String getHeapsterUrl() {
        return heapsterUrl.get();
    }

    public StringProperty masterUrl() {
        return masterUrl;
    }

    public void setMasterUrl(String masterUrl) {
        this.masterUrl.set(masterUrl);
    }

    public void setHeapsterUrl(String heapsterUrl) {
        this.heapsterUrl.set(heapsterUrl);
    }

    public BooleanProperty isActive() {
        return isActive;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + Objects.hashCode(this.name);
        hash = 47 * hash + Objects.hashCode(this.username);
        hash = 47 * hash + Objects.hashCode(this.password);
        hash = 47 * hash + Objects.hashCode(this.masterUrl);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BasicAuthCredential other = (BasicAuthCredential) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.username, other.username)) {
            return false;
        }
        if (!Objects.equals(this.password, other.password)) {
            return false;
        }
        if (!Objects.equals(this.masterUrl, other.masterUrl)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return name.get();
    }

    private String getNodeName(String filePath) {
        HashCode hc = Hashing.md5().newHasher().putString(filePath, Charsets.UTF_8).hash();
        return hc.toString();
    }

}
