package com.company.gsap.model;

/**
 * A circular arc defined by:
 *   center     — the circle center point
 *   radius     — circle radius in mm
 *   startAngle — angle in RADIANS where arc begins
 *   endAngle   — angle in RADIANS where arc ends
 *   clockwise  — direction of travel
 *
 * Angle convention (standard math):
 *   0      = right  (3 o'clock)
 *   PI/2   = up     (12 o'clock)
 *   PI     = left   (9 o'clock)
 *   3*PI/2 = down   (6 o'clock)
 */
public class ArcEdge extends Edge {

    private final Point center;
    private final double radius;
    private final double startAngle; // radians
    private final double endAngle;   // radians
    private final boolean clockwise;

    public ArcEdge(String id, Point center, double radius,
                   double startAngle, double endAngle, boolean clockwise) {
        super(id);
        if (center == null) throw new IllegalArgumentException("ArcEdge center is null");
        if (radius <= 0) throw new IllegalArgumentException("ArcEdge radius must be > 0");
        this.center = center;
        this.radius = radius;
        this.startAngle = startAngle;
        this.endAngle = endAngle;
        this.clockwise = clockwise;
    }

    /** Point on circle at startAngle */
    @Override
    public Point getStart() {
        return new Point(
                center.x + radius * Math.cos(startAngle),
                center.y + radius * Math.sin(startAngle)
        );
    }

    /** Point on circle at endAngle */
    @Override
    public Point getEnd() {
        return new Point(
                center.x + radius * Math.cos(endAngle),
                center.y + radius * Math.sin(endAngle)
        );
    }

    /** Arc length = radius × sweep angle */
    @Override
    public double length() {
        return radius * sweepAngle();
    }

    /**
     * Sweep angle in radians (always positive).
     * Accounts for clockwise vs counter-clockwise direction.
     */
    public double sweepAngle() {
        double sweep = endAngle - startAngle;
        if (clockwise && sweep > 0) sweep -= 2 * Math.PI;
        if (!clockwise && sweep < 0) sweep += 2 * Math.PI;
        return Math.abs(sweep);
    }

    @Override
    public void validate() {
        if (radius < 1e-6)
            throw new IllegalStateException("ArcEdge [" + id + "] zero radius");
        if (sweepAngle() < 1e-6)
            throw new IllegalStateException("ArcEdge [" + id + "] zero sweep angle");
    }

    /** Offset arc: expand or shrink radius */
    @Override
    public Edge offset(double distance) {
        double newR = clockwise ? radius - distance : radius + distance;
        if (newR <= 0)
            throw new IllegalStateException("ArcEdge [" + id + "] collapses on offset");
        return new ArcEdge(id + "_offset", center, newR,
                startAngle, endAngle, clockwise);
    }

    public Point getCenter() { return center; }
    public double getRadius() { return radius; }
    public double getStartAngle() { return startAngle; }
    public double getEndAngle() { return endAngle; }
    public boolean isClockwise() { return clockwise; }

    @Override
    public String toString() {
        return String.format(
                "ArcEdge[%s] center=%s r=%.2f sweep=%.1f deg (len=%.2f)",
                id, center, radius, Math.toDegrees(sweepAngle()), length());
    }
}
