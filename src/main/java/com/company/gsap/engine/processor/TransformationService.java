package com.company.gsap.engine.processor;

import com.company.gsap.engine.core.InvalidShapeDefinitionException;
import com.company.gsap.engine.model.*;

import java.util.ArrayList;
import java.util.List;

public class TransformationService {

    public List<EdgeDefinition> apply(List<EdgeDefinition> edges, List<Transformation> transformations) {
        List<EdgeDefinition> result = new ArrayList<>(edges);
        for (Transformation transformation : transformations) {
            result = applySingle(result, transformation);
        }
        return result;
    }

    private List<EdgeDefinition> applySingle(List<EdgeDefinition> edges, Transformation transformation) {
        return switch (transformation.type()) {
            case RESIZE -> resize(edges, requiredFactor(transformation));
            case ROTATE -> rotate(edges, requiredAngle(transformation));
            case FLIP -> flip(edges, transformation.horizontal(), transformation.vertical());
        };
    }

    private List<EdgeDefinition> resize(List<EdgeDefinition> edges, double factor) {
        if (factor <= 0) {
            throw new InvalidShapeDefinitionException("Resize factor must be > 0");
        }
        List<EdgeDefinition> resized = new ArrayList<>();
        for (EdgeDefinition edge : edges) {
            if (edge instanceof LineEdgeDefinition line) {
                resized.add(new LineEdgeDefinition(line.id(), line.start().scale(factor), line.end().scale(factor)));
            } else if (edge instanceof ArcEdgeDefinition arc) {
                resized.add(new ArcEdgeDefinition(arc.id(), arc.center().scale(factor), arc.radius() * factor,
                        arc.startAngleRadians(), arc.endAngleRadians(), arc.clockwise()));
            }
        }
        return resized;
    }

    private List<EdgeDefinition> rotate(List<EdgeDefinition> edges, double degrees) {
        double radians = Math.toRadians(degrees);
        List<EdgeDefinition> rotated = new ArrayList<>();
        for (EdgeDefinition edge : edges) {
            if (edge instanceof LineEdgeDefinition line) {
                rotated.add(new LineEdgeDefinition(line.id(),
                        line.start().rotateRadians(radians),
                        line.end().rotateRadians(radians)));
            } else if (edge instanceof ArcEdgeDefinition arc) {
                rotated.add(new ArcEdgeDefinition(
                        arc.id(),
                        arc.center().rotateRadians(radians),
                        arc.radius(),
                        arc.startAngleRadians() + radians,
                        arc.endAngleRadians() + radians,
                        arc.clockwise()));
            }
        }
        return rotated;
    }

    private List<EdgeDefinition> flip(List<EdgeDefinition> edges, boolean horizontal, boolean vertical) {
        List<EdgeDefinition> flipped = new ArrayList<>();
        for (EdgeDefinition edge : edges) {
            if (edge instanceof LineEdgeDefinition line) {
                flipped.add(new LineEdgeDefinition(line.id(),
                        line.start().flip(horizontal, vertical),
                        line.end().flip(horizontal, vertical)));
            } else if (edge instanceof ArcEdgeDefinition arc) {
                flipped.add(new ArcEdgeDefinition(
                        arc.id(),
                        arc.center().flip(horizontal, vertical),
                        arc.radius(),
                        arc.startAngleRadians(),
                        arc.endAngleRadians(),
                        arc.clockwise()));
            }
        }
        return flipped;
    }

    private double requiredFactor(Transformation transformation) {
        if (transformation.factor() == null) {
            throw new InvalidShapeDefinitionException("Resize transformation requires factor");
        }
        return transformation.factor();
    }

    private double requiredAngle(Transformation transformation) {
        if (transformation.angleDegrees() == null) {
            throw new InvalidShapeDefinitionException("Rotate transformation requires angleDegrees");
        }
        return transformation.angleDegrees();
    }
}
