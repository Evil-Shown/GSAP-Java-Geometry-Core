package com.company.gsap.engine.parser;

import com.company.gsap.engine.core.InvalidShapeDefinitionException;
import com.company.gsap.engine.core.ShapeDefinition;
import com.company.gsap.engine.model.*;
import com.company.gsap.engine.validator.ShapeDefinitionValidator;
import com.google.gson.*;

import java.util.*;

public class JsonShapeDefinitionParser {
    private static final Gson GSON = new Gson();
    private final ShapeDefinitionValidator validator;

    public JsonShapeDefinitionParser(ShapeDefinitionValidator validator) {
        this.validator = validator;
    }

    public ShapeDefinition fromJson(String json) {
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            String shapeId = requiredString(root, "shapeId");

            Map<String, Double> parameters = parseParameters(root.getAsJsonObject("parameters"));
            Map<String, String> metadata = parseMetadata(root.getAsJsonObject("metadata"));
            List<Transformation> transformations = parseTransformations(root.getAsJsonArray("transformations"));
            List<EdgeDefinition> edges = parseEdges(root.getAsJsonArray("edges"));

            validator.validate(shapeId, edges);
            return new JsonShapeDefinition(shapeId, parameters, edges, transformations, metadata);
        } catch (InvalidShapeDefinitionException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InvalidShapeDefinitionException("Invalid JSON shape definition", ex);
        }
    }

    private List<EdgeDefinition> parseEdges(JsonArray arr) {
        if (arr == null || arr.isEmpty()) {
            throw new InvalidShapeDefinitionException("edges are required");
        }
        List<EdgeDefinition> edges = new ArrayList<>();
        for (JsonElement element : arr) {
            JsonObject edge = element.getAsJsonObject();
            String id = requiredString(edge, "id");
            String type = requiredString(edge, "type").toLowerCase(Locale.ROOT);
            if ("line".equals(type)) {
                Point2D start = parsePoint(edge.getAsJsonObject("start"));
                Point2D end = parsePoint(edge.getAsJsonObject("end"));
                edges.add(new LineEdgeDefinition(id, start, end));
            } else if ("arc".equals(type)) {
                Point2D center = parsePoint(edge.getAsJsonObject("center"));
                double radius = edge.get("radius").getAsDouble();
                double startAngle = edge.get("startAngle").getAsDouble();
                double endAngle = edge.get("endAngle").getAsDouble();
                boolean clockwise = edge.get("clockwise").getAsBoolean();
                edges.add(new ArcEdgeDefinition(id, center, radius, startAngle, endAngle, clockwise));
            } else {
                throw new InvalidShapeDefinitionException("Unsupported edge type: " + type);
            }
        }
        return edges;
    }

    private List<Transformation> parseTransformations(JsonArray arr) {
        if (arr == null) {
            return List.of();
        }
        List<Transformation> transformations = new ArrayList<>();
        for (JsonElement element : arr) {
            JsonObject t = element.getAsJsonObject();
            TransformationType type = TransformationType.valueOf(requiredString(t, "type").toUpperCase(Locale.ROOT));
            Double factor = t.has("factor") ? t.get("factor").getAsDouble() : null;
            Double angle = t.has("angleDegrees") ? t.get("angleDegrees").getAsDouble() : null;
            boolean horizontal = t.has("horizontal") && t.get("horizontal").getAsBoolean();
            boolean vertical = t.has("vertical") && t.get("vertical").getAsBoolean();
            transformations.add(new Transformation(type, factor, angle, horizontal, vertical));
        }
        return transformations;
    }

    private Map<String, Double> parseParameters(JsonObject obj) {
        if (obj == null) {
            return Map.of();
        }
        Map<String, Double> result = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getAsDouble());
        }
        return result;
    }

    private Map<String, String> parseMetadata(JsonObject obj) {
        if (obj == null) {
            return Map.of();
        }
        return GSON.fromJson(obj, Map.class);
    }

    private Point2D parsePoint(JsonObject point) {
        if (point == null || !point.has("x") || !point.has("y")) {
            throw new InvalidShapeDefinitionException("point must contain x and y");
        }
        return new Point2D(point.get("x").getAsDouble(), point.get("y").getAsDouble());
    }

    private String requiredString(JsonObject object, String key) {
        if (!object.has(key) || object.get(key).isJsonNull() || object.get(key).getAsString().isBlank()) {
            throw new InvalidShapeDefinitionException(key + " is required");
        }
        return object.get(key).getAsString();
    }
}
