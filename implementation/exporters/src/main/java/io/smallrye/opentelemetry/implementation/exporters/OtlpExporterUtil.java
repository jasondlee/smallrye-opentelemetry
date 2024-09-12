package io.smallrye.opentelemetry.implementation.exporters;

import java.net.URI;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import io.opentelemetry.exporter.otlp.internal.OtlpUserAgent;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;

public final class OtlpExporterUtil {
    public static final String OTLP_VALUE = "otlp";

    public static final String DEFAULT_OTLP_GRPC_ENDPOINT = "http://localhost:4317";
    public static final String DEFAULT_OTLP_HTTP_PROTOBUF_ENDPOINT = "http://localhost:4318";

    public static final String OTEL_DISABLE_SDK = "otel.disable.sdk";

    public static final String OTEL_EXPORTER_OTLP_ENDPOINT = "otel.exporter.otlp.endpoint";
    public static final String OTEL_EXPORTER_OTLP_METRICS_ENDPOINT = "otel.exporter.otlp.metrics.endpoint";
    public static final String OTEL_EXPORTER_OTLP_TRACES_ENDPOINT = "otel.exporter.otlp.traces.endpoint";

    public static final String OTEL_EXPORTER_OTLP_PROTOCOL = "otel.exporter.otlp.protocol";
    public static final String OTEL_EXPORTER_OTLP_METRICS_PROTOCOL = "otel.exporter.otlp.metrics.protocol";
    public static final String OTEL_EXPORTER_OTLP_TRACES_PROTOCOL = "otel.exporter.otlp.traces.protocol";

    public static final String OTEL_EXPORTER_OTLP_TIMEOUT = "otel.exporter.otlp.timeout";

    public static final String PROTOCOL_GRPC = "grpc";
    public static final String PROTOCOL_HTTP_PROTOBUF = "http/protobuf";

    private OtlpExporterUtil() {
    }

    public static int getPort(URI uri) {
        int originalPort = uri.getPort();
        if (originalPort > -1) {
            return originalPort;
        }

        if ("https".equals(uri.getScheme().toLowerCase(Locale.ROOT))) {
            return 443;
        }
        return 80;
    }

    public static Map<String, String> populateTracingExportHttpHeaders() {
        Map<String, String> headersMap = new HashMap<>();
        OtlpUserAgent.addUserAgentHeader(headersMap::put);
        return headersMap;
    }

    /**
     * Gets the OTLP metrics endpoint, if defined. If it is not, it returns the OTLP endpoint. If that is not defined,
     * it returns defaultEndpoint.
     *
     * @param config
     * @param defaultEndpoint The default endpoint for the desired protocol
     * @return
     */
    public static String getMetricsOtlpEndpoint(ConfigProperties config, String defaultEndpoint) {
        return config.getString(OtlpExporterUtil.OTEL_EXPORTER_OTLP_METRICS_ENDPOINT,
                config.getString(OtlpExporterUtil.OTEL_EXPORTER_OTLP_ENDPOINT, defaultEndpoint));
    }

    /**
     * Gets the OTLP traces endpoint, if defined. If it is not, it returns the OTLP endpoint. If that is not defined,
     * it returns defaultEndpoint.
     *
     * @param config
     * @param defaultEndpoint The default endpoint for the desired protocol
     * @return
     */
    public static String getTracingOtlpEndpoint(ConfigProperties config, String defaultEndpoint) {
        return config.getString(OtlpExporterUtil.OTEL_EXPORTER_OTLP_TRACES_ENDPOINT,
                config.getString(OtlpExporterUtil.OTEL_EXPORTER_OTLP_ENDPOINT, defaultEndpoint));
    }
}
