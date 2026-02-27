package com.company.gsap.model;

/**
 * Immutable 2D point in millimeter coordinate space.
 * Used by all edge types as start, end, and center references.
 */
public class Point {

    public final double x;
    public final double y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Euclidean distance to another point.
     */
    public double distanceTo(Point other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Returns true if this point is within epsilon of another.
     * Use this instead of == to avoid floating-point errors.
     */
    public boolean isCloseTo(Point other, double epsilon) {
        return distanceTo(other) <= epsilon;
    }

    /**
     * Returns a NEW point shifted by (dx, dy).
     */
    public Point translate(double dx, double dy) {
        return new Point(this.x + dx, this.y + dy);
    }

    @Override
    public String toString() {
        return String.format("Point(%.4f, %.4f)", x, y);
    }
}
