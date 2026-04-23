package com.company.gsap.engine.util;

import com.company.gsap.engine.model.*;

import java.util.ArrayList;
import java.util.List;

public final class GeometryMath {
    private GeometryMath() {
    }

    public static List<Point2D> toPolygonPoints(List<EdgeDefinition> edges, int arcSegments) {
        List<Point2D> points = new ArrayList<>();
        for (EdgeDefinition edge : edges) {
            if (points.isEmpty()) {
                points.add(edge.start());
            }
            if (edge instanceof ArcEdgeDefinition arc) {
                double sweep = arc.sweep();
                double direction = arc.clockwise() ? -1.0 : 1.0;
                double step = sweep / Math.max(1, arcSegments);
                for (int i = 1; i <= arcSegments; i++) {
                    double angle = arc.startAngleRadians() + direction * step * i;
                    points.add(new Point2D(
                            arc.center().x() + arc.radius() * Math.cos(angle),
                            arc.center().y() + arc.radius() * Math.sin(angle)));
                }
            } else {
                points.add(edge.end());
            }
        }
        return points;
    }

    public static double polygonArea(List<Point2D> points) {
        if (points.size() < 3) {
            return 0.0;
        }
        double sum = 0.0;
        for (int i = 0; i < points.size(); i++) {
            Point2D a = points.get(i);
            Point2D b = points.get((i + 1) % points.size());
            sum += (a.x() * b.y()) - (b.x() * a.y());
        }
        return Math.abs(sum) / 2.0;
    }
}
