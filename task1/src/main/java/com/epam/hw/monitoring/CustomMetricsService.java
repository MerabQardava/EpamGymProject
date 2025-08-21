package com.epam.hw.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.Getter;
import org.springframework.stereotype.Service;

@Service
public class CustomMetricsService {
    private final MeterRegistry registry;
    @Getter
    private final Timer requestTimer;



    public CustomMetricsService(MeterRegistry registry) {
        this.registry = registry;

        this.requestTimer = Timer.builder("app_request_duration_seconds")
                .description("Duration of requests in seconds")
                .register(registry);
    }

    public void recordRequest(String method, String endpoint) {
        Counter.builder("http_requests_total")
                .tag("method", method)
                .tag("endpoint", endpoint)
                .register(registry)
                .increment();
    }

}