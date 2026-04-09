package com.company.gsap.engine.factory;

import com.company.gsap.engine.core.ShapeDefinition;

import java.util.Optional;

public interface ShapeFactory {
    ShapeDefinition create(String shapeId, String shapeJson);
    Optional<ShapeDefinition> findById(String shapeId);
}
