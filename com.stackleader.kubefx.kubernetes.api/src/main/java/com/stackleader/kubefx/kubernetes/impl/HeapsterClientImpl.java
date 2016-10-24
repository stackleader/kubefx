package com.stackleader.kubefx.kubernetes.impl;

import com.stackleader.kubefx.kubernetes.api.HeapsterClient;
import com.stackleader.kubefx.kubernetes.api.HeapsterClient.PodCpuUsage;
import com.stackleader.kubefx.kubernetes.api.KubernetesClient;
import java.io.IOException;
import java.util.Optional;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.LoggerFactory;
import retrofit2.Call;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true)
public class HeapsterClientImpl implements HeapsterClient {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(HeapsterClientImpl.class);
    private RetroFitServiceGenerator serviceGenerator;
    private KubernetesClientImpl kubernetesClient;
    private HeapsterClientInternal clientInternal;

    @Activate
    public void activate() {
        kubernetesClient.getClient().addListener(new ChangeListener<io.fabric8.kubernetes.client.KubernetesClient>() {
            @Override
            public void changed(ObservableValue<? extends io.fabric8.kubernetes.client.KubernetesClient> observable, io.fabric8.kubernetes.client.KubernetesClient oldValue, io.fabric8.kubernetes.client.KubernetesClient newValue) {
                if (newValue != null) {
                    clientInternal = serviceGenerator.createService(HeapsterClientInternal.class, newValue.getConfiguration());
                } else {
                    clientInternal = null;
                }
            }
        });
        if (kubernetesClient.getClient().get() != null) {
            clientInternal = serviceGenerator.createService(HeapsterClientInternal.class, kubernetesClient.getClient().get().getConfiguration());
        }
    }

    @Reference
    public void setKubernetesClient(KubernetesClient kubernetesClient) {
        this.kubernetesClient = (KubernetesClientImpl) kubernetesClient;
    }

    @Reference
    public void setRetroFitServiceGenerator(RetroFitServiceGenerator serviceGenerator) {
        this.serviceGenerator = serviceGenerator;
    }

    @Override
    public Optional<PodCpuUsage> getPodCpuUsage(String namespace, String podName) {
        if (clientInternal != null) {
            PodCpuUsage body = null;
            final Call<PodCpuUsage> podCpuUsage = clientInternal.podCpuUsage(namespace, podName);
            try {
                body = podCpuUsage.execute().body();
            } catch (IOException ex) {
                LOG.error(ex.getMessage(), ex);
            }
            return Optional.ofNullable(body);
        }
        return Optional.empty();
    }

}
