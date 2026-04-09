package com.company.gsap.engine.parser;

import com.company.gsap.engine.core.InvalidShapeDefinitionException;
import com.company.gsap.engine.core.ShapeDefinition;
import com.company.gsap.engine.validator.ShapeDefinitionValidator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JsonShapeDefinitionParserTest {

    private final JsonShapeDefinitionParser parser = new JsonShapeDefinitionParser(new ShapeDefinitionValidator());

    @Test
    void parsesValidShapeJson() {
        String json = """
                {
                  "shapeId": "RECT_DYNAMIC",
                  "parameters": { "width": 100, "height": 50 },
                  "edges": [
                    { "id": "e1", "type": "line", "start": { "x": 0, "y": 0 }, "end": { "x": 100, "y": 0 } },
                    { "id": "e2", "type": "line", "start": { "x": 100, "y": 0 }, "end": { "x": 100, "y": 50 } },
                    { "id": "e3", "type": "line", "start": { "x": 100, "y": 50 }, "end": { "x": 0, "y": 50 } },
                    { "id": "e4", "type": "line", "start": { "x": 0, "y": 50 }, "end": { "x": 0, "y": 0 } }
                  ],
                  "transformations": [
                    { "type": "resize", "factor": 1.2 }
                  ],
                  "metadata": { "tenant": "acme" }
                }
                """;
        ShapeDefinition result = parser.fromJson(json);
        assertEquals("RECT_DYNAMIC", result.id());
        assertEquals(4, result.edges().size());
        assertEquals(1, result.transformations().size());
    }

    @Test
    void rejectsMissingEdges() {
        String json = """
                {
                  "shapeId": "INVALID"
                }
                """;
        assertThrows(InvalidShapeDefinitionException.class, () -> parser.fromJson(json));
    }
}
