package com.stackleader.kubefx.kubernetes.api.model;

import com.google.common.base.Joiner;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;
import static java.util.stream.Collectors.toList;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author dcnorris
 */
public class Service {

    private StringProperty name;
    private StringProperty startTime;
    private StringProperty serviceIp;
    io.fabric8.kubernetes.api.model.Service service;

    public Service(io.fabric8.kubernetes.api.model.Service service) {
        this.service = service;
        name = new SimpleStringProperty(service.getMetadata().getName());
        startTime = new SimpleStringProperty(service.getMetadata().getCreationTimestamp());
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

    public String getClusterIp() {
        return service.getSpec().getClusterIP();
    }

    public String getExternalIp() {
        return Joiner.on(",").join(service.getStatus().getLoadBalancer().getIngress().stream().map(ingress->ingress.getIp()).collect(toList()));
    }

    public String getPorts() {
        List<String> portStrings = service.getSpec().getPorts().stream().map(port -> port.getPort() + "/" + port.getProtocol()).collect(toList());
        String portsInfo = Joiner.on(",").join(portStrings);
        return portsInfo;
    }

    public String getAge() {
        LocalDate dt = LocalDate.parse(startTime.get(), DateTimeFormatter.ISO_ZONED_DATE_TIME);
        LocalDate now = LocalDate.now();
        int ageInDays = Period.between(dt, now).getDays();
        return ageInDays + "d";
    }

//    public String getStatus() {
//        return service.getStatus().getPhase();
//    }
    public StringProperty getServiceIpProperty() {
        return serviceIp;
    }

    public ReadOnlyStringProperty startTimeProperty() {
        return startTime;
    }

    public String serviceIp() {
        return serviceIp.get();
    }

    @Override
    public String toString() {
        return name.get();
    }
}
