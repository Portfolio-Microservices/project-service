package com.portfolio.project.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.micrometer.tracing.Tracer;

@RestController
public class TraceTestController {

    private final Tracer tracer;

    public TraceTestController(Tracer tracer) {
        this.tracer = tracer;
    }

    @GetMapping("/trace-test")
    public String traceTest() {
        if (tracer.currentSpan() == null) {
            return "NO TRACE";
        }

        return "TRACE ID = " + tracer.currentSpan().context().traceId();
    }
}