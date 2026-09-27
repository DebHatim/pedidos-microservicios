package dev.hatimdebboun.orderservice.shared.config;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.otel.bridge.OtelCurrentTraceContext;
import io.micrometer.tracing.otel.bridge.OtelTracer;
import io.micrometer.tracing.otel.bridge.OtelTracer.EventPublisher;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.micrometer.observation.autoconfigure.ObservationRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TracingConfig {

    @Value("${spring.application.name:unknown-service}")
    private String serviceName;

    @Bean
    public SpanExporter otlpHttpSpanExporter(
            @Value("${management.otlp.tracing.endpoint:http://localhost:4318/v1/traces}") String endpoint) {
        return OtlpHttpSpanExporter.builder()
                .setEndpoint(endpoint)
                .build();
    }

    @Bean
    public SdkTracerProvider sdkTracerProvider(SpanExporter otlpHttpSpanExporter) {
        return SdkTracerProvider.builder()
                .addSpanProcessor(BatchSpanProcessor.builder(otlpHttpSpanExporter).build())
                .setResource(Resource.getDefault().toBuilder()
                        .put("service.name", serviceName)
                        .build())
                .build();
    }

    @Bean
    public OpenTelemetry openTelemetry(SdkTracerProvider sdkTracerProvider) {
        return OpenTelemetrySdk.builder()
                .setTracerProvider(sdkTracerProvider)
                .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
                .build();
    }

    @Bean
    public io.opentelemetry.api.trace.Tracer otelTracer(OpenTelemetry openTelemetry) {
        return openTelemetry.getTracer(serviceName);
    }

    @Bean
    public OtelCurrentTraceContext otelCurrentTraceContext() {
        return new OtelCurrentTraceContext();
    }

    @Bean
    public EventPublisher otelTracerEventPublisher() {
        return event -> { };
    }

    @Bean
    public Tracer micrometerTracer(io.opentelemetry.api.trace.Tracer otelTracer,
                                   OtelCurrentTraceContext otelCurrentTraceContext,
                                   EventPublisher eventPublisher) {
        return new OtelTracer(otelTracer, otelCurrentTraceContext, eventPublisher);
    }
    @Bean
    public io.micrometer.tracing.handler.DefaultTracingObservationHandler defaultTracingObservationHandler(Tracer tracer) {
        return new io.micrometer.tracing.handler.DefaultTracingObservationHandler(tracer);
    }

    @Bean
    public ObservationRegistryCustomizer<ObservationRegistry> tracingObservationRegistryCustomizer(
            io.micrometer.tracing.handler.DefaultTracingObservationHandler handler) {
        return registry -> registry.observationConfig().observationHandler(handler);
    }
}