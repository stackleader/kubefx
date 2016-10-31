package com.stackleader.kubefx.kubernetes.impl;

import com.stackleader.kubefx.heapster.api.HeapsterClient.PodCpuUsage;
import com.stackleader.kubefx.heapster.api.HeapsterClient.PodMemoryLimit;
import com.stackleader.kubefx.heapster.api.HeapsterClient.PodMemoryUsage;
import com.stackleader.kubefx.heapster.api.HeapsterClient.PodNetworkIn;
import com.stackleader.kubefx.heapster.api.HeapsterClient.PodNetworkOut;
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

    @GET("api/v1/proxy/namespaces/kube-system/services/heapster/api/v1/model/namespaces/{namespace}/pods/{podName}/metrics/memory/limit")
    Call<PodMemoryLimit> podMemoryLimit(@Path("namespace") String namespace, @Path("podName") String podName);

    @GET("api/v1/proxy/namespaces/kube-system/services/heapster/api/v1/model/namespaces/{namespace}/pods/{podName}/metrics/memory/usage")
    Call<PodMemoryUsage> podMemoryUsage(@Path("namespace") String namespace, @Path("podName") String podName);
   
    
    @GET("api/v1/proxy/namespaces/kube-system/services/heapster/api/v1/model/namespaces/{namespace}/pods/{podName}/metrics/network/rx")
    Call<PodNetworkIn> podNetworkIn(@Path("namespace") String namespace, @Path("podName") String podName);

    @GET("api/v1/proxy/namespaces/kube-system/services/heapster/api/v1/model/namespaces/{namespace}/pods/{podName}/metrics/network/tx")
    Call<PodNetworkOut> podNetworkOut(@Path("namespace") String namespace, @Path("podName") String podName);

}
