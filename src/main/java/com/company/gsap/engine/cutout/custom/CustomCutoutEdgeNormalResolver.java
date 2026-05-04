package com.company.gsap.engine.cutout.custom;

import java.util.List;

public class CustomCutoutEdgeNormalResolver {

    public double[] resolveOutwardNormal(List<double[]> polygonPoints, double[] targetPoint) {
        Segment nearest = findNearestSegment(polygonPoints, targetPoint);
        double dx = nearest.bx - nearest.ax;
        double dy = nearest.by - nearest.ay;
        double len = Math.hypot(dx, dy);
        if (len < 1e-9) {
            return new double[] {0.0, -1.0};
        }

        double[] n1 = new double[] {-dy / len, dx / len};
        double[] n2 = new double[] {dy / len, -dx / len};

        double[] centroid = centroid(polygonPoints);
        double mx = (nearest.ax + nearest.bx) * 0.5;
        double my = (nearest.ay + nearest.by) * 0.5;
        double toCx = centroid[0] - mx;
        double toCy = centroid[1] - my;
        double dot = n1[0] * toCx + n1[1] * toCy;
        return dot < 0.0 ? n1 : n2;
    }

    public double[] findNearestEdgeMidpoint(List<double[]> polygonPoints, double[] targetPoint) {
        Segment nearest = findNearestSegment(polygonPoints, targetPoint);
        return new double[] {(nearest.ax + nearest.bx) * 0.5, (nearest.ay + nearest.by) * 0.5};
    }

    private Segment findNearestSegment(List<double[]> polygonPoints, double[] targetPoint) {
        if (polygonPoints == null || polygonPoints.size() < 2) {
            throw new IllegalArgumentException("polygonPoints must have at least two points");
        }
        Segment best = null;
        double bestDist = Double.MAX_VALUE;
        for (int i = 0; i < polygonPoints.size(); i++) {
            double[] a = polygonPoints.get(i);
            double[] b = polygonPoints.get((i + 1) % polygonPoints.size());
            double[] p = projectToSegment(a, b, targetPoint);
            double dx = p[0] - targetPoint[0];
            double dy = p[1] - targetPoint[1];
            double d2 = dx * dx + dy * dy;
            if (d2 < bestDist) {
                bestDist = d2;
                best = new Segment(a[0], a[1], b[0], b[1]);
            }
        }
        if (best == null) {
            throw new IllegalStateException("Unable to resolve nearest segment");
        }
        return best;
    }

    private static double[] projectToSegment(double[] a, double[] b, double[] p) {
        double abx = b[0] - a[0];
        double aby = b[1] - a[1];
        double ab2 = abx * abx + aby * aby;
        if (ab2 < 1e-12) {
            return new double[] {a[0], a[1]};
        }
        double apx = p[0] - a[0];
        double apy = p[1] - a[1];
        double t = (apx * abx + apy * aby) / ab2;
        t = Math.max(0.0, Math.min(1.0, t));
        return new double[] {a[0] + t * abx, a[1] + t * aby};
    }

    private static double[] centroid(List<double[]> polygonPoints) {
        double sx = 0.0;
        double sy = 0.0;
        for (double[] p : polygonPoints) {
            sx += p[0];
            sy += p[1];
        }
        return new double[] {sx / polygonPoints.size(), sy / polygonPoints.size()};
    }

    private record Segment(double ax, double ay, double bx, double by) {
    }
}
