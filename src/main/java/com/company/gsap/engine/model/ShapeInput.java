package com.company.gsap.engine.model;

import java.util.List;
import java.util.Map;

public record ShapeInput(
        String shapeId,
        String shapeJson,
        Map<String, Double> parameters,
        List<Transformation> transformations,
        Map<String, String> metadata,
        /**
         * Per-edge outline expansion in drawing units (e.g. mm), keyed by edge {@code id} from the shape JSON.
         * Positive values grow the outline outward around the base shape (same intent as library shapes with edge services).
         */
        Map<String, Double> edgeServiceAmountsByEdgeId
) {
    public ShapeInput {
        if (edgeServiceAmountsByEdgeId == null) {
            edgeServiceAmountsByEdgeId = Map.of();
        }
    }
}
