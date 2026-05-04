package com.company.gsap.engine.cutout.custom.impl;

import com.company.gsap.engine.cutout.custom.CutoutPlacement;
import com.company.gsap.engine.cutout.custom.CustomCutoutGeometryEngine;
import com.company.gsap.engine.cutout.custom.CustomCutoutRequest;

public class CustomCutout_100 extends AbstractCustomProfileCutout {
    public CustomCutout_100(CustomCutoutRequest request, CustomCutoutGeometryEngine geometryEngine) {
        super(request, geometryEngine);
    }

    @Override
    public CutoutPlacement computePlacement() {
        double w = param("W", 24.0);
        double h = param("H", 24.0);
        double profileSize = Math.max(w, h); // rectangular notch profile
        return geometryEngine.computeEdgeCutout(placementPoint(), profileSize);
    }
}
