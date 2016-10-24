package com.stackleader.kubefx.kubernetes.impl;

import com.stackleader.kubefx.kubernetes.api.HeapsterClient.PodCpuUsage;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 *
 * @author dcnorris
 */
public interface HeapsterClientInternal {

//        api/v1/proxy/namespaces/kube-system/services/heapster/api/v1/model/namespaces/default/pods/nginx-proxy-v1.0.19-k3ib6/metrics/cpu/usage_rate
    @GET("api/v1/proxy/namespaces/kube-system/services/heapster/api/v1/model/namespaces/{namespace}/pods/{podName}/metrics/cpu/usage_rate")
    Call<PodCpuUsage> podCpuUsage(@Path("namespace") String namespace, @Path("podName") String podName);

}
