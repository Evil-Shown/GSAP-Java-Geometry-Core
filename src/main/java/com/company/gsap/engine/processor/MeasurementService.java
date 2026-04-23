package com.company.gsap.engine.processor;

import com.company.gsap.engine.model.EdgeDefinition;
import com.company.gsap.engine.model.Point2D;
import com.company.gsap.engine.model.ShapeMetrics;
import com.company.gsap.engine.util.GeometryMath;

import java.util.List;

public class MeasurementService {

    public ShapeMetrics measure(List<EdgeDefinition> edges) {
        double perimeter = edges.stream().mapToDouble(EdgeDefinition::length).sum();
        List<Point2D> polygonPoints = GeometryMath.toPolygonPoints(edges, 24);
        double area = GeometryMath.polygonArea(polygonPoints);

        double minX = polygonPoints.stream().mapToDouble(Point2D::x).min().orElse(0.0);
        double minY = polygonPoints.stream().mapToDouble(Point2D::y).min().orElse(0.0);
        double maxX = polygonPoints.stream().mapToDouble(Point2D::x).max().orElse(0.0);
        double maxY = polygonPoints.stream().mapToDouble(Point2D::y).max().orElse(0.0);
        return new ShapeMetrics(perimeter, area, minX, minY, maxX, maxY);
    }
}
