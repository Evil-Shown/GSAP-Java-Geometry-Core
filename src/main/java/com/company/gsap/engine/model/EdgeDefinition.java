package com.company.gsap.engine.model;

public interface EdgeDefinition {
    String id();
    Point2D start();
    Point2D end();
    double length();
    String svgPathSegment();
}
