package com.stackleader.kubefx.kubernetes.impl;

import static com.google.common.base.Preconditions.checkNotNull;
import com.google.common.base.Strings;
import com.stackleader.kubefx.kubernetes.api.KubernetesClient;
import com.stackleader.kubefx.kubernetes.api.model.Node;
import com.stackleader.kubefx.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.internal.SSLUtils;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import static java.util.stream.Collectors.toList;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import okhttp3.Call;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE, configurationPid = KubernetesClient.PID)
public class KubernetesClientImpl implements KubernetesClient {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(KubernetesClientImpl.class);
    private io.fabric8.kubernetes.client.KubernetesClient client;
    private Config config;

    @Activate
    public void activate(Map<String, String> props) {
        try {
            initializeKubeClientConfig(props);
        } catch (Exception ex) {
            LOG.info("could not initialize kubernetes client, possibly there is not yet any configuration.");
        }
    }

    @Modified //TODO not sure why update isn't working with configuratino metatype 
    public void updated(Map<String, String> props) {
        try {
            initializeKubeClientConfig(props);
        } catch (Exception ex) {
            LOG.warn("could not update kubernetes client, invalid configuration, possibly there is not yet any configuration");
        }
    }

    private void initializeKubeClientConfig(Map<String, String> props) throws KubernetesClientException {
        checkNotNull(props, "configuration cannot be null");
        checkNotNull(props.get("masterUrl"), "masterUrl is required");
        checkNotNull(props.get("username"), "username is required");
        checkNotNull(props.get("password"), "password is required");
        String certificateAuthorityData = props.getOrDefault("certificateAuthorityData", "");
        String clientCertData = props.getOrDefault("clientCertData", "");
        String clientKeyData = props.getOrDefault("clientKeyData", "");
        if (Strings.isNullOrEmpty(certificateAuthorityData) || Strings.isNullOrEmpty(clientCertData) || Strings.isNullOrEmpty(clientKeyData)) {
            config = new ConfigBuilder()
                    .withMasterUrl(props.get("masterUrl"))
                    .withUsername(props.get("username"))
                    .withPassword(props.get("password"))
                    .withTrustCerts(true)
                    .build();
        } else {
            config = new ConfigBuilder()
                    .withMasterUrl(props.get("masterUrl"))
                    .withUsername(props.get("username"))
                    .withPassword(props.get("password"))
                    .withCaCertData(certificateAuthorityData)
                    .withClientCertData(clientCertData)
                    .withClientKeyData(clientKeyData)
                    .build();
        }
        client = new DefaultKubernetesClient(config);
    }

    @Override
    public List<Pod> getPods() {
        if (client != null) {
            return client.pods().list().getItems().stream().map(pod -> new Pod(pod)).collect(toList());
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public List<Pod> getPods(String namespace) {
        if (client != null) {
            return client.pods().inNamespace(namespace).list().getItems().stream().map(pod -> new Pod(pod)).collect(toList());
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public List<Node> getNodes() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Node> getNodes(String namespace) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Call tailLogs(Pod pod) {

        try {
            TrustManager[] trustManagers = SSLUtils.trustManagers(config);
            KeyManager[] keyManagers = SSLUtils.keyManagers(config);
            if (keyManagers != null || trustManagers != null || config.isTrustCerts()) {
                try {
                    SSLContext sslContext = SSLUtils.sslContext(keyManagers, trustManagers, config.isTrustCerts());

                    String credential = Credentials.basic(config.getUsername(), config.getPassword());
                    OkHttpClient client = new OkHttpClient.Builder()
                            .authenticator((Route route, Response rspns) -> rspns.request().newBuilder().addHeader("Authorization", credential).build())
                            .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustManagers[0])
                            .connectTimeout(10, TimeUnit.SECONDS)
                            .writeTimeout(30, TimeUnit.MINUTES)
                            .readTimeout(30, TimeUnit.MINUTES)
                            .build();
                    final String selectedNamespace = "default";

                    final String kube = config.getMasterUrl() + "api/" + config.getApiVersion() + "/namespaces/" + selectedNamespace + "/pods/" + pod.getName() + "/log?follow=true";
                    Request request = new Request.Builder()
                            .url(kube)
                            .build();
                    final Call newCall = client.newCall(request);

                    return newCall;
                } catch (GeneralSecurityException e) {
                    throw new AssertionError(); // The system has no TLS. Just give up.
                }

            }
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        }
        return null;
    }

}
