package com.company.gsap.engine.cutout.custom.impl;

import com.company.gsap.engine.cutout.custom.CutoutPlacement;
import com.company.gsap.engine.cutout.custom.CustomCutoutGeometryEngine;
import com.company.gsap.engine.cutout.custom.CustomCutoutRequest;

public class CustomCutout_108 extends AbstractCustomProfileCutout {
    public CustomCutout_108(CustomCutoutRequest request, CustomCutoutGeometryEngine geometryEngine) {
        super(request, geometryEngine);
    }

    @Override
    public CutoutPlacement computePlacement() {
        double w = param("W", 24.0);
        double r = param("R", w / 2.0);
        double a = param("A", 0.0); // param retained for profile compatibility
        double profileSize = Math.max(w, 2.0 * r) + Math.abs(a) * 0.0;
CutoutPlacement base = geometryEngine.computeInteriorCutout(placementPoint(), profileSize);
        return new CutoutPlacement(base.x(), base.y(), base.rotationAngleDegrees(), base.size(), 108);
    }
}
