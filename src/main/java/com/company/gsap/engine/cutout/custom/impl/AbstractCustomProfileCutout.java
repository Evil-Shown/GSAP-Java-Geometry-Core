package com.company.gsap.engine.cutout.custom.impl;

import com.company.gsap.engine.cutout.custom.CustomCutout;
import com.company.gsap.engine.cutout.custom.CustomCutoutGeometryEngine;
import com.company.gsap.engine.cutout.custom.CustomCutoutRequest;

import java.util.Map;

abstract class AbstractCustomProfileCutout implements CustomCutout {
    protected final CustomCutoutRequest request;
    protected final CustomCutoutGeometryEngine geometryEngine;

    protected AbstractCustomProfileCutout(CustomCutoutRequest request, CustomCutoutGeometryEngine geometryEngine) {
        this.request = request;
        this.geometryEngine = geometryEngine;
    }

    protected double param(String name, double defaultValue) {
        Map<String, Double> params = request.params();
        if (params == null || params.isEmpty()) {
            return defaultValue;
        }
        Double v = params.get(name);
        return v != null ? v : defaultValue;
    }

    protected double[] placementPoint() {
        return new double[] {
                request.x() == null ? 0.0 : request.x(),
                request.y() == null ? 0.0 : request.y()
        };
    }

    @Override
    public CustomCutoutRequest request() {
        return request;
    }
}
