package com.stackleader.kubefx.ui.selections;

import aQute.bnd.annotation.component.Component;
import com.stackleader.kubefx.kubernetes.api.model.Pod;
import com.stackleader.kubefx.tabs.api.TabProvider;
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
    private ObjectProperty<Optional<TabProvider>> selectedTabProvider;

    public SelectionInfoImpl() {
        selectedPod = new SimpleObjectProperty<>(Optional.empty());
        selectedTabProvider = new SimpleObjectProperty<>(Optional.empty());
    }

    @Override
    public ObjectProperty<Optional<Pod>> getSelectedPod() {
        return selectedPod;
    }

    @Override
    public ObjectProperty<Optional<TabProvider>> getSelectedTabProvider() {
        return selectedTabProvider;
    }
}
