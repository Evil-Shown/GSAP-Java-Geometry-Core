package com.company.gsap.engine.cutout.custom;

public record CustomCutoutRequest(
        String id,
        CustomCutoutType type,
        double x,
        double y,
        double size
) {
}
