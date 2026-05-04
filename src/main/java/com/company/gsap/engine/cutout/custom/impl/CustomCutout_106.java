package com.company.gsap.engine.cutout.custom.impl;

import com.company.gsap.engine.cutout.custom.CutoutPlacement;
import com.company.gsap.engine.cutout.custom.CustomCutoutGeometryEngine;
import com.company.gsap.engine.cutout.custom.CustomCutoutRequest;

public class CustomCutout_106 extends AbstractCustomProfileCutout {
    public CustomCutout_106(CustomCutoutRequest request, CustomCutoutGeometryEngine geometryEngine) {
        super(request, geometryEngine);
    }

    @Override
    public CutoutPlacement computePlacement() {
        double w = param("W", 24.0);
        double h = param("H", 24.0);
        double r = param("R", Math.min(w, h) / 4.0); // chamfer+arc corner profile
        double profileSize = Math.max(Math.max(w, h), 2.0 * r);
CutoutPlacement base = geometryEngine.computeCornerCutout(placementPoint(), profileSize);
        return new CutoutPlacement(base.x(), base.y(), base.rotationAngleDegrees(), base.size(), 106);
    }
}
