package com.stackleader.kubefx.kubernetes.api.model;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author dcnorris
 */
public class Pod {

    private StringProperty name;
    private StringProperty startTime;
    private StringProperty podIp;
    io.fabric8.kubernetes.api.model.Pod pod;

    public Pod(io.fabric8.kubernetes.api.model.Pod pod) {
        this.pod = pod;
        name = new SimpleStringProperty(pod.getMetadata().getName());
        startTime = new SimpleStringProperty(pod.getStatus().getStartTime());
        podIp = new SimpleStringProperty(pod.getStatus().getPodIP());
    }

    public ReadOnlyStringProperty getName() {
        return name;
    }

    public ReadOnlyStringProperty name() {
        return name;
    }

    public StringProperty getStartTime() {
        return startTime;
    }

    public StringProperty getPodIp() {
        return podIp;
    }

    public ReadOnlyStringProperty startTime() {
        return startTime;
    }

    public ReadOnlyStringProperty podIp() {
        return podIp;
    }

    @Override
    public String toString() {
        return name.get();
    }

}
