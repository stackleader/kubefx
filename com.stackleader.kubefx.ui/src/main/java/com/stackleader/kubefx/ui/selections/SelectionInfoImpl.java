package com.stackleader.kubefx.ui.selections;

import aQute.bnd.annotation.component.Component;
import com.stackleader.kubefx.kubernetes.api.model.Node;
import com.stackleader.kubefx.kubernetes.api.model.Pod;
import com.stackleader.kubefx.kubernetes.api.model.Service;
import com.stackleader.kubefx.tabs.api.TabProvider;
import com.stackleader.kubefx.kubernetes.api.model.BasicAuthCredential;
import java.util.Optional;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true)
public class SelectionInfoImpl implements SelectionInfo {

    private ObjectProperty<Optional<Pod>> selectedPod;
    private ObjectProperty<Optional<Node>> selectedNode;
    private ObjectProperty<Optional<Service>> selectedService;
    private ObjectProperty<Optional<TabProvider>> selectedTabProvider;
    private ObjectProperty<Optional<BasicAuthCredential>> selectedCredential;

    public SelectionInfoImpl() {
        selectedPod = new SimpleObjectProperty<>(Optional.empty());
        selectedNode = new SimpleObjectProperty<>(Optional.empty());
        selectedService = new SimpleObjectProperty<>(Optional.empty());
        selectedTabProvider = new SimpleObjectProperty<>(Optional.empty());
        selectedCredential = new SimpleObjectProperty<>(Optional.empty());
    }

    @Override
    public ObjectProperty<Optional<Pod>> getSelectedPod() {
        return selectedPod;
    }

    @Override
    public ObjectProperty<Optional<TabProvider>> getSelectedTabProvider() {
        return selectedTabProvider;
    }

    @Override
    public ObjectProperty<Optional<Node>> getSelectedNode() {
        return selectedNode;
    }

    @Override
    public ObjectProperty<Optional<Service>> getSelectedService() {
        return selectedService;
    }

    @Override
    public ObjectProperty<Optional<BasicAuthCredential>> getSelectedCredential() {
        return selectedCredential;
    }
}
