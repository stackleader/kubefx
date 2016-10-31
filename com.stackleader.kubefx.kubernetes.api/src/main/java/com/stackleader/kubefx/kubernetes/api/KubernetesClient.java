package com.stackleader.kubefx.kubernetes.api;

import com.stackleader.kubefx.kubernetes.api.model.Node;
import com.stackleader.kubefx.kubernetes.api.model.Pod;
import com.stackleader.kubefx.kubernetes.api.model.Service;
import java.util.List;
import okhttp3.Call;

/**
 *
 * @author dcnorris
 */
public interface KubernetesClient {

    static final String PID = "com.stackleader.kubefx.kubernetes.api.KubernetesClient";


    List<Pod> getPods();

    List<Pod> getPods(String namespace);

    List<Node> getNodes();

    List<Service> getServices();

    Call tailLogs(Pod pod);

}
