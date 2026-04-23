package com.company.gsap.engine.model;

import java.util.Map;

public record ShapeResult(
        Geometry geometry,
        String svg,
        Map<String, String> metadata,
        ShapeMetrics dimensions
) {
}
