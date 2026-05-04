package com.company.gsap.engine.cutout.custom;

import java.util.List;
import java.util.Map;

public final class CustomCutoutProfiles {
    private CustomCutoutProfiles() {
    }

    private static final Map<String, CustomCutoutProfile> BY_ID = Map.ofEntries(
            Map.entry("100", new CustomCutoutProfile("100", CustomCutoutType.EDGE, List.of("W", "H"), "CustomCutout_100")),
            Map.entry("101", new CustomCutoutProfile("101", CustomCutoutType.EDGE, List.of("W", "H", "R"), "CustomCutout_101")),
            Map.entry("102", new CustomCutoutProfile("102", CustomCutoutType.CORNER, List.of("R"), "CustomCutout_102")),
            Map.entry("103", new CustomCutoutProfile("103", CustomCutoutType.EDGE, List.of("W", "H"), "CustomCutout_103")),
            Map.entry("104", new CustomCutoutProfile("104", CustomCutoutType.EDGE, List.of("W", "R"), "CustomCutout_104")),
            Map.entry("105", new CustomCutoutProfile("105", CustomCutoutType.CORNER, List.of("R"), "CustomCutout_105")),
            Map.entry("106", new CustomCutoutProfile("106", CustomCutoutType.CORNER, List.of("W", "H", "R"), "CustomCutout_106")),
            Map.entry("107", new CustomCutoutProfile("107", CustomCutoutType.INTERIOR, List.of("W", "H", "R", "A"), "CustomCutout_107")),
            Map.entry("108", new CustomCutoutProfile("108", CustomCutoutType.INTERIOR, List.of("W", "R", "A"), "CustomCutout_108")),
            Map.entry("109", new CustomCutoutProfile("109", CustomCutoutType.INTERIOR, List.of("R"), "CustomCutout_109")),
            Map.entry("110", new CustomCutoutProfile("110", CustomCutoutType.INTERIOR, List.of("H", "R", "A"), "CustomCutout_110")),
            Map.entry("111", new CustomCutoutProfile("111", CustomCutoutType.CORNER, List.of("W", "H", "R1", "R2"), "CustomCutout_111"))
    );

    public static CustomCutoutProfile resolve(String cutoutNo) {
        if (cutoutNo == null) {
            return null;
        }
        return BY_ID.get(cutoutNo.trim());
    }
}
