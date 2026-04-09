package com.company.gsap.engine.model;

import java.util.List;
import java.util.Map;

public record ShapeInput(
        String shapeId,
        String shapeJson,
        Map<String, Double> parameters,
        List<Transformation> transformations,
        Map<String, String> metadata
) {
}
