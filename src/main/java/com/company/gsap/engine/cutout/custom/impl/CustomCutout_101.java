package com.company.gsap.engine.cutout.custom.impl;

import com.company.gsap.engine.cutout.custom.CutoutPlacement;
import com.company.gsap.engine.cutout.custom.CustomCutoutGeometryEngine;
import com.company.gsap.engine.cutout.custom.CustomCutoutRequest;

public class CustomCutout_101 extends AbstractCustomProfileCutout {
    public CustomCutout_101(CustomCutoutRequest request, CustomCutoutGeometryEngine geometryEngine) {
        super(request, geometryEngine);
    }

    @Override
    public CutoutPlacement computePlacement() {
        double w = param("W", 24.0);
        double h = param("H", 24.0);
        double r = param("R", Math.min(w, h) / 4.0);
        double profileSize = Math.max(Math.max(w, h), 2.0 * r); // rounded-rect top
        return geometryEngine.computeEdgeCutout(placementPoint(), profileSize);
    }
}
