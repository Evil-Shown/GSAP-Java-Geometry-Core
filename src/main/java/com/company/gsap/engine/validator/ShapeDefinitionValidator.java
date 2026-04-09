package com.company.gsap.engine.validator;

import com.company.gsap.engine.core.InvalidShapeDefinitionException;
import com.company.gsap.engine.model.ArcEdgeDefinition;
import com.company.gsap.engine.model.EdgeDefinition;
import com.company.gsap.engine.model.LineEdgeDefinition;

import java.util.List;

public class ShapeDefinitionValidator {

    public void validate(String shapeId, List<EdgeDefinition> edges) {
        if (shapeId == null || shapeId.isBlank()) {
            throw new InvalidShapeDefinitionException("shapeId is required");
        }
        if (edges == null || edges.size() < 3) {
            throw new InvalidShapeDefinitionException("A shape must contain at least 3 edges");
        }
        for (EdgeDefinition edge : edges) {
            if (edge.id() == null || edge.id().isBlank()) {
                throw new InvalidShapeDefinitionException("Edge id is required");
            }
            if (edge instanceof LineEdgeDefinition line && line.length() <= 0.0) {
                throw new InvalidShapeDefinitionException("Line edge [" + edge.id() + "] has zero length");
            }
            if (edge instanceof ArcEdgeDefinition arc) {
                if (arc.radius() <= 0.0) {
                    throw new InvalidShapeDefinitionException("Arc edge [" + edge.id() + "] radius must be > 0");
                }
                if (arc.sweep() <= 0.0) {
                    throw new InvalidShapeDefinitionException("Arc edge [" + edge.id() + "] sweep must be > 0");
                }
            }
        }
    }
}
