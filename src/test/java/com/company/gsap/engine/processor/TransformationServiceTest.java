package com.company.gsap.engine.processor;

import com.company.gsap.engine.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TransformationServiceTest {

    private final TransformationService service = new TransformationService();

    @Test
    void appliesResizeAndRotate() {
        List<EdgeDefinition> edges = List.of(
                new LineEdgeDefinition("e1", new Point2D(0, 0), new Point2D(10, 0)),
                new LineEdgeDefinition("e2", new Point2D(10, 0), new Point2D(10, 10)),
                new LineEdgeDefinition("e3", new Point2D(10, 10), new Point2D(0, 10)),
                new LineEdgeDefinition("e4", new Point2D(0, 10), new Point2D(0, 0))
        );
        List<Transformation> transformations = List.of(
                new Transformation(TransformationType.RESIZE, 2.0, null, false, false),
                new Transformation(TransformationType.ROTATE, null, 90.0, false, false)
        );
        List<EdgeDefinition> result = service.apply(edges, transformations);
        LineEdgeDefinition first = (LineEdgeDefinition) result.get(0);
        assertEquals(20.0, first.length(), 0.001);
        assertEquals(0.0, first.end().x(), 0.001);
        assertEquals(20.0, first.end().y(), 0.001);
    }
}
