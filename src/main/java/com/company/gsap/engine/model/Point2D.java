package com.company.gsap.engine.model;

public record Point2D(double x, double y) {
    public Point2D scale(double factor) {
        return new Point2D(x * factor, y * factor);
    }

    public Point2D rotateRadians(double radians) {
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        return new Point2D(x * cos - y * sin, x * sin + y * cos);
    }

    public Point2D flip(boolean horizontal, boolean vertical) {
        double nextX = horizontal ? -x : x;
        double nextY = vertical ? -y : y;
        return new Point2D(nextX, nextY);
    }
}
