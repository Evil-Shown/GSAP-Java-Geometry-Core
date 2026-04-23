package com.company.gsap.engine.model;

public record ArcEdgeDefinition(
        String id,
        Point2D center,
        double radius,
        double startAngleRadians,
        double endAngleRadians,
        boolean clockwise
) implements EdgeDefinition {
    @Override
    public Point2D start() {
        return new Point2D(
                center.x() + radius * Math.cos(startAngleRadians),
                center.y() + radius * Math.sin(startAngleRadians));
    }

    @Override
    public Point2D end() {
        return new Point2D(
                center.x() + radius * Math.cos(endAngleRadians),
                center.y() + radius * Math.sin(endAngleRadians));
    }

    public double sweep() {
        double sweep = endAngleRadians - startAngleRadians;
        if (clockwise && sweep > 0) {
            sweep -= 2 * Math.PI;
        }
        if (!clockwise && sweep < 0) {
            sweep += 2 * Math.PI;
        }
        return Math.abs(sweep);
    }

    @Override
    public double length() {
        return radius * sweep();
    }

    @Override
    public String svgPathSegment() {
        int largeArc = sweep() > Math.PI ? 1 : 0;
        int sweepFlag = clockwise ? 0 : 1;
        Point2D end = end();
        return "A " + radius + " " + radius + " 0 " + largeArc + " " + sweepFlag + " " + end.x() + " " + end.y();
    }
}
