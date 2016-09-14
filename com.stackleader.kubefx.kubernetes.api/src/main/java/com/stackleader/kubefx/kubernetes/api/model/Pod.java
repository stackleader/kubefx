package com.stackleader.kubefx.kubernetes.api.model;

import io.fabric8.kubernetes.api.model.ContainerStatus;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;
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

    public String getReady() {
        final List<ContainerStatus> containerStatuses = pod.getStatus().getContainerStatuses();
        int size = containerStatuses.size();
        int readyCount = containerStatuses.stream().filter(cs -> cs.getReady()).mapToInt(cs -> 1).sum();
        return readyCount + "/" + size;
    }

    public String getAge() {
        LocalDate dt = LocalDate.parse(startTime.get(), DateTimeFormatter.ISO_ZONED_DATE_TIME);
        LocalDate now = LocalDate.now();
        int ageInDays = Period.between(dt, now).getDays();
        return ageInDays + "d";
    }

    public String getStatus() {
        return pod.getStatus().getPhase();
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
