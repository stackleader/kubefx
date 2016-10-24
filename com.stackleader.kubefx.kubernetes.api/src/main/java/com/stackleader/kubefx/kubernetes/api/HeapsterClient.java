package com.stackleader.kubefx.kubernetes.api;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author dcnorris
 */
public interface HeapsterClient {

    public Optional<PodCpuUsage> getPodCpuUsage(String namespace, String podName);

    public static class PodCpuUsage {

        public List<Metric> metrics;
        public LocalDateTime latestTimestamp;
    }

    public static class Metric {

        public LocalDateTime timestamp;
        public Integer value;
    }
}
