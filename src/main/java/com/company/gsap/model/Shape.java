package com.company.gsap.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Top-level domain object representing one glass shape.
 *
 * Pipeline:
 *   JSON → ShapeLoader → Shape → GeometryValidator → OffsetEngine → Optimizer
 */
public class Shape {

    private final String name;
    private final String version;
    private final double thickness; // glass thickness in mm
    private final List<Edge> edges;

    public Shape(String name, String version, double thickness) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Shape name cannot be blank");
        this.name = name;
        this.version = version;
        this.thickness = thickness;
        this.edges = new ArrayList<>();
    }

    public void addEdge(Edge edge) {
        if (edge == null) throw new IllegalArgumentException("Cannot add null edge");
        edges.add(edge);
    }

    /** Read-only view of edges — callers cannot modify the list */
    public List<Edge> getEdges() {
        return Collections.unmodifiableList(edges);
    }

    public int getEdgeCount() { return edges.size(); }

    /**
     * Total perimeter = sum of all edge arc lengths.
     */
    public double getPerimeter() {
        return edges.stream().mapToDouble(Edge::length).sum();
    }

    /**
     * Axis-aligned bounding box.
     * Returns double[] { minX, minY, maxX, maxY }
     *
     * Note: uses start/end points only — accurate for lines,
     * approximate for arcs (will improve in Phase 2).
     */
    public double[] getBoundingBox() {
        if (edges.isEmpty()) {
            return new double[]{ 0, 0, 0, 0 };
        }
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
        for (Edge e : edges) {
            for (Point p : new Point[]{ e.getStart(), e.getEnd() }) {
                if (p.x < minX) minX = p.x;
                if (p.y < minY) minY = p.y;
                if (p.x > maxX) maxX = p.x;
                if (p.y > maxY) maxY = p.y;
            }
        }
        return new double[]{ minX, minY, maxX, maxY };
    }

    /**
     * Validates each edge individually.
     * Full shape validation (closed? connected?) → GeometryValidator (Step 3).
     */
    public void validateEdges() {
        for (Edge e : edges) e.validate();
    }

    public String getName() { return name; }
    public String getVersion() { return version; }
    public double getThickness() { return thickness; }

    @Override
    public String toString() {
        double[] bb = getBoundingBox();
        return String.format(
                "Shape[%s] edges=%d perimeter=%.2fmm thickness=%.1fmm bbox=[%.1f,%.1f -> %.1f,%.1f]",
                name, edges.size(), getPerimeter(), thickness,
                bb[0], bb[1], bb[2], bb[3]);
    }
}
