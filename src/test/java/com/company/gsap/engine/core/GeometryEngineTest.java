package com.company.gsap.engine.core;

import com.company.gsap.engine.model.ShapeInput;
import com.company.gsap.engine.model.ShapeMetrics;
import com.company.gsap.engine.model.ShapeResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeometryEngineTest {

    private final GeometryEngine engine = new GeometryEngine();

    @Test
    void processesHardcodedShapeThroughPublicApi() {
        ShapeInput input = new ShapeInput(
                "RECTANGLE",
                null,
                Map.of(),
                List.of(),
                Map.of("requestId", "abc-123"),
                Map.of()
        );
        ShapeResult result = engine.processShape(input);
        assertEquals(4, result.geometry().edges().size());
        assertTrue(result.svg().contains("<svg"));
        assertEquals("abc-123", result.metadata().get("requestId"));
    }

    @Test
    void calculatesMetricsFromJsonShape() {
        String json = """
                {
                  "shapeId": "RECT_JSON",
                  "edges": [
                    { "id": "e1", "type": "line", "start": { "x": 0, "y": 0 }, "end": { "x": 4, "y": 0 } },
                    { "id": "e2", "type": "line", "start": { "x": 4, "y": 0 }, "end": { "x": 4, "y": 3 } },
                    { "id": "e3", "type": "line", "start": { "x": 4, "y": 3 }, "end": { "x": 0, "y": 3 } },
                    { "id": "e4", "type": "line", "start": { "x": 0, "y": 3 }, "end": { "x": 0, "y": 0 } }
                  ]
                }
                """;
        ShapeInput input = new ShapeInput(null, json, Map.of(), List.of(), Map.of(), Map.of());
        ShapeMetrics metrics = engine.calculateMetrics(input);
        assertEquals(14.0, metrics.perimeter(), 0.001);
        assertEquals(12.0, metrics.area(), 0.001);
    }

    @Test
    void appliesEdgeServiceOutlineExpansionForClosedLineLoop() {
        String json = """
                {
                  "shapeId": "EDGE_SVC_RECT",
                  "edges": [
                    { "id": "e1", "type": "line", "start": { "x": 0, "y": 0 }, "end": { "x": 4, "y": 0 } },
                    { "id": "e2", "type": "line", "start": { "x": 4, "y": 0 }, "end": { "x": 4, "y": 3 } },
                    { "id": "e3", "type": "line", "start": { "x": 4, "y": 3 }, "end": { "x": 0, "y": 3 } },
                    { "id": "e4", "type": "line", "start": { "x": 0, "y": 3 }, "end": { "x": 0, "y": 0 } }
                  ]
                }
                """;
        ShapeInput input = new ShapeInput(
                null,
                json,
                Map.of(),
                List.of(),
                Map.of(),
                Map.of("e1", 1.0, "e2", 1.0, "e3", 1.0, "e4", 1.0));
        ShapeMetrics metrics = engine.calculateMetrics(input);
        assertEquals(22.0, metrics.perimeter(), 0.05);
        assertEquals(30.0, metrics.area(), 0.05);
    }

    @Test
    void expandsOutlineWithMixedLinesAndArcEdgeService() {
        String json = """
                {
                  "shapeId": "MIX_RECT_ARC",
                  "edges": [
                    { "id": "e1", "type": "line", "start": { "x": 0, "y": 0 }, "end": { "x": 20, "y": 0 } },
                    { "id": "e2", "type": "line", "start": { "x": 20, "y": 0 }, "end": { "x": 20, "y": 12 } },
                    { "id": "e3", "type": "line", "start": { "x": 20, "y": 12 }, "end": { "x": 0, "y": 12 } },
                    { "id": "a4", "type": "arc", "center": { "x": 0, "y": 6 }, "radius": 6,
                      "startAngle": 1.5707963267948966, "endAngle": -1.5707963267948966, "clockwise": false }
                  ]
                }
                """;
        ShapeMetrics base = engine.calculateMetrics(
                new ShapeInput(null, json, Map.of(), List.of(), Map.of(), Map.of()));
        ShapeMetrics grown = engine.calculateMetrics(
                new ShapeInput(null, json, Map.of(), List.of(), Map.of(), Map.of("a4", 2.0)));
        assertTrue(grown.perimeter() > base.perimeter() + 1.0, "arc edge service should lengthen perimeter");
        assertTrue(grown.area() > base.area() + 1.0, "bulging arc offset should increase area");
    }
}
