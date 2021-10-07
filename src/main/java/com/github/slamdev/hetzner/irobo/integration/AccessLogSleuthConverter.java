package com.github.slamdev.hetzner.irobo.integration;

import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.core.pattern.CompositeConverter;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.TraceContext;

import java.util.Map;
import java.util.function.Function;

public class AccessLogSleuthConverter extends CompositeConverter<IAccessEvent> {

    private final Map<String, Function<TraceContext, String>> extractors = Map.of(
            "traceId", TraceContext::traceId,
            "spanId", TraceContext::spanId
    );

    @Override
    protected String transform(IAccessEvent event, String in) {
        Span span = (Span) event.getRequest().getAttribute("org.springframework.cloud.sleuth.SpanCustomizer");
        TraceContext context = span.context();
        if (extractors.containsKey(getFirstOption())) {
            return extractors.get(getFirstOption()).apply(context);
        }
        return "";
    }
}
