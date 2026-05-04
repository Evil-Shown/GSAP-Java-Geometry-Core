package com.company.gsap.engine.cutout.custom.impl;

import com.company.gsap.engine.cutout.custom.CutoutPlacement;
import com.company.gsap.engine.cutout.custom.CustomCutoutGeometryEngine;
import com.company.gsap.engine.cutout.custom.CustomCutoutRequest;

public class CustomCutout_107 extends AbstractCustomProfileCutout {
    public CustomCutout_107(CustomCutoutRequest request, CustomCutoutGeometryEngine geometryEngine) {
        super(request, geometryEngine);
    }

    @Override
    public CutoutPlacement computePlacement() {
        double w = param("W", 24.0);
        double h = param("H", 24.0);
        double r = param("R", Math.min(w, h) / 6.0);
        double a = param("A", 0.0); // angle affects profile orientation in legacy
        double profileSize = Math.max(Math.max(w, h), 2.0 * r) + Math.abs(a) * 0.0;
CutoutPlacement base = geometryEngine.computeInteriorCutout(placementPoint(), profileSize);
        return new CutoutPlacement(base.x(), base.y(), base.rotationAngleDegrees(), base.size(), 107);
    }
}
