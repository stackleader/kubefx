package com.stackleader.kubefx.kubernetes.impl;

import com.stackleader.kubefx.heapster.api.HeapsterClient;
import com.stackleader.kubefx.heapster.api.HeapsterClient.PodCpuUsage;
import com.stackleader.kubefx.kubernetes.api.model.BasicAuthCredential;
import com.stackleader.kubefx.selections.api.SelectionInfo;
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
    private HeapsterClientInternal clientInternal;
    private SelectionInfo selectionInfo;

    @Activate
    public void activate() {
        selectionInfo.getSelectedCredential().addListener(new ChangeListener<Optional<BasicAuthCredential>>() {
            @Override
            public void changed(ObservableValue<? extends Optional<BasicAuthCredential>> observable,
                    Optional<BasicAuthCredential> oldValue,
                    Optional<BasicAuthCredential> newValue) {
                if (newValue.isPresent()) {
                    try {
                        clientInternal = serviceGenerator.createService(HeapsterClientInternal.class, newValue.get());
                    } catch (Exception ex) {
                        LOG.error("Could not create heapster client, enable debug logging for more details");
                        if (LOG.isDebugEnabled()) {
                            LOG.debug(ex.getMessage(), ex);
                        }
                    }
                } else {
                    clientInternal = null;
                }
            }
        });

        if (selectionInfo.getSelectedCredential().get().isPresent()) {
            try {
                clientInternal = serviceGenerator.createService(HeapsterClientInternal.class, selectionInfo.getSelectedCredential().get().get());
            } catch (Exception ex) {
                LOG.error("Could not create heapster client, enable debug logging for more details");
                if (LOG.isDebugEnabled()) {
                    LOG.debug(ex.getMessage(), ex);
                }
            }
        }
    }

    @Reference
    public void setSelectionInfo(SelectionInfo selectionInfo) {
        this.selectionInfo = selectionInfo;
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

    @Override
    public Optional<PodMemoryLimit> getPodMemoryLimit(String namespace, String podName) {
        if (clientInternal != null) {
            PodMemoryLimit body = null;
            final Call<PodMemoryLimit> podCpuUsage = clientInternal.podMemoryLimit(namespace, podName);
            try {
                body = podCpuUsage.execute().body();
            } catch (IOException ex) {
                LOG.error(ex.getMessage(), ex);
            }
            return Optional.ofNullable(body);
        }
        return Optional.empty();
    }

    @Override
    public Optional<PodMemoryUsage> getPodMemoryUsage(String namespace, String podName) {
        if (clientInternal != null) {
            PodMemoryUsage body = null;
            final Call<PodMemoryUsage> podCpuUsage = clientInternal.podMemoryUsage(namespace, podName);
            try {
                body = podCpuUsage.execute().body();
            } catch (IOException ex) {
                LOG.error(ex.getMessage(), ex);
            }
            return Optional.ofNullable(body);
        }
        return Optional.empty();
    }

    @Override
    public Optional<PodNetworkIn> getPodNetworkIn(String namespace, String podName) {
        if (clientInternal != null) {
            PodNetworkIn body = null;
            final Call<PodNetworkIn> podCpuUsage = clientInternal.podNetworkIn(namespace, podName);
            try {
                body = podCpuUsage.execute().body();
            } catch (IOException ex) {
                LOG.error(ex.getMessage(), ex);
            }
            return Optional.ofNullable(body);
        }
        return Optional.empty();
    }

    @Override
    public Optional<PodNetworkOut> getPodNetworkOut(String namespace, String podName) {
        if (clientInternal != null) {
            PodNetworkOut body = null;
            final Call<PodNetworkOut> podCpuUsage = clientInternal.podNetworkOut(namespace, podName);
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
