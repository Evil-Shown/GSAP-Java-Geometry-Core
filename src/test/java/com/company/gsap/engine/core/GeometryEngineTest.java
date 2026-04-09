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
                Map.of("requestId", "abc-123")
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
        ShapeInput input = new ShapeInput(null, json, Map.of(), List.of(), Map.of());
        ShapeMetrics metrics = engine.calculateMetrics(input);
        assertEquals(14.0, metrics.perimeter(), 0.001);
        assertEquals(12.0, metrics.area(), 0.001);
    }
}
