/**
 * Copyright (C) 2015 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stackleader.kubefx.kubernetes.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import static com.google.common.base.Preconditions.checkNotNull;
import com.stackleader.kubefx.preferences.PreferenceUtils;
import io.fabric8.kubernetes.api.model.Config;
import io.fabric8.kubernetes.api.model.NamedAuthInfo;
import io.fabric8.kubernetes.api.model.NamedCluster;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.prefs.Preferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class for working with the YAML config file thats located in
 * <code>~/.kube/config</code> which is updated when you use commands
 * like <code>osc login</code> and <code>osc project myproject</code>
 */
public class KubeConfigUtils {

    private static final Logger LOG = LoggerFactory.getLogger(KubeConfigUtils.class);

    static Config parseConfig(File file) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return mapper.readValue(file, Config.class);
    }

    public static void parseKubeConfigToPreferenceNode(File configFile) {
        checkNotNull(configFile);
        if (configFile.exists()) {
            Preferences kubeClientConfigPrefNode = PreferenceUtils.getClassPrefsNode(KubernetesClient.class);
            try {
                ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
                final Config parseConfig = mapper.readValue(configFile, Config.class);
                String currentContext = parseConfig.getCurrentContext();
                final Optional<NamedCluster> currentCluster = parseConfig.getClusters().stream().filter(cluster -> cluster.getName().equals(currentContext)).findFirst();
                if (currentCluster.isPresent()) {
                    String certificateAuthorityData = currentCluster.get().getCluster().getCertificateAuthorityData();
                    String masterUrl = currentCluster.get().getCluster().getServer();
                    final List<NamedAuthInfo> configUsers = parseConfig.getUsers();
                    Optional<NamedAuthInfo> currentContextUser = configUsers.stream().filter(user -> user.getName().equals(parseConfig.getCurrentContext())).findFirst();
                    if (currentContextUser.isPresent()) {
                        NamedAuthInfo user = currentContextUser.get();
                        String username = user.getUser().getUsername();
                        String password = user.getUser().getPassword();
                        String clientCertData = user.getUser().getClientCertificateData();
                        String clientKeyData = user.getUser().getClientKeyData();
                        kubeClientConfigPrefNode.put("masterUrl", masterUrl);
                        kubeClientConfigPrefNode.put("username", username);
                        kubeClientConfigPrefNode.put("password", password);
                        kubeClientConfigPrefNode.put("certificateAuthorityData", certificateAuthorityData);
                        kubeClientConfigPrefNode.put("clientCertData", clientCertData);
                        kubeClientConfigPrefNode.put("clientKeyData", clientKeyData);
                        kubeClientConfigPrefNode.flush();
                    }
                }
            } catch (Exception ex) {
                LOG.error(ex.getMessage(), ex);
            }
        }
    }

}
