package io.smallrye.opentelemetry.implementation.exporters.sender;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

import io.grpc.Channel;
import io.opentelemetry.exporter.internal.compression.Compressor;
import io.opentelemetry.exporter.internal.grpc.GrpcSender;
import io.opentelemetry.exporter.internal.grpc.GrpcSenderProvider;
import io.opentelemetry.exporter.internal.grpc.MarshalerServiceStub;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.otlp.internal.OtlpUserAgent;
import io.opentelemetry.sdk.common.export.RetryPolicy;
import io.vertx.core.Vertx;

public class VertxGrpcSenderProvider implements GrpcSenderProvider {
    @Override
    public <T extends Marshaler> GrpcSender<T> createSender(
            URI endpoint,
            String endpointPath,
            @Nullable Compressor compressor,
            long timeoutNanos,
            long connectTimeoutNanos,
            Supplier<Map<String, List<String>>> headersSupplier,
            @Nullable Object managedChannel,
            Supplier<BiFunction<Channel, String, MarshalerServiceStub<T, ?, ?>>> stubFactory,
            @Nullable RetryPolicy retryPolicy,
            @Nullable SSLContext sslContext,
            @Nullable X509TrustManager trustManager) {
        Map<String, String> headers = headersSupplier.get().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().stream().findFirst().orElse("")));
        OtlpUserAgent.addUserAgentHeader(headers::put);
        return new VertxGrpcSender<>(
                endpoint,
                endpointPath,
                true,
                Duration.ofNanos(timeoutNanos),
                headers,
                Vertx.vertx());
    }
}
