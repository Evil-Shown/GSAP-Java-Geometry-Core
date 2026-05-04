package com.company.gsap.engine.cutout.custom.impl;

import com.company.gsap.engine.cutout.custom.CutoutPlacement;
import com.company.gsap.engine.cutout.custom.CustomCutoutGeometryEngine;
import com.company.gsap.engine.cutout.custom.CustomCutoutRequest;

public class CustomCutout_109 extends AbstractCustomProfileCutout {
    public CustomCutout_109(CustomCutoutRequest request, CustomCutoutGeometryEngine geometryEngine) {
        super(request, geometryEngine);
    }

    @Override
    public CutoutPlacement computePlacement() {
        double r = param("R", 18.0); // circular interior hole/notch
CutoutPlacement base = geometryEngine.computeInteriorCutout(placementPoint(), 2.0 * r);
        return new CutoutPlacement(base.x(), base.y(), base.rotationAngleDegrees(), base.size(), 109);
    }
}
