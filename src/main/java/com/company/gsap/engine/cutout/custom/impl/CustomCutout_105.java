package com.company.gsap.engine.cutout.custom.impl;

import com.company.gsap.engine.cutout.custom.CutoutPlacement;
import com.company.gsap.engine.cutout.custom.CustomCutoutGeometryEngine;
import com.company.gsap.engine.cutout.custom.CustomCutoutRequest;

public class CustomCutout_105 extends AbstractCustomProfileCutout {
    public CustomCutout_105(CustomCutoutRequest request, CustomCutoutGeometryEngine geometryEngine) {
        super(request, geometryEngine);
    }

    @Override
    public CutoutPlacement computePlacement() {
        double r = param("R", 18.0); // corner fillet style
        return geometryEngine.computeCornerCutout(placementPoint(), 2.0 * r);
    }
}
