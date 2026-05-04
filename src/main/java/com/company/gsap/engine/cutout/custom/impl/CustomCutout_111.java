package com.company.gsap.engine.cutout.custom.impl;

import com.company.gsap.engine.cutout.custom.CutoutPlacement;
import com.company.gsap.engine.cutout.custom.CustomCutoutGeometryEngine;
import com.company.gsap.engine.cutout.custom.CustomCutoutRequest;

public class CustomCutout_111 extends AbstractCustomProfileCutout {
    public CustomCutout_111(CustomCutoutRequest request, CustomCutoutGeometryEngine geometryEngine) {
        super(request, geometryEngine);
    }

    @Override
    public CutoutPlacement computePlacement() {
        double w = param("W", 24.0);
        double h = param("H", 24.0);
        double r1 = param("R1", Math.min(w, h) / 4.0);
        double r2 = param("R2", Math.min(w, h) / 4.0);
        double profileSize = Math.max(Math.max(w, h), Math.max(2.0 * r1, 2.0 * r2));
CutoutPlacement base = geometryEngine.computeCornerCutout(placementPoint(), profileSize);
        return new CutoutPlacement(base.x(), base.y(), base.rotationAngleDegrees(), base.size(), 111);
    }
}
