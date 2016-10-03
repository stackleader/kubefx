package com.stackleader.kubefx.ui.auth;

import javax.validation.constraints.NotNull;

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

}
