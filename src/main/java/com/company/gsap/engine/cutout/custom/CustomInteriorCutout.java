package com.company.gsap.engine.cutout.custom;

public class CustomInteriorCutout implements CustomCutout {
    private final CustomCutoutRequest request;
    private final CustomCutoutGeometryEngine geometryEngine;

    public CustomInteriorCutout(CustomCutoutRequest request, CustomCutoutGeometryEngine geometryEngine) {
        this.request = request;
        this.geometryEngine = geometryEngine;
    }

    @Override
    public CutoutPlacement computePlacement() {
        return geometryEngine.computeInteriorCutout(new double[] {request.x(), request.y()}, request.size());
    }

    @Override
    public CustomCutoutRequest request() {
        return request;
    }
}
