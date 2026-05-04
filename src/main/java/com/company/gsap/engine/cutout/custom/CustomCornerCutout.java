package com.company.gsap.engine.cutout.custom;

public class CustomCornerCutout implements CustomCutout {
    private final CustomCutoutRequest request;
    private final CustomCutoutGeometryEngine geometryEngine;

    public CustomCornerCutout(CustomCutoutRequest request, CustomCutoutGeometryEngine geometryEngine) {
        this.request = request;
        this.geometryEngine = geometryEngine;
    }

    @Override
    public CutoutPlacement computePlacement() {
        return geometryEngine.computeCornerCutout(new double[] {request.x(), request.y()}, request.size());
    }

    @Override
    public CustomCutoutRequest request() {
        return request;
    }
}
