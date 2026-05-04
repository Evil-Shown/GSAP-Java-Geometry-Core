package com.company.gsap.engine.cutout.custom;

import java.util.List;

public class CustomCutoutGeometryEngine {
    private final List<double[]> polygonPoints;
    private final CustomCutoutEdgeNormalResolver edgeNormalResolver;

    public CustomCutoutGeometryEngine(List<double[]> polygonPoints) {
        this.polygonPoints = polygonPoints;
        this.edgeNormalResolver = new CustomCutoutEdgeNormalResolver();
    }

    public CutoutPlacement computeEdgeCutout(double[] placementPoint, double size) {
        double[] midpoint = edgeNormalResolver.findNearestEdgeMidpoint(polygonPoints, placementPoint);
        double[] normal = edgeNormalResolver.resolveOutwardNormal(polygonPoints, placementPoint);
        double angle = Math.toDegrees(Math.atan2(normal[1], normal[0]));
        return new CutoutPlacement(midpoint[0], midpoint[1], angle, size);
    }

    public CutoutPlacement computeInteriorCutout(double[] placementPoint, double size) {
        double[] c = centroid(polygonPoints);
        double angle = Math.toDegrees(Math.atan2(placementPoint[1] - c[1], placementPoint[0] - c[0]));
        return new CutoutPlacement(placementPoint[0], placementPoint[1], angle, size);
    }

    public CutoutPlacement computeCornerCutout(double[] placementPoint, double size) {
        int nearest = nearestVertexIndex(polygonPoints, placementPoint);
        int prev = (nearest - 1 + polygonPoints.size()) % polygonPoints.size();
        int next = (nearest + 1) % polygonPoints.size();

        double[] p = polygonPoints.get(prev);
        double[] c = polygonPoints.get(nearest);
        double[] n = polygonPoints.get(next);

        double[] v1 = normalize(new double[] {p[0] - c[0], p[1] - c[1]});
        double[] v2 = normalize(new double[] {n[0] - c[0], n[1] - c[1]});
        double[] bisector = normalize(new double[] {v1[0] + v2[0], v1[1] + v2[1]});
        double angle = Math.toDegrees(Math.atan2(bisector[1], bisector[0]));

        return new CutoutPlacement(c[0], c[1], angle, size);
    }

    private static int nearestVertexIndex(List<double[]> points, double[] target) {
        int idx = 0;
        double min = Double.MAX_VALUE;
        for (int i = 0; i < points.size(); i++) {
            double dx = points.get(i)[0] - target[0];
            double dy = points.get(i)[1] - target[1];
            double d2 = dx * dx + dy * dy;
            if (d2 < min) {
                min = d2;
                idx = i;
            }
        }
        return idx;
    }

    private static double[] normalize(double[] v) {
        double len = Math.hypot(v[0], v[1]);
        if (len < 1e-9) {
            return new double[] {1.0, 0.0};
        }
        return new double[] {v[0] / len, v[1] / len};
    }

    private static double[] centroid(List<double[]> points) {
        double sx = 0.0;
        double sy = 0.0;
        for (double[] p : points) {
            sx += p[0];
            sy += p[1];
        }
        return new double[] {sx / points.size(), sy / points.size()};
    }
}
