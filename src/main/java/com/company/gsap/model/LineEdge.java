package com.company.gsap.model;

/**
 * A straight line segment from start to end.
 */
public class LineEdge extends Edge {

    private final Point start;
    private final Point end;

    public LineEdge(String id, Point start, Point end) {
        super(id);
        if (start == null || end == null) {
            throw new IllegalArgumentException("LineEdge start/end cannot be null");
        }
        this.start = start;
        this.end = end;
    }

    @Override
    public Point getStart() { return start; }

    @Override
    public Point getEnd() { return end; }

    @Override
    public double length() {
        return start.distanceTo(end);
    }

    @Override
    public void validate() {
        if (length() < 1e-6) {
            throw new IllegalStateException(
                    "LineEdge [" + id + "] has zero length"
            );
        }
    }

    /**
     * Offset: returns a parallel line shifted by distance.
     * Uses the normal vector perpendicular to the line direction.
     * Positive distance = left of direction of travel.
     */
    @Override
    public Edge offset(double distance) {
        double dx = end.x - start.x;
        double dy = end.y - start.y;
        double len = length();
        double nx = -dy / len * distance; // normal x
        double ny = dx / len * distance;  // normal y
        return new LineEdge(
                id + "_offset",
                start.translate(nx, ny),
                end.translate(nx, ny)
        );
    }

    @Override
    public String toString() {
        return String.format("LineEdge[%s] %s -> %s (len=%.2f)",
                id, start, end, length());
    }
}
