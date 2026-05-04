package com.company.gsap.engine.cutout.custom.impl;

import com.company.gsap.engine.cutout.custom.CutoutPlacement;
import com.company.gsap.engine.cutout.custom.CustomCutoutGeometryEngine;
import com.company.gsap.engine.cutout.custom.CustomCutoutRequest;

public class CustomCutout_110 extends AbstractCustomProfileCutout {
    public CustomCutout_110(CustomCutoutRequest request, CustomCutoutGeometryEngine geometryEngine) {
        super(request, geometryEngine);
    }

    @Override
    public CutoutPlacement computePlacement() {
        double h = param("H", 24.0);
        double r = param("R", h / 2.0);
        double a = param("A", 0.0); // slotted interior profile angle
        double profileSize = Math.max(h, 2.0 * r) + Math.abs(a) * 0.0;
CutoutPlacement base = geometryEngine.computeInteriorCutout(placementPoint(), profileSize);
        return new CutoutPlacement(base.x(), base.y(), base.rotationAngleDegrees(), base.size(), 110);
    }
}
