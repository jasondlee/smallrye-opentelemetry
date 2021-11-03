package io.smallrye.opentelemetry.implementation.cdi;

import static io.smallrye.opentelemetry.api.OpenTelemetryConfig.INSTRUMENTATION_NAME;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;

@Singleton
public class TracerProducer {
    @Inject
    OpenTelemetry openTelemetry;

    @Produces
    @Singleton
    public Tracer getTracer() {
        return openTelemetry.getTracer(INSTRUMENTATION_NAME);
    }
}