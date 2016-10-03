package com.stackleader.kubefx.kubernetes.api.model;

/**
 *
 * @author dcnorris
 */
public class ActiveConfig {

    private String name;
    private String masterUrl;
    private String username;
    private String password;

    public ActiveConfig(String name, String masterUrl, String username, String password) {
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

    public String getMasterUrl() {
        return masterUrl;
    }

    public void setMasterUrl(String masterUrl) {
        this.masterUrl = masterUrl;
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

}
