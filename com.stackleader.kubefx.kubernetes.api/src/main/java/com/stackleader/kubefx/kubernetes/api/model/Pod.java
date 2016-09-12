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

    public String getName() {
        return name.get();
    }

    public ReadOnlyStringProperty nameProperty() {
        return name;
    }

    public String getStartTime() {
        return startTime.get();
    }

    public StringProperty getPodIpProperty() {
        return podIp;
    }

    public ReadOnlyStringProperty startTimeProperty() {
        return startTime;
    }

    public String podIp() {
        return podIp.get();
    }

    @Override
    public String toString() {
        return name.get();
    }

}
