package com.stackleader.kubefx.kubernetes.impl;

import com.stackleader.kubefx.kubernetes.api.KubernetesClient;
import com.stackleader.kubefx.kubernetes.api.model.BasicAuthCredential;
import com.stackleader.kubefx.kubernetes.api.model.Node;
import com.stackleader.kubefx.kubernetes.api.model.Pod;
import com.stackleader.kubefx.kubernetes.api.model.Service;
import com.stackleader.kubefx.selections.api.SelectionInfo;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import static java.util.stream.Collectors.toList;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javax.net.ssl.SSLContext;
import okhttp3.Authenticator;
import okhttp3.Call;
import okhttp3.Credentials;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE, configurationPid = KubernetesClient.PID)
public class KubernetesClientImpl implements KubernetesClient {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(KubernetesClientImpl.class);
    private ObjectProperty<io.fabric8.kubernetes.client.KubernetesClient> client;
    private Config config;
    private SelectionInfo selectionInfo;

    public KubernetesClientImpl() {
        client = new SimpleObjectProperty<>();
    }

    @Activate
    public void activate() {
        try {
            initializeKubeClientConfig();
            initializeUpdateListener();
        } catch (Exception ex) {
            LOG.info("could not initialize kubernetes client, possibly there is not yet any configuration.");
        }
    }

    public void initializeUpdateListener() {
        selectionInfo.getSelectedCredential().addListener(new ChangeListener<Optional<BasicAuthCredential>>() {
            @Override
            public void changed(ObservableValue<? extends Optional<BasicAuthCredential>> observable, Optional<BasicAuthCredential> oldValue, Optional<BasicAuthCredential> newValue) {
                try {
                    initializeKubeClientConfig();
                } catch (Exception ex) {
                    LOG.info("could not initialize kubernetes client, enable debug logging for details.");
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(ex.getMessage(), ex);
                    }
                }
            }
        });

    }

    private void initializeKubeClientConfig() throws Exception {
        Optional<BasicAuthCredential> basicAuthCredential = selectionInfo.getSelectedCredential().get();
        if (basicAuthCredential.isPresent()) {
            config = new ConfigBuilder()
                    .withMasterUrl(basicAuthCredential.get().getMasterUrl())
                    .withUsername(basicAuthCredential.get().getUsername())
                    .withPassword(basicAuthCredential.get().getPassword())
                    .withTrustCerts(true)
                    .build();
            SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null,
                    new TrustSelfSignedStrategy()).build();
            httpClient = new OkHttpClient.Builder()
                    .authenticator(getBasicAuth(config.getUsername(), config.getPassword()))
                    .sslSocketFactory(sslContext.getSocketFactory())
                    .connectTimeout(3, TimeUnit.SECONDS)
                    .writeTimeout(5, TimeUnit.MINUTES)
                    .readTimeout(30, TimeUnit.MINUTES)
                    .build();
            client.set(new DefaultKubernetesClient(httpClient, config));
        }
    }
    private OkHttpClient httpClient;

    private Authenticator getBasicAuth(final String username, final String password) {
        return new Authenticator() {
            private int counter = 0;
            private HttpUrl previous = null;
            private String previousCred = null;

            @Override
            public Request authenticate(Route route, Response response) throws IOException {

                String credential = Credentials.basic(username, password);
                //try to detect if in auth fail loop
                if (counter > 3) {
                    if (previous.equals(route) && previousCred.equals(credential)) {
                        previous = response.request().url();
                        previousCred = credential;
                        counter++;
                        return null;
                    }
                }
                previous = response.request().url();
                previousCred = credential;
                return response.request().newBuilder().header("Authorization", credential).build();
            }
        };
    }

    @Override
    public List<Pod> getPods() {
        if (client.get() != null) {
            try {
                return client.get().pods().list().getItems().stream().map(pod -> new Pod(pod)).collect(toList());
            } catch (Throwable t) {
                logRemoteConnectionError(t);
            }
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public List<Pod> getPods(String namespace) {
        if (client.get() != null) {
            try {
                return client.get().pods().inNamespace(namespace).list().getItems().stream().map(pod -> new Pod(pod)).collect(toList());
            } catch (Throwable t) {
                logRemoteConnectionError(t);
            }
        }
        return Collections.EMPTY_LIST;
    }

    private void logRemoteConnectionError(Throwable t) {
        if (t.getCause() instanceof UnknownHostException) {
            LOG.warn("UnknownHostException, check url or internet connection");
        } else {
            LOG.warn("could not fetch resource, connection error", t);
        }
    }

    @Override
    public List<Node> getNodes() {
        if (client.get() != null) {
            try {
                return client.get().nodes().list().getItems().stream().map(node -> new Node(node)).collect(toList());
            } catch (Throwable t) {
                logRemoteConnectionError(t);
            }
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public List<Service> getServices() {
        if (client.get() != null) {
            try {
                final List<Service> services = client.get().services().list().getItems().stream().map(service -> new Service(service)).collect(toList());
                return services;
            } catch (Throwable t) {
                logRemoteConnectionError(t);
            }
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public Call tailLogs(Pod pod) {
        try {

            final String kube = config.getMasterUrl() + "api/" + config.getApiVersion() + "/namespaces/" + pod.getNamespace() + "/pods/" + pod.getName() + "/log?follow=true";
            Request request = new Request.Builder()
                    .url(kube)
                    .build();
            final Call newCall = httpClient.newCall(request);

            return newCall;
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        }
        return null;
    }

    ObjectProperty<io.fabric8.kubernetes.client.KubernetesClient> getClient() {
        return client;
    }

    @Reference
    public void setSelectionInfo(SelectionInfo selectionInfo) {
        this.selectionInfo = selectionInfo;
    }

}
