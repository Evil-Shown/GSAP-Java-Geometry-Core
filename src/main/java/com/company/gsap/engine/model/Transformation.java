package com.company.gsap.engine.model;

public record Transformation(
        TransformationType type,
        Double factor,
        Double angleDegrees,
        boolean horizontal,
        boolean vertical
) {
}
