package com.company.gsap.engine.factory;

import com.company.gsap.engine.core.InvalidShapeDefinitionException;
import com.company.gsap.engine.core.ShapeDefinition;
import com.company.gsap.engine.model.LineEdgeDefinition;
import com.company.gsap.engine.model.Point2D;
import com.company.gsap.engine.parser.JsonShapeDefinitionParser;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultShapeFactory implements ShapeFactory {
    private final JsonShapeDefinitionParser jsonParser;
    private final Map<String, ShapeDefinition> predefined = new ConcurrentHashMap<>();

    public DefaultShapeFactory(JsonShapeDefinitionParser jsonParser) {
        this.jsonParser = jsonParser;
        registerDefaults();
    }

    @Override
    public ShapeDefinition create(String shapeId, String shapeJson) {
        if (shapeJson != null && !shapeJson.isBlank()) {
            return jsonParser.fromJson(shapeJson);
        }
        return findById(shapeId)
                .orElseThrow(() -> new InvalidShapeDefinitionException("No predefined shape registered for: " + shapeId));
    }

    @Override
    public Optional<ShapeDefinition> findById(String shapeId) {
        return Optional.ofNullable(predefined.get(shapeId));
    }

    public void register(String shapeId, ShapeDefinition shapeDefinition) {
        predefined.put(shapeId, shapeDefinition);
    }

    private void registerDefaults() {
        ShapeDefinition rectangle = new HardcodedShapeDefinition(
                "RECTANGLE",
                Map.of("width", 100.0, "height", 50.0),
                List.of(
                        new LineEdgeDefinition("e1", new Point2D(0, 0), new Point2D(100, 0)),
                        new LineEdgeDefinition("e2", new Point2D(100, 0), new Point2D(100, 50)),
                        new LineEdgeDefinition("e3", new Point2D(100, 50), new Point2D(0, 50)),
                        new LineEdgeDefinition("e4", new Point2D(0, 50), new Point2D(0, 0))
                ),
                List.of(),
                Map.of("source", "hardcoded-default")
        );
        register(rectangle.id(), rectangle);
    }
}
