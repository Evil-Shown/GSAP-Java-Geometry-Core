package com.company.gsap.engine.cutout.custom;

import com.company.gsap.engine.model.EdgeDefinition;
import com.company.gsap.engine.model.Point2D;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CustomCutoutProcessor {
    private static final Gson GSON = new Gson();
    private static final Type CUTOUT_LIST_TYPE = new TypeToken<List<CustomCutoutRequest>>() {}.getType();

    public List<CutoutPlacementResult> process(List<EdgeDefinition> edges, Map<String, String> metadata) {
        List<CustomCutoutRequest> requests = parseRequests(metadata);
        if (requests.isEmpty()) {
            return List.of();
        }
        List<double[]> polygon = toPolygon(edges);
        List<CutoutPlacementResult> out = new ArrayList<>();
        for (CustomCutoutRequest request : requests) {
            CustomCutout cutout = CustomCutoutFactory.create(request, polygon);
            CutoutPlacement p = cutout.computePlacement();
            out.add(new CutoutPlacementResult(request.id(), request.type(), p));
        }
        return out;
    }

    public String serializeResults(List<CutoutPlacementResult> placements) {
        return GSON.toJson(placements);
    }

    private static List<CustomCutoutRequest> parseRequests(Map<String, String> metadata) {
        if (metadata == null) {
            return List.of();
        }
        String raw = metadata.get("customCutouts");
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

    public record CutoutPlacementResult(
            String id,
            CustomCutoutType type,
            CutoutPlacement placement
    ) {
    }
}
