package com.company.gsap.engine.cutout.custom;

import com.company.gsap.engine.model.EdgeDefinition;
import com.company.gsap.engine.model.Point2D;
import com.company.gsap.engine.model.ArcEdgeDefinition;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomCutoutProcessor {
    private static final Logger log = LoggerFactory.getLogger(CustomCutoutProcessor.class);
    private static final Gson GSON = new Gson();
    private static final Type CUTOUT_LIST_TYPE = new TypeToken<List<CustomCutoutRequest>>() {}.getType();

    public List<CutoutPlacementResult> process(List<EdgeDefinition> edges, Map<String, String> metadata) {
        List<CustomCutoutRequest> requests = parseRequests(metadata);
        if (requests.isEmpty()) {
            return List.of();
        }
        // Polygon points are derived directly from the currently transformed GSAP edge list.
        // This is the real custom-shape geometry at processing time used for edge/normal resolution.
        List<double[]> polygon = toPolygon(edges);
        Map<String, EdgeDefinition> edgeById = new HashMap<>();
        for (EdgeDefinition edge : edges) {
            edgeById.put(edge.id(), edge);
        }
        List<CutoutPlacementResult> out = new ArrayList<>();
        for (CustomCutoutRequest request : requests) {
            double[] placementPoint = resolvePlacementPoint(request, edgeById);
            if (placementPoint == null) {
                continue;
            }
            double size = request.size() != null && request.size() > 0.0 ? request.size() : 24.0;
            CustomCutoutRequest normalized = new CustomCutoutRequest(
                    request.id(),
                    request.type(),
                    placementPoint[0],
                    placementPoint[1],
                    size,
                    request.edgeId(),
                    request.offset(),
                    request.startOffset(),
                    request.offsetX(),
                    request.offsetY());
            CustomCutout cutout = CustomCutoutFactory.create(normalized, polygon);
            CutoutPlacement p = cutout.computePlacement();
            out.add(new CutoutPlacementResult(normalized.id(), normalized.type(), p));
        }
        String shapeEntityId = metadata == null ? "unknown" : metadata.getOrDefault(CustomCutoutMetadataKeys.SHAPE_ENTITY_ID, "unknown");
        log.info("[CustomCutout] Emitting {} placements for shapeId={}", out.size(), shapeEntityId);
        return out;
    }

    public String serializeResults(List<CutoutPlacementResult> placements) {
        return GSON.toJson(placements);
    }

    private static List<CustomCutoutRequest> parseRequests(Map<String, String> metadata) {
        if (metadata == null) {
            return List.of();
        }
        String raw = metadata.get(CustomCutoutMetadataKeys.CUSTOM_CUTOUTS_INPUT);
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        try {
            List<CustomCutoutRequest> parsed = GSON.fromJson(raw, CUTOUT_LIST_TYPE);
            return parsed == null ? List.of() : parsed;
        } catch (Exception ex) {
            return List.of();
        }
    }

    private static List<double[]> toPolygon(List<EdgeDefinition> edges) {
        List<double[]> out = new ArrayList<>();
        for (EdgeDefinition e : edges) {
            Point2D s = e.start();
            out.add(new double[] {s.x(), s.y()});
        }
        if (!edges.isEmpty()) {
            Point2D end = edges.get(edges.size() - 1).end();
            out.add(new double[] {end.x(), end.y()});
        }
        return out;
    }

    private static double[] resolvePlacementPoint(CustomCutoutRequest request, Map<String, EdgeDefinition> edgeById) {
        if (request.x() != null && request.y() != null) {
            return new double[] {request.x(), request.y()};
        }
        if (request.edgeId() == null || request.edgeId().isBlank()) {
            return null;
        }
        EdgeDefinition edge = edgeById.get(request.edgeId());
        if (edge == null) {
            return null;
        }

        if (request.type() == CustomCutoutType.CORNER) {
            return new double[] {edge.end().x(), edge.end().y()};
        }
        if (request.type() == CustomCutoutType.EDGE) {
            double offset = request.offset() == null ? 0.0 : request.offset();
            boolean fromStart = request.startOffset() == null || request.startOffset();
            double fromStartDistance = fromStart ? offset : Math.max(0.0, edge.length() - offset);
            Point2D p = pointAlongEdge(edge, fromStartDistance);
            return new double[] {p.x(), p.y()};
        }
        if (request.type() == CustomCutoutType.INTERIOR) {
            // Same reference convention as shapes-service: edgeId selects the corner-ending edge.
            double offsetX = request.offsetX() == null ? 0.0 : request.offsetX();
            double offsetY = request.offsetY() == null ? 0.0 : request.offsetY();
            Point2D along = pointAlongEdge(edge, Math.max(0.0, edge.length() - offsetX));
            double dx = edge.end().x() - edge.start().x();
            double dy = edge.end().y() - edge.start().y();
            double len = Math.max(1e-9, Math.hypot(dx, dy));
            double tx = dx / len;
            double ty = dy / len;
            // Rotate tangent clockwise to move toward interior in custom mapper conventions.
            double nx = ty;
            double ny = -tx;
            return new double[] {along.x() + nx * offsetY, along.y() + ny * offsetY};
        }
        return null;
    }

    private static Point2D pointAlongEdge(EdgeDefinition edge, double distance) {
        double clamped = Math.max(0.0, Math.min(distance, edge.length()));
        if (edge instanceof ArcEdgeDefinition arc) {
            double t = edge.length() > 1e-9 ? (clamped / edge.length()) : 0.0;
            double sweep = arc.endAngleRadians() - arc.startAngleRadians();
            double angle = arc.startAngleRadians() + sweep * t;
            return new Point2D(
                    arc.center().x() + arc.radius() * Math.cos(angle),
                    arc.center().y() + arc.radius() * Math.sin(angle));
        }
        double t = edge.length() > 1e-9 ? (clamped / edge.length()) : 0.0;
        return new Point2D(
                edge.start().x() + (edge.end().x() - edge.start().x()) * t,
                edge.start().y() + (edge.end().y() - edge.start().y()) * t);
    }

    public record CutoutPlacementResult(
            String id,
            CustomCutoutType type,
            CutoutPlacement placement
    ) {
    }
}
