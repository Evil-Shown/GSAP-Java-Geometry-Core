package com.company.gsap.engine.cutout.custom.impl;

import com.company.gsap.engine.cutout.custom.CutoutPlacement;
import com.company.gsap.engine.cutout.custom.CustomCutoutGeometryEngine;
import com.company.gsap.engine.cutout.custom.CustomCutoutRequest;

public class CustomCutout_104 extends AbstractCustomProfileCutout {
    public CustomCutout_104(CustomCutoutRequest request, CustomCutoutGeometryEngine geometryEngine) {
        super(request, geometryEngine);
    }

    @Override
    public CutoutPlacement computePlacement() {
        double w = param("W", 24.0);
        double r = param("R", w / 2.0); // circular/arched edge slot
        double profileSize = Math.max(w, 2.0 * r);
CutoutPlacement base = geometryEngine.computeEdgeCutout(placementPoint(), profileSize);
        return new CutoutPlacement(base.x(), base.y(), base.rotationAngleDegrees(), base.size(), 104);
    }
}
