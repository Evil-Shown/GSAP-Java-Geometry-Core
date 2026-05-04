package com.company.gsap.engine.cutout.custom;

public record CutoutPlacement(
        double x,
        double y,
        double rotationAngleDegrees,
        double size,
        int cutoutNo
) {
    public CutoutPlacement(double x, double y, double rotationAngleDegrees, double size) {
        this(x, y, rotationAngleDegrees, size, 0);
    }
}
