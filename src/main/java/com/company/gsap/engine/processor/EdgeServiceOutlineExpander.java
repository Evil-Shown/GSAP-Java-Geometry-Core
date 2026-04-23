package com.company.gsap.engine.processor;

import com.company.gsap.engine.model.ArcEdgeDefinition;
import com.company.gsap.engine.model.EdgeDefinition;
import com.company.gsap.engine.model.LineEdgeDefinition;
import com.company.gsap.engine.model.Point2D;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Expands a closed chain of line and/or arc edges outward by per-edge amounts (edge services).
 * Line corners use offset-line intersections; arc segments use a concentric circular offset
 * (radius adjusted along the polygon exterior); line–arc and arc–arc junctions intersect the
 * offset line with the offset circle, or two offset circles.
 */
public final class EdgeServiceOutlineExpander {

    private static final double EPS = 1e-5;

    private EdgeServiceOutlineExpander() {
    }

    public static List<EdgeDefinition> apply(List<EdgeDefinition> edges, Map<String, Double> amounts) {
        if (edges == null || edges.isEmpty() || amounts == null || amounts.isEmpty()) {
            return edges;
        }
        boolean anyNonZero = false;
        for (Double v : amounts.values()) {
            if (v != null && Math.abs(v) > EPS) {
                anyNonZero = true;
                break;
            }
        }
        if (!anyNonZero) {
            return edges;
        }
        if (edges.size() < 3 || !isClosedChain(edges)) {
            return edges;
        }

        boolean polygonCcw = signedContourArea(edges) > 0.0;
        int n = edges.size();

        double[] d = new double[n];
        for (int i = 0; i < n; i++) {
            String id = edges.get(i).id();
            Double raw = id == null ? null : amounts.get(id);
            d[i] = raw == null ? 0.0 : Math.abs(raw);
        }

        boolean[] isLine = new boolean[n];
        Point2D[] lineBase = new Point2D[n];
        Point2D[] lineDir = new Point2D[n];
        Point2D[] arcCenter = new Point2D[n];
        double[] arcRPrime = new double[n];

        Point2D[] v = new Point2D[n];
        for (int i = 0; i < n; i++) {
            v[i] = edges.get(i).start();
        }

        for (int i = 0; i < n; i++) {
            EdgeDefinition e = edges.get(i);
            Point2D vi = v[i];
            Point2D vip1 = v[(i + 1) % n];
            if (e instanceof LineEdgeDefinition) {
                isLine[i] = true;
                Point2D dir = sub(vip1, vi);
                Point2D n0 = outwardUnit(dir, polygonCcw);
                lineBase[i] = add(vi, scale(n0, d[i]));
                lineDir[i] = dir;
                arcCenter[i] = null;
                arcRPrime[i] = 0.0;
            } else if (e instanceof ArcEdgeDefinition arc) {
                isLine[i] = false;
                lineBase[i] = null;
                lineDir[i] = null;
                arcCenter[i] = arc.center();
                arcRPrime[i] = arcRadiusPrime(arc, d[i], polygonCcw);
            } else {
                return edges;
            }
        }

        Point2D[] q = new Point2D[n];
        for (int i = 0; i < n; i++) {
            int im1 = Math.floorMod(i - 1, n);
            Point2D hint = v[i];
            q[i] = intersectJunction(
                    isLine[im1], lineBase[im1], lineDir[im1], arcCenter[im1], arcRPrime[im1],
                    isLine[i], lineBase[i], lineDir[i], arcCenter[i], arcRPrime[i],
                    hint);
        }

        List<EdgeDefinition> out = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            EdgeDefinition orig = edges.get(i);
            int ip1 = (i + 1) % n;
            if (orig instanceof LineEdgeDefinition) {
                out.add(new LineEdgeDefinition(orig.id(), q[i], q[ip1]));
            } else if (orig instanceof ArcEdgeDefinition arc) {
                out.add(new ArcEdgeDefinition(
                        orig.id(),
                        arc.center(),
                        arcRPrime[i],
                        arc.startAngleRadians(),
                        arc.endAngleRadians(),
                        arc.clockwise()));
            }
        }
        return out;
    }

    private static boolean isClosedChain(List<EdgeDefinition> edges) {
        int n = edges.size();
        for (int i = 0; i < n; i++) {
            if (!near(edges.get(i).end(), edges.get((i + 1) % n).start())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Signed area via shoelace (lines) and chordal steps along arcs so winding matches the contour.
     */
    private static double signedContourArea(List<EdgeDefinition> edges) {
        double sum = 0.0;
        for (EdgeDefinition e : edges) {
            if (e instanceof LineEdgeDefinition le) {
                sum += shoelaceCross(le.start(), le.end());
            } else if (e instanceof ArcEdgeDefinition arc) {
                double dth = signedSweepRadians(arc);
                int steps = 64;
                double th0 = arc.startAngleRadians();
                double cx = arc.center().x();
                double cy = arc.center().y();
                double r = arc.radius();
                Point2D prev = arc.start();
                for (int k = 1; k <= steps; k++) {
                    double t = k / (double) steps;
                    double th = th0 + t * dth;
                    Point2D cur = new Point2D(cx + r * Math.cos(th), cy + r * Math.sin(th));
                    sum += shoelaceCross(prev, cur);
                    prev = cur;
                }
            }
        }
        return 0.5 * sum;
    }

    private static double shoelaceCross(Point2D a, Point2D b) {
        return a.x() * b.y() - b.x() * a.y();
    }

    /**
     * Signed angular sweep from start to end following {@link ArcEdgeDefinition} SVG/walk rules.
     */
    private static double signedSweepRadians(ArcEdgeDefinition arc) {
        double raw = arc.endAngleRadians() - arc.startAngleRadians();
        if (arc.clockwise()) {
            if (raw > 0.0) {
                raw -= 2.0 * Math.PI;
            }
        } else {
            if (raw < 0.0) {
                raw += 2.0 * Math.PI;
            }
        }
        return raw;
    }

    private static double arcRadiusPrime(ArcEdgeDefinition arc, double d, boolean polygonCcw) {
        if (d < EPS) {
            return arc.radius();
        }
        double dth = signedSweepRadians(arc);
        double thm = arc.startAngleRadians() + 0.5 * dth;
        Point2D t = unitWalkTangent(thm, dth);
        Point2D nExt = exteriorNormalFromTangent(t, polygonCcw);
        double urx = Math.cos(thm);
        double ury = Math.sin(thm);
        double sign = nExt.x() * urx + nExt.y() * ury >= 0.0 ? 1.0 : -1.0;
        return Math.max(EPS, arc.radius() + sign * d);
    }

    private static Point2D unitWalkTangent(double theta, double signedSweep) {
        double tx = -Math.sin(theta);
        double ty = Math.cos(theta);
        if (signedSweep < 0.0) {
            tx = -tx;
            ty = -ty;
        }
        double len = Math.hypot(tx, ty);
        if (len < EPS) {
            return new Point2D(1.0, 0.0);
        }
        return new Point2D(tx / len, ty / len);
    }

    private static Point2D exteriorNormalFromTangent(Point2D tangent, boolean polygonCcw) {
        return polygonCcw ? new Point2D(tangent.y(), -tangent.x()) : new Point2D(-tangent.y(), tangent.x());
    }

    private static Point2D intersectJunction(
            boolean prevLine, Point2D prevLineBase, Point2D prevLineDir, Point2D prevArcC, double prevArcR,
            boolean currLine, Point2D currLineBase, Point2D currLineDir, Point2D currArcC, double currArcR,
            Point2D hint) {
        if (prevLine && currLine) {
            return intersectInfiniteLines(prevLineBase, prevLineDir, currLineBase, currLineDir);
        }
        if (prevLine && !currLine) {
            return pickClosest(hint, lineCircleIntersections(prevLineBase, prevLineDir, currArcC, currArcR));
        }
        if (!prevLine && currLine) {
            return pickClosest(hint, lineCircleIntersections(currLineBase, currLineDir, prevArcC, prevArcR));
        }
        return pickClosest(hint, circleCircleIntersections(prevArcC, prevArcR, currArcC, currArcR));
    }

    private static Point2D pickClosest(Point2D hint, List<Point2D> candidates) {
        if (candidates.isEmpty()) {
            return hint;
        }
        Point2D best = candidates.get(0);
        double bestD = dist2(best, hint);
        for (int i = 1; i < candidates.size(); i++) {
            Point2D c = candidates.get(i);
            double d2 = dist2(c, hint);
            if (d2 < bestD) {
                bestD = d2;
                best = c;
            }
        }
        return best;
    }

    private static double dist2(Point2D a, Point2D b) {
        double dx = a.x() - b.x();
        double dy = a.y() - b.y();
        return dx * dx + dy * dy;
    }

    private static List<Point2D> lineCircleIntersections(Point2D p, Point2D dir, Point2D c, double r) {
        List<Point2D> out = new ArrayList<>(2);
        if (dir == null) {
            return out;
        }
        double a = dot(dir, dir);
        if (Math.abs(a) < EPS * EPS) {
            return out;
        }
        Point2D fp = sub(p, c);
        double b = 2.0 * dot(fp, dir);
        double cq = dot(fp, fp) - r * r;
        double disc = b * b - 4.0 * a * cq;
        if (disc < -1e-8) {
            return out;
        }
        if (disc < 0.0) {
            disc = 0.0;
        }
        double sqrtD = Math.sqrt(disc);
        double inv2a = 1.0 / (2.0 * a);
        double t0 = (-b - sqrtD) * inv2a;
        double t1 = (-b + sqrtD) * inv2a;
        out.add(add(p, scale(dir, t0)));
        if (disc > 1e-12) {
            out.add(add(p, scale(dir, t1)));
        }
        return out;
    }

    private static List<Point2D> circleCircleIntersections(Point2D c0, double r0, Point2D c1, double r1) {
        List<Point2D> out = new ArrayList<>(2);
        double dx = c1.x() - c0.x();
        double dy = c1.y() - c0.y();
        double d = Math.hypot(dx, dy);
        if (d < EPS) {
            return out;
        }
        if (d > r0 + r1 + 1e-6) {
            return out;
        }
        if (d < Math.abs(r0 - r1) - 1e-6) {
            return out;
        }
        double a = (r0 * r0 - r1 * r1 + d * d) / (2.0 * d);
        double h2 = r0 * r0 - a * a;
        if (h2 < -1e-6) {
            return out;
        }
        double h = h2 <= 0.0 ? 0.0 : Math.sqrt(h2);
        double mx = c0.x() + a * dx / d;
        double my = c0.y() + a * dy / d;
        double rx = -dy * (h / d);
        double ry = dx * (h / d);
        out.add(new Point2D(mx + rx, my + ry));
        if (h > 1e-9) {
            out.add(new Point2D(mx - rx, my - ry));
        }
        return out;
    }

    private static double dot(Point2D a, Point2D b) {
        return a.x() * b.x() + a.y() * b.y();
    }

    private static boolean near(Point2D a, Point2D b) {
        return Math.hypot(a.x() - b.x(), a.y() - b.y()) <= EPS;
    }

    private static Point2D sub(Point2D a, Point2D b) {
        return new Point2D(a.x() - b.x(), a.y() - b.y());
    }

    private static Point2D add(Point2D a, Point2D b) {
        return new Point2D(a.x() + b.x(), a.y() + b.y());
    }

    private static Point2D scale(Point2D a, double s) {
        return new Point2D(a.x() * s, a.y() * s);
    }

    private static Point2D outwardUnit(Point2D edgeDir, boolean polygonCcw) {
        double dx = edgeDir.x();
        double dy = edgeDir.y();
        double len = Math.hypot(dx, dy);
        if (len < EPS) {
            return new Point2D(0.0, 0.0);
        }
        dx /= len;
        dy /= len;
        if (polygonCcw) {
            return new Point2D(dy, -dx);
        }
        return new Point2D(-dy, dx);
    }

    private static Point2D intersectInfiniteLines(Point2D p, Point2D u, Point2D q, Point2D v) {
        double cross = u.x() * v.y() - u.y() * v.x();
        if (Math.abs(cross) < EPS * EPS) {
            return q;
        }
        Point2D w = sub(q, p);
        double s = (w.x() * v.y() - w.y() * v.x()) / cross;
        return new Point2D(p.x() + s * u.x(), p.y() + s * u.y());
    }
}
