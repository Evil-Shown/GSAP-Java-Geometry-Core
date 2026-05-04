package com.company.gsap.engine.cutout.custom;

import java.util.Map;

public record CustomCutoutRequest(
        String id,
        String cutoutNo,
        CustomCutoutType type,
        Double x,
        Double y,
        Double size,
        String edgeId,
        Double offset,
        Boolean startOffset,
        Double offsetX,
        Double offsetY,
        Map<String, Double> params
) {
}
