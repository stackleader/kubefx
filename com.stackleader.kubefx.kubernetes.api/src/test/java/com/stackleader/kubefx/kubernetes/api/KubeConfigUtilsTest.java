/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.stackleader.kubefx.kubernetes.api;

import com.stackleader.kubefx.kubernetes.api.KubeConfigUtils;
import io.fabric8.kubernetes.api.model.Config;
import io.fabric8.kubernetes.api.model.NamedAuthInfo;
import java.io.File;
import java.util.List;
import java.util.Optional;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author dcnorris
 */
public class KubeConfigUtilsTest {
    
       @Test
    public void testParseConfig() throws Exception {
//        File configFile = new File(System.getProperty("user.home") + File.separator + ".kube" + File.separator + "config");
        File configFile = new File(KubeConfigUtilsTest.class.getClassLoader().getResource("kube-config").getFile());
        Config parseConfig = KubeConfigUtils.parseConfig(configFile);

        assertEquals(parseConfig.getApiVersion(), "v1");
        List<String> clusters = parseConfig.getClusters().stream().map(cluster -> cluster.getName()).collect(toList());
        assertThat(clusters, contains("gke_us-east1-c_production", "gke_b_cluster-1", "production"));
        List<String> contexts = parseConfig.getContexts().stream().map(context -> context.getName()).collect(toList());
        assertThat(contexts, contains("gke_us-east1-c_production", "gke_cluster-1"));

        Optional<NamedAuthInfo> currentContextUser = parseConfig.getUsers().stream().filter(user -> user.getName().equals(parseConfig.getCurrentContext())).findFirst();
        assertThat("currentContext user exist", currentContextUser.isPresent());
        currentContextUser.ifPresent(user -> {
            String username = user.getUser().getUsername();
            String password = user.getUser().getPassword();
            String clientCertData = user.getUser().getClientCertificateData();
            String clientKeyData = user.getUser().getClientKeyData();
            assertThat(username, equalTo("admin"));
            assertThat(password, equalTo("changme"));

        });
    }
    
}
