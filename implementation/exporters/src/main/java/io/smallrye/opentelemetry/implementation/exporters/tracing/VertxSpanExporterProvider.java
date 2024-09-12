package io.smallrye.opentelemetry.implementation.exporters.tracing;

import static io.smallrye.opentelemetry.implementation.exporters.OtlpExporterUtil.DEFAULT_OTLP_GRPC_ENDPOINT;
import static io.smallrye.opentelemetry.implementation.exporters.OtlpExporterUtil.DEFAULT_OTLP_HTTP_PROTOBUF_ENDPOINT;
import static io.smallrye.opentelemetry.implementation.exporters.OtlpExporterUtil.OTEL_DISABLE_SDK;
import static io.smallrye.opentelemetry.implementation.exporters.OtlpExporterUtil.OTEL_EXPORTER_OTLP_PROTOCOL;
import static io.smallrye.opentelemetry.implementation.exporters.OtlpExporterUtil.OTEL_EXPORTER_OTLP_TIMEOUT;
import static io.smallrye.opentelemetry.implementation.exporters.OtlpExporterUtil.OTEL_EXPORTER_OTLP_TRACES_PROTOCOL;
import static io.smallrye.opentelemetry.implementation.exporters.OtlpExporterUtil.PROTOCOL_GRPC;
import static io.smallrye.opentelemetry.implementation.exporters.OtlpExporterUtil.PROTOCOL_HTTP_PROTOBUF;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.exporter.internal.grpc.GrpcExporter;
import io.opentelemetry.exporter.internal.http.HttpExporter;
import io.opentelemetry.exporter.internal.otlp.traces.TraceRequestMarshaler;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.traces.ConfigurableSpanExporterProvider;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.smallrye.opentelemetry.implementation.exporters.OtlpExporterUtil;
import io.smallrye.opentelemetry.implementation.exporters.sender.VertxGrpcSender;
import io.smallrye.opentelemetry.implementation.exporters.sender.VertxHttpSender;
import io.vertx.core.Vertx;

public class VertxSpanExporterProvider implements ConfigurableSpanExporterProvider {

    protected static final String EXPORTER_NAME = "otlp";

    @Override
    public SpanExporter createExporter(final ConfigProperties config) {
        if (Boolean.TRUE.equals(config.getBoolean(OTEL_DISABLE_SDK))) {
            return NoopSpanExporter.INSTANCE;
        }
        try {
            final String otlpProtocol = config.getString(OTEL_EXPORTER_OTLP_TRACES_PROTOCOL,
                    config.getString(OTEL_EXPORTER_OTLP_PROTOCOL, PROTOCOL_GRPC));

            switch (otlpProtocol) {
                case PROTOCOL_GRPC:
                    return new VertxGrpcSpanExporter(new GrpcExporter<TraceRequestMarshaler>(
                            OtlpExporterUtil.OTLP_VALUE, // use the same as OTel does
                            "span", // use the same as OTel does
                            new VertxGrpcSender(
                                    new URI(OtlpExporterUtil.getTracingOtlpEndpoint(config, DEFAULT_OTLP_GRPC_ENDPOINT)),
                                    VertxGrpcSender.GRPC_TRACE_SERVICE_NAME,
                                    true,
                                    config.getDuration(OTEL_EXPORTER_OTLP_TIMEOUT, Duration.ofSeconds(10)),
                                    OtlpExporterUtil.populateTracingExportHttpHeaders(),
                                    Vertx.vertx()),
                            MeterProvider::noop));
                case PROTOCOL_HTTP_PROTOBUF:
                    return new VertxHttpSpanExporter(new HttpExporter<TraceRequestMarshaler>(
                            OtlpExporterUtil.OTLP_VALUE, // use the same as OTel does
                            "span", // use the same as OTel does
                            new VertxHttpSender(
                                    new URI(OtlpExporterUtil.getTracingOtlpEndpoint(config,
                                            DEFAULT_OTLP_HTTP_PROTOBUF_ENDPOINT)),
                                    VertxHttpSender.TRACES_PATH,
                                    true,
                                    config.getDuration(OTEL_EXPORTER_OTLP_TIMEOUT, Duration.ofSeconds(10)),
                                    OtlpExporterUtil.populateTracingExportHttpHeaders(),
                                    "application/x-protobuf",
                                    Vertx.vertx()),
                            MeterProvider::noop,
                            false));
                default:
                    throw new RuntimeException("Unsupported OTLP protocol: " + otlpProtocol);
            }
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
