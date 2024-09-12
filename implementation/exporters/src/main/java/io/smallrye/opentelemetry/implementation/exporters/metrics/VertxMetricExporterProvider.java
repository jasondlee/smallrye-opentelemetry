package io.smallrye.opentelemetry.implementation.exporters.metrics;

import static io.smallrye.opentelemetry.implementation.exporters.OtlpExporterUtil.DEFAULT_OTLP_GRPC_ENDPOINT;
import static io.smallrye.opentelemetry.implementation.exporters.OtlpExporterUtil.DEFAULT_OTLP_HTTP_PROTOBUF_ENDPOINT;
import static io.smallrye.opentelemetry.implementation.exporters.OtlpExporterUtil.OTEL_DISABLE_SDK;
import static io.smallrye.opentelemetry.implementation.exporters.OtlpExporterUtil.OTEL_EXPORTER_OTLP_METRICS_PROTOCOL;
import static io.smallrye.opentelemetry.implementation.exporters.OtlpExporterUtil.OTEL_EXPORTER_OTLP_PROTOCOL;
import static io.smallrye.opentelemetry.implementation.exporters.OtlpExporterUtil.OTEL_EXPORTER_OTLP_TIMEOUT;
import static io.smallrye.opentelemetry.implementation.exporters.OtlpExporterUtil.OTLP_VALUE;
import static io.smallrye.opentelemetry.implementation.exporters.OtlpExporterUtil.PROTOCOL_GRPC;
import static io.smallrye.opentelemetry.implementation.exporters.OtlpExporterUtil.PROTOCOL_HTTP_PROTOBUF;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.exporter.internal.grpc.GrpcExporter;
import io.opentelemetry.exporter.internal.http.HttpExporter;
import io.opentelemetry.exporter.internal.otlp.metrics.MetricsRequestMarshaler;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.metrics.ConfigurableMetricExporterProvider;
import io.opentelemetry.sdk.metrics.export.AggregationTemporalitySelector;
import io.opentelemetry.sdk.metrics.export.DefaultAggregationSelector;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.smallrye.opentelemetry.implementation.exporters.OtlpExporterUtil;
import io.smallrye.opentelemetry.implementation.exporters.sender.VertxGrpcSender;
import io.smallrye.opentelemetry.implementation.exporters.sender.VertxHttpSender;
import io.vertx.core.Vertx;

public class VertxMetricExporterProvider implements ConfigurableMetricExporterProvider {
    protected static final String EXPORTER_NAME = "otlp";

    @Override
    public MetricExporter createExporter(ConfigProperties config) {
        if (Boolean.TRUE.equals(config.getBoolean(OTEL_DISABLE_SDK))) {
            return NoopMetricExporter.INSTANCE;
        }

        final String otlpProtocol = config.getString(OTEL_EXPORTER_OTLP_METRICS_PROTOCOL,
                config.getString(OTEL_EXPORTER_OTLP_PROTOCOL, PROTOCOL_GRPC));

        try {
            MetricExporter metricExporter;

            switch (otlpProtocol) {
                case PROTOCOL_GRPC:
                    metricExporter = new VertxGrpcMetricExporter(
                            new GrpcExporter<MetricsRequestMarshaler>(
                                    OtlpExporterUtil.OTLP_VALUE, // use the same as OTel does
                                    "metric", // use the same as OTel does
                                    new VertxGrpcSender(
                                            new URI(OtlpExporterUtil.getMetricsOtlpEndpoint(config,
                                                    DEFAULT_OTLP_GRPC_ENDPOINT)),
                                            VertxGrpcSender.GRPC_TRACE_SERVICE_NAME,
                                            true,
                                            config.getDuration(OTEL_EXPORTER_OTLP_TIMEOUT, Duration.ofSeconds(10)),
                                            OtlpExporterUtil.populateTracingExportHttpHeaders(),
                                            Vertx.vertx()),
                                    MeterProvider::noop),
                            AggregationTemporalitySelector.alwaysCumulative(),
                            DefaultAggregationSelector.getDefault());
                    break;
                case PROTOCOL_HTTP_PROTOBUF:
                    metricExporter = new VertxHttpMetricsExporter(
                            new HttpExporter<MetricsRequestMarshaler>(
                                    OtlpExporterUtil.OTLP_VALUE, // use the same as OTel does
                                    "metric", // use the same as OTel does
                                    new VertxHttpSender(
                                            new URI(OtlpExporterUtil.getMetricsOtlpEndpoint(config,
                                                    DEFAULT_OTLP_HTTP_PROTOBUF_ENDPOINT)),
                                            VertxHttpSender.METRICS_PATH,
                                            true,
                                            config.getDuration(OTEL_EXPORTER_OTLP_TIMEOUT, Duration.ofSeconds(10)),
                                            OtlpExporterUtil.populateTracingExportHttpHeaders(),
                                            "application/x-protobuf",
                                            Vertx.vertx()),
                                    MeterProvider::noop,
                                    false),
                            AggregationTemporalitySelector.alwaysCumulative(),
                            DefaultAggregationSelector.getDefault());
                    break;
                default:
                    throw new RuntimeException("Unsupported OTLP protocol: " + otlpProtocol);
            }

            return metricExporter;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getName() {
        // Using the same name as the OpenTelemetry SDK ("otlp") allows us to override its definition, providing our
        // Vertx-based exporters without any additional changes to the user's application.
        return EXPORTER_NAME;
    }
}
