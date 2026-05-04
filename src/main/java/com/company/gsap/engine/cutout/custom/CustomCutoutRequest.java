package com.company.gsap.engine.cutout.custom;

public record CustomCutoutRequest(
        String id,
        CustomCutoutType type,
        Double x,
        Double y,
        Double size,
        String edgeId,
        Double offset,
        Boolean startOffset,
        Double offsetX,
        Double offsetY
) {
}
