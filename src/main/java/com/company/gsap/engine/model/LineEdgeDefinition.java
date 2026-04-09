package com.company.gsap.engine.model;

public record LineEdgeDefinition(String id, Point2D start, Point2D end) implements EdgeDefinition {
    @Override
    public double length() {
        return Math.hypot(end.x() - start.x(), end.y() - start.y());
    }

    @Override
    public String svgPathSegment() {
        return "L " + end.x() + " " + end.y();
    }
}
