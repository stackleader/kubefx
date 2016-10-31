package com.stackleader.kubefx.heapster.api;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author dcnorris
 */
public interface HeapsterClient {

    Optional<PodCpuUsage> getPodCpuUsage(String namespace, String podName);

    Optional<PodMemoryUsage> getPodMemoryUsage(String namespace, String podName);

    Optional<PodMemoryLimit> getPodMemoryLimit(String namespace, String podName);

    public static class PodNetworkOut {

        public List<MemoryMetric> metrics;
        public LocalDateTime latestTimestamp;
    }

    public static class PodNetworkIn {

        public List<MemoryMetric> metrics;
        public LocalDateTime latestTimestamp;
    }

    public static class PodMemoryLimit {

        public List<MemoryMetric> metrics;
        public LocalDateTime latestTimestamp;
    }

    public static class PodMemoryUsage {

        public List<MemoryMetric> metrics;
        public LocalDateTime latestTimestamp;
    }

    public static class PodCpuUsage {

        public List<Metric> metrics;
        public LocalDateTime latestTimestamp;
    }

    public static class MemoryMetric {

        public LocalDateTime timestamp;
        public MemoryString value;
    }

    public static class Metric {

        public LocalDateTime timestamp;
        public Integer value;
    }
}
