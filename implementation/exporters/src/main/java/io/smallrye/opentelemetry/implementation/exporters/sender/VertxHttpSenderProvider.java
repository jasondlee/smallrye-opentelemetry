package io.smallrye.opentelemetry.implementation.exporters.sender;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.Nullable;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

import io.opentelemetry.exporter.internal.auth.Authenticator;
import io.opentelemetry.exporter.internal.compression.Compressor;
import io.opentelemetry.exporter.internal.http.HttpSender;
import io.opentelemetry.exporter.internal.http.HttpSenderProvider;
import io.opentelemetry.sdk.common.export.ProxyOptions;
import io.opentelemetry.sdk.common.export.RetryPolicy;
import io.smallrye.opentelemetry.implementation.exporters.OtlpExporterUtil;
import io.vertx.core.Vertx;

public class VertxHttpSenderProvider implements HttpSenderProvider {
    @Override
    public HttpSender createSender(
            String endpoint,
            @Nullable Compressor compressor,
            boolean exportAsJson,
            String contentType,
            long timeoutNanos,
            long connectTimeout,
            Supplier<Map<String, List<String>>> headerSupplier,
            @Nullable ProxyOptions proxyOptions,
            @Nullable Authenticator authenticator,
            @Nullable RetryPolicy retryPolicy,
            @Nullable SSLContext sslContext, @Nullable X509TrustManager trustManager) {
        try {
            return new VertxHttpSender(
                    new URI(endpoint),
                    VertxHttpSender.METRICS_PATH,
                    true,
                    Duration.ofNanos(timeoutNanos),
                    OtlpExporterUtil.populateTracingExportHttpHeaders(),
                    "application/x-protobuf",
                    Vertx.vertx());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
