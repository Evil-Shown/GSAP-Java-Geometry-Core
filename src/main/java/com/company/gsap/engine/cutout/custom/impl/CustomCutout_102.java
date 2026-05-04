package com.company.gsap.engine.cutout.custom.impl;

import com.company.gsap.engine.cutout.custom.CutoutPlacement;
import com.company.gsap.engine.cutout.custom.CustomCutoutGeometryEngine;
import com.company.gsap.engine.cutout.custom.CustomCutoutRequest;

public class CustomCutout_102 extends AbstractCustomProfileCutout {
    public CustomCutout_102(CustomCutoutRequest request, CustomCutoutGeometryEngine geometryEngine) {
        super(request, geometryEngine);
    }

    @Override
    public CutoutPlacement computePlacement() {
        double r = param("R", 18.0); // quarter-round corner relief
CutoutPlacement base = geometryEngine.computeCornerCutout(placementPoint(), 2.0 * r);
        return new CutoutPlacement(base.x(), base.y(), base.rotationAngleDegrees(), base.size(), 102);
    }
}
