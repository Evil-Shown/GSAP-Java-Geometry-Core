package com.company.gsap.engine.cutout.custom;

import java.util.List;

public final class CustomCutoutFactory {
    private CustomCutoutFactory() {
    }

    public static CustomCutout create(CustomCutoutRequest request, List<double[]> polygonPoints) {
        CustomCutoutGeometryEngine engine = new CustomCutoutGeometryEngine(polygonPoints);
        return switch (request.type()) {
            case EDGE -> new CustomEdgeCutout(request, engine);
            case INTERIOR -> new CustomInteriorCutout(request, engine);
            case CORNER -> new CustomCornerCutout(request, engine);
        };
    }
}
