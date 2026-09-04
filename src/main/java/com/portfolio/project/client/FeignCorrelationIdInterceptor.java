package com.portfolio.project.client;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
public class FeignCorrelationIdInterceptor implements RequestInterceptor {

    private static final String CORRELATION_ID = "X-Correlation-ID";

    @Override
    public void apply(RequestTemplate template) {

        String correlationId = MDC.get(CORRELATION_ID);

        if (correlationId != null && !correlationId.isBlank()) {
            template.header(CORRELATION_ID, correlationId);
        }
    }
}