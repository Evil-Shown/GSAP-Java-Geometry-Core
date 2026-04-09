package com.company.gsap.engine.model;

import java.util.List;
import java.util.Map;

public record ResolvedShape(
        String shapeId,
        Map<String, Double> parameters,
        List<EdgeDefinition> edges,
        Map<String, String> metadata
) {
}
