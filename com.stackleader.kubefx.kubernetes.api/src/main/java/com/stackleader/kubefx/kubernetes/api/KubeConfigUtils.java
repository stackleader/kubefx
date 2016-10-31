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
import com.stackleader.kubefx.kubernetes.api.model.ActiveConfig;
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
import static com.stackleader.kubefx.kubernetes.api.model.BasicAuthCredential.PASSWORD_PREF_KEY;
import static com.stackleader.kubefx.kubernetes.api.model.BasicAuthCredential.USERNAME_PREF_KEY;
import static com.stackleader.kubefx.kubernetes.api.model.BasicAuthCredential.MASTER_URL_PREF_KEY;

/**
 * Helper class for working with the YAML config file thats located in
 * <code>~/.kube/config</code> which is updated when you use commands
 * like <code>osc login</code> and <code>osc project myproject</code>
 */
public class KubeConfigUtils {

    private static final Logger LOG = LoggerFactory.getLogger(KubeConfigUtils.class);

    public static Optional<ActiveConfig> parseConfig(File file) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        Config config = mapper.readValue(file, Config.class);
        String currentContext = config.getCurrentContext();
        final Optional<NamedCluster> currentCluster = config.getClusters().stream().filter(cluster -> cluster.getName().equals(currentContext)).findFirst();
        if (currentCluster.isPresent()) {
            String masterUrl = currentCluster.get().getCluster().getServer();
            final List<NamedAuthInfo> configUsers = config.getUsers();
            Optional<NamedAuthInfo> currentContextUser = configUsers.stream().filter(user -> user.getName().equals(config.getCurrentContext())).findFirst();
            if (currentContextUser.isPresent()) {
                NamedAuthInfo user = currentContextUser.get();
                String name = user.getName();
                String username = user.getUser().getUsername();
                String password = user.getUser().getPassword();
                if (name != null && masterUrl != null && username != null && password != null) {
                    ActiveConfig activeConfig = new ActiveConfig(name, masterUrl, username, password);
                    return Optional.ofNullable(activeConfig);
                }
            }
        }
        return Optional.empty();
    }

    public static void parseKubeConfigToPreferenceNode(File configFile) {
        checkNotNull(configFile);
        if (configFile.exists()) {
            Preferences kubeClientConfigPrefNode = PreferenceUtils.getClassPrefsNode(KubernetesClient.class);
            try {
                ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
                final Config parseConfig = mapper.readValue(configFile, Config.class);
                initializeFromConfig(parseConfig, kubeClientConfigPrefNode);
            } catch (Exception ex) {
                LOG.error(ex.getMessage(), ex);
            }
        }
    }

    private static void initializeFromConfig(final Config parseConfig, Preferences kubeClientConfigPrefNode) throws Exception {
        String currentContext = parseConfig.getCurrentContext();
        final Optional<NamedCluster> currentCluster = parseConfig.getClusters().stream().filter(cluster -> cluster.getName().equals(currentContext)).findFirst();
        if (currentCluster.isPresent()) {
            String masterUrl = currentCluster.get().getCluster().getServer();
            final List<NamedAuthInfo> configUsers = parseConfig.getUsers();
            Optional<NamedAuthInfo> currentContextUser = configUsers.stream().filter(user -> user.getName().equals(parseConfig.getCurrentContext())).findFirst();
            if (currentContextUser.isPresent()) {
                NamedAuthInfo user = currentContextUser.get();
                String name = user.getName();
                String username = user.getUser().getUsername();
                String password = user.getUser().getPassword();
                if (name != null && masterUrl != null && username != null && password != null) {
                    kubeClientConfigPrefNode.put("name", name);
                    kubeClientConfigPrefNode.put(MASTER_URL_PREF_KEY, masterUrl);
                    kubeClientConfigPrefNode.put(USERNAME_PREF_KEY, username);
                    kubeClientConfigPrefNode.put(PASSWORD_PREF_KEY, password);
                }
                kubeClientConfigPrefNode.flush();
            }
        }
    }

}
