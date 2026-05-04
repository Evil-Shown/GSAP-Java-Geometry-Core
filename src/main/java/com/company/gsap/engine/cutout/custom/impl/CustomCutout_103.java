package com.company.gsap.engine.cutout.custom.impl;

import com.company.gsap.engine.cutout.custom.CutoutPlacement;
import com.company.gsap.engine.cutout.custom.CustomCutoutGeometryEngine;
import com.company.gsap.engine.cutout.custom.CustomCutoutRequest;

public class CustomCutout_103 extends AbstractCustomProfileCutout {
    public CustomCutout_103(CustomCutoutRequest request, CustomCutoutGeometryEngine geometryEngine) {
        super(request, geometryEngine);
    }

    @Override
    public CutoutPlacement computePlacement() {
        double w = param("W", 24.0);
        double h = param("H", 24.0);
        double r = w / 2.0; // U-slot with semicircle cap
        double profileSize = Math.max(Math.max(w, h), 2.0 * r);
        return geometryEngine.computeEdgeCutout(placementPoint(), profileSize);
    }
}
