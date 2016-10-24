package com.stackleader.kubefx.ui.auth;

import java.util.Objects;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.URL;

/**
 *
 * @author dcnorris
 */
public class BasicAuthCredential {

    @NotNull
    private String name;
    @NotNull
    private String username;
    @NotNull
    private String password;
    @URL
    @NotNull
    private String masterUrl;

    public BasicAuthCredential(String name, String username, String password, String masterUrl) {
        this.name = name;
        this.masterUrl = masterUrl;
        this.username = username;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMasterUrl() {
        return masterUrl;
    }

    public void setMasterUrl(String masterUrl) {
        this.masterUrl = masterUrl;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + Objects.hashCode(this.name);
        hash = 67 * hash + Objects.hashCode(this.username);
        hash = 67 * hash + Objects.hashCode(this.password);
        hash = 67 * hash + Objects.hashCode(this.masterUrl);
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
        return true;
    }

}
