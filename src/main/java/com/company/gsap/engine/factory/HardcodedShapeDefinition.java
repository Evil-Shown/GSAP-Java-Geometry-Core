package com.company.gsap.engine.factory;

import com.company.gsap.engine.core.ShapeDefinition;
import com.company.gsap.engine.model.EdgeDefinition;
import com.company.gsap.engine.model.Transformation;

import java.util.List;
import java.util.Map;

public record HardcodedShapeDefinition(
        String id,
        Map<String, Double> parameters,
        List<EdgeDefinition> edges,
        List<Transformation> transformations,
        Map<String, String> metadata
) implements ShapeDefinition {
}
