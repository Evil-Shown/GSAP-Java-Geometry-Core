package com.company.gsap.engine.cutout.custom;

import java.util.List;

public record CustomCutoutProfile(
        String cutoutNo,
        CustomCutoutType type,
        List<String> paramNames,
        String implementationClass
) {
}
